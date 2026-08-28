-- ============================================================================
-- TourOS Migration: 20260828_005_create_club_members_report_rpc.sql
-- Description: Axileto Club Üye Raporu ve Müşteri Eğilim Analizi RPC Fonksiyonu
-- ============================================================================

CREATE OR REPLACE FUNCTION public.get_club_members_report(
    p_tier TEXT DEFAULT NULL,
    p_budget_range TEXT DEFAULT NULL,
    p_travel_group TEXT DEFAULT NULL,
    p_concept TEXT DEFAULT NULL,
    p_destination TEXT DEFAULT NULL,
    p_city TEXT DEFAULT NULL,
    p_search TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_clean_tier TEXT := NULLIF(TRIM(p_tier), '');
    v_clean_budget TEXT := NULLIF(TRIM(p_budget_range), '');
    v_clean_group TEXT := NULLIF(TRIM(p_travel_group), '');
    v_clean_concept TEXT := NULLIF(TRIM(p_concept), '');
    v_clean_destination TEXT := NULLIF(TRIM(p_destination), '');
    v_clean_city TEXT := NULLIF(TRIM(p_city), '');
    v_clean_search TEXT := NULLIF(LOWER(TRIM(p_search)), '');
    
    v_silver_min INT := 0;
    v_gold_min INT := 2000;
    v_platinum_min INT := 5000;
    
    v_members JSONB := '[]'::jsonb;
    v_kpis JSONB := '{}'::jsonb;
    v_total_members INT := 0;
    v_total_spent NUMERIC(12,2) := 0.0;
    v_dominant_budget TEXT := '150.000 - 250.000 RUB';
    v_dominant_group TEXT := 'Aile (Çocuklu)';
    v_top_destination TEXT := 'Türkiye • Antalya';
    v_top_concept TEXT := 'Ultra Her Şey Dahil';
BEGIN
    -- Tier Ayarlarını Al
    SELECT 
        COALESCE(silver_points_min, 0),
        COALESCE(gold_points_min, 2000),
        COALESCE(platinum_points_min, 5000)
    INTO v_silver_min, v_gold_min, v_platinum_min
    FROM public.club_vip_settings
    WHERE is_active = true
    LIMIT 1;

    -- Geçici Hesaplama Tablosu ile Üyeleri ve Rezervasyon Harcamalarını Birleştir
    WITH member_stats AS (
        SELECT
            m.email,
            COALESCE(m.phone, '') AS phone,
            COALESCE(m.full_name, split_part(m.email, '@', 1)) AS full_name,
            COALESCE(m.passport_no, '') AS passport_no,
            COALESCE(m.city, '') AS city,
            COALESCE(m.budget_range, '150.000 - 250.000 RUB') AS budget_range,
            COALESCE(m.travel_group, 'Aile (Çocuklu)') AS travel_group,
            COALESCE(m.holiday_concepts, ARRAY['Ultra Her Şey Dahil']) AS holiday_concepts,
            COALESCE(m.transport_preferences, ARRAY['Direkt Uçuş']) AS transport_preferences,
            COALESCE(m.favorite_destinations, ARRAY['Türkiye • Antalya']) AS favorite_destinations,
            COALESCE(m.special_requests, ARRAY[]::TEXT[]) AS special_requests,
            COALESCE(m.avatar_url, '') AS avatar_url,
            COALESCE(m.updated_at, m.created_at, now()) AS updated_at,
            
            -- Rezervasyon İstatistikleri
            COALESCE(SUM(b.total_amount) FILTER (WHERE LOWER(b.status) IN ('onaylandi', 'confirmed', 'tamamlandi', 'completed')), 0.0) AS total_spent,
            COUNT(b.id) AS total_bookings,
            (COALESCE(SUM(b.total_amount) FILTER (WHERE LOWER(b.status) IN ('onaylandi', 'confirmed', 'tamamlandi', 'completed')), 0.0) * 0.05)::INT AS points
        FROM public.member_travel_preferences m
        LEFT JOIN public.bookings b ON (
            (b.customer_email IS NOT NULL AND LOWER(TRIM(b.customer_email)) = LOWER(TRIM(m.email))) OR
            (b.customer_phone IS NOT NULL AND m.phone IS NOT NULL AND REGEXP_REPLACE(b.customer_phone, '[^0-9]', '', 'g') = REGEXP_REPLACE(m.phone, '[^0-9]', '', 'g'))
        )
        GROUP BY m.email, m.phone, m.full_name, m.passport_no, m.city, m.budget_range, m.travel_group, m.holiday_concepts, m.transport_preferences, m.favorite_destinations, m.special_requests, m.avatar_url, m.updated_at, m.created_at
    ),
    member_tiers AS (
        SELECT 
            s.*,
            CASE 
                WHEN s.points >= v_platinum_min THEN 'PLATINUM'
                WHEN s.points >= v_gold_min THEN 'GOLD'
                ELSE 'SILVER'
            END AS tier_code,
            CASE 
                WHEN s.points >= v_platinum_min THEN 'Platinum VIP'
                WHEN s.points >= v_gold_min THEN 'Gold Üye'
                ELSE 'Silver Üye'
            END AS tier_name
        FROM member_stats s
    ),
    filtered_members AS (
        SELECT *
        FROM member_tiers t
        WHERE (v_clean_tier IS NULL OR UPPER(v_clean_tier) = 'ALL' OR t.tier_code = UPPER(v_clean_tier))
          AND (v_clean_budget IS NULL OR UPPER(v_clean_budget) = 'ALL' OR t.budget_range ILIKE '%' || v_clean_budget || '%')
          AND (v_clean_group IS NULL OR UPPER(v_clean_group) = 'ALL' OR t.travel_group ILIKE '%' || v_clean_group || '%')
          AND (v_clean_concept IS NULL OR UPPER(v_clean_concept) = 'ALL' OR v_clean_concept = ANY(t.holiday_concepts))
          AND (v_clean_destination IS NULL OR UPPER(v_clean_destination) = 'ALL' OR v_clean_destination = ANY(t.favorite_destinations))
          AND (v_clean_city IS NULL OR UPPER(v_clean_city) = 'ALL' OR t.city ILIKE '%' || v_clean_city || '%')
          AND (v_clean_search IS NULL OR (
                t.full_name ILIKE '%' || v_clean_search || '%' OR
                t.email ILIKE '%' || v_clean_search || '%' OR
                t.phone ILIKE '%' || v_clean_search || '%' OR
                t.city ILIKE '%' || v_clean_search || '%'
          ))
    )
    SELECT 
        COALESCE(jsonb_agg(
            jsonb_build_object(
                'email', fm.email,
                'phone', fm.phone,
                'full_name', fm.full_name,
                'passport_no', fm.passport_no,
                'city', fm.city,
                'budget_range', fm.budget_range,
                'travel_group', fm.travel_group,
                'holiday_concepts', to_jsonb(fm.holiday_concepts),
                'transport_preferences', to_jsonb(fm.transport_preferences),
                'favorite_destinations', to_jsonb(fm.favorite_destinations),
                'special_requests', to_jsonb(fm.special_requests),
                'avatar_url', fm.avatar_url,
                'updated_at', to_char(fm.updated_at, 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                'total_spent', fm.total_spent,
                'total_bookings', fm.total_bookings,
                'points', fm.points,
                'tier_code', fm.tier_code,
                'tier_name', fm.tier_name
            ) ORDER BY fm.updated_at DESC
        ), '[]'::jsonb),
        COUNT(*),
        COALESCE(SUM(fm.total_spent), 0.0)
    INTO v_members, v_total_members, v_total_spent
    FROM filtered_members fm;

    -- Baskın Eğilimleri Hesapla
    SELECT budget_range INTO v_dominant_budget
    FROM public.member_travel_preferences
    GROUP BY budget_range
    ORDER BY count(*) DESC
    LIMIT 1;

    SELECT travel_group INTO v_dominant_group
    FROM public.member_travel_preferences
    GROUP BY travel_group
    ORDER BY count(*) DESC
    LIMIT 1;

    SELECT unnest(favorite_destinations) INTO v_top_destination
    FROM public.member_travel_preferences
    GROUP BY 1
    ORDER BY count(*) DESC
    LIMIT 1;

    SELECT unnest(holiday_concepts) INTO v_top_concept
    FROM public.member_travel_preferences
    GROUP BY 1
    ORDER BY count(*) DESC
    LIMIT 1;

    v_kpis := jsonb_build_object(
        'total_members', v_total_members,
        'total_spent', v_total_spent,
        'dominant_budget', COALESCE(v_dominant_budget, '150.000 - 250.000 RUB'),
        'dominant_group', COALESCE(v_dominant_group, 'Aile (Çocuklu)'),
        'top_destination', COALESCE(v_top_destination, 'Türkiye • Antalya'),
        'top_concept', COALESCE(v_top_concept, 'Ultra Her Şey Dahil')
    );

    RETURN jsonb_build_object(
        'success', true,
        'kpis', v_kpis,
        'members', v_members
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_club_members_report TO anon, authenticated, service_role;
NOTIFY pgrst, 'reload schema';

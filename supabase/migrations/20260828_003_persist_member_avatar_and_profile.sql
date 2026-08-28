-- ============================================================================
-- TourOS Migration: 20260828_003_persist_member_avatar_and_profile.sql
-- Description: Persist Member Avatar, Profile & Travel Preferences in DB and Storage
-- ============================================================================

-- 1. Tabloya avatar_url Kolonunu Ekle
ALTER TABLE public.member_travel_preferences 
ADD COLUMN IF NOT EXISTS avatar_url TEXT;

-- 2. Storage Avatars Bucket ve İzinleri
INSERT INTO storage.buckets (id, name, public) 
VALUES ('avatars', 'avatars', true) 
ON CONFLICT (id) DO UPDATE SET public = true;

DO $$ BEGIN
    DROP POLICY IF EXISTS "Public avatars access" ON storage.objects;
    DROP POLICY IF EXISTS "Allow avatar uploads" ON storage.objects;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

CREATE POLICY "Public avatars access" ON storage.objects FOR SELECT USING (bucket_id = 'avatars');
CREATE POLICY "Allow avatar uploads" ON storage.objects FOR ALL USING (bucket_id = 'avatars') WITH CHECK (bucket_id = 'avatars');

-- 3. Hızlı Atomik Avatar Kaydetme RPC
CREATE OR REPLACE FUNCTION public.save_member_avatar(
    p_email TEXT,
    p_avatar_url TEXT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_clean_email TEXT := LOWER(TRIM(COALESCE(p_email, '')));
BEGIN
    IF v_clean_email = '' THEN
        RETURN jsonb_build_object('success', false, 'error', 'Email is required');
    END IF;

    INSERT INTO public.member_travel_preferences (
        email, avatar_url, updated_at
    )
    VALUES (
        v_clean_email, p_avatar_url, now()
    )
    ON CONFLICT (tenant_id, email) DO UPDATE SET
        avatar_url = EXCLUDED.avatar_url,
        updated_at = now();

    RETURN jsonb_build_object('success', true, 'email', v_clean_email, 'avatar_url', p_avatar_url);
END;
$$;

GRANT EXECUTE ON FUNCTION public.save_member_avatar(TEXT, TEXT) TO anon, authenticated, service_role;

-- 4. Kapsamlı Profil ve Tercih Kaydetme RPC (Eski İmzaları Temizle)
DROP FUNCTION IF EXISTS public.save_member_travel_preferences(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT[], TEXT[], TEXT[], TEXT, TEXT, TEXT[], TEXT[]);
DROP FUNCTION IF EXISTS public.save_member_travel_preferences(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT[], TEXT[], TEXT[], TEXT[], TEXT);

CREATE OR REPLACE FUNCTION public.save_member_travel_preferences(
    p_email TEXT,
    p_phone TEXT DEFAULT NULL,
    p_full_name TEXT DEFAULT NULL,
    p_passport_no TEXT DEFAULT NULL,
    p_city TEXT DEFAULT NULL,
    p_budget_range TEXT DEFAULT '150.000 - 250.000 RUB',
    p_travel_group TEXT DEFAULT 'Aile (Çocuklu)',
    p_holiday_concepts TEXT[] DEFAULT '{}',
    p_transport_preferences TEXT[] DEFAULT '{}',
    p_favorite_destinations TEXT[] DEFAULT '{}',
    p_special_requests TEXT[] DEFAULT '{}',
    p_avatar_url TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_clean_email TEXT := LOWER(TRIM(COALESCE(p_email, '')));
BEGIN
    IF v_clean_email = '' THEN
        RETURN jsonb_build_object('success', false, 'error', 'Email is required');
    END IF;

    INSERT INTO public.member_travel_preferences (
        email, phone, full_name, passport_no, city,
        budget_range, travel_group, holiday_concepts,
        transport_preferences, favorite_destinations, special_requests,
        avatar_url, updated_at
    )
    VALUES (
        v_clean_email, p_phone, p_full_name, p_passport_no, p_city,
        p_budget_range, p_travel_group, p_holiday_concepts,
        p_transport_preferences, p_favorite_destinations, p_special_requests,
        NULLIF(p_avatar_url, ''), now()
    )
    ON CONFLICT (tenant_id, email) DO UPDATE SET
        phone = COALESCE(EXCLUDED.phone, member_travel_preferences.phone),
        full_name = COALESCE(EXCLUDED.full_name, member_travel_preferences.full_name),
        passport_no = COALESCE(EXCLUDED.passport_no, member_travel_preferences.passport_no),
        city = COALESCE(EXCLUDED.city, member_travel_preferences.city),
        budget_range = COALESCE(EXCLUDED.budget_range, member_travel_preferences.budget_range),
        travel_group = COALESCE(EXCLUDED.travel_group, member_travel_preferences.travel_group),
        holiday_concepts = COALESCE(EXCLUDED.holiday_concepts, member_travel_preferences.holiday_concepts),
        transport_preferences = COALESCE(EXCLUDED.transport_preferences, member_travel_preferences.transport_preferences),
        favorite_destinations = COALESCE(EXCLUDED.favorite_destinations, member_travel_preferences.favorite_destinations),
        special_requests = COALESCE(EXCLUDED.special_requests, member_travel_preferences.special_requests),
        avatar_url = COALESCE(NULLIF(EXCLUDED.avatar_url, ''), member_travel_preferences.avatar_url),
        updated_at = now();

    RETURN jsonb_build_object('success', true, 'email', v_clean_email);
END;
$$;

GRANT EXECUTE ON FUNCTION public.save_member_travel_preferences(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT[], TEXT[], TEXT[], TEXT[], TEXT) TO anon, authenticated, service_role;

-- 5. Dashboard Summary RPC (Üye Profil & Avatar Verisi Dahil)
CREATE OR REPLACE FUNCTION public.get_member_vip_dashboard_summary(
    p_email TEXT DEFAULT NULL,
    p_phone TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_clean_email TEXT := LOWER(TRIM(COALESCE(p_email, '')));
    v_clean_phone TEXT := REGEXP_REPLACE(COALESCE(p_phone, ''), '[^0-9]', '', 'g');
    v_total_bookings INT := 0;
    v_pending_bookings INT := 0;
    v_completed_bookings INT := 0;
    v_total_spent NUMERIC(12,2) := 0.0;
    v_points INT := 0;
    v_tier_code TEXT := 'SILVER';
    v_tier_name TEXT := 'Silver Üye';
    v_next_tier_points INT := 2000;
    v_upcoming_bookings JSONB := '[]'::jsonb;
    v_settings JSONB;
    v_member_profile JSONB := NULL;
BEGIN
    -- VIP Ayarlarını Al
    SELECT jsonb_build_object(
        'silver_points_min', COALESCE(silver_points_min, 0),
        'gold_points_min', COALESCE(gold_points_min, 2000),
        'platinum_points_min', COALESCE(platinum_points_min, 5000),
        'hero_title', COALESCE(hero_title, 'Yaza Özel Fırsatlar'),
        'hero_subtitle', COALESCE(hero_subtitle, 'Erken rezervasyon fırsatlarını kaçırma!'),
        'hero_image_url', COALESCE(hero_image_url, 'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80'),
        'hero_button_text', COALESCE(hero_button_text, 'Teklifleri Keşfet')
    ) INTO v_settings
    FROM public.club_vip_settings
    WHERE is_active = true
    LIMIT 1;

    -- Kullanıcı Rezervasyon İstatistikleri
    IF v_clean_email <> '' OR v_clean_phone <> '' THEN
        SELECT
            COUNT(*),
            COUNT(*) FILTER (WHERE LOWER(status) IN ('bekliyor', 'pending', 'islemde', 'processing')),
            COUNT(*) FILTER (WHERE LOWER(status) IN ('onaylandi', 'confirmed', 'tamamlandi', 'completed')),
            COALESCE(SUM(COALESCE(total_price, 0)) FILTER (WHERE LOWER(status) IN ('onaylandi', 'confirmed', 'tamamlandi', 'completed')), 0)
        INTO
            v_total_bookings,
            v_pending_bookings,
            v_completed_bookings,
            v_total_spent
        FROM public.bookings
        WHERE (v_clean_email <> '' AND LOWER(TRIM(COALESCE(customer_email, ''))) = v_clean_email)
           OR (v_clean_phone <> '' AND REGEXP_REPLACE(COALESCE(customer_phone, ''), '[^0-9]', '', 'g') = v_clean_phone);

        -- Puan ve Tier Hesaplama
        v_points := 250 + (v_total_spent / 100)::INT;
        
        IF v_points >= 5000 THEN
            v_tier_code := 'PLATINUM';
            v_tier_name := 'Platinum VIP';
            v_next_tier_points := 10000;
        ELSIF v_points >= 2000 THEN
            v_tier_code := 'GOLD';
            v_tier_name := 'Gold Üye';
            v_next_tier_points := 5000;
        ELSE
            v_tier_code := 'SILVER';
            v_tier_name := 'Silver Üye';
            v_next_tier_points := 2000;
        END IF;

        -- Yaklaşan / Son Rezervasyonlar (İlk 5)
        SELECT COALESCE(jsonb_agg(b), '[]'::jsonb)
        INTO v_upcoming_bookings
        FROM (
            SELECT
                id,
                COALESCE(booking_code, 'AXI-' || SUBSTRING(id::text, 1, 8)) AS booking_code,
                customer_name,
                customer_email,
                customer_phone,
                total_price,
                COALESCE(currency, 'RUB') AS currency,
                COALESCE(status, 'Bekliyor') AS status,
                operator_name,
                check_in_date,
                check_out_date,
                room_type_name,
                COALESCE(nights, 7) AS nights,
                COALESCE(pax_count, 2) AS pax_count
            FROM public.bookings
            WHERE (v_clean_email <> '' AND LOWER(TRIM(COALESCE(customer_email, ''))) = v_clean_email)
               OR (v_clean_phone <> '' AND REGEXP_REPLACE(COALESCE(customer_phone, ''), '[^0-9]', '', 'g') = v_clean_phone)
            ORDER BY created_at DESC
            LIMIT 5
        ) b;

        -- Kaydedilmiş Profil ve Tercihleri Çek
        SELECT jsonb_build_object(
            'avatar_url', COALESCE(avatar_url, ''),
            'full_name', COALESCE(full_name, ''),
            'phone', COALESCE(phone, ''),
            'passport_no', COALESCE(passport_no, ''),
            'city', COALESCE(city, ''),
            'budget_range', COALESCE(budget_range, '150.000 - 250.000 RUB'),
            'travel_group', COALESCE(travel_group, 'Aile (Çocuklu)'),
            'holiday_concepts', COALESCE(to_jsonb(holiday_concepts), '[]'::jsonb),
            'transport_preferences', COALESCE(to_jsonb(transport_preferences), '[]'::jsonb),
            'favorite_destinations', COALESCE(to_jsonb(favorite_destinations), '[]'::jsonb),
            'special_requests', COALESCE(to_jsonb(special_requests), '[]'::jsonb)
        ) INTO v_member_profile
        FROM public.member_travel_preferences
        WHERE email = v_clean_email
        LIMIT 1;
    END IF;

    RETURN jsonb_build_object(
        'total_bookings', v_total_bookings,
        'pending_bookings', v_pending_bookings,
        'completed_bookings', v_completed_bookings,
        'total_spent', v_total_spent,
        'points', v_points,
        'tier_code', v_tier_code,
        'tier_name', v_tier_name,
        'next_tier_points', v_next_tier_points,
        'upcoming_bookings', v_upcoming_bookings,
        'settings', v_settings,
        'member_profile', v_member_profile
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_member_vip_dashboard_summary(TEXT, TEXT) TO anon, authenticated, service_role;

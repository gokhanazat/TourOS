-- ============================================================================
-- TourOS Migration: 20260828_001_create_club_vip_dashboard_and_settings.sql
-- Description: Axileto Club VIP Dashboard, Loyalty/Tier Settings & Single RPC
-- ============================================================================

-- 1. Club VIP Settings Tablosu
CREATE TABLE IF NOT EXISTS public.club_vip_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    silver_points_min INT DEFAULT 0,
    gold_points_min INT DEFAULT 2000,
    platinum_points_min INT DEFAULT 5000,
    point_earning_rate NUMERIC(5,2) DEFAULT 0.05, -- Harcamanın %5'i puan
    hero_title VARCHAR(255) DEFAULT 'Yaza Özel Fırsatlar',
    hero_subtitle TEXT DEFAULT 'Erken rezervasyon fırsatlarını kaçırma! Axileto Club üyelerine özel ek indirimler.',
    hero_image_url TEXT DEFAULT 'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80',
    hero_button_text VARCHAR(100) DEFAULT 'Teklifleri Keşfet',
    hero_button_link VARCHAR(255) DEFAULT '#offers',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- İlk varsayılan VIP ayar kaydı
INSERT INTO public.club_vip_settings (
    id, tenant_id, silver_points_min, gold_points_min, platinum_points_min,
    hero_title, hero_subtitle, hero_image_url, hero_button_text
) VALUES (
    'c10b0000-0000-0000-0000-000000000001'::uuid,
    '00000000-0000-0000-0000-000000000001'::uuid,
    0, 2000, 5000,
    'Yaza Özel Fırsatlar',
    'Erken rezervasyon fırsatlarını kaçırma! Axileto Club üyelerine özel ek indirimler.',
    'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80',
    'Teklifleri Keşfet'
) ON CONFLICT (id) DO NOTHING;

-- 2. RLS Politikaları
ALTER TABLE public.club_vip_settings ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    DROP POLICY IF EXISTS "Public can view club vip settings" ON public.club_vip_settings;
    DROP POLICY IF EXISTS "Admins can update club vip settings" ON public.club_vip_settings;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

CREATE POLICY "Public can view club vip settings"
    ON public.club_vip_settings FOR SELECT
    USING (true);

CREATE POLICY "Admins can update club vip settings"
    ON public.club_vip_settings FOR ALL
    USING (true)
    WITH CHECK (true);

-- 3. Sıfır Yük & Tek İstek Dashboard RPC Fonksiyonu
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
        'settings', v_settings
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_member_vip_dashboard_summary(TEXT, TEXT) TO anon, authenticated, service_role;

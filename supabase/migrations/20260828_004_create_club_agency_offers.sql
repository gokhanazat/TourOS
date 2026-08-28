-- ============================================================================
-- TourOS Migration: 20260828_004_create_club_agency_offers.sql
-- Description: Axileto Club Acente Tekliflerinin Kalıcı Olarak Saklanması
-- ============================================================================

-- 1. Acente Kulüp Teklifleri Tablosu
CREATE TABLE IF NOT EXISTS public.club_agency_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT 'a0000000-0000-0000-0000-000000000001'::uuid,
    agency_id TEXT NOT NULL,
    agency_name TEXT NOT NULL,
    hotel_name TEXT NOT NULL,
    stars INT DEFAULT 5,
    operator_badge TEXT DEFAULT 'VIP Özel Teklif',
    flight_badge TEXT DEFAULT 'Charter & Lounge 🧳',
    location_text TEXT DEFAULT 'Türkiye • Antalya',
    nights_text TEXT DEFAULT '7 Gece',
    meal_text TEXT DEFAULT 'Ultra Her Şey Dahil',
    lowest_price TEXT NOT NULL,
    highest_price TEXT NOT NULL,
    award_badge TEXT DEFAULT 'Starway Award',
    discount_percent INT DEFAULT 25,
    rating_score NUMERIC(3,1) DEFAULT 4.8,
    review_count INT DEFAULT 120,
    image_url TEXT DEFAULT 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=60',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. İndeksler
CREATE INDEX IF NOT EXISTS idx_club_agency_offers_agency ON public.club_agency_offers(agency_name, is_active);
CREATE INDEX IF NOT EXISTS idx_club_agency_offers_created ON public.club_agency_offers(created_at DESC);

-- 3. RLS Güvenlik Politikaları
ALTER TABLE public.club_agency_offers ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    DROP POLICY IF EXISTS "Public can view active club agency offers" ON public.club_agency_offers;
    DROP POLICY IF EXISTS "Agencies and admins can manage club agency offers" ON public.club_agency_offers;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

CREATE POLICY "Public can view active club agency offers"
    ON public.club_agency_offers FOR SELECT
    USING (is_active = true);

CREATE POLICY "Agencies and admins can manage club agency offers"
    ON public.club_agency_offers FOR ALL
    USING (true)
    WITH CHECK (true);

-- 4. Acente Teklif Ekleme / Güncelleme Fonksiyonu
CREATE OR REPLACE FUNCTION public.save_club_agency_offer(
    p_id TEXT DEFAULT NULL,
    p_agency_id TEXT DEFAULT 'agn-default',
    p_agency_name TEXT DEFAULT 'Yetkili Acente',
    p_hotel_name TEXT DEFAULT '',
    p_stars INT DEFAULT 5,
    p_operator_badge TEXT DEFAULT 'VIP Özel Teklif',
    p_flight_badge TEXT DEFAULT 'Charter & Lounge 🧳',
    p_location_text TEXT DEFAULT 'Türkiye • Antalya',
    p_nights_text TEXT DEFAULT '7 Gece',
    p_meal_text TEXT DEFAULT 'Ultra Her Şey Dahil',
    p_lowest_price TEXT DEFAULT '₺35.000',
    p_highest_price TEXT DEFAULT '₺42.000',
    p_image_url TEXT DEFAULT 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=60'
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_offer_id UUID;
    v_agency_offer_count INT;
BEGIN
    IF TRIM(COALESCE(p_hotel_name, '')) = '' THEN
        RETURN jsonb_build_object('success', false, 'error', 'Otel adı zorunludur.');
    END IF;

    -- Acente için 10 teklif sınırını kontrol et (yeni ekleme ise)
    IF p_id IS NULL OR TRIM(p_id) = '' OR TRIM(p_id) LIKE 'new-off-%' THEN
        SELECT COUNT(*) INTO v_agency_offer_count
        FROM public.club_agency_offers
        WHERE LOWER(TRIM(agency_name)) = LOWER(TRIM(p_agency_name)) AND is_active = true;

        IF v_agency_offer_count >= 10 THEN
            RETURN jsonb_build_object('success', false, 'error', '10 teklif sınırına ulaştınız!');
        END IF;

        INSERT INTO public.club_agency_offers (
            agency_id, agency_name, hotel_name, stars,
            operator_badge, flight_badge, location_text, nights_text,
            meal_text, lowest_price, highest_price, image_url, updated_at
        ) VALUES (
            p_agency_id, p_agency_name, p_hotel_name, p_stars,
            p_operator_badge, p_flight_badge, p_location_text, p_nights_text,
            p_meal_text, p_lowest_price, p_highest_price, p_image_url, now()
        ) RETURNING id INTO v_offer_id;
    ELSE
        v_offer_id := p_id::uuid;
        UPDATE public.club_agency_offers SET
            hotel_name = p_hotel_name,
            stars = p_stars,
            operator_badge = p_operator_badge,
            flight_badge = p_flight_badge,
            location_text = p_location_text,
            nights_text = p_nights_text,
            meal_text = p_meal_text,
            lowest_price = p_lowest_price,
            highest_price = p_highest_price,
            image_url = p_image_url,
            updated_at = now()
        WHERE id = v_offer_id;
    END IF;

    RETURN jsonb_build_object('success', true, 'id', v_offer_id);
END;
$$;

-- 5. Acente Teklif Silme Fonksiyonu
CREATE OR REPLACE FUNCTION public.delete_club_agency_offer(
    p_offer_id TEXT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    DELETE FROM public.club_agency_offers WHERE id::text = p_offer_id;
    RETURN jsonb_build_object('success', true);
END;
$$;

-- İzinleri Tanımla
GRANT ALL ON TABLE public.club_agency_offers TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.save_club_agency_offer TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.delete_club_agency_offer TO anon, authenticated, service_role;

-- PostgREST Schema Cache Reload
NOTIFY pgrst, 'reload schema';

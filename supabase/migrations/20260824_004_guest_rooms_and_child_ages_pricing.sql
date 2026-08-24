-- ============================================================
-- TourOS Migration: 20260824_004_guest_rooms_and_child_ages_pricing.sql
-- Misafir, Oda Sayısı ve Çocuk Yaşları Mimarisi & Dinamik Fiyatlandırma RPC
-- ============================================================

-- 1. REZERVASYONLAR TABLOSUNA ODA VE ÇOCUK YAŞLARI KOLONLARI
ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS room_count INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS children_ages INT[] DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS room_distribution JSONB DEFAULT '[{"room": 1, "adults": 2, "children_ages": []}]'::jsonb;

-- 2. MARKETPLACE ÜRÜNLERİNE ODA & BEBEK/ÇOCUK ALANLARI
ALTER TABLE public.marketplace_products
    ADD COLUMN IF NOT EXISTS max_rooms INT DEFAULT 5,
    ADD COLUMN IF NOT EXISTS infant_price NUMERIC(12,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS child_price NUMERIC(12,2) DEFAULT 0.00;

-- 3. ÇOCUK YAŞINA GÖRE DİNAMİK FİYAT VE UYGUNLUK HESAPLAYAN RPC
CREATE OR REPLACE FUNCTION public.calculate_pax_pricing(
    p_base_price     NUMERIC,
    p_adults         INT,
    p_children_ages  INT[],
    p_room_count     INT DEFAULT 1
)
RETURNS NUMERIC
LANGUAGE plpgsql
IMMUTABLE
SECURITY DEFINER
AS $$
DECLARE
    total_price NUMERIC := 0;
    c_age INT;
BEGIN
    -- Yetişkinlerin Toplamı
    total_price := (p_adults * p_base_price);

    -- Çocuk Yaş Kırılımlarına Göre Fiyatlandırma
    IF array_length(p_children_ages, 1) > 0 THEN
        FOREACH c_age IN ARRAY p_children_ages
        LOOP
            IF c_age < 2 THEN
                -- Bebek (0-1.99 Yaş): Sadece %10 transfer/uçak harcı
                total_price := total_price + (p_base_price * 0.10);
            ELSIF c_age BETWEEN 2 AND 6 THEN
                -- Küçük Çocuk (2-6 Yaş): %50 İndirimli
                total_price := total_price + (p_base_price * 0.50);
            ELSIF c_age BETWEEN 7 AND 11 THEN
                -- Büyük Çocuk (7-11 Yaş): %75 Oranında
                total_price := total_price + (p_base_price * 0.75);
            ELSE
                -- 12+ Yaş: Tam Yetişkin Ücreti
                total_price := total_price + p_base_price;
            END IF;
        END LOOP;
    END IF;

    -- Oda Çarpanı (Birden fazla oda seçildiyse)
    RETURN ROUND(total_price * GREATEST(p_room_count, 1), 2);
END;
$$;

GRANT EXECUTE ON FUNCTION public.calculate_pax_pricing TO authenticated, anon, service_role;

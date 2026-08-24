-- Migration: 20260824_008_country_discovery_and_fast_deals.sql
-- Description: Ülke Giriş Kartları, 2 Kişi 7 Gece Başlayan Fiyatlar ve Hızlı Paket Listeleme RPC

-- 1. Ülke Keşif Kartları Tablosu
CREATE TABLE IF NOT EXISTS public.country_discovery_destinations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_code VARCHAR(10) NOT NULL UNIQUE,
    country_name VARCHAR(100) NOT NULL,
    country_flag VARCHAR(10) NOT NULL,
    hero_image_url TEXT NOT NULL,
    popular_regions TEXT NOT NULL,
    starting_price_2p_7n_usd NUMERIC(12, 2) DEFAULT 500.00,
    display_order INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Güvenlik Politikası
ALTER TABLE public.country_discovery_destinations ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'country_discovery_destinations' AND policyname = 'Public Read Country Discovery'
    ) THEN
        CREATE POLICY "Public Read Country Discovery" ON public.country_discovery_destinations
            FOR SELECT USING (TRUE);
    END IF;
END $$;

-- 2. Varsayılan Ülke Kartları Verisi
INSERT INTO public.country_discovery_destinations 
(country_code, country_name, country_flag, hero_image_url, popular_regions, starting_price_2p_7n_usd, display_order)
VALUES
    ('ALL', 'Tüm Dünyayı Keşfet', '🌍', 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&auto=format&fit=crop&q=80', 'Global Destinasyonlar', 400.00, 1),
    ('TR', 'Türkiye', '🇹🇷', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80', 'Antalya · Belek · Bodrum · Kemer', 580.00, 2),
    ('EG', 'Mısır', '🇪🇬', 'https://images.unsplash.com/photo-1539768942893-daf53e448371?w=800&auto=format&fit=crop&q=80', 'Şarm El-Şeyh · Hurgada · El Gouna', 490.00, 3),
    ('TH', 'Tayland', '🇹🇭', 'https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=800&auto=format&fit=crop&q=80', 'Phuket · Pattaya · Bangkok · Samui', 790.00, 4),
    ('VN', 'Vietnam', '🇻🇳', 'https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop&q=80', 'Da Nang · Phu Quoc · Nha Trang', 850.00, 5),
    ('AE', 'BAE (Dubai)', '🇦🇪', 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80', 'Dubai Marina · Palm Jumeirah', 690.00, 6),
    ('RU', 'Rusya', '🇷🇺', 'https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800&auto=format&fit=crop&q=80', 'Moskova · Sochi · St. Petersburg', 420.00, 7)
ON CONFLICT (country_code) DO UPDATE 
SET 
    hero_image_url = EXCLUDED.hero_image_url,
    popular_regions = EXCLUDED.popular_regions,
    starting_price_2p_7n_usd = EXCLUDED.starting_price_2p_7n_usd,
    display_order = EXCLUDED.display_order,
    updated_at = NOW();

-- 3. Ülke Kartlarını ve Anlık Ürün Sayılarını Getiren RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.get_country_discovery_cards()
RETURNS TABLE (
    country_code VARCHAR(10),
    country_name VARCHAR(100),
    country_flag VARCHAR(10),
    hero_image_url TEXT,
    popular_regions TEXT,
    starting_price_2p_7n_usd NUMERIC(12, 2),
    total_packages_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        cdd.country_code,
        cdd.country_name,
        cdd.country_flag,
        cdd.hero_image_url,
        cdd.popular_regions,
        cdd.starting_price_2p_7n_usd,
        CASE 
            WHEN cdd.country_code = 'ALL' THEN (SELECT COUNT(id) FROM public.marketplace_products WHERE is_active = TRUE)
            ELSE (SELECT COUNT(id) FROM public.marketplace_products WHERE country_code = cdd.country_code AND is_active = TRUE)
        END as total_packages_count
    FROM public.country_discovery_destinations cdd
    WHERE cdd.is_active = TRUE
    ORDER BY cdd.display_order ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

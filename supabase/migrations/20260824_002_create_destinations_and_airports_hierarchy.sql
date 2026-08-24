-- ============================================================
-- TourOS Migration: 20260824_002_create_destinations_and_airports_hierarchy.sql
-- Hiyerarşik Destinasyon, Bölge, Alt Bölge (Resort) & Havalimanı (IATA) Mimarisi
-- ============================================================

-- 1. HAVALİMANLARI & ÇIKIŞ / VARIŞ HUBLARI TABLOSU
CREATE TABLE IF NOT EXISTS public.airports (
    iata_code        VARCHAR(3) PRIMARY KEY,
    airport_name     TEXT NOT NULL,
    city_name        TEXT NOT NULL,
    country_code     VARCHAR(2) NOT NULL DEFAULT 'TR',
    country_name     TEXT NOT NULL DEFAULT 'Türkiye',
    is_departure_hub BOOLEAN DEFAULT TRUE,
    is_arrival_hub   BOOLEAN DEFAULT TRUE,
    display_order    INT DEFAULT 0,
    created_at       TIMESTAMPTZ DEFAULT now()
);

-- Havalimanı Temel Verileri (Türkiye, Rusya, BAE, Mısır)
INSERT INTO public.airports (iata_code, airport_name, city_name, country_code, country_name, is_departure_hub, is_arrival_hub, display_order)
VALUES
    -- Türkiye Havalimanları
    ('IST', 'İstanbul Havalimanı', 'İstanbul', 'TR', 'Türkiye', TRUE, TRUE, 1),
    ('SAW', 'İstanbul Sabiha Gökçen Havalimanı', 'İstanbul', 'TR', 'Türkiye', TRUE, TRUE, 2),
    ('AYT', 'Antalya Havalimanı', 'Antalya', 'TR', 'Türkiye', TRUE, TRUE, 3),
    ('GZP', 'Gazipaşa - Alanya Havalimanı', 'Antalya / Alanya', 'TR', 'Türkiye', TRUE, TRUE, 4),
    ('DLM', 'Dalaman Havalimanı', 'Muğla / Dalaman', 'TR', 'Türkiye', TRUE, TRUE, 5),
    ('BJV', 'Milas - Bodrum Havalimanı', 'Muğla / Bodrum', 'TR', 'Türkiye', TRUE, TRUE, 6),
    ('ADB', 'İzmir Adnan Menderes Havalimanı', 'İzmir', 'TR', 'Türkiye', TRUE, TRUE, 7),
    ('ESB', 'Ankara Esenboğa Havalimanı', 'Ankara', 'TR', 'Türkiye', TRUE, TRUE, 8),
    ('TZX', 'Trabzon Havalimanı', 'Trabzon', 'TR', 'Türkiye', TRUE, TRUE, 9),
    ('NAV', 'Kapadokya Nevşehir Havalimanı', 'Nevşehir', 'TR', 'Türkiye', TRUE, TRUE, 10),
    -- Rusya Havalimanları
    ('SVO', 'Moskova Şeremetyevo Havalimanı', 'Moskova', 'RU', 'Rusya', TRUE, TRUE, 20),
    ('DME', 'Moskova Domodedovo Havalimanı', 'Moskova', 'RU', 'Rusya', TRUE, TRUE, 21),
    ('VKO', 'Moskova Vnukovo Havalimanı', 'Moskova', 'RU', 'Rusya', TRUE, TRUE, 22),
    ('ZIA', 'Moskova Jukovski Havalimanı', 'Moskova', 'RU', 'Rusya', TRUE, TRUE, 23),
    ('LED', 'St. Petersburg Pulkovo Havalimanı', 'St. Petersburg', 'RU', 'Rusya', TRUE, TRUE, 24),
    ('AER', 'Sochi Uluslararası Havalimanı', 'Sochi', 'RU', 'Rusya', TRUE, TRUE, 25),
    ('KZN', 'Kazan Uluslararası Havalimanı', 'Kazan', 'RU', 'Rusya', TRUE, TRUE, 26),
    ('SVX', 'Yekaterinburg Koltsovo Havalimanı', 'Yekaterinburg', 'RU', 'Rusya', TRUE, TRUE, 27),
    -- Mısır & BAE
    ('SSH', 'Şarm El-Şeyh Havalimanı', 'Şarm El-Şeyh', 'EG', 'Mısır', FALSE, TRUE, 30),
    ('HRG', 'Hurgada Havalimanı', 'Hurgada', 'EG', 'Mısır', FALSE, TRUE, 31),
    ('DXB', 'Dubai Uluslararası Havalimanı', 'Dubai', 'AE', 'Birleşik Arap Emirlikleri', TRUE, TRUE, 40)
ON CONFLICT (iata_code) DO UPDATE SET
    airport_name = EXCLUDED.airport_name,
    city_name = EXCLUDED.city_name,
    country_name = EXCLUDED.country_name;

-- 2. HİYERARŞİK DESTİNASYON TABLOSU (Ülke -> Şehir/Bölge -> Alt Bölge/Resort)
CREATE TABLE IF NOT EXISTS public.destinations (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_code     VARCHAR(2) NOT NULL DEFAULT 'TR',
    country_name     TEXT NOT NULL,
    city_name        TEXT NOT NULL,
    region_name      TEXT NOT NULL,
    sub_region_name  TEXT NOT NULL,
    primary_airport  VARCHAR(3) REFERENCES public.airports(iata_code) ON DELETE SET NULL,
    is_active        BOOLEAN DEFAULT TRUE,
    display_order    INT DEFAULT 0,
    created_at       TIMESTAMPTZ DEFAULT now()
);

-- İndeksler
CREATE INDEX IF NOT EXISTS idx_destinations_country ON public.destinations(country_name);
CREATE INDEX IF NOT EXISTS idx_destinations_city ON public.destinations(city_name);
CREATE INDEX IF NOT EXISTS idx_destinations_sub_region ON public.destinations(sub_region_name);

-- Temel Destinasyon Kırılımları
INSERT INTO public.destinations (country_code, country_name, city_name, region_name, sub_region_name, primary_airport, display_order)
VALUES
    -- ANTALYA VE ALT BÖLGELERİ
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Lara / Kundu', 'AYT', 1),
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Belek / Boğazkent', 'AYT', 2),
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Side / Manavgat', 'AYT', 3),
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Alanya / Mahmutlar / Okurcalar', 'GZP', 4),
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Kemer (Beldibi, Göynük, Tekirova)', 'AYT', 5),
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Çıralı / Olimpos / Adrasan', 'AYT', 6),
    ('TR', 'Türkiye', 'Antalya', 'Akdeniz', 'Kaş / Kalkan', 'DLM', 7),
    -- MUĞLA / EGE
    ('TR', 'Türkiye', 'Muğla', 'Ege', 'Bodrum (Gümbet, Yalıkavak, Torba)', 'BJV', 10),
    ('TR', 'Türkiye', 'Muğla', 'Ege', 'Marmaris / İçmeler / Turunç', 'DLM', 11),
    ('TR', 'Türkiye', 'Muğla', 'Ege', 'Fethiye / Ölüdeniz / Göcek', 'DLM', 12),
    ('TR', 'Türkiye', 'Muğla', 'Ege', 'Datça', 'DLM', 13),
    -- İZMİR / EGE
    ('TR', 'Türkiye', 'İzmir', 'Ege', 'Çeşme / Alaçatı', 'ADB', 20),
    ('TR', 'Türkiye', 'İzmir', 'Ege', 'Kuşadası / Selçuk', 'ADB', 21),
    -- İSTANBUL & DİĞER
    ('TR', 'Türkiye', 'İstanbul', 'Marmara', 'Tarihi Yarımada / Sultanahmet', 'IST', 30),
    ('TR', 'Türkiye', 'İstanbul', 'Marmara', 'Taksim / Beyoğlu / Boğaz', 'IST', 31),
    ('TR', 'Türkiye', 'Nevşehir', 'İç Anadolu', 'Kapadokya / Göreme / Ürgüp', 'NAV', 40),
    -- RUSYA DESTİNASYONLARI
    ('RU', 'Rusya', 'Moskova', 'Merkez', 'Moskova Şehir Merkezi & Çevresi', 'SVO', 50),
    ('RU', 'Rusya', 'St. Petersburg', 'Kuzeybatı', 'St. Petersburg & Tarihi Saraylar', 'LED', 51),
    ('RU', 'Rusya', 'Sochi', 'Krasnodar', 'Sochi Sahil / Adler / Krasnaya Polyana', 'AER', 52),
    ('RU', 'Rusya', 'Kazan', 'Tataristan', 'Kazan & Volga Bölgesi', 'KZN', 53)
ON CONFLICT DO NOTHING;

-- 3. HİYERARŞİK DESTİNASYON AĞACI DÖNDÜREN RPC FONKSİYONU
CREATE OR REPLACE FUNCTION public.get_destination_hierarchy()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    tree jsonb;
BEGIN
    SELECT jsonb_agg(
        jsonb_build_object(
            'country_name', c.country_name,
            'country_code', c.country_code,
            'cities', (
                SELECT jsonb_agg(
                    jsonb_build_object(
                        'city_name', ci.city_name,
                        'region_name', ci.region_name,
                        'primary_airport', ci.primary_airport,
                        'sub_regions', (
                            SELECT jsonb_agg(
                                jsonb_build_object(
                                    'id', d.id,
                                    'sub_region_name', d.sub_region_name,
                                    'primary_airport', d.primary_airport
                                ) ORDER BY d.display_order
                            )
                            FROM public.destinations d
                            WHERE d.country_name = c.country_name AND d.city_name = ci.city_name AND d.is_active = TRUE
                        )
                    ) ORDER BY ci.city_name
                )
                FROM (
                    SELECT DISTINCT city_name, region_name, primary_airport
                    FROM public.destinations
                    WHERE country_name = c.country_name AND is_active = TRUE
                ) ci
            )
        ) ORDER BY c.country_name
    ) INTO tree
    FROM (
        SELECT DISTINCT country_name, country_code
        FROM public.destinations
        WHERE is_active = TRUE
    ) c;

    RETURN coalesce(tree, '[]'::jsonb);
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_destination_hierarchy() TO authenticated, anon, service_role;

-- 4. ÇIKIŞ / VARIŞ HAVALİMANLARINI DÖNDÜREN RPC FONKSİYONU
CREATE OR REPLACE FUNCTION public.get_airports_list()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN (
        SELECT jsonb_agg(
            jsonb_build_object(
                'iata_code', a.iata_code,
                'airport_name', a.airport_name,
                'city_name', a.city_name,
                'country_name', a.country_name,
                'is_departure_hub', a.is_departure_hub,
                'is_arrival_hub', a.is_arrival_hub
            ) ORDER BY a.display_order, a.country_name, a.city_name
        )
        FROM public.airports a
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_airports_list() TO authenticated, anon, service_role;

-- ============================================================================
-- TourOS - 9 Kanonik Tur Operatörü Kısıtlaması ve Veritabanı Temizliği (Yandex Cloud)
-- ============================================================================

-- 1. Kanonik Operatörler Tablosunun Güncellenmesi
CREATE TABLE IF NOT EXISTS public.canonical_operators (
    id SERIAL PRIMARY KEY,
    canonical_name TEXT NOT NULL UNIQUE,
    tourvisor_code INT,
    aliases TEXT[] DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9 Kanonik Operatör Dışındakileri Pasife Al
UPDATE public.canonical_operators
SET is_active = false
WHERE canonical_name NOT IN (
    'Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 
    'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'
);

-- 9 Kanonik Operatörü Ekle / Güncelle
INSERT INTO public.canonical_operators (canonical_name, tourvisor_code, aliases, is_active, display_order)
VALUES
    ('Bibloglobus', 18, ARRAY['bibloglobus', 'biblio globus', 'библио глобус', 'bg', 'biblioglobus', 'библио-глобус', 'biblio-globus', 'biblo'], true, 1),
    ('Anex', 13, ARRAY['anex', 'anex tour', 'анекс', 'анекс тур', 'anextour'], true, 2),
    ('Coral Travel', 11, ARRAY['coral', 'coral travel', 'корал', 'корал тревел', 'coral ru', 'coral-travel'], true, 3),
    ('Sunmar', 24, ARRAY['sunmar', 'санмар', 'sunmar tour', 'санмар тур'], true, 4),
    ('Fun&Sun (Ru)', 25, ARRAY['fun&sun', 'fun and sun', 'фан сан', 'funsun', 'tui', 'fun&sun (ru)', 'фан & сан'], true, 5),
    ('Kazunion', 89, ARRAY['kazunion', 'казунион'], true, 6),
    ('Loti', 94, ARRAY['loti', 'loti travel', 'лоти'], true, 7),
    ('Pegas Touristik', 12, ARRAY['pegas', 'pegast', 'pegas touristik', 'пегас', 'пегас туристик'], true, 8),
    ('Интурист', 43, ARRAY['интурист', 'intourist', 'intourist ru', 'нтк интурист'], true, 9)
ON CONFLICT (canonical_name) DO UPDATE 
SET tourvisor_code = EXCLUDED.tourvisor_code,
    aliases = EXCLUDED.aliases,
    is_active = true,
    display_order = EXCLUDED.display_order;

-- 2. Normalizasyon Fonksiyonu
CREATE OR REPLACE FUNCTION public.normalize_operator_name(p_name TEXT, p_code INT DEFAULT NULL)
RETURNS TEXT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    matched_name TEXT;
    clean_name TEXT;
BEGIN
    IF p_code IS NOT NULL THEN
        SELECT canonical_name INTO matched_name 
        FROM public.canonical_operators 
        WHERE tourvisor_code = p_code AND is_active = true 
        LIMIT 1;
        IF matched_name IS NOT NULL THEN
            RETURN matched_name;
        END IF;
    END IF;

    IF p_name IS NULL OR TRIM(p_name) = '' THEN
        RETURN NULL;
    END IF;

    clean_name := LOWER(TRIM(p_name));

    SELECT canonical_name INTO matched_name 
    FROM public.canonical_operators 
    WHERE is_active = true 
      AND (LOWER(canonical_name) = clean_name OR clean_name = ANY(aliases))
    LIMIT 1;

    RETURN matched_name;
END;
$$;

-- 3. marketplace_products Tablosunda Normalizasyon
UPDATE public.marketplace_products 
SET operator_name = public.normalize_operator_name(operator_name, NULL) 
WHERE public.normalize_operator_name(operator_name, NULL) IS NOT NULL;

-- 4. marketplace_products: 9 Kanonik Operatör Dışındaki Paket Turların Silinmesi
DELETE FROM public.marketplace_products 
WHERE operator_name NOT IN (
    'Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 
    'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'
)
AND COALESCE(product_type, '') != 'FLIGHT'
AND COALESCE(flight_number, '') = ''
AND NOT (tour_name ILIKE 'Uçuş:%' OR hotel_name ILIKE 'Uçuş:%' OR hotel_name ILIKE '✈️%');

-- 5. Uçuş kayıtlarında 9 TO dışındaki eski operatör isimlerini 'Charter' olarak güncelle
UPDATE public.marketplace_products
SET operator_name = 'Charter'
WHERE operator_name NOT IN (
    'Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 
    'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'
)
AND (COALESCE(product_type, '') = 'FLIGHT' OR flight_number != '' OR tour_name ILIKE 'Uçuş:%' OR hotel_name ILIKE 'Uçuş:%' OR hotel_name ILIKE '✈️%');

-- 6. marketplace_products_staging Temizliği
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'marketplace_products_staging') THEN
        UPDATE public.marketplace_products_staging 
        SET operator_name = public.normalize_operator_name(operator_name, NULL) 
        WHERE public.normalize_operator_name(operator_name, NULL) IS NOT NULL;

        DELETE FROM public.marketplace_products_staging 
        WHERE operator_name NOT IN (
            'Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 
            'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'
        )
        AND COALESCE(product_type, '') != 'FLIGHT'
        AND COALESCE(flight_number, '') = ''
        AND NOT (tour_name ILIKE 'Uçuş:%' OR hotel_name ILIKE 'Uçuş:%' OR hotel_name ILIKE '✈️%');

        UPDATE public.marketplace_products_staging
        SET operator_name = 'Charter'
        WHERE operator_name NOT IN (
            'Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 
            'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'
        )
        AND (COALESCE(product_type, '') = 'FLIGHT' OR flight_number != '' OR tour_name ILIKE 'Uçuş:%' OR hotel_name ILIKE 'Uçuş:%' OR hotel_name ILIKE '✈️%');
    END IF;
END $$;

-- 7. Yetkiler ve Şema Yenileme
GRANT ALL ON public.canonical_operators TO postgres, service_role;
GRANT SELECT ON public.canonical_operators TO anon, authenticated;
GRANT EXECUTE ON FUNCTION public.normalize_operator_name(TEXT, INT) TO anon, authenticated, service_role, postgres;
NOTIFY pgrst, 'reload schema';

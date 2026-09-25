-- 20260924_001_operator_filter_and_alias_mapping.sql
-- Tur Operatörleri Filtreleme, Kanonik İsimlendirme ve Eşleştirme Tablosu

CREATE TABLE IF NOT EXISTS public.canonical_operators (
    id SERIAL PRIMARY KEY,
    canonical_name TEXT NOT NULL UNIQUE,
    tourvisor_code INT,
    aliases TEXT[] NOT NULL DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Başlangıç 9 Ana Tur Operatörü Tohum Verisi
INSERT INTO public.canonical_operators (canonical_name, tourvisor_code, aliases, is_active, display_order)
VALUES 
    ('Bibloglobus', 18, ARRAY['bibloglobus', 'biblio globus', 'библио глобус', 'bg', 'biblioglobus', 'библио-глобус'], true, 1),
    ('Anex', 13, ARRAY['anex', 'anex tour', 'анекс', 'анекс тур', 'anextour'], true, 2),
    ('Coral Travel', 11, ARRAY['coral', 'coral travel', 'корал', 'корал тревел', 'coral ru', 'coral-travel'], true, 3),
    ('Sunmar', 24, ARRAY['sunmar', 'санмар', 'sunmar tour', 'санмар тур'], true, 4),
    ('Fun&Sun (Ru)', 25, ARRAY['fun&sun', 'fun and sun', 'фан сан', 'funsun', 'tui', 'fun&sun (ru)', 'фан & сан'], true, 5),
    ('Kazunion', 89, ARRAY['kazunion', 'казунион'], true, 6),
    ('Loti', 94, ARRAY['loti', 'loti travel', 'лоти'], true, 7),
    ('Pegas Touristik', 12, ARRAY['pegas', 'pegast', 'pegas touristik', 'пегас', 'пегас туристик'], true, 8),
    ('Интурист', 43, ARRAY['интурист', 'intourist', 'intourist ru', 'нтк интурист'], true, 9)
ON CONFLICT (canonical_name) DO UPDATE SET
    tourvisor_code = EXCLUDED.tourvisor_code,
    aliases = EXCLUDED.aliases,
    display_order = EXCLUDED.display_order;

-- data_feed_sources tablosuna seçili operatörler sütunu ekle
ALTER TABLE public.data_feed_sources
ADD COLUMN IF NOT EXISTS selected_operators TEXT[] DEFAULT ARRAY['Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'];

-- Operatör İsmi Normalizasyon Fonksiyonu
CREATE OR REPLACE FUNCTION public.normalize_operator_name(p_name TEXT, p_code INT DEFAULT 0)
RETURNS TEXT
LANGUAGE plpgsql
STABLE
AS $func$
DECLARE
    v_clean TEXT := LOWER(TRIM(COALESCE(p_name, '')));
    v_result TEXT;
BEGIN
    -- 1. Kod eşleşmesi (TourVisor operatorcode)
    IF p_code > 0 THEN
        SELECT canonical_name INTO v_result
        FROM public.canonical_operators
        WHERE is_active IS TRUE AND tourvisor_code = p_code
        LIMIT 1;
        
        IF v_result IS NOT NULL THEN
            RETURN v_result;
        END IF;
    END IF;

    -- 2. Birebir veya Alias eşleşmesi
    SELECT canonical_name INTO v_result
    FROM public.canonical_operators
    WHERE is_active IS TRUE AND (
        LOWER(canonical_name) = v_clean
        OR v_clean = ANY(aliases)
        OR v_clean LIKE '%' || LOWER(canonical_name) || '%'
    )
    ORDER BY display_order ASC
    LIMIT 1;

    RETURN v_result;
END;
$func$;

-- İzinler
GRANT SELECT, INSERT, UPDATE ON public.canonical_operators TO anon, authenticated, service_role;
GRANT USAGE, SELECT ON SEQUENCE public.canonical_operators_id_seq TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.normalize_operator_name(TEXT, INT) TO anon, authenticated, service_role;

NOTIFY pgrst, 'reload schema';

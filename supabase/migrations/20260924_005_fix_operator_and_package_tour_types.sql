-- ============================================================================
-- TourOS - Aktif Operatör Yönetimi & Temiz Uçuş/Otel/Paket Tur Ayrımı
-- ============================================================================

-- 1. Kanonik Operatör Tablosu İndeksi ve Güncelleme Yetkileri
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

-- 2. Varsayılan 9 Operatör
INSERT INTO public.canonical_operators (id, canonical_name, tourvisor_code, aliases, is_active, display_order)
VALUES
    (1, 'Bibloglobus', 18, ARRAY['bibloglobus', 'biblio globus', 'библио глобус', 'bg', 'biblioglobus', 'библио-глобус', 'biblio-globus'], true, 1),
    (2, 'Anex', 13, ARRAY['anex', 'anex tour', 'анекс', 'анекс тур', 'anextour'], true, 2),
    (3, 'Coral Travel', 11, ARRAY['coral', 'coral travel', 'корал', 'корал тревел', 'coral ru', 'coral-travel'], true, 3),
    (4, 'Sunmar', 24, ARRAY['sunmar', 'санмар', 'sunmar tour', 'санмар тур'], true, 4),
    (5, 'Fun&Sun (Ru)', 25, ARRAY['fun&sun', 'fun and sun', 'фан сан', 'funsun', 'tui', 'fun&sun (ru)', 'фан & сан'], true, 5),
    (6, 'Kazunion', 89, ARRAY['kazunion', 'казунион'], true, 6),
    (7, 'Loti', 94, ARRAY['loti', 'loti travel', 'лоти'], true, 7),
    (8, 'Pegas Touristik', 12, ARRAY['pegas', 'pegast', 'pegas touristik', 'пегас', 'пегас туристик'], true, 8),
    (9, 'Интурист', 43, ARRAY['интурист', 'intourist', 'intourist ru', 'нтк интурист'], true, 9)
ON CONFLICT (canonical_name) DO UPDATE 
SET tourvisor_code = EXCLUDED.tourvisor_code,
    aliases = EXCLUDED.aliases,
    display_order = EXCLUDED.display_order;

-- 3. Paket Turların Yanlışlıkla FLIGHT Olarak İşaretlenmesini Düzeltme
UPDATE public.marketplace_products
SET product_type = 'PACKAGE_TOUR'
WHERE product_type = 'FLIGHT' 
  AND hotel_name IS NOT NULL 
  AND hotel_name != '' 
  AND hotel_name NOT LIKE 'Uçuş:%';

-- 4. PostgREST Yetkileri
GRANT ALL ON public.canonical_operators TO postgres, service_role;
GRANT SELECT, UPDATE ON public.canonical_operators TO anon, authenticated;
NOTIFY pgrst, 'reload schema';

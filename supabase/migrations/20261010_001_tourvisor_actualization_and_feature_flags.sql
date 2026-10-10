-- ============================================================================
-- TourOS Migration: 20261010_001_tourvisor_actualization_and_feature_flags.sql
-- Yandex Cloud PostgreSQL 16 üzerinde çalıştırılır.
--
-- KAPSAM (SADECE EKLEME — mevcut hiçbir tablo/kolon/fonksiyon değiştirilmez veya silinmez):
--   1. app_feature_flags            : Aç/kapa ayarları (uçuş sekmesi, canlı tur detayı)
--   2. tour_actualizations          : Tourvisor tek tur sorgusu (actualize.php + actdetail.php) önbelleği
--   3. tourvisor_api_usage          : Günlük Tourvisor sorgu sayacı (kota takibi)
--   4. request_tour_actualization() : Uygulamanın çağırdığı RPC (önbellek + NOTIFY)
--   5. set_app_feature_flag()       : Sadece süper admin için aç/kapa RPC'si
--
-- Sorguyu yapan servis: scripts/tourvisor_actualize_worker.py (Yandex VM, systemd)
-- Servis 'tour_actualize' kanalını LISTEN eder, sonucu tour_actualizations tablosuna yazar.
-- ============================================================================

-- ─── 1. AÇ/KAPA AYARLARI ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.app_feature_flags (
    flag_key    TEXT PRIMARY KEY,
    is_enabled  BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO public.app_feature_flags (flag_key, is_enabled, description) VALUES
    ('flight_search_tab',  false, 'Web ve Acente aramasında Uçak Bileti / Uçuşlar sekmesi (gerçek uçuş kaynağı bağlanana kadar kapalı)'),
    ('tour_actualization', true,  'Rezervasyon adımında Tourvisor canlı tur detayı (uçuşlar, ek ödemeler, vize, tura dahil olanlar)')
ON CONFLICT (flag_key) DO NOTHING;

ALTER TABLE public.app_feature_flags ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "app_feature_flags_read" ON public.app_feature_flags;
CREATE POLICY "app_feature_flags_read" ON public.app_feature_flags
    FOR SELECT USING (true);
-- Yazma politikası YOK: değişiklik sadece set_app_feature_flag() RPC'si ile (süper admin kontrolü).

GRANT SELECT ON public.app_feature_flags TO anon, authenticated;
GRANT ALL ON public.app_feature_flags TO postgres, service_role;

CREATE OR REPLACE FUNCTION public.set_app_feature_flag(p_flag_key TEXT, p_enabled BOOLEAN)
RETURNS public.app_feature_flags
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_row public.app_feature_flags;
BEGIN
    IF NOT public.is_super_admin() THEN
        RAISE EXCEPTION 'Bu ayarı sadece sistem yöneticisi değiştirebilir.';
    END IF;

    UPDATE public.app_feature_flags
       SET is_enabled = p_enabled,
           updated_at = now()
     WHERE flag_key = p_flag_key
    RETURNING * INTO v_row;

    IF v_row.flag_key IS NULL THEN
        RAISE EXCEPTION 'Tanımsız ayar: %', p_flag_key;
    END IF;

    RETURN v_row;
END;
$$;

GRANT EXECUTE ON FUNCTION public.set_app_feature_flag(TEXT, BOOLEAN) TO authenticated, service_role;


-- ─── 2. TOURVISOR TEK TUR SORGUSU ÖNBELLEĞİ ───────────────────────────────────
CREATE TABLE IF NOT EXISTS public.tour_actualizations (
    tour_id           TEXT PRIMARY KEY,                 -- Tourvisor tourid (marketplace_products.id = 'tv-' || tour_id)
    status            TEXT NOT NULL DEFAULT 'pending',  -- pending | ok | error | quota_exceeded
    error_message     TEXT,
    requested_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    fetched_at        TIMESTAMPTZ,

    -- actualize.php alanları (fiyatlar RUB, currency=0)
    price             NUMERIC(14,2),
    currency          TEXT,
    fuel_charge       NUMERIC(14,2),
    visa_charge       NUMERIC(14,2),                    -- KİŞİ BAŞI
    operator_price    NUMERIC(14,2),
    operator_currency TEXT,
    operator_link     TEXT,
    hotel_status      INT,                              -- 1 = talep üzerine, 2 = anında onay
    flight_status     INT,                              -- 1 = talep üzerine, 2 = az yer
    on_request        BOOLEAN,                          -- uçuşlarda yer talep üzerine
    night_flight      INT,                              -- yolda geçen gece sayısı
    placement         TEXT,
    adults            INT,
    child             INT,

    -- actdetail.php alanları (servis tarafından sabit bir şekle normalize edilir)
    flights           JSONB NOT NULL DEFAULT '[]'::jsonb,
    add_payments      JSONB NOT NULL DEFAULT '[]'::jsonb, -- [{name, amount}] KİŞİ BAŞI
    contents          JSONB NOT NULL DEFAULT '[]'::jsonb, -- ["Авиаперелет", ...]
    flags             JSONB NOT NULL DEFAULT '{}'::jsonb, -- {notransfer, nomedinsurance, noflight, nomeal}

    -- Hata ayıklama için ham yanıtlar
    raw_actualize     JSONB,
    raw_detail        JSONB
);

CREATE INDEX IF NOT EXISTS idx_tour_actualizations_status ON public.tour_actualizations(status);

ALTER TABLE public.tour_actualizations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "tour_actualizations_read" ON public.tour_actualizations;
CREATE POLICY "tour_actualizations_read" ON public.tour_actualizations
    FOR SELECT USING (true);
-- İstemci yazamaz; yazma sadece RPC (SECURITY DEFINER) ve sunucu servisi (postgres) ile.

GRANT SELECT ON public.tour_actualizations TO anon, authenticated;
GRANT ALL ON public.tour_actualizations TO postgres, service_role;


-- ─── 3. GÜNLÜK TOURVISOR SORGU SAYACI ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.tourvisor_api_usage (
    usage_date      DATE PRIMARY KEY,
    actualize_calls INT NOT NULL DEFAULT 0,
    detail_calls    INT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.tourvisor_api_usage ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "tourvisor_api_usage_admin_read" ON public.tourvisor_api_usage;
CREATE POLICY "tourvisor_api_usage_admin_read" ON public.tourvisor_api_usage
    FOR SELECT USING (public.is_super_admin());

GRANT SELECT ON public.tourvisor_api_usage TO authenticated;
GRANT ALL ON public.tourvisor_api_usage TO postgres, service_role;


-- ─── 4. UYGULAMANIN ÇAĞIRDIĞI RPC ─────────────────────────────────────────────
-- Önbellek kuralı: başarılı sonuç 30 dk geçerli; bekleyen istek 2 dk içinde tekrar tetiklenmez.
CREATE OR REPLACE FUNCTION public.request_tour_actualization(p_tour_id TEXT)
RETURNS public.tour_actualizations
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_row     public.tour_actualizations;
    v_enabled BOOLEAN;
BEGIN
    IF p_tour_id IS NULL OR p_tour_id !~ '^[0-9]{5,30}$' THEN
        RAISE EXCEPTION 'Geçersiz tur numarası: %', p_tour_id;
    END IF;

    SELECT is_enabled INTO v_enabled FROM public.app_feature_flags WHERE flag_key = 'tour_actualization';
    IF COALESCE(v_enabled, false) = false THEN
        v_row.tour_id := p_tour_id;
        v_row.status := 'disabled';
        RETURN v_row;
    END IF;

    SELECT * INTO v_row FROM public.tour_actualizations WHERE tour_id = p_tour_id;

    IF FOUND THEN
        IF v_row.status = 'ok' AND v_row.fetched_at > now() - interval '30 minutes' THEN
            RETURN v_row;
        END IF;
        IF v_row.status = 'pending' AND v_row.requested_at > now() - interval '2 minutes' THEN
            RETURN v_row;
        END IF;
    END IF;

    INSERT INTO public.tour_actualizations (tour_id, status, requested_at, error_message)
    VALUES (p_tour_id, 'pending', now(), NULL)
    ON CONFLICT (tour_id) DO UPDATE
        SET status = 'pending',
            requested_at = now(),
            error_message = NULL
    RETURNING * INTO v_row;

    PERFORM pg_notify('tour_actualize', p_tour_id);

    RETURN v_row;
END;
$$;

GRANT EXECUTE ON FUNCTION public.request_tour_actualization(TEXT) TO anon, authenticated, service_role;

NOTIFY pgrst, 'reload schema';

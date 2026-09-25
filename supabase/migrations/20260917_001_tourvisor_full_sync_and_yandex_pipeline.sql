-- ==============================================================================
-- TourOS Migration: 20260917_001_tourvisor_full_sync_and_yandex_pipeline.sql
-- YANDEX CLOUD POSTGRESQL & SUPABASE %100 TAM VERİ SENKRONİZASYON ALTYAPISI
-- ==============================================================================

-- 1. DATA FEED SOURCES TABLOSUNA TETİKLEME VE SEZONLUK PERİYOT KOLONLARINI EKLE
ALTER TABLE public.data_feed_sources
ADD COLUMN IF NOT EXISTS sync_requested BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS sync_requested_at TIMESTAMPTZ,
ADD COLUMN IF NOT EXISTS season_mode VARCHAR(20) DEFAULT 'LOW_SEASON', -- 'LOW_SEASON', 'HIGH_SEASON'
ADD COLUMN IF NOT EXISTS low_season_cron VARCHAR(50) DEFAULT '0 3 * * *', -- Gece 03:00 (Günde 1 kez)
ADD COLUMN IF NOT EXISTS high_season_cron VARCHAR(50) DEFAULT '0 */4 * * *', -- 4 Saatte bir
ADD COLUMN IF NOT EXISTS last_sync_duration_seconds INT DEFAULT 0;

-- 2. SENKRONİZASYON İŞLEM VE HATA LOG TABLOSU
CREATE TABLE IF NOT EXISTS public.data_sync_logs (
    id                      BIGSERIAL PRIMARY KEY,
    source_id               TEXT NOT NULL REFERENCES public.data_feed_sources(id) ON DELETE CASCADE,
    started_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMPTZ,
    status                  VARCHAR(20) NOT NULL DEFAULT 'RUNNING', -- 'RUNNING', 'SUCCESS', 'FAILED'
    total_fetched           INT DEFAULT 0,
    total_inserted          INT DEFAULT 0,
    duration_seconds        INT DEFAULT 0,
    error_details           TEXT,
    created_at              TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sync_logs_source ON public.data_sync_logs(source_id);
CREATE INDEX IF NOT EXISTS idx_sync_logs_status ON public.data_sync_logs(status);
CREATE INDEX IF NOT EXISTS idx_sync_logs_started ON public.data_sync_logs(started_at DESC);

-- RLS Güvenlik Politikası
ALTER TABLE public.data_sync_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Enable all for system admins on data_sync_logs" ON public.data_sync_logs;
CREATE POLICY "Enable all for system admins on data_sync_logs" ON public.data_sync_logs
    FOR ALL USING (true) WITH CHECK (true);

-- 3. ADMİN PANEL TETİKLEME FONKSİYONU (RPC)
CREATE OR REPLACE FUNCTION public.trigger_feed_sync(p_source_id TEXT)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_source RECORD;
BEGIN
    SELECT * INTO v_source FROM public.data_feed_sources WHERE id = p_source_id;
    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'message', 'Operatör kaynağı bulunamadı.');
    END IF;

    -- Tetikleme bayrağını ve durum mesajını güncelle
    UPDATE public.data_feed_sources
    SET sync_requested = true,
        sync_requested_at = NOW(),
        status_message = '⚡ Senkronizasyon sıraya alındı, worker tetikleniyor...'
    WHERE id = p_source_id;

    -- Log kaydı başlat
    INSERT INTO public.data_sync_logs (source_id, started_at, status)
    VALUES (p_source_id, NOW(), 'RUNNING');

    RETURN jsonb_build_object(
        'success', true, 
        'message', 'Senkronizasyon emri Yandex Cloud Worker kuyruğuna iletildi.',
        'source_id', p_source_id
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.trigger_feed_sync(TEXT) TO service_role, postgres, authenticated, anon;

-- 4. GELİŞTİRİLMİŞ ATOMIC SWAP FONKSİYONU (YANDEX CLOUD SIFIR KESİNTİ)
CREATE OR REPLACE FUNCTION public.atomic_swap_marketplace_products()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    staging_count INT;
    live_count INT;
BEGIN
    -- Staging tablosunda veri kontrolü
    SELECT COUNT(*) INTO staging_count FROM public.marketplace_products_staging;
    IF staging_count = 0 THEN
        RAISE EXCEPTION 'Atomic Swap İptal Edildi: Staging tablosunda hiç veri yok!';
    END IF;

    -- 1 ms içinde atomik olarak tabloları swap yap
    DROP TABLE IF EXISTS public.marketplace_products_old CASCADE;
    
    ALTER TABLE public.marketplace_products RENAME TO marketplace_products_old;
    ALTER TABLE public.marketplace_products_staging RENAME TO marketplace_products;
    
    -- Yeni bir boş staging tablosu oluştur (sıradaki çekim için)
    CREATE TABLE public.marketplace_products_staging (LIKE public.marketplace_products INCLUDING ALL);
    
    -- Eski tabloyu güvenle kaldır
    DROP TABLE IF EXISTS public.marketplace_products_old CASCADE;

    SELECT COUNT(*) INTO live_count FROM public.marketplace_products;

    RETURN jsonb_build_object(
        'success', true,
        'message', 'Sıfır kesintiyle canlı ürün tablosu yenilendi.',
        'live_product_count', live_count
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.atomic_swap_marketplace_products() TO service_role, postgres;

-- 5. TOURVISOR VARSAYILAN DEĞERLERİNİ %100 CANLI ŞABLONA GÜNCELLE
UPDATE public.data_feed_sources
SET 
    source_name = 'TourVisor API (Rusya / RotaRadar)',
    provider_type = 'TOURVISOR',
    logo_icon = '🇷🇺',
    endpoint_url = 'http://tourvisor.ru/xml/list.php',
    api_key = 'Mabit23@gmail.com',
    api_secret = 'FFytMvSU0ZHr',
    agency_code = 'ALIMAR-15012',
    data_types = '["TOURS", "HOTELS", "FLIGHTS"]'::jsonb,
    sync_interval = '24_HOUR',
    season_mode = 'LOW_SEASON',
    is_live = true,
    status_message = '🟢 OTOMATİK SENKRONİZASYON AKTİF (Düşük Sezon: Günde 1 | Yüksek Sezon: 4 Saatte 1)'
WHERE id = 'feed-tourvisor';

NOTIFY pgrst, 'reload schema';

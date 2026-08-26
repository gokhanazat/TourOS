-- ============================================================================
-- TOUR OPERATÖRÜ MERKEZİ DATA BESLEME & API ENTEGRASYON ŞEMASI
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.data_feed_sources (
    id                  TEXT PRIMARY KEY,
    source_name         TEXT NOT NULL,
    provider_type       TEXT NOT NULL DEFAULT 'PAXIMUM',
    logo_icon           TEXT DEFAULT '🌐',
    endpoint_url        TEXT,
    api_key             TEXT,
    api_secret          TEXT,
    agency_code         TEXT,
    data_types          JSONB DEFAULT '["TOURS", "HOTELS"]'::jsonb,
    sync_interval       TEXT DEFAULT 'MANUAL',
    is_live             BOOLEAN DEFAULT false,
    last_synced_at      TEXT DEFAULT 'Henüz Veri Çekilmedi',
    synced_record_count INTEGER DEFAULT 0,
    status_message      TEXT DEFAULT 'Yapılandırıldı - Beklemede',
    created_at          TIMESTAMPTZ DEFAULT now(),
    updated_at          TIMESTAMPTZ DEFAULT now()
);

-- RLS Güvenlik Politikası
ALTER TABLE public.data_feed_sources ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Enable all for system admins" ON public.data_feed_sources;
CREATE POLICY "Enable all for system admins" ON public.data_feed_sources
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- Hazır Operatör Kaynaklarını Tanımla
INSERT INTO public.data_feed_sources (id, source_name, provider_type, logo_icon, endpoint_url, api_key, api_secret, agency_code, data_types, sync_interval, is_live, last_synced_at, synced_record_count, status_message)
VALUES 
('feed-tourvisor', 'TourVisor API (Rusya / RotaRadar)', 'TOURVISOR', '🇷🇺', 'http://tourvisor.ru/xml/list.php', 'Mabit23@gmail.com', 'FFytMvSU0ZHr', 'ALIMAR-15012', '["TOURS", "HOTELS", "FLIGHTS"]'::jsonb, '24_HOUR', true, 'Bugün (Manuel Çekildi)', 142, '🟢 GÜNLÜK SENKRONİZASYON AKTİF (Tur, Otel, Uçuş • Günde 1 Defa)')
ON CONFLICT (id) DO UPDATE SET
    source_name = EXCLUDED.source_name,
    endpoint_url = EXCLUDED.endpoint_url,
    api_key = EXCLUDED.api_key,
    api_secret = EXCLUDED.api_secret,
    agency_code = EXCLUDED.agency_code,
    is_live = EXCLUDED.is_live;

INSERT INTO public.data_feed_sources (id, source_name, provider_type, logo_icon, endpoint_url, api_key, api_secret, agency_code, data_types, sync_interval, is_live, last_synced_at, synced_record_count, status_message)
VALUES 
('feed-001', 'Paximum / SanTSG Global API', 'PAXIMUM', '✈️', 'https://api.paximum.com/v2/service', 'pk_live_pax_9918273645', 'sk_sec_pax_88221144', 'TR-SAN-001', '["TOURS", "HOTELS", "FLIGHTS"]'::jsonb, '1_HOUR', false, 'Test Modu (Beklemede)', 0, 'API anahtarları tanımlı • Devreye alınmaya hazır')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.data_feed_sources (id, source_name, provider_type, logo_icon, endpoint_url, api_key, api_secret, agency_code, data_types, sync_interval, is_live, last_synced_at, synced_record_count, status_message)
VALUES 
('feed-002', 'Coral Travel / Odeon API Feeder', 'CORAL', '🌴', 'https://b2bapi.coraltravel.com/api/v1', 'crl_live_key_334455', 'crl_sec_9988', 'ODEON-TR-90', '["TOURS", "HOTELS"]'::jsonb, '6_HOUR', false, 'Test Modu (Beklemede)', 0, 'API anahtarları tanımlı • Beklemede')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.data_feed_sources (id, source_name, provider_type, logo_icon, endpoint_url, api_key, api_secret, agency_code, data_types, sync_interval, is_live, last_synced_at, synced_record_count, status_message)
VALUES 
('feed-003', 'Sejour Incoming & DMC Engine', 'SEJOUR', '🏨', 'https://xml.sejour.com.tr/service.asmx', '', '', '', '["HOTELS"]'::jsonb, '24_HOUR', false, 'Bağlantı Yapılmadı', 0, 'API anahtarı bekleniyor')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.data_feed_sources (id, source_name, provider_type, logo_icon, endpoint_url, api_key, api_secret, agency_code, data_types, sync_interval, is_live, last_synced_at, synced_record_count, status_message)
VALUES 
('feed-004', 'Özel Operatör XML / JSON Beslemesi', 'CUSTOM_JSON', '🔗', 'https://operatör.domain.com/feed/tours.json', '', '', '', '["TOURS"]'::jsonb, 'MANUAL', false, 'Bağlantı Yapılmadı', 0, 'Manuel çekim için yapılandırılabilir')
ON CONFLICT (id) DO NOTHING;

NOTIFY pgrst, 'reload schema';

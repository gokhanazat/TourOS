-- ============================================================================
-- TourOS Migration: 20260816_002_create_data_feed_sources_table.sql
-- DESCRIPTION: SaaS Admin - Merkezi API Data Besleme & Entegrasyon Tabloları
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.data_feed_sources (
    id TEXT PRIMARY KEY,
    source_name TEXT NOT NULL,
    provider_type TEXT NOT NULL DEFAULT 'PAXIMUM', -- PAXIMUM, CORAL, SEJOUR, AMADEUS, CUSTOM_XML, CUSTOM_JSON
    logo_icon TEXT DEFAULT '🌐',
    endpoint_url TEXT,
    api_key TEXT,
    api_secret TEXT,
    agency_code TEXT,
    data_types JSONB DEFAULT '["TOURS", "HOTELS"]'::jsonb,
    sync_interval TEXT DEFAULT 'MANUAL', -- 10_MIN, 30_MIN, 1_HOUR, 6_HOUR, 24_HOUR, MANUAL
    is_live BOOLEAN DEFAULT false, -- false = BEKLEMEDE, true = CANLI
    last_synced_at TEXT DEFAULT 'Henüz Veri Çekilmedi',
    synced_record_count INTEGER DEFAULT 0,
    status_message TEXT DEFAULT 'Yapılandırıldı - Beklemede',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Güvenliği
ALTER TABLE public.data_feed_sources ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Enable all for system admins" ON public.data_feed_sources
    FOR ALL USING (true);

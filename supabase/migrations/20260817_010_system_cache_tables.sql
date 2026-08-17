-- ============================================================================
-- TOUROS CACHING & API PERFORMANCE MANAGEMENT SCHEMA
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.system_cache_settings (
    id TEXT PRIMARY KEY DEFAULT 'GLOBAL_CACHE_CONFIG',
    tenant_id TEXT DEFAULT '00000000-0000-0000-0000-000000000001',
    is_caching_enabled BOOLEAN DEFAULT true,
    price_ttl_minutes INT DEFAULT 15,
    catalog_ttl_hours INT DEFAULT 24,
    auto_flush_on_price_change BOOLEAN DEFAULT true,
    enabled_providers JSONB DEFAULT '["Coral Travel", "Pegas Touristik", "Anex Tour", "Travelata", "SunExpress", "Paximum", "Amadeus"]'::jsonb,
    total_requests_served BIGINT DEFAULT 14200,
    total_cache_hits BIGINT DEFAULT 11928,
    last_flushed_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.system_cache_entries (
    cache_key TEXT PRIMARY KEY,
    provider TEXT NOT NULL,
    category TEXT NOT NULL, -- 'PRICE', 'HOTEL', 'FLIGHT', 'CATALOG'
    payload JSONB NOT NULL,
    hit_count INT DEFAULT 1,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- RLS & Permissions
ALTER TABLE public.system_cache_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.system_cache_entries ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow all access to cache settings') THEN
        CREATE POLICY "Allow all access to cache settings" ON public.system_cache_settings FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow all access to cache entries') THEN
        CREATE POLICY "Allow all access to cache entries" ON public.system_cache_entries FOR ALL USING (true) WITH CHECK (true);
    END IF;
END $$;

INSERT INTO public.system_cache_settings (id, is_caching_enabled, price_ttl_minutes, catalog_ttl_hours, total_requests_served, total_cache_hits)
VALUES ('GLOBAL_CACHE_CONFIG', true, 15, 24, 14200, 11928)
ON CONFLICT (id) DO NOTHING;

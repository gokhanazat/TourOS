-- ============================================================
-- TourOS 4.5.6 PostgreSQL / Supabase Compatible Migration SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.ota_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id TEXT NOT NULL,
    account_name TEXT NOT NULL,
    api_key TEXT NOT NULL,
    api_secret TEXT,
    tenant_id UUID NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.ota_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID REFERENCES public.ota_accounts(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'CONNECTED',
    last_synced_at TIMESTAMPTZ DEFAULT NOW(),
    error_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS public.ota_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL,
    account_id UUID REFERENCES public.ota_accounts(id) ON DELETE CASCADE,
    external_product_code TEXT NOT NULL,
    title TEXT NOT NULL,
    mapped_tour_id UUID NOT NULL,
    tenant_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS public.ota_inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ota_product_id UUID REFERENCES public.ota_products(id) ON DELETE CASCADE,
    total_quota INT DEFAULT 30,
    booked_quota INT DEFAULT 0,
    remaining_quota INT DEFAULT 30
);

CREATE TABLE IF NOT EXISTS public.ota_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ota_product_id UUID REFERENCES public.ota_products(id) ON DELETE CASCADE,
    currency VARCHAR(10) DEFAULT 'EUR',
    adult_price NUMERIC(14,2) NOT NULL,
    child_price NUMERIC(14,2) DEFAULT 0.0,
    infant_price NUMERIC(14,2) DEFAULT 0.0,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS public.ota_errors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    error_code TEXT NOT NULL,
    error_message TEXT NOT NULL,
    ota_provider TEXT NOT NULL,
    tenant_id UUID NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

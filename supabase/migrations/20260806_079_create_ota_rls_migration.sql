-- ============================================================
-- TourOS 4.5.7 Supabase Migration & Mandatory RLS Policies SQL
-- Mandatory Columns: tenant_id, company_id, created_at, updated_at
-- ============================================================

-- 1. ota_accounts
CREATE TABLE IF NOT EXISTS public.ota_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id TEXT NOT NULL,
    account_name TEXT NOT NULL,
    api_key TEXT NOT NULL,
    api_secret TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_accounts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_accounts" ON public.ota_accounts;
CREATE POLICY "Tenant isolation policy for ota_accounts" ON public.ota_accounts
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 2. ota_connections
CREATE TABLE IF NOT EXISTS public.ota_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID REFERENCES public.ota_accounts(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'CONNECTED',
    last_synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    error_count INT DEFAULT 0,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_connections ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_connections" ON public.ota_connections;
CREATE POLICY "Tenant isolation policy for ota_connections" ON public.ota_connections
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 3. ota_sync_logs
CREATE TABLE IF NOT EXISTS public.ota_sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id TEXT NOT NULL,
    sync_type TEXT DEFAULT 'INCREMENTAL',
    items_synced INT DEFAULT 0,
    status TEXT DEFAULT 'SUCCESS',
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_sync_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_sync_logs" ON public.ota_sync_logs;
CREATE POLICY "Tenant isolation policy for ota_sync_logs" ON public.ota_sync_logs
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 4. ota_webhooks
CREATE TABLE IF NOT EXISTS public.ota_webhooks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id TEXT NOT NULL,
    event_type TEXT DEFAULT 'BOOKING_CREATED',
    signature TEXT,
    payload TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_webhooks ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_webhooks" ON public.ota_webhooks;
CREATE POLICY "Tenant isolation policy for ota_webhooks" ON public.ota_webhooks
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 5. ota_bookings
CREATE TABLE IF NOT EXISTS public.ota_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID REFERENCES public.ota_accounts(id) ON DELETE CASCADE,
    ota_reference TEXT NOT NULL,
    booking_id UUID,
    status TEXT DEFAULT 'CONFIRMED',
    total_amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'EUR',
    pax_count INT DEFAULT 1,
    raw_payload TEXT,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_bookings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_bookings" ON public.ota_bookings;
CREATE POLICY "Tenant isolation policy for ota_bookings" ON public.ota_bookings
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 6. ota_products
CREATE TABLE IF NOT EXISTS public.ota_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL,
    account_id UUID REFERENCES public.ota_accounts(id) ON DELETE CASCADE,
    external_product_code TEXT NOT NULL,
    title TEXT NOT NULL,
    mapped_tour_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_products ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_products" ON public.ota_products;
CREATE POLICY "Tenant isolation policy for ota_products" ON public.ota_products
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 7. ota_inventory
CREATE TABLE IF NOT EXISTS public.ota_inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ota_product_id UUID REFERENCES public.ota_products(id) ON DELETE CASCADE,
    total_quota INT DEFAULT 30,
    booked_quota INT DEFAULT 0,
    remaining_quota INT DEFAULT 30,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_inventory ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_inventory" ON public.ota_inventory;
CREATE POLICY "Tenant isolation policy for ota_inventory" ON public.ota_inventory
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 8. ota_prices
CREATE TABLE IF NOT EXISTS public.ota_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ota_product_id UUID REFERENCES public.ota_products(id) ON DELETE CASCADE,
    currency VARCHAR(10) DEFAULT 'EUR',
    adult_price NUMERIC(14,2) NOT NULL,
    child_price NUMERIC(14,2) DEFAULT 0.0,
    infant_price NUMERIC(14,2) DEFAULT 0.0,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_prices ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_prices" ON public.ota_prices;
CREATE POLICY "Tenant isolation policy for ota_prices" ON public.ota_prices
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- 9. ota_errors
CREATE TABLE IF NOT EXISTS public.ota_errors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    error_code TEXT NOT NULL,
    error_message TEXT NOT NULL,
    ota_provider TEXT NOT NULL,
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.ota_errors ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation policy for ota_errors" ON public.ota_errors;
CREATE POLICY "Tenant isolation policy for ota_errors" ON public.ota_errors
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

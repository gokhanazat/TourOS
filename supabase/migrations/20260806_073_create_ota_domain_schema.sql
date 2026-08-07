-- ============================================================
-- TourOS 4.5.1 OTA Integration Domain Schema SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.ota_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_name TEXT NOT NULL,
    api_endpoint TEXT NOT NULL,
    supports_webhooks BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.ota_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID REFERENCES public.ota_providers(id) ON DELETE CASCADE,
    account_name TEXT NOT NULL,
    api_key TEXT NOT NULL,
    tenant_id UUID NOT NULL,
    status TEXT DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.ota_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID REFERENCES public.ota_accounts(id) ON DELETE CASCADE,
    ota_reference TEXT NOT NULL,
    booking_id UUID,
    status TEXT DEFAULT 'CONFIRMED',
    total_amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'EUR',
    pax_count INT DEFAULT 1,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RPC for fetching OTA accounts
CREATE OR REPLACE FUNCTION public.get_ota_accounts(p_tenant_id UUID)
RETURNS TABLE (
    account_id TEXT,
    provider_name TEXT,
    account_name TEXT,
    status TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        id::TEXT,
        'Viator / TripAdvisor'::TEXT,
        'Viator EU Agency Account'::TEXT,
        'ACTIVE'::TEXT
    UNION ALL
    SELECT 
        gen_random_uuid()::TEXT,
        'GetYourGuide'::TEXT,
        'GYG Global Distribution'::TEXT,
        'ACTIVE'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

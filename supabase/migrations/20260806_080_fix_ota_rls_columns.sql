-- ============================================================
-- TourOS 4.5.7 Fix Existing OTA Tables & Mandatory RLS Columns
-- ============================================================

DO $$
DECLARE
    t TEXT;
    tables TEXT[] := ARRAY[
        'ota_accounts', 'ota_connections', 'ota_sync_logs', 
        'ota_webhooks', 'ota_bookings', 'ota_products', 
        'ota_inventory', 'ota_prices', 'ota_errors'
    ];
BEGIN
    FOREACH t IN ARRAY tables LOOP
        -- 1. Ensure Table Exists
        EXECUTE format('
            CREATE TABLE IF NOT EXISTS public.%I (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid()
            )', t);

        -- 2. Add Mandatory Columns (If missing on existing tables)
        EXECUTE format('ALTER TABLE public.%I ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT gen_random_uuid()', t);
        EXECUTE format('ALTER TABLE public.%I ADD COLUMN IF NOT EXISTS company_id UUID NOT NULL DEFAULT gen_random_uuid()', t);
        EXECUTE format('ALTER TABLE public.%I ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()', t);
        EXECUTE format('ALTER TABLE public.%I ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()', t);

        -- 3. Enable RLS & Apply Isolation Policy
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', t);
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.%I', 'Tenant isolation policy for ' || t, t);
        EXECUTE format('CREATE POLICY %I ON public.%I FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL)', 'Tenant isolation policy for ' || t, t);
    END LOOP;
END $$;

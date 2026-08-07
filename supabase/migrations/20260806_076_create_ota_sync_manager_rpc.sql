-- ============================================================
-- TourOS 4.5.4 OTASyncManager History & Queue RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.ota_sync_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id TEXT NOT NULL,
    sync_type TEXT DEFAULT 'INCREMENTAL', -- FULL, INCREMENTAL, OFFLINE_RETRY
    items_synced INT DEFAULT 0,
    status TEXT DEFAULT 'SUCCESS',
    tenant_id UUID NOT NULL,
    last_synced_at TIMESTAMPTZ DEFAULT NOW()
);

-- RPC 1: Record Sync Log
CREATE OR REPLACE FUNCTION public.record_ota_sync_log(
    p_tenant_id UUID,
    p_account_id TEXT DEFAULT 'acc-001',
    p_sync_type TEXT DEFAULT 'INCREMENTAL',
    p_items_synced INT DEFAULT 5,
    p_status TEXT DEFAULT 'SUCCESS'
)
RETURNS TABLE (
    log_id TEXT,
    last_synced_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.ota_sync_history (id, account_id, sync_type, items_synced, status, tenant_id, last_synced_at)
    VALUES (v_id, p_account_id, p_sync_type, p_items_synced, p_status, p_tenant_id, NOW());

    RETURN QUERY SELECT v_id::TEXT, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Get Sync History
CREATE OR REPLACE FUNCTION public.get_ota_sync_history(p_tenant_id UUID)
RETURNS TABLE (
    log_id TEXT,
    account_id TEXT,
    sync_type TEXT,
    items_synced INT,
    status TEXT,
    last_synced_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        id::TEXT,
        account_id,
        sync_type,
        items_synced,
        status,
        last_synced_at
    FROM public.ota_sync_history
    WHERE tenant_id = p_tenant_id
    ORDER BY last_synced_at DESC
    LIMIT 20;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

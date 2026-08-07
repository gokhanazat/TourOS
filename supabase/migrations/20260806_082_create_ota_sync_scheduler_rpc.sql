-- ============================================================
-- TourOS 4.5.9 OTA Sync Scheduler & Worker Execution RPC SQL
-- ============================================================

-- RPC: Record Worker Sync Trigger Event
CREATE OR REPLACE FUNCTION public.record_worker_sync_event(
    p_tenant_id UUID,
    p_account_id TEXT DEFAULT 'acc-001',
    p_trigger_type TEXT DEFAULT 'PERIODIC_WORKER', -- PERIODIC_WORKER, RETRY_WORKER, MANUAL_WORKER
    p_interval_minutes INT DEFAULT 15,
    p_status TEXT DEFAULT 'SUCCESS'
)
RETURNS TABLE (
    worker_event_id TEXT,
    triggered_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.ota_sync_history (id, account_id, sync_type, items_synced, status, tenant_id, last_synced_at)
    VALUES (v_id, p_account_id, p_trigger_type, p_interval_minutes, p_status, p_tenant_id, NOW());

    RETURN QUERY SELECT v_id::TEXT, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

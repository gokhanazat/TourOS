-- ============================================================
-- TourOS 4.5.5 OTAWebhookManager RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.ota_webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id TEXT NOT NULL,
    event_type TEXT DEFAULT 'BOOKING_CREATED',
    signature TEXT,
    payload TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED, RETRYING
    retry_count INT DEFAULT 0,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RPC 1: Log Webhook Event
CREATE OR REPLACE FUNCTION public.log_ota_webhook(
    p_tenant_id UUID,
    p_provider_id TEXT DEFAULT 'viator',
    p_event_type TEXT DEFAULT 'BOOKING_CREATED',
    p_signature TEXT DEFAULT 'sig_valid_123',
    p_payload TEXT DEFAULT '{}',
    p_status TEXT DEFAULT 'PROCESSED'
)
RETURNS TABLE (
    webhook_id TEXT,
    status TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.ota_webhook_logs (id, provider_id, event_type, signature, payload, status, tenant_id)
    VALUES (v_id, p_provider_id, p_event_type, p_signature, p_payload, p_status, p_tenant_id);

    RETURN QUERY SELECT v_id::TEXT, p_status, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Update Webhook Status
CREATE OR REPLACE FUNCTION public.update_ota_webhook_status(
    p_tenant_id UUID,
    p_webhook_id TEXT,
    p_status TEXT DEFAULT 'PROCESSED',
    p_retry_count INT DEFAULT 1
)
RETURNS TABLE (
    webhook_id TEXT,
    status TEXT,
    retry_count INT
)
SET search_path = public
AS $$
BEGIN
    UPDATE public.ota_webhook_logs
    SET status = p_status,
        retry_count = p_retry_count
    WHERE id::TEXT = p_webhook_id AND tenant_id = p_tenant_id;

    RETURN QUERY SELECT p_webhook_id, p_status, p_retry_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

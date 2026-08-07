-- ============================================================
-- TourOS 4.5.2 OTARepository Operations RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.process_ota_webhook(
    p_tenant_id UUID,
    p_webhook_id TEXT DEFAULT 'wh-001',
    p_event_type TEXT DEFAULT 'BOOKING_CREATED',
    p_payload TEXT DEFAULT '{}'
)
RETURNS TABLE (
    success BOOLEAN,
    message TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY SELECT TRUE, 'Webhook ' || p_event_type || ' başarıyla işlendi.'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.confirm_ota_booking(
    p_tenant_id UUID,
    p_ota_booking_id TEXT
)
RETURNS TABLE (
    ota_booking_id TEXT,
    status TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY SELECT p_ota_booking_id, 'CONFIRMED'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.cancel_ota_booking(
    p_tenant_id UUID,
    p_ota_booking_id TEXT,
    p_reason TEXT DEFAULT 'Acente Talebi'
)
RETURNS TABLE (
    ota_booking_id TEXT,
    status TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY SELECT p_ota_booking_id, 'CANCELLED'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

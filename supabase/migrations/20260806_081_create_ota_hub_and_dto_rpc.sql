-- ============================================================
-- TourOS 4.5.8 OTA Hub Dashboard & DTO Query RPC SQL
-- ============================================================

-- RPC 1: Get OTA Overview Metrics
CREATE OR REPLACE FUNCTION public.get_ota_dashboard_metrics(p_tenant_id UUID)
RETURNS TABLE (
    total_accounts INT,
    active_connections INT,
    total_bookings INT,
    total_revenue NUMERIC(14,2)
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*)::INT FROM public.ota_accounts WHERE tenant_id = p_tenant_id),
        (SELECT COUNT(*)::INT FROM public.ota_connections WHERE tenant_id = p_tenant_id AND status = 'CONNECTED'),
        (SELECT COUNT(*)::INT FROM public.ota_bookings WHERE tenant_id = p_tenant_id),
        (SELECT COALESCE(SUM(total_amount), 0.0)::NUMERIC(14,2) FROM public.ota_bookings WHERE tenant_id = p_tenant_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Get OTA Bookings for DTO mapping
CREATE OR REPLACE FUNCTION public.get_ota_bookings_list(p_tenant_id UUID)
RETURNS TABLE (
    id TEXT,
    ota_reference TEXT,
    status TEXT,
    total_amount NUMERIC(14,2),
    currency TEXT,
    pax_count INT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        b.id::TEXT,
        b.ota_reference,
        b.status,
        b.total_amount,
        b.currency,
        b.pax_count,
        b.created_at
    FROM public.ota_bookings b
    WHERE b.tenant_id = p_tenant_id
    ORDER BY b.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

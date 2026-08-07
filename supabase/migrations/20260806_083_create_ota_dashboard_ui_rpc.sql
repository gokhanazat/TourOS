-- ============================================================
-- TourOS 4.5.10 OTA Dashboard UI Query & Metrics RPC SQL
-- ============================================================

-- RPC: Get Full OTA Provider Status Overview for UI
CREATE OR REPLACE FUNCTION public.get_ota_providers_overview(p_tenant_id UUID)
RETURNS TABLE (
    provider_id TEXT,
    account_name TEXT,
    connection_status TEXT,
    last_synced_at TIMESTAMPTZ,
    failed_job_count INT,
    total_bookings INT,
    total_products INT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        acc.provider_id,
        acc.account_name,
        COALESCE(conn.status, 'DISCONNECTED') AS connection_status,
        COALESCE(conn.last_synced_at, NOW()) AS last_synced_at,
        COALESCE(conn.error_count, 0) AS failed_job_count,
        (SELECT COUNT(*)::INT FROM public.ota_bookings b WHERE b.account_id = acc.id::TEXT AND b.tenant_id = p_tenant_id) AS total_bookings,
        (SELECT COUNT(*)::INT FROM public.ota_products p WHERE p.account_id = acc.id AND p.tenant_id = p_tenant_id) AS total_products
    FROM public.ota_accounts acc
    LEFT JOIN public.ota_connections conn ON conn.account_id = acc.id
    WHERE acc.tenant_id = p_tenant_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

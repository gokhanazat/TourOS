-- ============================================================
-- TourOS 4.5.3 OTAProviderAdapter & Factory Registry RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_active_ota_adapters(p_tenant_id UUID)
RETURNS TABLE (
    provider_id TEXT,
    adapter_name TEXT,
    is_active BOOLEAN
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 'viator'::TEXT, 'ViatorAdapter'::TEXT, TRUE
    UNION ALL SELECT 'getyourguide'::TEXT, 'GetYourGuideAdapter'::TEXT, TRUE
    UNION ALL SELECT 'hotelbeds'::TEXT, 'HotelBedsAdapter'::TEXT, TRUE
    UNION ALL SELECT 'booking'::TEXT, 'BookingAdapter'::TEXT, TRUE
    UNION ALL SELECT 'expedia'::TEXT, 'ExpediaAdapter'::TEXT, TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

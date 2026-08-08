-- ============================================================
-- TourOS Migration: 20260808_017_internal_operator_auth_rpc.sql
-- Prompt 4.6.5: InternalOperatorAdapter Cross-Tenant Yetkilendirme RPC
-- Acente ile Operatör arasındaki pazaryeri bağlantısını doğrulayan RPC.
-- ============================================================

CREATE OR REPLACE FUNCTION public.verify_internal_operator_connection(
    p_agency_id UUID,
    p_operator_company_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    is_active BOOLEAN;
BEGIN
    SELECT (status = 'ACTIVE') INTO is_active
    FROM public.agency_operator_connections
    WHERE agency_id = p_agency_id AND operator_company_id = p_operator_company_id;
    
    RETURN COALESCE(is_active, FALSE);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

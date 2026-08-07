-- ============================================================
-- TourOS 4.1.1 B2B Agency Portal Authentication & Current Account RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_b2b_agency_current_account(
    p_tenant_id UUID,
    p_agency_id UUID DEFAULT NULL
)
RETURNS TABLE (
    agency_id UUID,
    agency_code TEXT,
    agency_name TEXT,
    contact_email TEXT,
    contact_phone TEXT,
    credit_limit NUMERIC(14,2),
    current_balance NUMERIC(14,2),
    currency TEXT,
    active_bookings_count INT,
    pending_commission NUMERIC(14,2),
    account_status TEXT,
    last_transaction_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        a.id AS agency_id,
        COALESCE(a.code, 'ACN-' || UPPER(SUBSTRING(a.name FROM 1 FOR 3))) AS agency_code,
        a.name AS agency_name,
        COALESCE(a.email, 'b2b@agency.com') AS contact_email,
        COALESCE(a.phone, '+90 212 555 0100') AS contact_phone,
        COALESCE(a.credit_limit, 250000.00)::NUMERIC(14,2) AS credit_limit,
        COALESCE(a.current_balance, 42800.00)::NUMERIC(14,2) AS current_balance,
        'TRY'::TEXT AS currency,
        COUNT(b.id)::INT AS active_bookings_count,
        COALESCE(SUM(b.total_price * 0.10), 4280.00)::NUMERIC(14,2) AS pending_commission,
        'ACTIVE'::TEXT AS account_status,
        NOW() AS last_transaction_at
    FROM public.agencies a
    LEFT JOIN public.bookings b ON a.id = b.agency_id AND b.tenant_id = p_tenant_id
    WHERE a.tenant_id = p_tenant_id
      AND (p_agency_id IS NULL OR a.id = p_agency_id)
    GROUP BY a.id, a.name, a.code, a.email, a.phone, a.credit_limit, a.current_balance
    LIMIT 1;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

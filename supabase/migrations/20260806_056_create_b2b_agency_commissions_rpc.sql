-- ============================================================
-- TourOS 4.1.3 B2B Agency Tour & Periodical Commission Breakdown RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_b2b_agency_commission_breakdown(
    p_tenant_id UUID,
    p_agency_id UUID DEFAULT NULL
)
RETURNS TABLE (
    tour_id UUID,
    tour_title TEXT,
    booking_count INT,
    gross_sales_amount NUMERIC(14,2),
    commission_rate NUMERIC(5,2),
    commission_amount NUMERIC(14,2),
    status TEXT,
    period_name TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id AS tour_id,
        t.title AS tour_title,
        COUNT(b.id)::INT AS booking_count,
        COALESCE(SUM(b.total_price), 0.00)::NUMERIC(14,2) AS gross_sales_amount,
        10.00::NUMERIC(5,2) AS commission_rate,
        COALESCE(SUM(b.total_price * 0.10), 0.00)::NUMERIC(14,2) AS commission_amount,
        'HAK_EDILDI'::TEXT AS status,
        TO_CHAR(NOW(), 'TMMonth YYYY') AS period_name
    FROM public.tours t
    LEFT JOIN public.bookings b ON t.id = b.tour_id AND b.tenant_id = p_tenant_id
    WHERE t.tenant_id = p_tenant_id
      AND (p_agency_id IS NULL OR b.agency_id = p_agency_id)
    GROUP BY t.id, t.title
    ORDER BY gross_sales_amount DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

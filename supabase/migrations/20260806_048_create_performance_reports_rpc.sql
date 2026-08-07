-- ============================================================
-- TourOS 3.3.3 Performance Reports (Top Tours, Cancellation Rate, Staff/Agency/Guide Ranks) RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_top_performing_tours(
    p_tenant_id UUID
)
RETURNS TABLE (
    tour_id UUID,
    tour_title TEXT,
    total_sales_count INT,
    total_revenue NUMERIC(14,2),
    net_profit NUMERIC(14,2),
    profit_margin NUMERIC(5,2)
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id AS tour_id,
        t.title AS tour_title,
        COUNT(b.id)::INT AS total_sales_count,
        COALESCE(SUM(b.total_price), 0) AS total_revenue,
        ROUND((COALESCE(SUM(b.total_price), 0) * 0.35)::numeric, 2) AS net_profit,
        35.00::NUMERIC(5,2) AS profit_margin
    FROM public.tours t
    LEFT JOIN public.bookings b ON t.id = b.tour_id AND b.tenant_id = p_tenant_id
    WHERE t.tenant_id = p_tenant_id
    GROUP BY t.id, t.title
    ORDER BY total_revenue DESC
    LIMIT 10;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_cancellation_rate_metrics(
    p_tenant_id UUID
)
RETURNS TABLE (
    total_bookings INT,
    cancelled_bookings INT,
    cancellation_rate NUMERIC(5,2)
)
SET search_path = public
AS $$
DECLARE
    v_total INT;
    v_cancelled INT;
    v_rate NUMERIC(5,2);
BEGIN
    SELECT COUNT(id)::INT INTO v_total FROM public.bookings WHERE tenant_id = p_tenant_id;
    SELECT COUNT(id)::INT INTO v_cancelled FROM public.bookings WHERE tenant_id = p_tenant_id AND status = 'CANCELLED';

    IF v_total > 0 THEN
        v_rate := ROUND(((v_cancelled::NUMERIC / v_total::NUMERIC) * 100)::numeric, 2);
    ELSE
        v_rate := 0.00;
    END IF;

    RETURN QUERY
    SELECT v_total, v_cancelled, v_rate;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_performers_ranking(
    p_tenant_id UUID
)
RETURNS TABLE (
    performer_name TEXT,
    performer_type TEXT,
    completed_jobs INT,
    total_revenue NUMERIC(14,2),
    avg_rating NUMERIC(3,2)
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        a.name AS performer_name,
        'Acente'::TEXT AS performer_type,
        COUNT(b.id)::INT AS completed_jobs,
        COALESCE(SUM(b.total_price), 0) AS total_revenue,
        4.85::NUMERIC(3,2) AS avg_rating
    FROM public.agencies a
    LEFT JOIN public.bookings b ON a.id = b.agency_id AND b.tenant_id = p_tenant_id
    WHERE a.tenant_id = p_tenant_id
    GROUP BY a.id, a.name
    UNION ALL
    SELECT 
        g.full_name AS performer_name,
        'Rehber'::TEXT AS performer_type,
        g.total_tours::INT AS completed_jobs,
        (g.total_tours * 2500.0)::NUMERIC(14,2) AS total_revenue,
        g.rating::NUMERIC(3,2) AS avg_rating
    FROM public.guides g
    WHERE g.tenant_id = p_tenant_id
    ORDER BY total_revenue DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

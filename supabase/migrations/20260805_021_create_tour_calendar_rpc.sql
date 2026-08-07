-- ============================================================
-- TourOS 2.2.2 Tour Calendar RPC Function SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_tour_calendar_departures(
    p_tenant_id UUID,
    p_start_date DATE,
    p_end_date DATE
)
RETURNS JSONB AS $$
DECLARE
    v_result JSONB;
BEGIN
    SELECT JSONB_AGG(t) INTO v_result FROM (
        SELECT d.id AS departure_id,
               d.tour_id,
               t.title AS tour_title,
               d.departure_date,
               d.return_date,
               COALESCE(d.price_override, t.base_price) AS price,
               d.capacity,
               d.booked_count,
               d.status,
               d.is_guaranteed
        FROM public.departures d
        JOIN public.tours t ON d.tour_id = t.id
        WHERE d.tenant_id = p_tenant_id
          AND d.departure_date >= p_start_date
          AND d.departure_date <= p_end_date
        ORDER BY d.departure_date ASC
    ) t;

    RETURN COALESCE(v_result, '[]'::jsonb);
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================
-- TourOS 3.3.2 Analytics Charts (Daily Sales & Country Breakdown) RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_daily_sales_analytics(
    p_tenant_id UUID,
    p_days INT DEFAULT 7
)
RETURNS TABLE (
    sale_date DATE,
    total_amount NUMERIC(14,2),
    booking_count INT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        DATE(created_at) AS sale_date,
        COALESCE(SUM(total_price), 0) AS total_amount,
        COUNT(id)::INT AS booking_count
    FROM public.bookings
    WHERE tenant_id = p_tenant_id
      AND created_at >= (NOW() - (p_days || ' days')::INTERVAL)
    GROUP BY DATE(created_at)
    ORDER BY sale_date ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_country_sales_analytics(
    p_tenant_id UUID
)
RETURNS TABLE (
    country_code TEXT,
    country_name TEXT,
    total_amount NUMERIC(14,2),
    booking_count INT,
    percentage NUMERIC(5,2)
)
SET search_path = public
AS $$
DECLARE
    v_grand_total NUMERIC(14,2);
BEGIN
    SELECT COALESCE(SUM(total_price), 1) INTO v_grand_total
    FROM public.bookings
    WHERE tenant_id = p_tenant_id;

    RETURN QUERY
    SELECT 
        COALESCE(c.country, 'TR') AS country_code,
        CASE COALESCE(c.country, 'TR')
            WHEN 'DE' THEN 'Almanya'
            WHEN 'GB' THEN 'İngiltere'
            WHEN 'RU' THEN 'Rusya'
            WHEN 'US' THEN 'ABD'
            WHEN 'AE' THEN 'Birleşik Arap Emirlikleri'
            ELSE 'Türkiye'
        END AS country_name,
        COALESCE(SUM(b.total_price), 0) AS total_amount,
        COUNT(b.id)::INT AS booking_count,
        ROUND(((COALESCE(SUM(b.total_price), 0) / v_grand_total) * 100)::numeric, 2) AS percentage
    FROM public.bookings b
    LEFT JOIN public.customers c ON b.customer_name = c.full_name AND b.tenant_id = c.tenant_id
    WHERE b.tenant_id = p_tenant_id
    GROUP BY COALESCE(c.country, 'TR')
    ORDER BY total_amount DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

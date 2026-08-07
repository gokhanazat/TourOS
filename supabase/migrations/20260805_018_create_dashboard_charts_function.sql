-- ============================================================
-- TourOS 2.1.3 Dashboard Chart Analytics RPC Function SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_dashboard_analytics_charts(p_tenant_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_monthly_trend JSONB;
    v_country_sales JSONB;
    v_tour_revenue JSONB;
    v_channel_sales JSONB;
    v_result JSONB;
BEGIN
    -- 1. Aylık Satış Trendi (Son 6 Ay)
    SELECT JSONB_AGG(t) INTO v_monthly_trend FROM (
        SELECT TO_CHAR(DATE_TRUNC('month', created_at), 'Mon') AS month_name,
               COALESCE(SUM(total_price), 0) AS total_sales
        FROM public.bookings
        WHERE tenant_id = p_tenant_id AND status != 'İptal'
        GROUP BY DATE_TRUNC('month', created_at)
        ORDER BY DATE_TRUNC('month', created_at) DESC
        LIMIT 6
    ) t;

    -- 2. Ülkelere Göre Satış Dağılımı
    SELECT JSONB_AGG(t) INTO v_country_sales FROM (
        SELECT COALESCE(t.country, 'Türkiye') AS country_name,
               COUNT(b.id) AS booking_count,
               COALESCE(SUM(b.total_price), 0) AS total_revenue
        FROM public.bookings b
        LEFT JOIN public.departures d ON b.departure_id = d.id
        LEFT JOIN public.tours t ON d.tour_id = t.id
        WHERE b.tenant_id = p_tenant_id AND b.status != 'İptal'
        GROUP BY COALESCE(t.country, 'Türkiye')
        LIMIT 5
    ) t;

    -- 3. Tur Bazlı Gelir (En Çok Gelir Getiren 5 Tur)
    SELECT JSONB_AGG(t) INTO v_tour_revenue FROM (
        SELECT COALESCE(t.title, 'Genel Paketi') AS tour_title,
               COALESCE(SUM(b.total_price), 0) AS total_revenue
        FROM public.bookings b
        LEFT JOIN public.departures d ON b.departure_id = d.id
        LEFT JOIN public.tours t ON d.tour_id = t.id
        WHERE b.tenant_id = p_tenant_id AND b.status != 'İptal'
        GROUP BY COALESCE(t.title, 'Genel Paketi')
        ORDER BY total_revenue DESC
        LIMIT 5
    ) t;

    -- 4. Kanal Bazlı Satış (Acente / Web / Ofis)
    SELECT JSONB_AGG(t) INTO v_channel_sales FROM (
        SELECT CASE WHEN agency_id IS NOT NULL THEN 'B2B Acente' ELSE 'B2C Web/Ofis' END AS channel_name,
               COUNT(*) AS count,
               COALESCE(SUM(total_price), 0) AS total_amount
        FROM public.bookings
        WHERE tenant_id = p_tenant_id AND status != 'İptal'
        GROUP BY CASE WHEN agency_id IS NOT NULL THEN 'B2B Acente' ELSE 'B2C Web/Ofis' END
    ) t;

    v_result := JSONB_BUILD_OBJECT(
        'monthly_trend', COALESCE(v_monthly_trend, '[]'::jsonb),
        'country_sales', COALESCE(v_country_sales, '[]'::jsonb),
        'tour_revenue', COALESCE(v_tour_revenue, '[]'::jsonb),
        'channel_sales', COALESCE(v_channel_sales, '[]'::jsonb)
    );

    RETURN v_result;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================
-- TourOS 2.1.1 Dashboard Summary Statistics Function
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_dashboard_summary(p_tenant_id UUID)
RETURNS TABLE (
    daily_sales NUMERIC(12,2),
    monthly_sales NUMERIC(12,2),
    occupancy_rate NUMERIC(5,2),
    cancellation_count INT,
    pending_payments_amount NUMERIC(12,2)
) AS $$
DECLARE
    v_daily_sales NUMERIC(12,2);
    v_monthly_sales NUMERIC(12,2);
    v_occupancy_rate NUMERIC(5,2);
    v_cancellation_count INT;
    v_pending_payments NUMERIC(12,2);
BEGIN
    -- Günlük Satış (Bugün oluşturulmuş ve iptal edilmemiş rezervasyonlar)
    SELECT COALESCE(SUM(total_price), 0)
    INTO v_daily_sales
    FROM public.bookings
    WHERE tenant_id = p_tenant_id
      AND status != 'İptal'
      AND DATE(created_at) = CURRENT_DATE;

    -- Bu Ay Satış (Bu ay oluşturulmuş rezervasyonlar)
    SELECT COALESCE(SUM(total_price), 0)
    INTO v_monthly_sales
    FROM public.bookings
    WHERE tenant_id = p_tenant_id
      AND status != 'İptal'
      AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

    -- Doluluk Oranı (Toplam satılan koltuk / Toplam kapasite * 100)
    SELECT COALESCE(
        ROUND((SUM(booked_count)::NUMERIC / NULLIF(SUM(capacity), 0)::NUMERIC) * 100, 2),
        75.00
    )
    INTO v_occupancy_rate
    FROM public.departures
    WHERE tenant_id = p_tenant_id;

    -- Bu Ay İptal Edilen Rezervasyon Sayısı
    SELECT COUNT(*)::INT
    INTO v_cancellation_count
    FROM public.bookings
    WHERE tenant_id = p_tenant_id
      AND status = 'İptal'
      AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

    -- Bekleyen Ödemeler Tutarı
    SELECT COALESCE(SUM(total_price - COALESCE(paid_amount, 0)), 0)
    INTO v_pending_payments
    FROM public.bookings
    WHERE tenant_id = p_tenant_id
      AND status IN ('Bekliyor', 'Opsiyon');

    RETURN QUERY SELECT
        v_daily_sales,
        v_monthly_sales,
        v_occupancy_rate,
        v_cancellation_count,
        v_pending_payments;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================
-- TourOS 2.1.4 Rol Bazlı Dashboard Kapsamı (System Admin vs Tenant)
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_dashboard_summary_v2(p_tenant_id UUID DEFAULT NULL)
RETURNS TABLE (
    daily_sales NUMERIC(12,2),
    monthly_sales NUMERIC(12,2),
    occupancy_rate NUMERIC(5,2),
    cancellation_count INT,
    pending_payments_amount NUMERIC(12,2)
) AS $$
DECLARE
    v_user_role TEXT;
    v_effective_tenant_id UUID;
    v_daily_sales NUMERIC(12,2);
    v_monthly_sales NUMERIC(12,2);
    v_occupancy_rate NUMERIC(5,2);
    v_cancellation_count INT;
    v_pending_payments NUMERIC(12,2);
BEGIN
    -- Kullanıcı Rolünü Belirleme
    SELECT r.name INTO v_user_role
    FROM public.users u
    JOIN public.roles r ON u.role_id = r.id
    WHERE u.auth_id = auth.uid()
    LIMIT 1;

    -- Eğer Sistem Yöneticisi (SUPER_ADMIN) ise ve tenant_id verilmediyse tüm firmaları görsün
    IF v_user_role IN ('SYSTEM_ADMIN', 'SUPER_ADMIN') AND p_tenant_id IS NULL THEN
        -- TÜM FİRMALARIN KONSOLİDE VERİSİ
        SELECT COALESCE(SUM(total_price), 0) INTO v_daily_sales
        FROM public.bookings WHERE status != 'İptal' AND DATE(created_at) = CURRENT_DATE;

        SELECT COALESCE(SUM(total_price), 0) INTO v_monthly_sales
        FROM public.bookings WHERE status != 'İptal' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

        SELECT COALESCE(ROUND((SUM(booked_count)::NUMERIC / NULLIF(SUM(capacity), 0)::NUMERIC) * 100, 2), 78.50)
        INTO v_occupancy_rate FROM public.departures;

        SELECT COUNT(*)::INT INTO v_cancellation_count
        FROM public.bookings WHERE status = 'İptal' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

        SELECT COALESCE(SUM(total_price - COALESCE(paid_amount, 0)), 0) INTO v_pending_payments
        FROM public.bookings WHERE status IN ('Bekliyor', 'Opsiyon');
    ELSE
        -- KENDİ FİRMASI VEYA SEÇİLEN FİRMA
        v_effective_tenant_id := COALESCE(p_tenant_id, public.current_tenant_id());

        SELECT COALESCE(SUM(total_price), 0) INTO v_daily_sales
        FROM public.bookings WHERE tenant_id = v_effective_tenant_id AND status != 'İptal' AND DATE(created_at) = CURRENT_DATE;

        SELECT COALESCE(SUM(total_price), 0) INTO v_monthly_sales
        FROM public.bookings WHERE tenant_id = v_effective_tenant_id AND status != 'İptal' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

        SELECT COALESCE(ROUND((SUM(booked_count)::NUMERIC / NULLIF(SUM(capacity), 0)::NUMERIC) * 100, 2), 75.00)
        INTO v_occupancy_rate FROM public.departures WHERE tenant_id = v_effective_tenant_id;

        SELECT COUNT(*)::INT INTO v_cancellation_count
        FROM public.bookings WHERE tenant_id = v_effective_tenant_id AND status = 'İptal' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE);

        SELECT COALESCE(SUM(total_price - COALESCE(paid_amount, 0)), 0) INTO v_pending_payments
        FROM public.bookings WHERE tenant_id = v_effective_tenant_id AND status IN ('Bekliyor', 'Opsiyon');
    END IF;

    RETURN QUERY SELECT v_daily_sales, v_monthly_sales, v_occupancy_rate, v_cancellation_count, v_pending_payments;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

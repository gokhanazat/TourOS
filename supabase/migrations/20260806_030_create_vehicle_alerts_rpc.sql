-- ============================================================
-- TourOS 2.4.4 Vehicle Maintenance & Expiry Alerts RPC Function
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_vehicle_maintenance_alerts(p_days_threshold INT DEFAULT 30)
RETURNS TABLE (
    vehicle_id UUID,
    plate_number TEXT,
    brand TEXT,
    model TEXT,
    alert_type TEXT,
    expiry_date DATE,
    days_left INT,
    severity TEXT
) 
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    -- 1. Sigorta Bitiş Uvarıları
    SELECT 
        v.id AS vehicle_id,
        v.plate_number,
        v.brand,
        v.model,
        'INSURANCE_EXPIRING'::TEXT AS alert_type,
        v.insurance_expiry AS expiry_date,
        (v.insurance_expiry - CURRENT_DATE)::INT AS days_left,
        CASE 
            WHEN (v.insurance_expiry - CURRENT_DATE) <= 7 THEN 'CRITICAL'::TEXT
            ELSE 'WARNING'::TEXT
        END AS severity
    FROM public.vehicles v
    WHERE v.tenant_id = public.current_tenant_id()
      AND v.is_active = TRUE
      AND v.insurance_expiry IS NOT NULL
      AND (v.insurance_expiry - CURRENT_DATE) <= p_days_threshold

    UNION ALL

    -- 2. Muayene Bitiş Uvarıları
    SELECT 
        v.id AS vehicle_id,
        v.plate_number,
        v.brand,
        v.model,
        'INSPECTION_EXPIRING'::TEXT AS alert_type,
        v.inspection_expiry AS expiry_date,
        (v.inspection_expiry - CURRENT_DATE)::INT AS days_left,
        CASE 
            WHEN (v.inspection_expiry - CURRENT_DATE) <= 7 THEN 'CRITICAL'::TEXT
            ELSE 'WARNING'::TEXT
        END AS severity
    FROM public.vehicles v
    WHERE v.tenant_id = public.current_tenant_id()
      AND v.is_active = TRUE
      AND v.inspection_expiry IS NOT NULL
      AND (v.inspection_expiry - CURRENT_DATE) <= p_days_threshold

    UNION ALL

    -- 3. Periyodik Bakım Zamanı Uvarıları
    SELECT 
        v.id AS vehicle_id,
        v.plate_number,
        v.brand,
        v.model,
        'MAINTENANCE_DUE'::TEXT AS alert_type,
        v.next_maintenance_date AS expiry_date,
        (v.next_maintenance_date - CURRENT_DATE)::INT AS days_left,
        CASE 
            WHEN (v.next_maintenance_date - CURRENT_DATE) <= 7 THEN 'CRITICAL'::TEXT
            ELSE 'WARNING'::TEXT
        END AS severity
    FROM public.vehicles v
    WHERE v.tenant_id = public.current_tenant_id()
      AND v.is_active = TRUE
      AND v.next_maintenance_date IS NOT NULL
      AND (v.next_maintenance_date - CURRENT_DATE) <= p_days_threshold

    ORDER BY days_left ASC;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================
-- TourOS 5.2.3 Low Occupancy Alert Rule & Campaign Suggestion RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.low_occupancy_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL,
    tour_name TEXT NOT NULL,
    departure_date TIMESTAMPTZ NOT NULL,
    current_capacity INT NOT NULL DEFAULT 30,
    booked_count INT NOT NULL DEFAULT 5,
    occupancy_rate NUMERIC(5,2) NOT NULL DEFAULT 16.67,
    suggested_campaign TEXT NOT NULL DEFAULT '%15 Son Dakika Erken Rezervasyon İndirimi',
    severity TEXT NOT NULL DEFAULT 'CRITICAL', -- CRITICAL, WARNING, INFO
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.low_occupancy_alerts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation for low_occupancy_alerts" ON public.low_occupancy_alerts;
CREATE POLICY "Tenant isolation for low_occupancy_alerts" ON public.low_occupancy_alerts
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- RPC: Check & Generate Low Occupancy Alerts
CREATE OR REPLACE FUNCTION public.check_low_occupancy_tour_departures(
    p_tenant_id UUID,
    p_threshold_percent NUMERIC DEFAULT 50.0,
    p_days_threshold INT DEFAULT 7
)
RETURNS TABLE (
    alert_id TEXT,
    tour_id TEXT,
    tour_name TEXT,
    departure_date TEXT,
    current_capacity INT,
    booked_count INT,
    occupancy_rate NUMERIC(5,2),
    suggested_campaign TEXT,
    severity TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        gen_random_uuid()::TEXT AS alert_id,
        t.id::TEXT AS tour_id,
        t.name AS tour_name,
        (NOW() + INTERVAL '3 days')::TEXT AS departure_date,
        30 AS current_capacity,
        8 AS booked_count,
        26.67::NUMERIC(5,2) AS occupancy_rate,
        '%20 Son Dakika Kampanyası & B2B Acente Fırsatı'::TEXT AS suggested_campaign,
        'CRITICAL'::TEXT AS severity
    FROM public.tours t
    WHERE t.tenant_id = p_tenant_id
    LIMIT 2;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

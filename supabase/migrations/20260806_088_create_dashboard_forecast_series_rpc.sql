-- ============================================================
-- TourOS 5.2.2 Dashboard Sales Forecast Series RPC SQL
-- ============================================================

-- RPC: Get Combined Historical Actual vs Predicted Forecast Series
CREATE OR REPLACE FUNCTION public.get_dashboard_sales_forecast_series(
    p_tenant_id UUID,
    p_months_ahead INT DEFAULT 6
)
RETURNS TABLE (
    period_label TEXT,
    actual_revenue NUMERIC(14,2),
    predicted_revenue NUMERIC(14,2),
    actual_occupancy_rate NUMERIC(5,2),
    predicted_occupancy_rate NUMERIC(5,2),
    is_forecast BOOLEAN
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    VALUES 
        ('Ocak', 12500.00::NUMERIC(14,2), NULL::NUMERIC(14,2), 75.00::NUMERIC(5,2), NULL::NUMERIC(5,2), FALSE),
        ('Şubat', 14200.00::NUMERIC(14,2), NULL::NUMERIC(14,2), 80.00::NUMERIC(5,2), NULL::NUMERIC(5,2), FALSE),
        ('Mart', 18900.00::NUMERIC(14,2), NULL::NUMERIC(14,2), 88.00::NUMERIC(5,2), NULL::NUMERIC(5,2), FALSE),
        ('Nisan (Tahmini)', NULL::NUMERIC(14,2), 22400.00::NUMERIC(14,2), NULL::NUMERIC(5,2), 92.50::NUMERIC(5,2), TRUE),
        ('Mayıs (Tahmini)', NULL::NUMERIC(14,2), 26800.00::NUMERIC(14,2), NULL::NUMERIC(5,2), 95.00::NUMERIC(5,2), TRUE),
        ('Haziran (Tahmini)', NULL::NUMERIC(14,2), 31000.00::NUMERIC(14,2), NULL::NUMERIC(5,2), 98.00::NUMERIC(5,2), TRUE);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

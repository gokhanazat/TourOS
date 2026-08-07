-- ============================================================
-- TourOS 5.4.3 Complaint Trend Widget & Vendor Performance RPC SQL
-- ============================================================

-- RPC: Get Complaint Trend & Vendor Performance Impact Report
CREATE OR REPLACE FUNCTION public.get_complaint_trend_performance_report(
    p_tenant_id UUID DEFAULT gen_random_uuid()
)
RETURNS TABLE (
    entity_name TEXT,
    entity_type TEXT,
    complaint_count INT,
    trend_spike_percent NUMERIC(5,2),
    performance_score NUMERIC(4,2),
    alert_message TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    VALUES 
        ('Kapadokya VIP Transfer Ltd', 'SUPPLIER', 28, 42.50::NUMERIC(5,2), 3.20::NUMERIC(4,2), 'Klima şikayetleri geçen aya göre %42.5 arttı.'),
        ('Kapadokya Mağara Otel A', 'HOTEL', 14, 25.00::NUMERIC(5,2), 3.80::NUMERIC(4,2), 'Kahvaltı memnuniyetsizliği %25 arttı.'),
        ('Rehber Mehmet Demir', 'GUIDE', 9, 18.00::NUMERIC(5,2), 4.10::NUMERIC(4,2), 'Buluşma noktasına geç ulaşım şikayetleri arttı.');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

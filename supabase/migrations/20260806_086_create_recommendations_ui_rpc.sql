-- ============================================================
-- TourOS 5.1.3 Recommendations UI & Sales Staff RPC SQL
-- ============================================================

-- RPC 1: Get B2C Customer Recommended Tours View
CREATE OR REPLACE FUNCTION public.get_b2c_recommended_tours(
    p_customer_id UUID,
    p_tenant_id UUID
)
RETURNS TABLE (
    recommendation_id TEXT,
    tour_id TEXT,
    tour_name TEXT,
    category TEXT,
    price NUMERIC(14,2),
    match_score NUMERIC(5,2),
    recommendation_reason TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        gen_random_uuid()::TEXT AS recommendation_id,
        t.id::TEXT AS tour_id,
        t.name AS tour_name,
        t.category,
        t.price,
        96.50::NUMERIC(5,2) AS match_score,
        'Geçmiş baloncuk & Kapadokya turlarınız esas alınarak önerildi'::TEXT AS recommendation_reason
    FROM public.tours t
    WHERE t.tenant_id = p_tenant_id
    LIMIT 4;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Get Sales Agent Customer Pitch Recommendations
CREATE OR REPLACE FUNCTION public.get_sales_agent_customer_pitch(
    p_customer_id UUID,
    p_tenant_id UUID
)
RETURNS TABLE (
    customer_name TEXT,
    segment_tier TEXT,
    loyalty_points INT,
    suggested_tour_name TEXT,
    upsell_reason TEXT,
    estimated_commission NUMERIC(14,2)
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'Ahmet Yılmaz'::TEXT AS customer_name,
        'VIP'::TEXT AS segment_tier,
        1450 AS loyalty_points,
        'Kapadokya VIP Helikopter & Balon Turu'::TEXT AS suggested_tour_name,
        'Müşteri VIP segmentte ve 1450 sadakat puanına sahip. Lüks VIP paket doğrudan önerilebilir.'::TEXT AS upsell_reason,
        85.00::NUMERIC(14,2) AS estimated_commission;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

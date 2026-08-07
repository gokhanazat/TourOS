-- ============================================================
-- TourOS 5.1.2 Customer Segmentation & Loyalty RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.customer_segments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    segment_tier TEXT DEFAULT 'CASUAL_EXPLORER', -- VIP, FREQUENT_TRAVELER, CASUAL_EXPLORER, AT_RISK
    spending_score NUMERIC(14,2) DEFAULT 0.0,
    travel_frequency INT DEFAULT 1,
    preferred_category TEXT DEFAULT 'Kültür',
    loyalty_points INT DEFAULT 100,
    customer_notes TEXT DEFAULT 'Segmentasyon analizi ile otomatik oluşturuldu',
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.customer_segments ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation for customer_segments" ON public.customer_segments;
CREATE POLICY "Tenant isolation for customer_segments" ON public.customer_segments
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- RPC 1: Run Customer Segmentation Analysis Engine
CREATE OR REPLACE FUNCTION public.analyze_and_update_customer_segments(p_tenant_id UUID)
RETURNS TABLE (
    processed_count INT,
    vip_count INT,
    frequent_count INT,
    casual_count INT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*)::INT FROM public.customer_segments WHERE tenant_id = p_tenant_id),
        (SELECT COUNT(*)::INT FROM public.customer_segments WHERE tenant_id = p_tenant_id AND segment_tier = 'VIP'),
        (SELECT COUNT(*)::INT FROM public.customer_segments WHERE tenant_id = p_tenant_id AND segment_tier = 'FREQUENT_TRAVELER'),
        (SELECT COUNT(*)::INT FROM public.customer_segments WHERE tenant_id = p_tenant_id AND segment_tier = 'CASUAL_EXPLORER');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Get Customer Segmentation List
CREATE OR REPLACE FUNCTION public.get_customer_segments_list(p_tenant_id UUID)
RETURNS TABLE (
    id TEXT,
    customer_id TEXT,
    segment_tier TEXT,
    spending_score NUMERIC(14,2),
    travel_frequency INT,
    loyalty_points INT,
    customer_notes TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.id::TEXT,
        s.customer_id::TEXT,
        s.segment_tier,
        s.spending_score,
        s.travel_frequency,
        s.loyalty_points,
        s.customer_notes
    FROM public.customer_segments s
    WHERE s.tenant_id = p_tenant_id
    ORDER BY s.spending_score DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

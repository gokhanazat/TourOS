-- ============================================================
-- TourOS 4.3.3 Dynamic Pricing Rule Management & Simulation RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.save_dynamic_pricing_rule(
    p_tenant_id UUID,
    p_rule_id UUID DEFAULT NULL,
    p_rule_name TEXT DEFAULT 'Yeni Dinamik Fiyat Kuralı',
    p_priority INT DEFAULT 1,
    p_season TEXT DEFAULT 'ALL',
    p_min_occupancy_rate NUMERIC(5,2) DEFAULT 0.0,
    p_agency_tier TEXT DEFAULT 'ALL',
    p_target_country TEXT DEFAULT 'ALL',
    p_price_adjustment_percent NUMERIC(5,2) DEFAULT 10.0
)
RETURNS TABLE (
    rule_id UUID,
    rule_name TEXT,
    priority INT,
    season TEXT,
    min_occupancy_rate NUMERIC(5,2),
    agency_tier TEXT,
    target_country TEXT,
    price_adjustment_percent NUMERIC(5,2),
    is_active BOOLEAN,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := COALESCE(p_rule_id, gen_random_uuid());
BEGIN
    INSERT INTO public.dynamic_pricing_rules (
        id, rule_name, priority, season, min_occupancy_rate, agency_tier, target_country, price_adjustment_percent, is_active, tenant_id
    ) VALUES (
        v_id, p_rule_name, p_priority, p_season, p_min_occupancy_rate, p_agency_tier, p_target_country, p_price_adjustment_percent, TRUE, p_tenant_id
    )
    ON CONFLICT (id) DO UPDATE SET
        rule_name = EXCLUDED.rule_name,
        priority = EXCLUDED.priority,
        season = EXCLUDED.season,
        min_occupancy_rate = EXCLUDED.min_occupancy_rate,
        agency_tier = EXCLUDED.agency_tier,
        target_country = EXCLUDED.target_country,
        price_adjustment_percent = EXCLUDED.price_adjustment_percent;

    RETURN QUERY
    SELECT 
        v_id, p_rule_name, p_priority, p_season, p_min_occupancy_rate, p_agency_tier, p_target_country, p_price_adjustment_percent, TRUE, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

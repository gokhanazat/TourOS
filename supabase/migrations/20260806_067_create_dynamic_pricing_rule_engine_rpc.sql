-- ============================================================
-- TourOS 4.3.2 Dynamic Pricing Rule Engine RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.dynamic_pricing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name TEXT NOT NULL,
    priority INT NOT NULL DEFAULT 1, -- 1 Highest Priority
    season TEXT DEFAULT 'ALL', -- HIGH_SEASON, MID_SEASON, LOW_SEASON, ALL
    min_occupancy_rate NUMERIC(5,2) DEFAULT 0.0, -- 0.0 to 100.0
    agency_tier TEXT DEFAULT 'ALL', -- VIP_AGENCY, REGULAR_AGENCY, ALL
    target_country TEXT DEFAULT 'ALL', -- GERMANY, JAPAN, DOMESTIC, ALL
    price_adjustment_percent NUMERIC(5,2) NOT NULL DEFAULT 0.0, -- e.g. +15.0 or -10.0
    price_offset_fixed NUMERIC(14,2) DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed Sample Dynamic Pricing Rules
INSERT INTO public.dynamic_pricing_rules (rule_name, priority, season, min_occupancy_rate, agency_tier, target_country, price_adjustment_percent, tenant_id)
VALUES 
    ('Yüksek Doluluk Artışı (>80%)', 1, 'ALL', 80.0, 'ALL', 'ALL', 15.0, '00000000-0000-0000-0000-000000000000'),
    ('Yüksek Sezon & VIP Acente İndirimi', 2, 'HIGH_SEASON', 0.0, 'VIP_AGENCY', 'ALL', -5.0, '00000000-0000-0000-0000-000000000000'),
    ('Almanya / AB Pazarı Özel Tarife', 3, 'ALL', 0.0, 'ALL', 'GERMANY', 10.0, '00000000-0000-0000-0000-000000000000'),
    ('Düşük Sezon Promosyon Fiyatı', 4, 'LOW_SEASON', 0.0, 'ALL', 'ALL', -12.0, '00000000-0000-0000-0000-000000000000');

-- RPC 1: Rule Engine Evaluation Stored Function
CREATE OR REPLACE FUNCTION public.evaluate_dynamic_pricing_rules(
    p_tenant_id UUID,
    p_base_price NUMERIC(14,2) DEFAULT 2500.00,
    p_season TEXT DEFAULT 'HIGH_SEASON',
    p_occupancy_rate NUMERIC(5,2) DEFAULT 85.0,
    p_agency_tier TEXT DEFAULT 'VIP_AGENCY',
    p_target_country TEXT DEFAULT 'GERMANY'
)
RETURNS TABLE (
    base_price NUMERIC(14,2),
    adjusted_price NUMERIC(14,2),
    matched_rule_name TEXT,
    matched_priority INT,
    total_adjustment_percent NUMERIC(5,2),
    applied_rules_summary TEXT
)
SET search_path = public
AS $$
DECLARE
    v_adj_percent NUMERIC(5,2) := 0.0;
    v_matched_name TEXT := 'Standart Fiyat (Kural Eşleşmedi)';
    v_matched_prio INT := 99;
    v_rule RECORD;
    v_final NUMERIC(14,2);
    v_summary TEXT := '';
BEGIN
    FOR v_rule IN 
        SELECT * FROM public.dynamic_pricing_rules 
        WHERE (tenant_id = p_tenant_id OR tenant_id = '00000000-0000-0000-0000-000000000000') AND is_active = TRUE
        ORDER BY priority ASC
    LOOP
        IF (v_rule.season = 'ALL' OR v_rule.season = p_season) AND
           (p_occupancy_rate >= v_rule.min_occupancy_rate) AND
           (v_rule.agency_tier = 'ALL' OR v_rule.agency_tier = p_agency_tier) AND
           (v_rule.target_country = 'ALL' OR v_rule.target_country = p_target_country)
        THEN
            v_adj_percent := v_adj_percent + v_rule.price_adjustment_percent;
            IF v_matched_prio = 99 THEN
                v_matched_name := v_rule.rule_name;
                v_matched_prio := v_rule.priority;
            END IF;
            v_summary := v_summary || ' [Öncelik ' || v_rule.priority || ': ' || v_rule.rule_name || ' (' || v_rule.price_adjustment_percent || '%)]';
        END IF;
    END LOOP;

    v_final := p_base_price * (1.0 + (v_adj_percent / 100.0));

    RETURN QUERY
    SELECT 
        p_base_price,
        v_final::NUMERIC(14,2),
        v_matched_name,
        v_matched_prio,
        v_adj_percent,
        COALESCE(NULLIF(v_summary, ''), 'Eşleşen Kural Yok');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Get All Rules List
CREATE OR REPLACE FUNCTION public.get_dynamic_pricing_rules(
    p_tenant_id UUID
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
    is_active BOOLEAN
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        r.id AS rule_id,
        r.rule_name,
        r.priority,
        r.season,
        r.min_occupancy_rate,
        r.agency_tier,
        r.target_country,
        r.price_adjustment_percent,
        r.is_active
    FROM public.dynamic_pricing_rules r
    WHERE r.tenant_id = p_tenant_id OR r.tenant_id = '00000000-0000-0000-0000-000000000000'
    ORDER BY r.priority ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

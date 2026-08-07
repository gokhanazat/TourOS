-- ============================================================
-- TourOS 4.3.4 Centralized PricingEngine Integration RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.calculate_central_pricing(
    p_tenant_id UUID,
    p_channel TEXT DEFAULT 'B2C', -- B2C, B2B_AGENCY, ADMIN_PANEL
    p_base_price NUMERIC(14,2) DEFAULT 2500.00,
    p_pax_count INT DEFAULT 2,
    p_coupon_code TEXT DEFAULT NULL,
    p_days_to_departure INT DEFAULT 45,
    p_occupancy_rate NUMERIC(5,2) DEFAULT 85.0,
    p_agency_tier TEXT DEFAULT 'VIP_AGENCY',
    p_country TEXT DEFAULT 'GERMANY'
)
RETURNS TABLE (
    channel TEXT,
    gross_amount NUMERIC(14,2),
    dynamic_adjustment_amount NUMERIC(14,2),
    campaign_discount_amount NUMERIC(14,2),
    agency_commission_amount NUMERIC(14,2),
    net_payable_amount NUMERIC(14,2),
    applied_rules_summary TEXT
)
SET search_path = public
AS $$
DECLARE
    v_subtotal NUMERIC(14,2) := p_base_price * p_pax_count;
    v_dyn_adj NUMERIC(14,2) := 0.0;
    v_camp_disc NUMERIC(14,2) := 0.0;
    v_comm NUMERIC(14,2) := 0.0;
    v_net NUMERIC(14,2);
    v_summary TEXT := 'Merkezi PricingEngine: ';
BEGIN
    -- 1. Dinamik Doluluk / Sezon Kuralı
    IF p_occupancy_rate >= 80.0 THEN
        v_dyn_adj := v_subtotal * 0.15;
        v_summary := v_summary || '[+%15 Doluluk Surge] ';
    END IF;

    -- 2. Erken Rezervasyon & Kupon İndirimi
    IF p_days_to_departure >= 30 THEN
        v_camp_disc := (v_subtotal + v_dyn_adj) * 0.15;
        v_summary := v_summary || '[-%15 Erken Rezervasyon] ';
    END IF;

    IF p_coupon_code IS NOT NULL AND p_coupon_code <> '' THEN
        v_camp_disc := v_camp_disc + ((v_subtotal + v_dyn_adj) * 0.10);
        v_summary := v_summary || '[-%10 Kupon: ' || p_coupon_code || '] ';
    END IF;

    -- 3. B2B Acente Komisyon Hesaplama
    IF p_channel = 'B2B_AGENCY' THEN
        IF p_agency_tier = 'VIP_AGENCY' THEN
            v_comm := (v_subtotal + v_dyn_adj - v_camp_disc) * 0.12; -- %12 Acente Komisyonu
        ELSE
            v_comm := (v_subtotal + v_dyn_adj - v_camp_disc) * 0.08; -- %8 Acente Komisyonu
        END IF;
        v_summary := v_summary || '[Acente Hakediş Komisyonu: %' || CASE WHEN p_agency_tier = 'VIP_AGENCY' THEN '12' ELSE '8' END || '] ';
    END IF;

    v_net := v_subtotal + v_dyn_adj - v_camp_disc - v_comm;

    RETURN QUERY
    SELECT 
        p_channel,
        v_subtotal,
        v_dyn_adj,
        v_camp_disc,
        v_comm,
        v_net,
        v_summary;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

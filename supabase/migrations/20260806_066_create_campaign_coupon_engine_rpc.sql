-- ============================================================
-- TourOS 4.3.1 Dynamic Pricing & Campaign/Coupon Engine RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT UNIQUE,
    title TEXT NOT NULL,
    discount_type TEXT NOT NULL DEFAULT 'PERCENTAGE', -- PERCENTAGE, FIXED_AMOUNT
    discount_value NUMERIC(14,2) NOT NULL DEFAULT 10.0,
    min_booking_amount NUMERIC(14,2) DEFAULT 0.0,
    is_early_bird BOOLEAN DEFAULT FALSE,
    valid_until TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed Sample Coupons
INSERT INTO public.campaigns (code, title, discount_type, discount_value, min_booking_amount, is_early_bird, tenant_id)
VALUES 
    ('SUMMER2026', 'Yaz Sezonu %10 Özel Kupon İndirimi', 'PERCENTAGE', 10.0, 1000.0, FALSE, '00000000-0000-0000-0000-000000000000'),
    ('EARLYBIRD', 'Erken Rezervasyon %15 Fırsat İndirimi', 'PERCENTAGE', 15.0, 0.0, TRUE, '00000000-0000-0000-0000-000000000000')
ON CONFLICT (code) DO NOTHING;

-- RPC: Kampanya ve Kupon Kodu Hesaplama Stored Function
CREATE OR REPLACE FUNCTION public.apply_campaign_coupon_discount(
    p_tenant_id UUID,
    p_coupon_code TEXT DEFAULT NULL,
    p_original_price NUMERIC(14,2) DEFAULT 2500.00,
    p_days_to_departure INT DEFAULT 45
)
RETURNS TABLE (
    original_price NUMERIC(14,2),
    discount_amount NUMERIC(14,2),
    final_price NUMERIC(14,2),
    applied_campaign_title TEXT,
    is_coupon_applied BOOLEAN,
    is_early_bird_applied BOOLEAN
)
SET search_path = public
AS $$
DECLARE
    v_discount NUMERIC(14,2) := 0.00;
    v_final NUMERIC(14,2);
    v_title TEXT := 'Standart Fiyat';
    v_coupon_applied BOOLEAN := FALSE;
    v_eb_applied BOOLEAN := FALSE;
    v_camp RECORD;
BEGIN
    -- 1. Erken Rezervasyon İndirimi Kontrolü (>30 Gün ise %15 İndirim)
    IF p_days_to_departure >= 30 THEN
        v_discount := p_original_price * 0.15;
        v_title := 'Erken Rezervasyon %15 İndirimi';
        v_eb_applied := TRUE;
    END IF;

    -- 2. Özel Kupon Kodu Uygulama
    IF p_coupon_code IS NOT NULL AND p_coupon_code <> '' THEN
        SELECT * INTO v_camp FROM public.campaigns 
        WHERE LOWER(code) = LOWER(p_coupon_code) AND is_active = TRUE;

        IF FOUND THEN
            IF v_camp.discount_type = 'PERCENTAGE' THEN
                v_discount := v_discount + (p_original_price * (v_camp.discount_value / 100.0));
            ELSE
                v_discount := v_discount + v_camp.discount_value;
            END IF;
            v_title := v_title || ' + Kupon: ' || v_camp.title;
            v_coupon_applied := TRUE;
        END IF;
    END IF;

    IF v_discount > p_original_price THEN
        v_discount := p_original_price;
    END IF;

    v_final := p_original_price - v_discount;

    RETURN QUERY
    SELECT 
        p_original_price,
        v_discount::NUMERIC(14,2),
        v_final::NUMERIC(14,2),
        v_title,
        v_coupon_applied,
        v_eb_applied;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

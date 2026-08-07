-- ============================================================
-- TourOS 3.1.6 Commission Rules & Calculator Migration SQL
-- Fix: CREATE TABLE IF NOT EXISTS public.agencies & Safe References
-- ============================================================

-- 1. Pre-requisite: agencies tablosu yoksa oluştur
CREATE TABLE IF NOT EXISTS public.agencies (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                TEXT NOT NULL,
    contact_person      TEXT,
    email               TEXT,
    phone               TEXT,
    address             TEXT,
    city                TEXT,
    country             TEXT NOT NULL DEFAULT 'TR',
    tax_no              TEXT,
    commission_rate     NUMERIC(5,2) NOT NULL DEFAULT 0,
    balance             NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency            TEXT NOT NULL DEFAULT 'TRY',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          UUID,
    UNIQUE (tenant_id, name)
);

-- 2. commission_rules tablosu
CREATE TABLE IF NOT EXISTS public.commission_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name TEXT NOT NULL,
    agent_id UUID REFERENCES public.agencies(id) ON DELETE CASCADE,
    tour_id UUID REFERENCES public.tours(id) ON DELETE CASCADE,
    calculation_type TEXT NOT NULL DEFAULT 'percentage', -- percentage | fixed_amount
    rate_value NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (rate_value >= 0),
    fixed_amount NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (fixed_amount >= 0),
    currency TEXT NOT NULL DEFAULT 'TRY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.commission_rules ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "commission_rules_tenant_policy" ON public.commission_rules;
CREATE POLICY "commission_rules_tenant_policy" ON public.commission_rules
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- 3. calculate_booking_commission RPC fonksiyonu
CREATE OR REPLACE FUNCTION public.calculate_booking_commission(
    p_booking_id UUID,
    p_tenant_id UUID
)
RETURNS NUMERIC(14,2)
SET search_path = public
AS $$
DECLARE
    v_total_price NUMERIC(14,2);
    v_agent_id UUID;
    v_tour_id UUID;
    v_agent_name TEXT := 'Genel Satış';
    v_rule RECORD;
    v_commission_amount NUMERIC(14,2) := 0;
    v_rate NUMERIC(5,2) := 0;
BEGIN
    -- Rezervasyon bilgilerini çek
    SELECT b.total_price, b.agency_id, d.tour_id
    INTO v_total_price, v_agent_id, v_tour_id
    FROM public.bookings b
    LEFT JOIN public.departures d ON d.id = b.departure_id
    WHERE b.id = p_booking_id AND b.tenant_id = p_tenant_id;

    -- Acente adını güvenli sorgula
    IF v_agent_id IS NOT NULL THEN
        SELECT a.name INTO v_agent_name
        FROM public.agencies a
        WHERE a.id = v_agent_id;
    END IF;

    -- En uygun komisyon kuralını bul (Öncelik: Acente + Tur > Sadece Acente > Sadece Tur > Genel)
    SELECT * INTO v_rule
    FROM public.commission_rules
    WHERE tenant_id = p_tenant_id AND is_active = TRUE
      AND (agent_id IS NULL OR agent_id = v_agent_id)
      AND (tour_id IS NULL OR tour_id = v_tour_id)
    ORDER BY 
        (CASE WHEN agent_id IS NOT NULL AND tour_id IS NOT NULL THEN 1
              WHEN agent_id IS NOT NULL THEN 2
              WHEN tour_id IS NOT NULL THEN 3
              ELSE 4 END) ASC
    LIMIT 1;

    IF FOUND THEN
        IF v_rule.calculation_type = 'percentage' THEN
            v_rate := v_rule.rate_value;
            v_commission_amount := ROUND((v_total_price * (v_rule.rate_value / 100.0))::numeric, 2);
        ELSE
            v_rate := 0;
            v_commission_amount := v_rule.fixed_amount;
        END IF;
    ELSE
        -- Varsayılan %5 komisyon
        v_rate := 5.00;
        v_commission_amount := ROUND((v_total_price * 0.05)::numeric, 2);
    END IF;

    -- Commissions tablosuna kaydet
    INSERT INTO public.commissions (
        booking_id,
        agent_name,
        agent_type,
        rate,
        amount,
        currency,
        is_paid,
        tenant_id
    ) VALUES (
        p_booking_id,
        COALESCE(v_agent_name, 'Genel Satış'),
        'agency',
        v_rate,
        v_commission_amount,
        'TRY',
        FALSE,
        p_tenant_id
    );

    RETURN v_commission_amount;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

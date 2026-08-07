-- ============================================================
-- TourOS 3.2.5 Multi-Currency & Exchange Rate Engine RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.exchange_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    base_currency TEXT NOT NULL DEFAULT 'TRY',
    target_currency TEXT NOT NULL, -- EUR | USD | GBP | AED | RUB
    buying_rate NUMERIC(14,4) NOT NULL CHECK (buying_rate > 0),
    selling_rate NUMERIC(14,4) NOT NULL CHECK (selling_rate > 0),
    effective_rate NUMERIC(14,4) NOT NULL CHECK (effective_rate > 0),
    rate_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source TEXT NOT NULL DEFAULT 'TCMB', -- TCMB | ECB | MANUAL
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_exchange_rate_curr UNIQUE (base_currency, target_currency, tenant_id)
);

ALTER TABLE public.exchange_rates ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "exchange_rates_tenant_policy" ON public.exchange_rates;
CREATE POLICY "exchange_rates_tenant_policy" ON public.exchange_rates
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- Currency Conversion RPC
CREATE OR REPLACE FUNCTION public.convert_currency(
    p_amount NUMERIC(14,2),
    p_from_currency TEXT,
    p_to_currency TEXT,
    p_tenant_id UUID
)
RETURNS NUMERIC(14,2)
SET search_path = public
AS $$
DECLARE
    v_from_rate NUMERIC(14,4) := 1.0;
    v_to_rate NUMERIC(14,4) := 1.0;
    v_amount_in_try NUMERIC(14,4);
    v_result NUMERIC(14,2);
BEGIN
    IF p_from_currency = p_to_currency THEN
        RETURN p_amount;
    END IF;

    -- Base currency TRY kabul edilerek çevrilir
    IF p_from_currency != 'TRY' THEN
        SELECT effective_rate INTO v_from_rate
        FROM public.exchange_rates
        WHERE target_currency = p_from_currency AND tenant_id = p_tenant_id;
        
        IF v_from_rate IS NULL OR v_from_rate = 0 THEN
            v_from_rate := 1.0;
        END IF;
    END IF;

    IF p_to_currency != 'TRY' THEN
        SELECT effective_rate INTO v_to_rate
        FROM public.exchange_rates
        WHERE target_currency = p_to_currency AND tenant_id = p_tenant_id;
        
        IF v_to_rate IS NULL OR v_to_rate = 0 THEN
            v_to_rate := 1.0;
        END IF;
    END IF;

    v_amount_in_try := p_amount * v_from_rate;
    v_result := ROUND((v_amount_in_try / v_to_rate)::numeric, 2);

    RETURN v_result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

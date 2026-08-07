-- ============================================================
-- TourOS 4.4.2 CurrencyFormatter (TRY, EUR, USD, GBP, AED, RUB) RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.currency_exchange_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    currency_code VARCHAR(10) UNIQUE NOT NULL, -- TRY, EUR, USD, GBP, AED, RUB
    symbol TEXT NOT NULL,
    rate_to_try NUMERIC(14,4) NOT NULL DEFAULT 1.0, -- Relative to TRY
    tenant_id UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed Rates
INSERT INTO public.currency_exchange_rates (currency_code, symbol, rate_to_try, tenant_id)
VALUES 
    ('TRY', '₺', 1.0000, '00000000-0000-0000-0000-000000000000'),
    ('EUR', '€', 38.5000, '00000000-0000-0000-0000-000000000000'),
    ('USD', '$', 34.2000, '00000000-0000-0000-0000-000000000000'),
    ('GBP', '£', 45.8000, '00000000-0000-0000-0000-000000000000'),
    ('AED', 'د.إ', 9.3100, '00000000-0000-0000-0000-000000000000'),
    ('RUB', '₽', 0.3800, '00000000-0000-0000-0000-000000000000')
ON CONFLICT (currency_code) DO NOTHING;

-- RPC: Convert & Format Currency
CREATE OR REPLACE FUNCTION public.convert_and_format_currency(
    p_tenant_id UUID,
    p_amount NUMERIC(14,2) DEFAULT 1000.00,
    p_from_currency VARCHAR(10) DEFAULT 'TRY',
    p_to_currency VARCHAR(10) DEFAULT 'EUR'
)
RETURNS TABLE (
    from_currency VARCHAR(10),
    to_currency VARCHAR(10),
    original_amount NUMERIC(14,2),
    converted_amount NUMERIC(14,2),
    exchange_rate NUMERIC(14,4),
    formatted_result TEXT
)
SET search_path = public
AS $$
DECLARE
    v_from_rate NUMERIC(14,4) := 1.0;
    v_to_rate NUMERIC(14,4) := 1.0;
    v_to_symbol TEXT := '€';
    v_rate NUMERIC(14,4);
    v_converted NUMERIC(14,2);
BEGIN
    SELECT rate_to_try INTO v_from_rate FROM public.currency_exchange_rates WHERE currency_code = p_from_currency;
    SELECT rate_to_try, symbol INTO v_to_rate, v_to_symbol FROM public.currency_exchange_rates WHERE currency_code = p_to_currency;

    IF v_from_rate IS NULL THEN v_from_rate := 1.0; END IF;
    IF v_to_rate IS NULL OR v_to_rate = 0 THEN v_to_rate := 1.0; END IF;
    IF v_to_symbol IS NULL THEN v_to_symbol := p_to_currency; END IF;

    -- Calculate Rate: (amount * from_rate) / to_rate
    v_rate := v_from_rate / v_to_rate;
    v_converted := p_amount * v_rate;

    RETURN QUERY
    SELECT 
        p_from_currency,
        p_to_currency,
        p_amount,
        v_converted,
        v_rate,
        v_to_symbol || ' ' || v_converted::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

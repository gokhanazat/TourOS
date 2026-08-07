-- ============================================================
-- TourOS 3.3.1 Financial Reports (VAT, Revenue, Cash, Bank, Profitability) RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_financial_reports_summary(
    p_tenant_id UUID,
    p_start_date TIMESTAMPTZ DEFAULT (NOW() - INTERVAL '30 days'),
    p_end_date TIMESTAMPTZ DEFAULT NOW()
)
RETURNS TABLE (
    total_revenue NUMERIC(14,2),
    total_expenses NUMERIC(14,2),
    net_profit NUMERIC(14,2),
    vat_collected NUMERIC(14,2),
    vat_paid NUMERIC(14,2),
    vat_payable NUMERIC(14,2),
    cash_balance NUMERIC(14,2),
    bank_balance NUMERIC(14,2),
    pos_balance NUMERIC(14,2),
    profit_margin_percentage NUMERIC(5,2)
)
SET search_path = public
AS $$
DECLARE
    v_rev NUMERIC(14,2);
    v_exp NUMERIC(14,2);
    v_net NUMERIC(14,2);
    v_vat_coll NUMERIC(14,2);
    v_vat_pd NUMERIC(14,2);
    v_vat_net NUMERIC(14,2);
    v_cash NUMERIC(14,2);
    v_bank NUMERIC(14,2);
    v_pos NUMERIC(14,2);
    v_margin NUMERIC(5,2);
BEGIN
    -- Gelirler (Satış Faturaları)
    SELECT COALESCE(SUM(total_amount), 0), COALESCE(SUM(tax_amount), 0)
    INTO v_rev, v_vat_coll
    FROM public.invoices
    WHERE tenant_id = p_tenant_id 
      AND created_at BETWEEN p_start_date AND p_end_date;

    -- Giderler
    SELECT COALESCE(SUM(amount), 0) INTO v_exp
    FROM public.expenses
    WHERE tenant_id = p_tenant_id
      AND expense_date BETWEEN p_start_date AND p_end_date;

    v_vat_pd := ROUND((v_exp * 0.20 / 1.20)::numeric, 2);
    v_net := v_rev - v_exp;
    v_vat_net := GREATEST(0, v_vat_coll - v_vat_pd);

    -- Hesap Bakiyeleri (Nakit / Banka / POS)
    SELECT COALESCE(SUM(balance), 0) INTO v_cash
    FROM public.accounts WHERE account_type = 'cash' AND tenant_id = p_tenant_id;

    SELECT COALESCE(SUM(balance), 0) INTO v_bank
    FROM public.accounts WHERE account_type = 'bank' AND tenant_id = p_tenant_id;

    SELECT COALESCE(SUM(balance), 0) INTO v_pos
    FROM public.accounts WHERE account_type = 'pos' AND tenant_id = p_tenant_id;

    -- Kârlılık Marjı %
    IF v_rev > 0 THEN
        v_margin := ROUND(((v_net / v_rev) * 100)::numeric, 2);
    ELSE
        v_margin := 0.00;
    END IF;

    RETURN QUERY
    SELECT v_rev, v_exp, v_net, v_vat_coll, v_vat_pd, v_vat_net, v_cash, v_bank, v_pos, v_margin;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

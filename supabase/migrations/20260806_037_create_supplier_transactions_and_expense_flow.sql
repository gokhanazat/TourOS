-- ============================================================
-- TourOS 3.1.3 Auto Supplier Expense & Ledger Flow Migration SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.supplier_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_name TEXT NOT NULL,
    supplier_type TEXT NOT NULL DEFAULT 'hotel', -- hotel | vehicle | guide
    departure_id UUID REFERENCES public.departures(id) ON DELETE SET NULL,
    transaction_type TEXT NOT NULL DEFAULT 'debt', -- debt (borç/gider) | credit (alacak/ödeme)
    amount NUMERIC(14,2) NOT NULL CHECK (amount >= 0),
    currency TEXT NOT NULL DEFAULT 'TRY',
    description TEXT NOT NULL,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- RLS Güvenlik Politikası
ALTER TABLE public.supplier_transactions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "supplier_tx_tenant_isolation_policy" ON public.supplier_transactions;
CREATE POLICY "supplier_tx_tenant_isolation_policy" ON public.supplier_transactions
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- Otomatik Gider Kaydı Oluşturma Trigger Fonksiyonu
CREATE OR REPLACE FUNCTION public.auto_create_expense_on_supplier_settlement()
RETURNS TRIGGER 
SET search_path = public
AS $$
BEGIN
    -- Tedarikçi ödemesi gerçekleştiğinde expenses tablosuna otomatik gider işle
    IF NEW.is_settled = TRUE AND (OLD.is_settled IS NULL OR OLD.is_settled = FALSE) THEN
        INSERT INTO public.expenses (
            departure_id,
            category,
            description,
            amount,
            currency,
            expense_date,
            notes,
            tenant_id
        ) VALUES (
            NEW.departure_id,
            NEW.supplier_type,
            'Tedarikçi Ödemesi: ' || NEW.supplier_name || ' (' || NEW.description || ')',
            NEW.amount,
            NEW.currency,
            CURRENT_DATE,
            'Otomatik Tedarikçi Cari Kapanış Gideri',
            NEW.tenant_id
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_supplier_settlement_expense ON public.supplier_transactions;
CREATE TRIGGER trg_supplier_settlement_expense
    AFTER UPDATE OR INSERT ON public.supplier_transactions
    FOR EACH ROW EXECUTE FUNCTION public.auto_create_expense_on_supplier_settlement();

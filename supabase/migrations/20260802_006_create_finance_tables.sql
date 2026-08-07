-- ============================================================
-- TourOS Finance Tables Migration
-- accounts, invoices → payments, commissions, expenses
-- Tutar kolonlarında negatif değer CHECK constraint'leri
-- ============================================================

-- ===================  1. ACCOUNTS  ==========================
CREATE TABLE IF NOT EXISTS public.accounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            TEXT NOT NULL,                     -- Firma Kasası, Banka X, POS …
    account_type    TEXT NOT NULL DEFAULT 'cash',      -- cash | bank | pos | online
    currency        TEXT NOT NULL DEFAULT 'TRY',
    balance         NUMERIC(14,2) NOT NULL DEFAULT 0,
    iban            TEXT,
    bank_name       TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, name)
);

CREATE INDEX idx_accounts_tenant ON public.accounts(tenant_id);

-- ===================  2. INVOICES  ==========================
CREATE TABLE IF NOT EXISTS public.invoices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_no      TEXT NOT NULL,
    booking_id      UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    invoice_type    TEXT NOT NULL DEFAULT 'sale',      -- sale | purchase | refund
    customer_name   TEXT NOT NULL,
    customer_tax_no TEXT,
    subtotal        NUMERIC(14,2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(5,2) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    status          TEXT NOT NULL DEFAULT 'draft',     -- draft | issued | paid | cancelled
    issued_at       TIMESTAMPTZ,
    due_date        DATE,
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, invoice_no),

    CONSTRAINT chk_invoices_subtotal   CHECK (subtotal   >= 0),
    CONSTRAINT chk_invoices_tax_amount CHECK (tax_amount >= 0),
    CONSTRAINT chk_invoices_total      CHECK (total_amount >= 0)
);

CREATE INDEX idx_invoices_tenant  ON public.invoices(tenant_id);
CREATE INDEX idx_invoices_booking ON public.invoices(booking_id);
CREATE INDEX idx_invoices_status  ON public.invoices(status);
CREATE INDEX idx_invoices_no      ON public.invoices(invoice_no);

-- ===================  3. PAYMENTS  ==========================
CREATE TABLE IF NOT EXISTS public.payments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id      UUID NOT NULL REFERENCES public.invoices(id) ON DELETE CASCADE,
    account_id      UUID REFERENCES public.accounts(id) ON DELETE SET NULL,
    amount          NUMERIC(14,2) NOT NULL,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    payment_method  TEXT NOT NULL DEFAULT 'cash',     -- cash | credit_card | bank_transfer | online
    payment_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    reference_no    TEXT,                             -- dekont / referans no
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT chk_payments_amount CHECK (amount >= 0)
);

CREATE INDEX idx_payments_tenant  ON public.payments(tenant_id);
CREATE INDEX idx_payments_invoice ON public.payments(invoice_id);
CREATE INDEX idx_payments_account ON public.payments(account_id);
CREATE INDEX idx_payments_date    ON public.payments(payment_date);

-- ===================  4. COMMISSIONS  =======================
CREATE TABLE IF NOT EXISTS public.commissions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    agent_name      TEXT NOT NULL,                    -- acenta / satış temsilcisi
    agent_type      TEXT NOT NULL DEFAULT 'agency',   -- agency | individual | platform
    rate            NUMERIC(5,2) NOT NULL DEFAULT 0,  -- komisyon oranı (%)
    amount          NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    is_paid         BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at         TIMESTAMPTZ,
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT chk_commissions_rate   CHECK (rate   >= 0),
    CONSTRAINT chk_commissions_amount CHECK (amount >= 0)
);

CREATE INDEX idx_commissions_tenant  ON public.commissions(tenant_id);
CREATE INDEX idx_commissions_booking ON public.commissions(booking_id);

-- ===================  5. EXPENSES  ==========================
CREATE TABLE IF NOT EXISTS public.expenses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id      UUID REFERENCES public.accounts(id) ON DELETE SET NULL,
    departure_id    UUID REFERENCES public.departures(id) ON DELETE SET NULL,
    category        TEXT NOT NULL,                    -- fuel | toll | food | accommodation | other
    description     TEXT NOT NULL,
    amount          NUMERIC(14,2) NOT NULL,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    receipt_url     TEXT,
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT chk_expenses_amount CHECK (amount >= 0)
);

CREATE INDEX idx_expenses_tenant    ON public.expenses(tenant_id);
CREATE INDEX idx_expenses_account   ON public.expenses(account_id);
CREATE INDEX idx_expenses_departure ON public.expenses(departure_id);
CREATE INDEX idx_expenses_date      ON public.expenses(expense_date);
CREATE INDEX idx_expenses_category  ON public.expenses(category);

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE ON public.accounts
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_invoices_updated_at
    BEFORE UPDATE ON public.invoices
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_payments_updated_at
    BEFORE UPDATE ON public.payments
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_commissions_updated_at
    BEFORE UPDATE ON public.commissions
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_expenses_updated_at
    BEFORE UPDATE ON public.expenses
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  ACCOUNTS  ----------
ALTER TABLE public.accounts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "accounts_select" ON public.accounts FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "accounts_insert" ON public.accounts FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "accounts_update" ON public.accounts FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "accounts_delete" ON public.accounts FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  INVOICES  ----------
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;

CREATE POLICY "invoices_select" ON public.invoices FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "invoices_insert" ON public.invoices FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "invoices_update" ON public.invoices FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "invoices_delete" ON public.invoices FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  PAYMENTS  ----------
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "payments_select" ON public.payments FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "payments_insert" ON public.payments FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "payments_update" ON public.payments FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "payments_delete" ON public.payments FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  COMMISSIONS  ----------
ALTER TABLE public.commissions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "commissions_select" ON public.commissions FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "commissions_insert" ON public.commissions FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "commissions_update" ON public.commissions FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "commissions_delete" ON public.commissions FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  EXPENSES  ----------
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "expenses_select" ON public.expenses FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "expenses_insert" ON public.expenses FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "expenses_update" ON public.expenses FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "expenses_delete" ON public.expenses FOR DELETE
    USING (tenant_id = public.current_tenant_id());

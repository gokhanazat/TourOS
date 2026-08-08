-- ============================================================
-- TourOS Migration: 20260808_009_finance_tables_and_relations.sql
-- Prompt 0.2.6: Finans Tabloları (accounts, invoices, payments, commissions, expenses)
-- payments -> invoices, commissions -> bookings FK ilişkileri ve negatif tutar CHECK engeli.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. ACCOUNTS TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.accounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_name    TEXT NOT NULL,
    account_type    TEXT NOT NULL DEFAULT 'BANK', -- BANK, CASH, CREDIT_CARD, AGENCY, SUPPLIER
    currency        TEXT NOT NULL DEFAULT 'TRY',
    balance         NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.accounts ADD COLUMN IF NOT EXISTS account_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.accounts ADD COLUMN IF NOT EXISTS account_type TEXT NOT NULL DEFAULT 'BANK';
ALTER TABLE public.accounts ADD COLUMN IF NOT EXISTS currency TEXT NOT NULL DEFAULT 'TRY';
ALTER TABLE public.accounts ADD COLUMN IF NOT EXISTS balance NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.accounts ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE public.accounts ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_accounts_tenant ON public.accounts(tenant_id);

-- ===================  2. INVOICES TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.invoices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number  TEXT NOT NULL,
    booking_id      UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    customer_name   TEXT NOT NULL,
    tax_number      TEXT,
    tax_office      TEXT,
    address         TEXT,
    subtotal        NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_invoices_subtotal CHECK (subtotal >= 0),
    tax_amount      NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_invoices_tax CHECK (tax_amount >= 0),
    total_amount    NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_invoices_total CHECK (total_amount >= 0),
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_invoices_paid CHECK (paid_amount >= 0),
    currency        TEXT NOT NULL DEFAULT 'TRY',
    status          TEXT NOT NULL DEFAULT 'DRAFT', -- DRAFT, ISSUED, PAID, CANCELLED
    issue_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT uq_invoices_tenant_number UNIQUE (tenant_id, invoice_number)
);

ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS invoice_number TEXT NOT NULL DEFAULT '';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS customer_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS subtotal NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS currency TEXT NOT NULL DEFAULT 'TRY';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'DRAFT';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS issue_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_invoices_tenant ON public.invoices(tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoices_booking ON public.invoices(booking_id);

-- ===================  3. PAYMENTS TABLOSU  =================
-- payments -> invoices (FK: invoice_id ON DELETE SET NULL)
CREATE TABLE IF NOT EXISTS public.payments (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id              UUID REFERENCES public.invoices(id) ON DELETE SET NULL,
    booking_id              UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    account_id              UUID REFERENCES public.accounts(id) ON DELETE SET NULL,
    payment_method          TEXT NOT NULL DEFAULT 'CREDIT_CARD', -- CREDIT_CARD, BANK_TRANSFER, CASH, PAYPAL
    amount                  NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_payments_amount CHECK (amount >= 0),
    currency                TEXT NOT NULL DEFAULT 'TRY',
    transaction_reference  TEXT,
    payment_date            TIMESTAMPTZ NOT NULL DEFAULT now(),
    status                  TEXT NOT NULL DEFAULT 'COMPLETED', -- PENDING, COMPLETED, FAILED, REFUNDED

    -- Audit & Multi-tenant
    tenant_id               UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID
);

ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS invoice_id UUID REFERENCES public.invoices(id) ON DELETE SET NULL;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS account_id UUID REFERENCES public.accounts(id) ON DELETE SET NULL;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS payment_method TEXT NOT NULL DEFAULT 'CREDIT_CARD';
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS amount NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS currency TEXT NOT NULL DEFAULT 'TRY';
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS transaction_reference TEXT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS payment_date TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_payments_invoice ON public.payments(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_booking ON public.payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_account ON public.payments(account_id);
CREATE INDEX IF NOT EXISTS idx_payments_tenant ON public.payments(tenant_id);

-- ===================  4. COMMISSIONS TABLOSU  =============
-- commissions -> bookings (FK: booking_id ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS public.commissions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    agency_name     TEXT NOT NULL,
    rate_percent    NUMERIC(5,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_commissions_rate CHECK (rate_percent >= 0),
    amount          NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_commissions_amount CHECK (amount >= 0),
    currency        TEXT NOT NULL DEFAULT 'TRY',
    is_paid         BOOLEAN NOT NULL DEFAULT FALSE,
    paid_date       TIMESTAMPTZ,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS booking_id UUID REFERENCES public.bookings(id) ON DELETE CASCADE;
ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS agency_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS rate_percent NUMERIC(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS amount NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS currency TEXT NOT NULL DEFAULT 'TRY';
ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS is_paid BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE public.commissions ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_commissions_booking ON public.commissions(booking_id);
CREATE INDEX IF NOT EXISTS idx_commissions_tenant ON public.commissions(tenant_id);

-- ===================  5. EXPENSES TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.expenses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    category        TEXT NOT NULL DEFAULT 'OPERATIONAL', -- OPERATIONAL, MARKETING, SALARY, VEHICLE, HOTEL, OTHER
    amount          NUMERIC(12,2) NOT NULL DEFAULT 0.00 CONSTRAINT chk_expenses_amount CHECK (amount >= 0),
    currency        TEXT NOT NULL DEFAULT 'TRY',
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    receipt_url     TEXT,
    notes           TEXT,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS title TEXT NOT NULL DEFAULT '';
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS category TEXT NOT NULL DEFAULT 'OPERATIONAL';
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS amount NUMERIC(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS currency TEXT NOT NULL DEFAULT 'TRY';
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS expense_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_expenses_tenant ON public.expenses(tenant_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DO $$ BEGIN
    DROP TRIGGER IF EXISTS trg_accounts_updated_at ON public.accounts;
    CREATE TRIGGER trg_accounts_updated_at BEFORE UPDATE ON public.accounts FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_invoices_updated_at ON public.invoices;
    CREATE TRIGGER trg_invoices_updated_at BEFORE UPDATE ON public.invoices FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_payments_updated_at ON public.payments;
    CREATE TRIGGER trg_payments_updated_at BEFORE UPDATE ON public.payments FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_commissions_updated_at ON public.commissions;
    CREATE TRIGGER trg_commissions_updated_at BEFORE UPDATE ON public.commissions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_expenses_updated_at ON public.expenses;
    CREATE TRIGGER trg_expenses_updated_at BEFORE UPDATE ON public.expenses FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
END $$;

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.commissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;

-- ACCOUNTS RLS
DROP POLICY IF EXISTS "accounts_select" ON public.accounts;
DROP POLICY IF EXISTS "accounts_insert" ON public.accounts;
DROP POLICY IF EXISTS "accounts_update" ON public.accounts;
DROP POLICY IF EXISTS "accounts_delete" ON public.accounts;

CREATE POLICY "accounts_select" ON public.accounts FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "accounts_insert" ON public.accounts FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "accounts_update" ON public.accounts FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "accounts_delete" ON public.accounts FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- INVOICES RLS
DROP POLICY IF EXISTS "invoices_select" ON public.invoices;
DROP POLICY IF EXISTS "invoices_insert" ON public.invoices;
DROP POLICY IF EXISTS "invoices_update" ON public.invoices;
DROP POLICY IF EXISTS "invoices_delete" ON public.invoices;

CREATE POLICY "invoices_select" ON public.invoices FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "invoices_insert" ON public.invoices FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "invoices_update" ON public.invoices FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "invoices_delete" ON public.invoices FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- PAYMENTS RLS
DROP POLICY IF EXISTS "payments_select" ON public.payments;
DROP POLICY IF EXISTS "payments_insert" ON public.payments;
DROP POLICY IF EXISTS "payments_update" ON public.payments;
DROP POLICY IF EXISTS "payments_delete" ON public.payments;

CREATE POLICY "payments_select" ON public.payments FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "payments_insert" ON public.payments FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "payments_update" ON public.payments FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "payments_delete" ON public.payments FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- COMMISSIONS RLS
DROP POLICY IF EXISTS "commissions_select" ON public.commissions;
DROP POLICY IF EXISTS "commissions_insert" ON public.commissions;
DROP POLICY IF EXISTS "commissions_update" ON public.commissions;
DROP POLICY IF EXISTS "commissions_delete" ON public.commissions;

CREATE POLICY "commissions_select" ON public.commissions FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "commissions_insert" ON public.commissions FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "commissions_update" ON public.commissions FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "commissions_delete" ON public.commissions FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- EXPENSES RLS
DROP POLICY IF EXISTS "expenses_select" ON public.expenses;
DROP POLICY IF EXISTS "expenses_insert" ON public.expenses;
DROP POLICY IF EXISTS "expenses_update" ON public.expenses;
DROP POLICY IF EXISTS "expenses_delete" ON public.expenses;

CREATE POLICY "expenses_select" ON public.expenses FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "expenses_insert" ON public.expenses FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "expenses_update" ON public.expenses FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "expenses_delete" ON public.expenses FOR DELETE USING (public.is_valid_tenant(tenant_id));

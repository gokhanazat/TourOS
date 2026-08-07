-- ============================================================
-- TourOS 3.1.4 Current Account Statement RPC Function SQL
-- Pre-requisites included: customers & agencies
-- ============================================================

CREATE TABLE IF NOT EXISTS public.customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    tc_no TEXT,
    passport_no TEXT,
    birth_date DATE,
    gender TEXT,
    address TEXT,
    city TEXT,
    country TEXT NOT NULL DEFAULT 'TR',
    source TEXT NOT NULL DEFAULT 'direct',
    tags TEXT[],
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    UNIQUE (tenant_id, email)
);

CREATE TABLE IF NOT EXISTS public.agencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    contact_person TEXT,
    email TEXT,
    phone TEXT,
    address TEXT,
    city TEXT,
    country TEXT NOT NULL DEFAULT 'TR',
    tax_no TEXT,
    commission_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    balance NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency TEXT NOT NULL DEFAULT 'TRY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    UNIQUE (tenant_id, name)
);

CREATE OR REPLACE FUNCTION public.get_current_account_statement(
    p_tenant_id UUID,
    p_entity_type TEXT DEFAULT NULL
)
RETURNS TABLE (
    entity_id UUID,
    entity_name TEXT,
    entity_type TEXT,
    phone TEXT,
    email TEXT,
    total_debit NUMERIC(14,2),
    total_credit NUMERIC(14,2),
    balance NUMERIC(14,2),
    currency TEXT,
    last_transaction_date TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    -- 1. Müşteriler (Customers)
    SELECT 
        c.id AS entity_id,
        c.full_name AS entity_name,
        'customer'::TEXT AS entity_type,
        c.phone,
        c.email,
        COALESCE(SUM(b.total_price)::NUMERIC(14,2), 0) AS total_debit,
        COALESCE(SUM(inv.total_amount)::NUMERIC(14,2), 0) AS total_credit,
        COALESCE(SUM(b.total_price - inv.total_amount)::NUMERIC(14,2), 0) AS balance,
        'TRY'::TEXT AS currency,
        MAX(b.created_at) AS last_transaction_date
    FROM public.customers c
    LEFT JOIN public.bookings b ON b.customer_email = c.email AND b.tenant_id = p_tenant_id
    LEFT JOIN public.invoices inv ON inv.booking_id = b.id AND inv.tenant_id = p_tenant_id
    WHERE c.tenant_id = p_tenant_id
      AND (p_entity_type IS NULL OR p_entity_type = 'customer')
    GROUP BY c.id, c.full_name, c.phone, c.email

    UNION ALL

    -- 2. Acenteler (Agencies)
    SELECT 
        a.id AS entity_id,
        a.name AS entity_name,
        'agency'::TEXT AS entity_type,
        a.phone,
        a.email,
        COALESCE(SUM(com.amount)::NUMERIC(14,2), 0) AS total_debit,
        COALESCE(SUM(CASE WHEN com.is_paid THEN com.amount ELSE 0 END)::NUMERIC(14,2), 0) AS total_credit,
        COALESCE(SUM(CASE WHEN NOT com.is_paid THEN com.amount ELSE 0 END)::NUMERIC(14,2), 0) AS balance,
        'TRY'::TEXT AS currency,
        MAX(com.created_at) AS last_transaction_date
    FROM public.agencies a
    LEFT JOIN public.commissions com ON com.agent_name = a.name AND com.tenant_id = p_tenant_id
    WHERE a.tenant_id = p_tenant_id
      AND (p_entity_type IS NULL OR p_entity_type = 'agency')
    GROUP BY a.id, a.name, a.phone, a.email

    UNION ALL

    -- 3. Tedarikçiler (Suppliers)
    SELECT 
        st.id AS entity_id,
        st.supplier_name AS entity_name,
        'supplier'::TEXT AS entity_type,
        ''::TEXT AS phone,
        ''::TEXT AS email,
        COALESCE(SUM(CASE WHEN st.transaction_type = 'debt' THEN st.amount ELSE 0 END)::NUMERIC(14,2), 0) AS total_debit,
        COALESCE(SUM(CASE WHEN st.is_settled THEN st.amount ELSE 0 END)::NUMERIC(14,2), 0) AS total_credit,
        COALESCE(SUM(CASE WHEN NOT st.is_settled THEN st.amount ELSE 0 END)::NUMERIC(14,2), 0) AS balance,
        'TRY'::TEXT AS currency,
        MAX(st.created_at) AS last_transaction_date
    FROM public.supplier_transactions st
    WHERE st.tenant_id = p_tenant_id
      AND (p_entity_type IS NULL OR p_entity_type = 'supplier')
    GROUP BY st.id, st.supplier_name;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================
-- TourOS 3.2.3 Pay by Link (Link ile Ödeme) Migration SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.payment_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_link_code TEXT NOT NULL UNIQUE,
    booking_id UUID REFERENCES public.bookings(id) ON DELETE CASCADE,
    amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    currency TEXT NOT NULL DEFAULT 'TRY',
    gateway_provider TEXT NOT NULL DEFAULT 'stripe', -- stripe | iyzico | paytr
    checkout_url TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING', -- PENDING | PAID | EXPIRED | CANCELLED
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '24 hours'),
    paid_at TIMESTAMPTZ,
    customer_email TEXT,
    customer_phone TEXT,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.payment_links ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "payment_links_tenant_policy" ON public.payment_links;
CREATE POLICY "payment_links_tenant_policy" ON public.payment_links
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

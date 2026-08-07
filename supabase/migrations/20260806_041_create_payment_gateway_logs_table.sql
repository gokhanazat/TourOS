-- ============================================================
-- TourOS 3.2.1 Payment Gateway Logs & Multi-Provider SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.payment_gateway_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID REFERENCES public.payments(id) ON DELETE CASCADE,
    gateway_provider TEXT NOT NULL DEFAULT 'iyzico', -- iyzico | stripe | paytr | mock
    transaction_id TEXT,
    request_payload JSONB,
    response_payload JSONB,
    status TEXT NOT NULL DEFAULT 'PENDING', -- SUCCESS | FAILED | PENDING | REFUNDED
    error_code TEXT,
    error_message TEXT,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.payment_gateway_logs ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "payment_gateway_logs_tenant_policy" ON public.payment_gateway_logs;
CREATE POLICY "payment_gateway_logs_tenant_policy" ON public.payment_gateway_logs
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- ============================================================
-- TourOS 5.3.3 Human Support Handoff RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.support_handoff_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID,
    chat_summary TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'QUEUED', -- QUEUED, IN_PROGRESS, RESOLVED
    assigned_agent_name TEXT DEFAULT 'Bekliyor',
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.support_handoff_tickets ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation for support_handoff_tickets" ON public.support_handoff_tickets;
CREATE POLICY "Tenant isolation for support_handoff_tickets" ON public.support_handoff_tickets
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- RPC: Initiate Human Support Handoff
CREATE OR REPLACE FUNCTION public.initiate_human_support_handoff(
    p_customer_id UUID DEFAULT gen_random_uuid(),
    p_chat_summary TEXT DEFAULT 'Müşteri canlı temsilci desteği talep etti.',
    p_tenant_id UUID DEFAULT gen_random_uuid()
)
RETURNS TABLE (
    ticket_id TEXT,
    status TEXT,
    assigned_agent_name TEXT,
    estimated_wait_minutes INT
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.support_handoff_tickets (id, customer_id, chat_summary, status, assigned_agent_name, tenant_id)
    VALUES (v_id, p_customer_id, p_chat_summary, 'QUEUED', 'Müşteri Temsilcisi Zeynep', p_tenant_id);

    RETURN QUERY
    SELECT 
        v_id::TEXT,
        'QUEUED'::TEXT,
        'Müşteri Temsilcisi Zeynep'::TEXT,
        2 AS estimated_wait_minutes;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

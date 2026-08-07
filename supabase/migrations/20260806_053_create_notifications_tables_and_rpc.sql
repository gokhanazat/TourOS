-- ============================================================
-- TourOS 3.4.4 NotificationService (Push, SMS, WhatsApp, E-Mail) RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.notifications_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel TEXT NOT NULL,
    recipient TEXT NOT NULL,
    title TEXT,
    content TEXT NOT NULL,
    status TEXT DEFAULT 'SENT',
    provider TEXT DEFAULT 'FCM',
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Güvenliği
ALTER TABLE public.notifications_log ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Tenant Isolated Notifications Access" ON public.notifications_log;
CREATE POLICY "Tenant Isolated Notifications Access" ON public.notifications_log
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- RPC: Bildirim Gönderim Günlüğü Kaydetme
CREATE OR REPLACE FUNCTION public.log_notification_dispatch(
    p_tenant_id UUID,
    p_channel TEXT,
    p_recipient TEXT,
    p_title TEXT,
    p_content TEXT,
    p_provider TEXT DEFAULT 'Firebase/FCM'
)
RETURNS TABLE (
    notification_id UUID,
    status TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_new_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.notifications_log (
        id, channel, recipient, title, content, status, provider, tenant_id
    ) VALUES (
        v_new_id, UPPER(p_channel), p_recipient, p_title, p_content, 'SENT', p_provider, p_tenant_id
    );

    RETURN QUERY
    SELECT v_new_id, 'SENT'::TEXT, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

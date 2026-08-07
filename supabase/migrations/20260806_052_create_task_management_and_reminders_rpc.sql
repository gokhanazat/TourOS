-- ============================================================
-- TourOS 3.4.3 Staff Task Management, Reminders & Calendar Integration RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.staff_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT,
    assigned_to TEXT,
    due_date TIMESTAMPTZ NOT NULL,
    priority TEXT DEFAULT 'MEDIUM',
    status TEXT DEFAULT 'PENDING',
    reminder_minutes_before INT DEFAULT 30,
    calendar_event_id TEXT,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Güvenliği
ALTER TABLE public.staff_tasks ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Tenant Isolated Staff Tasks Access" ON public.staff_tasks;
CREATE POLICY "Tenant Isolated Staff Tasks Access" ON public.staff_tasks
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- RPC: Personel Görevlerini ve Takvim Hatırlatmalarını Getir
CREATE OR REPLACE FUNCTION public.get_staff_tasks_with_reminders(
    p_tenant_id UUID
)
RETURNS TABLE (
    task_id UUID,
    title TEXT,
    description TEXT,
    assigned_to TEXT,
    due_date TIMESTAMPTZ,
    priority TEXT,
    status TEXT,
    reminder_minutes_before INT,
    calendar_event_id TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        st.id AS task_id,
        st.title,
        st.description,
        st.assigned_to,
        st.due_date,
        st.priority,
        st.status,
        st.reminder_minutes_before,
        st.calendar_event_id,
        st.created_at
    FROM public.staff_tasks st
    WHERE st.tenant_id = p_tenant_id
    ORDER BY st.due_date ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- TourOS Support Tables Migration
-- documents, images (polymorphic), vouchers, notifications,
-- tasks, calendars, audit_logs
-- ============================================================

-- ===================  1. DOCUMENTS  =========================
-- Polymorphic: owner_type + owner_id ile herhangi bir tabloya bağlanır
CREATE TABLE IF NOT EXISTS public.documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_type      TEXT NOT NULL,                    -- booking | tour | customer | hotel | vehicle | invoice
    owner_id        UUID NOT NULL,                    -- ilgili kaydın id'si
    title           TEXT NOT NULL,
    file_url        TEXT NOT NULL,
    file_type       TEXT,                             -- pdf | docx | xlsx …
    file_size_bytes BIGINT,
    mime_type       TEXT,
    is_public       BOOLEAN NOT NULL DEFAULT FALSE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_documents_tenant ON public.documents(tenant_id);
CREATE INDEX idx_documents_owner  ON public.documents(owner_type, owner_id);

-- ===================  2. IMAGES  ============================
-- Polymorphic: owner_type + owner_id ile herhangi bir tabloya bağlanır
CREATE TABLE IF NOT EXISTS public.images (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_type      TEXT NOT NULL,                    -- tour | hotel | room_type | customer | vehicle | guide
    owner_id        UUID NOT NULL,
    url             TEXT NOT NULL,
    thumbnail_url   TEXT,
    alt_text        TEXT,
    sort_order      INT NOT NULL DEFAULT 0,
    is_cover        BOOLEAN NOT NULL DEFAULT FALSE,
    width           INT,
    height          INT,
    file_size_bytes BIGINT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_images_tenant ON public.images(tenant_id);
CREATE INDEX idx_images_owner  ON public.images(owner_type, owner_id);
CREATE INDEX idx_images_cover  ON public.images(owner_type, owner_id) WHERE is_cover = TRUE;

-- ===================  3. VOUCHERS  ==========================
CREATE TABLE IF NOT EXISTS public.vouchers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    voucher_no      TEXT NOT NULL,
    voucher_type    TEXT NOT NULL DEFAULT 'hotel',    -- hotel | transfer | tour | activity
    content         JSONB NOT NULL DEFAULT '{}',      -- dinamik voucher içeriği
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_from      DATE,
    valid_until     DATE,
    status          TEXT NOT NULL DEFAULT 'active',   -- active | used | cancelled | expired
    pdf_url         TEXT,
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, voucher_no)
);

CREATE INDEX idx_vouchers_tenant  ON public.vouchers(tenant_id);
CREATE INDEX idx_vouchers_booking ON public.vouchers(booking_id);
CREATE INDEX idx_vouchers_status  ON public.vouchers(status);

-- ===================  4. NOTIFICATIONS  =====================
CREATE TABLE IF NOT EXISTS public.notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title           TEXT NOT NULL,
    body            TEXT,
    channel         TEXT NOT NULL DEFAULT 'in_app',   -- in_app | push | email | sms
    ref_type        TEXT,                             -- booking | departure | task …
    ref_id          UUID,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_notifications_tenant  ON public.notifications(tenant_id);
CREATE INDEX idx_notifications_user    ON public.notifications(user_id);
CREATE INDEX idx_notifications_unread  ON public.notifications(user_id) WHERE is_read = FALSE;

-- ===================  5. TASKS  =============================
CREATE TABLE IF NOT EXISTS public.tasks (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    description     TEXT,
    assigned_to     UUID REFERENCES public.users(id) ON DELETE SET NULL,
    ref_type        TEXT,                             -- booking | departure | transfer …
    ref_id          UUID,
    priority        TEXT NOT NULL DEFAULT 'medium',   -- low | medium | high | urgent
    status          TEXT NOT NULL DEFAULT 'open',     -- open | in_progress | done | cancelled
    due_date        TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_tasks_tenant   ON public.tasks(tenant_id);
CREATE INDEX idx_tasks_assigned ON public.tasks(assigned_to);
CREATE INDEX idx_tasks_status   ON public.tasks(status);
CREATE INDEX idx_tasks_due      ON public.tasks(due_date);

-- ===================  6. CALENDARS  =========================
CREATE TABLE IF NOT EXISTS public.calendars (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    description     TEXT,
    event_type      TEXT NOT NULL DEFAULT 'departure', -- departure | meeting | deadline | holiday | custom
    ref_type        TEXT,                              -- departure | booking | task …
    ref_id          UUID,
    start_at        TIMESTAMPTZ NOT NULL,
    end_at          TIMESTAMPTZ,
    is_all_day      BOOLEAN NOT NULL DEFAULT FALSE,
    color           TEXT,                              -- hex renk kodu
    assigned_to     UUID REFERENCES public.users(id) ON DELETE SET NULL,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_calendars_tenant   ON public.calendars(tenant_id);
CREATE INDEX idx_calendars_range    ON public.calendars(start_at, end_at);
CREATE INDEX idx_calendars_assigned ON public.calendars(assigned_to);
CREATE INDEX idx_calendars_type     ON public.calendars(event_type);

-- ===================  7. AUDIT_LOGS  ========================
-- Tüm kritik yazma işlemlerini kaydeden immutable log tablosu
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_name      TEXT NOT NULL,                    -- etkilenen tablo adı
    record_id       UUID NOT NULL,                    -- etkilenen kaydın id'si
    action          TEXT NOT NULL,                    -- INSERT | UPDATE | DELETE
    old_data        JSONB,                            -- önceki değer (UPDATE/DELETE)
    new_data        JSONB,                            -- yeni değer (INSERT/UPDATE)
    changed_fields  TEXT[],                           -- değişen kolon adları
    ip_address      TEXT,
    user_agent      TEXT,
    performed_by    UUID,                             -- auth.uid()

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_audit_logs_tenant     ON public.audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_table      ON public.audit_logs(table_name);
CREATE INDEX idx_audit_logs_record     ON public.audit_logs(table_name, record_id);
CREATE INDEX idx_audit_logs_action     ON public.audit_logs(action);
CREATE INDEX idx_audit_logs_performer  ON public.audit_logs(performed_by);
CREATE INDEX idx_audit_logs_created    ON public.audit_logs(created_at);

-- ============================================================
-- GENERIC AUDIT TRIGGER FUNCTION
-- Kritik tablolara bağlanarak INSERT/UPDATE/DELETE loglar
-- ============================================================
CREATE OR REPLACE FUNCTION public.fn_audit_log()
RETURNS TRIGGER AS $$
DECLARE
    v_old JSONB := NULL;
    v_new JSONB := NULL;
    v_changed TEXT[] := '{}';
    v_key TEXT;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_old := to_jsonb(OLD);
        INSERT INTO public.audit_logs (table_name, record_id, action, old_data, new_data, changed_fields, performed_by, tenant_id, created_by)
        VALUES (TG_TABLE_NAME, OLD.id, 'DELETE', v_old, NULL, NULL, auth.uid(), OLD.tenant_id, auth.uid());
        RETURN OLD;
    END IF;

    IF TG_OP = 'INSERT' THEN
        v_new := to_jsonb(NEW);
        INSERT INTO public.audit_logs (table_name, record_id, action, old_data, new_data, changed_fields, performed_by, tenant_id, created_by)
        VALUES (TG_TABLE_NAME, NEW.id, 'INSERT', NULL, v_new, NULL, auth.uid(), NEW.tenant_id, auth.uid());
        RETURN NEW;
    END IF;

    IF TG_OP = 'UPDATE' THEN
        v_old := to_jsonb(OLD);
        v_new := to_jsonb(NEW);
        -- Değişen alanları tespit et
        FOR v_key IN SELECT jsonb_object_keys(v_new)
        LOOP
            IF v_old ->> v_key IS DISTINCT FROM v_new ->> v_key THEN
                v_changed := array_append(v_changed, v_key);
            END IF;
        END LOOP;
        INSERT INTO public.audit_logs (table_name, record_id, action, old_data, new_data, changed_fields, performed_by, tenant_id, created_by)
        VALUES (TG_TABLE_NAME, NEW.id, 'UPDATE', v_old, v_new, v_changed, auth.uid(), NEW.tenant_id, auth.uid());
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Kritik tablolara audit trigger bağla
CREATE TRIGGER trg_audit_bookings
    AFTER INSERT OR UPDATE OR DELETE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.fn_audit_log();

CREATE TRIGGER trg_audit_payments
    AFTER INSERT OR UPDATE OR DELETE ON public.payments
    FOR EACH ROW EXECUTE FUNCTION public.fn_audit_log();

CREATE TRIGGER trg_audit_invoices
    AFTER INSERT OR UPDATE OR DELETE ON public.invoices
    FOR EACH ROW EXECUTE FUNCTION public.fn_audit_log();

CREATE TRIGGER trg_audit_users
    AFTER INSERT OR UPDATE OR DELETE ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.fn_audit_log();

CREATE TRIGGER trg_audit_roles
    AFTER INSERT OR UPDATE OR DELETE ON public.roles
    FOR EACH ROW EXECUTE FUNCTION public.fn_audit_log();

CREATE TRIGGER trg_audit_permissions
    AFTER INSERT OR UPDATE OR DELETE ON public.permissions
    FOR EACH ROW EXECUTE FUNCTION public.fn_audit_log();

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_documents_updated_at
    BEFORE UPDATE ON public.documents
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_images_updated_at
    BEFORE UPDATE ON public.images
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_vouchers_updated_at
    BEFORE UPDATE ON public.vouchers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_notifications_updated_at
    BEFORE UPDATE ON public.notifications
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON public.tasks
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_calendars_updated_at
    BEFORE UPDATE ON public.calendars
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_audit_logs_updated_at
    BEFORE UPDATE ON public.audit_logs
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  DOCUMENTS  ----------
ALTER TABLE public.documents ENABLE ROW LEVEL SECURITY;

CREATE POLICY "documents_select" ON public.documents FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "documents_insert" ON public.documents FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "documents_update" ON public.documents FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "documents_delete" ON public.documents FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  IMAGES  ----------
ALTER TABLE public.images ENABLE ROW LEVEL SECURITY;

CREATE POLICY "images_select" ON public.images FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "images_insert" ON public.images FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "images_update" ON public.images FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "images_delete" ON public.images FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  VOUCHERS  ----------
ALTER TABLE public.vouchers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "vouchers_select" ON public.vouchers FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "vouchers_insert" ON public.vouchers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "vouchers_update" ON public.vouchers FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "vouchers_delete" ON public.vouchers FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  NOTIFICATIONS  ----------
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "notifications_select" ON public.notifications FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "notifications_insert" ON public.notifications FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "notifications_update" ON public.notifications FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "notifications_delete" ON public.notifications FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  TASKS  ----------
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tasks_select" ON public.tasks FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "tasks_insert" ON public.tasks FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tasks_update" ON public.tasks FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tasks_delete" ON public.tasks FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  CALENDARS  ----------
ALTER TABLE public.calendars ENABLE ROW LEVEL SECURITY;

CREATE POLICY "calendars_select" ON public.calendars FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "calendars_insert" ON public.calendars FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "calendars_update" ON public.calendars FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "calendars_delete" ON public.calendars FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  AUDIT_LOGS  ----------
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- Audit loglar sadece okunabilir, silinemez/güncellenemez (immutable)
CREATE POLICY "audit_logs_select" ON public.audit_logs FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "audit_logs_insert" ON public.audit_logs FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
-- UPDATE ve DELETE policy yok → immutable log

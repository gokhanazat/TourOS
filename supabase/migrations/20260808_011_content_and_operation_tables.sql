-- ============================================================
-- TourOS Migration: 20260808_011_content_and_operation_tables.sql
-- Prompt 0.2.8: İçerik ve Operasyon Tabloları (documents, images, vouchers, notifications, tasks, calendars, audit_logs)
-- documents & images için Polymorphic (owner_type, owner_id) ilişki.
-- audit_logs için kritik yazma işlemlerini izleyen altyapı ve RLS.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. DOCUMENTS TABLOSU  ================
-- Polymorphic: owner_type, owner_id
CREATE TABLE IF NOT EXISTS public.documents (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_type      TEXT NOT NULL, -- TOUR, BOOKING, HOTEL, CUSTOMER, VEHICLE, COMPANY
    owner_id        UUID NOT NULL,
    title           TEXT NOT NULL,
    file_url        TEXT NOT NULL,
    file_type       TEXT, -- PDF, DOCX, PNG, JPG
    file_size_bytes BIGINT,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.documents ADD COLUMN IF NOT EXISTS owner_type TEXT NOT NULL DEFAULT 'TOUR';
ALTER TABLE public.documents ADD COLUMN IF NOT EXISTS owner_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE public.documents ADD COLUMN IF NOT EXISTS title TEXT NOT NULL DEFAULT '';
ALTER TABLE public.documents ADD COLUMN IF NOT EXISTS file_url TEXT NOT NULL DEFAULT '';
ALTER TABLE public.documents ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_documents_owner ON public.documents(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_documents_tenant ON public.documents(tenant_id);

-- ===================  2. IMAGES TABLOSU  ===================
-- Polymorphic: owner_type, owner_id
CREATE TABLE IF NOT EXISTS public.images (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_type      TEXT NOT NULL, -- TOUR, HOTEL, VEHICLE, COMPANY, USER
    owner_id        UUID NOT NULL,
    url             TEXT NOT NULL,
    alt_text        TEXT,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.images ADD COLUMN IF NOT EXISTS owner_type TEXT NOT NULL DEFAULT 'TOUR';
ALTER TABLE public.images ADD COLUMN IF NOT EXISTS owner_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE public.images ADD COLUMN IF NOT EXISTS url TEXT NOT NULL DEFAULT '';
ALTER TABLE public.images ADD COLUMN IF NOT EXISTS is_primary BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE public.images ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_images_owner ON public.images(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_images_tenant ON public.images(tenant_id);

-- ===================  3. VOUCHERS TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.vouchers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    voucher_code    TEXT NOT NULL,
    booking_id      UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    voucher_type    TEXT NOT NULL DEFAULT 'HOTEL', -- HOTEL, TOUR, TRANSFER
    qr_code_url     TEXT,
    pdf_url         TEXT,
    status          TEXT NOT NULL DEFAULT 'VALID', -- VALID, USED, EXPIRED, CANCELLED
    valid_until     DATE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT uq_vouchers_tenant_code UNIQUE (tenant_id, voucher_code)
);

ALTER TABLE public.vouchers ADD COLUMN IF NOT EXISTS voucher_code TEXT NOT NULL DEFAULT '';
ALTER TABLE public.vouchers ADD COLUMN IF NOT EXISTS booking_id UUID REFERENCES public.bookings(id) ON DELETE CASCADE;
ALTER TABLE public.vouchers ADD COLUMN IF NOT EXISTS voucher_type TEXT NOT NULL DEFAULT 'HOTEL';
ALTER TABLE public.vouchers ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'VALID';
ALTER TABLE public.vouchers ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_vouchers_booking ON public.vouchers(booking_id);
CREATE INDEX IF NOT EXISTS idx_vouchers_tenant ON public.vouchers(tenant_id);

-- ===================  4. NOTIFICATIONS TABLOSU  ============
CREATE TABLE IF NOT EXISTS public.notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES public.users(id) ON DELETE CASCADE,
    title           TEXT NOT NULL,
    message         TEXT NOT NULL,
    type            TEXT NOT NULL DEFAULT 'INFO', -- INFO, WARNING, SUCCESS, URGENT
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    action_url      TEXT,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS title TEXT NOT NULL DEFAULT '';
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS message TEXT NOT NULL DEFAULT '';
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_notifications_user ON public.notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_tenant ON public.notifications(tenant_id);

-- ===================  5. TASKS TABLOSU  ====================
CREATE TABLE IF NOT EXISTS public.tasks (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title               TEXT NOT NULL,
    description         TEXT,
    assigned_to         UUID REFERENCES public.users(id) ON DELETE SET NULL,
    due_date            TIMESTAMPTZ,
    priority            TEXT NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    status              TEXT NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    related_entity_type TEXT, -- BOOKING, TOUR, TRANSFER, HOTEL
    related_entity_id   UUID,

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID
);

ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS title TEXT NOT NULL DEFAULT '';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS assigned_to UUID REFERENCES public.users(id) ON DELETE SET NULL;
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS priority TEXT NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'PENDING';
ALTER TABLE public.tasks ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_tasks_assigned ON public.tasks(assigned_to, status);
CREATE INDEX IF NOT EXISTS idx_tasks_tenant ON public.tasks(tenant_id);

-- ===================  6. CALENDARS TABLOSU  ================
CREATE TABLE IF NOT EXISTS public.calendars (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title               TEXT NOT NULL,
    event_type          TEXT NOT NULL DEFAULT 'DEPARTURE', -- DEPARTURE, TRANSFER, HOTEL_CHECKIN, TASK, MEETING
    start_time          TIMESTAMPTZ NOT NULL,
    end_time            TIMESTAMPTZ,
    is_all_day          BOOLEAN NOT NULL DEFAULT FALSE,
    location            TEXT,
    description         TEXT,
    related_entity_type TEXT,
    related_entity_id   UUID,

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID
);

ALTER TABLE public.calendars ADD COLUMN IF NOT EXISTS title TEXT NOT NULL DEFAULT '';
ALTER TABLE public.calendars ADD COLUMN IF NOT EXISTS event_type TEXT NOT NULL DEFAULT 'DEPARTURE';
ALTER TABLE public.calendars ADD COLUMN IF NOT EXISTS start_time TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE public.calendars ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_calendars_start ON public.calendars(start_time);
CREATE INDEX IF NOT EXISTS idx_calendars_tenant ON public.calendars(tenant_id);

-- ===================  7. AUDIT_LOGS TABLOSU  ===============
-- Tüm kritik yazma işlemlerini (INSERT, UPDATE, DELETE) kaydeder.
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_name      TEXT NOT NULL,
    record_id       UUID NOT NULL,
    action          TEXT NOT NULL, -- INSERT, UPDATE, DELETE
    old_values      JSONB,
    new_values      JSONB,
    changed_by      UUID,
    ip_address      TEXT,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.audit_logs ADD COLUMN IF NOT EXISTS table_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.audit_logs ADD COLUMN IF NOT EXISTS record_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE public.audit_logs ADD COLUMN IF NOT EXISTS action TEXT NOT NULL DEFAULT 'INSERT';
ALTER TABLE public.audit_logs ADD COLUMN IF NOT EXISTS old_values JSONB;
ALTER TABLE public.audit_logs ADD COLUMN IF NOT EXISTS new_values JSONB;
ALTER TABLE public.audit_logs ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_audit_logs_table_record ON public.audit_logs(table_name, record_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant ON public.audit_logs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON public.audit_logs(created_at);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DO $$ BEGIN
    DROP TRIGGER IF EXISTS trg_documents_updated_at ON public.documents;
    CREATE TRIGGER trg_documents_updated_at BEFORE UPDATE ON public.documents FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_images_updated_at ON public.images;
    CREATE TRIGGER trg_images_updated_at BEFORE UPDATE ON public.images FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_vouchers_updated_at ON public.vouchers;
    CREATE TRIGGER trg_vouchers_updated_at BEFORE UPDATE ON public.vouchers FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_notifications_updated_at ON public.notifications;
    CREATE TRIGGER trg_notifications_updated_at BEFORE UPDATE ON public.notifications FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_tasks_updated_at ON public.tasks;
    CREATE TRIGGER trg_tasks_updated_at BEFORE UPDATE ON public.tasks FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_calendars_updated_at ON public.calendars;
    CREATE TRIGGER trg_calendars_updated_at BEFORE UPDATE ON public.calendars FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
END $$;

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.images ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.vouchers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.calendars ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- DOCUMENTS RLS
DROP POLICY IF EXISTS "documents_select" ON public.documents;
DROP POLICY IF EXISTS "documents_insert" ON public.documents;
DROP POLICY IF EXISTS "documents_update" ON public.documents;
DROP POLICY IF EXISTS "documents_delete" ON public.documents;

CREATE POLICY "documents_select" ON public.documents FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "documents_insert" ON public.documents FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "documents_update" ON public.documents FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "documents_delete" ON public.documents FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- IMAGES RLS
DROP POLICY IF EXISTS "images_select" ON public.images;
DROP POLICY IF EXISTS "images_insert" ON public.images;
DROP POLICY IF EXISTS "images_update" ON public.images;
DROP POLICY IF EXISTS "images_delete" ON public.images;

CREATE POLICY "images_select" ON public.images FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "images_insert" ON public.images FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "images_update" ON public.images FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "images_delete" ON public.images FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- VOUCHERS RLS
DROP POLICY IF EXISTS "vouchers_select" ON public.vouchers;
DROP POLICY IF EXISTS "vouchers_insert" ON public.vouchers;
DROP POLICY IF EXISTS "vouchers_update" ON public.vouchers;
DROP POLICY IF EXISTS "vouchers_delete" ON public.vouchers;

CREATE POLICY "vouchers_select" ON public.vouchers FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "vouchers_insert" ON public.vouchers FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "vouchers_update" ON public.vouchers FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "vouchers_delete" ON public.vouchers FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- NOTIFICATIONS RLS
DROP POLICY IF EXISTS "notifications_select" ON public.notifications;
DROP POLICY IF EXISTS "notifications_insert" ON public.notifications;
DROP POLICY IF EXISTS "notifications_update" ON public.notifications;
DROP POLICY IF EXISTS "notifications_delete" ON public.notifications;

CREATE POLICY "notifications_select" ON public.notifications FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "notifications_insert" ON public.notifications FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "notifications_update" ON public.notifications FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "notifications_delete" ON public.notifications FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- TASKS RLS
DROP POLICY IF EXISTS "tasks_select" ON public.tasks;
DROP POLICY IF EXISTS "tasks_insert" ON public.tasks;
DROP POLICY IF EXISTS "tasks_update" ON public.tasks;
DROP POLICY IF EXISTS "tasks_delete" ON public.tasks;

CREATE POLICY "tasks_select" ON public.tasks FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "tasks_insert" ON public.tasks FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "tasks_update" ON public.tasks FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "tasks_delete" ON public.tasks FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- CALENDARS RLS
DROP POLICY IF EXISTS "calendars_select" ON public.calendars;
DROP POLICY IF EXISTS "calendars_insert" ON public.calendars;
DROP POLICY IF EXISTS "calendars_update" ON public.calendars;
DROP POLICY IF EXISTS "calendars_delete" ON public.calendars;

CREATE POLICY "calendars_select" ON public.calendars FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "calendars_insert" ON public.calendars FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "calendars_update" ON public.calendars FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "calendars_delete" ON public.calendars FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- AUDIT_LOGS RLS
DROP POLICY IF EXISTS "audit_logs_select" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs_insert" ON public.audit_logs;

CREATE POLICY "audit_logs_select" ON public.audit_logs FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "audit_logs_insert" ON public.audit_logs FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));

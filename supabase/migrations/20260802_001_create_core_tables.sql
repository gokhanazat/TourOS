-- ============================================================
-- TourOS Core Tables Migration
-- companies → roles → permissions → users
-- Her tablo tenant_id bazlı RLS ile izole edilir.
-- ============================================================

-- ===================  EXTENSIONS  ===========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. COMPANIES  =========================
CREATE TABLE IF NOT EXISTS public.companies (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    slug        TEXT NOT NULL UNIQUE,        -- firma kısa adı (subdomain vb.)
    logo_url    TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id   UUID NOT NULL,               -- self-ref: kendi id'si ile eşleşir
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID                          -- ilk oluşturan kullanıcı
);

-- tenant_id → self reference
ALTER TABLE public.companies
    ADD CONSTRAINT fk_companies_tenant
    FOREIGN KEY (tenant_id) REFERENCES public.companies(id) ON DELETE CASCADE;

CREATE INDEX idx_companies_tenant ON public.companies(tenant_id);

-- ===================  2. ROLES  =============================
CREATE TABLE IF NOT EXISTS public.roles (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,                -- admin, manager, guide, driver …
    description TEXT,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,

    -- audit
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    UNIQUE (tenant_id, name)                  -- aynı firmada tekrar eden rol adı yok
);

CREATE INDEX idx_roles_tenant ON public.roles(tenant_id);

-- ===================  3. PERMISSIONS  =======================
CREATE TABLE IF NOT EXISTS public.permissions (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_id     UUID NOT NULL REFERENCES public.roles(id) ON DELETE CASCADE,
    resource    TEXT NOT NULL,                -- tours, bookings, vehicles …
    action      TEXT NOT NULL,                -- create, read, update, delete
    is_allowed  BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    UNIQUE (role_id, resource, action)
);

CREATE INDEX idx_permissions_tenant  ON public.permissions(tenant_id);
CREATE INDEX idx_permissions_role    ON public.permissions(role_id);

-- ===================  4. USERS  =============================
CREATE TABLE IF NOT EXISTS public.users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_id     UUID UNIQUE,                  -- supabase auth.users.id
    email       TEXT NOT NULL,
    full_name   TEXT,
    phone       TEXT,
    avatar_url  TEXT,
    role_id     UUID REFERENCES public.roles(id) ON DELETE SET NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant  ON public.users(tenant_id);
CREATE INDEX idx_users_auth    ON public.users(auth_id);
CREATE INDEX idx_users_role    ON public.users(role_id);

-- ============================================================
-- UPDATED_AT TRIGGER (tüm tablolar için tek fonksiyon)
-- ============================================================
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_companies_updated_at
    BEFORE UPDATE ON public.companies
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_roles_updated_at
    BEFORE UPDATE ON public.roles
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_permissions_updated_at
    BEFORE UPDATE ON public.permissions
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- HELPER: Oturum açan kullanıcının tenant_id'sini döner
-- ============================================================
CREATE OR REPLACE FUNCTION public.current_tenant_id()
RETURNS UUID AS $$
    SELECT tenant_id
    FROM public.users
    WHERE auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  COMPANIES  ----------
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;

CREATE POLICY "companies_select"
    ON public.companies FOR SELECT
    USING (id = public.current_tenant_id());

CREATE POLICY "companies_insert"
    ON public.companies FOR INSERT
    WITH CHECK (id = public.current_tenant_id());

CREATE POLICY "companies_update"
    ON public.companies FOR UPDATE
    USING (id = public.current_tenant_id())
    WITH CHECK (id = public.current_tenant_id());

CREATE POLICY "companies_delete"
    ON public.companies FOR DELETE
    USING (id = public.current_tenant_id());

-- ----------  ROLES  ----------
ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "roles_select"
    ON public.roles FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "roles_insert"
    ON public.roles FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "roles_update"
    ON public.roles FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "roles_delete"
    ON public.roles FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  PERMISSIONS  ----------
ALTER TABLE public.permissions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "permissions_select"
    ON public.permissions FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "permissions_insert"
    ON public.permissions FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "permissions_update"
    ON public.permissions FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "permissions_delete"
    ON public.permissions FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  USERS  ----------
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "users_select"
    ON public.users FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "users_insert"
    ON public.users FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "users_update"
    ON public.users FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "users_delete"
    ON public.users FOR DELETE
    USING (tenant_id = public.current_tenant_id());

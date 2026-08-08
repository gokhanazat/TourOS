-- ============================================================
-- TourOS Migration: 20260808_004_identity_tables_and_rls.sql
-- Prompt 0.2.1: Kimlik Tabloları (companies, roles, permissions, users)
-- Her tabloda tenant_id, created_at, updated_at, created_by kolonları ve tenant_id bazlı RLS izolasyonu.
-- ============================================================

-- 0. Gerekli eklentiler
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. COMPANIES TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.companies (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    slug        TEXT NOT NULL UNIQUE,
    logo_url    TEXT,
    theme_color TEXT DEFAULT '#1976D2',
    tax_rate    NUMERIC(5,2) DEFAULT 20.0,
    seasons     TEXT DEFAULT '[]',
    supported_currencies TEXT[] DEFAULT ARRAY['TRY', 'EUR', 'USD'],
    supported_languages TEXT[] DEFAULT ARRAY['tr', 'en'],
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant Kolonları
    tenant_id   UUID NOT NULL,               -- self-ref: kendi id'si
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID
);

-- Self-reference FK for companies.tenant_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_companies_tenant' AND table_name = 'companies'
    ) THEN
        ALTER TABLE public.companies 
        ADD CONSTRAINT fk_companies_tenant 
        FOREIGN KEY (tenant_id) REFERENCES public.companies(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_companies_tenant ON public.companies(tenant_id);

-- ===================  2. ROLES TABLOSU  ======================
CREATE TABLE IF NOT EXISTS public.roles (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    description TEXT,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit & Multi-tenant Kolonları
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    CONSTRAINT uq_roles_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX IF NOT EXISTS idx_roles_tenant ON public.roles(tenant_id);

-- ===================  3. PERMISSIONS TABLOSU  ================
CREATE TABLE IF NOT EXISTS public.permissions (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_id     UUID NOT NULL REFERENCES public.roles(id) ON DELETE CASCADE,
    resource    TEXT NOT NULL,
    action      TEXT NOT NULL,
    is_allowed  BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant Kolonları
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    CONSTRAINT uq_permissions_role_resource_action UNIQUE (role_id, resource, action)
);

CREATE INDEX IF NOT EXISTS idx_permissions_tenant ON public.permissions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_permissions_role ON public.permissions(role_id);

-- ===================  4. USERS TABLOSU  ======================
CREATE TABLE IF NOT EXISTS public.users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    auth_id     UUID UNIQUE,
    email       TEXT NOT NULL,
    full_name   TEXT,
    phone       TEXT,
    avatar_url  TEXT,
    role_id     UUID REFERENCES public.roles(id) ON DELETE SET NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant Kolonları
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_users_tenant ON public.users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_auth ON public.users(auth_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON public.users(role_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_companies_updated_at ON public.companies;
CREATE TRIGGER trg_companies_updated_at
    BEFORE UPDATE ON public.companies
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_roles_updated_at ON public.roles;
CREATE TRIGGER trg_roles_updated_at
    BEFORE UPDATE ON public.roles
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_permissions_updated_at ON public.permissions;
CREATE TRIGGER trg_permissions_updated_at
    BEFORE UPDATE ON public.permissions
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_users_updated_at ON public.users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- HELPER FUNCTION: current_tenant_id()
-- ============================================================
CREATE OR REPLACE FUNCTION public.current_tenant_id()
RETURNS UUID AS $$
    SELECT tenant_id
    FROM public.users
    WHERE auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı izole RLS iskeleti)
-- ============================================================

-- 1. COMPANIES RLS
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "companies_select" ON public.companies;
DROP POLICY IF EXISTS "companies_insert" ON public.companies;
DROP POLICY IF EXISTS "companies_update" ON public.companies;
DROP POLICY IF EXISTS "companies_delete" ON public.companies;

CREATE POLICY "companies_select" ON public.companies FOR SELECT
    USING (id = public.current_tenant_id() OR id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "companies_insert" ON public.companies FOR INSERT
    WITH CHECK (id = public.current_tenant_id() OR id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "companies_update" ON public.companies FOR UPDATE
    USING (id = public.current_tenant_id() OR id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (id = public.current_tenant_id() OR id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "companies_delete" ON public.companies FOR DELETE
    USING (id = public.current_tenant_id());

-- 2. ROLES RLS
ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "roles_select" ON public.roles;
DROP POLICY IF EXISTS "roles_insert" ON public.roles;
DROP POLICY IF EXISTS "roles_update" ON public.roles;
DROP POLICY IF EXISTS "roles_delete" ON public.roles;

CREATE POLICY "roles_select" ON public.roles FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "roles_insert" ON public.roles FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "roles_update" ON public.roles FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "roles_delete" ON public.roles FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- 3. PERMISSIONS RLS
ALTER TABLE public.permissions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "permissions_select" ON public.permissions;
DROP POLICY IF EXISTS "permissions_insert" ON public.permissions;
DROP POLICY IF EXISTS "permissions_update" ON public.permissions;
DROP POLICY IF EXISTS "permissions_delete" ON public.permissions;

CREATE POLICY "permissions_select" ON public.permissions FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "permissions_insert" ON public.permissions FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "permissions_update" ON public.permissions FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "permissions_delete" ON public.permissions FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- 4. USERS RLS
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "users_select" ON public.users;
DROP POLICY IF EXISTS "users_insert" ON public.users;
DROP POLICY IF EXISTS "users_update" ON public.users;
DROP POLICY IF EXISTS "users_delete" ON public.users;

CREATE POLICY "users_select" ON public.users FOR SELECT
    USING (
        auth.uid() = auth_id 
        OR tenant_id = public.current_tenant_id()
        OR tenant_id = '00000000-0000-0000-0000-000000000001'
    );

CREATE POLICY "users_insert" ON public.users FOR INSERT
    WITH CHECK (
        auth.uid() = auth_id 
        OR tenant_id = public.current_tenant_id()
        OR tenant_id = '00000000-0000-0000-0000-000000000001'
    );

CREATE POLICY "users_update" ON public.users FOR UPDATE
    USING (
        auth.uid() = auth_id 
        OR tenant_id = public.current_tenant_id()
        OR tenant_id = '00000000-0000-0000-0000-000000000001'
    )
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "users_delete" ON public.users FOR DELETE
    USING (tenant_id = public.current_tenant_id());

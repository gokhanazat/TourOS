-- ============================================================
-- TourOS Migration: 20260808_015_create_agency_operator_connections_table.sql
-- Prompt 4.6.3: agency_operator_connections Veri Modeli
-- Acente - Operatör pazaryeri bağlantı tablosu ve RLS politikaları.
-- E-posta alanı YOK — bildirimler tamamen sistem içi.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. AGENCY_OPERATOR_CONNECTIONS TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.agency_operator_connections (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agency_id               UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    operator_company_id     UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    price_adjustment_type   TEXT NOT NULL DEFAULT 'percentage', -- 'percentage' veya 'fixed'
    price_adjustment_value  NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    commission_rate         NUMERIC(5,2) NOT NULL DEFAULT 10.00,
    status                  TEXT NOT NULL DEFAULT 'ACTIVE', -- 'PENDING', 'ACTIVE', 'PAUSED', 'TERMINATED'
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_agency_operator_connection UNIQUE (agency_id, operator_company_id),
    CONSTRAINT chk_price_adjustment_type CHECK (price_adjustment_type IN ('percentage', 'fixed')),
    CONSTRAINT chk_connection_status CHECK (status IN ('PENDING', 'ACTIVE', 'PAUSED', 'TERMINATED'))
);

CREATE INDEX IF NOT EXISTS idx_agency_operator_conn_agency ON public.agency_operator_connections(agency_id);
CREATE INDEX IF NOT EXISTS idx_agency_operator_conn_operator ON public.agency_operator_connections(operator_company_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DO $$ BEGIN
    DROP TRIGGER IF EXISTS trg_agency_operator_conn_updated_at ON public.agency_operator_connections;
    CREATE TRIGGER trg_agency_operator_conn_updated_at 
        BEFORE UPDATE ON public.agency_operator_connections 
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
END $$;

-- ============================================================
-- ROW LEVEL SECURITY (Acente & Operatör İzolasyonu)
-- Acente sadece kendi bağlantılarını, Operatör kendisiyle ilgili olanları görür.
-- ============================================================
ALTER TABLE public.agency_operator_connections ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "agency_operator_conn_select" ON public.agency_operator_connections;
DROP POLICY IF EXISTS "agency_operator_conn_insert" ON public.agency_operator_connections;
DROP POLICY IF EXISTS "agency_operator_conn_update" ON public.agency_operator_connections;
DROP POLICY IF EXISTS "agency_operator_conn_delete" ON public.agency_operator_connections;

CREATE POLICY "agency_operator_conn_select" ON public.agency_operator_connections FOR SELECT
    USING (
        agency_id = public.current_tenant_id()
        OR operator_company_id = public.current_tenant_id()
        OR public.is_valid_tenant(agency_id)
    );

CREATE POLICY "agency_operator_conn_insert" ON public.agency_operator_connections FOR INSERT
    WITH CHECK (
        agency_id = public.current_tenant_id()
        OR public.is_valid_tenant(agency_id)
    );

CREATE POLICY "agency_operator_conn_update" ON public.agency_operator_connections FOR UPDATE
    USING (
        agency_id = public.current_tenant_id()
        OR operator_company_id = public.current_tenant_id()
    )
    WITH CHECK (
        agency_id = public.current_tenant_id()
        OR operator_company_id = public.current_tenant_id()
    );

CREATE POLICY "agency_operator_conn_delete" ON public.agency_operator_connections FOR DELETE
    USING (
        agency_id = public.current_tenant_id()
        OR operator_company_id = public.current_tenant_id()
    );

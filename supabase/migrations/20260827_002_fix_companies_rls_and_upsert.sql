-- ============================================================================
-- TourOS Migration: 20260827_002_fix_companies_rls_and_upsert.sql
-- DESCRIPTION: Fix RLS policies on companies table for agency tenant isolation and upsert
-- ============================================================================

ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "companies_insert" ON public.companies;
CREATE POLICY "companies_insert" ON public.companies
    FOR INSERT
    TO authenticated
    WITH CHECK (
        is_super_admin() 
        OR id = current_tenant_id()
        OR tenant_id = current_tenant_id()
    );

DROP POLICY IF EXISTS "companies_update" ON public.companies;
CREATE POLICY "companies_update" ON public.companies
    FOR UPDATE
    TO authenticated
    USING (
        is_super_admin() 
        OR id = current_tenant_id()
        OR tenant_id = current_tenant_id()
    )
    WITH CHECK (
        is_super_admin() 
        OR id = current_tenant_id()
        OR tenant_id = current_tenant_id()
    );

DROP POLICY IF EXISTS "companies_select" ON public.companies;
CREATE POLICY "companies_select" ON public.companies
    FOR SELECT
    TO authenticated
    USING (
        is_super_admin() 
        OR id = current_tenant_id()
        OR tenant_id = current_tenant_id()
        OR company_type = 'operator'
    );

-- ============================================================================
-- TourOS Migration: 20260821_003_strict_rls_data_isolation_audit.sql
-- DESCRIPTION: Sıkı Multi-Tenant Veri İzolasyonu ve PostgreSQL RLS Güvenlik Kalkanı
-- HEDEF: Hiçbir acente başka bir acentenin müşterisini, rezervasyonunu, cirosunu veya ekstresini göremez.
-- ============================================================================

-- 1. Oturum Açan Kullanıcının Şirket (tenant_id) ve Yetki Bilgilerini Döndüren Güvenli Fonksiyonlar
CREATE OR REPLACE FUNCTION public.current_company_id()
RETURNS UUID AS $$
    SELECT tenant_id FROM public.users 
    WHERE (auth_id = auth.uid() OR id = auth.uid()) 
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.is_system_admin()
RETURNS BOOLEAN AS $$
    SELECT COALESCE(
        EXISTS (
            SELECT 1 FROM public.users u 
            LEFT JOIN public.roles r ON r.id = u.role_id 
            WHERE (u.auth_id = auth.uid() OR u.id = auth.uid()) 
              AND (r.name = 'SYSTEM_ADMIN' OR u.email = 'gkhnazat@gmail.com')
        ),
        false
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- 2. Şirketler Tablosu (companies) İzolasyonu
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.companies FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "companies_tenant_isolation_select" ON public.companies;
CREATE POLICY "companies_tenant_isolation_select" ON public.companies
    FOR SELECT
    USING (
        id = public.current_company_id() 
        OR public.is_system_admin()
        OR company_type = 'operator'
    );

DROP POLICY IF EXISTS "companies_tenant_isolation_update" ON public.companies;
CREATE POLICY "companies_tenant_isolation_update" ON public.companies
    FOR UPDATE
    USING (
        id = public.current_company_id() 
        OR public.is_system_admin()
    );

-- 3. Acente Cari Hareketleri (agency_ledger_transactions) İzolasyonu
ALTER TABLE public.agency_ledger_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agency_ledger_transactions FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "agency_ledger_select_policy" ON public.agency_ledger_transactions;
CREATE POLICY "agency_ledger_select_policy" ON public.agency_ledger_transactions
    FOR SELECT
    USING (
        company_id = public.current_company_id() 
        OR public.is_system_admin()
    );

DROP POLICY IF EXISTS "agency_ledger_admin_only_modify" ON public.agency_ledger_transactions;
CREATE POLICY "agency_ledger_admin_only_modify" ON public.agency_ledger_transactions
    FOR ALL
    USING (public.is_system_admin());

-- 4. Rezervasyonlar & Yolcular (bookings) İzolasyonu
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookings FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "bookings_tenant_isolation" ON public.bookings;
CREATE POLICY "bookings_tenant_isolation" ON public.bookings
    FOR ALL
    USING (
        tenant_id = public.current_company_id()
        OR public.is_system_admin()
    );

-- 5. Müşteriler & CRM (customers) İzolasyonu
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "customers_tenant_isolation" ON public.customers;
CREATE POLICY "customers_tenant_isolation" ON public.customers
    FOR ALL
    USING (
        tenant_id = public.current_company_id()
        OR public.is_system_admin()
    );

-- 6. Muhasebe & Finans (invoices, payments, expenses) İzolasyonu
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.invoices FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "invoices_tenant_isolation" ON public.invoices;
CREATE POLICY "invoices_tenant_isolation" ON public.invoices
    FOR ALL
    USING (
        tenant_id = public.current_company_id()
        OR public.is_system_admin()
    );

ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "payments_tenant_isolation" ON public.payments;
CREATE POLICY "payments_tenant_isolation" ON public.payments
    FOR ALL
    USING (
        tenant_id = public.current_company_id()
        OR public.is_system_admin()
    );

ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "expenses_tenant_isolation" ON public.expenses;
CREATE POLICY "expenses_tenant_isolation" ON public.expenses
    FOR ALL
    USING (
        tenant_id = public.current_company_id()
        OR public.is_system_admin()
    );

-- 7. SaaS Sistem Yapılandırması (saas_system_config) İzolasyonu
ALTER TABLE public.saas_system_config ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "saas_config_select_all" ON public.saas_system_config;
CREATE POLICY "saas_config_select_all" ON public.saas_system_config
    FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "saas_config_admin_only_modify" ON public.saas_system_config;
CREATE POLICY "saas_config_admin_only_modify" ON public.saas_system_config
    FOR ALL
    USING (public.is_system_admin());

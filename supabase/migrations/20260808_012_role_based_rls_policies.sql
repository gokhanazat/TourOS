-- ============================================================
-- TourOS Migration: 20260808_012_role_based_rls_policies.sql
-- Prompt 0.2.9: Rol Bazlı RLS Politikaları
-- Rehber, Acente, Müşteri ve Operatör rolleri için detaylı erişim kontrolü.
-- ============================================================

-- ============================================================
-- HELPER FUNCTIONS FOR ROLE-BASED ACCESS CONTROL
-- ============================================================

-- 1. Kullanıcının Rehber ID'sini döner
CREATE OR REPLACE FUNCTION public.current_user_guide_id()
RETURNS UUID AS $$
    SELECT g.id
    FROM public.guides g
    JOIN public.users u ON u.phone = g.phone OR u.email = g.phone
    WHERE u.auth_id = auth.uid() AND g.tenant_id = public.current_tenant_id()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- 2. Kullanıcının Acente ID'sini döner
CREATE OR REPLACE FUNCTION public.current_user_agency_id()
RETURNS UUID AS $$
    SELECT a.id
    FROM public.agencies a
    JOIN public.users u ON u.email = a.email OR u.phone = a.phone
    WHERE u.auth_id = auth.uid() AND a.tenant_id = public.current_tenant_id()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- 3. Kullanıcının Müşteri ID'sini döner
CREATE OR REPLACE FUNCTION public.current_user_customer_id()
RETURNS UUID AS $$
    SELECT c.id
    FROM public.customers c
    JOIN public.users u ON u.email = c.email
    WHERE u.auth_id = auth.uid() AND c.tenant_id = public.current_tenant_id()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- ============================================================
-- REHBER ROLÜ POLİTİKALARI (GUIDE)
-- Rehberler sadece atandıkları transfer ve operasyonları görür.
-- ============================================================
DROP POLICY IF EXISTS "transfers_guide_select" ON public.transfers;
CREATE POLICY "transfers_guide_select" ON public.transfers FOR SELECT
    USING (
        public.is_valid_tenant(tenant_id)
        AND (
            public.is_admin_or_operator()
            OR guide_id = public.current_user_guide_id()
        )
    );

-- ============================================================
-- ACENTE ROLÜ POLİTİKALARI (AGENCY)
-- Acenteler sadece kendi oluşturdukları rezervasyonları ve komisyonları görür.
-- ============================================================
DROP POLICY IF EXISTS "bookings_agency_select" ON public.bookings;
CREATE POLICY "bookings_agency_select" ON public.bookings FOR SELECT
    USING (
        public.is_valid_tenant(tenant_id)
        AND (
            public.is_admin_or_operator()
            OR agency_id = public.current_user_agency_id()
        )
    );

DROP POLICY IF EXISTS "commissions_agency_select" ON public.commissions;
CREATE POLICY "commissions_agency_select" ON public.commissions FOR SELECT
    USING (
        public.is_valid_tenant(tenant_id)
        AND (
            public.is_admin_or_operator()
            OR booking_id IN (
                SELECT id FROM public.bookings WHERE agency_id = public.current_user_agency_id()
            )
        )
    );

-- ============================================================
-- MÜŞTERİ ROLÜ POLİTİKALARI (CUSTOMER)
-- Müşteriler sadece kendi rezervasyonlarını ve sadakat puanlarını görür.
-- ============================================================
DROP POLICY IF EXISTS "bookings_customer_select" ON public.bookings;
CREATE POLICY "bookings_customer_select" ON public.bookings FOR SELECT
    USING (
        public.is_valid_tenant(tenant_id)
        AND (
            public.is_admin_or_operator()
            OR customer_id = public.current_user_customer_id()
        )
    );

DROP POLICY IF EXISTS "loyalty_points_customer_select" ON public.loyalty_points;
CREATE POLICY "loyalty_points_customer_select" ON public.loyalty_points FOR SELECT
    USING (
        public.is_valid_tenant(tenant_id)
        AND (
            public.is_admin_or_operator()
            OR customer_id = public.current_user_customer_id()
        )
    );

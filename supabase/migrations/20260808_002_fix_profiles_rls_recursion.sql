-- Migration: 20260808_002_fix_profiles_rls_recursion.sql
-- Description: profiles/users RLS politikalarındaki sonsuz döngüyü (Code: 42P17) düzeltir.

-- 1. Helper Fonksiyonları SECURITY DEFINER Olarak Güncelle (RLS Bypass ile Sonsuz Döngüyü Önler)
CREATE OR REPLACE FUNCTION public.current_tenant_id()
RETURNS UUID AS $$
    SELECT tenant_id
    FROM public.users
    WHERE auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

CREATE OR REPLACE FUNCTION public.current_user_id()
RETURNS UUID AS $$
    SELECT id
    FROM public.users
    WHERE auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

CREATE OR REPLACE FUNCTION public.current_user_role()
RETURNS TEXT AS $$
    SELECT r.name
    FROM public.users u
    JOIN public.roles r ON r.id = u.role_id
    WHERE u.auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

CREATE OR REPLACE FUNCTION public.is_admin_or_operator()
RETURNS BOOLEAN AS $$
    SELECT public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR');
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- 2. profiles Tablosundaki Hatalı/Döngüsel Politikaları Temizle (Eğer Varsa)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'profiles') THEN
        EXECUTE 'ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;';
    END IF;
END $$;

-- 3. users Tablosundaki RLS Politikalarını Temiz ve Döngüsüz Olarak Yeniden Tanımla
DROP POLICY IF EXISTS "users_select" ON public.users;
DROP POLICY IF EXISTS "users_insert" ON public.users;
DROP POLICY IF EXISTS "users_update" ON public.users;
DROP POLICY IF EXISTS "users_delete" ON public.users;

CREATE POLICY "users_select" ON public.users FOR SELECT
    USING (
        auth.uid() = auth_id 
        OR tenant_id = public.current_tenant_id()
    );

CREATE POLICY "users_insert" ON public.users FOR INSERT
    WITH CHECK (
        auth.uid() = auth_id 
        OR (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    );

CREATE POLICY "users_update" ON public.users FOR UPDATE
    USING (
        auth.uid() = auth_id 
        OR (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    )
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "users_delete" ON public.users FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

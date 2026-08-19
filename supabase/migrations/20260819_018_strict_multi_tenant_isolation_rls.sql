-- ============================================================================
-- KESİN VE TAM SAAS ÇOK KİRACILI (MULTI-TENANT) İZOLASYON POLİTİKALARI
-- Dosya: supabase/migrations/20260819_018_strict_multi_tenant_isolation_rls.sql
-- ============================================================================

-- 1. Helper: Süper Admin Kontrolü
CREATE OR REPLACE FUNCTION public.is_super_admin()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.users 
        WHERE auth_id = auth.uid() 
          AND (email = 'gkhnazat@gmail.com' OR tenant_id = '00000000-0000-0000-0000-000000000001'::uuid)
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- 2. Helper: Aktif Kullanıcının Tenant ID'si
CREATE OR REPLACE FUNCTION public.current_tenant_id()
RETURNS UUID AS $$
    SELECT tenant_id
    FROM public.users
    WHERE auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- 3. COMPANIES İZOLASYONU
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow all access to companies" ON public.companies;
DROP POLICY IF EXISTS "companies_allow_all" ON public.companies;
DROP POLICY IF EXISTS "companies_select" ON public.companies;
DROP POLICY IF EXISTS "companies_insert" ON public.companies;
DROP POLICY IF EXISTS "companies_update" ON public.companies;
DROP POLICY IF EXISTS "companies_delete" ON public.companies;

CREATE POLICY "companies_select" ON public.companies FOR SELECT
    USING (public.is_super_admin() OR id = public.current_tenant_id());

CREATE POLICY "companies_insert" ON public.companies FOR INSERT
    WITH CHECK (public.is_super_admin() OR id = public.current_tenant_id());

CREATE POLICY "companies_update" ON public.companies FOR UPDATE
    USING (public.is_super_admin() OR id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR id = public.current_tenant_id());

CREATE POLICY "companies_delete" ON public.companies FOR DELETE
    USING (public.is_super_admin() OR id = public.current_tenant_id());

-- 4. TOURS İZOLASYONU
ALTER TABLE public.tours ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "tours_select" ON public.tours;
DROP POLICY IF EXISTS "tours_insert" ON public.tours;
DROP POLICY IF EXISTS "tours_update" ON public.tours;
DROP POLICY IF EXISTS "tours_delete" ON public.tours;

CREATE POLICY "tours_select" ON public.tours FOR SELECT
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "tours_insert" ON public.tours FOR INSERT
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "tours_update" ON public.tours FOR UPDATE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "tours_delete" ON public.tours FOR DELETE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

-- 5. DEPARTURES İZOLASYONU
ALTER TABLE public.departures ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "departures_all_policy" ON public.departures;
DROP POLICY IF EXISTS "departures_select" ON public.departures;
DROP POLICY IF EXISTS "departures_insert" ON public.departures;
DROP POLICY IF EXISTS "departures_update" ON public.departures;
DROP POLICY IF EXISTS "departures_delete" ON public.departures;

CREATE POLICY "departures_select" ON public.departures FOR SELECT
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "departures_insert" ON public.departures FOR INSERT
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "departures_update" ON public.departures FOR UPDATE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "departures_delete" ON public.departures FOR DELETE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

-- 6. BOOKINGS İZOLASYONU
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "b2b_agency_bookings_isolation_policy" ON public.bookings;
DROP POLICY IF EXISTS "bookings_agency_select" ON public.bookings;
DROP POLICY IF EXISTS "bookings_customer_select" ON public.bookings;
DROP POLICY IF EXISTS "bookings_select" ON public.bookings;
DROP POLICY IF EXISTS "bookings_insert" ON public.bookings;
DROP POLICY IF EXISTS "bookings_update" ON public.bookings;
DROP POLICY IF EXISTS "bookings_delete" ON public.bookings;

CREATE POLICY "bookings_select" ON public.bookings FOR SELECT
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "bookings_insert" ON public.bookings FOR INSERT
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "bookings_update" ON public.bookings FOR UPDATE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "bookings_delete" ON public.bookings FOR DELETE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

-- 7. HOTELS VE ROOM_TYPES İZOLASYONU
ALTER TABLE public.hotels ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Hotels tenant isolation" ON public.hotels;
DROP POLICY IF EXISTS "hotels_select" ON public.hotels;
DROP POLICY IF EXISTS "hotels_insert" ON public.hotels;
DROP POLICY IF EXISTS "hotels_update" ON public.hotels;
DROP POLICY IF EXISTS "hotels_delete" ON public.hotels;

CREATE POLICY "hotels_select" ON public.hotels FOR SELECT
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "hotels_insert" ON public.hotels FOR INSERT
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "hotels_update" ON public.hotels FOR UPDATE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "hotels_delete" ON public.hotels FOR DELETE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

ALTER TABLE public.room_types ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Room types tenant isolation" ON public.room_types;
DROP POLICY IF EXISTS "room_types_allow_all" ON public.room_types;
DROP POLICY IF EXISTS "room_types_select" ON public.room_types;
DROP POLICY IF EXISTS "room_types_insert" ON public.room_types;
DROP POLICY IF EXISTS "room_types_update" ON public.room_types;
DROP POLICY IF EXISTS "room_types_delete" ON public.room_types;

CREATE POLICY "room_types_select" ON public.room_types FOR SELECT
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "room_types_insert" ON public.room_types FOR INSERT
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "room_types_update" ON public.room_types FOR UPDATE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR tenant_id = public.current_tenant_id());

CREATE POLICY "room_types_delete" ON public.room_types FOR DELETE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

-- 8. USERS İZOLASYONU
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "users_select" ON public.users;
DROP POLICY IF EXISTS "users_insert" ON public.users;
DROP POLICY IF EXISTS "users_update" ON public.users;
DROP POLICY IF EXISTS "users_delete" ON public.users;

CREATE POLICY "users_select" ON public.users FOR SELECT
    USING (public.is_super_admin() OR auth_id = auth.uid() OR tenant_id = public.current_tenant_id());

CREATE POLICY "users_insert" ON public.users FOR INSERT
    WITH CHECK (public.is_super_admin() OR auth_id = auth.uid() OR tenant_id = public.current_tenant_id());

CREATE POLICY "users_update" ON public.users FOR UPDATE
    USING (public.is_super_admin() OR auth_id = auth.uid() OR tenant_id = public.current_tenant_id())
    WITH CHECK (public.is_super_admin() OR auth_id = auth.uid() OR tenant_id = public.current_tenant_id());

CREATE POLICY "users_delete" ON public.users FOR DELETE
    USING (public.is_super_admin() OR tenant_id = public.current_tenant_id());

-- 9. DİĞER TÜM MULTI-TENANT TABLOLARIN SIZINTISIZ İZOLASYONU (Sadece Base Table)
DO $$
DECLARE
    tbl text;
BEGIN
    FOR tbl IN 
        SELECT t.table_name 
        FROM information_schema.tables t
        JOIN information_schema.columns c ON t.table_name = c.table_name AND t.table_schema = c.table_schema
        WHERE t.table_schema = 'public' 
          AND t.table_type = 'BASE TABLE'
          AND c.column_name = 'tenant_id' 
          AND t.table_name NOT IN ('companies', 'tours', 'departures', 'bookings', 'hotels', 'room_types', 'users')
        GROUP BY t.table_name
    LOOP
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY;', tbl);
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.%I;', tbl || '_select', tbl);
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.%I;', tbl || '_insert', tbl);
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.%I;', tbl || '_update', tbl);
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.%I;', tbl || '_delete', tbl);
        
        EXECUTE format('CREATE POLICY %I ON public.%I FOR SELECT USING (public.is_super_admin() OR (tenant_id)::text = (public.current_tenant_id())::text);', tbl || '_select', tbl);
        EXECUTE format('CREATE POLICY %I ON public.%I FOR INSERT WITH CHECK (public.is_super_admin() OR (tenant_id)::text = (public.current_tenant_id())::text);', tbl || '_insert', tbl);
        EXECUTE format('CREATE POLICY %I ON public.%I FOR UPDATE USING (public.is_super_admin() OR (tenant_id)::text = (public.current_tenant_id())::text) WITH CHECK (public.is_super_admin() OR (tenant_id)::text = (public.current_tenant_id())::text);', tbl || '_update', tbl);
        EXECUTE format('CREATE POLICY %I ON public.%I FOR DELETE USING (public.is_super_admin() OR (tenant_id)::text = (public.current_tenant_id())::text);', tbl || '_delete', tbl);
    END LOOP;
END $$;

NOTIFY pgrst, 'reload schema';

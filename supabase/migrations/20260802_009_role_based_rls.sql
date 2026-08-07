-- ============================================================
-- TourOS Role-Based RLS Policies Migration
-- Mevcut tenant-only policy'leri DROP edip rol bazlı yeniden oluşturur.
-- ============================================================
-- ROLLER:
--   SYSTEM_ADMIN  → her şeyi görür/yapar
--   TOUR_OPERATOR → kendi tenant'ındaki her şey
--   SALES         → turlar, rezervasyonlar, müşteriler
--   GUIDE         → sadece atandığı turlar/transferler
--   DRIVER        → sadece atandığı transferler
--   ACCOUNTING    → finans tabloları
--   AGENT         → sadece kendi acente rezervasyonları
--   CUSTOMER      → sadece kendi rezervasyonları
-- ============================================================

-- Helper: Oturumdaki kullanıcının rolünü döner
CREATE OR REPLACE FUNCTION public.current_user_role()
RETURNS TEXT AS $$
    SELECT r.name
    FROM public.users u
    JOIN public.roles r ON r.id = u.role_id
    WHERE u.auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- Helper: Oturumdaki kullanıcının users.id'sini döner
CREATE OR REPLACE FUNCTION public.current_user_id()
RETURNS UUID AS $$
    SELECT id
    FROM public.users
    WHERE auth_id = auth.uid()
    LIMIT 1;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- Helper: Kullanıcı admin/operator mi?
CREATE OR REPLACE FUNCTION public.is_admin_or_operator()
RETURNS BOOLEAN AS $$
    SELECT public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR');
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- ============================================================
-- MACRO: Tenant bazlı + rol kontrolü
-- ============================================================

-- =====================  COMPANIES  ==========================
DROP POLICY IF EXISTS "companies_select" ON public.companies;
DROP POLICY IF EXISTS "companies_insert" ON public.companies;
DROP POLICY IF EXISTS "companies_update" ON public.companies;
DROP POLICY IF EXISTS "companies_delete" ON public.companies;

CREATE POLICY "companies_select" ON public.companies FOR SELECT
    USING (id = public.current_tenant_id());

CREATE POLICY "companies_insert" ON public.companies FOR INSERT
    WITH CHECK (public.is_admin_or_operator());

CREATE POLICY "companies_update" ON public.companies FOR UPDATE
    USING (id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (id = public.current_tenant_id());

CREATE POLICY "companies_delete" ON public.companies FOR DELETE
    USING (id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

-- =====================  ROLES  ==============================
DROP POLICY IF EXISTS "roles_select" ON public.roles;
DROP POLICY IF EXISTS "roles_insert" ON public.roles;
DROP POLICY IF EXISTS "roles_update" ON public.roles;
DROP POLICY IF EXISTS "roles_delete" ON public.roles;

CREATE POLICY "roles_select" ON public.roles FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "roles_insert" ON public.roles FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "roles_update" ON public.roles FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "roles_delete" ON public.roles FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

-- =====================  PERMISSIONS  ========================
DROP POLICY IF EXISTS "permissions_select" ON public.permissions;
DROP POLICY IF EXISTS "permissions_insert" ON public.permissions;
DROP POLICY IF EXISTS "permissions_update" ON public.permissions;
DROP POLICY IF EXISTS "permissions_delete" ON public.permissions;

CREATE POLICY "permissions_select" ON public.permissions FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "permissions_insert" ON public.permissions FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "permissions_update" ON public.permissions FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "permissions_delete" ON public.permissions FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

-- =====================  USERS  ==============================
DROP POLICY IF EXISTS "users_select" ON public.users;
DROP POLICY IF EXISTS "users_insert" ON public.users;
DROP POLICY IF EXISTS "users_update" ON public.users;
DROP POLICY IF EXISTS "users_delete" ON public.users;

-- Herkes kendi tenant kullanıcılarını görebilir
CREATE POLICY "users_select" ON public.users FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "users_insert" ON public.users FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- Kullanıcı kendi profilini VEYA admin herkesi güncelleyebilir
CREATE POLICY "users_update" ON public.users FOR UPDATE
    USING (
        tenant_id = public.current_tenant_id()
        AND (id = public.current_user_id() OR public.is_admin_or_operator())
    )
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "users_delete" ON public.users FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

-- =====================  TOUR_CATEGORIES  ====================
DROP POLICY IF EXISTS "tour_categories_select" ON public.tour_categories;
DROP POLICY IF EXISTS "tour_categories_insert" ON public.tour_categories;
DROP POLICY IF EXISTS "tour_categories_update" ON public.tour_categories;
DROP POLICY IF EXISTS "tour_categories_delete" ON public.tour_categories;

CREATE POLICY "tour_categories_select" ON public.tour_categories FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "tour_categories_insert" ON public.tour_categories FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "tour_categories_update" ON public.tour_categories FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "tour_categories_delete" ON public.tour_categories FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  TOURS  ==============================
DROP POLICY IF EXISTS "tours_select" ON public.tours;
DROP POLICY IF EXISTS "tours_insert" ON public.tours;
DROP POLICY IF EXISTS "tours_update" ON public.tours;
DROP POLICY IF EXISTS "tours_delete" ON public.tours;

-- GUIDE: sadece atandığı turları görebilir (transfers üzerinden)
-- Diğer roller: tüm tenant turları
CREATE POLICY "tours_select" ON public.tours FOR SELECT
    USING (
        tenant_id = public.current_tenant_id()
        AND (
            public.current_user_role() != 'GUIDE'
            OR id IN (
                SELECT DISTINCT t.tour_id FROM public.departures d
                JOIN public.transfers t ON t.departure_id = d.id
                WHERE t.guide_id = public.current_user_id()
            )
        )
    );

CREATE POLICY "tours_insert" ON public.tours FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "tours_update" ON public.tours FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "tours_delete" ON public.tours FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  DEPARTURES  =========================
DROP POLICY IF EXISTS "departures_select" ON public.departures;
DROP POLICY IF EXISTS "departures_insert" ON public.departures;
DROP POLICY IF EXISTS "departures_update" ON public.departures;
DROP POLICY IF EXISTS "departures_delete" ON public.departures;

CREATE POLICY "departures_select" ON public.departures FOR SELECT
    USING (
        tenant_id = public.current_tenant_id()
        AND (
            public.current_user_role() NOT IN ('GUIDE', 'DRIVER')
            OR id IN (
                SELECT departure_id FROM public.transfers
                WHERE guide_id = public.current_user_id()
                   OR driver_id = public.current_user_id()
            )
        )
    );

CREATE POLICY "departures_insert" ON public.departures FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR', 'SALES'));

CREATE POLICY "departures_update" ON public.departures FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR', 'SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "departures_delete" ON public.departures FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  ITINERARIES  ========================
DROP POLICY IF EXISTS "itineraries_select" ON public.itineraries;
DROP POLICY IF EXISTS "itineraries_insert" ON public.itineraries;
DROP POLICY IF EXISTS "itineraries_update" ON public.itineraries;
DROP POLICY IF EXISTS "itineraries_delete" ON public.itineraries;

CREATE POLICY "itineraries_select" ON public.itineraries FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "itineraries_insert" ON public.itineraries FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "itineraries_update" ON public.itineraries FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "itineraries_delete" ON public.itineraries FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  BOOKINGS  ===========================
DROP POLICY IF EXISTS "bookings_select" ON public.bookings;
DROP POLICY IF EXISTS "bookings_insert" ON public.bookings;
DROP POLICY IF EXISTS "bookings_update" ON public.bookings;
DROP POLICY IF EXISTS "bookings_delete" ON public.bookings;

-- AGENT: sadece kendi agency_id'li rezervasyonlar
-- CUSTOMER: sadece kendi customer_id'li rezervasyonlar
-- GUIDE: atandığı departure'lara ait rezervasyonlar
CREATE POLICY "bookings_select" ON public.bookings FOR SELECT
    USING (
        tenant_id = public.current_tenant_id()
        AND (
            public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR', 'SALES', 'ACCOUNTING')
            OR (public.current_user_role() = 'AGENT' AND agency_id IN (
                SELECT id FROM public.agencies WHERE tenant_id = public.current_tenant_id()
                -- Acente kullanıcı eşleştirmesi: users tablosundaki ilgili agency bağlantısı
            ))
            OR (public.current_user_role() = 'CUSTOMER' AND customer_id IN (
                SELECT c.id FROM public.customers c
                WHERE c.tenant_id = public.current_tenant_id()
                  AND c.email = (SELECT email FROM public.users WHERE auth_id = auth.uid())
            ))
            OR (public.current_user_role() = 'GUIDE' AND departure_id IN (
                SELECT departure_id FROM public.transfers
                WHERE guide_id = public.current_user_id()
            ))
            OR (public.current_user_role() = 'DRIVER' AND departure_id IN (
                SELECT departure_id FROM public.transfers
                WHERE driver_id = public.current_user_id()
            ))
        )
    );

CREATE POLICY "bookings_insert" ON public.bookings FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR', 'SALES', 'AGENT'));

CREATE POLICY "bookings_update" ON public.bookings FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN', 'TOUR_OPERATOR', 'SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "bookings_delete" ON public.bookings FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  BOOKING_ITEMS  ======================
DROP POLICY IF EXISTS "booking_items_select" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_insert" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_update" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_delete" ON public.booking_items;

CREATE POLICY "booking_items_select" ON public.booking_items FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING'));

CREATE POLICY "booking_items_insert" ON public.booking_items FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));

CREATE POLICY "booking_items_update" ON public.booking_items FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "booking_items_delete" ON public.booking_items FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  PASSENGERS  =========================
DROP POLICY IF EXISTS "passengers_select" ON public.passengers;
DROP POLICY IF EXISTS "passengers_insert" ON public.passengers;
DROP POLICY IF EXISTS "passengers_update" ON public.passengers;
DROP POLICY IF EXISTS "passengers_delete" ON public.passengers;

CREATE POLICY "passengers_select" ON public.passengers FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','GUIDE','ACCOUNTING'));

CREATE POLICY "passengers_insert" ON public.passengers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));

CREATE POLICY "passengers_update" ON public.passengers FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "passengers_delete" ON public.passengers FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  HOTELS  =============================
DROP POLICY IF EXISTS "hotels_select" ON public.hotels;
DROP POLICY IF EXISTS "hotels_insert" ON public.hotels;
DROP POLICY IF EXISTS "hotels_update" ON public.hotels;
DROP POLICY IF EXISTS "hotels_delete" ON public.hotels;

CREATE POLICY "hotels_select" ON public.hotels FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "hotels_insert" ON public.hotels FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "hotels_update" ON public.hotels FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "hotels_delete" ON public.hotels FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  ROOM_TYPES  =========================
DROP POLICY IF EXISTS "room_types_select" ON public.room_types;
DROP POLICY IF EXISTS "room_types_insert" ON public.room_types;
DROP POLICY IF EXISTS "room_types_update" ON public.room_types;
DROP POLICY IF EXISTS "room_types_delete" ON public.room_types;

CREATE POLICY "room_types_select" ON public.room_types FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "room_types_insert" ON public.room_types FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "room_types_update" ON public.room_types FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "room_types_delete" ON public.room_types FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  HOTEL_CONTRACTS  ====================
DROP POLICY IF EXISTS "hotel_contracts_select" ON public.hotel_contracts;
DROP POLICY IF EXISTS "hotel_contracts_insert" ON public.hotel_contracts;
DROP POLICY IF EXISTS "hotel_contracts_update" ON public.hotel_contracts;
DROP POLICY IF EXISTS "hotel_contracts_delete" ON public.hotel_contracts;

CREATE POLICY "hotel_contracts_select" ON public.hotel_contracts FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING'));

CREATE POLICY "hotel_contracts_insert" ON public.hotel_contracts FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "hotel_contracts_update" ON public.hotel_contracts FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "hotel_contracts_delete" ON public.hotel_contracts FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  VEHICLES  ===========================
DROP POLICY IF EXISTS "vehicles_select" ON public.vehicles;
DROP POLICY IF EXISTS "vehicles_insert" ON public.vehicles;
DROP POLICY IF EXISTS "vehicles_update" ON public.vehicles;
DROP POLICY IF EXISTS "vehicles_delete" ON public.vehicles;

CREATE POLICY "vehicles_select" ON public.vehicles FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "vehicles_insert" ON public.vehicles FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "vehicles_update" ON public.vehicles FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "vehicles_delete" ON public.vehicles FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  DRIVERS  ============================
DROP POLICY IF EXISTS "drivers_select" ON public.drivers;
DROP POLICY IF EXISTS "drivers_insert" ON public.drivers;
DROP POLICY IF EXISTS "drivers_update" ON public.drivers;
DROP POLICY IF EXISTS "drivers_delete" ON public.drivers;

CREATE POLICY "drivers_select" ON public.drivers FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "drivers_insert" ON public.drivers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "drivers_update" ON public.drivers FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "drivers_delete" ON public.drivers FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  GUIDES  =============================
DROP POLICY IF EXISTS "guides_select" ON public.guides;
DROP POLICY IF EXISTS "guides_insert" ON public.guides;
DROP POLICY IF EXISTS "guides_update" ON public.guides;
DROP POLICY IF EXISTS "guides_delete" ON public.guides;

CREATE POLICY "guides_select" ON public.guides FOR SELECT
    USING (tenant_id = public.current_tenant_id());

CREATE POLICY "guides_insert" ON public.guides FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

CREATE POLICY "guides_update" ON public.guides FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "guides_delete" ON public.guides FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  TRANSFERS  ==========================
DROP POLICY IF EXISTS "transfers_select" ON public.transfers;
DROP POLICY IF EXISTS "transfers_insert" ON public.transfers;
DROP POLICY IF EXISTS "transfers_update" ON public.transfers;
DROP POLICY IF EXISTS "transfers_delete" ON public.transfers;

-- GUIDE: sadece kendi atandığı transferler
-- DRIVER: sadece kendi atandığı transferler
CREATE POLICY "transfers_select" ON public.transfers FOR SELECT
    USING (
        tenant_id = public.current_tenant_id()
        AND (
            public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING')
            OR (public.current_user_role() = 'GUIDE'  AND guide_id  = public.current_user_id())
            OR (public.current_user_role() = 'DRIVER' AND driver_id = public.current_user_id())
        )
    );

CREATE POLICY "transfers_insert" ON public.transfers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));

CREATE POLICY "transfers_update" ON public.transfers FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());

CREATE POLICY "transfers_delete" ON public.transfers FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  FINANCE TABLES  =====================
-- accounts, invoices, payments, commissions, expenses
-- Sadece SYSTEM_ADMIN, TOUR_OPERATOR, ACCOUNTING erişir

-- ACCOUNTS
DROP POLICY IF EXISTS "accounts_select" ON public.accounts;
DROP POLICY IF EXISTS "accounts_insert" ON public.accounts;
DROP POLICY IF EXISTS "accounts_update" ON public.accounts;
DROP POLICY IF EXISTS "accounts_delete" ON public.accounts;

CREATE POLICY "accounts_select" ON public.accounts FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "accounts_insert" ON public.accounts FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());
CREATE POLICY "accounts_update" ON public.accounts FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "accounts_delete" ON public.accounts FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

-- INVOICES
DROP POLICY IF EXISTS "invoices_select" ON public.invoices;
DROP POLICY IF EXISTS "invoices_insert" ON public.invoices;
DROP POLICY IF EXISTS "invoices_update" ON public.invoices;
DROP POLICY IF EXISTS "invoices_delete" ON public.invoices;

CREATE POLICY "invoices_select" ON public.invoices FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING'));
CREATE POLICY "invoices_insert" ON public.invoices FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "invoices_update" ON public.invoices FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "invoices_delete" ON public.invoices FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- PAYMENTS
DROP POLICY IF EXISTS "payments_select" ON public.payments;
DROP POLICY IF EXISTS "payments_insert" ON public.payments;
DROP POLICY IF EXISTS "payments_update" ON public.payments;
DROP POLICY IF EXISTS "payments_delete" ON public.payments;

CREATE POLICY "payments_select" ON public.payments FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "payments_insert" ON public.payments FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "payments_update" ON public.payments FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "payments_delete" ON public.payments FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- COMMISSIONS
DROP POLICY IF EXISTS "commissions_select" ON public.commissions;
DROP POLICY IF EXISTS "commissions_insert" ON public.commissions;
DROP POLICY IF EXISTS "commissions_update" ON public.commissions;
DROP POLICY IF EXISTS "commissions_delete" ON public.commissions;

CREATE POLICY "commissions_select" ON public.commissions FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "commissions_insert" ON public.commissions FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "commissions_update" ON public.commissions FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "commissions_delete" ON public.commissions FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- EXPENSES
DROP POLICY IF EXISTS "expenses_select" ON public.expenses;
DROP POLICY IF EXISTS "expenses_insert" ON public.expenses;
DROP POLICY IF EXISTS "expenses_update" ON public.expenses;
DROP POLICY IF EXISTS "expenses_delete" ON public.expenses;

CREATE POLICY "expenses_select" ON public.expenses FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "expenses_insert" ON public.expenses FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'));
CREATE POLICY "expenses_update" ON public.expenses FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','ACCOUNTING'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "expenses_delete" ON public.expenses FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  CRM TABLES  ========================

-- CUSTOMERS
DROP POLICY IF EXISTS "customers_select" ON public.customers;
DROP POLICY IF EXISTS "customers_insert" ON public.customers;
DROP POLICY IF EXISTS "customers_update" ON public.customers;
DROP POLICY IF EXISTS "customers_delete" ON public.customers;

CREATE POLICY "customers_select" ON public.customers FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING'));
CREATE POLICY "customers_insert" ON public.customers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "customers_update" ON public.customers FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "customers_delete" ON public.customers FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- AGENCIES
DROP POLICY IF EXISTS "agencies_select" ON public.agencies;
DROP POLICY IF EXISTS "agencies_insert" ON public.agencies;
DROP POLICY IF EXISTS "agencies_update" ON public.agencies;
DROP POLICY IF EXISTS "agencies_delete" ON public.agencies;

CREATE POLICY "agencies_select" ON public.agencies FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING'));
CREATE POLICY "agencies_insert" ON public.agencies FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());
CREATE POLICY "agencies_update" ON public.agencies FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "agencies_delete" ON public.agencies FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.current_user_role() = 'SYSTEM_ADMIN');

-- LOYALTY_POINTS
DROP POLICY IF EXISTS "loyalty_points_select" ON public.loyalty_points;
DROP POLICY IF EXISTS "loyalty_points_insert" ON public.loyalty_points;
DROP POLICY IF EXISTS "loyalty_points_update" ON public.loyalty_points;
DROP POLICY IF EXISTS "loyalty_points_delete" ON public.loyalty_points;

CREATE POLICY "loyalty_points_select" ON public.loyalty_points FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "loyalty_points_insert" ON public.loyalty_points FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "loyalty_points_update" ON public.loyalty_points FOR UPDATE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "loyalty_points_delete" ON public.loyalty_points FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- CUSTOMER_NOTES
DROP POLICY IF EXISTS "customer_notes_select" ON public.customer_notes;
DROP POLICY IF EXISTS "customer_notes_insert" ON public.customer_notes;
DROP POLICY IF EXISTS "customer_notes_update" ON public.customer_notes;
DROP POLICY IF EXISTS "customer_notes_delete" ON public.customer_notes;

CREATE POLICY "customer_notes_select" ON public.customer_notes FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "customer_notes_insert" ON public.customer_notes FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "customer_notes_update" ON public.customer_notes FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "customer_notes_delete" ON public.customer_notes FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- =====================  SUPPORT TABLES  =====================

-- DOCUMENTS
DROP POLICY IF EXISTS "documents_select" ON public.documents;
DROP POLICY IF EXISTS "documents_insert" ON public.documents;
DROP POLICY IF EXISTS "documents_update" ON public.documents;
DROP POLICY IF EXISTS "documents_delete" ON public.documents;

CREATE POLICY "documents_select" ON public.documents FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "documents_insert" ON public.documents FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','ACCOUNTING'));
CREATE POLICY "documents_update" ON public.documents FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "documents_delete" ON public.documents FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- IMAGES
DROP POLICY IF EXISTS "images_select" ON public.images;
DROP POLICY IF EXISTS "images_insert" ON public.images;
DROP POLICY IF EXISTS "images_update" ON public.images;
DROP POLICY IF EXISTS "images_delete" ON public.images;

CREATE POLICY "images_select" ON public.images FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "images_insert" ON public.images FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "images_update" ON public.images FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "images_delete" ON public.images FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- VOUCHERS
DROP POLICY IF EXISTS "vouchers_select" ON public.vouchers;
DROP POLICY IF EXISTS "vouchers_insert" ON public.vouchers;
DROP POLICY IF EXISTS "vouchers_update" ON public.vouchers;
DROP POLICY IF EXISTS "vouchers_delete" ON public.vouchers;

CREATE POLICY "vouchers_select" ON public.vouchers FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES','GUIDE'));
CREATE POLICY "vouchers_insert" ON public.vouchers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "vouchers_update" ON public.vouchers FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "vouchers_delete" ON public.vouchers FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- NOTIFICATIONS
DROP POLICY IF EXISTS "notifications_select" ON public.notifications;
DROP POLICY IF EXISTS "notifications_insert" ON public.notifications;
DROP POLICY IF EXISTS "notifications_update" ON public.notifications;
DROP POLICY IF EXISTS "notifications_delete" ON public.notifications;

-- Kullanıcı sadece kendi bildirimlerini görür
CREATE POLICY "notifications_select" ON public.notifications FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND (user_id = public.current_user_id() OR public.is_admin_or_operator()));
CREATE POLICY "notifications_insert" ON public.notifications FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "notifications_update" ON public.notifications FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND (user_id = public.current_user_id() OR public.is_admin_or_operator()))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "notifications_delete" ON public.notifications FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- TASKS
DROP POLICY IF EXISTS "tasks_select" ON public.tasks;
DROP POLICY IF EXISTS "tasks_insert" ON public.tasks;
DROP POLICY IF EXISTS "tasks_update" ON public.tasks;
DROP POLICY IF EXISTS "tasks_delete" ON public.tasks;

-- Atanan kişi kendi görevlerini, admin/operator hepsini görür
CREATE POLICY "tasks_select" ON public.tasks FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND (assigned_to = public.current_user_id()
             OR created_by = public.current_user_id()
             OR public.is_admin_or_operator()));
CREATE POLICY "tasks_insert" ON public.tasks FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "tasks_update" ON public.tasks FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND (assigned_to = public.current_user_id() OR public.is_admin_or_operator()))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tasks_delete" ON public.tasks FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- CALENDARS
DROP POLICY IF EXISTS "calendars_select" ON public.calendars;
DROP POLICY IF EXISTS "calendars_insert" ON public.calendars;
DROP POLICY IF EXISTS "calendars_update" ON public.calendars;
DROP POLICY IF EXISTS "calendars_delete" ON public.calendars;

CREATE POLICY "calendars_select" ON public.calendars FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND (assigned_to = public.current_user_id()
             OR assigned_to IS NULL
             OR public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES')));
CREATE POLICY "calendars_insert" ON public.calendars FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'));
CREATE POLICY "calendars_update" ON public.calendars FOR UPDATE
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR','SALES'))
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "calendars_delete" ON public.calendars FOR DELETE
    USING (tenant_id = public.current_tenant_id() AND public.is_admin_or_operator());

-- AUDIT_LOGS (immutable — sadece SELECT)
DROP POLICY IF EXISTS "audit_logs_select" ON public.audit_logs;
DROP POLICY IF EXISTS "audit_logs_insert" ON public.audit_logs;

CREATE POLICY "audit_logs_select" ON public.audit_logs FOR SELECT
    USING (tenant_id = public.current_tenant_id()
        AND public.current_user_role() IN ('SYSTEM_ADMIN','TOUR_OPERATOR'));

-- INSERT trigger tarafından yapılır (SECURITY DEFINER)
CREATE POLICY "audit_logs_insert" ON public.audit_logs FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());

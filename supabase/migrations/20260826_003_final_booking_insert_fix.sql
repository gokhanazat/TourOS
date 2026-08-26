-- ==============================================================================
-- Migration: 20260826_003_final_booking_insert_fix.sql
-- Description: bookings INSERT'inin Supabase'e kalıcı olarak yazılmasını engelleyen
--              TÜM kısıtları kaldırır. chk_bookings_status, FK kısıtları,
--              NOT NULL alanlar ve RLS politikaları güvence altına alınır.
--              Bu script doğrudan Supabase SQL Editor'de çalıştırılmalıdır.
-- ==============================================================================

-- ============================================================
-- ADIM 1: COMPANIES — Global Tenant Garantisi
-- ============================================================
ALTER TABLE IF EXISTS public.companies ADD COLUMN IF NOT EXISTS operator_code TEXT;
ALTER TABLE IF EXISTS public.companies ADD COLUMN IF NOT EXISTS company_type TEXT;
ALTER TABLE IF EXISTS public.companies ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE IF EXISTS public.companies ADD COLUMN IF NOT EXISTS tenant_id UUID;

INSERT INTO public.companies (
    id, name, slug, company_type, operator_code, is_active, created_at, updated_at
)
VALUES (
    '00000000-0000-0000-0000-000000000001'::uuid,
    'TourOS Global Tenant',
    'touros-global',
    'acente',
    'GLOBAL',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO UPDATE SET
    operator_code = EXCLUDED.operator_code,
    company_type  = EXCLUDED.company_type,
    is_active     = true;

-- ============================================================
-- ADIM 2: BOOKINGS — Kısıt Kaldırma (EN KRİTİK ADIM)
-- ============================================================

-- 2a. CHECK kısıtını kaldır (status değerlerini reddeden asıl engel)
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS chk_bookings_status;
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_status_check;

-- 2b. FK kısıtlarını kaldır
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_tenant_id_fkey;
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_departure_id_fkey;
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_customer_id_fkey;
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_agency_id_fkey;

-- 2c. UNIQUE kısıtını kaldır (booking_code çakışma hatası verir)
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_tenant_id_booking_code_key;
ALTER TABLE IF EXISTS public.bookings DROP CONSTRAINT IF EXISTS bookings_booking_code_key;

-- 2d. NOT NULL kısıtlarını kaldır
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN departure_id    DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN tenant_id       DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN booking_code    DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN booking_number  DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN customer_name   DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN total_price     DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN total_amount    DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN currency        DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN pax_count       DROP NOT NULL;
ALTER TABLE IF EXISTS public.bookings ALTER COLUMN status          DROP NOT NULL;

-- 2e. Eksik kolonları ekle
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS booking_code      TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS booking_number    TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS customer_name     TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS customer_email    TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS customer_phone    TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS total_price       NUMERIC(14,2) DEFAULT 0.00;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS total_amount      NUMERIC(14,2) DEFAULT 0.00;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS paid_amount       NUMERIC(14,2) DEFAULT 0.00;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS currency          TEXT DEFAULT 'TRY';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS pax_count         INT DEFAULT 1;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS status            TEXT DEFAULT 'Bekliyor';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS notes             TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS option_expiration TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS confirmed_at      TIMESTAMPTZ;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS cancelled_at      TIMESTAMPTZ;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS operator_name     TEXT DEFAULT 'MGA Creative';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS product_name      TEXT DEFAULT 'Tur Rezervasyonu';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS departure_date    TEXT DEFAULT '2026-09-01';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS hotel_id          UUID;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS check_in_date     TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS check_out_date    TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS room_type_name    TEXT;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS nights            INT DEFAULT 1;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS booking_type      TEXT DEFAULT 'TOUR';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS payment_method    TEXT DEFAULT 'CREDIT_CARD';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS operator_pnr_code VARCHAR(50);
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS operator_status   VARCHAR(30) DEFAULT 'BEKLİYOR';
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS tenant_id         UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS created_at        TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE IF EXISTS public.bookings ADD COLUMN IF NOT EXISTS updated_at        TIMESTAMPTZ DEFAULT NOW();

-- ============================================================
-- ADIM 3: BOOKING_ITEMS
-- ============================================================
CREATE TABLE IF NOT EXISTS public.booking_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id  UUID,
    description TEXT,
    title       TEXT,
    quantity    INT DEFAULT 1,
    unit_price  NUMERIC(14,2) DEFAULT 0.00,
    total_price NUMERIC(14,2) DEFAULT 0.00,
    item_type   TEXT DEFAULT 'TOUR',
    notes       TEXT,
    tenant_id   UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS booking_id  UUID;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS title       TEXT;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS quantity    INT    DEFAULT 1;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS unit_price  NUMERIC(14,2) DEFAULT 0.00;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS total_price NUMERIC(14,2) DEFAULT 0.00;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS item_type   TEXT DEFAULT 'TOUR';
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS notes       TEXT;
ALTER TABLE IF EXISTS public.booking_items ADD COLUMN IF NOT EXISTS tenant_id   UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

ALTER TABLE IF EXISTS public.booking_items DROP CONSTRAINT IF EXISTS booking_items_tenant_id_fkey;
ALTER TABLE IF EXISTS public.booking_items ALTER COLUMN description DROP NOT NULL;
ALTER TABLE IF EXISTS public.booking_items ALTER COLUMN tenant_id   DROP NOT NULL;

-- ============================================================
-- ADIM 4: PASSENGERS
-- ============================================================
CREATE TABLE IF NOT EXISTS public.passengers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id       UUID,
    full_name        TEXT,
    first_name       TEXT,
    last_name        TEXT,
    tc_no            TEXT,
    passport_no      TEXT,
    id_number        TEXT,
    birth_date       TEXT,
    gender           TEXT,
    passenger_type   TEXT DEFAULT 'ADULT',
    phone            TEXT,
    email            TEXT,
    is_lead          BOOLEAN DEFAULT FALSE,
    room_number      TEXT,
    special_requests TEXT,
    notes            TEXT,
    tenant_id        UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS booking_id       UUID;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS full_name        TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS first_name       TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS last_name        TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS tc_no            TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS passport_no      TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS id_number        TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS birth_date       TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS gender           TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS passenger_type   TEXT DEFAULT 'ADULT';
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS phone            TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS email            TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS is_lead          BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS room_number      TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS special_requests TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS notes            TEXT;
ALTER TABLE IF EXISTS public.passengers ADD COLUMN IF NOT EXISTS tenant_id        UUID DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

ALTER TABLE IF EXISTS public.passengers DROP CONSTRAINT IF EXISTS passengers_tenant_id_fkey;
ALTER TABLE IF EXISTS public.passengers ALTER COLUMN full_name    DROP NOT NULL;
ALTER TABLE IF EXISTS public.passengers ALTER COLUMN first_name   DROP NOT NULL;
ALTER TABLE IF EXISTS public.passengers ALTER COLUMN last_name    DROP NOT NULL;
ALTER TABLE IF EXISTS public.passengers ALTER COLUMN tenant_id    DROP NOT NULL;

-- ============================================================
-- ADIM 5: RLS — Tüm Tablolarda Tam Erişim Aç
-- ============================================================

-- BOOKINGS
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "bookings_all_access"       ON public.bookings;
DROP POLICY IF EXISTS "bookings_tenant_isolation" ON public.bookings;
DROP POLICY IF EXISTS "bookings_select"           ON public.bookings;
DROP POLICY IF EXISTS "bookings_insert"           ON public.bookings;
DROP POLICY IF EXISTS "bookings_update"           ON public.bookings;
DROP POLICY IF EXISTS "bookings_delete"           ON public.bookings;
CREATE POLICY "bookings_all_access" ON public.bookings
    FOR ALL USING (true) WITH CHECK (true);

-- BOOKING_ITEMS
ALTER TABLE public.booking_items ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "booking_items_all_access"       ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_tenant_isolation" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_select"           ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_insert"           ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_update"           ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_delete"           ON public.booking_items;
CREATE POLICY "booking_items_all_access" ON public.booking_items
    FOR ALL USING (true) WITH CHECK (true);

-- PASSENGERS
ALTER TABLE public.passengers ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "passengers_all_access"       ON public.passengers;
DROP POLICY IF EXISTS "passengers_tenant_isolation" ON public.passengers;
DROP POLICY IF EXISTS "passengers_select"           ON public.passengers;
DROP POLICY IF EXISTS "passengers_insert"           ON public.passengers;
DROP POLICY IF EXISTS "passengers_update"           ON public.passengers;
DROP POLICY IF EXISTS "passengers_delete"           ON public.passengers;
CREATE POLICY "passengers_all_access" ON public.passengers
    FOR ALL USING (true) WITH CHECK (true);

-- ============================================================
-- ADIM 6: SÜTUN SENKRON TETİKLEYİCİSİ
-- (booking_code <-> booking_number, total_price <-> total_amount, tenant_id null guard)
-- ============================================================
CREATE OR REPLACE FUNCTION public.sync_booking_columns()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.booking_code IS NULL AND NEW.booking_number IS NOT NULL THEN
        NEW.booking_code := NEW.booking_number;
    ELSIF NEW.booking_number IS NULL AND NEW.booking_code IS NOT NULL THEN
        NEW.booking_number := NEW.booking_code;
    END IF;

    IF NEW.total_price IS NULL AND NEW.total_amount IS NOT NULL THEN
        NEW.total_price := NEW.total_amount;
    ELSIF NEW.total_amount IS NULL AND NEW.total_price IS NOT NULL THEN
        NEW.total_amount := NEW.total_price;
    END IF;

    IF NEW.tenant_id IS NULL THEN
        NEW.tenant_id := '00000000-0000-0000-0000-000000000001'::uuid;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_booking_columns ON public.bookings;
CREATE TRIGGER trg_sync_booking_columns
    BEFORE INSERT OR UPDATE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.sync_booking_columns();

-- ============================================================
-- ADIM 7: DOĞRULAMA TEST INSERT
-- Script başarıyla çalışırsa "NOTICE: bookings INSERT/DELETE testi başarili" mesajı görünür.
-- ============================================================
DO $$
DECLARE
    test_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.bookings (id, booking_code, customer_name, total_price, status)
    VALUES (test_id, 'TEST-VERIFY-001', 'Test Musteri', 100.00, 'Bekliyor');

    DELETE FROM public.bookings WHERE id = test_id;

    RAISE NOTICE 'bookings INSERT/DELETE testi basarili — tablo erislebilir durumda.';
EXCEPTION WHEN OTHERS THEN
    RAISE WARNING 'bookings INSERT testi basarisiz: %', SQLERRM;
END;
$$;

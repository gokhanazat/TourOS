-- ============================================================
-- TourOS Migration: Surcharges & Advanced Booking Extras
-- Adds surcharges, child responsible adult, infant seat preference & cost/margin tracking
-- ============================================================

-- 1. marketplace_products tablosuna surcharges ve ekstra hizmet JSON sütunları ekle
ALTER TABLE public.marketplace_products 
    ADD COLUMN IF NOT EXISTS surcharges_json JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS extra_services_json JSONB DEFAULT '[]'::jsonb;

-- 2. passengers tablosuna Çocuk Sorumlusu & İnfant Koltuğu sütunları ekle
ALTER TABLE public.passengers
    ADD COLUMN IF NOT EXISTS parent_passenger_id UUID REFERENCES public.passengers(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS is_infant_seat_requested BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS country_of_birth TEXT,
    ADD COLUMN IF NOT EXISTS document_issue_date DATE,
    ADD COLUMN IF NOT EXISTS document_expire_date DATE;

-- 3. bookings tablosuna ERP Back-Office Alış/Satış/Net Kâr sütunları ekle
ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS net_cost NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS gross_sales NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS profit_margin NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS commission_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS incoming_voucher_code TEXT,
    ADD COLUMN IF NOT EXISTS is_bsp BOOLEAN DEFAULT FALSE;

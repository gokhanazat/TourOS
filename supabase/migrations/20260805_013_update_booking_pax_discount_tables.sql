-- ============================================================
-- TourOS 1.4.3 Booking Pax, Discount & Commission Migration
-- ============================================================

ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS adult_count INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS child_count INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS infant_count INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12,2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS coupon_code TEXT,
    ADD COLUMN IF NOT EXISTS commission_rate NUMERIC(5,2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS commission_amount NUMERIC(12,2) DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_bookings_coupon ON public.bookings(coupon_code);

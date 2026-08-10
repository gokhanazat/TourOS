-- ============================================================
-- TourOS Migration: 20260809_028_hotel_bookings_support.sql
-- Otel Rezervasyonları (B2C Hotel Booking) Desteği
-- ============================================================

-- 1. Otel rezervasyonları için departure_id üzerindeki NOT NULL kısıtını kaldır
ALTER TABLE public.bookings ALTER COLUMN departure_id DROP NOT NULL;

-- 2. Otel konaklama alanlarını bookings tablosuna ekle
ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS hotel_id UUID REFERENCES public.hotels(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS check_in_date DATE,
    ADD COLUMN IF NOT EXISTS check_out_date DATE,
    ADD COLUMN IF NOT EXISTS room_type_name TEXT,
    ADD COLUMN IF NOT EXISTS nights INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS booking_type TEXT DEFAULT 'TOUR',
    ADD COLUMN IF NOT EXISTS payment_method TEXT DEFAULT 'CREDIT_CARD';

CREATE INDEX IF NOT EXISTS idx_bookings_hotel ON public.bookings(hotel_id);

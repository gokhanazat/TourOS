-- ============================================================
-- TourOS Migration: 20260809_029_fix_booking_hotel_fields.sql
-- Bookings Tablosu Otel ve Tur Rezervasyon Alanları Uyumluluğu
-- ============================================================

-- 1. departure_id sütununu NULL yapılabilir hale getir (Otel rezervasyonları için)
ALTER TABLE public.bookings ALTER COLUMN departure_id DROP NOT NULL;

-- 2. Otel Rezervasyon Kolonlarını Ekle (Eğer yoksa)
ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS hotel_id UUID REFERENCES public.hotels(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS check_in_date DATE,
    ADD COLUMN IF NOT EXISTS check_out_date DATE,
    ADD COLUMN IF NOT EXISTS room_type_name TEXT,
    ADD COLUMN IF NOT EXISTS nights INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS booking_type TEXT DEFAULT 'TOUR',
    ADD COLUMN IF NOT EXISTS payment_method TEXT DEFAULT 'CREDIT_CARD';

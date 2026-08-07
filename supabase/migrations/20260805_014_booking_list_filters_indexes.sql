-- ============================================================
-- TourOS 1.4.4 Rezervasyon Listesi İndeks & Filtreleme Scripti
-- ============================================================

-- Durum, Tarih Aralığı ve Tur Bazlı Arama İndeksleri
CREATE INDEX IF NOT EXISTS idx_bookings_status_tenant ON public.bookings(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON public.bookings(created_at);
CREATE INDEX IF NOT EXISTS idx_bookings_search_text ON public.bookings(booking_code, customer_name, customer_phone);

-- Departure - Booking Join İndeksi
CREATE INDEX IF NOT EXISTS idx_bookings_departure_tenant ON public.bookings(departure_id, tenant_id);

-- ============================================================
-- TourOS 1.4.1 Booking Table Schema & Index Update
-- ============================================================

ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS agency_id UUID,
    ADD COLUMN IF NOT EXISTS option_expiration TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_bookings_customer ON public.bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_agency ON public.bookings(agency_id);

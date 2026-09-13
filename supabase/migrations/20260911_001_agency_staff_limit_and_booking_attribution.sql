-- ==============================================================================
-- TourOS Migration: 20260911_001_agency_staff_limit_and_booking_attribution.sql
-- YANDEX CLOUD POSTGRESQL CANLIDA ÇALIŞTIRILMIŞTIR (Status: Applied)
-- ==============================================================================

-- 1. agencies tablosuna max_staff_limit kolonu (varsayılan 3)
ALTER TABLE public.agencies 
ADD COLUMN IF NOT EXISTS max_staff_limit INT DEFAULT 3;

-- 2. users tablosuna agency_id kolonu ekleme (elemanın bağlı olduğu acenta)
ALTER TABLE public.users 
ADD COLUMN IF NOT EXISTS agency_id UUID REFERENCES public.agencies(id) ON DELETE SET NULL;

-- 3. bookings tablosuna staff_user_id kolonu (rezervasyonu giren eleman)
ALTER TABLE public.bookings 
ADD COLUMN IF NOT EXISTS staff_user_id UUID REFERENCES public.users(id) ON DELETE SET NULL;

-- 4. İndeksler (Sorgu performansı için)
CREATE INDEX IF NOT EXISTS idx_users_agency_id ON public.users(agency_id);
CREATE INDEX IF NOT EXISTS idx_bookings_agency_id ON public.bookings(agency_id);
CREATE INDEX IF NOT EXISTS idx_bookings_staff_user_id ON public.bookings(staff_user_id);

-- 5. Kota Kontrol Trigger Fonksiyonu (Acentanın max_staff_limit değerini aşmasını önler)
CREATE OR REPLACE FUNCTION public.check_agency_user_limit()
RETURNS TRIGGER AS $$
DECLARE
    current_active_staff INT;
    allowed_limit INT;
BEGIN
    IF NEW.agency_id IS NOT NULL AND (NEW.is_active IS TRUE OR NEW.is_active IS NULL) THEN
        SELECT COUNT(*) INTO current_active_staff 
        FROM public.users 
        WHERE agency_id = NEW.agency_id 
          AND is_active = TRUE 
          AND id <> COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid);

        SELECT COALESCE(max_staff_limit, 3) INTO allowed_limit 
        FROM public.agencies 
        WHERE id = NEW.agency_id;

        IF allowed_limit IS NOT NULL AND current_active_staff >= allowed_limit THEN
            RAISE EXCEPTION 'Acenta için tanımlanan maksimum aktif kullanıcı kotası (%) dolmuştur. Lütfen paket yükseltiniz.', allowed_limit;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 6. Trigger
DROP TRIGGER IF EXISTS trg_check_agency_user_limit ON public.users;
CREATE TRIGGER trg_check_agency_user_limit
BEFORE INSERT OR UPDATE OF agency_id, is_active ON public.users
FOR EACH ROW
EXECUTE FUNCTION public.check_agency_user_limit();

-- ============================================================
-- TourOS Migration: 20260808_022_marketplace_cancellation_and_inapp_notifications.sql
-- Prompt 4.6.10: İptal ve Event Bildirimi (Sistem İçi, Operatör Tarafı)
-- Rezervasyon iptal edildiğinde operatörün kendi bookings tablosunu günceller
-- ve SADECE sistem içi bildirim (notifications tablosu) düşürür. E-POSTA GÖNDERİMİ YOKTUR.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Pazaryeri Rezervasyon İptali ve Otomatik Sistem İçi Bildirim RPC
CREATE OR REPLACE FUNCTION public.cancel_operator_marketplace_booking(
    p_booking_id UUID,
    p_reason TEXT DEFAULT 'Acente Talebiyle İptal'
) RETURNS VOID AS $$
DECLARE
    target_tenant_id UUID;
    target_booking_code TEXT;
    target_agency_id UUID;
BEGIN
    -- Rezervasyonu bul
    SELECT tenant_id, booking_code, agency_id
    INTO target_tenant_id, target_booking_code, target_agency_id
    FROM public.bookings
    WHERE id = p_booking_id;

    IF target_tenant_id IS NULL THEN
        RAISE EXCEPTION 'İptal edilecek rezervasyon bulunamadı: %', p_booking_id;
    END IF;

    -- 1. Rezervasyon statüsünü CANCELLED yap
    UPDATE public.bookings
    SET status = 'CANCELLED',
        updated_at = now()
    WHERE id = p_booking_id;

    -- 2. SADECE Sistem İçi Bildirim Oluştur (notifications tablosuna ekler, e-posta GÖNDERMEZ)
    INSERT INTO public.notifications (
        id,
        tenant_id,
        title,
        body,
        type,
        is_read,
        created_at
    ) VALUES (
        uuid_generate_v4(),
        target_tenant_id, -- Operatörün kendi paneline bildirim düşer
        'Pazaryeri İptal Bildirimi ⚠️',
        'Acente tarafından ' || COALESCE(target_booking_code, p_booking_id::text) || ' kodlu rezervasyon iptal edildi. Neden: ' || COALESCE(p_reason, 'Belirtilmedi'),
        'BOOKING_CANCELLED',
        false,
        now()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

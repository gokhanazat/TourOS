-- ============================================================
-- TourOS Migration: 20260808_021_marketplace_booking_routing_rpc.sql
-- Prompt 4.6.9: Rezervasyon Routing Motoru RPC ve Çözümleme Fonksiyonları
-- Tur kodundaki prefiksten (ör. ANK) ilgili operator_company_id'yi çözer
-- ve operatörün kendi bookings tablosuna cross-tenant kayıt oluşturur.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tur Kodundan Prefiks Ayıklayıp Operatör Firma ID'sini Çözen RPC
CREATE OR REPLACE FUNCTION public.resolve_operator_company_id(p_tour_code TEXT)
RETURNS UUID AS $$
DECLARE
    prefix_code TEXT;
    operator_id UUID;
BEGIN
    -- Örn. "ANK-00001" -> "ANK"
    prefix_code := UPPER(TRIM(SPLIT_PART(p_tour_code, '-', 1)));

    SELECT c.id INTO operator_id
    FROM public.companies c
    WHERE UPPER(TRIM(c.operator_code)) = prefix_code
    LIMIT 1;

    -- Eğer prefiksle bulunamadıysa tur_id üzerinden doğrudan turu üreten operatörü bul
    IF operator_id IS NULL THEN
        SELECT t.tenant_id INTO operator_id
        FROM public.tours t
        WHERE UPPER(TRIM(t.code)) = UPPER(TRIM(p_tour_code))
        LIMIT 1;
    END IF;

    RETURN operator_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 2. Operatörün Kendi Bookings Tablosuna Cross-Tenant Rezervasyon Kaydeden RPC
CREATE OR REPLACE FUNCTION public.confirm_operator_marketplace_booking(
    p_agency_id UUID,
    p_operator_company_id UUID,
    p_tour_id UUID,
    p_customer_name TEXT,
    p_pax_count INT,
    p_total_amount NUMERIC
) RETURNS UUID AS $$
DECLARE
    new_booking_id UUID;
    generated_booking_code TEXT;
BEGIN
    generated_booking_code := 'MP-' || TO_CHAR(now(), 'YYYYMMDD') || '-' || LPAD(FLOOR(RANDOM()*10000)::text, 4, '0');

    INSERT INTO public.bookings (
        tenant_id,
        agency_id,
        tour_id,
        booking_code,
        customer_name,
        pax_count,
        total_price,
        status,
        created_at,
        updated_at
    ) VALUES (
        p_operator_company_id, -- Operatörün kendi tenant'ına kaydedilir
        p_agency_id,           -- Rezervasyonu alan acente
        p_tour_id,
        generated_booking_code,
        p_customer_name,
        p_pax_count,
        p_total_amount,
        'CONFIRMED',
        now(),
        now()
    ) RETURNING id INTO new_booking_id;

    RETURN new_booking_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

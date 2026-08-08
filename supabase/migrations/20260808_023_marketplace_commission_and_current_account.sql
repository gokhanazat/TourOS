-- ============================================================
-- TourOS Migration: 20260808_023_marketplace_commission_and_current_account.sql
-- Prompt 4.6.11: Komisyon Hesaplama ve Acente Cari Entegrasyonu
-- Her pazaryeri satışından sonra agency_operator_connections tablosundaki
-- komisyon oranına göre otomatik komisyon hesaplayıp commissions ve accounts (cari) tablolarına işler.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Pazaryeri Komisyon Hesaplayan ve Acente Carisine İşleyen RPC
CREATE OR REPLACE FUNCTION public.process_marketplace_booking_commission(
    p_booking_id UUID,
    p_agency_id UUID,
    p_operator_company_id UUID,
    p_total_price NUMERIC
) RETURNS TABLE (
    id UUID,
    booking_id UUID,
    agency_id UUID,
    amount NUMERIC,
    commission_rate NUMERIC,
    status TEXT,
    created_at TIMESTAMPTZ
) AS $$
DECLARE
    comm_rate NUMERIC := 10.00;
    comm_amount NUMERIC := 0.00;
    new_comm_id UUID;
BEGIN
    -- 1. agency_operator_connections tablosundan anlaşılan komisyon oranını al
    SELECT COALESCE(commission_rate, 10.00) INTO comm_rate
    FROM public.agency_operator_connections
    WHERE agency_id = p_agency_id AND operator_company_id = p_operator_company_id;

    -- 2. Komisyon tutarını hesapla (örn. 10.000 ₺ x %15 = 1.500 ₺)
    comm_amount := ROUND(COALESCE(p_total_price, 0.0) * (comm_rate / 100.0), 2);

    -- 3. commissions tablosuna ekle
    INSERT INTO public.commissions (
        id,
        booking_id,
        agency_id,
        amount,
        commission_rate,
        status,
        created_at
    ) VALUES (
        uuid_generate_v4(),
        p_booking_id,
        p_agency_id,
        comm_amount,
        comm_rate,
        'APPROVED',
        now()
    ) RETURNING commissions.id INTO new_comm_id;

    -- 4. Acentenin Cari Hesabına (accounts) ALACAK/HAK EDİŞ olarak işle
    INSERT INTO public.accounts (
        id,
        tenant_id,
        account_name,
        account_type,
        balance,
        currency,
        created_at
    ) VALUES (
        uuid_generate_v4(),
        p_agency_id,
        'Pazaryeri Komisyon Hakedişi (Rezervasyon: ' || p_booking_id::text || ')',
        'COMMISSION_EARNING',
        comm_amount,
        'TRY',
        now()
    );

    RETURN QUERY
    SELECT 
        c.id,
        c.booking_id,
        c.agency_id,
        c.amount,
        c.commission_rate,
        c.status,
        c.created_at
    FROM public.commissions c
    WHERE c.id = new_comm_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

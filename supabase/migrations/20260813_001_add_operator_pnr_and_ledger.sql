-- ==============================================================================
-- Migration: 20260813_001_add_operator_pnr_and_ledger.sql
-- Description: Tur Operatörü Onay Kodu (TO PNR) ve Cari Hesap Takip Yapısı
-- ==============================================================================

-- 1. bookings tablosuna operator_pnr_code ve operator_status sütunlarını ekle
ALTER TABLE public.bookings 
ADD COLUMN IF NOT EXISTS operator_pnr_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS operator_status VARCHAR(30) DEFAULT 'BEKLİYOR';

-- PNR koduna göre arama performansını artırmak için indeks
CREATE INDEX IF NOT EXISTS idx_bookings_operator_pnr_code ON public.bookings(operator_pnr_code);

-- 2. current_account_transactions (Cari Hesap Hareketleri) tablosunu oluştur (Eğer yoksa)
CREATE TABLE IF NOT EXISTS public.current_account_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID,
    booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    supplier_id UUID,
    operator_pnr_code VARCHAR(50),
    transaction_type VARCHAR(20) NOT NULL DEFAULT 'CREDIT',
    amount NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'TRY',
    description TEXT,
    tenant_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Eğer tablo zaten varsa sütunların bulunduğundan emin ol
ALTER TABLE public.current_account_transactions 
ADD COLUMN IF NOT EXISTS operator_pnr_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS supplier_id UUID;

-- Cari işlemlerde PNR takibi için indeks
CREATE INDEX IF NOT EXISTS idx_cat_operator_pnr_code ON public.current_account_transactions(operator_pnr_code);

-- 3. Cari hareket PNR sorgusu için RPC fonksiyonu
CREATE OR REPLACE FUNCTION public.get_transactions_by_pnr(p_pnr_code TEXT)
RETURNS TABLE (
    id UUID,
    account_id UUID,
    booking_id UUID,
    operator_pnr_code VARCHAR(50),
    transaction_type VARCHAR(20),
    amount NUMERIC,
    currency VARCHAR(10),
    description TEXT,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        cat.id,
        cat.account_id,
        cat.booking_id,
        cat.operator_pnr_code,
        cat.transaction_type,
        cat.amount,
        cat.currency,
        cat.description,
        cat.created_at
    FROM public.current_account_transactions cat
    WHERE cat.operator_pnr_code ILIKE '%' || p_pnr_code || '%';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

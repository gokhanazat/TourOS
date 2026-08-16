-- ============================================================================
-- TourOS Migration: 20260816_001_agency_subscription_and_status.sql
-- DESCRIPTION: Acente Abonelik Süresi (+365 Gün) ve Aktif/Pasif Erişim Yönetimi
-- ============================================================================

-- 1. Companies Tablosuna Abonelik Kolonlarının Eklenmesi (Varsa Atlar)
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS subscription_start_date TIMESTAMPTZ DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS subscription_end_date TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '365 days');

-- Default erişim kapalı (false) olarak ayarlanması
ALTER TABLE public.companies ALTER COLUMN is_active SET DEFAULT false;

-- 2. Acente Arama ve Abonelik Bilgisi Getirme RPC Fonksiyonu
DROP FUNCTION IF EXISTS public.search_agencies(TEXT, TEXT);

CREATE OR REPLACE FUNCTION public.search_agencies(
    p_search_query TEXT DEFAULT '',
    p_country TEXT DEFAULT ''
)
RETURNS TABLE (
    company_id UUID,
    agency_name TEXT,
    operator_code TEXT,
    email TEXT,
    phone TEXT,
    country TEXT,
    address TEXT,
    tax_number TEXT,
    tax_office TEXT,
    mersis_no TEXT,
    is_active BOOLEAN,
    subscription_start_date TIMESTAMPTZ,
    subscription_end_date TIMESTAMPTZ,
    remaining_days INTEGER,
    created_at TIMESTAMPTZ
) 
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id AS company_id,
        c.name AS agency_name,
        COALESCE(c.operator_code, 'TANIMSIZ') AS operator_code,
        COALESCE(c.email, '') AS email,
        COALESCE(c.phone, '') AS phone,
        COALESCE(c.address, 'Türkiye') AS country,
        COALESCE(c.address, '') AS address,
        COALESCE(c.tax_number, '') AS tax_number,
        COALESCE(c.tax_office, '') AS tax_office,
        COALESCE(c.mersis_no, '') AS mersis_no,
        COALESCE(c.is_active, false) AS is_active,
        COALESCE(c.subscription_start_date, c.created_at, NOW()) AS subscription_start_date,
        COALESCE(c.subscription_end_date, (COALESCE(c.subscription_start_date, NOW()) + INTERVAL '365 days')) AS subscription_end_date,
        GREATEST(0, EXTRACT(DAY FROM (COALESCE(c.subscription_end_date, NOW() + INTERVAL '365 days') - NOW()))::INTEGER) AS remaining_days,
        c.created_at
    FROM public.companies c
    WHERE c.company_type = 'acente'
      AND (p_search_query = '' OR (
          c.name ILIKE '%' || p_search_query || '%' OR 
          c.email ILIKE '%' || p_search_query || '%' OR 
          c.operator_code ILIKE '%' || p_search_query || '%'
      ))
      AND (p_country = '' OR c.address ILIKE '%' || p_country || '%')
    ORDER BY c.name ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Acente Abonelik ve Aktiflik Durumunu Güncelleme RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.update_agency_subscription_status(
    p_company_id UUID,
    p_is_active BOOLEAN,
    p_subscription_start_date TIMESTAMPTZ,
    p_subscription_end_date TIMESTAMPTZ
)
RETURNS BOOLEAN
SET search_path = public
AS $$
BEGIN
    UPDATE public.companies
    SET is_active = p_is_active,
        subscription_start_date = p_subscription_start_date,
        subscription_end_date = p_subscription_end_date,
        updated_at = NOW()
    WHERE id = p_company_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

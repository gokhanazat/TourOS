-- ============================================================================
-- TourOS Migration: 20260816_003_agency_query_quota_and_usage.sql
-- DESCRIPTION: Acente Sorgu / Arama Kotası ve Canlı Tüketim Takibi
-- ============================================================================

-- 1. Companies Tablosuna Kota Kolonlarının Eklenmesi
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS monthly_query_quota INTEGER DEFAULT 5000,
ADD COLUMN IF NOT EXISTS current_month_queries INTEGER DEFAULT 0;

-- 2. Acente Arama RPC Fonksiyonunun Kota Kolonları ile Güncellenmesi
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
    monthly_query_quota INTEGER,
    current_month_queries INTEGER,
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
        COALESCE(c.monthly_query_quota, 5000) AS monthly_query_quota,
        COALESCE(c.current_month_queries, 0) AS current_month_queries,
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

-- 3. Acente Lisans, Aktiflik ve Kota Güncelleme RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.update_agency_subscription_and_quota(
    p_company_id UUID,
    p_is_active BOOLEAN,
    p_subscription_start_date TIMESTAMPTZ,
    p_subscription_end_date TIMESTAMPTZ,
    p_monthly_query_quota INTEGER,
    p_current_month_queries INTEGER
)
RETURNS BOOLEAN
SET search_path = public
AS $$
BEGIN
    UPDATE public.companies
    SET is_active = p_is_active,
        subscription_start_date = p_subscription_start_date,
        subscription_end_date = p_subscription_end_date,
        monthly_query_quota = p_monthly_query_quota,
        current_month_queries = p_current_month_queries,
        updated_at = NOW()
    WHERE id = p_company_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

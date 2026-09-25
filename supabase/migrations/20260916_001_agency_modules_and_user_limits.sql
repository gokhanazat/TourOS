-- ==============================================================================
-- TourOS Migration: 20260916_001_agency_modules_and_user_limits.sql
-- DESCRIPTION: Acente Modül Paket Yönetimi (Aç/Kapa) ve Kullanıcı Tanımlama Limiti
-- ==============================================================================

-- 1. Companies Tablosuna Modül Yetkileri, Kullanıcı Limiti ve Kota Kolonlarının Eklenmesi
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS max_user_limit INTEGER DEFAULT 5,
ADD COLUMN IF NOT EXISTS allowed_modules TEXT[] DEFAULT ARRAY['B2B_SALES', 'SETTINGS'],
ADD COLUMN IF NOT EXISTS daily_query_quota INTEGER DEFAULT 250,
ADD COLUMN IF NOT EXISTS today_queries INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS last_query_date DATE DEFAULT CURRENT_DATE,
ADD COLUMN IF NOT EXISTS last_query_month INTEGER DEFAULT EXTRACT(MONTH FROM CURRENT_DATE);

-- Mevcut şirketlerin varsayılan değerlerini doldur
UPDATE public.companies 
SET allowed_modules = ARRAY['B2B_SALES', 'SETTINGS'] 
WHERE allowed_modules IS NULL;

UPDATE public.companies 
SET max_user_limit = 5 
WHERE max_user_limit IS NULL;

-- 2. search_agencies RPC Fonksiyonunu max_user_limit ve allowed_modules ile Güncelle
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
    daily_query_quota INTEGER,
    today_queries INTEGER,
    monthly_query_quota INTEGER,
    current_month_queries INTEGER,
    max_user_limit INTEGER,
    allowed_modules TEXT[],
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
        COALESCE(c.daily_query_quota, 250) AS daily_query_quota,
        CASE WHEN c.last_query_date < CURRENT_DATE THEN 0 ELSE COALESCE(c.today_queries, 0) END AS today_queries,
        COALESCE(c.monthly_query_quota, 5000) AS monthly_query_quota,
        CASE WHEN c.last_query_month != EXTRACT(MONTH FROM CURRENT_DATE) THEN 0 ELSE COALESCE(c.current_month_queries, 0) END AS current_month_queries,
        COALESCE(c.max_user_limit, 5) AS max_user_limit,
        COALESCE(c.allowed_modules, ARRAY['B2B_SALES', 'SETTINGS']) AS allowed_modules,
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

-- 3. update_agency_subscription_and_quota RPC Fonksiyonunu Güncelle
DROP FUNCTION IF EXISTS public.update_agency_subscription_and_quota(UUID, BOOLEAN, TIMESTAMPTZ, TIMESTAMPTZ, INTEGER, INTEGER, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS public.update_agency_subscription_and_quota(UUID, BOOLEAN, TIMESTAMPTZ, TIMESTAMPTZ, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, TEXT[]);

CREATE OR REPLACE FUNCTION public.update_agency_subscription_and_quota(
    p_company_id UUID,
    p_is_active BOOLEAN,
    p_subscription_start_date TIMESTAMPTZ,
    p_subscription_end_date TIMESTAMPTZ,
    p_daily_query_quota INTEGER DEFAULT 250,
    p_today_queries INTEGER DEFAULT 0,
    p_monthly_query_quota INTEGER DEFAULT 5000,
    p_current_month_queries INTEGER DEFAULT 0,
    p_max_user_limit INTEGER DEFAULT 5,
    p_allowed_modules TEXT[] DEFAULT ARRAY['B2B_SALES', 'SETTINGS']
)
RETURNS BOOLEAN
SET search_path = public
AS $$
BEGIN
    UPDATE public.companies
    SET is_active = p_is_active,
        subscription_start_date = p_subscription_start_date,
        subscription_end_date = p_subscription_end_date,
        daily_query_quota = p_daily_query_quota,
        today_queries = p_today_queries,
        monthly_query_quota = p_monthly_query_quota,
        current_month_queries = p_current_month_queries,
        max_user_limit = GREATEST(1, COALESCE(p_max_user_limit, 5)),
        allowed_modules = COALESCE(p_allowed_modules, ARRAY['B2B_SALES', 'SETTINGS']),
        updated_at = NOW()
    WHERE id = p_company_id;

    -- agencies tablosunda da max_staff_limit eşitlensin
    UPDATE public.agencies
    SET max_staff_limit = GREATEST(1, COALESCE(p_max_user_limit, 5))
    WHERE tenant_id = p_company_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. Kullanıcı Limiti Denetim Trigger Fonksiyonu
CREATE OR REPLACE FUNCTION public.check_agency_user_limit()
RETURNS TRIGGER AS $$
DECLARE
    current_active_staff INT;
    allowed_limit INT;
    v_tenant_id UUID;
BEGIN
    v_tenant_id := NEW.tenant_id;
    
    IF v_tenant_id IS NOT NULL AND (NEW.is_active IS TRUE OR NEW.is_active IS NULL) THEN
        SELECT COUNT(*) INTO current_active_staff 
        FROM public.users 
        WHERE tenant_id = v_tenant_id 
          AND is_active = TRUE 
          AND id <> COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid);

        SELECT COALESCE(max_user_limit, 5) INTO allowed_limit 
        FROM public.companies 
        WHERE id = v_tenant_id;

        IF allowed_limit IS NOT NULL AND current_active_staff >= allowed_limit THEN
            RAISE EXCEPTION 'Acenta için tanımlanan maksimum aktif kullanıcı kotası (%) dolmuştur. Lütfen paket yükseltiniz.', allowed_limit;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger'ın bağlı olduğundan emin ol
DROP TRIGGER IF EXISTS trg_check_agency_user_limit ON public.users;
CREATE TRIGGER trg_check_agency_user_limit
BEFORE INSERT OR UPDATE OF tenant_id, is_active ON public.users
FOR EACH ROW
EXECUTE FUNCTION public.check_agency_user_limit();

-- ============================================================================
-- TourOS Migration: 20260821_001_daily_and_monthly_quota_limits.sql
-- DESCRIPTION: Acente Günlük & Aylık Arama Kotası, 00:00 Otomatik Sıfırlama ve Arama Kilidi
-- ============================================================================

-- 1. Companies Tablosuna Günlük ve Aylık Takip Kolonlarının Eklenmesi
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS daily_query_quota INTEGER DEFAULT 250,
ADD COLUMN IF NOT EXISTS today_queries INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS last_query_date DATE DEFAULT CURRENT_DATE,
ADD COLUMN IF NOT EXISTS last_query_month INTEGER DEFAULT EXTRACT(MONTH FROM CURRENT_DATE);

-- 2. Atomik Kota Kontrolü ve Sayaç Artırma RPC Fonksiyonu
DROP FUNCTION IF EXISTS public.check_and_increment_agency_quota(UUID);

CREATE OR REPLACE FUNCTION public.check_and_increment_agency_quota(
    p_company_id UUID
)
RETURNS JSONB
SET search_path = public
AS $$
DECLARE
    v_company RECORD;
    v_curr_date DATE := CURRENT_DATE;
    v_curr_month INTEGER := EXTRACT(MONTH FROM CURRENT_DATE);
    v_today_queries INTEGER;
    v_month_queries INTEGER;
    v_daily_quota INTEGER;
    v_monthly_quota INTEGER;
BEGIN
    -- Şirket bilgilerini satır düzeyinde kilitle (Race condition önleme)
    SELECT * INTO v_company 
    FROM public.companies 
    WHERE id = p_company_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
            'allowed', true,
            'reason', 'COMPANY_NOT_FOUND',
            'message', 'Şirket kaydı bulunamadı (serbest geçiş).'
        );
    END IF;

    -- Şirket aktif değilse veya lisans süresi bitmişse
    IF v_company.is_active = false THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'reason', 'ACCOUNT_INACTIVE',
            'message', 'Acente hesabınız pasif durumdadır. Lütfen yöneticinizle iletişime geçin.'
        );
    END IF;

    v_today_queries := COALESCE(v_company.today_queries, 0);
    v_month_queries := COALESCE(v_company.current_month_queries, 0);
    v_daily_quota := COALESCE(v_company.daily_query_quota, 250);
    v_monthly_quota := COALESCE(v_company.monthly_query_quota, 5000);

    -- GÜN DEĞİŞİMİ KONTROLÜ (Gece 00:00 Otomatik Sıfırlama)
    IF v_company.last_query_date IS NULL OR v_company.last_query_date < v_curr_date THEN
        v_today_queries := 0;
    END IF;

    -- AY DEĞİŞİMİ KONTROLÜ (Ay Sonu 00:00 Otomatik Sıfırlama)
    IF v_company.last_query_month IS NULL OR v_company.last_query_month != v_curr_month THEN
        v_month_queries := 0;
    END IF;

    -- 1. GÜNLÜK KOTA KONTROLÜ
    IF v_today_queries >= v_daily_quota THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'reason', 'DAILY_QUOTA_EXCEEDED',
            'daily_quota', v_daily_quota,
            'today_queries', v_today_queries,
            'message', format('⚠️ Günlük arama limitinize (%s/%s) ulaştınız. Limitiniz bu gece 00:00''da otomatik olarak yenilenecektir.', v_today_queries, v_daily_quota)
        );
    END IF;

    -- 2. AYLIK KOTA KONTROLÜ
    IF v_month_queries >= v_monthly_quota THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'reason', 'MONTHLY_QUOTA_EXCEEDED',
            'monthly_quota', v_monthly_quota,
            'current_month_queries', v_month_queries,
            'message', format('⚠️ Aylık paket arama kotanız (%s/%s) dolmuştur. Yeni kota veya paket yükseltme için SaaS yöneticinizle görüşün.', v_month_queries, v_monthly_quota)
        );
    END IF;

    -- LİMİTLER UYGUN -> SAYAÇLARI +1 ARTIR VE GÜNCELLE
    UPDATE public.companies
    SET today_queries = v_today_queries + 1,
        current_month_queries = v_month_queries + 1,
        last_query_date = v_curr_date,
        last_query_month = v_curr_month,
        updated_at = NOW()
    WHERE id = p_company_id;

    RETURN jsonb_build_object(
        'allowed', true,
        'today_queries', v_today_queries + 1,
        'daily_quota', v_daily_quota,
        'remaining_daily', GREATEST(0, v_daily_quota - (v_today_queries + 1)),
        'current_month_queries', v_month_queries + 1,
        'monthly_quota', v_monthly_quota,
        'remaining_monthly', GREATEST(0, v_monthly_quota - (v_month_queries + 1))
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Acente Arama RPC Fonksiyonunun Günlük/Aylık Kolonlarla Güncellenmesi
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
        -- Gün değişmişse anlık 0 olarak göster
        CASE WHEN c.last_query_date < CURRENT_DATE THEN 0 ELSE COALESCE(c.today_queries, 0) END AS today_queries,
        COALESCE(c.monthly_query_quota, 5000) AS monthly_query_quota,
        -- Ay değişmişse anlık 0 olarak göster
        CASE WHEN c.last_query_month != EXTRACT(MONTH FROM CURRENT_DATE) THEN 0 ELSE COALESCE(c.current_month_queries, 0) END AS current_month_queries,
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

-- 4. Acente Lisans, Günlük & Aylık Kota Güncelleme RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.update_agency_subscription_and_quota(
    p_company_id UUID,
    p_is_active BOOLEAN,
    p_subscription_start_date TIMESTAMPTZ,
    p_subscription_end_date TIMESTAMPTZ,
    p_daily_query_quota INTEGER DEFAULT 250,
    p_today_queries INTEGER DEFAULT 0,
    p_monthly_query_quota INTEGER DEFAULT 5000,
    p_current_month_queries INTEGER DEFAULT 0
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
        updated_at = NOW()
    WHERE id = p_company_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

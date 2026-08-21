-- ============================================================================
-- TourOS Migration: 20260821_002_agency_billing_and_ledger.sql
-- DESCRIPTION: SaaS Acente Cari Hesabı, Otomatik Borçlandırma, Tahsilat ve Aç/Kapa Kilit Sistemi
-- ============================================================================

-- 1. Companies Tablosuna Cari & Abonelik Kolonlarının Eklenmesi
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS monthly_subscription_fee NUMERIC(12, 2) DEFAULT 2500.00,
ADD COLUMN IF NOT EXISTS current_balance NUMERIC(12, 2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS is_debt_locked BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS currency TEXT DEFAULT 'TRY';

-- 2. SaaS Sistem Yapılandırma Tablosu (Global Aç/Kapa Anahtarı için)
CREATE TABLE IF NOT EXISTS public.saas_system_config (
    config_key TEXT PRIMARY KEY,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Varsayılan olarak kilit modu aktif/pasif flag'i
INSERT INTO public.saas_system_config (config_key, config_value, description)
VALUES 
    ('auto_debt_lock_enabled', 'false', 'Borcu olan acentelerin otomatik kilitlenmesi modu (true=Açık, false=Pasif/İzleme)'),
    ('auto_monthly_billing_enabled', 'true', 'Ayın 1inde otomatik abonelik borcu tahakkuku (true=Açık, false=Kapalı)')
ON CONFLICT (config_key) DO NOTHING;

-- 3. Acente Cari Hareketleri Tablosu (Ledger Transactions)
CREATE TABLE IF NOT EXISTS public.agency_ledger_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    transaction_date TIMESTAMPTZ DEFAULT NOW(),
    transaction_type TEXT NOT NULL, -- 'DEBIT' (Borç/Fatura) veya 'CREDIT' (Alacak/Tahsilat)
    category TEXT NOT NULL, -- 'MONTHLY_SUBSCRIPTION', 'EXTRA_QUOTA', 'CUSTOM_SERVICE', 'BANK_TRANSFER', 'CREDIT_CARD', 'CASH'
    amount NUMERIC(12, 2) NOT NULL,
    balance_after NUMERIC(12, 2) NOT NULL,
    currency TEXT DEFAULT 'TRY',
    reference_no TEXT, -- Fatura No / Havale Dekont No / Pos Onay Kodu
    description TEXT NOT NULL,
    created_by TEXT DEFAULT 'SaaS Admin',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_agency_ledger_company ON public.agency_ledger_transactions(company_id);
CREATE INDEX IF NOT EXISTS idx_agency_ledger_date ON public.agency_ledger_transactions(transaction_date);

-- 4. Tahsilat Girişi & Borçtan Düşme RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.record_agency_payment(
    p_company_id UUID,
    p_amount NUMERIC(12, 2),
    p_payment_method TEXT, -- 'BANK_TRANSFER', 'CREDIT_CARD', 'CASH'
    p_reference_no TEXT,
    p_description TEXT,
    p_recorded_by TEXT DEFAULT 'SaaS Admin'
)
RETURNS JSONB
SET search_path = public
AS $$
DECLARE
    v_company RECORD;
    v_new_balance NUMERIC(12, 2);
    v_is_locked BOOLEAN;
    v_lock_config TEXT;
BEGIN
    -- Şirketi satır düzeyinde kilitle
    SELECT * INTO v_company FROM public.companies WHERE id = p_company_id FOR UPDATE;
    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'message', 'Acente kaydı bulunamadı.');
    END IF;

    -- Yeni bakiyeyi hesapla (Ödeme yapıldığı için bakiye azalır)
    v_new_balance := COALESCE(v_company.current_balance, 0.00) - p_amount;
    
    -- Global kilit kuralını oku
    SELECT config_value INTO v_lock_config FROM public.saas_system_config WHERE config_key = 'auto_debt_lock_enabled';
    
    -- Borç sıfırlandıysa veya negatifse (alacaklıysa) kilit anında açılır
    IF v_new_balance <= 0 THEN
        v_is_locked := false;
    ELSE
        v_is_locked := (COALESCE(v_lock_config, 'false') = 'true');
    END IF;

    -- Şirket tablosunu güncelle
    UPDATE public.companies
    SET current_balance = v_new_balance,
        is_debt_locked = v_is_locked,
        is_active = CASE WHEN v_is_locked THEN false ELSE is_active END,
        updated_at = NOW()
    WHERE id = p_company_id;

    -- Cari hareket kaydı oluştur
    INSERT INTO public.agency_ledger_transactions (
        company_id,
        transaction_type,
        category,
        amount,
        balance_after,
        currency,
        reference_no,
        description,
        created_by
    ) VALUES (
        p_company_id,
        'CREDIT',
        p_payment_method,
        p_amount,
        v_new_balance,
        COALESCE(v_company.currency, 'TRY'),
        p_reference_no,
        p_description,
        p_recorded_by
    );

    RETURN jsonb_build_object(
        'success', true,
        'new_balance', v_new_balance,
        'is_debt_locked', v_is_locked,
        'message', format('✅ %s TL tutarında tahsilat işlendi. Güncel Borç: %s TL', p_amount, GREATEST(0, v_new_balance))
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 5. Manuel Borç / Ekstra Fatura Girişi RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.record_agency_debit(
    p_company_id UUID,
    p_amount NUMERIC(12, 2),
    p_category TEXT, -- 'MONTHLY_SUBSCRIPTION', 'EXTRA_QUOTA', 'CUSTOM_SERVICE'
    p_reference_no TEXT,
    p_description TEXT,
    p_recorded_by TEXT DEFAULT 'SaaS Admin'
)
RETURNS JSONB
SET search_path = public
AS $$
DECLARE
    v_company RECORD;
    v_new_balance NUMERIC(12, 2);
    v_is_locked BOOLEAN;
    v_lock_config TEXT;
BEGIN
    SELECT * INTO v_company FROM public.companies WHERE id = p_company_id FOR UPDATE;
    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'message', 'Acente kaydı bulunamadı.');
    END IF;

    v_new_balance := COALESCE(v_company.current_balance, 0.00) + p_amount;
    SELECT config_value INTO v_lock_config FROM public.saas_system_config WHERE config_key = 'auto_debt_lock_enabled';
    
    v_is_locked := (COALESCE(v_lock_config, 'false') = 'true' AND v_new_balance > 0);

    UPDATE public.companies
    SET current_balance = v_new_balance,
        is_debt_locked = v_is_locked,
        is_active = CASE WHEN v_is_locked THEN false ELSE is_active END,
        updated_at = NOW()
    WHERE id = p_company_id;

    INSERT INTO public.agency_ledger_transactions (
        company_id,
        transaction_type,
        category,
        amount,
        balance_after,
        currency,
        reference_no,
        description,
        created_by
    ) VALUES (
        p_company_id,
        'DEBIT',
        p_category,
        p_amount,
        v_new_balance,
        COALESCE(v_company.currency, 'TRY'),
        p_reference_no,
        p_description,
        p_recorded_by
    );

    RETURN jsonb_build_object(
        'success', true,
        'new_balance', v_new_balance,
        'is_debt_locked', v_is_locked,
        'message', format('⚠️ %s TL tutarında borç kaydedildi. Güncel Bakiye: %s TL', p_amount, v_new_balance)
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 6. Aylık Otomatik Borç Tahakkuku Fonksiyonu (Ayın 1'inde Çalışır)
CREATE OR REPLACE FUNCTION public.apply_monthly_subscription_debts()
RETURNS JSONB
SET search_path = public
AS $$
DECLARE
    r RECORD;
    v_processed_count INTEGER := 0;
    v_total_debited NUMERIC(12, 2) := 0.00;
    v_period_name TEXT := to_char(NOW(), 'TMMonth YYYY');
BEGIN
    FOR r IN 
        SELECT id, name, monthly_subscription_fee, current_balance, currency
        FROM public.companies
        WHERE company_type = 'acente' 
          AND is_active = true 
          AND COALESCE(monthly_subscription_fee, 0) > 0
    LOOP
        PERFORM public.record_agency_debit(
            r.id,
            r.monthly_subscription_fee,
            'MONTHLY_SUBSCRIPTION',
            format('SUB-%s-%s', to_char(NOW(), 'YYYYMM'), SUBSTRING(r.id::text, 1, 6)),
            format('%s Dönemi Sabit Abonelik & Sorgu Paketi Bedeli', v_period_name),
            'SaaS Otomasyonu (Cron)'
        );
        v_processed_count := v_processed_count + 1;
        v_total_debited := v_total_debited + r.monthly_subscription_fee;
    END LOOP;

    RETURN jsonb_build_object(
        'success', true,
        'processed_agencies', v_processed_count,
        'total_amount', v_total_debited,
        'message', format('Toplam %s acenteye %s TL abonelik borcu tahakkuk ettirildi.', v_processed_count, v_total_debited)
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Borç Kilitli check_and_increment_agency_quota Fonksiyonu
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
    v_auto_lock TEXT;
BEGIN
    SELECT * INTO v_company 
    FROM public.companies 
    WHERE id = p_company_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('allowed', true, 'reason', 'COMPANY_NOT_FOUND', 'message', 'Şirket kaydı bulunamadı.');
    END IF;

    -- 0. BORÇ KİLİT KONTROLÜ (Global Switch Açık ve Borç Varsa Kilitle)
    SELECT config_value INTO v_auto_lock FROM public.saas_system_config WHERE config_key = 'auto_debt_lock_enabled';
    IF COALESCE(v_auto_lock, 'false') = 'true' AND COALESCE(v_company.current_balance, 0) > 0 THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'reason', 'DEBT_LOCKED',
            'current_balance', v_company.current_balance,
            'message', format('⛔ Ödenmemiş dönem borcunuz (%s TL) bulunmaktadır. Arama yapabilmek için lütfen ödemenizi tamamlayın.', v_company.current_balance)
        );
    END IF;

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

    IF v_company.last_query_date IS NULL OR v_company.last_query_date < v_curr_date THEN
        v_today_queries := 0;
    END IF;

    IF v_company.last_query_month IS NULL OR v_company.last_query_month != v_curr_month THEN
        v_month_queries := 0;
    END IF;

    -- 1. Günlük Limit Kontrolü
    IF v_today_queries >= v_daily_quota THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'reason', 'DAILY_QUOTA_EXCEEDED',
            'daily_quota', v_daily_quota,
            'today_queries', v_today_queries,
            'message', format('⚠️ Günlük arama limitinize (%s/%s) ulaştınız. Limitiniz bu gece 00:00''da yenilenecektir.', v_today_queries, v_daily_quota)
        );
    END IF;

    -- 2. Aylık Kota Kontrolü
    IF v_month_queries >= v_monthly_quota THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'reason', 'MONTHLY_QUOTA_EXCEEDED',
            'monthly_quota', v_monthly_quota,
            'current_month_queries', v_month_queries,
            'message', format('⚠️ Aylık paket arama kotanız (%s/%s) dolmuştur. Yeni kota için SaaS yöneticinizle görüşün.', v_month_queries, v_monthly_quota)
        );
    END IF;

    -- Sayaçları Artır
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

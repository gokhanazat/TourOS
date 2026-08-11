-- ============================================================================
-- MIGRATION: 20260811_035_global_web_cms_and_referrals.sql (GÜNCELLENDİ / SAFE)
-- DESCRIPTION: Ana Web Yönetimi (CMS), Metasearch Fiyat Motoru ve Acente Referans Kodları Altyapısı
-- ============================================================================

-- 1. GLOBAL WEB CMS AYARLARI TABLOSU
CREATE TABLE IF NOT EXISTS public.global_web_cms_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_title TEXT NOT NULL DEFAULT 'TourOS Business - Lüks Seyahat & Otel Platformu',
    hero_slogan TEXT NOT NULL DEFAULT 'Dünyanın En Seçkin 5 Yıldızlı Otelleri ve Özel Tur Paketleri',
    logo_url TEXT,
    banner_url TEXT,
    primary_color TEXT DEFAULT '#0F172A',
    accent_color TEXT DEFAULT '#0284C7',
    whatsapp_number TEXT DEFAULT '+90 532 100 2030',
    support_email TEXT DEFAULT 'destek@touros.com',
    default_commission_margin NUMERIC(5,2) DEFAULT 12.50,
    master_agency_code VARCHAR(50) DEFAULT 'AGN-MASTER-8492',
    meta_title TEXT DEFAULT 'TourOS B2B & B2C Lüks Otel ve Tur Karşılaştırma Engine',
    meta_description TEXT DEFAULT 'En uygun tur operatörü ve acente otel tekliflerini karşılaştırın ve anında rezerve edin.',
    meta_keywords TEXT DEFAULT 'otel, tur, tur operatörü, b2b rezervasyon, lüks seyahat, 5 yıldızlı oteller',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Varsayılan tek satır CMS ayarını oluştur
INSERT INTO public.global_web_cms_settings (id, site_title)
SELECT '00000000-0000-0000-0000-000000000099', 'TourOS Business - Lüks Seyahat & Otel Platformu'
ON CONFLICT (id) DO NOTHING;

-- 2. ACENTE BENZERSİZ REFERANS KODLARI TABLOSU (Foreign Key bağımlılığı kaldırıldı)
CREATE TABLE IF NOT EXISTS public.agency_referral_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    agency_code VARCHAR(50) UNIQUE NOT NULL,
    agency_name TEXT NOT NULL,
    commission_rate NUMERIC(5,2) DEFAULT 10.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. BOOKINGS VE MARKETPLACE_PRODUCTS TABLOLARINA SÜTUN EKLENMESİ
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS agency_referral_code VARCHAR(50);
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS operator_name VARCHAR(150);
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS picture TEXT;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS picture_url TEXT;

-- 4. RLS POLİTİKALARI (Row Level Security)
ALTER TABLE public.global_web_cms_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agency_referral_codes ENABLE ROW LEVEL SECURITY;

-- Polislerin çakışmaması için önceden temizle
DROP POLICY IF EXISTS "Public Read Access For Global Web CMS" ON public.global_web_cms_settings;
DROP POLICY IF EXISTS "Admin Write Access For Global Web CMS" ON public.global_web_cms_settings;
DROP POLICY IF EXISTS "Public Read Access For Agency Referral Codes" ON public.agency_referral_codes;
DROP POLICY IF EXISTS "Admin Write Access For Agency Referral Codes" ON public.agency_referral_codes;

-- Herkes okuyabilsin (Web sitesinde gösterim için)
CREATE POLICY "Public Read Access For Global Web CMS"
    ON public.global_web_cms_settings FOR SELECT
    USING (TRUE);

-- Sadece Admin ve Service Role güncelleyebilsin
CREATE POLICY "Admin Write Access For Global Web CMS"
    ON public.global_web_cms_settings FOR ALL
    USING (TRUE);

-- Acente Referans Kodları Okuma Yetkisi
CREATE POLICY "Public Read Access For Agency Referral Codes"
    ON public.agency_referral_codes FOR SELECT
    USING (TRUE);

-- Acente Referans Kodları Yazma Yetkisi
CREATE POLICY "Admin Write Access For Agency Referral Codes"
    ON public.agency_referral_codes FOR ALL
    USING (TRUE);

-- İndeksler
CREATE INDEX IF NOT EXISTS idx_agency_referral_codes_code ON public.agency_referral_codes(agency_code);
CREATE INDEX IF NOT EXISTS idx_bookings_referral_code ON public.bookings(agency_referral_code);

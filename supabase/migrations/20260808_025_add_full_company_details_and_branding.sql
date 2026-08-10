-- ============================================================
-- TourOS Migration: 20260808_025_add_full_company_details_and_branding.sql
-- Adds missing general company information and tax/branding columns to companies & agency_branding tables.
-- Guarantees default company seed row with valid operator_code, slug default, and fixes RLS write access.
-- ============================================================

-- 1. COMPANIES TABLOSUNA GENEL FİRMA BİLGİLERİ SÜTUNLARINI VE DEFAULTEKLE
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS legal_title TEXT,
ADD COLUMN IF NOT EXISTS tax_office TEXT,
ADD COLUMN IF NOT EXISTS tax_number TEXT,
ADD COLUMN IF NOT EXISTS trade_registry_no TEXT,
ADD COLUMN IF NOT EXISTS mersis_no TEXT,
ADD COLUMN IF NOT EXISTS address TEXT,
ADD COLUMN IF NOT EXISTS phone TEXT,
ADD COLUMN IF NOT EXISTS email TEXT,
ADD COLUMN IF NOT EXISTS operator_code TEXT;

ALTER TABLE public.companies 
ALTER COLUMN slug SET DEFAULT 'default-company';

-- 2. AGENCY_BRANDING TABLOSUNA İLETİŞİM TELEFONU EKLE
ALTER TABLE public.agency_branding
ADD COLUMN IF NOT EXISTS contact_phone TEXT;

-- 3. VARSAYILAN ŞİRKET SEED KAYDINI OLUŞTUR / GÜNCELLE (CHECK KISITI UYUMLU)
INSERT INTO public.companies (id, name, slug, tenant_id, company_type, operator_code, is_active)
VALUES ('00000000-0000-0000-0000-000000000001', 'TourOS Acente', 'default-company', '00000000-0000-0000-0000-000000000001', 'acente', 'ACT', true)
ON CONFLICT (id) DO UPDATE SET 
    company_type = 'acente',
    operator_code = COALESCE(public.companies.operator_code, 'ACT');

-- 4. COMPANIES TABLOSU RLS İZİNLERİ
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "companies_allow_all" ON public.companies;
CREATE POLICY "companies_allow_all" ON public.companies FOR ALL USING (true) WITH CHECK (true);

-- 5. AGENCY_BRANDING TABLOSU RLS İZİNLERİ
ALTER TABLE public.agency_branding ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "agency_branding_allow_all" ON public.agency_branding;
CREATE POLICY "agency_branding_allow_all" ON public.agency_branding FOR ALL USING (true) WITH CHECK (true);

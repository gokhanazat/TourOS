-- ============================================================================
-- FULL FIX FOR CMS, BRANDING & COMPANY SETTINGS PERSISTENCE
-- ============================================================================

-- 1. Ensure all columns exist in `companies`
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS bank_name TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS iban TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS account_holder TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS paypal_email TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS paypal_me_url TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS theme_color TEXT DEFAULT '#1F4E5F';
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS tax_rate NUMERIC DEFAULT 20.0;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS seasons JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS supported_currencies JSONB DEFAULT '["TRY", "EUR", "USD"]'::jsonb;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS supported_languages JSONB DEFAULT '["tr", "en", "ru", "de"]'::jsonb;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS header_image_url TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS hero_subtitle TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS footer_text TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_phone TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_whatsapp TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_email TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_address TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_mersis_no TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_tax_office TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS web_tax_number TEXT;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS promo_banners JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS service_cards JSONB DEFAULT '[]'::jsonb;

-- 2. Ensure all columns exist in `agency_branding`
ALTER TABLE public.agency_branding ADD COLUMN IF NOT EXISTS service_cards JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.agency_branding ADD COLUMN IF NOT EXISTS web_mersis_no TEXT;
ALTER TABLE public.agency_branding ADD COLUMN IF NOT EXISTS web_tax_office TEXT;
ALTER TABLE public.agency_branding ADD COLUMN IF NOT EXISTS web_tax_number TEXT;
ALTER TABLE public.agency_branding ADD COLUMN IF NOT EXISTS meta_description TEXT;
ALTER TABLE public.agency_branding ADD COLUMN IF NOT EXISTS logo_url TEXT;

-- 3. RLS Check
ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agency_branding ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow all access to companies') THEN
        CREATE POLICY "Allow all access to companies" ON public.companies FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow all access to agency_branding') THEN
        CREATE POLICY "Allow all access to agency_branding" ON public.agency_branding FOR ALL USING (true) WITH CHECK (true);
    END IF;
END $$;

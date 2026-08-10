-- ============================================================
-- TourOS Migration: 20260810_029_fix_agency_branding_upsert_and_seed.sql
-- Fixes agency_branding upsert constraint, removes hardcoded seed defaults, and clears dummy phone numbers.
-- ============================================================

-- 1. agency_branding tablosunda varsayılan acente için seed kaydı oluştur (hardcoded varsayılan metinler olmadan)
INSERT INTO public.agency_branding (
    id,
    agency_id,
    hero_title,
    hero_subtitle,
    footer_text
)
VALUES (
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000001'::uuid,
    '',
    '',
    ''
)
ON CONFLICT (agency_id) DO NOTHING;

-- 2. Hardcoded test numaralarını sıfırla
UPDATE public.agency_branding
SET contact_phone = NULL 
WHERE contact_phone LIKE '%5320000000%';

UPDATE public.agency_branding
SET whatsapp_number = NULL 
WHERE whatsapp_number LIKE '%5320000000%';

UPDATE public.companies
SET phone = NULL 
WHERE phone LIKE '%5320000000%';

-- TourOS Migration: 20260819_013_add_app_download_urls_to_cms.sql
-- Adds desktop (.exe) and android (.apk) download URLs and visibility toggle to companies and agency_branding tables.

ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS desktop_app_url TEXT,
ADD COLUMN IF NOT EXISTS android_apk_url TEXT,
ADD COLUMN IF NOT EXISTS is_app_download_active BOOLEAN DEFAULT true;

ALTER TABLE public.agency_branding 
ADD COLUMN IF NOT EXISTS desktop_app_url TEXT,
ADD COLUMN IF NOT EXISTS android_apk_url TEXT,
ADD COLUMN IF NOT EXISTS is_app_download_active BOOLEAN DEFAULT true;

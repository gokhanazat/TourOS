-- ==============================================================================
-- Migration: 20260813_003_add_promo_banners_to_cms_and_branding.sql
-- Description: CMS ve Acente Branding Tablolarına promo_banners JSON Sütunu Ekleme
-- ==============================================================================

-- 1. agency_branding tablosuna promo_banners JSONB sütunu ekle
ALTER TABLE public.agency_branding 
ADD COLUMN IF NOT EXISTS promo_banners JSONB DEFAULT '[]'::jsonb;

-- 2. global_web_cms_settings tablosuna promo_banners JSONB sütunu ekle
ALTER TABLE public.global_web_cms_settings 
ADD COLUMN IF NOT EXISTS promo_banners JSONB DEFAULT '[]'::jsonb;

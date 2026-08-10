-- ============================================================
-- TourOS Migration: 20260809_026_add_agency_web_contact_and_whatsapp.sql
-- Adds agency storefront contact email, whatsapp number, and contact address columns to agency_branding table.
-- ============================================================

ALTER TABLE public.agency_branding
ADD COLUMN IF NOT EXISTS contact_email TEXT,
ADD COLUMN IF NOT EXISTS whatsapp_number TEXT,
ADD COLUMN IF NOT EXISTS contact_address TEXT;

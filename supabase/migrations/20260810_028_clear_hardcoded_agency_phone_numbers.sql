-- ============================================================
-- TourOS Migration / Fix Script: 20260810_028_clear_hardcoded_agency_phone_numbers.sql
-- Resets any existing test/dummy phone numbers (e.g. 905320000000) in agency_branding table so agencies can set clean contact info.
-- ============================================================

UPDATE public.agency_branding
SET 
    contact_phone = NULL,
    whatsapp_number = NULL
WHERE contact_phone = '905320000000' OR whatsapp_number = '905320000000';

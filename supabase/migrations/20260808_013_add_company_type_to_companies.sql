-- ============================================================
-- TourOS Migration: 20260808_013_add_company_type_to_companies.sql
-- Prompt 4.6.1: company_type ve Tenant Modeli Güncellemesi
-- companies tablosuna company_type (tur_operatoru / acente) kolonu eklenir.
-- ============================================================

ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS company_type TEXT NOT NULL DEFAULT 'tur_operatoru';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.constraint_column_usage 
        WHERE constraint_name = 'chk_companies_company_type' AND table_name = 'companies'
    ) THEN
        ALTER TABLE public.companies 
        ADD CONSTRAINT chk_companies_company_type 
        CHECK (company_type IN ('tur_operatoru', 'acente'));
    END IF;
END $$;

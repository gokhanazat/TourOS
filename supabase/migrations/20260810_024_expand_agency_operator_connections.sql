-- ============================================================
-- TourOS Migration: 20260810_024_expand_agency_operator_connections.sql
-- Tur Operatörü Acente Bağlantıları Tablosu Genişletmesi
-- Acente perspektifinden 5-Adımlı Wizard verileri (Marka adı, API, Banka, İletişim vb.)
-- ============================================================

ALTER TABLE public.agency_operator_connections
    ADD COLUMN IF NOT EXISTS operator_name       TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS operator_logo       TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS operator_type       TEXT DEFAULT 'GLOBAL',
    ADD COLUMN IF NOT EXISTS integration_type    TEXT DEFAULT 'API',
    ADD COLUMN IF NOT EXISTS api_endpoint        TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS api_key             TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS currency            TEXT DEFAULT 'TRY',
    ADD COLUMN IF NOT EXISTS tax_office          TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS tax_number          TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS iban                TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS bank_name           TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS contact_name        TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS contact_phone       TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS contact_email       TEXT DEFAULT '';

-- Migration: 20260809_038_create_invoices_table_and_indexes.sql
-- Description: Creates invoices table, RLS policies, and performance indexes for e-Invoice management

CREATE TABLE IF NOT EXISTS "public"."invoices" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "invoice_no" VARCHAR(100) NOT NULL UNIQUE,
    "booking_id" VARCHAR(100),
    "invoice_type" VARCHAR(50) DEFAULT 'sale',
    "customer_name" VARCHAR(255) NOT NULL,
    "customer_tax_no" VARCHAR(50),
    "subtotal" NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    "tax_rate" NUMERIC(5, 2) NOT NULL DEFAULT 20.00,
    "tax_amount" NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    "total_amount" NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    "currency" VARCHAR(10) DEFAULT 'TRY',
    "status" VARCHAR(50) DEFAULT 'issued',
    "issued_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    "due_date" DATE,
    "notes" TEXT,
    "tenant_id" VARCHAR(100) NOT NULL,
    "created_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS "idx_invoices_tenant_id" ON "public"."invoices" ("tenant_id");
CREATE INDEX IF NOT EXISTS "idx_invoices_booking_id" ON "public"."invoices" ("booking_id");
CREATE INDEX IF NOT EXISTS "idx_invoices_customer_name" ON "public"."invoices" ("customer_name");
CREATE INDEX IF NOT EXISTS "idx_invoices_issued_at" ON "public"."invoices" ("issued_at" DESC);

-- Enable RLS
ALTER TABLE "public"."invoices" ENABLE ROW LEVEL SECURITY;

-- Service Role Policy
CREATE POLICY "Enable all for authenticated tenant" ON "public"."invoices"
    FOR ALL USING (auth.role() = 'authenticated' OR tenant_id IS NOT NULL);

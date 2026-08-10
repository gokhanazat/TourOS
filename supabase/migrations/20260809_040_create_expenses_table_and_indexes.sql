-- Migration: 20260809_040_create_expenses_table_and_indexes.sql
-- Description: Creates expenses table, RLS policies, and performance indexes for supplier expenses and operational costs

CREATE TABLE IF NOT EXISTS "public"."expenses" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "category" VARCHAR(100) NOT NULL,
    "amount" NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    "currency" VARCHAR(10) DEFAULT 'TRY',
    "description" TEXT NOT NULL,
    "reference_no" VARCHAR(100),
    "expense_date" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    "tenant_id" VARCHAR(100) NOT NULL,
    "created_at" TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS "idx_expenses_tenant_id" ON "public"."expenses" ("tenant_id");
CREATE INDEX IF NOT EXISTS "idx_expenses_category" ON "public"."expenses" ("category");
CREATE INDEX IF NOT EXISTS "idx_expenses_expense_date" ON "public"."expenses" ("expense_date" DESC);

-- Enable RLS
ALTER TABLE "public"."expenses" ENABLE ROW LEVEL SECURITY;

-- Service Role Policy
CREATE POLICY "Enable all for authenticated tenant expenses" ON "public"."expenses"
    FOR ALL USING (auth.role() = 'authenticated' OR tenant_id IS NOT NULL);

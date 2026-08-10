-- Migration: 20260809_033_create_crm_customer_views_and_indexes.sql
-- Description: Creates performance indexes and CRM customer segmentation views for dynamic LTV calculation

-- 1. Müşteri sorguları ve LTV analizleri için performans indeksleri
CREATE INDEX IF NOT EXISTS "idx_bookings_customer_phone" 
  ON "public"."bookings" ("tenant_id", "customer_phone");

CREATE INDEX IF NOT EXISTS "idx_bookings_customer_email" 
  ON "public"."bookings" ("tenant_id", "customer_email");

-- 2. Canlı Müşteri LTV ve Segmentasyon Görünümü (View)
CREATE OR REPLACE VIEW "public"."v_customer_crm_segmentation" AS
SELECT 
    tenant_id,
    COALESCE(NULLIF(customer_phone, ''), NULLIF(customer_email, ''), customer_name) AS customer_key,
    MAX(customer_name) AS customer_name,
    MAX(customer_email) AS customer_email,
    MAX(customer_phone) AS customer_phone,
    COUNT(id) AS total_bookings,
    SUM(total_price) AS total_ltv_amount,
    AVG(total_price) AS avg_booking_amount,
    MAX(created_at) AS last_activity_date
FROM "public"."bookings"
GROUP BY tenant_id, COALESCE(NULLIF(customer_phone, ''), NULLIF(customer_email, ''), customer_name);

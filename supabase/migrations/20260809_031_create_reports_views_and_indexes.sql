-- Migration: 20260809_031_create_reports_views_and_indexes.sql
-- Description: Creates performance indexes and reporting views for Tour & Hotel Operations, Operator Sales, and Financial Reports

-- 1. Indexing for fast analytical query filtering
CREATE INDEX IF NOT EXISTS "idx_bookings_report_filter" 
  ON "public"."bookings" ("tenant_id", "booking_type", "status", "check_in_date", "departure_date");

CREATE INDEX IF NOT EXISTS "idx_bookings_operator_report" 
  ON "public"."bookings" ("tenant_id", "operator_name");

-- 2. View: Summary report for bookings by type and operator
CREATE OR REPLACE VIEW "public"."v_reports_summary" AS
SELECT 
    tenant_id,
    COALESCE(booking_type, 'OTHER') AS booking_type,
    COALESCE(operator_name, 'Kendi Ürünlerimiz') AS operator_name,
    status,
    COUNT(id) AS total_bookings,
    SUM(total_price) AS total_revenue,
    AVG(total_price) AS avg_booking_value,
    SUM(COALESCE(nights, pax_count, 1)) AS total_volume
FROM "public"."bookings"
GROUP BY tenant_id, booking_type, operator_name, status;

-- Migration: 20260809_035_create_analytics_trend_views.sql
-- Description: Creates SQL analytics views for daily sales trends and product category distributions

-- 1. Günlük Satış Analitik SQL Görünümü (COALESCE Tip Uyumlu)
CREATE OR REPLACE VIEW "public"."v_daily_sales_analytics" AS
SELECT 
    tenant_id,
    COALESCE(check_in_date::text, departure_date::text, DATE(created_at)::text) AS sale_date,
    COUNT(id) AS booking_count,
    SUM(total_price) AS total_sales,
    AVG(total_price) AS avg_booking_amount
FROM "public"."bookings"
GROUP BY tenant_id, COALESCE(check_in_date::text, departure_date::text, DATE(created_at)::text)
ORDER BY sale_date DESC;

-- 2. Kategori / Operasyon Dağılımı SQL Görünümü
CREATE OR REPLACE VIEW "public"."v_category_sales_analytics" AS
SELECT 
    tenant_id,
    booking_type,
    COALESCE(product_name, 'Genel Operasyon') AS product_name,
    COUNT(id) AS booking_count,
    SUM(total_price) AS total_sales
FROM "public"."bookings"
GROUP BY tenant_id, booking_type, COALESCE(product_name, 'Genel Operasyon');

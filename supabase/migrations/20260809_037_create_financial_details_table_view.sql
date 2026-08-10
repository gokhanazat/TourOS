-- Migration: 20260809_037_create_financial_details_table_view.sql
-- Description: Creates SQL view for real-time financial detailed items table (COALESCE text cast type-safe)

CREATE OR REPLACE VIEW "public"."v_financial_details_table" AS
SELECT 
    b.tenant_id,
    COALESCE(b.check_in_date::text, b.departure_date::text, DATE(b.created_at)::text) AS transaction_date,
    CONCAT(b.customer_name, ' - ', b.product_name, ' Satış Faturası') AS description,
    CASE WHEN b.booking_type = 'HOTEL' THEN 'Otel Tahsilatı' ELSE 'Tur Tahsilatı' END AS category,
    ROUND((b.total_price / 1.20)::numeric, 2) AS subtotal,
    ROUND((b.total_price - (b.total_price / 1.20))::numeric, 2) AS vat_amount,
    b.total_price AS total_amount,
    b.currency
FROM "public"."bookings" b
ORDER BY transaction_date DESC;

-- Migration: 20260809_034_create_at_risk_customers_view.sql
-- Description: Pure SQL View for identifying At-Risk / Inactive Customers in Supabase

CREATE OR REPLACE VIEW "public"."v_at_risk_customers" AS
SELECT 
    tenant_id,
    COALESCE(NULLIF(customer_phone, ''), NULLIF(customer_email, ''), customer_name) AS customer_key,
    MAX(customer_name) AS customer_name,
    MAX(customer_email) AS customer_email,
    MAX(customer_phone) AS customer_phone,
    COUNT(id) AS total_bookings,
    SUM(CASE WHEN status = 'İptal' THEN 1 ELSE 0 END) AS cancelled_bookings,
    SUM(CASE WHEN status = 'Bekliyor' THEN 1 ELSE 0 END) AS pending_bookings,
    SUM(total_price) AS total_ltv_amount,
    MAX(created_at) AS last_activity_date,
    CASE 
        WHEN MAX(created_at) < (NOW() - INTERVAL '180 days') THEN '180+ Gün İnaktif'
        WHEN SUM(CASE WHEN status = 'İptal' THEN 1 ELSE 0 END) > 0 THEN 'Yüksek İptal Riski'
        WHEN SUM(CASE WHEN status = 'Bekliyor' THEN 1 ELSE 0 END) > 0 THEN 'Ödeme Bekleyen Bakiye'
        ELSE 'Riskli'
    END AS risk_reason
FROM "public"."bookings"
GROUP BY tenant_id, COALESCE(NULLIF(customer_phone, ''), NULLIF(customer_email, ''), customer_name)
HAVING 
    MAX(created_at) < (NOW() - INTERVAL '180 days')
    OR SUM(CASE WHEN status = 'İptal' THEN 1 ELSE 0 END) > 0
    OR SUM(CASE WHEN status = 'Bekliyor' THEN 1 ELSE 0 END) > 0;

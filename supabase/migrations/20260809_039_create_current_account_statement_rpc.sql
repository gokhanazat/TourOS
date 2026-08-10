-- Migration: 20260809_039_create_current_account_statement_rpc.sql
-- Description: Creates current account statement view and PostgreSQL function supporting unique account code and tax no (using customer_phone and customer_email)

CREATE OR REPLACE VIEW "public"."v_current_accounts" AS
SELECT 
    b.tenant_id,
    COALESCE(b.customer_name, 'Bilinmeyen Müşteri') AS entity_name,
    'customer' AS entity_type,
    CONCAT('CAR-CUST-', SUBSTRING(MD5(COALESCE(b.customer_name, 'cust')) FROM 1 FOR 6)) AS account_code,
    '11111111111' AS tax_no,
    COALESCE(b.customer_phone, '—') AS phone,
    COALESCE(b.customer_email, '—') AS email,
    SUM(b.total_price) AS total_debit,
    0.00 AS total_credit,
    SUM(b.total_price) AS balance,
    b.currency,
    MAX(DATE(b.created_at)::text) AS last_transaction_date
FROM "public"."bookings" b
GROUP BY b.tenant_id, b.customer_name, b.customer_phone, b.customer_email, b.currency;

-- PostgreSQL Function for RPC current accounts
CREATE OR REPLACE FUNCTION "public"."get_current_account_statement"(
    p_tenant_id VARCHAR,
    p_entity_type VARCHAR DEFAULT NULL
)
RETURNS TABLE (
    entity_id VARCHAR,
    account_code VARCHAR,
    tax_no VARCHAR,
    entity_name VARCHAR,
    entity_type VARCHAR,
    phone VARCHAR,
    email VARCHAR,
    total_debit NUMERIC,
    total_credit NUMERIC,
    balance NUMERIC,
    currency VARCHAR,
    last_transaction_date VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ca.account_code AS entity_id,
        ca.account_code,
        ca.tax_no,
        ca.entity_name,
        ca.entity_type,
        ca.phone,
        ca.email,
        ca.total_debit::numeric,
        ca.total_credit::numeric,
        ca.balance::numeric,
        ca.currency,
        ca.last_transaction_date
    FROM "public"."v_current_accounts" ca
    WHERE ca.tenant_id = p_tenant_id
      AND (p_entity_type IS NULL OR ca.entity_type = p_entity_type);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

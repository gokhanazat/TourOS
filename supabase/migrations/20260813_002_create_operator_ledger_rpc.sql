-- ==============================================================================
-- Migration: 20260813_002_create_operator_ledger_rpc.sql
-- Description: Tur Operatörü PNR Cari Ekstresi ve Raporlama RPC Fonksiyonu
-- ==============================================================================

CREATE OR REPLACE FUNCTION public.get_operator_ledger_report(
    p_operator_name TEXT DEFAULT NULL,
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL
)
RETURNS TABLE (
    operator_pnr_code VARCHAR(50),
    customer_name VARCHAR(255),
    booking_code VARCHAR(50),
    operator_name VARCHAR(100),
    total_sales NUMERIC(14,2),
    total_paid NUMERIC(14,2),
    balance NUMERIC(14,2),
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(b.operator_pnr_code, '-') AS operator_pnr_code,
        b.customer_name,
        b.booking_code,
        COALESCE(b.operator_name, 'Kendi Ürünümüz') AS operator_name,
        COALESCE(b.total_price, 0.00) AS total_sales,
        COALESCE(SUM(cat.amount) FILTER (WHERE cat.transaction_type = 'DEBIT'), 0.00) AS total_paid,
        (COALESCE(b.total_price, 0.00) - COALESCE(SUM(cat.amount) FILTER (WHERE cat.transaction_type = 'DEBIT'), 0.00)) AS balance,
        b.created_at
    FROM public.bookings b
    LEFT JOIN public.current_account_transactions cat 
        ON cat.operator_pnr_code = b.operator_pnr_code AND cat.operator_pnr_code IS NOT NULL AND cat.operator_pnr_code <> '-'
    WHERE (p_operator_name IS NULL OR p_operator_name = '' OR p_operator_name = 'Tümü' OR b.operator_name ILIKE '%' || p_operator_name || '%')
      AND (p_start_date IS NULL OR b.created_at::DATE >= p_start_date)
      AND (p_end_date IS NULL OR b.created_at::DATE <= p_end_date)
    GROUP BY b.id, b.operator_pnr_code, b.customer_name, b.booking_code, b.operator_name, b.total_price, b.created_at
    ORDER BY b.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

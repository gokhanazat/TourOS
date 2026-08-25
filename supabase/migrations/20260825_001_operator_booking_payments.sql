-- ==============================================================================
-- Migration: 20260825_001_operator_booking_payments.sql
-- Description: Tur Operatörü Rezervasyon Ödemeleri, TO PNR Takibi ve Dekont Yönetimi
-- ==============================================================================

CREATE TABLE IF NOT EXISTS public.operator_booking_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES public.companies(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES public.bookings(id) ON DELETE CASCADE,
    operator_name TEXT NOT NULL,
    operator_pnr_code TEXT NOT NULL,
    amount NUMERIC(12,2) NOT NULL DEFAULT 0.0,
    currency TEXT NOT NULL DEFAULT 'EUR',
    payment_method TEXT NOT NULL DEFAULT 'BANK_TRANSFER', -- BANK_TRANSFER, CREDIT_CARD, CURRENT_ACCOUNT, CASH
    receipt_number TEXT,
    notes TEXT,
    paid_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Indeksler
CREATE INDEX IF NOT EXISTS idx_op_pay_company ON public.operator_booking_payments(company_id);
CREATE INDEX IF NOT EXISTS idx_op_pay_booking ON public.operator_booking_payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_op_pay_pnr ON public.operator_booking_payments(operator_pnr_code);
CREATE INDEX IF NOT EXISTS idx_op_pay_paid_at ON public.operator_booking_payments(paid_at);

-- RLS Güvenlik Politikaları
ALTER TABLE public.operator_booking_payments ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "operator_booking_payments_company_isolation" ON public.operator_booking_payments;
CREATE POLICY "operator_booking_payments_company_isolation"
    ON public.operator_booking_payments
    FOR ALL
    USING (
        company_id IS NULL OR 
        company_id::text = current_setting('app.current_company_id', true) OR
        auth.role() = 'service_role' OR
        company_id = '00000000-0000-0000-0000-000000000001'::uuid
    );

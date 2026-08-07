-- ============================================================
-- TourOS 3.2.2 Cash/Card/Wire & Partial Payment (Deposit) RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.process_booking_partial_payment(
    p_booking_id UUID,
    p_payment_method TEXT, -- cash | credit_card | bank_transfer | online
    p_amount NUMERIC(14,2),
    p_account_id UUID DEFAULT NULL,
    p_reference_no TEXT DEFAULT NULL,
    p_notes TEXT DEFAULT NULL,
    p_tenant_id UUID DEFAULT NULL
)
RETURNS TABLE (
    payment_id UUID,
    booking_id UUID,
    total_price NUMERIC(14,2),
    total_paid NUMERIC(14,2),
    remaining_balance NUMERIC(14,2),
    payment_status TEXT
)
SET search_path = public
AS $$
DECLARE
    v_total_price NUMERIC(14,2);
    v_existing_paid NUMERIC(14,2);
    v_new_total_paid NUMERIC(14,2);
    v_remaining NUMERIC(14,2);
    v_invoice_id UUID;
    v_new_payment_id UUID;
    v_status TEXT;
BEGIN
    -- Rezervasyon toplam tutarını al
    SELECT b.total_price INTO v_total_price
    FROM public.bookings b
    WHERE b.id = p_booking_id AND b.tenant_id = p_tenant_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Rezervasyon bulunamadı veya yetkisiz erişim.';
    END IF;

    -- İlişkili faturayı bul veya oluştur
    SELECT id INTO v_invoice_id
    FROM public.invoices
    WHERE booking_id = p_booking_id AND tenant_id = p_tenant_id
    LIMIT 1;

    IF v_invoice_id IS NULL THEN
        INSERT INTO public.invoices (
            invoice_no, booking_id, customer_name, subtotal, tax_rate, tax_amount, total_amount, status, tenant_id
        ) SELECT 
            'INV-' || b.booking_code, b.id, b.customer_name, ROUND((b.total_price/1.20)::numeric,2), 20.00, b.total_price - ROUND((b.total_price/1.20)::numeric,2), b.total_price, 'issued', p_tenant_id
        FROM public.bookings b WHERE b.id = p_booking_id
        RETURNING id INTO v_invoice_id;
    END IF;

    -- Ödeme kaydını oluştur
    INSERT INTO public.payments (
        invoice_id, account_id, amount, currency, payment_method, payment_date, reference_no, notes, tenant_id
    ) VALUES (
        v_invoice_id, p_account_id, p_amount, 'TRY', p_payment_method, NOW(), p_reference_no, p_notes, p_tenant_id
    ) RETURNING id INTO v_new_payment_id;

    -- Toplam ödenen tutarı hesapla
    SELECT COALESCE(SUM(p.amount), 0) INTO v_existing_paid
    FROM public.payments p
    WHERE p.invoice_id = v_invoice_id AND p.tenant_id = p_tenant_id;

    v_new_total_paid := v_existing_paid;
    v_remaining := GREATEST(0, v_total_price - v_new_total_paid);

    IF v_remaining <= 0 THEN
        v_status := 'PAID';
    ELSIF v_new_total_paid > 0 THEN
        v_status := 'PARTIALLY_PAID';
    ELSE
        v_status := 'UNPAID';
    END IF;

    -- Rezervasyondaki ödeme durumunu güncelle
    UPDATE public.bookings
    SET notes = COALESCE(notes, '') || ' [Ödeme Durumu: ' || v_status || ' - Ödenen: ' || v_new_total_paid::text || ' TRY, Kalan: ' || v_remaining::text || ' TRY]'
    WHERE id = p_booking_id AND tenant_id = p_tenant_id;

    RETURN QUERY
    SELECT v_new_payment_id, p_booking_id, v_total_price, v_new_total_paid, v_remaining, v_status;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

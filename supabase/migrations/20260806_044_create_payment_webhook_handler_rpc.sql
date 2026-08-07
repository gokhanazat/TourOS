-- ============================================================
-- TourOS 3.2.4 Webhook/Callback Sync RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.handle_payment_webhook_callback(
    p_payment_link_code TEXT,
    p_transaction_id TEXT,
    p_gateway_provider TEXT DEFAULT 'stripe',
    p_event_type TEXT DEFAULT 'payment_intent.succeeded',
    p_payload_json JSONB DEFAULT '{}'::jsonb
)
RETURNS TABLE (
    link_id UUID,
    booking_id UUID,
    invoice_id UUID,
    paid_amount NUMERIC(14,2),
    sync_status TEXT
)
SET search_path = public
AS $$
DECLARE
    v_link RECORD;
    v_invoice_id UUID;
    v_payment_id UUID;
BEGIN
    -- Ödeme linkini bul
    SELECT * INTO v_link
    FROM public.payment_links
    WHERE payment_link_code = p_payment_link_code;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ödeme linki bulunamadı: %', p_payment_link_code;
    END IF;

    -- Link durumunu ÖDENDİ olarak güncelle
    UPDATE public.payment_links
    SET status = 'PAID', paid_at = NOW()
    WHERE id = v_link.id;

    -- Faturayı bul veya oluştur
    SELECT id INTO v_invoice_id
    FROM public.invoices
    WHERE booking_id = v_link.booking_id AND tenant_id = v_link.tenant_id
    LIMIT 1;

    IF v_invoice_id IS NULL THEN
        INSERT INTO public.invoices (
            invoice_no, booking_id, customer_name, subtotal, tax_rate, tax_amount, total_amount, status, tenant_id
        ) SELECT 
            'INV-WH-' || b.booking_code, b.id, b.customer_name, ROUND((v_link.amount/1.20)::numeric,2), 20.00, v_link.amount - ROUND((v_link.amount/1.20)::numeric,2), v_link.amount, 'paid', v_link.tenant_id
        FROM public.bookings b WHERE b.id = v_link.booking_id
        RETURNING id INTO v_invoice_id;
    ELSE
        UPDATE public.invoices SET status = 'paid', updated_at = NOW() WHERE id = v_invoice_id;
    END IF;

    -- Ödeme tablosuna işle
    INSERT INTO public.payments (
        invoice_id, amount, currency, payment_method, payment_date, reference_no, notes, tenant_id
    ) VALUES (
        v_invoice_id, v_link.amount, v_link.currency, p_gateway_provider, NOW(), p_transaction_id, 'Webhook Senkronizasyonu (' || p_event_type || ')', v_link.tenant_id
    ) RETURNING id INTO v_payment_id;

    -- Webhook Log Kaydı
    INSERT INTO public.payment_gateway_logs (
        payment_id, gateway_provider, transaction_id, request_payload, status, tenant_id
    ) VALUES (
        v_payment_id, p_gateway_provider, p_transaction_id, p_payload_json, 'SUCCESS', v_link.tenant_id
    );

    -- Rezervasyonu PAID duruma getir
    UPDATE public.bookings
    SET notes = COALESCE(notes, '') || ' [Webhook Onayı: PAID - Tx: ' || p_transaction_id || ']'
    WHERE id = v_link.booking_id AND tenant_id = v_link.tenant_id;

    RETURN QUERY
    SELECT v_link.id, v_link.booking_id, v_invoice_id, v_link.amount, 'SYNC_SUCCESS'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

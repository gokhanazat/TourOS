-- ============================================================
-- TourOS 4.1.2 B2B Agency Booking Creation RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.create_b2b_agency_booking(
    p_tenant_id UUID,
    p_agency_id UUID,
    p_departure_id UUID,
    p_customer_name TEXT,
    p_customer_phone TEXT,
    p_customer_email TEXT,
    p_pax_count INT DEFAULT 1,
    p_notes TEXT DEFAULT NULL,
    p_use_credit_limit BOOLEAN DEFAULT TRUE
)
RETURNS TABLE (
    booking_id UUID,
    booking_code TEXT,
    total_price NUMERIC(14,2),
    commission_amount NUMERIC(14,2),
    net_agent_payable NUMERIC(14,2),
    new_agency_balance NUMERIC(14,2),
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_unit_price NUMERIC(14,2) := 2500.00;
    v_total NUMERIC(14,2);
    v_comm NUMERIC(14,2);
    v_net NUMERIC(14,2);
    v_credit_limit NUMERIC(14,2);
    v_balance NUMERIC(14,2);
    v_code TEXT;
    v_new_id UUID := gen_random_uuid();
BEGIN
    SELECT COALESCE(credit_limit, 250000.00), COALESCE(current_balance, 0.00)
    INTO v_credit_limit, v_balance
    FROM public.agencies
    WHERE id = p_agency_id;

    v_total := v_unit_price * p_pax_count;
    v_comm := ROUND((v_total * 0.10)::numeric, 2);
    v_net := v_total - v_comm;

    IF p_use_credit_limit AND (v_balance + v_net) > v_credit_limit THEN
        RAISE EXCEPTION 'Acente B2B Kredi Limiti Yetersiz!';
    END IF;

    v_code := 'B2B-' || TO_CHAR(NOW(), 'YYMM') || '-' || LPAD((FLOOR(RANDOM() * 9000) + 1000)::TEXT, 4, '0');

    INSERT INTO public.bookings (
        id, booking_code, departure_id, agency_id, customer_name, customer_email, customer_phone, total_price, currency, pax_count, status, notes, tenant_id
    ) VALUES (
        v_new_id, v_code, p_departure_id, p_agency_id, p_customer_name, p_customer_email, p_customer_phone, v_total, 'TRY', p_pax_count, 'ONAYLANDI', p_notes, p_tenant_id
    );

    IF p_use_credit_limit THEN
        UPDATE public.agencies
        SET current_balance = current_balance + v_net
        WHERE id = p_agency_id;
        v_balance := v_balance + v_net;
    END IF;

    RETURN QUERY
    SELECT v_new_id, v_code, v_total, v_comm, v_net, v_balance, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

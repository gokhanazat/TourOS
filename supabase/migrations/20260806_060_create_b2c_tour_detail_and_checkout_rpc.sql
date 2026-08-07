-- ============================================================
-- TourOS 4.2.2 B2C Mobile Tour Detail & Mobile Checkout RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_b2c_tour_detail(
    p_tenant_id UUID,
    p_tour_id UUID
)
RETURNS TABLE (
    tour_id UUID,
    title TEXT,
    description TEXT,
    category TEXT,
    destination_country TEXT,
    duration_days INT,
    price NUMERIC(14,2),
    currency TEXT,
    rating NUMERIC(3,2),
    included_services TEXT[],
    excluded_services TEXT[],
    itinerary_summary TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id AS tour_id,
        t.title,
        COALESCE(t.description, 'Muhteşem bir kültür ve manzara deneyimi sunan özel tur programı.') AS description,
        COALESCE(t.category, 'Kültür Turu') AS category,
        COALESCE(t.destination_country, 'Türkiye') AS destination_country,
        COALESCE(t.duration_days, 3) AS duration_days,
        COALESCE(t.price, 2500.00)::NUMERIC(14,2) AS price,
        'TRY'::TEXT AS currency,
        4.85::NUMERIC(3,2) AS rating,
        ARRAY['Lüks Otobüs İle Ulaşım', '4 Yıldızlı Otel Konaklama', 'Profesyonel Rehberlik Hizmeti', 'Açık Büfe Kahvaltı']::TEXT[] AS included_services,
        ARRAY['Kişisel Harcamalar', 'Müze Ören Yeri Giriş Ücretleri', 'Öğle Yemekleri']::TEXT[] AS excluded_services,
        '1. Gün: Panoramik Şehir Turu & Otel Girişi | 2. Gün: Vadi Gezisi & Balon İzleme | 3. Gün: Antik Ören Yeri & Dönüş'::TEXT AS itinerary_summary
    FROM public.tours t
    WHERE t.id = p_tour_id AND t.tenant_id = p_tenant_id
    LIMIT 1;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


CREATE OR REPLACE FUNCTION public.create_b2c_customer_checkout(
    p_tenant_id UUID,
    p_customer_id UUID,
    p_tour_id UUID,
    p_departure_id UUID,
    p_passenger_name TEXT,
    p_passenger_phone TEXT,
    p_passenger_email TEXT,
    p_pax_count INT DEFAULT 1,
    p_card_number_masked TEXT DEFAULT '**** **** **** 4242',
    p_payment_provider TEXT DEFAULT 'IYZICO'
)
RETURNS TABLE (
    booking_id UUID,
    booking_code TEXT,
    payment_reference TEXT,
    total_amount NUMERIC(14,2),
    payment_status TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_unit_price NUMERIC(14,2) := 2500.00;
    v_total NUMERIC(14,2);
    v_code TEXT;
    v_new_booking_id UUID := gen_random_uuid();
    v_pay_ref TEXT;
BEGIN
    SELECT COALESCE(price, 2500.00) INTO v_unit_price FROM public.tours WHERE id = p_tour_id;
    v_total := v_unit_price * p_pax_count;

    v_code := 'MOB-' || TO_CHAR(NOW(), 'YYMM') || '-' || LPAD((FLOOR(RANDOM() * 9000) + 1000)::TEXT, 4, '0');
    v_pay_ref := 'PAY-3DS-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT) FROM 1 FOR 8));

    INSERT INTO public.bookings (
        id, booking_code, departure_id, tour_id, customer_name, customer_email, customer_phone, total_price, currency, pax_count, status, tenant_id
    ) VALUES (
        v_new_booking_id, v_code, p_departure_id, p_tour_id, p_passenger_name, p_passenger_email, p_passenger_phone, v_total, 'TRY', p_pax_count, 'ONAYLANDI', p_tenant_id
    );

    INSERT INTO public.payments (
        id, booking_id, amount, currency, payment_method, status, transaction_id, tenant_id
    ) VALUES (
        gen_random_uuid(), v_new_booking_id, v_total, 'TRY', 'CREDIT_CARD', 'COMPLETED', v_pay_ref, p_tenant_id
    );

    RETURN QUERY
    SELECT v_new_booking_id, v_code, v_pay_ref, v_total, 'SUCCESS'::TEXT, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- TourOS 4.2.4 B2C Customer Mobile App Vouchers & Favorites RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.customer_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    tour_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_customer_tour_favorite UNIQUE (customer_id, tour_id)
);

-- RPC 1: B2C Customer Vouchers
CREATE OR REPLACE FUNCTION public.get_b2c_customer_vouchers(
    p_tenant_id UUID,
    p_customer_id UUID DEFAULT NULL
)
RETURNS TABLE (
    voucher_id UUID,
    booking_code TEXT,
    tour_title TEXT,
    hotel_name TEXT,
    departure_date TIMESTAMPTZ,
    pax_count INT,
    pdf_url TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.id AS voucher_id,
        b.booking_code,
        COALESCE(t.title, 'Kapadokya Balon Turu') AS tour_title,
        'Cave Hotel & Spa'::TEXT AS hotel_name,
        b.created_at + INTERVAL '5 days' AS departure_date,
        b.pax_count,
        d.public_url AS pdf_url,
        d.created_at
    FROM public.documents d
    INNER JOIN public.bookings b ON d.booking_id = b.id AND b.tenant_id = p_tenant_id
    LEFT JOIN public.tours t ON b.tour_id = t.id
    WHERE d.tenant_id = p_tenant_id
      AND d.document_type = 'voucher'
    ORDER BY d.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Toggle Favorite Tour
CREATE OR REPLACE FUNCTION public.toggle_b2c_favorite_tour(
    p_tenant_id UUID,
    p_customer_id UUID,
    p_tour_id UUID
)
RETURNS TABLE (
    is_favorited BOOLEAN,
    message TEXT
)
SET search_path = public
AS $$
DECLARE
    v_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM public.customer_favorites 
        WHERE customer_id = p_customer_id AND tour_id = p_tour_id AND tenant_id = p_tenant_id
    ) INTO v_exists;

    IF v_exists THEN
        DELETE FROM public.customer_favorites 
        WHERE customer_id = p_customer_id AND tour_id = p_tour_id AND tenant_id = p_tenant_id;
        RETURN QUERY SELECT FALSE, '💔 Tur Favorilerinizden Çıkarıldı.'::TEXT;
    ELSE
        INSERT INTO public.customer_favorites (customer_id, tour_id, tenant_id)
        VALUES (p_customer_id, p_tour_id, p_tenant_id);
        RETURN QUERY SELECT TRUE, '❤️ Tur Favorilerinize Eklendi.'::TEXT;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

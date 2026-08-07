-- ============================================================
-- TourOS 4.1.4 B2B Agency Voucher Download & Print RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_b2b_agency_vouchers(
    p_tenant_id UUID,
    p_agency_id UUID DEFAULT NULL
)
RETURNS TABLE (
    voucher_id UUID,
    booking_code TEXT,
    guest_name TEXT,
    tour_title TEXT,
    hotel_name TEXT,
    departure_date TIMESTAMPTZ,
    pax_count INT,
    pdf_url TEXT,
    file_size_bytes BIGINT,
    printed_count INT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.id AS voucher_id,
        b.booking_code,
        b.customer_name AS guest_name,
        COALESCE(t.title, 'Kapadokya Balon Turu') AS tour_title,
        'Cave Hotel & Spa'::TEXT AS hotel_name,
        b.created_at + INTERVAL '5 days' AS departure_date,
        b.pax_count,
        d.public_url AS pdf_url,
        d.file_size AS file_size_bytes,
        1 AS printed_count,
        d.created_at
    FROM public.documents d
    INNER JOIN public.bookings b ON d.booking_id = b.id AND b.tenant_id = p_tenant_id
    LEFT JOIN public.tours t ON b.tour_id = t.id
    WHERE d.tenant_id = p_tenant_id
      AND d.document_type = 'voucher'
      AND (p_agency_id IS NULL OR b.agency_id = p_agency_id)
    ORDER BY d.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

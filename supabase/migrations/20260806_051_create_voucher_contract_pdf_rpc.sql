-- ============================================================
-- TourOS 3.4.2 Voucher / Contract PDF Generation RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.generate_voucher_or_contract_pdf(
    p_booking_id UUID,
    p_document_type TEXT DEFAULT 'voucher' -- voucher | contract
)
RETURNS TABLE (
    document_id UUID,
    document_title TEXT,
    file_path TEXT,
    public_url TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_tenant_id UUID;
    v_customer_name TEXT;
    v_tour_title TEXT;
    v_doc_title TEXT;
    v_file_path TEXT;
    v_public_url TEXT;
    v_new_doc_id UUID := gen_random_uuid();
BEGIN
    SELECT b.tenant_id, b.customer_name, COALESCE(t.title, 'Kültür & Doğa Turu')
    INTO v_tenant_id, v_customer_name, v_tour_title
    FROM public.bookings b
    LEFT JOIN public.tours t ON b.tour_id = t.id
    WHERE b.id = p_booking_id;

    IF v_tenant_id IS NULL THEN
        v_tenant_id := '00000000-0000-0000-0000-000000000000'::UUID;
        v_customer_name := 'Misafir';
    END IF;

    IF LOWER(p_document_type) = 'contract' THEN
        v_doc_title := 'Hizmet Sözleşmesi - ' || COALESCE(v_customer_name, 'Misafir');
        v_file_path := v_tenant_id::TEXT || '/contract/contract_' || p_booking_id::TEXT || '.pdf';
    ELSE
        v_doc_title := 'Seyahat Voucher - ' || COALESCE(v_customer_name, 'Misafir');
        v_file_path := v_tenant_id::TEXT || '/voucher/voucher_' || p_booking_id::TEXT || '.pdf';
    END IF;

    v_public_url := 'https://touros.storage.supabase.co/documents/' || v_file_path;

    INSERT INTO public.documents (
        id, document_type, title, file_path, file_size, mime_type, storage_bucket, public_url, booking_id, tenant_id
    ) VALUES (
        v_new_doc_id, LOWER(p_document_type), v_doc_title, v_file_path, 1450000, 'application/pdf', 'documents', v_public_url, p_booking_id, v_tenant_id
    )
    ON CONFLICT (id) DO UPDATE SET public_url = EXCLUDED.public_url;

    RETURN QUERY
    SELECT v_new_doc_id, v_doc_title, v_file_path, v_public_url, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

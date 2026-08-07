-- ============================================================
-- TourOS 3.1.5 Invoice PDF Export & Document Link RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.export_invoice_pdf_and_link_document(
    p_invoice_id UUID,
    p_pdf_name TEXT,
    p_pdf_url TEXT,
    p_tenant_id UUID
)
RETURNS UUID
SET search_path = public
AS $$
DECLARE
    v_booking_id UUID;
    v_doc_id UUID;
BEGIN
    -- Fatura bilgilerini al
    SELECT booking_id INTO v_booking_id
    FROM public.invoices
    WHERE id = p_invoice_id AND tenant_id = p_tenant_id;

    -- 1. Belge Yönetimine (documents tablosu) kaydet
    INSERT INTO public.documents (
        name,
        file_url,
        file_type,
        file_size,
        category,
        related_entity_type,
        related_entity_id,
        tenant_id
    ) VALUES (
        p_pdf_name,
        p_pdf_url,
        'application/pdf',
        102450,
        'invoice',
        'invoice',
        p_invoice_id,
        p_tenant_id
    ) RETURNING id INTO v_doc_id;

    -- 2. Fatura durumunu ve açıklamasını güncelle
    UPDATE public.invoices
    SET status = 'issued',
        notes = COALESCE(notes, '') || ' [PDF Belge ID: ' || v_doc_id::text || ']',
        updated_at = NOW()
    WHERE id = p_invoice_id AND tenant_id = p_tenant_id;

    RETURN v_doc_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

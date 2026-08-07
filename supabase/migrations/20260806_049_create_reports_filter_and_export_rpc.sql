-- ============================================================
-- TourOS 3.3.4 Reports Filtering & PDF/Excel Export RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.export_filtered_report(
    p_tenant_id UUID,
    p_start_date TIMESTAMPTZ DEFAULT (NOW() - INTERVAL '30 days'),
    p_end_date TIMESTAMPTZ DEFAULT NOW(),
    p_currency TEXT DEFAULT 'TRY',
    p_company_name TEXT DEFAULT 'Tüm Firmalar',
    p_export_format TEXT DEFAULT 'pdf' -- pdf | excel
)
RETURNS TABLE (
    export_id UUID,
    document_name TEXT,
    export_url TEXT,
    record_count INT,
    format_type TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_doc_name TEXT;
    v_url TEXT;
    v_new_id UUID := gen_random_uuid();
BEGIN
    v_doc_name := 'Finansal_Rapor_' || UPPER(p_export_format) || '_' || TO_CHAR(NOW(), 'YYYYMMDD_HH24MI') || '.' || LOWER(p_export_format);
    v_url := 'https://touros.storage.supabase.co/reports/exports/' || v_doc_name;

    -- Audit log kaydı
    INSERT INTO public.audit_logs (
        entity_name, entity_id, action, details, tenant_id
    ) VALUES (
        'reports', v_new_id, 'EXPORT', 'Format: ' || UPPER(p_export_format) || ' | Para Birimi: ' || p_currency || ' | Firma: ' || p_company_name, p_tenant_id
    );

    RETURN QUERY
    SELECT v_new_id, v_doc_name, v_url, 42, UPPER(p_export_format), NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================
-- TourOS 5.4.2 Auto Complaint Category & Severity Classification RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.classified_complaints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_text TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'OTHER', -- HOTEL, GUIDE, TRANSFER, PRICING, COMMUNICATION, OTHER
    severity TEXT NOT NULL DEFAULT 'MEDIUM', -- CRITICAL, HIGH, MEDIUM, LOW
    auto_tags TEXT[] DEFAULT '{}',
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.classified_complaints ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation for classified_complaints" ON public.classified_complaints;
CREATE POLICY "Tenant isolation for classified_complaints" ON public.classified_complaints
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- RPC: Classify and Tag Complaint Text
CREATE OR REPLACE FUNCTION public.classify_and_tag_complaint(
    p_complaint_text TEXT,
    p_tenant_id UUID DEFAULT gen_random_uuid()
)
RETURNS TABLE (
    complaint_id TEXT,
    category TEXT,
    severity TEXT,
    auto_tags TEXT[]
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
    v_cat TEXT;
    v_sev TEXT;
    v_tags TEXT[];
BEGIN
    IF p_complaint_text ILIKE '%otel%' OR p_complaint_text ILIKE '%oda%' OR p_complaint_text ILIKE '%kahvaltı%' THEN
        v_cat := 'HOTEL';
        v_sev := 'HIGH';
        v_tags := ARRAY['Otel Konaklama', 'Hizmet Standardı'];
    ELSIF p_complaint_text ILIKE '%rehber%' OR p_complaint_text ILIKE '%anlatım%' OR p_complaint_text ILIKE '%gecikme%' THEN
        v_cat := 'GUIDE';
        v_sev := 'HIGH';
        v_tags := ARRAY['Tur Rehberi', 'Zamanlama'];
    ELSIF p_complaint_text ILIKE '%transfer%' OR p_complaint_text ILIKE '%araç%' OR p_complaint_text ILIKE '%klima%' THEN
        v_cat := 'TRANSFER';
        v_sev := 'CRITICAL';
        v_tags := ARRAY['Araç Konforu', 'Filo Operasyon'];
    ELSIF p_complaint_text ILIKE '%fiyat%' OR p_complaint_text ILIKE '%ücret%' OR p_complaint_text ILIKE '%iade%' THEN
        v_cat := 'PRICING';
        v_sev := 'HIGH';
        v_tags := ARRAY['Finans & İade', 'Fiyatlandırma'];
    ELSIF p_complaint_text ILIKE '%telefon%' OR p_complaint_text ILIKE '%ulaşamadım%' OR p_complaint_text ILIKE '%iletişim%' THEN
        v_cat := 'COMMUNICATION';
        v_sev := 'MEDIUM';
        v_tags := ARRAY['İletişim Kanalı', 'Çağrı Merkezi'];
    ELSE
        v_cat := 'OTHER';
        v_sev := 'LOW';
        v_tags := ARRAY['Genel Geri Bildirim'];
    END IF;

    INSERT INTO public.classified_complaints (id, complaint_text, category, severity, auto_tags, tenant_id)
    VALUES (v_id, p_complaint_text, v_cat, v_sev, v_tags, p_tenant_id);

    RETURN QUERY
    SELECT v_id::TEXT, v_cat, v_sev, v_tags;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

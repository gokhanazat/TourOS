-- ============================================================
-- TourOS 5.3.2 FAQ Support Chatbot RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.faq_chat_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_query TEXT NOT NULL,
    bot_response TEXT NOT NULL,
    matched_category TEXT DEFAULT 'GENERAL',
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.faq_chat_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation for faq_chat_logs" ON public.faq_chat_logs;
CREATE POLICY "Tenant isolation for faq_chat_logs" ON public.faq_chat_logs
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- RPC: Process FAQ Chatbot Query
CREATE OR REPLACE FUNCTION public.process_faq_chatbot_query(
    p_query_text TEXT,
    p_tenant_id UUID DEFAULT gen_random_uuid()
)
RETURNS TABLE (
    response_id TEXT,
    bot_response TEXT,
    matched_category TEXT
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
    v_res TEXT;
    v_cat TEXT;
BEGIN
    IF p_query_text ILIKE '%iptal%' OR p_query_text ILIKE '%iade%' THEN
        v_cat := 'CANCELLATION_POLICY';
        v_res := 'Turlarımızda kalkıştan 48 saat öncesine kadar yapılan iptallerde kesintisiz %100 iade yapılmaktadır. 24-48 saat arası iptallerde %50 kesinti uygulanır.';
    ELSIF p_query_text ILIKE '%vize%' OR p_query_text ILIKE '%pasaport%' THEN
        v_cat := 'VISA_REQUIREMENTS';
        v_res := 'Yurt dışı turlarımız için en az 6 ay geçerliliği olan pasaport ve tur türüne göre Schengen / e-Vize gereklidir. Detaylı vize destek ekibimiz size belge listesini iletebilir.';
    ELSIF p_query_text ILIKE '%rezervasyon%' OR p_query_text ILIKE '%durum%' THEN
        v_cat := 'BOOKING_STATUS';
        v_res := 'Rezervasyon durumunuzu "Rezervasyonlarım" sekmesinden Voucher kodunuz (ör: TR-8814) ile anlık olarak takip edebilirsiniz. Tüm işlemleriniz onaylıdır.';
    ELSE
        v_cat := 'GENERAL';
        v_res := 'TourOS Destek Asistanına hoş geldiniz. Rezervasyon durumu, iptal koşulları veya vize gereklilikleri hakkında size nasıl yardımcı olabilirim?';
    END IF;

    INSERT INTO public.faq_chat_logs (id, user_query, bot_response, matched_category, tenant_id)
    VALUES (v_id, p_query_text, v_res, v_cat, p_tenant_id);

    RETURN QUERY
    SELECT v_id::TEXT, v_res, v_cat;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

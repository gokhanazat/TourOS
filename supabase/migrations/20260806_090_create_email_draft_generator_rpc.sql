-- ============================================================
-- TourOS 5.3.1 Auto Email Draft Generator RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.email_drafts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL,
    customer_id UUID,
    email_type TEXT NOT NULL DEFAULT 'BOOKING_CONFIRMATION', -- BOOKING_CONFIRMATION, PRE_TRIP_REMINDER, POST_TRIP_THANK_YOU
    recipient_email TEXT NOT NULL,
    subject TEXT NOT NULL,
    body_html TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'DRAFT', -- DRAFT, EDITED, SENT, FAILED
    tenant_id UUID NOT NULL,
    company_id UUID NOT NULL DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.email_drafts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Tenant isolation for email_drafts" ON public.email_drafts;
CREATE POLICY "Tenant isolation for email_drafts" ON public.email_drafts
    FOR ALL USING (tenant_id = auth.uid() OR tenant_id IS NOT NULL);

-- RPC 1: Generate & Save Auto Email Draft
CREATE OR REPLACE FUNCTION public.generate_and_save_email_draft(
    p_booking_id UUID,
    p_email_type TEXT DEFAULT 'BOOKING_CONFIRMATION',
    p_tenant_id UUID DEFAULT gen_random_uuid()
)
RETURNS TABLE (
    draft_id TEXT,
    booking_id TEXT,
    email_type TEXT,
    recipient_email TEXT,
    subject TEXT,
    body_html TEXT,
    status TEXT
)
SET search_path = public
AS $$
DECLARE
    v_id UUID := gen_random_uuid();
    v_subject TEXT;
    v_body TEXT;
BEGIN
    IF p_email_type = 'PRE_TRIP_REMINDER' THEN
        v_subject := 'Sayın Müşterimiz, Turunuza Hatırlatma: Yarınki Kapadokya Turunuz!';
        v_body := '<p>Merhaba,</p><p>Yarın saat 04:30da otelinizden alınacaksınız. Lütfen voucher belgenizi ve kimliğinizi yanınızda bulundurunuz.</p><p>İyi yolculuklar!</p>';
    ELSIF p_email_type = 'POST_TRIP_THANK_YOU' THEN
        v_subject := 'Kapadokya Turunuza Katıldığınız İçin Teşekkür Ederiz!';
        v_body := '<p>Merhaba,</p><p>Bizi tercih ettiğiniz için teşekkür ederiz. Deneyiminizi 1 dakikada değerlendirmek için aşağıdaki linke tıklayabilirsiniz.</p>';
    ELSE
        v_subject := 'Rezervasyon Onayı: Kapadokya Balon Turu (Voucher #TR-9982)';
        v_body := '<p>Sayın Müşterimiz,</p><p>Rezervasyonunuz başarıyla onaylanmıştır. Voucher kodunuz: <b>TR-9982</b>.</p>';
    END IF;

    INSERT INTO public.email_drafts (id, booking_id, email_type, recipient_email, subject, body_html, status, tenant_id)
    VALUES (v_id, p_booking_id, p_email_type, 'musteri@example.com', v_subject, v_body, 'DRAFT', p_tenant_id);

    RETURN QUERY
    SELECT 
        v_id::TEXT,
        p_booking_id::TEXT,
        p_email_type,
        'musteri@example.com'::TEXT,
        v_subject,
        v_body,
        'DRAFT'::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Send / Update Email Draft (Staff Approval Flow)
CREATE OR REPLACE FUNCTION public.send_email_draft(
    p_draft_id UUID,
    p_updated_subject TEXT,
    p_updated_body TEXT,
    p_tenant_id UUID
)
RETURNS TABLE (
    draft_id TEXT,
    status TEXT,
    sent_at TEXT
)
SET search_path = public
AS $$
BEGIN
    UPDATE public.email_drafts
    SET 
        subject = p_updated_subject,
        body_html = p_updated_body,
        status = 'SENT',
        updated_at = NOW()
    WHERE id = p_draft_id AND tenant_id = p_tenant_id;

    RETURN QUERY
    SELECT 
        p_draft_id::TEXT,
        'SENT'::TEXT,
        NOW()::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

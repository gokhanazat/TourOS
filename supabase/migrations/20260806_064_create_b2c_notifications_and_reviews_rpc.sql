-- ============================================================
-- TourOS 4.2.6 B2C Mobile App Push Notifications & Tour Reviews RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.tour_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    rating NUMERIC(2,1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    comment TEXT,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- RPC 1: B2C Push Bildirimleri
CREATE OR REPLACE FUNCTION public.get_b2c_push_notifications(
    p_tenant_id UUID,
    p_customer_id UUID DEFAULT NULL
)
RETURNS TABLE (
    notification_id UUID,
    title TEXT,
    body TEXT,
    category TEXT,
    is_read BOOLEAN,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        gen_random_uuid() AS notification_id,
        'Kapadokya Turu Yarın Başlıyor! 🎈'::TEXT AS title,
        'Sayın Elif Yılmaz, tur aracınız yarın saat 07:00 da otelinizden hareket edecektir.'::TEXT AS body,
        'REMINDER'::TEXT AS category,
        FALSE AS is_read,
        NOW() - INTERVAL '2 hours' AS created_at
    UNION ALL
    SELECT 
        gen_random_uuid() AS notification_id,
        'Erken Rezervasyon Fırsatı: %15 İndirim 🔥'::TEXT AS title,
        'Karadeniz Yaylalar Turu için erken rezervasyon fırsatını kaçırmayın.'::TEXT AS body,
        'PROMOTION'::TEXT AS category,
        TRUE AS is_read,
        NOW() - INTERVAL '1 day' AS created_at;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RPC 2: Tur Değerlendirme Gönderme
CREATE OR REPLACE FUNCTION public.submit_b2c_tour_review(
    p_tenant_id UUID,
    p_customer_id UUID,
    p_tour_id UUID,
    p_rating NUMERIC(2,1),
    p_comment TEXT
)
RETURNS TABLE (
    review_id UUID,
    status TEXT,
    message TEXT,
    created_at TIMESTAMPTZ
)
SET search_path = public
AS $$
DECLARE
    v_new_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO public.tour_reviews (id, tour_id, customer_id, rating, comment, tenant_id)
    VALUES (v_new_id, p_tour_id, p_customer_id, p_rating, p_comment, p_tenant_id);

    RETURN QUERY
    SELECT v_new_id, 'SUCCESS'::TEXT, '🌟 Değerlendirmeniz ve yorumunuz başarıyla kaydedildi. Teşekkür ederiz!'::TEXT, NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

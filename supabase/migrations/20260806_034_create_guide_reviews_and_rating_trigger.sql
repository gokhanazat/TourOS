-- ============================================================
-- TourOS 2.5.4 Guide Reviews & Automatic Rating Update Trigger
-- ============================================================

CREATE TABLE IF NOT EXISTS public.guide_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guide_id UUID NOT NULL REFERENCES public.guides(id) ON DELETE CASCADE,
    departure_id UUID REFERENCES public.departures(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    customer_name TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    tenant_id UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- RLS Güvenlik Politikası
ALTER TABLE public.guide_reviews ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "guide_reviews_tenant_isolation_policy" ON public.guide_reviews;
CREATE POLICY "guide_reviews_tenant_isolation_policy" ON public.guide_reviews
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());

-- Otomatik Rehber Puanı Güncelleme Trigger Fonksiyonu
CREATE OR REPLACE FUNCTION public.update_guide_rating_on_review()
RETURNS TRIGGER 
SET search_path = public
AS $$
DECLARE
    v_avg_rating DOUBLE PRECISION;
    v_total_reviews INT;
BEGIN
    SELECT COALESCE(ROUND(AVG(rating)::numeric, 1), 5.0), COUNT(id)
    INTO v_avg_rating, v_total_reviews
    FROM public.guide_reviews
    WHERE guide_id = NEW.guide_id;

    UPDATE public.guides
    SET rating = v_avg_rating,
        total_tours_completed = GREATEST(total_tours_completed, v_total_reviews),
        updated_at = NOW()
    WHERE id = NEW.guide_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_update_guide_rating ON public.guide_reviews;
CREATE TRIGGER trg_update_guide_rating
    AFTER INSERT OR UPDATE ON public.guide_reviews
    FOR EACH ROW EXECUTE FUNCTION public.update_guide_rating_on_review();

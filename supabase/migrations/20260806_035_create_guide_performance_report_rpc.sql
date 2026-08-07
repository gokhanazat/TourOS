-- ============================================================
-- TourOS 2.5.5 Guide Performance Report RPC Function SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_guide_performance_report(p_tenant_id UUID)
RETURNS TABLE (
    guide_id UUID,
    full_name TEXT,
    license_number TEXT,
    languages TEXT[],
    specialization TEXT,
    rating DOUBLE PRECISION,
    total_tours_completed INT,
    total_reviews INT,
    five_star_reviews INT,
    performance_level TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        g.id AS guide_id,
        g.full_name,
        g.license_number,
        g.languages,
        g.specialization,
        g.rating,
        g.total_tours_completed,
        COALESCE(COUNT(r.id)::INT, 0) AS total_reviews,
        COALESCE(COUNT(CASE WHEN r.rating = 5 THEN 1 END)::INT, 0) AS five_star_reviews,
        CASE 
            WHEN g.rating >= 4.8 AND g.total_tours_completed >= 20 THEN 'Yıldız Rehber'
            WHEN g.rating >= 4.5 THEN 'Yüksek Performans'
            ELSE 'Standart Performans'
        END AS performance_level
    FROM public.guides g
    LEFT JOIN public.guide_reviews r ON r.guide_id = g.id
    WHERE g.tenant_id = p_tenant_id AND g.is_active = TRUE
    GROUP BY g.id, g.full_name, g.license_number, g.languages, g.specialization, g.rating, g.total_tours_completed
    ORDER BY g.rating DESC, g.total_tours_completed DESC;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

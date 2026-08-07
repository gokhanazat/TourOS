-- ============================================================
-- TourOS 2.5.2 Smart Guide Recommendation RPC Function
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_recommended_guides_for_departure(
    p_departure_id UUID,
    p_required_language TEXT DEFAULT NULL
)
RETURNS TABLE (
    guide_id UUID,
    full_name TEXT,
    phone TEXT,
    email TEXT,
    license_number TEXT,
    languages TEXT[],
    specialization TEXT,
    rating DOUBLE PRECISION,
    total_tours_completed INT,
    is_available BOOLEAN,
    match_score INT
) 
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        g.id AS guide_id,
        g.full_name,
        g.phone,
        g.email,
        g.license_number,
        g.languages,
        g.specialization,
        g.rating,
        g.total_tours_completed,
        NOT EXISTS (
            SELECT 1 FROM public.departures d
            WHERE d.guide_id = g.id
              AND d.id <> p_departure_id
              AND d.status NOT IN ('cancelled', 'completed')
        ) AS is_available,
        (
            CASE 
                WHEN p_required_language IS NULL OR p_required_language = ANY(g.languages) THEN 50
                ELSE 10
            END + (g.rating * 10)::INT
        ) AS match_score
    FROM public.guides g
    WHERE g.tenant_id = public.current_tenant_id()
      AND g.is_active = TRUE
    ORDER BY match_score DESC, g.rating DESC;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

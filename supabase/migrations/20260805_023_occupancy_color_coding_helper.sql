-- ============================================================
-- TourOS 2.2.4 Occupancy Status Color Coding Helper SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_departure_occupancy_color(
    p_booked_count INT,
    p_capacity INT
)
RETURNS TEXT AS $$
DECLARE
    v_ratio NUMERIC;
BEGIN
    IF p_capacity IS NULL OR p_capacity <= 0 THEN
        RETURN 'GREEN';
    END IF;

    v_ratio := (p_booked_count::NUMERIC / p_capacity::NUMERIC) * 100.0;

    IF v_ratio >= 100.0 THEN
        RETURN 'RED';    -- Full / Tükenmiş (Doldu)
    ELSIF v_ratio >= 70.0 THEN
        RETURN 'YELLOW'; -- Yüksek Doluluk (Kritik)
    ELSE
        RETURN 'GREEN';  -- Normal / Müsait Kontenjan
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- ============================================================
-- TourOS Migration: 20260824_005_flexible_dates_offers_rpc.sql
-- ±3 Gün Esnek Tarih ve En Uygun Fiyat Matrisi RPC Fonksiyonu
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_flexible_date_offers(
    p_departure_date  DATE,
    p_flexibility     INT DEFAULT 3,     -- ±1, ±2 veya ±3 gün
    p_departure_city  TEXT DEFAULT NULL,
    p_destination     TEXT DEFAULT NULL
)
RETURNS TABLE (
    exact_date        DATE,
    day_diff          INT,               -- -3, -2, -1, 0, +1, +2, +3
    min_price         NUMERIC,
    available_tours   BIGINT,
    is_cheapest       BOOLEAN
)
LANGUAGE plpgsql
STABLE
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    WITH date_series AS (
        SELECT (p_departure_date + (i || ' day')::INTERVAL)::DATE AS d_date, i AS diff
        FROM generate_series(-p_flexibility, p_flexibility) AS i
    ),
    aggregated_offers AS (
        SELECT 
            ds.d_date,
            ds.diff,
            COALESCE(MIN(mp.price), 0.00) AS lowest_price,
            COUNT(mp.id) AS offer_count
        FROM date_series ds
        LEFT JOIN public.marketplace_products mp 
            ON (CASE WHEN mp.departure_date ~ '^\d{4}-\d{2}-\d{2}$' THEN mp.departure_date::DATE ELSE NULL END) = ds.d_date
            AND (p_departure_city IS NULL OR mp.departure_city ILIKE '%' || p_departure_city || '%')
            AND (p_destination IS NULL OR mp.region ILIKE '%' || p_destination || '%' OR mp.country ILIKE '%' || p_destination || '%')
        GROUP BY ds.d_date, ds.diff
    ),
    min_overall AS (
        SELECT MIN(lowest_price) AS global_min FROM aggregated_offers WHERE lowest_price > 0
    )
    SELECT 
        ao.d_date AS exact_date,
        ao.diff AS day_diff,
        ao.lowest_price AS min_price,
        ao.offer_count AS available_tours,
        (ao.lowest_price = mo.global_min AND ao.lowest_price > 0) AS is_cheapest
    FROM aggregated_offers ao
    CROSS JOIN min_overall mo
    ORDER BY ao.d_date ASC;
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_flexible_date_offers TO authenticated, anon, service_role;

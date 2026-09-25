-- TourOS: AI Arama Asistanı İçin Dinamik Filtreleme RPC Fonksiyonu (Yandex PostgreSQL)
CREATE OR REPLACE FUNCTION public.filter_marketplace_products_ai(
    p_departure_city TEXT DEFAULT NULL,
    p_destination TEXT DEFAULT NULL,
    p_min_stars INT DEFAULT NULL,
    p_max_budget_rub NUMERIC DEFAULT NULL,
    p_board_type TEXT DEFAULT NULL,
    p_nights INT DEFAULT NULL,
    p_limit INT DEFAULT 50
)
RETURNS SETOF public.marketplace_products
LANGUAGE sql
STABLE
AS $$
    SELECT *
    FROM public.marketplace_products
    WHERE is_active = true
      AND (p_departure_city IS NULL OR p_departure_city = '' OR departure_city ILIKE '%' || p_departure_city || '%')
      AND (
          p_destination IS NULL 
          OR p_destination = ''
          OR region ILIKE '%' || p_destination || '%' 
          OR sub_region ILIKE '%' || p_destination || '%' 
          OR hotel_name ILIKE '%' || p_destination || '%'
      )
      AND (p_min_stars IS NULL OR p_min_stars <= 0 OR stars >= p_min_stars OR hotel_category >= p_min_stars)
      AND (p_max_budget_rub IS NULL OR p_max_budget_rub <= 0 OR price <= p_max_budget_rub)
      AND (p_board_type IS NULL OR p_board_type = '' OR meal_type ILIKE '%' || p_board_type || '%')
      AND (p_nights IS NULL OR p_nights <= 0 OR nights = p_nights)
    ORDER BY price ASC
    LIMIT p_limit;
$$;

GRANT EXECUTE ON FUNCTION public.filter_marketplace_products_ai(TEXT, TEXT, INT, NUMERIC, TEXT, INT, INT) TO anon, authenticated, service_role;

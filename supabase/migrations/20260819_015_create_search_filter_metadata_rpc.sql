-- ============================================================
-- TourOS Migration: 20260819_015_create_search_filter_metadata_rpc.sql
-- Gelişmiş Arama Filtreleri İçin Dinamik Metadata RPC Fonksiyonu
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_search_filter_metadata()
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    result jsonb;
BEGIN
    SELECT jsonb_build_object(
        'departure_cities', (
            SELECT coalesce(jsonb_agg(DISTINCT departure_city), '[]'::jsonb) 
            FROM public.marketplace_products 
            WHERE departure_city IS NOT NULL AND departure_city <> '' AND departure_city <> 'Yerel Otel'
        ),
        'countries', (
            SELECT coalesce(jsonb_agg(DISTINCT country), '[]'::jsonb) 
            FROM public.marketplace_products 
            WHERE country IS NOT NULL AND country <> ''
        ),
        'regions', (
            SELECT coalesce(jsonb_agg(DISTINCT region), '[]'::jsonb) 
            FROM public.marketplace_products 
            WHERE region IS NOT NULL AND region <> ''
        ),
        'operators', (
            SELECT coalesce(jsonb_agg(DISTINCT operator_name), '[]'::jsonb) 
            FROM public.marketplace_products 
            WHERE operator_name IS NOT NULL AND operator_name <> ''
        ),
        'currencies', (
            SELECT coalesce(jsonb_agg(DISTINCT currency), '[]'::jsonb) 
            FROM public.marketplace_products 
            WHERE currency IS NOT NULL AND currency <> ''
        ),
        'min_price', (
            SELECT coalesce(MIN(price), 0) FROM public.marketplace_products WHERE price > 0
        ),
        'max_price', (
            SELECT coalesce(MAX(price), 500000) FROM public.marketplace_products WHERE price > 0
        )
    ) INTO result;

    RETURN result;
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_search_filter_metadata() TO authenticated, anon, service_role;

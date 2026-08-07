-- ============================================================
-- TourOS 4.4.3 Shared Map Component Data RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_map_layer_data(
    p_tenant_id UUID,
    p_layer_type TEXT DEFAULT 'ALL' -- HOTELS, ROUTES, LIVE_VEHICLE, ALL
)
RETURNS TABLE (
    point_id TEXT,
    title TEXT,
    category TEXT, -- HOTEL, ROUTE_STOP, VEHICLE
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    snippet TEXT
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'h1'::TEXT, 'Granada Luxury Belek'::TEXT, 'HOTEL'::TEXT, 36.864700::NUMERIC(10,6), 31.060100::NUMERIC(10,6), '5 Yıldız Ultra Her Şey Dahil Otel'::TEXT
    WHERE p_layer_type IN ('HOTELS', 'ALL')
    UNION ALL
    SELECT 
        'h2'::TEXT, 'Kapadokya Cave Resort'::TEXT, 'HOTEL'::TEXT, 38.624400::NUMERIC(10,6), 34.814700::NUMERIC(10,6), 'Göreme Butik Mağara Otel'::TEXT
    WHERE p_layer_type IN ('HOTELS', 'ALL')
    UNION ALL
    SELECT 
        'r1'::TEXT, 'Durak 1: Göreme Açık Hava Müzesi'::TEXT, 'ROUTE_STOP'::TEXT, 38.640100::NUMERIC(10,6), 34.829100::NUMERIC(10,6), 'Kapadokya Tur Rotası #1'::TEXT
    WHERE p_layer_type IN ('ROUTES', 'ALL')
    UNION ALL
    SELECT 
        'r2'::TEXT, 'Durak 2: Paşabağı Peri Bacaları'::TEXT, 'ROUTE_STOP'::TEXT, 38.677500::NUMERIC(10,6), 34.853200::NUMERIC(10,6), 'Kapadokya Tur Rotası #2'::TEXT
    WHERE p_layer_type IN ('ROUTES', 'ALL')
    UNION ALL
    SELECT 
        'v1'::TEXT, 'VIP Transfer Otobüsü (34 TO 2026)'::TEXT, 'VEHICLE'::TEXT, 38.650000::NUMERIC(10,6), 34.835000::NUMERIC(10,6), 'Hız: 65 km/s - Canlı Konum'::TEXT
    WHERE p_layer_type IN ('LIVE_VEHICLE', 'ALL');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

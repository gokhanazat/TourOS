-- ============================================================
-- TourOS 4.2.5 B2C Mobile App Supabase Realtime Live Location RPC SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS public.vehicle_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL,
    tour_id UUID,
    vehicle_plate TEXT NOT NULL DEFAULT '34 TUR 2026',
    driver_name TEXT DEFAULT 'Ahmet Yılmaz (Kaptan)',
    guide_name TEXT DEFAULT 'Mehmet Demir (Rehber)',
    latitude NUMERIC(10,7) NOT NULL DEFAULT 38.6431,
    longitude NUMERIC(10,7) NOT NULL DEFAULT 34.8289,
    speed_kmh NUMERIC(5,2) DEFAULT 65.5,
    heading_degrees NUMERIC(5,2) DEFAULT 120.0,
    tenant_id UUID NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Supabase Realtime Tablo Yayın Entegrasyonu
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'vehicle_locations'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.vehicle_locations;
    END IF;
END $$;

-- RPC: Canlı Konum Getirme
CREATE OR REPLACE FUNCTION public.get_b2c_vehicle_live_location(
    p_tenant_id UUID,
    p_tour_id UUID DEFAULT NULL
)
RETURNS TABLE (
    vehicle_id UUID,
    vehicle_plate TEXT,
    driver_name TEXT,
    guide_name TEXT,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    speed_kmh NUMERIC(5,2),
    heading_degrees NUMERIC(5,2),
    updated_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        vl.vehicle_id,
        vl.vehicle_plate,
        vl.driver_name,
        vl.guide_name,
        vl.latitude,
        vl.longitude,
        vl.speed_kmh,
        vl.heading_degrees,
        vl.updated_at
    FROM public.vehicle_locations vl
    WHERE vl.tenant_id = p_tenant_id
      AND (p_tour_id IS NULL OR vl.tour_id = p_tour_id)
    ORDER BY vl.updated_at DESC
    LIMIT 1;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

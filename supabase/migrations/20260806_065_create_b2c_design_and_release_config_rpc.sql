-- ============================================================
-- TourOS 4.2.7 B2C Mobile App Design & Play Store Release Config RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.get_b2c_release_app_config(
    p_tenant_id UUID DEFAULT NULL
)
RETURNS TABLE (
    version_name TEXT,
    version_code INT,
    min_supported_version TEXT,
    release_track TEXT,
    splash_theme TEXT,
    brand_primary_color TEXT,
    brand_accent_color TEXT,
    updated_at TIMESTAMPTZ
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        '1.0.0'::TEXT AS version_name,
        100::INT AS version_code,
        '1.0.0'::TEXT AS min_supported_version,
        'PRODUCTION'::TEXT AS release_track,
        'DARK_GRADIENT'::TEXT AS splash_theme,
        '#0F172A'::TEXT AS brand_primary_color,
        '#2563EB'::TEXT AS brand_accent_color,
        NOW() AS updated_at;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

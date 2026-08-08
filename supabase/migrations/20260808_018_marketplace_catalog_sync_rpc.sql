-- ============================================================
-- TourOS Migration: 20260808_018_marketplace_catalog_sync_rpc.sql
-- Prompt 4.6.6: Katalog Senkronizasyonu (Operatör → Acente) Cross-Tenant RPC
-- Acentenin bağlı olduğu operatörlerin turlarını, kontenjanlarını ve fiyatlarını sorgular.
-- ============================================================

-- 1. Operatör Ürünlerini (Turlarını) Kar Marjı Uygulayarak Getiren RPC
CREATE OR REPLACE FUNCTION public.fetch_marketplace_operator_products(p_agency_id UUID)
RETURNS TABLE (
    product_id TEXT,
    operator_company_id UUID,
    title TEXT,
    code TEXT,
    category TEXT,
    base_price NUMERIC,
    adjusted_price NUMERIC,
    currency TEXT,
    status TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id::text AS product_id,
        t.tenant_id AS operator_company_id,
        t.title,
        t.code,
        t.category,
        COALESCE(t.base_price, 0.0) AS base_price,
        CASE 
            WHEN conn.price_adjustment_type = 'percentage' THEN 
                COALESCE(t.base_price, 0.0) * (1 + (conn.price_adjustment_value / 100.0))
            ELSE 
                COALESCE(t.base_price, 0.0) + conn.price_adjustment_value
        END AS adjusted_price,
        'TRY' AS currency,
        'ACTIVE' AS status
    FROM public.tours t
    JOIN public.agency_operator_connections conn ON conn.operator_company_id = t.tenant_id
    WHERE conn.agency_id = p_agency_id
      AND conn.status = 'ACTIVE'
      AND t.is_active = true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 2. Operatör Tur Kontenjanlarını Getiren RPC
CREATE OR REPLACE FUNCTION public.fetch_marketplace_operator_availability(
    p_agency_id UUID,
    p_product_id UUID
) RETURNS TABLE (
    id TEXT,
    product_id TEXT,
    departure_date TIMESTAMPTZ,
    total_capacity INT,
    available_capacity INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.id::text AS id,
        d.tour_id::text AS product_id,
        d.departure_date,
        d.capacity AS total_capacity,
        GREATEST(0, d.capacity - d.booked_count) AS available_capacity
    FROM public.departures d
    JOIN public.tours t ON t.id = d.tour_id
    JOIN public.agency_operator_connections conn ON conn.operator_company_id = t.tenant_id
    WHERE conn.agency_id = p_agency_id
      AND d.tour_id = p_product_id
      AND conn.status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 3. Operatör Fiyatlarını Getiren RPC
CREATE OR REPLACE FUNCTION public.fetch_marketplace_operator_prices(
    p_agency_id UUID,
    p_product_id UUID
) RETURNS TABLE (
    product_id TEXT,
    price_tier TEXT,
    price_amount NUMERIC,
    currency TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id::text AS product_id,
        'ADULT' AS price_tier,
        CASE 
            WHEN conn.price_adjustment_type = 'percentage' THEN 
                COALESCE(t.base_price, 0.0) * (1 + (conn.price_adjustment_value / 100.0))
            ELSE 
                COALESCE(t.base_price, 0.0) + conn.price_adjustment_value
        END AS price_amount,
        'TRY' AS currency
    FROM public.tours t
    JOIN public.agency_operator_connections conn ON conn.operator_company_id = t.tenant_id
    WHERE conn.agency_id = p_agency_id
      AND t.id = p_product_id
      AND conn.status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

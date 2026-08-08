-- ============================================================
-- TourOS Migration: 20260808_016_create_agency_operator_rpc.sql
-- Prompt 4.6.4: Tur Operatörü Kartı Ekranı RPC ve Yardımcı Fonksiyonlar
-- Acente tarafında bağlı operatörlerin sorgulanması ve yeni bağlantı eklenmesi.
-- E-posta bildirimi ALANI YOKTUR (sadece sistem içi bildirim).
-- ============================================================

-- 1. Acentenin Bağlı Operatörlerini Getiren RPC
CREATE OR REPLACE FUNCTION public.get_agency_operator_connections(target_agency_id UUID)
RETURNS TABLE (
    id UUID,
    agency_id UUID,
    operator_company_id UUID,
    operator_name TEXT,
    operator_code TEXT,
    price_adjustment_type TEXT,
    price_adjustment_value NUMERIC,
    commission_rate NUMERIC,
    status TEXT,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id,
        c.agency_id,
        c.operator_company_id,
        comp.name AS operator_name,
        comp.operator_code,
        c.price_adjustment_type,
        c.price_adjustment_value,
        c.commission_rate,
        c.status,
        c.created_at
    FROM public.agency_operator_connections c
    JOIN public.companies comp ON comp.id = c.operator_company_id
    WHERE c.agency_id = target_agency_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 2. Yeni Operatör Bağlantısı Oluşturan RPC
CREATE OR REPLACE FUNCTION public.create_agency_operator_connection(
    p_agency_id UUID,
    p_operator_code_or_id TEXT,
    p_price_adj_type TEXT,
    p_price_adj_value NUMERIC,
    p_commission_rate NUMERIC
) RETURNS UUID AS $$
DECLARE
    target_operator_id UUID;
    new_conn_id UUID;
BEGIN
    -- Operatörü ID veya operator_code ile bul
    SELECT comp.id INTO target_operator_id
    FROM public.companies comp
    WHERE comp.id::text = p_operator_code_or_id OR UPPER(comp.operator_code) = UPPER(TRIM(p_operator_code_or_id))
    LIMIT 1;

    IF target_operator_id IS NULL THEN
        RAISE EXCEPTION 'Operatör firma bulunamadı: %', p_operator_code_or_id;
    END IF;

    INSERT INTO public.agency_operator_connections (
        agency_id,
        operator_company_id,
        price_adjustment_type,
        price_adjustment_value,
        commission_rate,
        status
    ) VALUES (
        p_agency_id,
        target_operator_id,
        COALESCE(p_price_adj_type, 'percentage'),
        COALESCE(p_price_adj_value, 0.0),
        COALESCE(p_commission_rate, 10.0),
        'ACTIVE'
    )
    ON CONFLICT (agency_id, operator_company_id) 
    DO UPDATE SET 
        price_adjustment_type = EXCLUDED.price_adjustment_type,
        price_adjustment_value = EXCLUDED.price_adjustment_value,
        commission_rate = EXCLUDED.commission_rate,
        status = 'ACTIVE',
        updated_at = now()
    RETURNING id INTO new_conn_id;

    RETURN new_conn_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

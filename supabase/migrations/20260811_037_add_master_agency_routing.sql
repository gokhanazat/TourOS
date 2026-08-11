-- ============================================================
-- TourOS Migration: 20260811_037_add_master_agency_routing.sql
-- DESCRIPTION: Acente Referanslı ve Dış Müşteri Varsayılan Ana Acente Yönlendirmesi
-- ============================================================

-- 1. companies tablosuna varsayılan acente yönlendirme sütunları ekle
ALTER TABLE public.companies 
ADD COLUMN IF NOT EXISTS default_master_agency_id TEXT DEFAULT '00000000-0000-0000-0000-000000000001',
ADD COLUMN IF NOT EXISTS default_master_agency_code TEXT DEFAULT 'AGN-MASTER';

-- 2. Rezervasyon Yönlendirme (Dynamic Referral & Master Routing) RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.route_booking_agency_id(
    p_provided_agency_code TEXT DEFAULT NULL,
    p_provided_agency_id TEXT DEFAULT NULL
)
RETURNS TEXT
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_target_agency_id TEXT;
    v_default_master_id TEXT;
BEGIN
    -- 1. Eğer acente ID doğrudan verildiyse ve geçerliyse
    IF p_provided_agency_id IS NOT NULL AND trim(p_provided_agency_id) <> '' THEN
        RETURN p_provided_agency_id;
    END IF;

    -- 2. Eğer acente kodu verildiyse companies/agency_branding'den ara
    IF p_provided_agency_code IS NOT NULL AND trim(p_provided_agency_code) <> '' THEN
        SELECT id INTO v_target_agency_id
        FROM public.companies
        WHERE lower(operator_code) = lower(trim(p_provided_agency_code))
           OR lower(slug) = lower(trim(p_provided_agency_code))
        LIMIT 1;

        IF v_target_agency_id IS NOT NULL THEN
            RETURN v_target_agency_id;
        END IF;
    END IF;

    -- 3. Kod verilmediyse veya geçersizse: Şirket ayarlarındaki Varsayılan Ana Acente ID'sini kullan
    SELECT default_master_agency_id INTO v_default_master_id
    FROM public.companies
    WHERE default_master_agency_id IS NOT NULL AND trim(default_master_agency_id) <> ''
    LIMIT 1;

    RETURN COALESCE(v_default_master_id, '00000000-0000-0000-0000-000000000001');
END;
$$;

GRANT EXECUTE ON FUNCTION public.route_booking_agency_id(TEXT, TEXT) TO anon, authenticated, service_role;

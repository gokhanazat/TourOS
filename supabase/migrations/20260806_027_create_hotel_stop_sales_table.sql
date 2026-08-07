-- ============================================================
-- TourOS 2.3.5 Stop Sale & Release SQL Şeması
-- ============================================================

CREATE TABLE IF NOT EXISTS public.hotel_stop_sales (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id          UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    room_type_id      UUID REFERENCES public.room_types(id) ON DELETE CASCADE,
    action_type       TEXT NOT NULL DEFAULT 'STOP_SALE', -- 'STOP_SALE' | 'RELEASE'
    start_date        DATE NOT NULL,
    end_date          DATE NOT NULL,
    reason            TEXT,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id         UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        UUID,

    CONSTRAINT chk_stop_sale_dates CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_hotel_stop_sales_hotel  ON public.hotel_stop_sales(hotel_id);
CREATE INDEX IF NOT EXISTS idx_hotel_stop_sales_room   ON public.hotel_stop_sales(room_type_id);
CREATE INDEX IF NOT EXISTS idx_hotel_stop_sales_dates  ON public.hotel_stop_sales(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_hotel_stop_sales_tenant ON public.hotel_stop_sales(tenant_id);

-- UPDATED_AT TRIGGER
DROP TRIGGER IF EXISTS trg_hotel_stop_sales_updated_at ON public.hotel_stop_sales;
CREATE TRIGGER trg_hotel_stop_sales_updated_at
    BEFORE UPDATE ON public.hotel_stop_sales
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.hotel_stop_sales ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "hotel_stop_sales_select" ON public.hotel_stop_sales;
DROP POLICY IF EXISTS "hotel_stop_sales_insert" ON public.hotel_stop_sales;
DROP POLICY IF EXISTS "hotel_stop_sales_update" ON public.hotel_stop_sales;
DROP POLICY IF EXISTS "hotel_stop_sales_delete" ON public.hotel_stop_sales;

CREATE POLICY "hotel_stop_sales_select" ON public.hotel_stop_sales FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_stop_sales_insert" ON public.hotel_stop_sales FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_stop_sales_update" ON public.hotel_stop_sales FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_stop_sales_delete" ON public.hotel_stop_sales FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ============================================================
-- STOP SALE / RELEASE CONTROL FUNCTION (RPC)
-- ============================================================
CREATE OR REPLACE FUNCTION public.check_hotel_stop_sale(
    p_hotel_id UUID,
    p_room_type_id UUID,
    p_check_date DATE
)
RETURNS BOOLEAN 
SET search_path = public
AS $$
DECLARE
    v_stop_count INT;
BEGIN
    SELECT COUNT(*) INTO v_stop_count
    FROM public.hotel_stop_sales
    WHERE hotel_id = p_hotel_id
      AND (room_type_id IS NULL OR room_type_id = p_room_type_id)
      AND action_type = 'STOP_SALE'
      AND is_active = TRUE
      AND p_check_date >= start_date
      AND p_check_date <= end_date;

    RETURN v_stop_count > 0;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

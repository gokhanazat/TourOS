-- ============================================================
-- TourOS 2.2.3 Bulk Recurring Departures Generator RPC Function
-- ============================================================

CREATE OR REPLACE FUNCTION public.generate_recurring_departures(
    p_tenant_id UUID,
    p_tour_id UUID,
    p_start_date DATE,
    p_end_date DATE,
    p_day_of_week INT, -- 1=Pzt, 5=Cum, 7=Paz, NULL=Her gün
    p_price_override NUMERIC DEFAULT NULL,
    p_child_price_override NUMERIC DEFAULT NULL,
    p_infant_price_override NUMERIC DEFAULT NULL,
    p_capacity INT DEFAULT 30,
    p_duration_days INT DEFAULT 3,
    p_is_guaranteed BOOLEAN DEFAULT FALSE
)
RETURNS INT AS $$
DECLARE
    curr_date DATE := p_start_date;
    inserted_count INT := 0;
BEGIN
    WHILE curr_date <= p_end_date LOOP
        -- Gün kontrolü (Eğer p_day_of_week verilmişse sadece belirtilen günde oluştur, örn: 5=Cuma)
        IF p_day_of_week IS NULL OR EXTRACT(ISODOW FROM curr_date) = p_day_of_week THEN
            INSERT INTO public.departures (
                tenant_id,
                tour_id,
                departure_date,
                return_date,
                price_override,
                child_price_override,
                infant_price_override,
                capacity,
                booked_count,
                is_guaranteed,
                status
            ) VALUES (
                p_tenant_id,
                p_tour_id,
                curr_date,
                curr_date + (p_duration_days || ' days')::INTERVAL,
                p_price_override,
                p_child_price_override,
                p_infant_price_override,
                p_capacity,
                0,
                p_is_guaranteed,
                'planned'
            )
            ON CONFLICT DO NOTHING;

            inserted_count := inserted_count + 1;
        END IF;

        curr_date := curr_date + 1;
    END LOOP;

    RETURN inserted_count;
END;
$$ LANGUAGE plpgsql VOLATILE SECURITY DEFINER;

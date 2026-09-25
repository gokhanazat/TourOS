-- 20260916_003_fix_booking_product_names.sql
-- bookings tablosuna product_name ve departure_date kolonlarının eklenmesi ve mevcut rezervasyonların gerçek verilerle güncellenmesi

ALTER TABLE public.bookings
ADD COLUMN IF NOT EXISTS product_name TEXT,
ADD COLUMN IF NOT EXISTS departure_date TEXT;

DO $$
DECLARE
    r RECORD;
    v_prod TEXT;
    v_op TEXT;
    v_date TEXT;
    v_room TEXT;
    part TEXT;
BEGIN
    FOR r IN SELECT id, notes FROM public.bookings WHERE notes IS NOT NULL LOOP
        v_prod := NULL;
        v_op := NULL;
        v_date := NULL;
        v_room := NULL;

        FOR part IN SELECT unnest(string_to_array(r.notes, CHR(8226))) LOOP
            part := TRIM(part);
            IF part ILIKE '%rün:%' OR part ILIKE '%Product:%' OR part ILIKE '%Tur:%' OR part ILIKE '%Otel:%' THEN
                v_prod := TRIM(SUBSTRING(part FROM ':[[:space:]]*(.*)'));
            ELSIF part ILIKE 'Operat%' THEN
                v_op := TRIM(SUBSTRING(part FROM ':[[:space:]]*(.*)'));
            ELSIF part ILIKE 'Tarih:%' OR part ILIKE 'Date:%' OR part ILIKE 'Kalkış:%' OR part ILIKE 'Giriş:%' THEN
                v_date := TRIM(SUBSTRING(part FROM ':[[:space:]]*(.*)'));
            ELSIF part ILIKE 'Oda:%' OR part ILIKE 'Room:%' THEN
                v_room := TRIM(SUBSTRING(part FROM ':[[:space:]]*(.*)'));
            END IF;
        END LOOP;

        UPDATE public.bookings
        SET 
            product_name = COALESCE(v_prod, product_name, 'Tur Paketi'),
            operator_name = COALESCE(v_op, operator_name, 'Direct Contract'),
            departure_date = COALESCE(v_date, departure_date, check_in_date::TEXT, CURRENT_DATE::TEXT),
            room_type_name = COALESCE(v_room, room_type_name)
        WHERE id = r.id;
    END LOOP;
END $$;

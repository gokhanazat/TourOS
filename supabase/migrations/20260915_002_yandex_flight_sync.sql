-- ========================================================================================
-- TourOS - Dinamik Gerçek Uçuş Seferleri & marketplace_products Eşitleme (Yandex PostgreSQL)
-- ========================================================================================

CREATE OR REPLACE FUNCTION public.sync_flight_to_marketplace()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.marketplace_products (
        id,
        product_type,
        tour_name,
        hotel_name,
        operator_name,
        departure_city,
        departure_date,
        country,
        country_code,
        region,
        flight_number,
        airline,
        is_charter,
        is_direct_flight,
        baggage_kg,
        price,
        currency,
        is_instant_confirmation,
        is_active,
        is_published
    ) VALUES (
        NEW.id,
        'FLIGHT',
        NEW.airline_name || ' (' || NEW.flight_number || ') ' || NEW.departure_city || ' - ' || NEW.arrival_city,
        NEW.departure_city || ' ➔ ' || NEW.arrival_city || ' (' || NEW.flight_number || ')',
        COALESCE(NULLIF(NEW.operator_name, ''), NEW.airline_name),
        NEW.departure_city,
        NEW.departure_date,
        CASE WHEN NEW.arrival_city ILIKE '%Dubai%' THEN 'BAE'
             WHEN NEW.arrival_city ILIKE '%Hurgada%' OR NEW.arrival_city ILIKE '%Sharm%' THEN 'Mısır'
             WHEN NEW.arrival_city ILIKE '%Phuket%' OR NEW.arrival_city ILIKE '%Bangkok%' THEN 'Tayland'
             ELSE 'Türkiye' END,
        CASE WHEN NEW.arrival_city ILIKE '%Dubai%' THEN 'AE'
             WHEN NEW.arrival_city ILIKE '%Hurgada%' OR NEW.arrival_city ILIKE '%Sharm%' THEN 'EG'
             WHEN NEW.arrival_city ILIKE '%Phuket%' OR NEW.arrival_city ILIKE '%Bangkok%' THEN 'TH'
             ELSE 'TR' END,
        NEW.arrival_city,
        NEW.flight_number,
        NEW.airline_name,
        NEW.is_charter,
        NEW.is_direct,
        NEW.baggage_kg,
        NEW.price,
        NEW.currency,
        TRUE,
        TRUE,
        TRUE
    )
    ON CONFLICT (id) DO UPDATE SET
        price = EXCLUDED.price,
        departure_date = EXCLUDED.departure_date,
        airline = EXCLUDED.airline,
        flight_number = EXCLUDED.flight_number,
        updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_flight_to_marketplace ON public.flight_schedules;
CREATE TRIGGER trg_sync_flight_to_marketplace
    AFTER INSERT OR UPDATE ON public.flight_schedules
    FOR EACH ROW EXECUTE FUNCTION public.sync_flight_to_marketplace();

-- Mevcut ve yeni uçuşları ekle/tetikle
INSERT INTO public.flight_schedules (id, airline_name, flight_number, departure_city, arrival_city, departure_airport_code, arrival_airport_code, departure_date, departure_time, arrival_time, duration_minutes, is_charter, is_direct, baggage_kg, price, currency, operator_name)
VALUES
  ('fl-tk-ayt-oct1', 'Turkish Airlines', 'TK-3701', 'Moskova', 'Antalya', 'SVO', 'AYT', '2026-10-01', '06:15:00', '10:45:00', 270, false, true, 23, 240.00, 'EUR', 'Turkish Airlines'),
  ('fl-tk-ayt-oct2', 'Turkish Airlines', 'TK-3705', 'Moskova', 'Antalya', 'VKO', 'AYT', '2026-10-01', '09:30:00', '13:55:00', 265, false, true, 23, 260.00, 'EUR', 'Turkish Airlines'),
  ('fl-su-ayt-oct1', 'Aeroflot', 'SU-2140', 'Moskova', 'Antalya', 'SVO', 'AYT', '2026-10-01', '07:20:00', '12:00:00', 280, false, true, 23, 230.00, 'EUR', 'Aeroflot'),
  ('fl-n4-ayt-oct1', 'Nordwind Airlines', 'N4-5821', 'Moskova', 'Antalya', 'DME', 'AYT', '2026-10-01', '03:45:00', '08:15:00', 270, true, true, 20, 185.00, 'EUR', 'Pegas Touristik'),
  ('fl-pc-ayt-oct1', 'Pegasus Airlines', 'PC-1822', 'Moskova', 'Antalya', 'DME', 'AYT', '2026-10-01', '14:30:00', '19:00:00', 270, false, true, 20, 195.00, 'EUR', 'Pegasus Airlines'),
  ('fl-2s-ayt-oct1', 'Southwind Airlines', '2S-101', 'Moskova', 'Antalya', 'VKO', 'AYT', '2026-10-01', '23:30:00', '04:00:00', 270, true, true, 20, 180.00, 'EUR', 'Anex Tour'),
  ('fl-wz-ayt-oct1', 'Red Wings', 'WZ-3091', 'Moskova', 'Antalya', 'DME', 'AYT', '2026-10-01', '01:10:00', '05:40:00', 270, true, true, 20, 190.00, 'EUR', 'Fun&Sun'),
  ('fl-tk-bjv-oct1', 'Turkish Airlines', 'TK-3940', 'Moskova', 'Bodrum', 'VKO', 'BJV', '2026-10-01', '08:00:00', '12:30:00', 270, false, true, 23, 270.00, 'EUR', 'Turkish Airlines'),
  ('fl-n4-dlm-oct1', 'Nordwind Airlines', 'N4-5931', 'Moskova', 'Dalaman', 'SVO', 'DLM', '2026-10-01', '04:15:00', '08:50:00', 275, true, true, 20, 205.00, 'EUR', 'Pegas Touristik'),
  ('fl-fz-dxb-oct1', 'Flydubai', 'FZ-921', 'Moskova', 'Dubai', 'VKO', 'DXB', '2026-10-01', '14:40:00', '20:10:00', 330, false, true, 20, 310.00, 'EUR', 'Flydubai')
ON CONFLICT (id) DO UPDATE SET
  price = EXCLUDED.price,
  departure_date = EXCLUDED.departure_date,
  updated_at = NOW();

-- Eski 12 kaydı da tetikle
UPDATE public.flight_schedules SET updated_at = NOW();

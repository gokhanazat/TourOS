-- ========================================================================================
-- TourOS - Tur Operatörü Bazlı Uçuş Seferleri ve Alternatif Uçuş Eşleştirme Migration
-- Versiyon: 20260909_001
-- Açıklama: Seçilen paket turun tur operatörüne (operator_name) özel uçuş seferlerinin
--           (Charter / Blok / Tarifeli) listelenmesi ve fiyat farklarının yönetimi.
-- ========================================================================================

-- 1. flight_schedules Tablosu Operatör Kolonları ve İndeksleri
ALTER TABLE public.flight_schedules 
    ADD COLUMN IF NOT EXISTS operator_name TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS operator_code VARCHAR(50) DEFAULT '',
    ADD COLUMN IF NOT EXISTS price_delta_rub NUMERIC(10, 2) DEFAULT 0.0;

CREATE INDEX IF NOT EXISTS idx_flight_schedules_operator 
    ON public.flight_schedules (operator_name, departure_city, arrival_city);

CREATE INDEX IF NOT EXISTS idx_flight_schedules_op_date 
    ON public.flight_schedules (operator_name, departure_date);

-- 2. Operatöre Özel Uçuş Alternatiflerini Getiren RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.get_operator_flight_options(
    p_operator_name TEXT,
    p_departure_city TEXT DEFAULT '',
    p_arrival_city TEXT DEFAULT '',
    p_departure_date DATE DEFAULT NULL
)
RETURNS TABLE (
    id TEXT,
    airline_name TEXT,
    flight_number TEXT,
    departure_city TEXT,
    arrival_city TEXT,
    departure_airport_code VARCHAR(10),
    arrival_airport_code VARCHAR(10),
    departure_time TIME,
    arrival_time TIME,
    duration_minutes INT,
    is_charter BOOLEAN,
    baggage_kg INT,
    hand_baggage_kg INT,
    price_delta_rub NUMERIC,
    operator_name TEXT
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        fs.id,
        fs.airline_name,
        fs.flight_number,
        fs.departure_city,
        fs.arrival_city,
        COALESCE(fs.departure_airport_code, '') AS departure_airport_code,
        COALESCE(fs.arrival_airport_code, '') AS arrival_airport_code,
        fs.departure_time,
        fs.arrival_time,
        fs.duration_minutes,
        fs.is_charter,
        fs.baggage_kg,
        fs.hand_baggage_kg,
        COALESCE(fs.price_delta_rub, 0.0) AS price_delta_rub,
        COALESCE(fs.operator_name, '') AS operator_name
    FROM public.flight_schedules fs
    WHERE (
            p_operator_name IS NULL 
            OR p_operator_name = '' 
            OR LOWER(TRIM(fs.operator_name)) = LOWER(TRIM(p_operator_name))
            OR LOWER(fs.operator_name) LIKE '%' || LOWER(TRIM(p_operator_name)) || '%'
          )
      AND (
            p_departure_city = '' 
            OR LOWER(fs.departure_city) LIKE LOWER('%' || p_departure_city || '%')
          )
      AND (
            p_arrival_city = '' 
            OR LOWER(fs.arrival_city) LIKE LOWER('%' || p_arrival_city || '%')
          )
      AND (
            p_departure_date IS NULL 
            OR fs.departure_date = p_departure_date
          )
    ORDER BY COALESCE(fs.price_delta_rub, 0.0) ASC;
END;
$$;

-- RLS İzni
GRANT EXECUTE ON FUNCTION public.get_operator_flight_options(TEXT, TEXT, TEXT, DATE) TO authenticated, anon, service_role;

-- ============================================================================
-- Migration: 20261006_001_flight_null_safe_cleanup.sql
-- Description: flight_schedules tablosundaki gereksiz NOT NULL kısıtlarının
--              kaldırılması ve trg_sync_flight_to_marketplace tetikleyici
--              fonksiyonunun NULL değerlere karşı dayanıklı hale getirilmesi.
-- Note: Bu dosya yalnızca kayıt amaçlıdır, canlı veritabanında daha önce uygulanmıştır.
-- ============================================================================

-- 1. flight_schedules tablosundaki 4 sütunun NOT NULL kısıtının kaldırılması
ALTER TABLE public.flight_schedules
    ALTER COLUMN departure_time DROP NOT NULL,
    ALTER COLUMN arrival_time DROP NOT NULL,
    ALTER COLUMN airline_name DROP NOT NULL,
    ALTER COLUMN flight_number DROP NOT NULL;

-- 2. sync_flight_to_marketplace tetikleyici fonksiyonunun NULL-safe olarak güncellenmesi
CREATE OR REPLACE FUNCTION public.sync_flight_to_marketplace()
RETURNS trigger
LANGUAGE plpgsql
AS $function$
DECLARE
    v_clean_flight_no text;
    v_clean_airline text;
    v_clean_dep_city text;
    v_clean_arr_city text;
    v_tour_name text;
    v_hotel_name text;
BEGIN
    v_clean_flight_no := COALESCE(TRIM(NEW.flight_number), '');
    v_clean_airline   := COALESCE(TRIM(NEW.airline_name), '');
    v_clean_dep_city  := COALESCE(TRIM(NEW.departure_city), '');
    v_clean_arr_city  := COALESCE(TRIM(NEW.arrival_city), '');

    -- tour_name: Uçuş numarası ve havayolu boşsa parantez ve boşluk yazılmaz
    IF v_clean_airline <> '' AND v_clean_flight_no <> '' THEN
        v_tour_name := v_clean_airline || ' (' || v_clean_flight_no || ') ' || v_clean_dep_city || ' - ' || v_clean_arr_city;
    ELSIF v_clean_airline <> '' THEN
        v_tour_name := v_clean_airline || ' ' || v_clean_dep_city || ' - ' || v_clean_arr_city;
    ELSIF v_clean_flight_no <> '' THEN
        v_tour_name := '(' || v_clean_flight_no || ') ' || v_clean_dep_city || ' - ' || v_clean_arr_city;
    ELSE
        v_tour_name := v_clean_dep_city || ' - ' || v_clean_arr_city;
    END IF;

    -- hotel_name: Uçuş: Kalkış ➔ Varış (No varsa parantezli, yoksa parantezsiz)
    IF v_clean_flight_no <> '' THEN
        v_hotel_name := v_clean_dep_city || ' ➔ ' || v_clean_arr_city || ' (' || v_clean_flight_no || ')';
    ELSE
        v_hotel_name := v_clean_dep_city || ' ➔ ' || v_clean_arr_city;
    END IF;

    INSERT INTO public.marketplace_products (
        id, product_type, tour_name, hotel_name, operator_name, departure_city, departure_date,
        country, country_code, region, flight_number, airline, is_charter, is_direct_flight,
        baggage_kg, price, currency, is_instant_confirmation, is_active, is_published
    ) VALUES (
        NEW.id,
        'FLIGHT',
        v_tour_name,
        v_hotel_name,
        COALESCE(NULLIF(TRIM(NEW.operator_name), ''), v_clean_airline),
        v_clean_dep_city,
        NEW.departure_date,
        CASE 
             WHEN NEW.arrival_city ILIKE '%Dubai%' OR NEW.arrival_city ILIKE '%Шарджа%' OR NEW.arrival_city ILIKE '%Дубай%' OR NEW.arrival_city ILIKE '%Abu Dhabi%' THEN 'BAE'
             WHEN NEW.arrival_city ILIKE '%Hurgada%' OR NEW.arrival_city ILIKE '%Sharm%' OR NEW.arrival_city ILIKE '%Шарм%' OR NEW.arrival_city ILIKE '%Хургада%' THEN 'Mısır'
             WHEN NEW.arrival_city ILIKE '%Phuket%' OR NEW.arrival_city ILIKE '%Bangkok%' OR NEW.arrival_city ILIKE '%Пхукет%' OR NEW.arrival_city ILIKE '%Паттайя%' THEN 'Tayland'
             WHEN NEW.arrival_city ILIKE '%Хайнань%' OR NEW.arrival_city ILIKE '%Hainan%' OR NEW.arrival_city ILIKE '%Пекин%' THEN 'Çin'
             WHEN NEW.arrival_city ILIKE '%Гагр%' OR NEW.arrival_city ILIKE '%Пицунда%' OR NEW.arrival_city ILIKE '%Гудаут%' THEN 'Abhazya'
             WHEN NEW.arrival_city ILIKE '%Нячанг%' OR NEW.arrival_city ILIKE '%Фукуок%' OR NEW.arrival_city ILIKE '%Дананг%' THEN 'Vietnam'
             WHEN NEW.arrival_city ILIKE '%Мальдив%' OR NEW.arrival_city ILIKE '%Male%' THEN 'Maldivler'
             WHEN NEW.arrival_city ILIKE '%Сочи%' OR NEW.arrival_city ILIKE '%Москва%' OR NEW.arrival_city ILIKE '%Петербург%' THEN 'Rusya'
             WHEN NEW.arrival_city ILIKE '%Girne%' OR NEW.arrival_city ILIKE '%Lefkoşa%' OR NEW.arrival_city ILIKE '%Кипр%' THEN 'Kıbrıs'
             WHEN NEW.arrival_city ILIKE '%Batum%' OR NEW.arrival_city ILIKE '%Tiflis%' OR NEW.arrival_city ILIKE '%Грузия%' THEN 'Gürcistan'
             WHEN NEW.arrival_city ILIKE '%Bali%' OR NEW.arrival_city ILIKE '%Бали%' THEN 'Endonezya (Bali)'
             WHEN NEW.arrival_city ILIKE '%Zanzibar%' OR NEW.arrival_city ILIKE '%Занзибар%' THEN 'Zanzibar'
             WHEN NEW.arrival_city ILIKE '%Budva%' OR NEW.arrival_city ILIKE '%Kotor%' OR NEW.arrival_city ILIKE '%Tivat%' THEN 'Karadağ'
             WHEN NEW.arrival_city ILIKE '%Rodos%' OR NEW.arrival_city ILIKE '%Girit%' OR NEW.arrival_city ILIKE '%Крит%' THEN 'Yunanistan'
             WHEN NEW.arrival_city ILIKE '%Colombo%' OR NEW.arrival_city ILIKE '%Bentota%' THEN 'Sri Lanka'
             WHEN NEW.arrival_city ILIKE '%Mauritius%' OR NEW.arrival_city ILIKE '%Маврикий%' THEN 'Mauritius'
             WHEN NEW.arrival_city ILIKE '%Seychelles%' OR NEW.arrival_city ILIKE '%Mahe%' THEN 'Seyşeller'
             ELSE 'Türkiye' 
        END,
        CASE 
             WHEN NEW.arrival_city ILIKE '%Dubai%' OR NEW.arrival_city ILIKE '%Шарджа%' OR NEW.arrival_city ILIKE '%Дубай%' OR NEW.arrival_city ILIKE '%Abu Dhabi%' THEN 'AE'
             WHEN NEW.arrival_city ILIKE '%Hurgada%' OR NEW.arrival_city ILIKE '%Sharm%' OR NEW.arrival_city ILIKE '%Шарм%' OR NEW.arrival_city ILIKE '%Хургада%' THEN 'EG'
             WHEN NEW.arrival_city ILIKE '%Phuket%' OR NEW.arrival_city ILIKE '%Bangkok%' OR NEW.arrival_city ILIKE '%Пхукет%' OR NEW.arrival_city ILIKE '%Паттайя%' THEN 'TH'
             WHEN NEW.arrival_city ILIKE '%Хайнань%' OR NEW.arrival_city ILIKE '%Hainan%' OR NEW.arrival_city ILIKE '%Пекин%' THEN 'CN'
             WHEN NEW.arrival_city ILIKE '%Гагр%' OR NEW.arrival_city ILIKE '%Пицунда%' OR NEW.arrival_city ILIKE '%Гудаут%' THEN 'AB'
             WHEN NEW.arrival_city ILIKE '%Нячанг%' OR NEW.arrival_city ILIKE '%Фукуок%' OR NEW.arrival_city ILIKE '%Дананг%' THEN 'VN'
             WHEN NEW.arrival_city ILIKE '%Мальдив%' OR NEW.arrival_city ILIKE '%Male%' THEN 'MV'
             WHEN NEW.arrival_city ILIKE '%Сочи%' OR NEW.arrival_city ILIKE '%Москва%' OR NEW.arrival_city ILIKE '%Петербург%' THEN 'RU'
             WHEN NEW.arrival_city ILIKE '%Girne%' OR NEW.arrival_city ILIKE '%Lefkoşa%' OR NEW.arrival_city ILIKE '%Кипр%' THEN 'CY'
             WHEN NEW.arrival_city ILIKE '%Batum%' OR NEW.arrival_city ILIKE '%Tiflis%' OR NEW.arrival_city ILIKE '%Грузия%' THEN 'GE'
             WHEN NEW.arrival_city ILIKE '%Bali%' OR NEW.arrival_city ILIKE '%Бали%' THEN 'ID'
             WHEN NEW.arrival_city ILIKE '%Zanzibar%' OR NEW.arrival_city ILIKE '%Занзибар%' THEN 'TZ'
             WHEN NEW.arrival_city ILIKE '%Budva%' OR NEW.arrival_city ILIKE '%Kotor%' OR NEW.arrival_city ILIKE '%Tivat%' THEN 'ME'
             WHEN NEW.arrival_city ILIKE '%Rodos%' OR NEW.arrival_city ILIKE '%Girit%' OR NEW.arrival_city ILIKE '%Крит%' THEN 'GR'
             WHEN NEW.arrival_city ILIKE '%Colombo%' OR NEW.arrival_city ILIKE '%Bentota%' THEN 'LK'
             WHEN NEW.arrival_city ILIKE '%Mauritius%' OR NEW.arrival_city ILIKE '%Маврикий%' THEN 'MU'
             WHEN NEW.arrival_city ILIKE '%Seychelles%' OR NEW.arrival_city ILIKE '%Mahe%' THEN 'SC'
             ELSE 'TR' 
        END,
        v_clean_arr_city,
        v_clean_flight_no,
        v_clean_airline,
        COALESCE(NEW.is_charter, false),
        COALESCE(NEW.is_direct, true),
        COALESCE(NEW.baggage_kg, 20),
        COALESCE(NEW.price, 0),
        COALESCE(NEW.currency, 'RUB'),
        TRUE,
        TRUE,
        TRUE
    )
    ON CONFLICT (id) DO UPDATE SET
        price = EXCLUDED.price,
        departure_date = EXCLUDED.departure_date,
        airline = EXCLUDED.airline,
        flight_number = EXCLUDED.flight_number,
        tour_name = EXCLUDED.tour_name,
        hotel_name = EXCLUDED.hotel_name,
        is_active = TRUE,
        is_published = TRUE,
        updated_at = NOW();
    RETURN NEW;
END;
$function$;

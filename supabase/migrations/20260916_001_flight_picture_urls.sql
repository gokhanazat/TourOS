-- ========================================================================================
-- TourOS - Dinamik Uçuş Görselleri & marketplace_products Eşitleme (Yandex PostgreSQL)
-- ========================================================================================

-- 1. flight_schedules tablosuna picture_url kolonu ekle
ALTER TABLE public.flight_schedules 
ADD COLUMN IF NOT EXISTS picture_url TEXT DEFAULT '';

-- 2. Havayollarına göre yüksek kaliteli gerçek uçak/uçuş görselleri ata
UPDATE public.flight_schedules
SET picture_url = CASE 
    -- Türk Hava Yolları (THY)
    WHEN airline_name ILIKE '%Turkish%' OR airline_name ILIKE '%Türk Hava%' OR flight_number LIKE 'TK%'
        THEN 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&auto=format&fit=crop&q=80'
    WHEN airline_name ILIKE '%Pegasus%' OR flight_number LIKE 'PC%'
        THEN 'https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop&q=80'
    WHEN airline_name ILIKE '%SunExpress%' OR flight_number LIKE 'XQ%'
        THEN 'https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800&auto=format&fit=crop&q=80'
    WHEN airline_name ILIKE '%Aeroflot%' OR flight_number LIKE 'SU%'
        THEN 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800&auto=format&fit=crop&q=80'
    -- Nordwind Airlines
    WHEN airline_name ILIKE '%Nordwind%' OR flight_number ILIKE 'N4%' 
        THEN 'https://images.unsplash.com/photo-1519074069444-1ba4eae16e60?w=800&auto=format&fit=crop&q=80'
    -- Red Wings
    WHEN airline_name ILIKE '%Red Wings%' OR flight_number ILIKE 'WZ%' 
        THEN 'https://images.unsplash.com/photo-1520437358207-323b43b50729?w=800&auto=format&fit=crop&q=80'
    -- S7 Airlines
    WHEN airline_name ILIKE '%S7%' OR flight_number ILIKE 'S7%' 
        THEN 'https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800&auto=format&fit=crop&q=80'
    -- Emirates / FlyDubai
    WHEN airline_name ILIKE '%Emirates%' OR flight_number ILIKE 'EK%' OR airline_name ILIKE '%Flydubai%' OR flight_number ILIKE 'FZ%' 
        THEN 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80'
    -- Genel Charter & Özel Uçuş Görseli (Uçak bulutların üstünde)
    ELSE 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&auto=format&fit=crop&q=80'
END;

-- 3. Senkronizasyon trigger fonksiyonunu picture_url ile güncelle
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
        picture_url,
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
        COALESCE(NULLIF(NEW.picture_url, ''), 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&auto=format&fit=crop&q=80'),
        TRUE,
        TRUE,
        TRUE
    )
    ON CONFLICT (id) DO UPDATE SET
        price = EXCLUDED.price,
        departure_date = EXCLUDED.departure_date,
        airline = EXCLUDED.airline,
        picture_url = EXCLUDED.picture_url,
        updated_at = NOW();

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. marketplace_products tablosundaki mevcut tüm uçuşların picture_url alanını güncelle
UPDATE public.marketplace_products mp
SET picture_url = fs.picture_url
FROM public.flight_schedules fs
WHERE mp.id = fs.id;

-- 5. Eğer marketplace_products içinde olup flight_schedules ile id eşleşmeyen uçuş varsa onlara da havayoluna göre ata
UPDATE public.marketplace_products
SET picture_url = CASE 
    WHEN airline ILIKE '%Turkish%' OR flight_number ILIKE 'TK%' 
        THEN 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800&auto=format&fit=crop&q=80'
    WHEN airline ILIKE '%Pegasus%' OR flight_number ILIKE 'PC%' 
        THEN 'https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop&q=80'
    WHEN airline ILIKE '%Aeroflot%' OR flight_number ILIKE 'SU%' 
        THEN 'https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=800&auto=format&fit=crop&q=80'
    ELSE 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&auto=format&fit=crop&q=80'
END
WHERE (product_type = 'FLIGHT' OR hotel_name ILIKE 'Uçuş:%' OR hotel_name ILIKE '✈️%') 
  AND (picture_url IS NULL OR picture_url = '' OR picture_url NOT ILIKE 'http%');

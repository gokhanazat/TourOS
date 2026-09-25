-- 20260924_001_replace_fl_tv_with_charter.sql
-- FL-TV ve Charter Airlines kodlarını temizleme ve Charter olarak güncelleme

-- 1. FL-TV -> Charter
UPDATE marketplace_products 
SET flight_number = 'Charter' 
WHERE flight_number = 'FL-TV';

UPDATE marketplace_products 
SET tour_name = REPLACE(tour_name, '(FL-TV)', '(Charter)') 
WHERE tour_name ILIKE '%(FL-TV)%';

UPDATE marketplace_products 
SET tour_name = REPLACE(tour_name, 'FL-TV', 'Charter') 
WHERE tour_name ILIKE '%FL-TV%';

UPDATE marketplace_products 
SET hotel_name = REPLACE(hotel_name, '(FL-TV)', '(Charter)') 
WHERE hotel_name ILIKE '%(FL-TV)%';

UPDATE marketplace_products 
SET hotel_name = REPLACE(hotel_name, 'FL-TV', 'Charter') 
WHERE hotel_name ILIKE '%FL-TV%';

UPDATE flight_schedules 
SET flight_number = 'Charter' 
WHERE flight_number = 'FL-TV';

UPDATE marketplace_products_staging 
SET flight_number = 'Charter' 
WHERE flight_number = 'FL-TV';

-- 2. Charter Airlines -> Charter
UPDATE marketplace_products 
SET airline = 'Charter' 
WHERE airline ILIKE '%Charter Airlines%';

UPDATE marketplace_products 
SET tour_name = REPLACE(tour_name, 'Charter Airlines', 'Charter') 
WHERE tour_name ILIKE '%Charter Airlines%';

UPDATE marketplace_products 
SET hotel_name = REPLACE(hotel_name, 'Charter Airlines', 'Charter') 
WHERE hotel_name ILIKE '%Charter Airlines%';

UPDATE flight_schedules 
SET airline_name = 'Charter' 
WHERE airline_name ILIKE '%Charter Airlines%';

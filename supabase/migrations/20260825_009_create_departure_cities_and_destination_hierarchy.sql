-- Migration: 20260825_009_create_departure_cities_and_destination_hierarchy.sql
-- Description: Create Russian departure cities and hierarchical destination tables with Cyrillic & Latin support

-- 1. Rusya Kalkış Şehirleri ve Havalimanları Tablosu
CREATE TABLE IF NOT EXISTS public.departure_cities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_code VARCHAR(10) DEFAULT 'RU',
    country_name_ru VARCHAR(100) DEFAULT 'Россия',
    country_name_tr VARCHAR(100) DEFAULT 'Rusya',
    city_name_ru VARCHAR(150) NOT NULL,
    city_name_tr VARCHAR(150) NOT NULL,
    city_name_en VARCHAR(150) NOT NULL,
    airport_code VARCHAR(10), -- SVO, DME, VKO, LED, KZN, SVX vb.
    airport_name_ru VARCHAR(200),
    is_popular BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Hiyerarşik Destinasyonlar (Ülke -> Şehir -> Alt Belde) Tablosu
CREATE TABLE IF NOT EXISTS public.destination_hierarchy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    level VARCHAR(20) NOT NULL, -- 'COUNTRY', 'CITY', 'RESORT'
    parent_id UUID REFERENCES public.destination_hierarchy(id) ON DELETE CASCADE,
    name_tr VARCHAR(150) NOT NULL,
    name_ru VARCHAR(150) NOT NULL,
    name_en VARCHAR(150) NOT NULL,
    country_code VARCHAR(10) NOT NULL,
    airport_code VARCHAR(10),
    flag_emoji VARCHAR(10),
    is_popular BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Hızlı Arama İndeksleri
CREATE INDEX IF NOT EXISTS idx_departure_cities_ru ON public.departure_cities(city_name_ru);
CREATE INDEX IF NOT EXISTS idx_departure_cities_en ON public.departure_cities(city_name_en);
CREATE INDEX IF NOT EXISTS idx_departure_cities_code ON public.departure_cities(airport_code);
CREATE INDEX IF NOT EXISTS idx_dest_hierarchy_parent ON public.destination_hierarchy(parent_id);
CREATE INDEX IF NOT EXISTS idx_dest_hierarchy_level ON public.destination_hierarchy(level);

-- 4. Temel Rusya Kalkış Şehirleri Verisi
INSERT INTO public.departure_cities (country_code, city_name_ru, city_name_tr, city_name_en, airport_code, airport_name_ru, is_popular, display_order)
VALUES
    ('RU', 'Москва (Все аэропорты)', 'Moskova (Tüm Havalimanları)', 'Moscow (All Airports)', 'MOW', 'Шереметьево, Домодедово, Внуково', TRUE, 1),
    ('RU', 'Москва (Шереметьево)', 'Moskova (Şeremetyevo)', 'Moscow (Sheremetyevo)', 'SVO', 'Международный аэропорт Шереметьево', TRUE, 2),
    ('RU', 'Москва (Внуково)', 'Moskova (Vnukovo)', 'Moscow (Vnukovo)', 'VKO', 'Международный аэропорт Внуково', TRUE, 3),
    ('RU', 'Москва (Домодедово)', 'Moskova (Domodedovo)', 'Moscow (Domodedovo)', 'DME', 'Московский аэропорт Домодедово', TRUE, 4),
    ('RU', 'Санкт-Петербург', 'St. Petersburg', 'St. Petersburg', 'LED', 'Аэропорт Пулково', TRUE, 5),
    ('RU', 'Казань', 'Kazan', 'Kazan', 'KZN', 'Международный аэропорт Казань', TRUE, 6),
    ('RU', 'Екатеринбург', 'Yekaterinburg', 'Yekaterinburg', 'SVX', 'Международный аэропорт Кольцово', TRUE, 7),
    ('RU', 'Новосибирск', 'Novosibirsk', 'Novosibirsk', 'OVB', 'Международный аэропорт Толмачёво', TRUE, 8),
    ('RU', 'Самара', 'Samara', 'Samara', 'KUF', 'Международный аэропорт Курумоч', TRUE, 9),
    ('RU', 'Уфа', 'Ufa', 'Ufa', 'UFA', 'Международный аэропорт Уфа', TRUE, 10),
    ('RU', 'Нижний Новгород', 'Nizhny Novgorod', 'Nizhny Novgorod', 'GOJ', 'Международный аэропорт Чкалов', FALSE, 11),
    ('RU', 'Челябинск', 'Chelyabinsk', 'Chelyabinsk', 'CEK', 'Международный аэропорт Баландино', FALSE, 12),
    ('RU', 'Красноярск', 'Krasnoyarsk', 'Krasnoyarsk', 'KJA', 'Международный аэропорт Емельяново', FALSE, 13),
    ('RU', 'Сочи (Адлер)', 'Sochi (Adler)', 'Sochi (Adler)', 'AER', 'Международный аэропорт Сочи', TRUE, 14),
    ('RU', 'Пермь', 'Perm', 'Perm', 'PEE', 'Международный аэропорт Большое Савино', FALSE, 15),
    ('RU', 'Волгоград', 'Volgograd', 'Volgograd', 'VOG', 'Международный аэропорт Гумрак', FALSE, 16),
    ('RU', 'Омск', 'Omsk', 'Omsk', 'OMS', 'Омск-Центральный', FALSE, 17),
    ('RU', 'Тюмень', 'Tyumen', 'Tyumen', 'TJM', 'Международный аэропорт Рощино', FALSE, 18),
    ('RU', 'Минеральные Воды', 'Mineralnye Vody', 'Mineralnye Vody', 'MRV', 'Международный аэропорт Минеральные Воды', TRUE, 19),
    ('RU', 'Иркутск', 'Irkutsk', 'Irkutsk', 'IKT', 'Международный аэропорт Иркутск', FALSE, 20),
    ('RU', 'Калининград', 'Kaliningrad', 'Kaliningrad', 'KGD', 'Международный аэропорт Храброво', FALSE, 21)
ON CONFLICT DO NOTHING;

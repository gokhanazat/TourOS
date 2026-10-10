-- ============================================================
-- TourOS Migration: 20261005_001_localize_marketplace_destinations_to_russian.sql
-- Yandex Cloud PostgreSQL 16 - Ülke & Destinasyon Standartlaştırma (Rusça)
-- ============================================================

-- 1. marketplace_products tablosundaki Türkçe ülke isimlerini Rusça'ya çevir
UPDATE public.marketplace_products
SET country_name = CASE 
    WHEN country_code = 'RU' OR country_name ILIKE '%Rusya%' OR region ILIKE '%Moskova%' OR region ILIKE '%Sochi%' OR region ILIKE '%Сочи%' OR region ILIKE '%Kazan%' THEN 'Россия'
    WHEN country_code = 'TR' OR country_name ILIKE '%Türkiye%' OR country_name ILIKE '%Turkey%' THEN 'Турция'
    WHEN country_code = 'EG' OR country_name ILIKE '%Mısır%' OR country_name ILIKE '%Egypt%' THEN 'Египет'
    WHEN country_code = 'AE' OR country_name ILIKE '%BAE%' OR country_name ILIKE '%Dubai%' OR country_name ILIKE '%UAE%' THEN 'ОАЭ'
    WHEN country_code = 'TH' OR country_name ILIKE '%Tayland%' OR country_name ILIKE '%Thailand%' THEN 'Таиланд'
    WHEN country_code = 'VN' OR country_name ILIKE '%Vietnam%' THEN 'Вьетнам'
    WHEN country_code = 'MV' OR country_name ILIKE '%Maldiv%' THEN 'Мальдивы'
    WHEN country_code = 'CY' OR country_name ILIKE '%Kıbrıs%' OR country_name ILIKE '%Cyprus%' THEN 'Кипр'
    WHEN country_code = 'GE' OR country_name ILIKE '%Gürcistan%' OR country_name ILIKE '%Georgia%' THEN 'Грузия'
    WHEN country_code = 'SC' OR country_name ILIKE '%Seyşel%' OR country_name ILIKE '%Seychelles%' THEN 'Сейшелы'
    WHEN country_code = 'LK' OR country_name ILIKE '%Sri Lanka%' THEN 'Шри-Ланка'
    WHEN country_code = 'MU' OR country_name ILIKE '%Mauritius%' THEN 'Маврикий'
    WHEN country_code = 'ID' OR country_name ILIKE '%Endonezya%' OR country_name ILIKE '%Bali%' THEN 'Индонезия'
    WHEN country_code = 'TZ' OR country_name ILIKE '%Zanzibar%' OR country_name ILIKE '%Tanzanya%' THEN 'Занзибар'
    WHEN country_code = 'ME' OR country_name ILIKE '%Karadağ%' OR country_name ILIKE '%Montenegro%' THEN 'Черногория'
    WHEN country_code = 'GR' OR country_name ILIKE '%Yunanistan%' OR country_name ILIKE '%Greece%' THEN 'Греция'
    WHEN country_code = 'CN' OR country_name ILIKE '%Çin%' OR country_name ILIKE '%China%' THEN 'Китай'
    WHEN country_code = 'AB' OR country_name ILIKE '%Abhazya%' OR country_name ILIKE '%Abkhazia%' THEN 'Абхазия'
    ELSE country_name
END
WHERE country_name IN ('Rusya', 'Türkiye', 'Turkey', 'Mısır', 'Egypt', 'BAE', 'Birleşik Arap Emirlikleri', 'Tayland', 'Thailand', 'Vietnam', 'Maldivler', 'Kıbrıs', 'Gürcistan', 'Seyşeller', 'Karadağ', 'Yunanistan', 'Çin', 'Abhazya')
   OR country_code IN ('RU', 'TR', 'EG', 'AE', 'TH', 'VN', 'MV', 'CY', 'GE', 'SC', 'LK', 'MU', 'ID', 'TZ', 'ME', 'GR', 'CN', 'AB');

-- 2. destination_hierarchy alanını Rusça olarak güncelle
UPDATE public.marketplace_products
SET destination_hierarchy = CASE 
    -- Rusya Destinasyonları
    WHEN region ILIKE '%Sochi%' OR region ILIKE '%Сочи%' OR sub_region ILIKE '%Адлер%' OR sub_region ILIKE '%Лоо%' OR sub_region ILIKE '%Лазаревск%' OR hotel_name ILIKE '%Сочи%' OR hotel_name ILIKE '%Sochi%' THEN '🇷🇺 Россия · Сочи'
    WHEN region ILIKE '%Petersburg%' OR region ILIKE '%Петербург%' OR sub_region ILIKE '%Петербург%' THEN '🇷🇺 Россия · Санкт-Петербург'
    WHEN region ILIKE '%Kazan%' OR region ILIKE '%Казань%' THEN '🇷🇺 Россия · Казань'
    WHEN region ILIKE '%Kaliningrad%' OR region ILIKE '%Калининград%' THEN '🇷🇺 Россия · Калининград'
    WHEN region ILIKE '%Moskova%' OR region ILIKE '%Moscow%' OR region ILIKE '%Москва%' OR hotel_name ILIKE '%Cosmos Moscow%' THEN '🇷🇺 Россия · Москва'
    WHEN country_code = 'RU' THEN '🇷🇺 Россия · Москва'

    -- Türkiye Destinasyonları
    WHEN region ILIKE '%Kemer%' OR region ILIKE '%Кемер%' THEN '🇹🇷 Турция · Анталья · Кемер'
    WHEN region ILIKE '%Belek%' OR region ILIKE '%Белек%' THEN '🇹🇷 Турция · Анталья · Белек'
    WHEN region ILIKE '%Lara%' OR region ILIKE '%Лара%' OR region ILIKE '%Kundu%' OR region ILIKE '%Кунду%' THEN '🇹🇷 Турция · Анталья · Лара'
    WHEN region ILIKE '%Alanya%' OR region ILIKE '%Аланья%' THEN '🇹🇷 Турция · Анталья · Аланья'
    WHEN region ILIKE '%Side%' OR region ILIKE '%Сиде%' OR region ILIKE '%Manavgat%' OR region ILIKE '%Манавгат%' THEN '🇹🇷 Турция · Анталья · Сиде'
    WHEN region ILIKE '%Bodrum%' OR region ILIKE '%Бодрум%' THEN '🇹🇷 Турция · Мугла · Бодрум'
    WHEN region ILIKE '%Marmaris%' OR region ILIKE '%Мармарис%' THEN '🇹🇷 Турция · Мугла · Мармарис'
    WHEN region ILIKE '%Fethiye%' OR region ILIKE '%Фетхие%' THEN '🇹🇷 Турция · Мугла · Фетхие'
    WHEN region ILIKE '%İstanbul%' OR region ILIKE '%Istanbul%' OR region ILIKE '%Стамбул%' THEN '🇹🇷 Турция · Стамбул'
    WHEN region ILIKE '%Antalya%' OR region ILIKE '%Анталья%' THEN '🇹🇷 Турция · Анталья'

    -- Diğer Ülkeler
    WHEN region ILIKE '%Dubai%' OR region ILIKE '%Дубай%' OR country_code = 'AE' THEN '🇦🇪 ОАЭ · Дубай'
    WHEN region ILIKE '%Şarm%' OR region ILIKE '%Шарм%' OR region ILIKE '%Sharm%' THEN '🇪🇬 Египет · Шарм-эль-Шейх'
    WHEN region ILIKE '%Hurgada%' OR region ILIKE '%Хургада%' OR region ILIKE '%Hurghada%' THEN '🇪🇬 Египет · Хургада'
    WHEN region ILIKE '%Phuket%' OR region ILIKE '%Пхукет%' THEN '🇹🇭 Таиланд · Пхукет'
    WHEN region ILIKE '%Pattaya%' OR region ILIKE '%Паттайя%' THEN '🇹🇭 Таиланд · Паттайя'
    WHEN region ILIKE '%Nha Trang%' OR region ILIKE '%Нячанг%' THEN '🇻🇳 Вьетнам · Нячанг'
    WHEN region ILIKE '%Phu Quoc%' OR region ILIKE '%Фукуок%' THEN '🇻🇳 Вьетнам · Фукуок'

    ELSE destination_hierarchy
END
WHERE destination_hierarchy ILIKE '%Rusya%'
   OR destination_hierarchy ILIKE '%Türkiye%'
   OR destination_hierarchy ILIKE '%BAE%'
   OR destination_hierarchy ILIKE '%Mısır%'
   OR destination_hierarchy ILIKE '%Tayland%'
   OR country_code IN ('RU', 'TR', 'AE', 'EG', 'TH', 'VN');

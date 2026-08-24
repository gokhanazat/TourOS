-- ============================================================
-- TourOS Migration: 20260824_006_package_tours_country_hierarchy.sql
-- Paket Turlar Ülke & Coğrafi Hiyerarşi Standardizasyonu
-- ============================================================

-- 1. Ürünler tablosuna ülke kodu ve bayrak desteği
ALTER TABLE public.marketplace_products
    ADD COLUMN IF NOT EXISTS country_code VARCHAR(2) DEFAULT 'TR',
    ADD COLUMN IF NOT EXISTS country_name TEXT DEFAULT 'Türkiye',
    ADD COLUMN IF NOT EXISTS destination_hierarchy TEXT DEFAULT 'Türkiye · Antalya · Kemer';

-- 2. Mevcut ürünlerin ülke ve hiyerarşi verilerini güncelleme
UPDATE public.marketplace_products
SET 
    country_name = CASE 
        WHEN region ILIKE '%Moskova%' OR region ILIKE '%Sochi%' OR region ILIKE '%Kazan%' THEN 'Rusya'
        WHEN region ILIKE '%Dubai%' THEN 'Birleşik Arap Emirlikleri'
        WHEN region ILIKE '%Şarm%' OR region ILIKE '%Hurgada%' THEN 'Mısır'
        ELSE 'Türkiye'
    END,
    country_code = CASE 
        WHEN region ILIKE '%Moskova%' OR region ILIKE '%Sochi%' OR region ILIKE '%Kazan%' THEN 'RU'
        WHEN region ILIKE '%Dubai%' THEN 'AE'
        WHEN region ILIKE '%Şarm%' OR region ILIKE '%Hurgada%' THEN 'EG'
        ELSE 'TR'
    END,
    destination_hierarchy = CASE 
        WHEN region ILIKE '%Kemer%' THEN '🇹🇷 Türkiye · Antalya · Kemer'
        WHEN region ILIKE '%Belek%' THEN '🇹🇷 Türkiye · Antalya · Belek'
        WHEN region ILIKE '%Lara%' THEN '🇹🇷 Türkiye · Antalya · Lara'
        WHEN region ILIKE '%Alanya%' THEN '🇹🇷 Türkiye · Antalya · Alanya'
        WHEN region ILIKE '%Bodrum%' THEN '🇹🇷 Türkiye · Muğla · Bodrum'
        WHEN region ILIKE '%Marmaris%' THEN '🇹🇷 Türkiye · Muğla · Marmaris'
        WHEN region ILIKE '%Moskova%' THEN '🇷🇺 Rusya · Moskova'
        WHEN region ILIKE '%Dubai%' THEN '🇦🇪 BAE · Dubai'
        ELSE '🇹🇷 Türkiye · ' || COALESCE(region, 'Antalya')
    END;

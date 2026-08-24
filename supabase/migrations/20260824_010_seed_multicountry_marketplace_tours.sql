-- ============================================================
-- TourOS Migration: 20260824_010_seed_multicountry_marketplace_tours.sql
-- Mısır, Tayland, Vietnam, BAE (Dubai), Rusya ve Türkiye İçin Çoklu Ülke Paket Turlar ve Oteller
-- ============================================================

-- 1. Mevcut Türkiye Turlarını Normalize Et (country_code ve country alanları düzeltildi)
UPDATE public.marketplace_products
SET 
    country_code = 'TR',
    country_name = 'Türkiye',
    country = 'Türkiye'
WHERE country_code IS NULL OR country_code = '' OR country ILIKE '%Турция%' OR country ILIKE '%Turkey%' OR region ILIKE '%Antalya%' OR region ILIKE '%Belek%' OR region ILIKE '%Kemer%' OR region ILIKE '%Lara%' OR region ILIKE '%Alanya%' OR region ILIKE '%Side%' OR region ILIKE '%Bodrum%' OR region ILIKE '%Marmaris%';

-- 2. Çoklu Ülke Paket Turlar ve Otel Paketleri Ekleme
INSERT INTO public.marketplace_products (
    id, product_type, tour_name, operator_id, operator_name, operator_link, price, fuel_charge, currency,
    hotel_id, hotel_name, hotel_category, country, country_code, country_name, region, sub_region,
    room_type, meal_type, departure_city, departure_date, nights, adults, childs,
    is_charter, is_promo, airline_name, flight_number, baggage_kg, picture_url,
    hotel_rating, beach_line, is_instant_confirmation, has_transfer, is_direct_flight,
    amenities, is_published, is_active
)
VALUES
    -- 🇪🇬 MISIR (EG) - ŞARM EL-ŞEYH & HURGADA
    (
        'tour-seed-eg-1', 'PACKAGE_TOUR', 'Moskova (SVO) - Şarm El-Şeyh (SSH) Lüks Plaj Paketi', 1, 'Coral Travel', '', 850.00, 40.00, 'EUR',
        101, 'Rixos Premium Seagate Sharm', 5, 'Mısır', 'EG', 'Mısır', 'Şarm El-Şeyh', 'Nabq Bay',
        'Deluxe Aqua Sea View', 'Ultra Her Şey Dahil (UAI)', 'Moskova', '2026-09-05', 7, 2, 0,
        true, false, 'EgyptAir', 'MS-729', 23, 'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800',
        9.4, 1, true, true, true,
        ARRAY['Aquapark', 'Wi-Fi', 'SPA', 'Kum Plaj', 'Çocuk Kulübü', 'Havuz'], true, true
    ),
    (
        'tour-seed-eg-2', 'PACKAGE_TOUR', 'İstanbul (IST) - Hurgada (HRG) Kızıldeniz Tatil Paketi', 2, 'Anex Tour', '', 720.00, 35.00, 'EUR',
        102, 'Steigenberger ALDAU Beach Hotel', 5, 'Mısır', 'EG', 'Mısır', 'Hurgada', 'El Gouna',
        'Standard Sea Front Room', 'Her Şey Dahil (AI)', 'İstanbul', '2026-09-07', 7, 2, 0,
        true, true, 'Pegasus Airlines', 'PC-622', 20, 'https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800',
        9.1, 1, true, true, true,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz', 'Aquapark'], true, true
    ),
    (
        'tour-seed-eg-3', 'HOTEL', 'Four Seasons Resort Sharm El Sheikh', 3, 'Pegas Touristik', '', 1100.00, 0.00, 'EUR',
        103, 'Four Seasons Resort Sharm El Sheikh', 5, 'Mısır', 'EG', 'Mısır', 'Şarm El-Şeyh', 'Ras Um Sid',
        'Premier Suite Sea View', 'Oda Kahvaltı (BB)', 'Moskova', '2026-09-10', 7, 2, 0,
        false, false, '', '', 0, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800',
        9.7, 1, true, true, true,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz', 'Çocuk Kulübü'], true, true
    ),

    -- 🇹🇭 TAYLAND (TH) - PHUKET & PATTAYA & BANGKOK
    (
        'tour-seed-th-1', 'PACKAGE_TOUR', 'Moskova (VKO) - Phuket (HKT) Egzotik Ada Paketi', 3, 'Pegas Touristik', '', 1150.00, 50.00, 'EUR',
        201, 'JW Marriott Phuket Resort & Spa', 5, 'Tayland', 'TH', 'Tayland', 'Phuket', 'Mai Khao Beach',
        'Deluxe Garden View', 'Oda Kahvaltı (BB)', 'Moskova', '2026-09-12', 10, 2, 0,
        true, false, 'Nordwind Airlines', 'N4-2451', 20, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800',
        9.3, 1, true, true, true,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz', 'Çocuk Kulübü'], true, true
    ),
    (
        'tour-seed-th-2', 'PACKAGE_TOUR', 'İstanbul (IST) - Pattaya (UTP) Eğlence & Sahil Turu', 4, 'Fun & Sun', '', 890.00, 45.00, 'EUR',
        202, 'Centara Grand Mirage Beach Resort', 5, 'Tayland', 'TH', 'Tayland', 'Pattaya', 'Naklua Beach',
        'Deluxe Ocean Facing', 'Oda Kahvaltı (BB)', 'İstanbul', '2026-09-15', 7, 2, 0,
        true, true, 'Turkish Airlines', 'TK-68', 30, 'https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?w=800',
        9.0, 1, true, true, true,
        ARRAY['Aquapark', 'Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz'], true, true
    ),
    (
        'tour-seed-th-3', 'PACKAGE_TOUR', 'Moskova (SVO) - Koh Samui (USM) Tropikal Balayı Turu', 1, 'Coral Travel', '', 1380.00, 60.00, 'EUR',
        203, 'Banyan Tree Samui', 5, 'Tayland', 'TH', 'Tayland', 'Koh Samui', 'Lamai Beach',
        'Ocean Pool Villa', 'Oda Kahvaltı (BB)', 'Moskova', '2026-09-20', 10, 2, 0,
        false, false, 'Aeroflot', 'SU-270', 23, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800',
        9.8, 1, true, true, false,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz'], true, true
    ),

    -- 🇻🇳 VİETNAM (VN) - PHU QUOC & DA NANG
    (
        'tour-seed-vn-1', 'PACKAGE_TOUR', 'Moskova (SVO) - Phu Quoc (PQC) Tropikal Cennet Paketi', 2, 'Anex Tour', '', 980.00, 50.00, 'EUR',
        301, 'Vinpearl Resort & Spa Phu Quoc', 5, 'Vietnam', 'VN', 'Vietnam', 'Phu Quoc', 'Long Beach',
        'Deluxe King Room', 'Tam Pansiyon (FB)', 'Moskova', '2026-09-18', 10, 2, 0,
        true, false, 'Vietnam Airlines', 'VN-61', 23, 'https://images.unsplash.com/photo-1528127269322-539801943592?w=800',
        9.2, 1, true, true, true,
        ARRAY['Aquapark', 'Wi-Fi', 'SPA', 'Kum Plaj', 'Çocuk Kulübü', 'Havuz'], true, true
    ),
    (
        'tour-seed-vn-2', 'PACKAGE_TOUR', 'İstanbul (IST) - Da Nang (DAD) Sahil & Kültür Paketi', 1, 'Coral Travel', '', 1290.00, 45.00, 'EUR',
        302, 'InterContinental Danang Sun Peninsula Resort', 5, 'Vietnam', 'VN', 'Vietnam', 'Da Nang', 'Son Tra Peninsula',
        'Classic Resort View', 'Oda Kahvaltı (BB)', 'İstanbul', '2026-09-22', 7, 2, 0,
        false, false, 'Qatar Airways', 'QR-970', 25, 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800',
        9.6, 1, true, true, false,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz'], true, true
    ),

    -- 🇦🇪 BAE (DUBAİ) (AE) - DUBAI MARINA & PALM JUMEIRAH
    (
        'tour-seed-ae-1', 'PACKAGE_TOUR', 'Moskova (DME) - Dubai (DXB) Lüks Tatil & Aquaventure', 1, 'Coral Travel', '', 1450.00, 40.00, 'EUR',
        401, 'Atlantis The Palm Dubai', 5, 'BAE (Dubai)', 'AE', 'BAE', 'Dubai', 'Palm Jumeirah',
        'Ocean King Room', 'Yarım Pansiyon (HB)', 'Moskova', '2026-09-08', 7, 2, 0,
        true, false, 'Emirates', 'EK-132', 30, 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800',
        9.5, 1, true, true, true,
        ARRAY['Aquapark', 'Wi-Fi', 'SPA', 'Kum Plaj', 'Çocuk Kulübü', 'Havuz'], true, true
    ),
    (
        'tour-seed-ae-2', 'PACKAGE_TOUR', 'İstanbul (IST) - Dubai (DXB) Şehir & Marina Paketi', 2, 'Anex Tour', '', 990.00, 35.00, 'EUR',
        402, 'Rixos Premium Dubai JBR', 5, 'BAE (Dubai)', 'AE', 'BAE', 'Dubai', 'Dubai Marina',
        'Deluxe Walk View', 'Oda Kahvaltı (BB)', 'İstanbul', '2026-09-14', 5, 2, 0,
        true, true, 'flydubai', 'FZ-752', 20, 'https://images.unsplash.com/photo-1580674684081-7617fbf3d745?w=800',
        9.3, 1, true, true, true,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz'], true, true
    ),

    -- 🇷🇺 RUSYA (RU) - SOÇİ & KRASNAYA POLYANA
    (
        'tour-seed-ru-1', 'PACKAGE_TOUR', 'Moskova (SVO) - Soçi (AER) Karadeniz Sahil & Spa Turu', 5, 'Biblio-Globus', '', 680.00, 0.00, 'EUR',
        501, 'Radisson Collection Paradise Resort & Spa Sochi', 5, 'Rusya', 'RU', 'Rusya', 'Sochi', 'Adler',
        'Superior Room Sea View', 'Her Şey Dahil (AI)', 'Moskova', '2026-09-06', 7, 2, 0,
        true, false, 'Aeroflot', 'SU-1124', 23, 'https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800',
        9.2, 1, true, true, true,
        ARRAY['Wi-Fi', 'SPA', 'Kum Plaj', 'Havuz', 'Çocuk Kulübü'], true, true
    ),
    (
        'tour-seed-ru-2', 'PACKAGE_TOUR', 'Saint Petersburg (LED) - Soçi Dağ & Doğa Tatili', 3, 'Pegas Touristik', '', 540.00, 0.00, 'EUR',
        502, 'Krasnaya Polyana Mountain Resort Hotel', 5, 'Rusya', 'RU', 'Rusya', 'Sochi', 'Krasnaya Polyana',
        'Standard Mountain View', 'Oda Kahvaltı (BB)', 'Saint Petersburg', '2026-09-11', 6, 2, 0,
        true, true, 'Rossiya Airlines', 'FV-6561', 20, 'https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=800',
        8.9, 3, true, true, true,
        ARRAY['Wi-Fi', 'SPA', 'Havuz'], true, true
    )
ON CONFLICT (id) DO UPDATE SET
    country_code = EXCLUDED.country_code,
    country_name = EXCLUDED.country_name,
    country = EXCLUDED.country,
    region = EXCLUDED.region,
    sub_region = EXCLUDED.sub_region,
    price = EXCLUDED.price,
    hotel_name = EXCLUDED.hotel_name,
    is_published = true,
    is_active = true;

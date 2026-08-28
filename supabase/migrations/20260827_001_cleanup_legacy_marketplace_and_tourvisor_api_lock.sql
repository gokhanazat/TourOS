-- ==============================================================================
-- 20260827_001_cleanup_legacy_marketplace_and_tourvisor_api_lock.sql
-- YANDEX CLOUD & SUPABASE TEMİZLİK VE TOURVISOR API STANDARDİZASYONU
-- ==============================================================================

-- 1. Eski manuel, JSON import ve tohum (seed) verilerini temizle
DELETE FROM marketplace_products
WHERE id LIKE 'tour-seed-%' 
   OR id LIKE 'mock-%' 
   OR id LIKE 'local-hotel-%'
   OR (operator_name = 'Yerel Oteller' AND departure_city = 'Yerel Otel');

-- 2. API sağlayıcı ve senkronizasyon takip sütunlarını standartlaştır
ALTER TABLE marketplace_products 
ADD COLUMN IF NOT EXISTS api_provider VARCHAR(50) DEFAULT 'TOURVISOR',
ADD COLUMN IF NOT EXISTS is_api_synced BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMPTZ DEFAULT NOW();

-- 3. Mükerrer ürün kayıtlarını önlemek için benzersiz index
CREATE UNIQUE INDEX IF NOT EXISTS idx_marketplace_api_unique 
ON marketplace_products (id, operator_name, departure_city, hotel_name);

-- 4. Tourvisor API besleme kaynağının DataFeedSources tablosunda tanımlı olduğunu garantiye al
INSERT INTO data_feed_sources (
    id,
    source_name,
    endpoint_url,
    api_key,
    logo_icon,
    sync_interval,
    is_live,
    status_message,
    last_synced_at
)
VALUES (
    'feed-tourvisor-main',
    '🇷🇺 TourVisor API (Radar / Rusya & Global)',
    'http://tourvisor.ru/xml/list.php',
    'tourvisor_live_key',
    '🇷🇺',
    '30_MIN',
    true,
    '🟢 CANLI DEVREDE (Otomatik API Beslemesi)',
    NOW()::TEXT
)
ON CONFLICT (id) DO UPDATE SET
    is_live = true,
    status_message = '🟢 CANLI DEVREDE (Otomatik API Beslemesi)';

# TourOS - Fiyata Göre Tur Paketi Arama Mimari Dokümantasyonu

## 📋 Genel Bakış
Bu doküman, TourOS platformunda (Web, Desktop, Android, iOS) tur paketlerinin fiyat aralığına (`minPrice` / `maxPrice`) göre aranması ve filtrelemesine ilişkin teknik mimariyi ve kullanıcı arayüzü (UI/UX) standartlarını tanımlar.

---

## 📐 1. Kullanıcı Arayüzü & Konumlandırma Stratejisi (UI/UX)

Tüm platformlarda görsel tutarlılık (Visual Consistency) ve kullanım kolaylığı için fiyat filtrelemesi iki kademeli olarak yapılandırılmıştır:

### 1.1. Hero Arama Bloğu (Hızlı Bütçe Filtresi)
- **Konum:** Ana sayfa arama barı (Hero Search Bar) içerisinde Kişi Sayısı alanının yanında.
- **Bileşen:** Hızlı Bütçe Seçim Açılır Menüsü (Dropdown).
- **Varsayılan Seçenekler:**
  - 🌐 *Tüm Fiyatlar*
  - 💵 *0 ₺ - 25.000 ₺ (Ekonomik)*
  - 💳 *25.000 ₺ - 60.000 ₺ (Standart)*
  - 💎 *60.000 ₺ ve Üzeri (Lüks)*

### 1.2. Detaylı Arama Sonuç Sayfası (Hassas Filtreleme)
- **Konum:** Web/Desktop platformlarında Sol Sticky Yan Panel; Android/iOS platformlarında Alt Filtre Sheet (`BottomSheetDialog`).
- **Bileşenler:**
  - Min / Max Fiyat Sayısal Girdileri (Input Fields).
  - Çift Yönlü Fiyat Aralığı Kaydırıcısı (Range Slider Component).

### 1.3. Platformlar Arası Görsel Dil Matrisi

| Platform | Ana Arama Bloğu Konumu | Sonuç Sayfası Konumu |
|---|---|---|
| **Web Browser** | Hero Bar Dropdown | Sol Yan Panel Sticky Filter & Slider |
| **Desktop App** | Hero Bar Dropdown | Sol Yan Panel Sticky Filter & Slider |
| **Android App** | Hero Card Filter Chip | BottomSheetDialog & Range Slider |
| **iOS App** | Hero Card Filter Chip | BottomSheetDialog & Range Slider |

---

## ⚙️ 2. Domain & UseCase Katmanı

Filtreleme parametreleri KMP (`shared`) katmanında `B2CTourSearchFilter` data class'ı üzerinden yönetilir:

```kotlin
// com.mgacreative.touros.domain.model.B2CTourSearchFilter
data class B2CTourSearchFilter(
    val category: String? = null,
    val country: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val searchQuery: String = ""
)
```

`SearchB2CToursUseCase` sınıfı, bu filtreyi Supabase RPC fonksiyonuna iletir. RPC'den veri dönmemesi durumunda in-memory yedek filtreyi (Fallback) çalıştırır:

```kotlin
val matchesPrice = (filter.minPrice == null || item.price >= filter.minPrice) && 
                   (filter.maxPrice == null || item.price <= filter.maxPrice)
```

---

## 🗄️ 3. Veritabanı & SQL Katmanı

Supabase veritabanında fiyat filtresini çalıştıran RPC SQL fonksiyonu (`search_b2c_tours`):

```sql
-- ============================================================
-- TourOS 4.2.1 B2C Customer Mobile App Tour Search & Filter RPC SQL
-- ============================================================

CREATE OR REPLACE FUNCTION public.search_b2c_tours(
    p_tenant_id UUID,
    p_category TEXT DEFAULT NULL,
    p_country TEXT DEFAULT NULL,
    p_min_price NUMERIC(14,2) DEFAULT NULL,
    p_max_price NUMERIC(14,2) DEFAULT NULL,
    p_start_date DATE DEFAULT NULL,
    p_end_date DATE DEFAULT NULL,
    p_search_query TEXT DEFAULT NULL
)
RETURNS TABLE (
    tour_id UUID,
    tour_code TEXT,
    title TEXT,
    category TEXT,
    destination_country TEXT,
    duration_days INT,
    price NUMERIC(14,2),
    currency TEXT,
    rating NUMERIC(3,2),
    review_count INT,
    cover_image_url TEXT,
    next_departure_date DATE
)
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id AS tour_id,
        COALESCE(t.code, 'TUR-' || UPPER(SUBSTRING(t.title FROM 1 FOR 3))) AS tour_code,
        t.title,
        COALESCE(t.category, 'Kültür Turu') AS category,
        COALESCE(t.destination_country, 'Türkiye') AS destination_country,
        COALESCE(t.duration_days, 3) AS duration_days,
        COALESCE(t.price, 2500.00)::NUMERIC(14,2) AS price,
        'TRY'::TEXT AS currency,
        4.85::NUMERIC(3,2) AS rating,
        124 AS review_count,
        COALESCE(t.cover_image_url, 'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff') AS cover_image_url,
        (CURRENT_DATE + INTERVAL '7 days')::DATE AS next_departure_date
    FROM public.tours t
    WHERE t.tenant_id = p_tenant_id
      AND (p_category IS NULL OR p_category = '' OR t.category ILIKE '%' || p_category || '%')
      AND (p_country IS NULL OR p_country = '' OR t.destination_country ILIKE '%' || p_country || '%')
      AND (p_min_price IS NULL OR t.price >= p_min_price)
      AND (p_max_price IS NULL OR t.price <= p_max_price)
      AND (p_search_query IS NULL OR p_search_query = '' OR t.title ILIKE '%' || p_search_query || '%')
    ORDER BY t.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

## 📌 Özet & Sonuç
- **Arama Altyapısı:** Veritabanı ve KMP kod katmanında mevcuttur ve aktiftir.
- **UI Standartı:** Hero arama bloğunda Hızlı Bütçe Dropdown'u, detaylı sonuçlarda Range Slider mimarisi esas alınmıştır.

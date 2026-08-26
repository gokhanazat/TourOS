# TourOS Çoklu Tur Operatörü (Multi-TO) Veritabanı ve Entegrasyon Mimarisi

Bu doküman, TourOS platformunun tek bir operatöre bağımlı kalmadan birden fazla tur operatörü (Coral, Anex, Pegas, Odeon, Tez Tour vb.) ve GDS/OTA sağlayıcıları ile entegre çalışmasını sağlayan **Unified Aggregator & Meta-Search Hub** mimarisini ve veritabanı şemasını tanımlar.

---

## 1. Mimari Prensipler

1. **Adapter Pattern (İzole Sürücüler):** Her operatörün API'si (XML/JSON/SOAP) bağımsız bir adapter sınıfı ile ortak `UnifiedProduct` modeline dönüştürülür.
2. **Circuit Breaker (Sigorta):** Bir operatör API'sinde yavaşlama veya kesinti olduğunda diğer operatörlerin aramaları ve rezervasyonları etkilenmez.
3. **Master Entity Matching (Mükerrer Veri Eşleme):** Farklı operatörlerdeki aynı otel ve destinasyonlar tek bir Master ID altında birleştirilir ve fiyat karşılaştırmalı sunulur.
4. **Zero-Downtime & Feature Flag:** Çoklu operatör mimarisi mevcut tek operatörlü canlı akışı bozmadan kademeli olarak devreye alınır.

---

## 2. Veritabanı Şeması (Multi-Operator Schema)

### A. Operatörler Tablosu (`operators`)
```sql
CREATE TABLE IF NOT EXISTS public.operators (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50) UNIQUE NOT NULL,      -- CORAL, ANEX, PEGAS, TEZ
    name            VARCHAR(150) NOT NULL,
    api_endpoint    TEXT,
    api_auth_type   VARCHAR(50) DEFAULT 'BEARER',     -- BEARER, BASIC, API_KEY
    is_active       BOOLEAN DEFAULT TRUE,
    timeout_ms      INT DEFAULT 8000,
    created_at      TIMESTAMPTZ DEFAULT now()
);
```

### B. Master Otel Eşleme Tablosu (`master_hotel_mappings`)
```sql
CREATE TABLE IF NOT EXISTS public.master_hotel_mappings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    master_hotel_id UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    operator_id     UUID NOT NULL REFERENCES public.operators(id) ON DELETE CASCADE,
    operator_hotel_code VARCHAR(100) NOT NULL,        -- Operatörün kendi otel ID'si
    operator_hotel_name VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT now(),
    UNIQUE (operator_id, operator_hotel_code)
);
```

### C. Çoklu Operatör Fiyat & Kontenjan Önbelleği (`operator_price_cache`)
```sql
CREATE TABLE IF NOT EXISTS public.operator_price_cache (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    master_hotel_id UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    operator_id     UUID NOT NULL REFERENCES public.operators(id) ON DELETE CASCADE,
    departure_date  DATE NOT NULL,
    nights          INT NOT NULL,
    room_type       VARCHAR(100),
    meal_type       VARCHAR(100),
    price_original  NUMERIC(14,2) NOT NULL,
    currency        VARCHAR(10) NOT NULL DEFAULT 'EUR',
    price_try       NUMERIC(14,2) NOT NULL,
    is_instant_confirm BOOLEAN DEFAULT FALSE,
    updated_at      TIMESTAMPTZ DEFAULT now()
);
```

---

## 3. Arama & Rezervasyon Akış Şeması

```
[ Acente / B2B Kullanıcı Arama İsteği ]
                   │
                   ▼
       [ Search Dispatcher (KMP) ]
    ┌──────────────┼──────────────┐
    ▼              ▼              ▼
[ Coral Adapter ] [ Anex Adapter ] [ Pegas Adapter ]
 (Paralel API)    (Paralel API)    (Paralel API)
    └──────────────┬──────────────┘
                   ▼
     [ Deduplication & Price Ranker ]
   (Aynı Oteller Eşlenir, En Uygun Fiyat Seçilir)
                   │
                   ▼
      [ TourOS B2B Sonuç Ekranı ]
   ("Coral: 1.200 € | Anex: 1.150 € ⚡ En İyi")
                   │
                   ▼
       [ Smart Booking Router ]
  (Seçilen Operatörün API'sine PNR Açılır)
```

---

## 4. Geliştirme ve Canlıya Geçiş Stratejisi

1. **İzole Dal (Branch):** Geliştirme `feature/multi-operator-aggregator` dalında yürütülür.
2. **Yandex Cloud Master Veritabanı:** Rusya ve küresel acentelerin doğrudan eriştiği Yandex Cloud altyapısı korunur.
3. **Kademeli Açılış (Feature Flag):** `enable_multi_operator = true` parametresi ile önce seçili test acentelerine, ardından genel kullanıma açılır.

# TourOS Agent Handoff Document - Katalog Ürün Yayınlama & Veri Kalıcılığı Raporu

## 📌 1. Sorun Özeti (Bug & Root Cause)
- **Ekran:** Katalog Ürün Yayınlama Yönetimi (`AgencyProductPublishingScreen`) & Canlı Web Platformu (`GlobalWebPublicScreen`)
- **Şikayet:** "Toplu Veri Yükle" butonu ile yüklenen 2.966+ tur operatörü verisi (JSON/TXT), uygulama güncellendiğinde, yeniden başlatıldığında veya ekrandan çıkılıp tekrar girildiğinde siliniyor ve kayboluyor.
- **Kök Nedenler (Root Causes):**
  1. **Supabase PostgREST 1.000 Satır Limiti:** Supabasevarsayılan olarak `.select()` sorgularında 1.000 satırdan sonrasını getirmez. Yüklenen 2.966 ürünün 1.966'sı sorgu sırasında kesintiye uğruyordu.
  2. **Koin ViewModel Yaşam Döngüsü (`factory` vs `single`):** `AgencyProductPublishingViewModel`, Koin modülünde `factory` olarak tanımlanmıştı. Ekran her değiştiğinde ViewModel sıfırdan oluşturuluyor ve RAM hafızası (`persistentMemoryProducts`) temizleniyordu.
  3. **Deterministik Olmayan (Rastgele) ID Üretimi:** İlk sürümlerde döngü indeksine (`$index`, `idx+1`) veya rastgele UUID'ye bağlı ID üretiliyordu. Uygulama her açıldığında farklı ID üretildiği için Supabase `UPSERT` işlemi kayıtları eşleştiremiyor ve veritabanındaki eski kayıtlar kayboluyordu.
  4. **PostgreSQL RLS ve Eksik Sütun Reddi:** `public.marketplace_products` SQL tablosunda `is_published`, `custom_price_override`, `picture` ve `picture_url` sütunları veya `GRANT ALL` izinleri eksik olduğunda Supabase HTTP 400/403 döndürüyor, `upsert` başarısız oluyor ve veriler DB'ye işlenemeden sadece geçici RAM'de kalıyordu.
  5. **Senkron Ağ Takılması (UI Hang):** 3.000 ürün veritabanına 100'erli paketlerle senkron olarak yazılmaya çalışıldığında UI donuyor ve kullanıcı pencereyi kapattığında işlem yarıda kesiliyordu.

---

## 🛠️ 2. Yapılan Değişiklikler ve Düzeltmeler

1. **ViewModel Singleton Tanımı (`AppModule.kt`):**
   - `AgencyProductPublishingViewModel` tanımı Koin modülünde `single` yapıldı. ViewModel nesnesi ve yüklenen veriler oturum boyunca yaşamaya devam eder.

2. **Gelişmiş PostgREST Sorgu Limiti (`AgencyProductPublishingViewModel.kt` & `GlobalWebPublicScreen.kt`):**
   - Supabase `.select()` çağrılarına `.select { range(0, 20000) }` parametresi eklendi. 20.000 kayda kadar veri veritabanından eksiksiz çekilir.

3. **Deterministik Hash ID Üretimi (`AgencyProductPublishingViewModel.kt`):**
   - Rastgele veya indeks bazlı ID üretimi tamamen kaldırıldı. Nesnenin değişmez alanlarından (`opId`, `name`, `hotelName`, `country`, `region`, `departureCity`, `date`, `price`, `roomType`, `mealType`) sabit Hash ID (`tour-${hotelId}-${hashCode}`) türetildi.

4. **Arka Plan Asenkron Upsert & Anında UI Kapanışı:**
   - JSON ayrıştırma tamamlandığı an (1 saniyenin altında) RAM ve UI güncellenir, modal kapanır. Veritabanı kayıtları arka planda (`Dispatchers.IO`) 250'şerli paketlerle işlenir.

5. **RAM Hafıza Koruma Mantığı:**
   - `loadCatalog()` metodu veritabanı boş dönse veya ağ hatası alsa dahi RAM'deki `persistentMemoryProducts` listesini asla `.clear()` ederek silmez.

6. **Global Web Entegrasyonu (`GlobalWebPublicScreen.kt`):**
   - Yerel `tours` ve `hotels` tabloları kaldırıldı. Global Web ekranı doğrudan `marketplace_products` tablosundaki yüklenmiş tur operatörü verilerine bağlandı.

7. **SQL Migrasyon Scripti (`20260811_036_fix_marketplace_products_batch_limits.sql`):**
   - Tüm sütunlar (`picture`, `picture_url`, `is_published`, `custom_price_override`, `surcharges_json`, `extra_services_json`) eklendi. RLS ve `GRANT ALL` izinleri yapılandırıldı.

---

## 🚀 3. Senden Sonraki Agent İçin Yapılabilecekler ve Tavsiyeler (Next Steps)

Eğer veriler uygulama güncellemesinde hala kayboluyorsa aşağıdaki adımlar uygulanmalıdır:

1. **Supabase SQL Migrasyonunu Çalıştırın:**
   - `supabase/migrations/20260811_036_fix_marketplace_products_batch_limits.sql` dosyasındaki SQL kodunun Supabase SQL Editor üzerinden çalıştırıldığından emin olun. RLS veya yetki eksikliği verilerin PostgreSQL'e yazılmasını engelliyor olabilir.

2. **Yerel Disk Önbelleği (Local Disk Storage Caching):**
   - Uygulama kapandığında veya güncellendiğinde Supabase bağlantısı olmasa dahi verilerin %100 korunması için `persistentMemoryProducts` listesini yerel bir `catalog_cache.json` dosyasına (veya `Settings/Preferences` içerisine) kaydetme ve uygulama açılışında bu dosyadan okuma mekanizması eklenebilir.

3. **Deterministik ID Çakışma Kontrolü:**
   - JSON içerisinden gelen verilerde `id` alanı boş ise üretilen hash ID'lerin Benzersiz (Unique Constraint) olduğunu doğrulayın.

---

## 📋 4. Ek SQL Kodu
Supabase üzerinde çalıştırılması gereken migrasyon dosyası:
- [20260811_036_fix_marketplace_products_batch_limits.sql](file:///d:/AndroidStudioProjects/TourOS/supabase/migrations/20260811_036_fix_marketplace_products_batch_limits.sql)

---
*Rapor Tarihi: 11 Ağustos 2026*

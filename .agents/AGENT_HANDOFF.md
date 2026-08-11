# TourOS Agent Handoff Document

## Son Yapılan İşlem Özeti (Katalog Ürün Yayınlama Veri Kaybı Düzeltmesi)

### Sorun Tespiti (Bug & Root Cause)
- **Ekran:** Katalog Ürün Yayınlama Yönetimi (`AgencyProductPublishingScreen`)
- **Şikayet:** JSON dosyası ile operatör ürünleri yükleniyor, ancak sayfa güncellendiğinde/yenilendiğinde ürünler siliniyordu.
- **Kök Neden:** Kotlin tarafındaki `UnifiedProductEntity` veri sınıfında yer alan `is_published` ve `custom_price_override` sütunları Supabase `public.marketplace_products` SQL tablosunda eksikti (`20260810_025_create_marketplace_products_table.sql` sürümünde eklenmemişti).
- PostgREST üzerinden `upsert` atıldığında Supabase 400 Bad Request hatası döndürüyor ve kayıtlar DB'ye hiç işlenmiyordu. Sadece ViewModel RAM'inde kaldığı için ilk yenilemede 0 kayıt dönüyor ve veriler kayboluyordu.

---

### Çözüm & Yapılan Değişiklikler

1. **Supabase SQL Migrasyonu Ekledi:**
   - [supabase/migrations/20260810_030_add_publishing_columns_to_marketplace_products.sql](file:///d:/AndroidStudioProjects/TourOS/supabase/migrations/20260810_030_add_publishing_columns_to_marketplace_products.sql)
   ```sql
   ALTER TABLE public.marketplace_products 
       ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true,
       ADD COLUMN IF NOT EXISTS custom_price_override NUMERIC(12,2) DEFAULT NULL;

   GRANT ALL ON TABLE public.marketplace_products TO anon;
   GRANT ALL ON TABLE public.marketplace_products TO authenticated;
   GRANT ALL ON TABLE public.marketplace_products TO service_role;
   ```

2. **ViewModel Veri Akışı Senkronizasyonu:**
   - `AgencyProductPublishingViewModel.kt` içerisindeki `importRawJsonPayload`, `togglePublishStatus` ve `loadCatalog` akışları Supabase DB ile tam uyumlu hale getirildi.

3. **Derleme Doğrulaması:**
   - Shared Module: `./gradlew :shared:compileKotlinJvm` ➔ **SUCCESS**
   - Android Target: `./gradlew :androidApp:assembleDebug` ➔ **SUCCESS**
   - Desktop Target: `./gradlew :desktopApp:compileKotlinJvm` ➔ **SUCCESS**

---

## Sonraki Agent İçin Önemli Kurallar & Hatırlatmalar

1. **Veritabanı Sütun Tutarlılığı:** `UnifiedProductEntity` veya diğer entity sınıflarına yeni bir alan eklendiğinde Supabase migration dosyası da mutlaka yazılmalıdır.
2. **Kullanıcı Kuralları (Must Follow):**
   - Prompt dışına çıkma, istek dışı kod değiştirmeyin veya silmeyin.
   - Sormadan hiçbir bileşeni/dosyayı kaldırmayın.
   - Veritabanını ilgilendiren her işlem sonrasında kullanıcının kopyalayabileceği eksiksiz **SQL çıktısını** verin.
   - Web/Desktop/Android/iOS platformlarında aynı tasarım dilini koruyun.
   - İşlemler tamamlandığında projeyi temiz şekilde build edip teslim edin.

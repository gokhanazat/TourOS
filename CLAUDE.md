# TourOS — AI Ajan Bağlam Dosyası

> Bu dosya Claude, Antigravity ve diğer AI kodlama ajanları için projenin özetidir.
> Kod her zaman nihai kaynaktır; burada yazanla kod çelişirse kodu esas al ve bu dosyayı güncellemeyi öner.
> Son güncelleme: 26 Eylül 2026

---

## 1. Ürün

- **TourOS**: Tur operatörleri ve acenteler için multi-tenant SaaS (tur, rezervasyon, otel, finans, raporlama, B2B/B2C portal).
- **Axileto**: Canlıdaki B2B Travel Market web ön yüzü → https://axileto.com (Wasm build, Yandex Cloud VM).
- Hedef pazar ağırlıklı olarak Rusya çıkışlı paket tur pazarı (kalkış şehirleri RU, fiyatlar çoğunlukla RUB, veri kaynağı Tourvisor).
- Pazaryeri modeli: `companies.company_type` = tur operatörü / acente. Acenteler birden fazla operatöre bağlanır (`agency_operator_connections`), ürünleri kendi vitrininde yayınlar ve komisyon kazanır.

---

## 2. KESİN KURALLAR (RULES.md / GEMINI.md / .cursorrules özeti)

1. **Kapsam:** Sadece istenen dosya/görev üzerinde çalış. Başka hiçbir kodu değiştirme, silme, yeniden yapılandırma.
2. **Silme yasağı:** Onay almadan hiçbir dosya, sınıf, fonksiyon veya bileşen kaldırılmaz.
3. **Belirsizlik:** Tahmin yürütme; sor.
4. **SQL:** Veritabanını etkileyen her değişiklikte eksiksiz SQL ver (Yandex üzerindeki PostgreSQL'de çalışacak formatta), `supabase/migrations/` altına `YYYYMMDD_NNN_aciklama.sql` adıyla ekle.
5. **Tasarım bütünlüğü:** Web, Desktop, Android, iOS aynı tasarım dili, tema ve bileşenleri kullanır.
6. **"Soru" / "düşünüyorum" / `SORUnotu`:** Kod yazma, dosya değiştirme; sadece analiz et ve yanıtla.
7. **"Sorun" ile başlayan mesaj:** Derin kök neden analizi + çözüm.
8. **🔒 SEARCH MODÜLÜ KİLİTLİ:** Arama ile ilgili hiçbir UI/ViewModel/Repository/UseCase/servis değiştirilmez, taşınmaz, silinmez. Değişiklik gerekiyorsa kod yazma; nedenini açıkla ve onay bekle. Kilitli dosyalar (en az):
   - `ui/screens/B2BTourSearchDashboardScreen.kt`, `ui/screens/AgencySearchScreen.kt`
   - `ui/viewmodel/B2BTourSearchViewModel.kt`
   - `ui/components/UniversalTourSearchBar.kt`
   - `data/remote/TourSearchProxyService.kt`, `ai/adapter/AITourSearchAdapter.kt`
   - Arama RPC'leri (`*search*` migration'ları) ve `supabase/functions/tour-search-proxy`
9. **Backend mantığı nereye yazılır (kesin karar):**
   - Supabase Cloud (supabase.co) ve Deno tabanlı Edge Function'lar **kullanılmaz**.
   - Merkezi iş mantığı (veri işleme, atomik hesaplama, yetki, raporlama, kota) → Yandex PostgreSQL'de **SQL/RPC** (`CREATE FUNCTION ... LANGUAGE plpgsql`) + `supabase/migrations/` altına migration dosyası.
   - Sadece UI hesaplamaları, yerel filtreleme, formatlama → Kotlin (`shared/commonMain`).
   - **Online ödeme yok:** TourOS ticari yazılım olarak kullanıcılara doğrudan satılır; herhangi bir ödeme sağlayıcısından (Stripe, iyzico vb.) ödeme alınmaz. `supabase/functions/payment-webhook` kullanılmıyor — ödeme entegrasyonu önerme/ekleme.
   - `supabase/functions/tour-search-proxy` + `TourSearchProxyService.kt` yalnızca DI'da kayıtlı, hiçbir ekran/ViewModel kullanmıyor (🔒 search kapsamında, dokunma).

---

## 3. Teknoloji

| Alan | Kullanılan |
|---|---|
| Dil / UI | Kotlin 2.4.10, Compose Multiplatform 1.11.1, Material3 1.11.0-alpha07, M3 Adaptive |
| Hedefler | Android (min 26, target 36, compile 37), iOS, Desktop (JVM), Web (wasmJs + js) |
| DI | Koin 4.2.2 (BOM) |
| Navigasyon | navigation-compose 2.9.2, type-safe `@Serializable` route'lar |
| Backend | supabase-kt 3.6.0 (auth, postgrest, realtime, storage, functions) |
| Ağ | Ktor 3.5.1 (okhttp / darwin / cio / js motorları) |
| Görsel | Coil 3.1.0 |
| Build | AGP 9.1.1, Gradle version catalog (`gradle/libs.versions.toml`) |
| Test | kotlin-test, Playwright (`e2e/`), k6 yük testi (`tools/k6`, `load-test.js`) |

**Altyapı:** Her şey Yandex Cloud'da. VM `touros-supabase-prod` üzerinde PostgreSQL 16 + self-hosted Supabase (PostgREST/Auth/Storage). Supabase ayrı bir veritabanı değil, bu PostgreSQL'in API katmanıdır. Tüm platformlar `SupabaseConfig` üzerinden **`https://api.axileto.com`** adresine bağlanır (iOS/JVM'de `SUPABASE_URL` ortam değişkeniyle ezilebilir). Web ön yüzü `https://axileto.com` (`deploy-yandex.ps1`).

---

## 4. Modül Yapısı

```
TourOS/
├── shared/          ← Tüm iş mantığı ve UI (commonMain) — asıl kod burada
├── androidApp/      ← Android giriş noktası
├── iosApp/          ← Xcode projesi (Swift giriş noktası)
├── desktopApp/      ← JVM main
├── webApp/          ← wasmJs/js main + index.html, görseller
├── supabase/
│   ├── migrations/  ← Kanonik şema + RPC'ler (~230 dosya)
│   └── functions/   ← payment-webhook, tour-search-proxy (TS)
├── database/        ← init-roles.sql, validate-migrations.sh (CI doğrulama)
├── scripts/         ← Tourvisor senkron scriptleri (py/js) + staging SQL
├── docs/            ← Çoklu TO mimarisi, fiyata göre arama
└── e2e/             ← Playwright testleri
```

### `shared/src/commonMain/kotlin/com/mgacreative/touros/`

| Paket | İçerik |
|---|---|
| `domain/model` | Saf modeller (Booking, Tour, Hotel, Tenant, UserRole, ota/, forecast/ …) |
| `domain/repository` | Repository arayüzleri (20 adet) |
| `domain/usecase` | Tek sorumluluklu use case'ler (`invoke` operatörü) |
| `domain/engine` | İş kuralı motorları: komisyon, pazaryeri rezervasyon yönlendirme, iptal, döviz, PDF/rapor export, OTA sync/webhook, satış tahmini |
| `domain/adapter` | `OTAProviderAdapter` arayüzü |
| `domain/security` | `PermissionGuard` |
| `data/repository` | `*RepositoryImpl` (Supabase postgrest/RPC) |
| `data/database/entity` | `@Serializable` tablo entity'leri, `TableRegistry` |
| `data/adapter` | Viator, GetYourGuide, HotelBeds, Booking, Expedia (boş iskelet), `InternalOperatorAdapter` (tenant'lar arası) |
| `data/cache` | `SystemCacheManager` (çok kullanılan) |
| `data/util` | `TenantUtils` — `isValidUuid()`, `generateUuid()` |
| `ai/` | `YandexGptService`, AI asistan ViewModel'i, AI arama adaptörü (🔒) |
| `network/` | `SupabaseClientProvider`, `expect object SupabaseConfig` (platform bazlı url/anonKey) |
| `di/` | `AppModule.kt` (network/repository/useCase/viewModel modülleri), `OtaModule`, `PlatformModule`, `KoinInit` |
| `ui/screens` | ~65 ekran |
| `ui/viewmodel` | ~43 ViewModel |
| `ui/components` | `TourOS*` ortak bileşenler (Button, Card, TextField, TopBar, Sidebar, DataTable, StatusBadge, Dialog…) |
| `ui/theme` | `TourOSTheme`, `TourOSColors`, `TourOSTypography`, `TourOSSpacing`, `WindowSize` |
| `ui/localization` | `AppLanguageManager.translate()` + TR/EN/RU/DE/ES/AR sözlükleri |
| `ui/navigation` | `Routes.kt`, `AppNavigation.kt`, `NavigationItems.kt` |
| `utils/` | Tarih, yazdırma, dosya seçici, `LocalAuthStorage`, `LocalCatalogStorage` |

---

## 5. Mimari Kalıplar

- **Clean Architecture + MVVM:** Screen → ViewModel → UseCase → Repository (arayüz, domain) → RepositoryImpl (data) → Supabase.
- **DI:** Yeni repository/usecase/viewmodel `AppModule.kt` içindeki ilgili modüle eklenir. Oturum boyunca veri tutması gereken ViewModel'ler `single` (örn. `AgencyProductPublishingViewModel`), diğerleri ekran yaşam döngüsüne bağlı.
- **Navigasyon:** Yeni ekran = `Routes.kt`'ye `@Serializable` route + `AppNavigation.kt`'ye `composable<Route>` + gerekiyorsa `NavigationItems.kt`.
- **UI:** Her zaman `TourOS*` ortak bileşenlerini ve `TourOSTheme` token'larını kullan; ham Material bileşeni ve sabit renk kullanma.
- **Metinler:** Kullanıcıya görünen her metin `AppLanguageManager.translate(...)` üzerinden. Dil değişiminde yeniden çizim için `key(currentLang.code)` kullan.
- **Bayrak emojisi kullanma:** Wasm/Canvas'ta kutucuk olarak çıkıyor; ISO kod çipi veya vektör kullan.
- **Mock / hardcoded veri yok:** Tüm veri Supabase'den gelir.

---

## 6. Multi-Tenant, Roller, Güvenlik

- Tenant izolasyonu `tenant_id` / `company_id` + RLS ile (`20260819_018_strict_multi_tenant_isolation_rls.sql`, `20260821_003_strict_rls_data_isolation_audit.sql`).
- Roller (`UserRole`): `SYSTEM_ADMIN`, `TOUR_OPERATOR`, `SALES`, `GUIDE`, `DRIVER`, `ACCOUNTING`, `AGENT`, `CUSTOMER`. Kullanıcı birden fazla role sahip olabilir (`20260911_002_user_multi_roles.sql`).
- Yetki kontrolü: `CheckPermissionUseCase`, `PermissionGuard`, `PermissionGuardComposable`, `PermissionMatrixScreen`.
- Acente abonelik, kota (günlük/aylık sorgu limiti), personel limiti, rate limiting / bot kalkanı migration'larla tanımlı.

---

## 7. Veri Akışı — Pazaryeri Kataloğu (kritik)

- Ana tablo: **`marketplace_products`** (paket tur + uçuş kayıtları; `product_type`, `operator_name`, `price`, `currency`, `departure_city`, `departure_date`, `nights`…).
- Kaynak: **Tourvisor** (`scripts/tourvisor_*`) → `marketplace_products_staging` → `atomic_swap_marketplace_products()` ile sıfır kesintili tablo değişimi.
- **Sadece 9 kanonik operatör** aktif (`canonical_operators`): Bibloglobus, Anex, Coral Travel, Sunmar, Fun&Sun (Ru), Kazunion, Loti, Pegas Touristik, Интурист. İsim eşleme `normalize_operator_name()` ile; listede olmayan uçuş kayıtları `Charter` olarak işaretlenir.
- PostgREST varsayılan 1.000 satır limiti var → büyük sorgularda `select { range(0, N) }` kullan.
- Upsert için ID'ler deterministik olmalı (rastgele UUID / indeks değil); ayrıntı `AGENT_HANDOFF.md`.
- Toplu yazmalar arka planda (`Dispatchers.IO`), 250'lik paketlerle; UI bloklanmaz.

---

## 8. Komutlar

```bash
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:run            # hot reload: :desktopApp:hotRun --auto
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :shared:jvmTest            # diğer: testAndroidHostTest, wasmJsTest, iosSimulatorArm64Test
./deploy-yandex.ps1                  # Wasm prod build → axileto.com
./database/validate-migrations.sh    # migration doğrulama (Docker postgres:16)
```

---

## 9. Referans Dokümanlar

- `RULES.md` — kesin kurallar (bu dosyanın 2. bölümü)
- `AGENT_HANDOFF.md` — katalog yayınlama / veri kalıcılığı kök neden raporu
- `LANGUAGE_I18N_HANDOFF.md` — çoklu dil mimarisi ve açık işler
- `docs/COKLU_TO_DATABASE_KURULUMU.md` — çoklu operatör aggregator mimarisi
- `docs/fiyata_gore_arama.md` — fiyata göre arama (🔒 search)
- `database/README.md` — migration ve CI süreci

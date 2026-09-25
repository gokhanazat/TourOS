# TourOS Veritabanı ve Migration Yönetimi (PostgreSQL 16)

Bu dizin, TourOS projesinin **Yandex Cloud Managed PostgreSQL** ve **Supabase** altyapısında çalışan şema, migration ve veritabanı doğrulama araçlarını barındırır.

---

## 1. Dizin ve Dosya Yapısı

* **`database/init-roles.sql`**: Test ve CI/CD konteynerleri için gerekli PostgreSQL eklentilerini (`uuid-ossp`, `pgcrypto`), `auth` şemasını ve PostgREST rollerini (`anon`, `authenticated`, `service_role`, `authenticator`) başlatan bootstrap scripti.
* **`database/validate-migrations.sh`**: Tüm migration dosyalarını sırayla koşturan, geçersiz indeksleri ve şema bütünlüğünü (`pg_dump`) denetleyen otomatik doğrulama betiği.
* **`database/.sqlfluff`**: PostgreSQL DDL/DML sözdizimi ve stil kurallarını belirleyen SQLFluff konfigürasyonu.
* **`supabase/migrations/`**: Projenin kanonik, zaman damgalı (`YYYYMMDD_NNN_*.sql`) tüm şema ve RPC migration dosyaları.

---

## 2. CI/CD Entegrasyonu (GitHub Actions)

Her Pull Request ve `main`/`master` kod gönderiminde GitHub Actions üzerinde bağımsız bir `postgres:16-alpine` servis konteyneri ayağa kaldırılır ve şu adımlar işletilir:

1. **Bağlantı & Bootstrap:** `init-roles.sql` ile roller ve uzantılar yüklenir.
2. **Sıralı Koşum:** `supabase/migrations/` altındaki tüm SQL dosyaları `ON_ERROR_STOP=1` parametresiyle sırayla çalıştırılır. Herhangi bir sözdizimi, tip uyuşmazlığı veya eksik foreign key hatasında derleme derhal durdurulur.
3. **Şema Denetimi (Integrity Audit):**
   * Geçersiz (invalid) indeks kontrolü (`pg_index.indisvalid = false`)
   * Temel tabloların (`companies`, `roles`, `bookings`, `tours`, `hotels`, `marketplace_products`, `canonical_operators`) mevcudiyet kontrolü
   * `pg_dump --schema-only` ile genel katalog bütünlüğü testi
4. **SQLFluff Linting:** Değişen SQL dosyaları üzerinde sözdizimi ve stil taraması yapılır.

---

## 3. Yerel Test Çalıştırma

Yerel ortamda Docker ile test etmek için:

```bash
# 1. Test PostgreSQL konteynerini başlat
docker run --name touros-db-test -e POSTGRES_PASSWORD=postgrespassword -e POSTGRES_DB=touros_test -p 5432:5432 -d postgres:16-alpine

# 2. Doğrulama scriptini koştur
./database/validate-migrations.sh

# 3. Konteyneri temizle
docker rm -f touros-db-test
```

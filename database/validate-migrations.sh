#!/usr/bin/env bash
# =========================================================================
# TourOS Database & Migration Doğrulama Scripti (PostgreSQL 16)
# Yandex Managed PostgreSQL & CI/CD Entegrasyonu
# =========================================================================

set -euo pipefail

# 1. Konfigürasyon ve Ortam Değişkenleri
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-postgres}"
PGPASSWORD="${PGPASSWORD:-postgrespassword}"
PGDATABASE="${PGDATABASE:-touros_test}"

export PGPASSWORD

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Migrations dizini tespiti (supabase/migrations veya database/migrations)
if [ -d "$PROJECT_ROOT/supabase/migrations" ]; then
    MIGRATIONS_DIR="$PROJECT_ROOT/supabase/migrations"
elif [ -d "$PROJECT_ROOT/database/migrations" ]; then
    MIGRATIONS_DIR="$PROJECT_ROOT/database/migrations"
else
    echo "❌ HATA: Migration dizini bulunamadı!"
    exit 1
fi

echo "========================================================================="
echo "🚀 TourOS Database & Migration Doğrulayıcı Başlatılıyor"
echo "📂 Migration Kaynağı : $MIGRATIONS_DIR"
echo "🗄️  Hedef Veritabanı : $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"
echo "========================================================================="

# 2. PostgreSQL Servis Bağlantı Kontrolü
echo "⏳ PostgreSQL bağlantısı test ediliyor..."
MAX_RETRIES=30
RETRY_COUNT=0

until pg_isready -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" > /dev/null 2>&1; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ "$RETRY_COUNT" -ge "$MAX_RETRIES" ]; then
        echo "❌ HATA: PostgreSQL servisine $MAX_RETRIES denemede bağlanılamadı!"
        exit 1
    fi
    echo "   Bekleniyor ($RETRY_COUNT/$MAX_RETRIES)..."
    sleep 1
done

echo "✅ PostgreSQL servisi hazır ve erişilebilir."

# 3. Rollerin ve Çekirdek Eklentilerin Başlatılması (init-roles.sql)
INIT_ROLES_FILE="$SCRIPT_DIR/init-roles.sql"
if [ -f "$INIT_ROLES_FILE" ]; then
    echo "🛠️  Sistem rolleri ve eklentileri yükleniyor: $(basename "$INIT_ROLES_FILE")"
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -v ON_ERROR_STOP=1 -f "$INIT_ROLES_FILE" > /dev/null
    echo "✅ Sistem rolleri ve eklentileri başarıyla yüklendi."
fi

# 4. Migration Dosyalarının Sırayla Koşturulması
echo "========================================================================="
echo "🔄 SQL Migration Dosyaları Uygulanıyor..."
echo "========================================================================="

TOTAL_FILES=0
FAILED_FILES=0
START_TIME=$(date +%s)

# Sadece standart zaman damgalı [0-9]*.sql migration dosyalarını sırayla çalıştır
for file in $(ls "$MIGRATIONS_DIR"/[0-9]*.sql 2>/dev/null | sort); do
    FILENAME=$(basename "$file")
    TOTAL_FILES=$((TOTAL_FILES + 1))
    
    # Çok büyük seed dosyalarını loglarda sade göster
    FILESIZE=$(wc -c < "$file")
    
    echo -n "   [#$TOTAL_FILES] $FILENAME (${FILESIZE}b)... "
    
    FILE_START=$(date +%s%N 2>/dev/null || date +%s)
    if psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -v ON_ERROR_STOP=1 -f "$file" > /tmp/psql_out.log 2>&1; then
        echo "OK"
    else
        echo "❌ HATA!"
        echo "-------------------------------------------------------------------------"
        cat /tmp/psql_out.log
        echo "-------------------------------------------------------------------------"
        echo "❌ HATA: '$FILENAME' migration dosyası çalıştırılırken hata oluştu!"
        FAILED_FILES=$((FAILED_FILES + 1))
        exit 1
    fi
done

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo "========================================================================="
echo "📊 Migration Koşum Özeti:"
echo "   Toplam Uygulanan Dosya : $TOTAL_FILES"
echo "   Hatalı Dosya           : $FAILED_FILES"
echo "   Toplam Süre            : ${DURATION}s"
echo "========================================================================="

# 5. Şema Bütünlüğü ve İndeks Doğrulama Denetimleri (Integrity Audit)
echo "🔍 Şema Bütünlüğü ve İndeks Denetimleri Yapılıyor..."

# 5.a) Bozuk / Geçersiz İndeks Kontrolü
INVALID_INDEX_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -t -A -c \
    "SELECT count(*) FROM pg_index WHERE NOT indisvalid;")

if [ "$INVALID_INDEX_COUNT" -gt 0 ]; then
    echo "❌ HATA: Veritabanında $INVALID_INDEX_COUNT adet geçersiz (invalid) indeks tespit edildi!"
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -c \
        "SELECT indexrelid::regclass AS invalid_index FROM pg_index WHERE NOT indisvalid;"
    exit 1
else
    echo "   ✅ Tüm indeksler geçerli (0 invalid index)."
fi

# 5.b) Kritik Tabloların Varlık Kontrolü
CRITICAL_TABLES=("companies" "roles" "bookings" "tours" "hotels" "marketplace_products" "canonical_operators")
for tbl in "${CRITICAL_TABLES[@]}"; do
    EXISTS=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -t -A -c \
        "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '$tbl';")
    if [ "$EXISTS" -ne 1 ]; then
        echo "❌ HATA: Kritik tablo 'public.$tbl' eksik!"
        exit 1
    fi
done
echo "   ✅ Tüm kritik tablolar (${#CRITICAL_TABLES[@]} tablo) doğrulandı."

# 5.c) pg_dump Şema Çıkarım Testi (Katalog Bütünlüğü)
echo "   📦 pg_dump şema çıkarımı doğrulanıyor..."
pg_dump -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" --schema-only > /tmp/touros_schema_dump.sql
if [ -s /tmp/touros_schema_dump.sql ]; then
    DUMP_LINES=$(wc -l < /tmp/touros_schema_dump.sql)
    echo "   ✅ pg_dump şema çıkarımı başarılı ($DUMP_LINES satır DDL)."
else
    echo "❌ HATA: pg_dump boş şema üretti!"
    exit 1
fi

echo "========================================================================="
echo "🎉 TEBRİKLER: Tüm migration dosyaları ve veritabanı şeması %100 doğrulandı!"
echo "========================================================================="
exit 0

#!/usr/bin/env bash
# =========================================================================
# TourOS Database & Migration Doğrulama Scripti (PostgreSQL 16)
# Yandex Managed PostgreSQL & CI/CD Entegrasyonu
# Sadece yeni eklenen veya değişen migration dosyalarını doğrular.
# =========================================================================

set -euo pipefail

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

# 1. PR / Branch Kapsamındaki Yeni veya Değişen Migration'ların Tespiti
VALIDATE_ALL="${VALIDATE_ALL:-false}"
TARGET_FILES=()

if [ "$VALIDATE_ALL" = "true" ]; then
    echo "📋 VALIDATE_ALL=true aktif: Tüm migration dosyaları doğrulanacak."
    for file in $(ls "$MIGRATIONS_DIR"/[0-9]*.sql 2>/dev/null | sort); do
        TARGET_FILES+=("$file")
    done
else
    # Base branch (origin/master, master, origin/main, main) tespiti
    BASE_BRANCH=""
    if git rev-parse --verify origin/master >/dev/null 2>&1; then
        BASE_BRANCH="origin/master"
    elif git rev-parse --verify master >/dev/null 2>&1; then
        BASE_BRANCH="master"
    elif git rev-parse --verify origin/main >/dev/null 2>&1; then
        BASE_BRANCH="origin/main"
    elif git rev-parse --verify main >/dev/null 2>&1; then
        BASE_BRANCH="main"
    fi

    if [ -n "$BASE_BRANCH" ]; then
        echo "🔍 Base branch ($BASE_BRANCH) ile HEAD arasındaki migration farkları taranıyor..."
        while IFS= read -r rel_file; do
            [ -z "$rel_file" ] && continue
            if [ -f "$PROJECT_ROOT/$rel_file" ]; then
                TARGET_FILES+=("$PROJECT_ROOT/$rel_file")
            fi
        done < <(git -C "$PROJECT_ROOT" diff --name-only --diff-filter=d "${BASE_BRANCH}...HEAD" -- "supabase/migrations/*.sql" "database/migrations/*.sql" 2>/dev/null || true)
    fi

    # Eğer bu PR/branch'te yeni eklenen veya değişen migration yoksa başarıyla atla (skip)
    if [ "${#TARGET_FILES[@]}" -eq 0 ]; then
        echo "========================================================================="
        echo "ℹ️  Bu PR / Branch üzerinde yeni veya değişen migration dosyası bulunamadı."
        echo "⏩ Migration doğrulama adımı başarıyla atlandı (SKIP)."
        echo "========================================================================="
        exit 0
    fi
fi

# 2. Konfigürasyon ve Ortam Değişkenleri
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-postgres}"
PGPASSWORD="${PGPASSWORD:-postgrespassword}"
PGDATABASE="${PGDATABASE:-touros_test}"

export PGPASSWORD

echo "========================================================================="
echo "🚀 TourOS Database & Migration Doğrulayıcı Başlatılıyor"
echo "📂 Hedef Migration Sayısı : ${#TARGET_FILES[@]}"
echo "🗄️  Hedef Veritabanı       : $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"
echo "========================================================================="

# 3. PostgreSQL Servis Bağlantı Kontrolü
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

# 4. Resmi Supabase Auth DDL ve Rollerin Yüklenmesi (Zero Mock)
OFFICIAL_AUTH_FILE="$SCRIPT_DIR/official-supabase-auth.sql"
if [ -f "$OFFICIAL_AUTH_FILE" ]; then
    echo "🛠️  Resmi Supabase Auth DDL ve Sistem Rolleri yükleniyor: $(basename "$OFFICIAL_AUTH_FILE")"
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -v ON_ERROR_STOP=1 -f "$OFFICIAL_AUTH_FILE" > /dev/null
    echo "✅ Resmi Supabase Auth şeması ve fonksiyonları (auth.users, auth.uid, auth.jwt) başarıyla yüklendi."
fi

# 5. Hedef Migration Dosyalarının Sırayla Koşturulması
echo "========================================================================="
echo "🔄 SQL Migration Dosyaları Uygulanıyor..."
echo "========================================================================="

TOTAL_FILES=0
FAILED_FILES=0
START_TIME=$(date +%s)

for file in "${TARGET_FILES[@]}"; do
    FILENAME=$(basename "$file")
    TOTAL_FILES=$((TOTAL_FILES + 1))
    
    FILESIZE=$(wc -c < "$file")
    echo -n "   [#$TOTAL_FILES] $FILENAME (${FILESIZE}b)... "
    
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

# 6. Şema Bütünlüğü ve İndeks Doğrulama Denetimleri (Integrity Audit)
echo "🔍 Şema Bütünlüğü ve İndeks Denetimleri Yapılıyor..."

# 6.a) Bozuk / Geçersiz İndeks Kontrolü
INVALID_INDEX_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -t -A -c \
    "SELECT count(*) FROM pg_index WHERE NOT indisvalid;" 2>/dev/null || echo 0)

if [ "$INVALID_INDEX_COUNT" -gt 0 ]; then
    echo "❌ HATA: Veritabanında $INVALID_INDEX_COUNT adet geçersiz (invalid) indeks tespit edildi!"
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -c \
        "SELECT indexrelid::regclass AS invalid_index FROM pg_index WHERE NOT indisvalid;"
    exit 1
else
    echo "   ✅ Tüm indeksler geçerli (0 invalid index)."
fi

# 6.b) Kritik Tabloların Varlık Kontrolü (Sadece tam doğrulama modunda)
if [ "$VALIDATE_ALL" = "true" ]; then
    CRITICAL_TABLES=("companies" "roles" "bookings" "tours" "hotels" "marketplace_products" "canonical_operators")
    for tbl in "${CRITICAL_TABLES[@]}"; do
        EXISTS=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -t -A -c \
            "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '$tbl';" 2>/dev/null || echo 0)
        if [ "$EXISTS" -ne 1 ]; then
            echo "❌ HATA: Kritik tablo 'public.$tbl' eksik!"
            exit 1
        fi
    done
    echo "   ✅ Tüm kritik tablolar (${#CRITICAL_TABLES[@]} tablo) doğrulandı."
fi

# 6.c) pg_dump Şema Çıkarım Testi (Katalog Bütünlüğü)
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
echo "🎉 TEBRİKLER: Migration doğrulaması başarıyla tamamlandı!"
echo "========================================================================="
exit 0

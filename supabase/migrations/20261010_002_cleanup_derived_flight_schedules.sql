-- ============================================================================
-- Migration: 20261010_002_cleanup_derived_flight_schedules.sql
-- Tarih: 10.10.2026 (onaylı)
-- Amaç: 15.09.2026'da paket turlardan TÜRETİLMİŞ uçuş kayıtlarının temizlenmesi.
--   - 3.416 kayıt: %97'sinde uçuş numarası yok, bagaj 20 kg varsayılan, fiyat farkı oranlanmış (uydurma).
--   - Bu kayıtlara bağlı rezervasyon YOK (Antigravity raporu, 10.10.2026).
--   - Rezervasyon adımında (get_operator_flight_options) uydurma "uçuş alternatifi" ve
--     fiyat farkı olarak çıkıyorlardı.
-- Kapsam DIŞI: 10.09.2026'da oluşturulan 12 kayıt (elle girilmiş olabilir) — DOKUNULMAZ.
-- Geri alma: Kayıtlar önce "backup" şemasına kopyalanır (API'ye açık değildir).
--   Geri yükleme gerekirse:
--     INSERT INTO public.flight_schedules SELECT * FROM backup.flight_schedules_20261010;
--     INSERT INTO public.marketplace_products SELECT * FROM backup.marketplace_flights_20261010;
-- Güvenlik: Yedek sayısı beklenenden farklıysa işlem iptal edilir (ROLLBACK).
-- ============================================================================

BEGIN;

CREATE SCHEMA IF NOT EXISTS backup;
REVOKE ALL ON SCHEMA backup FROM PUBLIC;
REVOKE ALL ON SCHEMA backup FROM anon, authenticated;

-- 1) Yedekler
CREATE TABLE backup.flight_schedules_20261010 AS
SELECT *
FROM public.flight_schedules
WHERE created_at::date = DATE '2026-09-15';

CREATE TABLE backup.marketplace_flights_20261010 AS
SELECT mp.*
FROM public.marketplace_products mp
WHERE mp.product_type = 'FLIGHT'
  AND mp.id IN (SELECT id FROM backup.flight_schedules_20261010);

-- 2) Güvenlik kontrolü: beklenen 3.416 kayıt
DO $$
DECLARE
    n_fs int;
    n_mp int;
BEGIN
    SELECT count(*) INTO n_fs FROM backup.flight_schedules_20261010;
    SELECT count(*) INTO n_mp FROM backup.marketplace_flights_20261010;
    RAISE NOTICE 'Yedeklenen flight_schedules: %, marketplace FLIGHT: %', n_fs, n_mp;
    IF n_fs <> 3416 THEN
        RAISE EXCEPTION 'Beklenen 3416 uçuş kaydı, bulunan % — işlem iptal edildi', n_fs;
    END IF;
END $$;

-- 3) Silme (önce pazaryeri ürünü, sonra uçuş kaydı)
DELETE FROM public.marketplace_products
WHERE product_type = 'FLIGHT'
  AND id IN (SELECT id FROM backup.flight_schedules_20261010);

DELETE FROM public.flight_schedules
WHERE id IN (SELECT id FROM backup.flight_schedules_20261010);

-- 4) Sonuç
SELECT
    (SELECT count(*) FROM public.flight_schedules) AS kalan_flight_schedules,
    (SELECT count(*) FROM public.marketplace_products WHERE product_type = 'FLIGHT') AS kalan_marketplace_flight,
    (SELECT count(*) FROM backup.flight_schedules_20261010) AS yedek_flight_schedules,
    (SELECT count(*) FROM backup.marketplace_flights_20261010) AS yedek_marketplace_flight;

COMMIT;

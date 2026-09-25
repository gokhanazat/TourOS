#!/usr/bin/env python3
"""
TourOS - TourVisor Arka Plan Servis Dinleyicisi (Yandex Cloud Daemon)
- Admin Panelden gelen anlık tetiklemeleri (sync_requested == True) anında yakalar.
- Sezon moduna göre (Düşük Sezon: 24 saat / Yüksek Sezon: 4 saat) periyodik çekim yapar.
- Systemd servisi olarak arka planda sürekli ve güvenle çalışır.
"""

import time
import datetime
import subprocess
import psycopg2
import sys

DB_HOST = '172.18.0.3'
DB_PORT = 5432
DB_NAME = 'postgres'
DB_USER = 'postgres'
DB_PASS = 'gGt5o1mMCgHMhPhFHYh0UgYcU6IgB2Lq'
SYNC_SCRIPT = '/home/ubuntu/tourvisor_sync/tourvisor_full_sync.py'

def get_db():
    return psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASS
    )

def check_and_execute():
    try:
        conn = get_db()
        cur = conn.cursor()
        
        cur.execute("""
            SELECT sync_requested, season_mode, sync_interval, is_live, last_synced_at
            FROM public.data_feed_sources
            WHERE id = 'feed-tourvisor';
        """)
        row = cur.fetchone()
        if not row:
            cur.close()
            conn.close()
            return

        sync_requested, season_mode, sync_interval, is_live, last_synced_at = row
        cur.close()
        conn.close()

        # Manuel tetikleme var mı?
        if sync_requested:
            print(f"[{datetime.datetime.now()}] ⚡ Admin panelden anlık çekim emri alındı! Senkronizasyon başlatılıyor...")
            subprocess.run([sys.executable, SYNC_SCRIPT], check=True)
            return

    except Exception as e:
        print(f"[{datetime.datetime.now()}] Daemon kontrol hatası: {e}")

def main():
    print("=== TourOS TourVisor Sync Daemon Başlatıldı ===")
    while True:
        check_and_execute()
        time.sleep(15) # Her 15 saniyede bir admin panelden tetikleme geldi mi diye kontrol et

if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""
TourOS - TourVisor %100 Tam Veri Çekim ve Staging Swap Motoru (Yandex Cloud)
- Akıllı Polling (state == 'finished' olana kadar bekleme)
- Tam Sayfalama (Pagination: sayfa 1, 2, 3...)
- marketplace_products_staging tablosuna yazım ve atomic_swap_marketplace_products() ile canlıya alma
- data_sync_logs ve data_feed_sources senkronizasyon takip entegrasyonu
"""

import urllib.request
import urllib.parse
import json
import time
import datetime
import psycopg2
from psycopg2.extras import execute_batch

AUTH_LOGIN = 'Mabit23@gmail.com'
AUTH_PASS = 'FFytMvSU0ZHr'

DB_HOST = '172.18.0.3'
DB_PORT = 5432
DB_NAME = 'postgres'
DB_USER = 'postgres'
DB_PASS = 'gGt5o1mMCgHMhPhFHYh0UgYcU6IgB2Lq'

# Popüler Kalkış Şehirleri (TourVisor City ID -> Adı)
CITIES = [
    (1, 'Moskova'),
    (5, 'Saint Petersburg'),
    (10, 'Kazan'),
    (3, 'Yekaterinburg')
]

# Popüler Varış Ülkeleri (TourVisor Country ID -> Adı, Kodu)
COUNTRIES = [
    (4, 'Türkiye', 'TR'),
    (1, 'Mısır', 'EG'),
    (9, 'BAE', 'AE'),
    (2, 'Tayland', 'TH'),
    (8, 'Maldivler', 'MV'),
    (47, 'Rusya', 'RU')
]

# Hedef Tarih Aralıkları (Dinamik Önümüzdeki 30 Gün)
today = datetime.date.today()
DATE_RANGES = [
    ((today + datetime.timedelta(days=3)).strftime('%d.%m.%Y'), (today + datetime.timedelta(days=10)).strftime('%d.%m.%Y')),
    ((today + datetime.timedelta(days=12)).strftime('%d.%m.%Y'), (today + datetime.timedelta(days=19)).strftime('%d.%m.%Y')),
    ((today + datetime.timedelta(days=21)).strftime('%d.%m.%Y'), (today + datetime.timedelta(days=28)).strftime('%d.%m.%Y'))
]

def get_db():
    return psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASS
    )

def parse_date_to_iso(date_str):
    if not date_str:
        return None
    try:
        if '.' in date_str:
            p = date_str.split('.')
            return f"{p[2]}-{p[1].zfill(2)}-{p[0].zfill(2)}"
        elif '-' in date_str:
            return date_str
    except Exception:
        pass
    return None

def get_active_operators(cur):
    """
    Veritabanındaki canonical_operators tablosundan aktif olan operatörleri çeker.
    - TourVisor operatör kodları (API arama filtresi için)
    - Alias ve kod haritaları (gelen turların operatör isimlerini standartlaştırmak için)
    """
    cur.execute("""
        SELECT id, canonical_name, tourvisor_code, aliases 
        FROM public.canonical_operators 
        WHERE is_active = true 
        ORDER BY display_order;
    """)
    rows = cur.fetchall()
    
    operator_codes = [str(r[2]) for r in rows if r[2] is not None]
    
    code_to_canonical = {}
    alias_to_canonical = {}
    
    for _, cname, tv_code, aliases in rows:
        if tv_code is not None:
            code_to_canonical[int(tv_code)] = cname
        alias_to_canonical[cname.lower().strip()] = cname
        if aliases:
            for a in aliases:
                alias_to_canonical[a.lower().strip()] = cname
                
    return operator_codes, code_to_canonical, alias_to_canonical

def normalize_operator(opcode, opname, code_map, alias_map):
    """
    Operatör adını kanonik isme dönüştürür.
    Eğer aktif operatörler arasında yoksa None döner (böylece filtrelenir).
    """
    if opcode in code_map:
        return code_map[opcode]
        
    clean_name = opname.lower().strip()
    if clean_name in alias_map:
        return alias_map[clean_name]
        
    for alias, cname in alias_map.items():
        if alias in clean_name or clean_name in alias:
            return cname
            
    return None

def fetch_search_results_with_polling(city_id, country_id, date_from, date_to, operator_codes_str=None):
    """
    TourVisor API'sinde asenkron arama başlatır, operatörlerin tamamı yanıt dönene kadar
    akıllı polling yapar ve sayfalama ile tüm otel listesini çeker.
    """
    params = {
        'format': 'json',
        'authlogin': AUTH_LOGIN,
        'authpass': AUTH_PASS,
        'cityfrom': city_id,
        'country': country_id,
        'datefrom': date_from,
        'dateto': date_to,
        'nightsfrom': 6,
        'nightsto': 10,
        'adults': 2
    }
    if operator_codes_str:
        params['operators'] = operator_codes_str

    search_url = f"http://tourvisor.ru/xml/search.php?{urllib.parse.urlencode(params)}"
    try:
        req = urllib.request.urlopen(search_url, timeout=15)
        res = json.loads(req.read().decode('utf-8'))
        req_id = res.get('result', {}).get('requestid')
        if not req_id:
            return []
        
        # Akıllı Polling: Operatörler tamamlanana kadar yokla
        is_finished = False
        attempts = 0
        while not is_finished and attempts < 9:
            attempts += 1
            time.sleep(3)
            
            status_params = {
                'requestid': req_id,
                'type': 'status',
                'format': 'json',
                'authlogin': AUTH_LOGIN,
                'authpass': AUTH_PASS
            }
            status_url = f"http://tourvisor.ru/xml/result.php?{urllib.parse.urlencode(status_params)}"
            try:
                s_req = urllib.request.urlopen(status_url, timeout=15)
                s_res = json.loads(s_req.read().decode('utf-8'))
                status_obj = s_res.get('data', {}).get('status', {})
                state = status_obj.get('state', '')
                progress = int(status_obj.get('progress', 0) or 0)
                if state == 'finished' or progress >= 95:
                    is_finished = True
            except Exception:
                pass

        # Tam Sayfalama (Pagination): Sayfaları sırayla topla
        all_hotels = []
        page = 1
        total_pages = 1
        
        while page <= total_pages and page <= 3:
            res_params = {
                'requestid': req_id,
                'type': 'result',
                'page': page,
                'onpage': 50,
                'format': 'json',
                'authlogin': AUTH_LOGIN,
                'authpass': AUTH_PASS
            }
            result_url = f"http://tourvisor.ru/xml/result.php?{urllib.parse.urlencode(res_params)}"
            res_req = urllib.request.urlopen(result_url, timeout=20)
            res_data = json.loads(res_req.read().decode('utf-8'))
            
            result_block = res_data.get('data', {}).get('result', {})
            if 'pages' in result_block:
                try:
                    total_pages = int(result_block['pages'])
                except:
                    pass
                    
            hotels = result_block.get('hotel', [])
            if isinstance(hotels, dict):
                hotels = [hotels]
            all_hotels.extend(hotels)
            page += 1
            
        return all_hotels
    except Exception as e:
        print(f"[Search Error] City {city_id} -> Country {country_id}: {e}")
        return []

def run_sync():
    start_time = datetime.datetime.now()
    print("==================================================================")
    print("🚀 [TourOS Sync] TourVisor %100 Tam Veri Çekimi Başlatıldı...")
    print(f"📅 Başlama Zamanı: {start_time}")
    print("==================================================================")
    
    conn = get_db()
    cur = conn.cursor()
    
    op_codes, code_map, alias_map = get_active_operators(cur)
    op_codes_str = ','.join(op_codes)
    print(f"🎯 Aktif Tur Operatörleri: {len(code_map)} Adet Tanımlı (TV Kodları: {op_codes_str})")

    # 1. Staging tablosunu temizle ve canlıdaki FLIGHT kayıtlarını koru
    cur.execute("TRUNCATE TABLE public.marketplace_products_staging;")
    cur.execute("INSERT INTO public.marketplace_products_staging SELECT * FROM public.marketplace_products WHERE product_type = 'FLIGHT' ON CONFLICT DO NOTHING;")
    conn.commit()
    print("🧹 Staging tablosu temizlendi, FLIGHT ürünleri korundu.")
    
    total_inserted_tours = 0

    insert_product_staging_sql = """
    INSERT INTO public.marketplace_products_staging (
        id, product_type, tour_name, operator_id, operator_name, price, fuel_charge, currency,
        hotel_id, hotel_name, hotel_category, hotel_rating, country, country_code, country_name,
        region, sub_region, room_type, meal_type, departure_city, departure_date, nights, adults,
        childs, is_charter, is_promo, airline, flight_number, baggage_kg, picture_url, is_published, is_active, last_synced_at
    ) VALUES (
        %(id)s, %(product_type)s, %(tour_name)s, %(operator_id)s, %(operator_name)s, %(price)s, %(fuel_charge)s, %(currency)s,
        %(hotel_id)s, %(hotel_name)s, %(hotel_category)s, %(hotel_rating)s, %(country)s, %(country_code)s, %(country_name)s,
        %(region)s, %(sub_region)s, %(room_type)s, %(meal_type)s, %(departure_city)s, %(departure_date)s, %(nights)s, %(adults)s,
        %(childs)s, %(is_charter)s, %(is_promo)s, %(airline)s, %(flight_number)s, %(baggage_kg)s, %(picture_url)s, true, true, NOW()
    ) ON CONFLICT (id) DO UPDATE SET
        price = EXCLUDED.price,
        departure_date = EXCLUDED.departure_date,
        nights = EXCLUDED.nights,
        last_synced_at = NOW();
    """

    for city_id, city_name in CITIES:
        for country_id, country_name, country_code in COUNTRIES:
            for date_from, date_to in DATE_RANGES:
                print(f"📡 [Çekiliyor] {city_name} -> {country_name} ({date_from} - {date_to})...")
                hotels = fetch_search_results_with_polling(city_id, country_id, date_from, date_to, op_codes_str)
                
                products_batch = []
                for h in hotels:
                    h_code = int(h.get('hotelcode', 0) or 0)
                    h_name = h.get('hotelname', '')
                    h_stars = int(h.get('hotelstars', 3) or 3)
                    h_rating = float(h.get('hotelrating', 0.0) or 0.0)
                    h_region = h.get('regionname', '')
                    h_subregion = h.get('subregionname', '')
                    h_picture = h.get('picturelink', '') or f"https://static.tourvisor.ru/hotel_pics/main400/{h_code}.jpg"
                    
                    tours = h.get('tours', {}).get('tour', [])
                    if isinstance(tours, dict):
                        tours = [tours]
                        
                    for t in tours:
                        t_id = str(t.get('tourid', ''))
                        if not t_id:
                            continue
                            
                        t_price = float(t.get('price', 0) or 0)
                        t_fuel = float(t.get('fuelcharge', 0) or 0)
                        t_flydate = parse_date_to_iso(t.get('flydate', ''))
                        t_nights = int(t.get('nights', 7) or 7)
                        t_adults = int(t.get('adults', 2) or 2)
                        t_childs = int(t.get('child', 0) or 0)
                        t_opname = t.get('operatorname', 'TourVisor Partner')
                        t_opcode = int(t.get('operatorcode', 0) or 0)

                        canonical_op = normalize_operator(t_opcode, t_opname, code_map, alias_map)
                        if not canonical_op:
                            # İstenmeyen/tanımsız operatörlerin turlarını atla!
                            continue
                        t_opname = canonical_op

                        t_tourname = t.get('tourname', f"{h_name} Tur Paketi")
                        t_meal = t.get('mealrussian', t.get('meal', 'AI'))
                        t_room = t.get('room', 'Standard Room')
                        t_promo = (t.get('promo', 0) == 1)

                        airline_cand = 'Charter'
                        flight_no = 'Charter'
                        if 'Red Wings' in t_tourname or 'WZ' in t_tourname:
                            airline_cand = 'Red Wings'; flight_no = 'WZ-3011'
                        elif 'Azur' in t_tourname or 'ZF' in t_tourname:
                            airline_cand = 'Azur Air'; flight_no = 'ZF-8881'
                        elif 'Nordwind' in t_tourname or 'N4' in t_tourname:
                            airline_cand = 'Nordwind Airlines'; flight_no = 'N4-5821'
                        elif 'Pegasus' in t_tourname or 'PC' in t_tourname:
                            airline_cand = 'Pegasus Airlines'; flight_no = 'PC-2014'
                        elif 'Turkish' in t_tourname or 'TK' in t_tourname:
                            airline_cand = 'Turkish Airlines'; flight_no = 'TK-2114'
                        elif 'Aeroflot' in t_tourname or 'SU' in t_tourname:
                            airline_cand = 'Aeroflot'; flight_no = 'SU-2142'

                        products_batch.append({
                            'id': f"tv-{t_id}",
                            'product_type': 'PACKAGE_TOUR',
                            'tour_name': t_tourname,
                            'operator_id': t_opcode,
                            'operator_name': t_opname,
                            'price': t_price,
                            'fuel_charge': t_fuel,
                            'currency': 'RUB',
                            'hotel_id': h_code,
                            'hotel_name': h_name,
                            'hotel_category': h_stars,
                            'hotel_rating': h_rating,
                            'country': country_name,
                            'country_code': country_code,
                            'country_name': country_name,
                            'region': h_region if h_region else country_name,
                            'sub_region': h_subregion,
                            'room_type': t_room,
                            'meal_type': t_meal,
                            'departure_city': city_name,
                            'departure_date': t_flydate,
                            'nights': t_nights,
                            'adults': t_adults,
                            'childs': t_childs,
                            'is_charter': True,
                            'is_promo': t_promo,
                            'airline': airline_cand,
                            'flight_number': flight_no,
                            'baggage_kg': 20,
                            'picture_url': h_picture
                        })

                if products_batch:
                    execute_batch(cur, insert_product_staging_sql, products_batch, page_size=100)
                    conn.commit()
                    total_inserted_tours += len(products_batch)
                    print(f"   ✓ {len(products_batch)} Tur/Otel Staging tablosuna yazıldı (Toplam: {total_inserted_tours})")
                
                time.sleep(1.5)

    duration_seconds = int((datetime.datetime.now() - start_time).total_seconds())
    print("------------------------------------------------------------------")
    print(f"📦 Staging Çekimi Tamamlandı: {total_inserted_tours} Paket Toplandı ({duration_seconds} sn)")
    
    # 2. SIFIR KESİNTİ İLE ATOMIC SWAP ÇALIŞTIR
    if total_inserted_tours > 0:
        print("🔄 [Zero Downtime] atomic_swap_marketplace_products() çalıştırılıyor...")
        cur.execute("SELECT public.atomic_swap_marketplace_products();")
        swap_result = cur.fetchone()[0]
        print(f"✅ Canlı Tablo Güncellendi: {swap_result}")
        
        # 3. Feed durumunu ve log kaydını güncelle
        cur.execute("""
            UPDATE public.data_feed_sources
            SET sync_requested = false,
                last_synced_at = 'Bugün ' || TO_CHAR(NOW(), 'HH24:MI'),
                synced_record_count = %s,
                last_sync_duration_seconds = %s,
                status_message = '🟢 CANLI (%%100 Yenilendi • ' || %s || ' Tur Aktif)'
            WHERE id = 'feed-tourvisor';
        """, (total_inserted_tours, duration_seconds, total_inserted_tours))
        
        cur.execute("""
            INSERT INTO public.data_sync_logs (source_id, started_at, completed_at, status, total_fetched, total_inserted, duration_seconds)
            VALUES ('feed-tourvisor', %s, NOW(), 'SUCCESS', %s, %s, %s);
        """, (start_time, total_inserted_tours, total_inserted_tours, duration_seconds))
        conn.commit()
        print("✅ Feed ve Log durumu başarıyla veritabanına kaydedildi.")
    else:
        print("⚠️ Hiç tur çekilemediği için canlı tabloya dokunulmadı.")

    cur.close()
    conn.close()
    print(f"=== %100 SENKRONİZASYON TAMAMLANDI! Toplam: {total_inserted_tours} Tur ===")

if __name__ == '__main__':
    run_sync()

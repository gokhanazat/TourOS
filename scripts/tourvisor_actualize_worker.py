#!/usr/bin/env python3
"""
TourOS - Tourvisor Tek Tur Sorgu Servisi (Yandex Cloud VM, systemd)

Ne yapar:
  - PostgreSQL 'tour_actualize' kanalını dinler (LISTEN).
  - Uygulama public.request_tour_actualization(tour_id) RPC'sini çağırınca kanala tur numarası düşer.
  - Servis o tur için Tourvisor'a iki sorgu atar:
        1) actualize.php  -> güncel fiyat, vize ücreti, operatör fiyatı/linki, yer durumları
        2) actdetail.php  -> gerçek gidiş-dönüş uçuşları, zorunlu ek ödemeler, tur içeriği, dahil olmayanlar
  - Sonucu public.tour_actualizations tablosuna yazar (status = ok / error / quota_exceeded).
  - Günlük sorgu sayısını public.tourvisor_api_usage tablosunda tutar, TV_DAILY_CAP aşılırsa sorgu atmaz.

Şifreler koda YAZILMAZ. Ortam değişkenlerinden okunur (systemd EnvironmentFile):
  TV_AUTH_LOGIN, TV_AUTH_PASS, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS
İsteğe bağlı:
  TV_DAILY_CAP (varsayılan 1500)  -> günlük tek tur sorgu üst sınırı (Tourvisor tarifesi: 3000 arama/gün)
  TV_ACTUALIZE_REQUEST (varsayılan 0) -> actualize.php 'request' parametresi (0 = gerekirse operatöre sor)
"""

import datetime
import json
import os
import re
import select
import sys
import time
import urllib.parse
import urllib.request

import psycopg2
import psycopg2.extensions
from psycopg2.extras import Json

CHANNEL = "tour_actualize"
HTTP_TIMEOUT = 30
ACTUALIZE_URL = "http://tourvisor.ru/xml/actualize.php"
DETAIL_URL = "http://tourvisor.ru/xml/actdetail.php"


def env(name, default=None, required=True):
    value = os.environ.get(name, default)
    if required and (value is None or str(value).strip() == ""):
        print(f"[HATA] Ortam değişkeni eksik: {name}", flush=True)
        sys.exit(1)
    return value


TV_LOGIN = env("TV_AUTH_LOGIN")
TV_PASS = env("TV_AUTH_PASS")
DB_CONF = dict(
    host=env("DB_HOST"),
    port=int(env("DB_PORT", "5432")),
    dbname=env("DB_NAME", "postgres"),
    user=env("DB_USER"),
    password=env("DB_PASS"),
)
DAILY_CAP = int(env("TV_DAILY_CAP", "1500", required=False))
ACTUALIZE_REQUEST_MODE = str(env("TV_ACTUALIZE_REQUEST", "0", required=False))


def log(msg):
    print(f"[{datetime.datetime.now():%Y-%m-%d %H:%M:%S}] {msg}", flush=True)


# ─── Güvenli tip dönüştürücüler (Tourvisor sayıları bazen metin gönderir) ────────
def to_float(v):
    if v is None:
        return None
    if isinstance(v, (int, float)):
        return float(v)
    s = str(v).strip().replace(" ", "").replace(",", ".")
    if s == "":
        return None
    try:
        return float(s)
    except ValueError:
        return None


def to_int(v):
    f = to_float(v)
    return int(f) if f is not None else None


def to_str(v):
    if v is None:
        return None
    if isinstance(v, dict):
        for k in ("name", "value", "id", "code"):
            if v.get(k) not in (None, ""):
                return str(v.get(k))
        return None
    s = str(v).strip()
    return s if s else None


def to_bool(v):
    if v is None:
        return None
    if isinstance(v, bool):
        return v
    i = to_int(v)
    if i is not None:
        return i == 1
    return str(v).strip().lower() in ("true", "yes")


def as_list(v):
    if v is None:
        return []
    if isinstance(v, list):
        return v
    if isinstance(v, dict):
        # Tourvisor bazen diziyi {"0": {...}, "1": {...}} veya {"item": [...]} olarak döner
        if all(str(k).isdigit() for k in v.keys()) and v:
            return [v[k] for k in sorted(v.keys(), key=lambda x: int(x))]
        for k in ("item", "flight", "addpayment", "content"):
            if k in v:
                return as_list(v[k])
        return [v]
    return [v]


def find_key(obj, key, depth=0):
    """JSON içinde verilen anahtarı ilk bulduğu yerde döndürür (yanıt şekli belgede kesin değil)."""
    if depth > 6:
        return None
    if isinstance(obj, dict):
        if key in obj:
            return obj[key]
        for v in obj.values():
            r = find_key(v, key, depth + 1)
            if r is not None:
                return r
    elif isinstance(obj, list):
        for v in obj:
            r = find_key(v, key, depth + 1)
            if r is not None:
                return r
    return None


def find_tour_object(obj, depth=0):
    """actualize.php yanıtında tur alanlarını taşıyan sözlüğü bulur."""
    if depth > 6:
        return None
    if isinstance(obj, dict):
        if "hotelcode" in obj or ("price" in obj and "operatorcode" in obj):
            return obj
        for v in obj.values():
            r = find_tour_object(v, depth + 1)
            if r is not None:
                return r
    elif isinstance(obj, list):
        for v in obj:
            r = find_tour_object(v, depth + 1)
            if r is not None:
                return r
    return None


# ─── Tourvisor HTTP ───────────────────────────────────────────────────────────
def http_get_json(base, params):
    q = dict(params)
    q.update({"authlogin": TV_LOGIN, "authpass": TV_PASS, "format": "json"})
    url = f"{base}?{urllib.parse.urlencode(q)}"
    req = urllib.request.Request(url, headers={"User-Agent": "TourOS-Actualize/1.0"})
    with urllib.request.urlopen(req, timeout=HTTP_TIMEOUT) as resp:
        body = resp.read().decode("utf-8", errors="replace")
    try:
        return json.loads(body)
    except json.JSONDecodeError:
        raise RuntimeError(f"Tourvisor JSON olmayan yanıt döndü: {body[:300]}")


def tourvisor_error_text(data):
    """Yanıtta açık bir hata mesajı varsa döndürür."""
    for key in ("error", "errormessage", "errormsg"):
        v = find_key(data, key)
        if v not in (None, "", 0, "0", False):
            return to_str(v) or str(v)
    return None


# ─── Normalizasyon ────────────────────────────────────────────────────────────
def port_text(v):
    """Havalimanı: 'Шереметьево (SVO)'. Tourvisor port nesnesi: {id, name, shortName, timeZone}."""
    if isinstance(v, dict):
        name = to_str(v.get("shortName")) or to_str(v.get("name"))
        code = to_str(v.get("id")) or to_str(v.get("code"))
        if name and code and name != code:
            return f"{name} ({code})"
        return name or code
    return to_str(v)


PLACEHOLDER_NUMBER = re.compile(r"^[A-Z0-9]{2}\s*0+$")


def norm_leg(leg):
    if not isinstance(leg, dict):
        return None
    company = leg.get("company") or {}
    dep = leg.get("departure") or {}
    arr = leg.get("arrival") or {}
    if not isinstance(dep, dict):
        dep = {}
    if not isinstance(arr, dict):
        arr = {}
    number = to_str(leg.get("number"))
    dep_time = to_str(dep.get("time"))
    arr_time = to_str(arr.get("time"))
    # Operatör henüz kesin uçuşu bildirmediğinde Tourvisor "SU000" ve "00:00" gibi yer tutucu gönderir.
    # Bunlar gerçek uçuş değildir; uygulamada "рейс уточняется" gösterilir.
    is_placeholder = bool(number and PLACEHOLDER_NUMBER.match(number.replace(" ", ""))) or (
        dep_time == "00:00" and arr_time == "00:00"
    )
    baggage_included = to_bool(leg.get("isBaggageIncluded"))
    return {
        "airline_code": to_str(company.get("id")) if isinstance(company, dict) else None,
        "airline_name": to_str(company.get("name")) if isinstance(company, dict) else to_str(company),
        "airline_logo": (to_str(company.get("logo")) or to_str(company.get("thumb"))) if isinstance(company, dict) else None,
        "number": None if is_placeholder else number,
        "plane": to_str(leg.get("plane")),
        "flight_class": to_str(leg.get("class")),
        "departure_date": to_str(dep.get("date")),
        "departure_time": None if is_placeholder else dep_time,
        "departure_port": port_text(dep.get("port")),
        "arrival_date": to_str(arr.get("date")),
        "arrival_time": None if is_placeholder else arr_time,
        "arrival_port": port_text(arr.get("port")),
        "is_placeholder": is_placeholder,
        "baggage_included": baggage_included,
        "baggage_kg": to_int(leg.get("maxWeightBaggageKg")),
        "on_demand": bool(to_bool(leg.get("onDemand"))),
        "no_places": bool(to_bool(leg.get("noPlaces"))),
    }


def norm_flights(raw_flights):
    result = []
    for idx, fs in enumerate(as_list(raw_flights)):
        if not isinstance(fs, dict):
            continue
        price = fs.get("price") or {}
        fuel = fs.get("fuelcharge") or {}
        forward = [l for l in (norm_leg(x) for x in as_list(fs.get("forward"))) if l]
        backward = [l for l in (norm_leg(x) for x in as_list(fs.get("backward"))) if l]
        if not forward and not backward:
            continue
        result.append({
            "is_default": bool(to_bool(fs.get("isdefault"))) or (idx == 0 and fs.get("isdefault") is None),
            "date_forward": to_str(fs.get("dateforward")),
            "date_backward": to_str(fs.get("datebackward")),
            "price": to_float(price.get("value")) if isinstance(price, dict) else to_float(price),
            "currency": to_str(price.get("currency")) if isinstance(price, dict) else None,
            "fuel_charge": to_float(fuel.get("value")) if isinstance(fuel, dict) else to_float(fuel),
            "forward": forward,
            "backward": backward,
        })
    return result


def norm_add_payments(raw):
    out = []
    for p in as_list(raw):
        if not isinstance(p, dict):
            continue
        name = to_str(p.get("name"))
        amount = to_float(p.get("amount"))
        if name and amount is not None:
            out.append({"name": name, "amount": amount})
    return out


def norm_contents(raw):
    out = []
    for c in as_list(raw):
        s = to_str(c)
        if s:
            out.append(s)
    return out


def norm_flags(raw):
    flags = {"notransfer": False, "nomedinsurance": False, "noflight": False, "nomeal": False}
    if isinstance(raw, dict):
        for k in flags:
            if k in raw:
                flags[k] = bool(to_bool(raw.get(k)))
    elif isinstance(raw, list):
        for item in raw:
            s = to_str(item)
            if s and s.lower() in flags:
                flags[s.lower()] = True
    return flags


# ─── Veritabanı ───────────────────────────────────────────────────────────────
def connect():
    conn = psycopg2.connect(**DB_CONF)
    conn.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
    return conn


def usage_today(cur):
    cur.execute(
        "SELECT COALESCE(actualize_calls,0) + COALESCE(detail_calls,0) FROM public.tourvisor_api_usage WHERE usage_date = CURRENT_DATE"
    )
    row = cur.fetchone()
    return row[0] if row else 0


def add_usage(cur, actualize=0, detail=0):
    cur.execute(
        """
        INSERT INTO public.tourvisor_api_usage (usage_date, actualize_calls, detail_calls, updated_at)
        VALUES (CURRENT_DATE, %s, %s, now())
        ON CONFLICT (usage_date) DO UPDATE
           SET actualize_calls = public.tourvisor_api_usage.actualize_calls + EXCLUDED.actualize_calls,
               detail_calls    = public.tourvisor_api_usage.detail_calls + EXCLUDED.detail_calls,
               updated_at      = now()
        """,
        (actualize, detail),
    )


def set_status(cur, tour_id, status, message=None):
    cur.execute(
        "UPDATE public.tour_actualizations SET status = %s, error_message = %s, fetched_at = now() WHERE tour_id = %s",
        (status, message, tour_id),
    )


def process(conn, tour_id):
    cur = conn.cursor()
    try:
        if usage_today(cur) + 2 > DAILY_CAP:
            set_status(cur, tour_id, "quota_exceeded", f"Günlük Tourvisor sorgu sınırı ({DAILY_CAP}) doldu.")
            log(f"{tour_id}: günlük sınır dolu, sorgu atılmadı")
            return

        # 1) actualize.php
        act_raw = None
        act_err = None
        try:
            act_raw = http_get_json(ACTUALIZE_URL, {"tourid": tour_id, "request": ACTUALIZE_REQUEST_MODE, "currency": 0})
        except Exception as e:  # noqa: BLE001
            act_err = f"actualize: {e}"
        finally:
            add_usage(cur, actualize=1)

        tour_pre = find_tour_object(act_raw) if act_raw is not None else None
        act_api_err = tourvisor_error_text(act_raw) if (act_raw is not None and tour_pre is None) else None

        # 2) actdetail.php — sadece Tourvisor "detay mevcut" diyorsa (kota tasarrufu)
        det_raw = None
        det_err = None
        detail_flag = to_str((tour_pre or {}).get("detailavailable"))
        if tour_pre is None:
            det_err = None  # tur bulunamadıysa detay da sorulmaz
        elif detail_flag is not None and detail_flag != "1":
            det_err = "actdetail: Tourvisor bu tur için ek bilgi sunmuyor (detailavailable=0)"
        else:
            try:
                det_raw = http_get_json(DETAIL_URL, {"tourid": tour_id, "currency": 0})
                if isinstance(det_raw, dict) and to_bool(det_raw.get("iserror")):
                    det_err = "actdetail: " + (to_str(det_raw.get("errormessage")) or "hata")
            except Exception as e:  # noqa: BLE001
                det_err = f"actdetail: {e}"
            finally:
                add_usage(cur, detail=1)

        tour = tour_pre
        if tour is None and act_raw is not None:
            act_err = act_err or (("actualize: " + act_api_err) if act_api_err else None) or "actualize: tur bulunamadı (süresi dolmuş olabilir)"

        flights = norm_flights(find_key(det_raw, "flights")) if det_raw is not None else []
        tourinfo = find_key(det_raw, "tourinfo") if det_raw is not None else None
        tourinfo = tourinfo if isinstance(tourinfo, dict) else {}
        add_payments = norm_add_payments(tourinfo.get("addpayments"))
        contents = norm_contents(tourinfo.get("contents"))
        flags = norm_flags(tourinfo.get("flags"))

        if tour is None and not flights and not add_payments and not contents:
            message = "; ".join([m for m in (act_err, det_err) if m]) or "Tourvisor bu tur için bilgi döndürmedi."
            cur.execute(
                """
                UPDATE public.tour_actualizations
                   SET status = 'error', error_message = %s, fetched_at = now(),
                       raw_actualize = %s, raw_detail = %s
                 WHERE tour_id = %s
                """,
                (message[:1000], Json(act_raw), Json(det_raw), tour_id),
            )
            log(f"{tour_id}: HATA - {message[:200]}")
            return

        t = tour or {}
        cur.execute(
            """
            UPDATE public.tour_actualizations SET
                status = 'ok',
                error_message = %s,
                fetched_at = now(),
                price = %s, currency = %s, fuel_charge = %s, visa_charge = %s,
                operator_price = %s, operator_currency = %s, operator_link = %s,
                hotel_status = %s, flight_status = %s, on_request = %s, night_flight = %s,
                placement = %s, adults = %s, child = %s,
                flights = %s, add_payments = %s, contents = %s, flags = %s,
                raw_actualize = %s, raw_detail = %s
            WHERE tour_id = %s
            """,
            (
                ("; ".join([m for m in (act_err, det_err) if m]) or None),
                to_float(t.get("price")), to_str(t.get("currency")), to_float(t.get("fuelcharge")),
                to_float(t.get("visacharge")),
                to_float(t.get("operatorprice")), to_str(t.get("operatorcurrency")), to_str(t.get("operatorlink")),
                to_int(t.get("hotelstatus")), to_int(t.get("flightstatus")), to_bool(t.get("onrequest")),
                to_int(t.get("nightflight")),
                to_str(t.get("placement")), to_int(t.get("adults")), to_int(t.get("child")),
                Json(flights), Json(add_payments), Json(contents), Json(flags),
                Json(act_raw), Json(det_raw),
                tour_id,
            ),
        )
        log(f"{tour_id}: OK - {len(flights)} uçuş seti, {len(add_payments)} ek ödeme")
    except Exception as e:  # noqa: BLE001
        try:
            set_status(cur, tour_id, "error", f"Servis hatası: {e}"[:1000])
        except Exception:  # noqa: BLE001
            pass
        log(f"{tour_id}: SERVİS HATASI - {e}")
    finally:
        cur.close()


def pending_ids(conn):
    cur = conn.cursor()
    cur.execute(
        "SELECT tour_id FROM public.tour_actualizations WHERE status = 'pending' AND requested_at > now() - interval '10 minutes' ORDER BY requested_at"
    )
    ids = [r[0] for r in cur.fetchall()]
    cur.close()
    return ids


def main():
    log(f"=== TourOS Tourvisor Actualize Worker başladı (günlük sınır {DAILY_CAP}) ===")
    while True:
        conn = None
        try:
            conn = connect()
            cur = conn.cursor()
            cur.execute(f"LISTEN {CHANNEL};")
            cur.close()

            # Servis kapalıyken gelmiş istekleri işle
            for tid in pending_ids(conn):
                process(conn, tid)

            while True:
                if select.select([conn], [], [], 60) == ([], [], []):
                    # 60 sn sessizlik: kaçan istek var mı kontrol et
                    for tid in pending_ids(conn):
                        process(conn, tid)
                    continue
                conn.poll()
                seen = set()
                while conn.notifies:
                    n = conn.notifies.pop(0)
                    tid = (n.payload or "").strip()
                    if tid and tid.isdigit() and tid not in seen:
                        seen.add(tid)
                        process(conn, tid)
        except Exception as e:  # noqa: BLE001
            log(f"Bağlantı hatası, 10 sn sonra yeniden denenecek: {e}")
            time.sleep(10)
        finally:
            if conn is not None:
                try:
                    conn.close()
                except Exception:  # noqa: BLE001
                    pass


if __name__ == "__main__":
    main()

import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * TourOS / Axileto 500 VU (Virtual Users) K6 Yük & Stres Testi
 *
 * Gerçek uç noktaları hedefler:
 * 1. Web Portal & Liveness Health Check (axileto.com)
 * 2. Canlı Operatör Arama & Liste Servisi (Tourvisor REST)
 * 3. B2B Whitelabel Rezervasyon Portalı Girişi
 */

const BASE_URL = __ENV.TARGET_URL || 'https://axileto.com';
const TOURVISOR_URL = __ENV.TOURVISOR_URL || 'http://tourvisor.ru/xml/list.php';
const TOURVISOR_LOGIN = __ENV.TOURVISOR_AUTH_LOGIN || 'Mabit23@gmail.com';
const TOURVISOR_PASS = __ENV.TOURVISOR_AUTH_PASS || 'FFytMvSU0ZHr';

const isSmokeTest = __ENV.SMOKE === 'true';

export const options = isSmokeTest
  ? {
      // Hızlı doğrulama (Smoke Test): 1 VU x 5 saniye
      vus: 1,
      duration: '5s',
      thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<3000'],
      },
    }
  : {
      // 500 Eşzamanlı Kullanıcı (VU) Kademeli Artış & İniş Senaryosu
      stages: [
        { duration: '30s', target: 50 },  // 1. Isınma (Warm-up) -> 50 VU
        { duration: '1m',  target: 200 }, // 2. Kademeli Artış (Ramp-up) -> 200 VU
        { duration: '2m',  target: 500 }, // 3. Zirve Yük (Peak Load) -> 500 VU
        { duration: '1m',  target: 500 }, // 4. Sabit Yük (Steady State) -> 500 VU
        { duration: '30s', target: 0 },   // 5. Kademeli İniş (Ramp-down) -> 0 VU
      ],
      thresholds: {
        // Genel Hata Oranı <%5 olmalı
        http_req_failed: ['rate<0.05'],
        // Yanıt sürelerinin %95'i (p95) 3000ms altında olmalı
        http_req_duration: ['p(95)<3000'],
        // Sağlık kontrolü istekleri 1500ms altında olmalı
        'http_req_duration{type:health}': ['p(95)<1500'],
        // Arama ve veri çekim istekleri 3500ms altında olmalı
        'http_req_duration{type:search}': ['p(95)<3500'],
      },
    };

export default function () {
  const defaultParams = {
    headers: {
      'User-Agent': 'TourOS-K6-LoadTest/1.0',
      'Accept': 'text/html,application/json,*/*',
    },
  };

  // 1. Sağlık Kontrolü & Web Ana Sayfa (Health Check)
  const healthRes = http.get(`${BASE_URL}/`, {
    ...defaultParams,
    tags: { type: 'health' },
  });
  check(healthRes, {
    'Health: Status 200': (r) => r.status === 200,
    'Health: Body not empty': (r) => r.body && r.body.length > 100,
  });

  sleep(0.5);

  // 2. Canlı Operatör Arama & Katalog Listeleme (Tourvisor REST)
  const searchUrl = `${TOURVISOR_URL}?authlogin=${encodeURIComponent(TOURVISOR_LOGIN)}&authpass=${encodeURIComponent(TOURVISOR_PASS)}&type=departure&format=json`;
  const searchRes = http.get(searchUrl, {
    ...defaultParams,
    tags: { type: 'search' },
  });
  check(searchRes, {
    'Search: Status 200': (r) => r.status === 200,
    'Search: Has Departures Data': (r) => r.body && r.body.includes('departures'),
  });

  sleep(0.5);

  // 3. B2B Whitelabel Rezervasyon Portalı Girişi
  const portalRes = http.get(`${BASE_URL}/?ref=ALIMAR-15012`, {
    ...defaultParams,
    tags: { type: 'portal' },
  });
  check(portalRes, {
    'Portal: Status 200': (r) => r.status === 200,
  });

  sleep(1);
}

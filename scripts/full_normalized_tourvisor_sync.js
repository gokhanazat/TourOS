const http = require('http');
const fs = require('fs');

const auth = 'authlogin=Mabit23%40gmail.com&authpass=FFytMvSU0ZHr';

function get(url) {
    return new Promise((resolve) => {
        http.get(url, res => {
            let d = '';
            res.on('data', c => d += c);
            res.on('end', () => {
                try { resolve(JSON.parse(d)); } catch(e) { resolve(null); }
            });
        }).on('error', () => resolve(null));
    });
}

function escapeSql(str) {
    if (!str) return "''";
    return "'" + str.replace(/'/g, "''") + "'";
}

// Genişletilmiş Normalizasyon Haritaları
const COUNTRY_MAP = {
    'Турция': { name: 'Türkiye', code: 'TR' },
    'Египет': { name: 'Mısır', code: 'EG' },
    'ОАЭ': { name: 'BAE', code: 'AE' },
    'Таиланд': { name: 'Tayland', code: 'TH' },
    'Мальдивы': { name: 'Maldivler', code: 'MV' },
    'Шри-Ланка': { name: 'Sri Lanka', code: 'LK' },
    'Куба': { name: 'Küba', code: 'CU' },
    'Вьетнам': { name: 'Vietnam', code: 'VN' },
    'Индонезия': { name: 'Endonezya (Bali)', code: 'ID' },
    'Кипр': { name: 'Kıbrıs', code: 'CY' },
    'Греция': { name: 'Yunanistan', code: 'GR' },
    'Черногория': { name: 'Karadağ', code: 'ME' },
    'Грузия': { name: 'Gürcistan', code: 'GE' },
    'Россия': { name: 'Rusya', code: 'RU' },
    'Сейшелы': { name: 'Seyşeller', code: 'SC' },
    'Маврикий': { name: 'Mauritius', code: 'MU' },
    'Танзания': { name: 'Zanzibar', code: 'TZ' },
    'Доминикана': { name: 'Dominik', code: 'DO' },
    'Китай': { name: 'Çin', code: 'CN' },
    'Абхазия': { name: 'Abhazya', code: 'AB' }
};

async function runMegaExpandedSync() {
    console.log('🚀 [Genişletilmiş Tüm Ülkeler Canlı Senkronizasyon Başlatıldı]...');

    // Popüler Rusya Tur Operatör Grupları
    const operatorBatches = [
        '11,12,13,18,25', // Coral, Pegas, Anex, Biblioglobus, Fun&Sun
        '21,23,27,40,43', // Panteon, Russian Express, Paks, ICS, Intourist
        '58,62,66,78,89', // Evroport, Ambotis, Space Travel, China Travel, Kazunion
        '96,118,134,143,161', // Premiera, Art Travel, Mercury, Planeta, OneTouch
        '164,169,178,189,33'  // Lets Fly, TourPlatform, Crystal Bay, One Click, Amigo-S
    ];

    const destinations = [
        // Moskova Çıkışlı Tüm Aktif Ülkeler
        { city: 1, cityName: 'Moskova', country: 4, countryRaw: 'Турция' },
        { city: 1, cityName: 'Moskova', country: 1, countryRaw: 'Египет' },
        { city: 1, cityName: 'Moskova', country: 9, countryRaw: 'ОАЭ' },
        { city: 1, cityName: 'Moskova', country: 2, countryRaw: 'Таиланд' },
        { city: 1, cityName: 'Moskova', country: 8, countryRaw: 'Мальдивы' },
        { city: 1, cityName: 'Moskova', country: 12, countryRaw: 'Шри-Ланка' },
        { city: 1, cityName: 'Moskova', country: 10, countryRaw: 'Куба' },
        { city: 1, cityName: 'Moskova', country: 16, countryRaw: 'Вьетнам' },
        { city: 1, cityName: 'Moskova', country: 7, countryRaw: 'Индонезия' },
        { city: 1, cityName: 'Moskova', country: 15, countryRaw: 'Кипр' },
        { city: 1, cityName: 'Moskova', country: 6, countryRaw: 'Греция' },
        { city: 1, cityName: 'Moskova', country: 21, countryRaw: 'Черногория' },
        { city: 1, cityName: 'Moskova', country: 54, countryRaw: 'Грузия' },
        { city: 1, cityName: 'Moskova', country: 28, countryRaw: 'Сейшелы' },
        { city: 1, cityName: 'Moskova', country: 27, countryRaw: 'Маврикий' },
        { city: 1, cityName: 'Moskova', country: 41, countryRaw: 'Танзания' },
        { city: 1, cityName: 'Moskova', country: 11, countryRaw: 'Доминикана' },
        { city: 1, cityName: 'Moskova', country: 13, countryRaw: 'Китай' },
        { city: 1, cityName: 'Moskova', country: 46, countryRaw: 'Абхазия' },
        { city: 1, cityName: 'Moskova', country: 47, countryRaw: 'Россия' },

        // St. Petersburg Çıkışlı
        { city: 2, cityName: 'Saint Petersburg', country: 4, countryRaw: 'Турция' },
        { city: 2, cityName: 'Saint Petersburg', country: 1, countryRaw: 'Египет' },
        { city: 2, cityName: 'Saint Petersburg', country: 9, countryRaw: 'ОАЭ' },
        { city: 2, cityName: 'Saint Petersburg', country: 2, countryRaw: 'Таиланд' },
        { city: 2, cityName: 'Saint Petersburg', country: 8, countryRaw: 'Мальдивы' },

        // Kazan Çıkışlı
        { city: 3, cityName: 'Kazan', country: 4, countryRaw: 'Турция' },
        { city: 3, cityName: 'Kazan', country: 1, countryRaw: 'Египет' },
        { city: 3, cityName: 'Kazan', country: 9, countryRaw: 'ОАЭ' },

        // Yekaterinburg Çıkışlı
        { city: 4, cityName: 'Yekaterinburg', country: 4, countryRaw: 'Турция' },
        { city: 4, cityName: 'Yekaterinburg', country: 1, countryRaw: 'Египет' },
        { city: 4, cityName: 'Yekaterinburg', country: 9, countryRaw: 'ОАЭ' },
        { city: 4, cityName: 'Yekaterinburg', country: 2, countryRaw: 'Таиланд' }
    ];

    const toursMap = new Map();

    for (const d of destinations) {
        for (const opBatch of operatorBatches) {
            const searchUrl = `http://tourvisor.ru/xml/search.php?city=${d.city}&country=${d.country}&operators=${opBatch}&format=json&${auth}`;
            const searchRes = await get(searchUrl);

            if (searchRes && searchRes.result && searchRes.result.requestid) {
                const reqId = searchRes.result.requestid;
                await new Promise(r => setTimeout(r, 3500));

                const resultUrl = `http://tourvisor.ru/xml/result.php?requestid=${reqId}&type=result&format=json&${auth}`;
                const resData = await get(resultUrl);

                if (resData && resData.data && resData.data.result && resData.data.result.hotel) {
                    const hotels = Array.isArray(resData.data.result.hotel) ? resData.data.result.hotel : [resData.data.result.hotel];
                    
                    for (const h of hotels) {
                        if (h.tours && h.tours.tour) {
                            const tourList = Array.isArray(h.tours.tour) ? h.tours.tour : [h.tours.tour];
                            for (const t of tourList) {
                                const rawId = t.tourid || `${h.hotelcode}-${t.operatorcode || 0}-${t.price || 0}`;
                                const uniqueId = 'tv-' + rawId;
                                
                                const normCountry = COUNTRY_MAP[h.countryname] || COUNTRY_MAP[d.countryRaw] || { name: h.countryname || d.countryRaw, code: 'TR' };
                                const normCity = d.cityName;

                                toursMap.set(uniqueId, {
                                    tourid: uniqueId,
                                    tourname: t.tourname || h.hotelname,
                                    operatorid: parseInt(t.operatorcode) || 0,
                                    operatorname: t.operatorname || 'TourVisor Partner',
                                    price: parseFloat(t.price || h.price || 0),
                                    currency: h.currency || 'RUB',
                                    hotelid: parseInt(h.hotelcode) || 0,
                                    hotelname: h.hotelname || '',
                                    hotelstars: parseInt(h.hotelstars) || 3,
                                    hotelrating: parseFloat(h.hotelrating || '0') || 0.0,
                                    country: normCountry.name,
                                    country_code: normCountry.code,
                                    country_name: normCountry.name,
                                    region: h.regionname || '',
                                    subregion: h.subregionname || '',
                                    room: t.room || 'Standart',
                                    meal: t.mealrussian || t.meal || 'Her Şey Dahil',
                                    flydate: t.flydate || '2026-09-05',
                                    nights: parseInt(t.nights) || 7,
                                    pictureurl: h.picturelink || '',
                                    departure_city: normCity
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    const uniqueTours = Array.from(toursMap.values());
    console.log(`✅ [Tamamlandı] Normalize Edilmiş Benzersiz Canlı Tur: ${uniqueTours.length}`);

    const countriesFound = [...new Set(uniqueTours.map(t => t.country_name))].sort();
    console.log(`Bulunan Ülkeler (${countriesFound.length} Adet):`, countriesFound.join(', '));

    let sql = `-- TourOS Fully Normalized Staging Feed\nTRUNCATE TABLE public.marketplace_products_staging;\nINSERT INTO public.marketplace_products_staging (\n  id, product_type, tour_name, operator_id, operator_name, price, currency,\n  hotel_id, hotel_name, hotel_category, hotel_rating, country, country_code, country_name, region, sub_region,\n  room_type, meal_type, departure_city, departure_date, nights, picture_url, is_published, is_active\n) VALUES\n`;
    
    const rows = uniqueTours.map(t => {
        let parts = t.flydate.split('.');
        let isoDate = parts.length === 3 ? `${parts[2]}-${parts[1]}-${parts[0]}` : '2026-09-05';
        return `(${escapeSql(t.tourid)}, 'PACKAGE_TOUR', ${escapeSql(t.tourname)}, ${t.operatorid}, ${escapeSql(t.operatorname)}, ${t.price}, ${escapeSql(t.currency)}, ${t.hotelid}, ${escapeSql(t.hotelname)}, ${t.hotelstars}, ${t.hotelrating}, ${escapeSql(t.country)}, ${escapeSql(t.country_code)}, ${escapeSql(t.country_name)}, ${escapeSql(t.region)}, ${escapeSql(t.subregion)}, ${escapeSql(t.room)}, ${escapeSql(t.meal)}, ${escapeSql(t.departure_city)}, '${isoDate}', ${t.nights}, ${escapeSql(t.pictureurl)}, true, true)`;
    });

    sql += rows.join(',\n') + ' ON CONFLICT (id) DO NOTHING;\n';
    fs.writeFileSync('scripts/normalized_live_feed.sql', sql, 'utf8');
}

runMegaExpandedSync();

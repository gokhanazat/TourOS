const http = require('http');
const fs = require('fs');

const auth = 'authlogin=Mabit23%40gmail.com&authpass=FFytMvSU0ZHr';

function get(url) {
    return new Promise((resolve, reject) => {
        http.get(url, res => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch(e) {
                    resolve(null);
                }
            });
        }).on('error', reject);
    });
}

function escapeSql(str) {
    if (!str) return "''";
    return "'" + str.replace(/'/g, "''") + "'";
}

async function runLiveIngestion() {
    const destinations = [
        { city: 1, country: 4, name: 'Moskova - Türkiye' },
        { city: 1, country: 1, name: 'Moskova - Mısır' },
        { city: 1, country: 9, name: 'Moskova - BAE' },
        { city: 1, country: 2, name: 'Moskova - Tayland' },
        { city: 2, country: 4, name: 'St. Petersburg - Türkiye' },
        { city: 2, country: 1, name: 'St. Petersburg - Mısır' },
        { city: 3, country: 4, name: 'Kazan - Türkiye' },
        { city: 4, country: 4, name: 'Yekaterinburg - Türkiye' }
    ];

    const toursMap = new Map();

    for (const d of destinations) {
        console.log(`[Canlı İstek] ${d.name}...`);
        const searchUrl = `http://tourvisor.ru/xml/search.php?city=${d.city}&country=${d.country}&format=json&${auth}`;
        const searchRes = await get(searchUrl);

        if (searchRes && searchRes.result && searchRes.result.requestid) {
            const reqId = searchRes.result.requestid;
            await new Promise(r => setTimeout(r, 4500));

            const resultUrl = `http://tourvisor.ru/xml/result.php?requestid=${reqId}&type=result&format=json&${auth}`;
            const resData = await get(resultUrl);

            if (resData && resData.data && resData.data.result && resData.data.result.hotel) {
                const hotels = Array.isArray(resData.data.result.hotel) ? resData.data.result.hotel : [resData.data.result.hotel];
                
                for (const h of hotels) {
                    if (h.tours && h.tours.tour) {
                        const tourList = Array.isArray(h.tours.tour) ? h.tours.tour : [h.tours.tour];
                        for (const t of tourList) {
                            const rawId = t.tourid || (h.hotelcode + '-' + (t.price || 0));
                            const uniqueId = 'tv-' + rawId;
                            
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
                                country: h.countryname || d.name.split(' - ')[1],
                                region: h.regionname || '',
                                subregion: h.subregionname || '',
                                room: t.room || 'Standart',
                                meal: t.mealrussian || t.meal || 'Her Şey Dahil',
                                flydate: t.flydate || '2026-09-05',
                                nights: parseInt(t.nights) || 7,
                                pictureurl: h.picturelink || '',
                                departure_city: d.name.split(' - ')[0]
                            });
                        }
                    }
                }
            }
        }
    }

    const uniqueTours = Array.from(toursMap.values());
    console.log(`Benzersiz Toplam Canlı Tur: ${uniqueTours.length}`);

    let sql = `-- TourOS Staging Feed Injection\nTRUNCATE TABLE public.marketplace_products_staging;\nINSERT INTO public.marketplace_products_staging (\n  id, product_type, tour_name, operator_id, operator_name, price, currency,\n  hotel_id, hotel_name, hotel_category, hotel_rating, country, region, sub_region,\n  room_type, meal_type, departure_city, departure_date, nights, picture_url, is_published, is_active\n) VALUES\n`;
    
    const rows = uniqueTours.map(t => {
        let parts = t.flydate.split('.');
        let isoDate = parts.length === 3 ? `${parts[2]}-${parts[1]}-${parts[0]}` : '2026-09-05';
        return `(${escapeSql(t.tourid)}, 'PACKAGE_TOUR', ${escapeSql(t.tourname)}, ${t.operatorid}, ${escapeSql(t.operatorname)}, ${t.price}, ${escapeSql(t.currency)}, ${t.hotelid}, ${escapeSql(t.hotelname)}, ${t.hotelstars}, ${t.hotelrating}, ${escapeSql(t.country)}, ${escapeSql(t.region)}, ${escapeSql(t.subregion)}, ${escapeSql(t.room)}, ${escapeSql(t.meal)}, ${escapeSql(t.departure_city)}, '${isoDate}', ${t.nights}, ${escapeSql(t.pictureurl)}, true, true)`;
    });

    sql += rows.join(',\n') + ' ON CONFLICT (id) DO NOTHING;\n';
    fs.writeFileSync('scripts/live_staging_feed.sql', sql, 'utf8');
}

runLiveIngestion();

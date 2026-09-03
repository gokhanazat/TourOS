SELECT 'marketplace_products' AS tbl, currency, COUNT(*) FROM marketplace_products GROUP BY currency
UNION ALL
SELECT 'hotel_contracts' AS tbl, currency, COUNT(*) FROM hotel_contracts GROUP BY currency
UNION ALL
SELECT 'hotel_season_rates' AS tbl, currency, COUNT(*) FROM hotel_season_rates GROUP BY currency
UNION ALL
SELECT 'bookings' AS tbl, currency, COUNT(*) FROM bookings GROUP BY currency
UNION ALL
SELECT 'invoices' AS tbl, currency, COUNT(*) FROM invoices GROUP BY currency
UNION ALL
SELECT 'flight_schedules' AS tbl, currency, COUNT(*) FROM flight_schedules GROUP BY currency
UNION ALL
SELECT 'club_tour_offers' AS tbl, currency, COUNT(*) FROM club_tour_offers GROUP BY currency;

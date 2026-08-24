-- ====================================================================
-- TourOS: Sayfa İçi Akıllı Yardım & Rehber Asistanı Tablosu ve Seed Verisi
-- Migration: 20260824_001_create_in_app_help_guides.sql
-- ====================================================================

CREATE TABLE IF NOT EXISTS public.in_app_help_guides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_route TEXT NOT NULL,
    field_key TEXT DEFAULT NULL,
    category TEXT NOT NULL DEFAULT 'GENEL',
    lang TEXT NOT NULL DEFAULT 'tr',
    title TEXT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    step_order INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- İndeksler
CREATE INDEX IF NOT EXISTS idx_in_app_help_screen_lang ON public.in_app_help_guides(screen_route, lang);
CREATE INDEX IF NOT EXISTS idx_in_app_help_active ON public.in_app_help_guides(is_active);

-- RLS (Row Level Security)
ALTER TABLE public.in_app_help_guides ENABLE ROW LEVEL SECURITY;

-- Okuma Yetkisi: Tüm doğrulanmış ve anonim acente kullanıcıları yardım içeriklerini okuyabilir
DROP POLICY IF EXISTS "Allow all users to read active help guides" ON public.in_app_help_guides;
CREATE POLICY "Allow all users to read active help guides"
    ON public.in_app_help_guides
    FOR SELECT
    USING (is_active = true);

-- Yazma Yetkisi: Sadece Sistem Yöneticileri düzenleyebilir
DROP POLICY IF EXISTS "Allow system admins to manage help guides" ON public.in_app_help_guides;
CREATE POLICY "Allow system admins to manage help guides"
    ON public.in_app_help_guides
    FOR ALL
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles p
            WHERE p.id = auth.uid()
            AND (p.role = 'SYSTEM_ADMIN' OR p.role = 'SUPER_ADMIN' OR p.email = 'gkhnazat@gmail.com')
        )
    );

-- ====================================================================
-- SEED VERİSİ (TÜM KULLANICI EKRANLARI İÇİN TR VE EN YARDIM SETLERİ)
-- ====================================================================

-- 1. YENİ TUR OLUŞTURMA & DÜZENLEME (TourFormRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('TourFormRoute', 'tour_name', 'TEMEL_BILGILER', 'tr', 'Tur Adı Nasıl Yazılmalı?', 'Tur Adı alanına ne yazmalıyım?', 'Turun müşteriye ve web kataloğuna yansıyacak resmi adıdır. Örneğin: "Kapadokya Balon & Kültür Turu". Açık ve çekici bir isim belirleyin.', 1),
('TourFormRoute', 'tour_code', 'TEMEL_BILGILER', 'tr', 'Tur Kodu Formatı', 'Tur Kodu nasıl belirlenmelidir?', 'Acentanızın turları hızlıca ayırt etmesi için benzersiz bir koddur (Örn: KPD-2026). Sistemde aramalarda ve voucher üzerinde bu kod kullanılır.', 2),
('TourFormRoute', 'category', 'TEMEL_BILGILER', 'tr', 'Kategori Seçimi', 'Tur kategorisini nasıl seçmeliyim?', 'Turun içeriğine en uygun kategoriyi (Günübirlik, Kültür, Macera, Otel Paket vb.) tıklayarak seçin. Bu seçim web vitrinindeki filtrelemeyi belirler.', 3),
('TourFormRoute', 'location_duration', 'LOKASYON_SURE', 'tr', 'Lokasyon ve Süre Bilgisi', 'Ülke, Şehir ve Gün Süresi nasıl girilir?', 'Turun başlangıç veya ana merkezinin Ülke ve Şehir bilgilerini seçin. Süre kısmına gün sayısını girin (Örn: Günübirlik turlar için 1, haftalık turlar için 7).', 4),
('TourFormRoute', 'capacity', 'KONTENJAN', 'tr', 'Kapasite ve Kontenjan Ayarı', 'Toplam Kapasite, Min ve Max Kişi nedir?', 'Toplam Kapasite: Turdaki toplam koltuk sayısıdır. Min Kişi: Turun kesin kalkışı için gereken asgari katılımcıdır. Max Kişi: Kabul edilecek üst sınırdır.', 5),
('TourFormRoute', 'pricing_pax', 'FIYATLANDIRMA', 'tr', 'Satış ve Pax Maliyet Hesaplama', 'Kişi Başı Maliyet ve Karlılık nasıl çalışır?', 'Yetişkin ve çocuk yaş grupları için satış fiyatı ve tedarik maliyetinizi girin. Sayfanın altındaki "Tahmini Pax Başı Karlılık Analizi" kutusu net kârınızı ve marjınızı (%) anında hesaplar.', 6),
('TourFormRoute', 'cover_image', 'MEDYA', 'tr', 'Kapak Fotoğrafı & Galeri', 'Fotoğraflar nasıl yüklenir ve nerede görünür?', 'Kapak resmi teklif formlarında ve web vitrininde ilk görünen ana fotoğraftır (Maks. 1MB). Ek Galeriye en fazla 10 adet kaliteli fotoğraf ekleyebilirsiniz.', 7),

-- 1. EN VERSION (TourFormRoute)
('TourFormRoute', 'tour_name', 'TEMEL_BILGILER', 'en', 'How to write Tour Name?', 'What should I enter in the Tour Name field?', 'The official name displayed to customers and the web catalog (e.g. "Cappadocia Balloon & Culture Tour"). Keep it descriptive and appealing.', 1),
('TourFormRoute', 'tour_code', 'TEMEL_BILGILER', 'en', 'Tour Code Format', 'How should I determine the Tour Code?', 'A unique identifier for your agency (e.g. KPD-2026). Used in search filters, booking vouchers, and invoices.', 2),
('TourFormRoute', 'category', 'TEMEL_BILGILER', 'en', 'Category Selection', 'How do I choose the tour category?', 'Select the category tag (Day Tour, Cultural, Adventure, Hotel Package, etc.). It determines customer filtering in the web storefront.', 3),
('TourFormRoute', 'pricing_pax', 'FIYATLANDIRMA', 'en', 'Sales & Pax Cost Calculation', 'How does Cost per Pax & Profitability work?', 'Enter sales price and supplier costs for adults and child brackets. The Profitability Analysis box at the bottom instantly calculates net profit and margin (%).', 4),

-- 1. RU VERSION (TourFormRoute)
('TourFormRoute', 'tour_name', 'TEMEL_BILGILER', 'ru', 'Как указать название тура?', 'Что писать в поле Название тура?', 'Официальное название тура для клиентов и веб-каталога (например, "Каппадокия: Воздушные шары и культура"). Указывайте понятное и привлекательное название.', 1),
('TourFormRoute', 'tour_code', 'TEMEL_BILGILER', 'ru', 'Формат кода тура', 'Как формируется Код тура?', 'Уникальный идентификатор агентства (например, KPD-2026). Используется в поиске, ваучерах и счетах.', 2),
('TourFormRoute', 'category', 'TEMEL_BILGILER', 'ru', 'Выбор категории', 'Как выбрать категорию тура?', 'Выберите подходящую категорию (Однодневный, Культурный, Приключения, Пакет с отелем и т.д.) для фильтрации на сайте.', 3),
('TourFormRoute', 'location_duration', 'LOKASYON_SURE', 'ru', 'Локация и длительность', 'Как указать Страну, Город и Дни?', 'Выберите страну и город отправления. В поле длительности укажите количество дней (например: 1 для однодневных, 7 для недельных туров).', 4),
('TourFormRoute', 'capacity', 'KONTENJAN', 'ru', 'Вместимость и квоты', 'Что такое Общая вместимость, Мин и Макс чел.?', 'Общая вместимость: общее число мест. Мин. чел: минимальная группа для гарантированного выезда. Макс. чел: предел бронирования.', 5),
('TourFormRoute', 'pricing_pax', 'FIYATLANDIRMA', 'ru', 'Цены и расчет рентабельности', 'Как работает расчет себестоимости и прибыли?', 'Укажите цены продажи и себестоимость для взрослых и детей. Блок "Анализ рентабельности на чел." автоматически рассчитает чистую прибыль и маржу (%).', 6),
('TourFormRoute', 'cover_image', 'MEDYA', 'ru', 'Обложка и галерея', 'Как загрузить фото тура?', 'Главное фото используется на сайте и в коммерческих предложениях (макс. 1 МБ). В галерею можно добавить до 10 дополнительных фото.', 7);

-- 2. YENİ OTEL KAYDI & DÜZENLEME (HotelFormRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('HotelFormRoute', 'hotel_name', 'TEMEL_BILGILER', 'tr', 'Otel Adı ve Konumu', 'Otel Adı ve Şehir alanlarına ne yazmalıyım?', 'Anlaşmalı tesisin tam ticari veya bilinen adını (Örn: Grand Cave Resort & Spa) ve bulunduğu şehri girin. Tur paketlerinde otel seçerken bu isimle arayacaksınız.', 1),
('HotelFormRoute', 'star_rating', 'TEMEL_BILGILER', 'tr', 'Yıldız ve Pansiyon Tipi', 'Yıldız derecesi ve Pansiyon Tipi ne işe yarar?', 'Tesisin resmi yıldız derecesini (1-5) ve konaklama konseptini (Oda Kahvaltı, Yarım Pansiyon, Her Şey Dahil vb.) belirler. Tekliflerde müşteriye otomatik iletilir.', 2),
('HotelFormRoute', 'periods_rates', 'FIYATLANDIRMA', 'tr', 'Otel Periyotları & Fiyatlandırma', 'Periyot nasıl açılır ve fiyat nasıl girilir?', '+ Yeni Periyot Ekle butonuna basarak sezonları (Örn: Yaz Sezonu, Kış Sezonu) tarih aralıklarıyla tanımlayın. Oda bazında maliyet ve satış fiyatı girerek dönemsel fiyatlandırma sağlayın.', 3),
('HotelFormRoute', 'cover_image', 'MEDYA', 'tr', 'Görsel Seçimi & Açıklama', 'Otel görseli ve tanıtım metni nasıl eklenir?', 'Yerel bilgisayarınızdan dosya seçebilir veya görsel linki (URL) yapıştırabilirsiniz. Tanıtım açıklamasına SPA, havuz, manzara gibi öne çıkan özellikleri yazın.', 4),

-- 2. EN VERSION (HotelFormRoute)
('HotelFormRoute', 'hotel_name', 'TEMEL_BILGILER', 'en', 'Hotel Name and Location', 'What should I enter for Hotel Name and City?', 'Enter the full registered name of the hotel (e.g. Grand Cave Resort & Spa) and its city. This will be used when pairing hotels with tour packages.', 1),
('HotelFormRoute', 'periods_rates', 'FIYATLANDIRMA', 'en', 'Hotel Seasons & Pricing', 'How do periods and room rates work?', 'Click "+ Add New Period" to define seasonal date ranges (e.g., High Season, Low Season) and assign specific room allotments, costs, and selling rates.', 2),

-- 2. RU VERSION (HotelFormRoute)
('HotelFormRoute', 'hotel_name', 'TEMEL_BILGILER', 'ru', 'Название отеля и город', 'Что писать в полях Название отеля и Город?', 'Введите официальное название отеля-партнера (например: Grand Cave Resort & Spa) и город. Используется при формировании туров.', 1),
('HotelFormRoute', 'star_rating', 'TEMEL_BILGILER', 'ru', 'Звездность и тип питания', 'Для чего нужны Звезды и Тип питания?', 'Указывает категорию отеля (1-5 звезд) и концепцию (Завтрак, Полупансион, Все включено). Отображается в ваучерах.', 2),
('HotelFormRoute', 'periods_rates', 'FIYATLANDIRMA', 'ru', 'Периоды сезонов и цены комнат', 'Как настроить сезонные тарифы?', 'Нажмите "+ Добавить период", укажите диапазон дат (Высокий/Низкий сезон) и задайте себестоимость и цену продажи по категориям номеров.', 3);

-- 3. B2B TUR & HİZMET ARAMA / YENİ REZERVASYON (B2BTourSearchDashboardRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('B2BTourSearchDashboardRoute', 'search_tabs', 'ARAMA', 'tr', 'Arama Sekmeleri Nasıl Kullanılır?', '5 Sekmeli arama çubuğu nasıl çalışır?', 'Paket Turlar, Oteller, Uçak Biletleri, Mavi Yolculuk ve Macera sekmelerinden ilgili hizmet türünü seçip tarih, kişi sayısı ve destinasyona göre anında sorgulama yapabilirsiniz.', 1),
('B2BTourSearchDashboardRoute', 'live_filters', 'FILTRELER', 'tr', 'Filtreleme Seçenekleri', 'Sonuçları nasıl daraltabilirim?', 'Sol paneldeki fiyat aralığı, yıldız sayısı, pansiyon tipi ve kalkış şehri filtrelerini kullanarak müşterinize en uygun seçenekleri saniyeler içinde listeleyin.', 2),
('B2BTourSearchDashboardRoute', 'fast_booking', 'REZERVASYON', 'tr', 'Hızlı Rezervasyon Akışı', 'Beğenilen bir ürünü nasıl rezerve ederim?', 'Ürün kartındaki "Rezerve Et" veya "Detay" butonuna tıklayarak doğrudan yolcu bilgileri ve ödeme adımına (Checkout Sihirbazı) geçebilirsiniz.', 3),

-- 3. RU VERSION (B2BTourSearchDashboardRoute)
('B2BTourSearchDashboardRoute', 'search_tabs', 'ARAMA', 'ru', 'Поиск по 5 вкладкам', 'Как искать услуги по категориям?', 'Переключайтесь между вкладками (Пакетные туры, Отели, Авиабилеты, Круизы, Приключения) для быстрого подбора по датам и направлениям.', 1),
('B2BTourSearchDashboardRoute', 'fast_booking', 'REZERVASYON', 'ru', 'Быстрое бронирование', 'Как забронировать выбранный тур?', 'Нажмите "Забронировать" на карточке товара для мгновенного перехода к вводу данных пассажиров и оформлению заказа.', 2);

-- 4. REZERVASYON LİSTESİ & DETAYI (BookingsRoute, BookingDetailRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('BookingsRoute', 'pnr_filter', 'YONETIM', 'tr', 'Rezervasyon Arama & Durumlar', 'Rezervasyonları nasıl arar ve filtrelerim?', 'Üst arama çubuğuna PNR Kodu, Müşteri Adı veya Telefon yazabilirsiniz. Durum filtreleri (Onaylandı, Beklemede, İptal Edildi) ile operasyonunuzu yönetin.', 1),
('BookingsRoute', 'voucher_action', 'ISLEMLER', 'tr', 'Voucher & Sözleşme İndirme', 'Müşteri voucher belgesi nasıl oluşturulur?', 'Rezervasyon satırındaki işlemler menüsünden "Voucher PDF" butonuna tıklayarak acenta logolu resmi voucher belgesini anında PDF olarak yazdırabilir veya müşteriye gönderebilirsiniz.', 2),

-- 4. RU VERSION (BookingsRoute)
('BookingsRoute', 'pnr_filter', 'YONETIM', 'ru', 'Поиск бронирований', 'Как найти заказ по PNR или клиенту?', 'Используйте строку поиска по номеру PNR, имени или телефону. Фильтруйте по статусам: Подтвержден, В ожидании, Отменен.', 1),
('BookingsRoute', 'voucher_action', 'ISLEMLER', 'ru', 'Печать ваучера и договора', 'Как сформировать ваучер для туриста?', 'В меню заказа нажмите кнопку "Ваучер PDF", чтобы мгновенно сгенерировать официальный брендированный документ с логотипом агентства.', 2);

-- 5. FİNANS, FATURA & CARİ HESAP (FinancialReportsRoute, InvoiceManagementRoute, CurrentAccountRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('FinancialReportsRoute', 'cash_flow', 'FINANS', 'tr', 'Nakit Akışı & Gelir-Gider', 'Finansal özet tabloları neleri gösterir?', 'Seçilen tarih aralığındaki toplam ciro, tahsil edilen ödemeler, bekleyen alacaklar, tedarikçi giderleri ve net kâr marjınızı anlık olarak özetler.', 1),
('InvoiceManagementRoute', 'create_invoice', 'FATURA', 'tr', 'Fatura Kesme & E-Arşiv', 'Rezervasyona ait fatura nasıl oluşturulur?', '"Yeni Fatura Oluştur" butonuna basarak ilgili rezervasyonun PNR kodunu seçin. Müşteri bilgileri, KDV oranları ve toplam tutar otomatik doldurulur.', 1),
('CurrentAccountRoute', 'statement', 'CARI', 'tr', 'Cari Hesap Ekstresi Alma', 'Müşteri veya acenta cari ekstresi nasıl çekilir?', 'Cari hesap listesinden ilgili firmayı seçip "Ekstre Görüntüle" diyerek borç, alacak ve bakiye hareketlerini tarih bazlı listeleyebilir ve Excel/PDF formatında indirebilirsiniz.', 1),

-- 5. RU VERSION (FinancialReportsRoute, InvoiceManagementRoute, CurrentAccountRoute)
('FinancialReportsRoute', 'cash_flow', 'FINANS', 'ru', 'Денежный поток и отчеты', 'Что показывают финансовые таблицы?', 'Отображает оборот, фактические поступления, дебиторскую задолженность, расходы поставщиков и чистую прибыль за выбранный период.', 1),
('InvoiceManagementRoute', 'create_invoice', 'FATURA', 'ru', 'Выписка счетов и фактур', 'Как выставить счет по заказу?', 'Нажмите "Создать счет" и выберите PNR. Данные клиента, НДС и итоговые суммы заполнятся автоматически.', 1),
('CurrentAccountRoute', 'statement', 'CARI', 'ru', 'Акт сверки и выписка по счету', 'Как получить выписку по контрагенту?', 'В списке взаиморасчетов нажмите "Показать выписку" для просмотра дебета, кредита и скачивания в PDF/Excel.', 1);

-- 6. ŞİRKET AYARLARI & MARKA (CompanySettingsRoute, SettingsRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('CompanySettingsRoute', 'brand_logo', 'MARKA', 'tr', 'Kurumsal Logo & Renk Teması', 'Marka sekmesindeki logo ve renk nerede kullanılır?', 'Yüklediğiniz kurumsal logo ve seçtiğiniz ana kurumsal renk kodu (Hex), web sitenizin üst/alt menülerinde, teklif formlarında ve resmi voucher belgelerinde otomatik uygulanır.', 1),
('CompanySettingsRoute', 'bank_accounts', 'BANKA', 'tr', 'Banka & PayPal Bilgileri', 'IBAN ve ödeme bilgileri ne işe yarar?', 'Müşterilerinizin rezervasyon yaparken gördüğü Havale/EFT banka hesap numaraları ve online PayPal linkleri bu alandan yönetilir.', 2),
('CompanySettingsRoute', 'seasons_commission', 'SEZON', 'tr', 'Sezonlar & Komisyon Oranları', 'Sezonluk komisyon oranları nasıl çalışır?', 'Yüksek ve düşük sezonlar için başlangıç-bitiş tarihleri ve komisyon oranları (%) belirleyin. Rezervasyon hareket tarihine göre sistem doğru komisyonu otomatik hesaplar.', 3),
('CompanySettingsRoute', 'languages_currencies', 'DIL_KUR', 'tr', 'Desteklenen Diller & Para Birimleri', 'Çoklu dil ve para birimi nasıl ayarlanır?', 'Sisteminizde ve web vitrininizde aktif olmasını istediğiniz para birimlerini (TRY, EUR, USD vb.) ve dilleri (Türkçe, İngilizce vb.) seçebilirsiniz.', 4),

-- 6. RU VERSION (CompanySettingsRoute)
('CompanySettingsRoute', 'brand_logo', 'MARKA', 'ru', 'Логотип и фирменный цвет', 'Где используются логотип и цвет бренда?', 'Загруженный логотип и фирменный Hex-код автоматически применяются в шапке/футере сайта, в коммерческих предложениях и ваучерах.', 1),
('CompanySettingsRoute', 'bank_accounts', 'BANKA', 'ru', 'Банковские реквизиты и PayPal', 'Для чего нужны банковские счета?', 'Реквизиты IBAN для безналичных переводов и ссылки PayPal отображаются туристам при оформлении оплаты.', 2),
('CompanySettingsRoute', 'seasons_commission', 'SEZON', 'ru', 'Сезоны и комиссия', 'Как работают сезонные комиссии агентства?', 'Настройте даты высокого/низкого сезона и % комиссии. Система рассчитает размер агентского вознаграждения автоматически.', 3),
('CompanySettingsRoute', 'languages_currencies', 'DIL_KUR', 'ru', 'Языки и валюты', 'Как настроить мультиязычность и валюты?', 'Выберите активные валюты (RUB, USD, EUR, TRY) и языки панели управления и онлайн-витрины.', 4);

-- 7. DASHBOARD (DashboardRoute)
INSERT INTO public.in_app_help_guides (screen_route, field_key, category, lang, title, question, answer, step_order) VALUES
('DashboardRoute', 'kpi_cards', 'OZET', 'tr', 'KPI Özet Kartları Neleri İfade Eder?', 'Dashboard üzerindeki ana metrikler nelerdir?', 'Günlük/Aylık Rezervasyon Sayısı, Toplam Satış Hacmi, Doluluk Oranı ve Bekleyen Tahsilatlar gibi işletmenizin en kritik performans göstergelerini anlık sunar.', 1),
('DashboardRoute', 'quick_actions', 'ISLEMLER', 'tr', 'Hızlı İşlemler Menüsü', 'Sık kullanılan işlemlere nasıl hızlı ulaşırım?', 'Dashboard üzerinden tek tıkla "Yeni Rezervasyon", "Yeni Tur", "Müşteri Ekle" veya "Fatura Kes" pencerelerine hızlıca geçiş yapabilirsiniz.', 2),

-- 7. RU VERSION (DashboardRoute)
('DashboardRoute', 'kpi_cards', 'OZET', 'ru', 'Ключевые показатели (KPI)', 'Что отображают карточки дашборда?', 'Оперативные данные о продажах за день/месяц, коэффициенте загрузки, выставленных счетах и ожидаемых оплатах.', 1),
('DashboardRoute', 'quick_actions', 'ISLEMLER', 'ru', 'Быстрые действия', 'Как быстро перейти к операциям?', 'Кнопки быстрого доступа позволяют в один клик перейти к созданию нового тура, отеля, бронирования или счета.', 2);

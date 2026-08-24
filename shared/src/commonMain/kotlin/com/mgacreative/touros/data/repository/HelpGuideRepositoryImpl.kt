package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.HelpGuide
import com.mgacreative.touros.domain.repository.HelpGuideRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json

class HelpGuideRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : HelpGuideRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getHelpGuidesForScreen(screenRoute: String, lang: String): Result<List<HelpGuide>> {
        return runCatching {
            val normalizedRoute = screenRoute.substringBefore("?").substringBefore("/")
            val remoteList = runCatching {
                supabaseClient.postgrest.from("in_app_help_guides")
                    .select {
                        filter {
                            eq("screen_route", normalizedRoute)
                            eq("lang", lang)
                            eq("is_active", true)
                        }
                    }
                    .decodeList<HelpGuide>()
            }.getOrNull()

            if (!remoteList.isNullOrEmpty()) {
                remoteList.sortedBy { it.stepOrder }
            } else {
                // Fallback to built-in local guides
                getFallbackGuides(normalizedRoute, lang)
            }
        }
    }

    override suspend fun getAllHelpGuides(lang: String): Result<List<HelpGuide>> {
        return runCatching {
            val remoteList = runCatching {
                supabaseClient.postgrest.from("in_app_help_guides")
                    .select {
                        filter {
                            eq("lang", lang)
                            eq("is_active", true)
                        }
                    }
                    .decodeList<HelpGuide>()
            }.getOrNull()

            if (!remoteList.isNullOrEmpty()) {
                remoteList.sortedBy { it.stepOrder }
            } else {
                getAllFallbackGuides(lang)
            }
        }
    }

    private fun getFallbackGuides(screenRoute: String, lang: String): List<HelpGuide> {
        val all = getAllFallbackGuides(lang)
        val matched = all.filter { it.screenRoute.contains(screenRoute, ignoreCase = true) || screenRoute.contains(it.screenRoute, ignoreCase = true) }
        return if (matched.isNotEmpty()) matched else all.filter { it.screenRoute == "DashboardRoute" }
    }

    private fun getAllFallbackGuides(lang: String): List<HelpGuide> {
        val lower = lang.lowercase()
        return when {
            lower.startsWith("ru") -> listOf(
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "tour_name",
                    category = "TEMEL_BILGILER",
                    lang = "ru",
                    title = "Как указать название тура?",
                    question = "Что писать в поле Название тура?",
                    answer = "Официальное название тура для клиентов и веб-каталога (например, 'Каппадокия: Воздушные шары и культура'). Указывайте понятное и привлекательное название.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "tour_code",
                    category = "TEMEL_BILGILER",
                    lang = "ru",
                    title = "Формат кода тура",
                    question = "Как формируется Код тура?",
                    answer = "Уникальный идентификатор агентства (например, KPD-2026). Используется в поиске, ваучерах и счетах.",
                    stepOrder = 2
                ),
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "hotel_name",
                    category = "TEMEL_BILGILER",
                    lang = "ru",
                    title = "Название отеля и город",
                    question = "Что писать в полях Название отеля и Город?",
                    answer = "Введите официальное название отеля-партнера (например: Grand Cave Resort & Spa) и город. Используется при формировании туров.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "periods_rates",
                    category = "FIYATLANDIRMA",
                    lang = "ru",
                    title = "Периоды сезонов и цены",
                    question = "Как настроить сезонные тарифы?",
                    answer = "Нажмите '+ Добавить период', укажите диапазон дат (Высокий/Низкий сезон) и задайте себестоимость и цену продажи по комнатам.",
                    stepOrder = 2
                ),
                HelpGuide(
                    screenRoute = "CompanySettingsRoute",
                    fieldKey = "brand_logo",
                    category = "MARKA",
                    lang = "ru",
                    title = "Логотип и фирменный цвет",
                    question = "Где используются логотип и цвет бренда?",
                    answer = "Загруженный логотип и фирменный Hex-код автоматически применяются в шапке/футере сайта, в предложениях и ваучерах.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "DashboardRoute",
                    fieldKey = "kpi_cards",
                    category = "OZET",
                    lang = "ru",
                    title = "Ключевые показатели (KPI)",
                    question = "Что отображают карточки дашборда?",
                    answer = "Оперативные данные о продажах за день/месяц, коэффициенте загрузки, выставленных счетах и ожидаемых оплатах.",
                    stepOrder = 1
                )
            )
            lower.startsWith("tr") -> listOf(
                // 1. Yeni Tur Oluştur
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "tour_name",
                    category = "TEMEL_BILGILER",
                    lang = "tr",
                    title = "Tur Adı Nasıl Yazılmalı?",
                    question = "Tur Adı alanına ne yazmalıyım?",
                    answer = "Turun müşteriye ve web kataloğuna yansıyacak resmi adıdır. Örneğin: 'Kapadokya Balon & Kültür Turu'. Açık ve çekici bir isim belirleyin.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "tour_code",
                    category = "TEMEL_BILGILER",
                    lang = "tr",
                    title = "Tur Kodu Formatı",
                    question = "Tur Kodu nasıl belirlenmelidir?",
                    answer = "Acentanızın turları hızlıca ayırt etmesi için benzersiz bir koddur (Örn: KPD-2026). Sistemde aramalarda ve voucher üzerinde bu kod kullanılır.",
                    stepOrder = 2
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "category",
                    category = "TEMEL_BILGILER",
                    lang = "tr",
                    title = "Kategori Seçimi",
                    question = "Tur kategorisini nasıl seçmeliyim?",
                    answer = "Turun içeriğine en uygun kategoriyi (Günübirlik, Kültür, Macera, Otel Paket vb.) tıklayarak seçin. Bu seçim web vitrinindeki filtrelemeyi belirler.",
                    stepOrder = 3
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "location_duration",
                    category = "LOKASYON_SURE",
                    lang = "tr",
                    title = "Lokasyon ve Süre Bilgisi",
                    question = "Ülke, Şehir ve Gün Süresi nasıl girilir?",
                    answer = "Turun başlangıç veya ana merkezinin Ülke ve Şehir bilgilerini seçin. Süre kısmına gün sayısını girin (Örn: Günübirlik turlar için 1, haftalık turlar için 7).",
                    stepOrder = 4
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "capacity",
                    category = "KONTENJAN",
                    lang = "tr",
                    title = "Kapasite ve Kontenjan Ayarı",
                    question = "Toplam Kapasite, Min ve Max Kişi nedir?",
                    answer = "Toplam Kapasite: Turdaki toplam koltuk sayısıdır. Min Kişi: Turun kesin kalkışı için gereken asgari katılımcıdır. Max Kişi: Kabul edilecek üst sınırdır.",
                    stepOrder = 5
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "pricing_pax",
                    category = "FIYATLANDIRMA",
                    lang = "tr",
                    title = "Satış ve Pax Maliyet Hesaplama",
                    question = "Kişi Başı Maliyet ve Karlılık nasıl çalışır?",
                    answer = "Yetişkin ve çocuk yaş grupları için satış fiyatı ve tedarik maliyetinizi girin. Sayfanın altındaki 'Tahmini Pax Başı Karlılık Analizi' kutusu net kârınızı ve marjınızı (%) anında hesaplar.",
                    stepOrder = 6
                ),
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "cover_image",
                    category = "MEDYA",
                    lang = "tr",
                    title = "Kapak Fotoğrafı & Galeri",
                    question = "Fotoğraflar nasıl yüklenir ve nerede görünür?",
                    answer = "Kapak resmi teklif formlarında ve web vitrininde ilk görünen ana fotoğraftır (Maks. 1MB). Ek Galeriye en fazla 10 adet kaliteli fotoğraf ekleyebilirsiniz.",
                    stepOrder = 7
                ),

                // 2. Yeni Otel Kaydı
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "hotel_name",
                    category = "TEMEL_BILGILER",
                    lang = "tr",
                    title = "Otel Adı ve Konumu",
                    question = "Otel Adı ve Şehir alanlarına ne yazmalıyım?",
                    answer = "Anlaşmalı tesisin tam ticari veya bilinen adını (Örn: Grand Cave Resort & Spa) ve bulunduğu şehri girin. Tur paketlerinde otel seçerken bu isimle arayacaksınız.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "star_rating",
                    category = "TEMEL_BILGILER",
                    lang = "tr",
                    title = "Yıldız ve Pansiyon Tipi",
                    question = "Yıldız derecesi ve Pansiyon Tipi ne işe yarar?",
                    answer = "Tesisin resmi yıldız derecesini (1-5) ve konaklama konseptini (Oda Kahvaltı, Yarım Pansiyon, Her Şey Dahil vb.) belirler. Tekliflerde müşteriye otomatik iletilir.",
                    stepOrder = 2
                ),
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "periods_rates",
                    category = "FIYATLANDIRMA",
                    lang = "tr",
                    title = "Otel Periyotları & Fiyatlandırma",
                    question = "Periyot nasıl açılır ve fiyat nasıl girilir?",
                    answer = "+ Yeni Periyot Ekle butonuna basarak sezonları (Örn: Yaz Sezonu, Kış Sezonu) tarih aralıklarıyla tanımlayın. Oda bazında maliyet ve satış fiyatı girerek dönemsel fiyatlandırma sağlayın.",
                    stepOrder = 3
                ),
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "cover_image",
                    category = "MEDYA",
                    lang = "tr",
                    title = "Görsel Seçimi & Açıklama",
                    question = "Otel görseli ve tanıtım metni nasıl eklenir?",
                    answer = "Yerel bilgisayarınızdan dosya seçebilir veya görsel linki (URL) yapıştırabilirsiniz. Tanıtım açıklamasına SPA, havuz, manzara gibi öne çıkan özellikleri yazın.",
                    stepOrder = 4
                ),

                // 3. Şirket Ayarları
                HelpGuide(
                    screenRoute = "CompanySettingsRoute",
                    fieldKey = "brand_logo",
                    category = "MARKA",
                    lang = "tr",
                    title = "Kurumsal Logo & Renk Teması",
                    question = "Marka sekmesindeki logo ve renk nerede kullanılır?",
                    answer = "Yüklediğiniz kurumsal logo ve seçtiğiniz ana kurumsal renk kodu (Hex), web sitenizin üst/alt menülerinde, teklif formlarında ve resmi voucher belgelerinde otomatik uygulanır.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "CompanySettingsRoute",
                    fieldKey = "bank_accounts",
                    category = "BANKA",
                    lang = "tr",
                    title = "Banka & PayPal Bilgileri",
                    question = "IBAN ve ödeme bilgileri ne işe yarar?",
                    answer = "Müşterilerinizin rezervasyon yaparken gördüğü Havale/EFT banka hesap numaraları ve online PayPal linkleri bu alandan yönetilir.",
                    stepOrder = 2
                ),
                HelpGuide(
                    screenRoute = "CompanySettingsRoute",
                    fieldKey = "seasons_commission",
                    category = "SEZON",
                    lang = "tr",
                    title = "Sezonlar & Komisyon Oranları",
                    question = "Sezonluk komisyon oranları nasıl çalışır?",
                    answer = "Yüksek ve düşük sezonlar için başlangıç-bitiş tarihleri ve komisyon oranları (%) belirleyin. Rezervasyon hareket tarihine göre sistem doğru komisyonu otomatik hesaplar.",
                    stepOrder = 3
                ),

                // 4. B2B Rezervasyon & Arama
                HelpGuide(
                    screenRoute = "B2BTourSearchDashboardRoute",
                    fieldKey = "search_tabs",
                    category = "ARAMA",
                    lang = "tr",
                    title = "5 Sekmeli Arama Sistemi",
                    question = "Hizmet türlerine göre nasıl arama yaparım?",
                    answer = "Paket Turlar, Oteller, Uçak Biletleri, Mavi Yolculuk ve Macera sekmelerinden dilediğinizi seçerek destinasyon ve tarih bazlı anında sorgulama yapabilirsiniz.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "BookingsRoute",
                    fieldKey = "voucher_action",
                    category = "YONETIM",
                    lang = "tr",
                    title = "Rezervasyon & Voucher Yönetimi",
                    question = "Rezervasyon durumları ve voucher nasıl yazdırılır?",
                    answer = "Rezervasyon satırındaki menüden 'Voucher PDF' butonuna tıklayarak acenta logolu resmi belgeyi indirebilir veya müşteriye e-posta ile gönderebilirsiniz.",
                    stepOrder = 1
                ),

                // 5. Dashboard
                HelpGuide(
                    screenRoute = "DashboardRoute",
                    fieldKey = "kpi_cards",
                    category = "OZET",
                    lang = "tr",
                    title = "Dashboard ve KPI Analizi",
                    question = "Ana ekrandaki göstergeler neleri ifade eder?",
                    answer = "Günlük/Aylık Satış Hacmi, Doluluk Oranları, Kesilen Faturalar ve Bekleyen Tahsilatlar gibi işletmenizin en kritik göstergelerini anlık olarak sunar.",
                    stepOrder = 1
                )
            )
            else -> listOf(
                HelpGuide(
                    screenRoute = "TourFormRoute",
                    fieldKey = "tour_name",
                    category = "BASIC_INFO",
                    lang = "en",
                    title = "How to write Tour Name?",
                    question = "What should I enter in the Tour Name field?",
                    answer = "The official name displayed to customers and the web catalog (e.g. 'Cappadocia Balloon & Culture Tour'). Keep it descriptive and appealing.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "HotelFormRoute",
                    fieldKey = "hotel_name",
                    category = "BASIC_INFO",
                    lang = "en",
                    title = "Hotel Name & Location",
                    question = "What should I enter for Hotel Name and City?",
                    answer = "Enter the full registered name of the hotel and its city. This will be used when pairing hotels with tour packages.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "CompanySettingsRoute",
                    fieldKey = "brand_logo",
                    category = "BRANDING",
                    lang = "en",
                    title = "Corporate Logo & Color Theme",
                    question = "Where are the logo and brand colors used?",
                    answer = "Your corporate logo and primary hex color are automatically applied across the public web header/footer, proposal forms, and booking vouchers.",
                    stepOrder = 1
                ),
                HelpGuide(
                    screenRoute = "DashboardRoute",
                    fieldKey = "kpi_cards",
                    category = "SUMMARY",
                    lang = "en",
                    title = "Dashboard KPI Metrics",
                    question = "What do the main dashboard cards indicate?",
                    answer = "They summarize daily/monthly booking volume, occupancy rate, generated invoices, and pending collections in real time.",
                    stepOrder = 1
                )
            )
        }
    }
}

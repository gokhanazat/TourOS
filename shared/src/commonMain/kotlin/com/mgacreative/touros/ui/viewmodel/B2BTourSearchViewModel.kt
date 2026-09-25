package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.BookingEntity
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.data.util.generateUuid
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingItem
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Passenger
import com.mgacreative.touros.domain.model.TourOperatorConfig
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FlightOption(
    val id: String,
    val outboundAirline: String,
    val outboundFlightNumber: String,
    val outboundDeparturePort: String,
    val outboundArrivalPort: String,
    val outboundDepartureTime: String = "02:05",
    val outboundArrivalTime: String = "06:45",
    val outboundDuration: String = "4s 40d",
    val inboundAirline: String,
    val inboundFlightNumber: String,
    val inboundDeparturePort: String,
    val inboundArrivalPort: String,
    val inboundDepartureTime: String = "18:40",
    val inboundArrivalTime: String = "23:05",
    val inboundDuration: String = "4s 25d",
    val baggageKg: Int = 20,
    val handBaggageKg: Int = 8,
    val priceDeltaRub: Double = 0.0,
    val operatorName: String = ""
)

data class ExtraService(
    val id: String,
    val name: String,
    val category: String, // "TRANSFER", "INSURANCE", "EXTRA"
    val unitPriceEur: Double,
    val isMandatory: Boolean = false,
    var isSelected: Boolean = false,
    val paxCount: Int = 1
)

data class PassengerInfo(
    val index: Int,
    var passengerType: String = "ADULT", // "ADULT", "CHILD", "INFANT"
    var childAge: Int? = null,
    var gender: String = "MALE", // "MALE", "FEMALE"
    var firstName: String = "",
    var lastName: String = "",
    var birthDate: String = "",
    var citizenship: String = "Россия",
    var documentType: String = "Загранпаспорт",
    var passportSeries: String = "",
    var passportNumber: String = "",
    var documentIssueDate: String = "",
    var documentExpiryDate: String = "",
    var documentIssuedBy: String = "",
    var birthCountry: String = "Россия",
    var isPayer: Boolean = false,
    var phone: String = "",
    var email: String = "",
    var address: String = "",
    var parentIndex: Int? = null,
    var isInfantSeatRequested: Boolean = false
)

@kotlinx.serialization.Serializable
data class OperatorFlightScheduleDto(
    val id: String = "",
    val airline_name: String = "",
    val flight_number: String = "",
    val departure_city: String = "",
    val arrival_city: String = "",
    val departure_time: String = "02:05:00",
    val arrival_time: String = "06:45:00",
    val duration_minutes: Int = 240,
    val is_charter: Boolean = true,
    val baggage_kg: Int = 20,
    val price_delta_rub: Double = 0.0,
    val operator_name: String = ""
)

@kotlinx.serialization.Serializable
data class SearchFilterMetadataDto(
    val departure_cities: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val regions: List<String> = emptyList(),
    val operators: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
    val min_price: Double = 0.0,
    val max_price: Double = 500000.0
)

sealed class B2BTourSearchUiState {
    data object Loading : B2BTourSearchUiState()
    data class Success(
        val allProducts: List<UnifiedProductEntity>,
        val filteredProducts: List<UnifiedProductEntity>,
        val totalFoundCount: Int
    ) : B2BTourSearchUiState()
    data class Error(val message: String) : B2BTourSearchUiState()
}

class B2BTourSearchViewModel(
    private val supabaseClient: SupabaseClient,
    private val bookingRepository: BookingRepository,
    private val hotelRepository: com.mgacreative.touros.domain.repository.HotelRepository? = null,
    private val tourRepository: com.mgacreative.touros.domain.repository.TourRepository? = null,
    private val getCurrentUserUseCase: com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase? = null
) : ViewModel() {

    companion object {
        private var globalCachedCombined: List<UnifiedProductEntity>? = null
        private var globalCachedMetadata: SearchFilterMetadataDto? = null

        fun clearGlobalCache() {
            globalCachedCombined = null
            globalCachedMetadata = null
        }

        fun calculateMultiplier(adultsCount: Int, childAgesList: List<Int>, isFlight: Boolean = false): Double {
            val adultWeight = adultsCount.coerceAtLeast(1) * 1.0
            val childWeight = childAgesList.sumOf { age ->
                when {
                    age <= 2 -> 0.10 // Bebek (%10 vergi/sigorta)
                    age <= 6 -> 0.50 // Küçük Çocuk (%50)
                    age <= 12 -> 0.70 // Büyük Çocuk (%70)
                    else -> 1.00 // 13-17 Genç/Yetişkin (%100)
                }
            }
            val totalWeight = adultWeight + childWeight
            return if (isFlight) totalWeight else (totalWeight / 2.0)
        }

        fun calculateDynamicPrice(basePrice: Double, adultsCount: Int, childAgesList: List<Int>, isFlight: Boolean = false): Double {
            return basePrice * calculateMultiplier(adultsCount, childAgesList, isFlight)
        }

        fun generateDynamicFlightSchedules(
            targetDate: String,
            depFilter: String = "",
            arrFilter: String = "",
            countryFilter: String = ""
        ): List<UnifiedProductEntity> {
            // ✈️ BİLGİ: Hardcoded uçuş verileri kaldırılmıştır. Tüm uçuşlar Yandex Cloud PostgreSQL (marketplace_products / flight_schedules) üzerinden %100 dinamik olarak çekilmektedir.
            return emptyList()
        }

        fun isDepartureMatchingText(targetDeparture: String, selectedDeparture: String): Boolean {
            val dep = selectedDeparture.trim()
            if (dep.isBlank() || dep.equals("Tüm Kalkış Şehirleri", ignoreCase = true) || dep.startsWith("Tüm", ignoreCase = true) || dep.contains("Tüm", ignoreCase = true) || dep.contains("Hepsi", ignoreCase = true) || dep.equals("ALL", ignoreCase = true) || dep.startsWith("Все", ignoreCase = true) || dep.contains("Все", ignoreCase = true)) {
                return true
            }
            if (targetDeparture.isBlank() || targetDeparture.contains("Yerel", ignoreCase = true)) {
                return true
            }
            val target = targetDeparture.lowercase()
            val query = dep.lowercase()

            if (target.contains(query) || query.contains(target)) return true

            // Eş anlamlı havaalanı ve şehir kodları eşleşmeleri
            val isTargetMoscow = target.contains("moskova") || target.contains("moscow") || target.contains("москва") || target.contains("svo") || target.contains("vko") || target.contains("dme") || target.contains("zia") || target.contains("mow")
            val isQueryMoscow = query.contains("moskova") || query.contains("moscow") || query.contains("москва") || query.contains("svo") || query.contains("vko") || query.contains("dme") || query.contains("zia") || query.contains("mow")
            if (isTargetMoscow && isQueryMoscow) return true

            val isTargetSpb = target.contains("petersburg") || target.contains("петербург") || target.contains("питер") || target.contains("led")
            val isQuerySpb = query.contains("petersburg") || query.contains("петербург") || query.contains("питер") || query.contains("led")
            if (isTargetSpb && isQuerySpb) return true

            val isTargetIst = target.contains("istanbul") || target.contains("ist") || target.contains("saw") || target.contains("стамбул")
            val isQueryIst = query.contains("istanbul") || query.contains("ist") || query.contains("saw") || query.contains("стамбул")
            if (isTargetIst && isQueryIst) return true

            val isTargetKzn = target.contains("kazan") || target.contains("казань") || target.contains("kzn")
            val isQueryKzn = query.contains("kazan") || query.contains("казань") || query.contains("kzn")
            if (isTargetKzn && isQueryKzn) return true

            val isTargetYek = target.contains("yekaterinburg") || target.contains("ekaterinburg") || target.contains("екатеринбург") || target.contains("svx")
            val isQueryYek = query.contains("yekaterinburg") || query.contains("ekaterinburg") || query.contains("екатеринбург") || query.contains("svx")
            if (isTargetYek && isQueryYek) return true

            val isTargetNov = target.contains("novosibirsk") || target.contains("новосибирск") || target.contains("ovb")
            val isQueryNov = query.contains("novosibirsk") || query.contains("новосибирск") || query.contains("ovb")
            if (isTargetNov && isQueryNov) return true

            val isTargetSam = target.contains("samara") || target.contains("самара") || target.contains("kuf")
            val isQuerySam = query.contains("samara") || query.contains("самара") || query.contains("kuf")
            if (isTargetSam && isQuerySam) return true

            val isTargetUfa = target.contains("ufa") || target.contains("уфа")
            val isQueryUfa = query.contains("ufa") || query.contains("уфа")
            if (isTargetUfa && isQueryUfa) return true

            val isTargetNiz = target.contains("nizhny") || target.contains("нижний") || target.contains("goj")
            val isQueryNiz = query.contains("nizhny") || query.contains("нижний") || query.contains("goj")
            if (isTargetNiz && isQueryNiz) return true

            val isTargetChe = target.contains("chelyabinsk") || target.contains("челябинск") || target.contains("cek")
            val isQueryChe = query.contains("chelyabinsk") || query.contains("челябинск") || query.contains("cek")
            if (isTargetChe && isQueryChe) return true

            val isTargetKra = target.contains("krasnoyarsk") || target.contains("красноярск") || target.contains("kja")
            val isQueryKra = query.contains("krasnoyarsk") || query.contains("красноярск") || query.contains("kja")
            if (isTargetKra && isQueryKra) return true

            val isTargetAntalya = target.contains("antalya") || target.contains("ayt") || target.contains("анталья")
            val isQueryAntalya = query.contains("antalya") || query.contains("ayt") || query.contains("анталья")
            if (isTargetAntalya && isQueryAntalya) return true

            val isTargetBod = target.contains("bodrum") || target.contains("бодрум") || target.contains("bjv")
            val isQueryBod = query.contains("bodrum") || query.contains("бодрум") || query.contains("bjv")
            if (isTargetBod && isQueryBod) return true

            val isTargetDal = target.contains("dalaman") || target.contains("даламан") || target.contains("dlm")
            val isQueryDal = query.contains("dalaman") || query.contains("даламан") || query.contains("dlm")
            if (isTargetDal && isQueryDal) return true

            val isTargetIzm = target.contains("izmir") || target.contains("измир") || target.contains("adb")
            val isQueryIzm = query.contains("izmir") || query.contains("измир") || query.contains("adb")
            if (isTargetIzm && isQueryIzm) return true

            val isTargetAnk = target.contains("ankara") || target.contains("анкара") || target.contains("esb")
            val isQueryAnk = query.contains("ankara") || query.contains("анкара") || query.contains("esb")
            if (isTargetAnk && isQueryAnk) return true

            val isTargetSoc = target.contains("sochi") || target.contains("сочи") || target.contains("aer")
            val isQuerySoc = query.contains("sochi") || query.contains("сочи") || query.contains("aer")
            if (isTargetSoc && isQuerySoc) return true

            return false
        }

        fun isDepartureMatching(item: UnifiedProductEntity, departure: String): Boolean {
            val depCityTarget = "${item.departureCity} ${item.flightNumber} ${item.tourName} ${item.hotelName}".trim()
            return isDepartureMatchingText(depCityTarget, departure)
        }

        fun hasAirport(destinationText: String): Boolean {
            val d = destinationText.lowercase().trim()
            if (d.isBlank() || d.startsWith("tüm") || d.startsWith("все") || d == "all") return false

            // Açık IATA kodları ve havalimanı anahtar kelimeleri
            val explicitAirportPatterns = listOf(
                "ayt", "gzp", "bjv", "dlm", "ist", "saw", "adb", "esb", "tzx", "ada",
                "svo", "vko", "dme", "zia", "led", "aer", "kzn", "svx", "ovb", "kuf", "ufa",
                "dxb", "dwc", "auh", "shj", "ssh", "hrg", "cai", "hkt", "bkk", "dmk", "utp",
                "havalimanı", "havalimani", "airport", "аэропорт"
            )
            if (explicitAirportPatterns.any { d.contains(it) }) return true

            // Havalimanı OLMAYAN, yalnızca otel / tatil beldesi olan yerler
            val nonAirportResorts = listOf(
                "kemer", "belek", "side", "manavgat", "beldibi", "göynük", "goynuk", "tekirova",
                "kiriş", "kiris", "çamyuva", "camyuva", "kundu", "boğazkent", "bogazkent", "kadriye",
                "çolaklı", "colakli", "kumköy", "kumkoy", "sorgun", "titreyengöl", "titreyengol",
                "okurcalar", "mahmutlar", "avsallar", "konaklı", "konakli", "ölüdeniz", "oludeniz",
                "göcek", "gocek", "marmaris", "fethiye", "yalıkavak", "yalikavak", "turgutreis",
                "gümbet", "gumbet", "torba", "bitez", "çeşme", "cesme", "alaçatı", "alacati",
                "makadi", "el gouna", "nabq", "naama", "patong", "karon", "kata", "chaweng"
            )
            if (nonAirportResorts.any { d.contains(it) }) {
                return false
            }

            // Doğrudan havalimanı barındıran şehir ve merkezler
            val airportCities = listOf(
                "antalya", "alanya", "bodrum", "dalaman", "istanbul", "izmir", "ankara", "trabzon", "adana",
                "moskova", "moscow", "москва", "petersburg", "петербург", "soçi", "sochi", "сочи", "kazan", "казань",
                "yekaterinburg", "екатеринбург", "novosibirsk", "новосибирск", "samara", "самара", "ufa", "уфа",
                "dubai", "дубай", "abu dhabi", "абу-даби", "şarm", "sharm", "шарм", "hurgada", "hurghada", "хургада",
                "phuket", "пхукет", "bangkok", "бангкок"
            )
            return airportCities.any { d.contains(it) }
        }

        fun isDestinationMatchingText(targetText: String, selectedDest: String): Boolean {
            val dest = selectedDest.trim()
            if (dest.isBlank() || dest.equals("Tüm Destinasyonlar", ignoreCase = true) || dest.equals("Tüm Varış Noktaları", ignoreCase = true) || dest.equals("Tüm", ignoreCase = true) || dest.equals("ALL", ignoreCase = true) || dest.equals("Все направления", ignoreCase = true) || dest.startsWith("Tüm", ignoreCase = true) || dest.startsWith("Все", ignoreCase = true)) {
                return true
            }

            val targetLower = targetText.lowercase()
            val destLower = dest.lowercase()

            if (targetLower.contains(destLower)) {
                return true
            }

            val tokens = dest.split('/', ',', '(', ')', '—', '-')
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 && !it.startsWith("tüm") && !it.startsWith("все") }

            return tokens.any { targetLower.contains(it) }
        }

        fun isDestinationMatching(item: UnifiedProductEntity, selectedDest: String): Boolean {
            val dest = selectedDest.trim()
            if (dest.isBlank() || dest.equals("Tüm Destinasyonlar", ignoreCase = true) || dest.equals("Tüm Varış Noktaları", ignoreCase = true) || dest.equals("Tüm", ignoreCase = true) || dest.equals("ALL", ignoreCase = true) || dest.equals("Все направления", ignoreCase = true) || dest.startsWith("Tüm", ignoreCase = true) || dest.startsWith("Все", ignoreCase = true)) {
                return true
            }

            val destLower = dest.lowercase()
            // Sadece Coğrafi Alanlar (Ülke, Bölge, Alt Bölge) ve Uçuş Kodu / Havayolu
            val geoText = "${item.country} ${item.countryName} ${item.countryCode} ${item.region} ${item.subRegion} ${item.flightNumber} ${item.airlineName}".lowercase()
            val itemHotelLower = item.safeHotelName.lowercase()

            // 1. Türkiye Destinasyonları (Antalya, Kemer, Belek, Side, Alanya, Bodrum, Marmaris, AYT, BJV, DLM, GZP, ADB, IST)
            val isTargetTurkey = destLower.contains("türkiye") || destLower.contains("turkey") || destLower.contains("турция") ||
                    destLower.contains("antalya") || destLower.contains("belek") || destLower.contains("kemer") || destLower.contains("lara") ||
                    destLower.contains("side") || destLower.contains("alanya") || destLower.contains("bodrum") || destLower.contains("marmaris") ||
                    destLower.contains("fethiye") || destLower.contains("çeşme") || destLower.contains("cesme") || destLower.contains("istanbul") ||
                    destLower.contains("ayt") || destLower.contains("bjv") || destLower.contains("dlm") || destLower.contains("gzp") || destLower.contains("adb") || destLower.contains("ist") || destLower.contains("saw")

            if (isTargetTurkey) {
                val isFlightItem = item.safeProductType.uppercase() == "FLIGHT" || item.flightNumber.isNotBlank()
                // Kesinlikle Türkiye kontrolü (Uçuş seferleri IATA kodları ve Türkiye varış noktalarıyla doğrudan eşleşir)
                if (!isFlightItem && !isCountryMatching(item, "TR")) return false

                if (destLower.contains("belek") || destLower.contains("белек")) return geoText.contains("belek") || geoText.contains("белек") || geoText.contains("boğazkent") || geoText.contains("kadriye") || geoText.contains("ayt")
                if (destLower.contains("kemer") || destLower.contains("кемер")) return geoText.contains("kemer") || geoText.contains("кемер") || geoText.contains("beldibi") || geoText.contains("göynük") || geoText.contains("tekirova") || geoText.contains("kiriş") || geoText.contains("çamyuva") || geoText.contains("ayt")
                if (destLower.contains("lara") || destLower.contains("лара")) return geoText.contains("lara") || geoText.contains("лара") || geoText.contains("kundu") || geoText.contains("ayt")
                if (destLower.contains("side") || destLower.contains("сиде") || destLower.contains("manavgat")) return geoText.contains("side") || geoText.contains("сиде") || geoText.contains("manavgat") || geoText.contains("çolaklı") || geoText.contains("kumköy") || geoText.contains("sorgun") || geoText.contains("titreyengöl") || geoText.contains("ayt")
                if (destLower.contains("alanya") || destLower.contains("аланья") || destLower.contains("gzp")) return geoText.contains("alanya") || geoText.contains("аланья") || geoText.contains("okurcalar") || geoText.contains("mahmutlar") || geoText.contains("avsallar") || geoText.contains("konaklı") || geoText.contains("gzp") || geoText.contains("ayt")
                if (destLower.contains("bodrum") || destLower.contains("бодрум") || destLower.contains("bjv")) return geoText.contains("bodrum") || geoText.contains("бодрум") || geoText.contains("yalıkavak") || geoText.contains("torba") || geoText.contains("gümbet") || geoText.contains("bjv")
                if (destLower.contains("marmaris") || destLower.contains("мармарис") || destLower.contains("fethiye") || destLower.contains("фетхие") || destLower.contains("dlm") || destLower.contains("dalaman")) return geoText.contains("marmaris") || geoText.contains("мармарис") || geoText.contains("fethiye") || geoText.contains("фетхие") || geoText.contains("dlm") || geoText.contains("dalaman") || geoText.contains("ölüdeniz") || geoText.contains("göcek")
                if (destLower.contains("çeşme") || destLower.contains("cesme") || destLower.contains("чешме") || destLower.contains("adb") || destLower.contains("izmir") || destLower.contains("измир")) return geoText.contains("çeşme") || geoText.contains("cesme") || geoText.contains("alaçatı") || geoText.contains("adb") || geoText.contains("izmir") || geoText.contains("измир")
                if (destLower.contains("antalya") || destLower.contains("анталья") || destLower.contains("ayt")) {
                    return geoText.contains("antalya") || geoText.contains("анталья") || geoText.contains("ayt") || 
                           geoText.contains("belek") || geoText.contains("белек") || 
                           geoText.contains("kemer") || geoText.contains("кемер") || 
                           geoText.contains("lara") || geoText.contains("лара") || 
                           geoText.contains("kundu") || geoText.contains("кунду") || 
                           geoText.contains("side") || geoText.contains("сиде") || 
                           geoText.contains("alanya") || geoText.contains("аланья") ||
                           geoText.contains("manavgat") || geoText.contains("манавгат") ||
                           geoText.contains("bogazkent") || geoText.contains("богазкент")
                }
                return true
            }

            // 2. Mısır Destinasyonları (Şarm, Hurgada, SSH, HRG vb.)
            val isTargetEgypt = destLower.contains("mısır") || destLower.contains("egypt") || destLower.contains("египет") ||
                    destLower.contains("şarm") || destLower.contains("sharm") || destLower.contains("шарм") || destLower.contains("ssh") ||
                    destLower.contains("hurgada") || destLower.contains("hurghada") || destLower.contains("хургада") || destLower.contains("hrg")
            if (isTargetEgypt) {
                if (!isCountryMatching(item, "EG")) return false
                if (destLower.contains("şarm") || destLower.contains("sharm") || destLower.contains("шарм") || destLower.contains("ssh")) return geoText.contains("şarm") || geoText.contains("sharm") || geoText.contains("шарм") || geoText.contains("ssh") || geoText.contains("nabq") || geoText.contains("naama")
                if (destLower.contains("hurgada") || destLower.contains("hurghada") || destLower.contains("хургада") || destLower.contains("hrg")) return geoText.contains("hurgada") || geoText.contains("hurghada") || geoText.contains("хургада") || geoText.contains("hrg") || geoText.contains("el gouna") || geoText.contains("makadi") || geoText.contains("sahl hasheesh")
                return true
            }

            // 3. Tayland Destinasyonları (Phuket, Pattaya, Bangkok, Samui, HKT, BKK, UTP, USM vb.)
            val isTargetThailand = destLower.contains("tayland") || destLower.contains("thailand") || destLower.contains("таиланд") || destLower.contains("тайланд") ||
                    destLower.contains("phuket") || destLower.contains("пхукет") || destLower.contains("hkt") ||
                    destLower.contains("pattaya") || destLower.contains("паттайя") || destLower.contains("utp") ||
                    destLower.contains("bangkok") || destLower.contains("бангкок") || destLower.contains("bkk") ||
                    destLower.contains("samui") || destLower.contains("самуи") || destLower.contains("usm")
            if (isTargetThailand) {
                if (!isCountryMatching(item, "TH")) return false
                if (destLower.contains("phuket") || destLower.contains("пхукет") || destLower.contains("hkt")) return geoText.contains("phuket") || geoText.contains("пхукет") || geoText.contains("hkt") || geoText.contains("patong") || geoText.contains("патонг") || geoText.contains("karon") || geoText.contains("карон") || geoText.contains("kata") || geoText.contains("ката")
                if (destLower.contains("pattaya") || destLower.contains("паттайя") || destLower.contains("utp")) return geoText.contains("pattaya") || geoText.contains("паттайя") || geoText.contains("utp") || geoText.contains("jomtien")
                if (destLower.contains("bangkok") || destLower.contains("бангкок") || destLower.contains("bkk")) return geoText.contains("bangkok") || geoText.contains("бангкок") || geoText.contains("bkk")
                if (destLower.contains("samui") || destLower.contains("самуи") || destLower.contains("usm")) return geoText.contains("samui") || geoText.contains("самуи") || geoText.contains("usm") || geoText.contains("chaweng")
                return true
            }

            // 4. BAE / Dubai Destinasyonları (DXB, AUH)
            val isTargetUAE = destLower.contains("bae") || destLower.contains("uae") || destLower.contains("оаэ") ||
                    destLower.contains("dubai") || destLower.contains("дубай") || destLower.contains("dxb") ||
                    destLower.contains("abu dhabi") || destLower.contains("абу-даби") || destLower.contains("auh")
            if (isTargetUAE) {
                if (!isCountryMatching(item, "AE")) return false
                if (destLower.contains("dubai") || destLower.contains("дубай") || destLower.contains("dxb")) return geoText.contains("dubai") || geoText.contains("дубай") || geoText.contains("dxb") || geoText.contains("marina") || geoText.contains("jumeirah") || geoText.contains("downtown")
                if (destLower.contains("abu dhabi") || destLower.contains("абу-даби") || destLower.contains("auh")) return geoText.contains("abu dhabi") || geoText.contains("абу-даби") || geoText.contains("auh") || geoText.contains("yas island")
                return true
            }

            // 5. Vietnam Destinasyonları (DAD, PQC, CXR)
            val isTargetVietnam = destLower.contains("vietnam") || destLower.contains("вьетнам") ||
                    destLower.contains("da nang") || destLower.contains("dad") ||
                    destLower.contains("phu quoc") || destLower.contains("pqc") ||
                    destLower.contains("nha trang") || destLower.contains("cxr") || destLower.contains("hoi an")
            if (isTargetVietnam) {
                if (!isCountryMatching(item, "VN")) return false
                if (destLower.contains("da nang") || destLower.contains("дананг") || destLower.contains("dad")) return geoText.contains("da nang") || geoText.contains("danang") || geoText.contains("дананг") || geoText.contains("dad")
                if (destLower.contains("phu quoc") || destLower.contains("фукуок") || destLower.contains("pqc")) return geoText.contains("phu quoc") || geoText.contains("phuquoc") || geoText.contains("фукуок") || geoText.contains("pqc")
                if (destLower.contains("nha trang") || destLower.contains("нячанг") || destLower.contains("cxr")) return geoText.contains("nha trang") || geoText.contains("nhatrang") || geoText.contains("нячанг") || geoText.contains("cxr")
                return true
            }

            // 6. Rusya Destinasyonları (AER, SVO, VKO, DME, LED, KZN)
            val isTargetRussia = destLower.contains("rusya") || destLower.contains("russia") || destLower.contains("россия") ||
                    destLower.contains("sochi") || destLower.contains("сочи") || destLower.contains("aer") ||
                    destLower.contains("moskova") || destLower.contains("москва") ||
                    destLower.contains("petersburg") || destLower.contains("петербург") || destLower.contains("kazan") || destLower.contains("казань")
            if (isTargetRussia) {
                if (!isCountryMatching(item, "RU")) return false
                if (destLower.contains("sochi") || destLower.contains("сочи") || destLower.contains("aer")) return geoText.contains("sochi") || geoText.contains("сочи") || geoText.contains("aer") || geoText.contains("krasnaya polyana") || geoText.contains("красная поляна")
                if (destLower.contains("moskova") || destLower.contains("москва")) return geoText.contains("moskova") || geoText.contains("moscow") || geoText.contains("москва")
                if (destLower.contains("petersburg") || destLower.contains("петербург")) return geoText.contains("petersburg") || geoText.contains("петербург")
                if (destLower.contains("kazan") || destLower.contains("казань")) return geoText.contains("kazan") || geoText.contains("казань")
                return true
            }

            // 7. Genel Fallback (Coğrafi ve Otel Adı)
            val combinedText = "$geoText $itemHotelLower"
            if (combinedText.contains(destLower)) return true

            val tokens = dest.split('/', ',', '(', ')', '—', '-')
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 && !it.startsWith("tüm") && !it.startsWith("все") }

            return tokens.any { combinedText.contains(it) }
        }

        fun isCountryMatching(item: UnifiedProductEntity, countryCodeOrName: String): Boolean {
            if (countryCodeOrName.isBlank() || countryCodeOrName == "ALL" || countryCodeOrName.equals("Tüm", ignoreCase = true)) return true
            val code = countryCodeOrName.uppercase().trim()
            val itemCode = item.countryCode.uppercase().trim()
            if (itemCode.isNotBlank() && (itemCode == code || 
                (code == "TR" && itemCode == "TUR") || 
                (code == "EG" && itemCode == "EGY") || 
                (code == "TH" && itemCode == "THA") || 
                (code == "VN" && itemCode == "VNM") || 
                (code == "AE" && itemCode == "ARE") || 
                (code == "RU" && itemCode == "RUS") ||
                (code == "MV" && itemCode == "MDV") ||
                (code == "CY" && itemCode == "CYP") ||
                (code == "GE" && itemCode == "GEO") ||
                (code == "SC" && itemCode == "SYC") ||
                (code == "LK" && itemCode == "LKA") ||
                (code == "MU" && itemCode == "MUS") ||
                (code == "ID" && itemCode == "IDN") ||
                (code == "TZ" && itemCode == "TZA") ||
                (code == "ME" && itemCode == "MNE") ||
                (code == "GR" && itemCode == "GRC") ||
                (code == "CN" && itemCode == "CHN") ||
                (code == "AB" && itemCode == "ABH")
            )) {
                return true
            }

            val geoText = "${item.country} ${item.countryName} ${item.region} ${item.subRegion}".lowercase().trim()

            return when (code) {
                "TR", "TÜRKIYE", "TURKEY", "ТУРЦИЯ" -> {
                    val isForeign = geoText.contains("хайнань") || geoText.contains("hainan") || geoText.contains("sanya") || geoText.contains("санья") ||
                            geoText.contains("гагра") || geoText.contains("пицунда") || geoText.contains("гудаут") || geoText.contains("сухум") || geoText.contains("abhazya") || geoText.contains("абхазия") ||
                            geoText.contains("нячанг") || geoText.contains("фукуок") || geoText.contains("дананг") || geoText.contains("фантьет") || geoText.contains("ханой") || geoText.contains("vietnam") || geoText.contains("вьетнам") ||
                            geoText.contains("дубай") || geoText.contains("dubai") || geoText.contains("шарджа") || geoText.contains("абу-даби") || geoText.contains("фуджейра") || geoText.contains("оаэ") || geoText.contains("bae") ||
                            geoText.contains("пхукет") || geoText.contains("паттайя") || geoText.contains("бангкок") || geoText.contains("краби") || geoText.contains("самуи") || geoText.contains("tayland") || geoText.contains("thailand") ||
                            geoText.contains("шарм") || geoText.contains("хургада") || geoText.contains("марса алам") || geoText.contains("египет") || geoText.contains("egypt") || geoText.contains("mısır") ||
                            geoText.contains("мальдив") || geoText.contains("maldiv") || geoText.contains("maldives") ||
                            geoText.contains("сочи") || geoText.contains("петербург") || geoText.contains("москва") || geoText.contains("калининград") || geoText.contains("россия") || geoText.contains("rusya") ||
                            geoText.contains("бали") || geoText.contains("bali") || geoText.contains("занзибар") || geoText.contains("zanzibar") || geoText.contains("шри-ланка") || geoText.contains("sri lanka") ||
                            geoText.contains("кипр") || geoText.contains("cyprus") || geoText.contains("грузия") || geoText.contains("georgia") || geoText.contains("батуми") || geoText.contains("черногория") || geoText.contains("montenegro") ||
                            geoText.contains("маврикий") || geoText.contains("mauritius") || geoText.contains("сейшел") || geoText.contains("seychelles")
                    if (isForeign) return false
                    itemCode == "TR" || geoText.contains("türkiye") || geoText.contains("turkey") || geoText.contains("турция") ||
                            geoText.contains("antalya") || geoText.contains("ayt") || geoText.contains("belek") || geoText.contains("kemer") || geoText.contains("lara") ||
                            geoText.contains("alanya") || geoText.contains("gzp") || geoText.contains("side") || geoText.contains("bodrum") || geoText.contains("bjv") || geoText.contains("marmaris") ||
                            geoText.contains("fethiye") || geoText.contains("dlm") || geoText.contains("çeşme") || geoText.contains("adb") || geoText.contains("izmir") ||
                            geoText.contains("белек") || geoText.contains("кемер") || geoText.contains("анталья") || geoText.contains("аланья") || geoText.contains("сиде") ||
                            geoText.contains("бодрум") || geoText.contains("мармарис") || geoText.contains("фетхие") || geoText.contains("ist") || geoText.contains("saw") || (geoText.contains("istanbul") && !geoText.contains("sharm") && !geoText.contains("dubai"))
                }
                "EG", "MISIR", "EGYPT", "ЕГИПЕТ" -> itemCode == "EG" || geoText.contains("mısır") || geoText.contains("egypt") || geoText.contains("египет") ||
                        geoText.contains("şarm") || geoText.contains("sharm") || geoText.contains("hurgada") || geoText.contains("hurghada") ||
                        geoText.contains("el gouna") || geoText.contains("makadi") || geoText.contains("шарм") || geoText.contains("хургада") ||
                        geoText.contains("эль гуна") || geoText.contains("макади") || geoText.contains("марса алам") || geoText.contains("дахаб")
                "TH", "TAYLAND", "THAILAND", "ТАИЛАНД", "ТАЙЛАНД" -> itemCode == "TH" || geoText.contains("tayland") || geoText.contains("thailand") || geoText.contains("таиланд") || geoText.contains("тайланд") ||
                        geoText.contains("phuket") || geoText.contains("pattaya") || geoText.contains("bangkok") || geoText.contains("samui") ||
                        geoText.contains("krabi") || geoText.contains("пхукет") || geoText.contains("паттайя") || geoText.contains("бангкок") ||
                        geoText.contains("самуи") || geoText.contains("краби") || geoText.contains("као лак")
                "VN", "VIETNAM", "ВЬЕТНАМ" -> itemCode == "VN" || geoText.contains("vietnam") || geoText.contains("вьетнам") ||
                        geoText.contains("da nang") || geoText.contains("phu quoc") || geoText.contains("nha trang") || geoText.contains("hoi an") ||
                        geoText.contains("дананг") || geoText.contains("фукуок") || geoText.contains("нячанг") || geoText.contains("хойан") ||
                        geoText.contains("фантьет") || geoText.contains("ханой") || geoText.contains("вунг тау") || geoText.contains("камрань")
                "AE", "BAE", "DUBAI", "UAE", "ОАЭ" -> itemCode == "AE" || geoText.contains("bae") || geoText.contains("dubai") || geoText.contains("uae") || geoText.contains("оаэ") ||
                        geoText.contains("дубай") || geoText.contains("abu dhabi") || geoText.contains("абу-даби") || geoText.contains("sharjah") ||
                        geoText.contains("шарджа") || geoText.contains("jumeirah") || geoText.contains("фуджейра") || geoText.contains("аджман") || geoText.contains("умм аль кувейн")
                "RU", "RUSYA", "RUSSIA", "РОССИЯ" -> itemCode == "RU" || geoText.contains("rusya") || geoText.contains("russia") || geoText.contains("россия") ||
                        geoText.contains("moskova") || geoText.contains("moscow") || geoText.contains("москва") ||
                        geoText.contains("sochi") || geoText.contains("сочи") || geoText.contains("st. petersburg") || geoText.contains("петербург") ||
                        geoText.contains("kazan") || geoText.contains("казань") || geoText.contains("калининград")
                "MV", "MALDIVLER", "MALDIVES", "МАЛЬДИВЫ" -> itemCode == "MV" || geoText.contains("maldiv") || geoText.contains("maldives") || geoText.contains("мальдив") ||
                        geoText.contains("male") || geoText.contains("мале") || geoText.contains("atoll") || geoText.contains("атолл")
                "CY", "KIBRIS", "CYPRUS", "КИПР" -> itemCode == "CY" || geoText.contains("kıbrıs") || geoText.contains("cyprus") || geoText.contains("кипр") ||
                        geoText.contains("girne") || geoText.contains("kyrenia") || geoText.contains("lefkoşa") || geoText.contains("nicosia") || geoText.contains("magusa") || geoText.contains("bafra") || geoText.contains("гирне") || geoText.contains("никосия") ||
                        geoText.contains("айя напа") || geoText.contains("ларнака") || geoText.contains("пафос") || geoText.contains("протарас") || geoText.contains("лимассол")
                "GE", "GÜRCISTAN", "GURCISTAN", "GEORGIA", "ГРУЗИЯ" -> itemCode == "GE" || geoText.contains("gürcistan") || geoText.contains("gurcistan") || geoText.contains("georgia") || geoText.contains("грузия") ||
                        geoText.contains("batum") || geoText.contains("batumi") || geoText.contains("батуми") || geoText.contains("tbilisi") || geoText.contains("tiflis") || geoText.contains("тбилиси") || geoText.contains("гудаури") || geoText.contains("бакуриани")
                "SC", "SEYSELLER", "ŞEYŞELLER", "SEYCHELLES", "СЕЙШЕЛЫ" -> itemCode == "SC" || geoText.contains("seyşel") || geoText.contains("seysel") || geoText.contains("seychelles") || geoText.contains("сейшел") ||
                        geoText.contains("mahe") || geoText.contains("маэ") || geoText.contains("praslin") || geoText.contains("праслин") || geoText.contains("ла диг") || geoText.contains("la digue")
                "LK", "SRI LANKA", "ŞRİ LANKA", "ШРИ-ЛАНКА" -> itemCode == "LK" || geoText.contains("sri lanka") || geoText.contains("srilanka") || geoText.contains("шри-ланка") || geoText.contains("шри ланка") ||
                        geoText.contains("colombo") || geoText.contains("коломбо") || geoText.contains("bentota") || geoText.contains("бентота") || geoText.contains("канди") || geoText.contains("галле")
                "MU", "MAURITIUS", "MAVRİKIY", "МАВРИКИЙ" -> itemCode == "MU" || geoText.contains("mauritius") || geoText.contains("маврикий") ||
                        geoText.contains("port louis") || geoText.contains("порт-луи") || geoText.contains("grand baie") || geoText.contains("flic")
                "ID", "ENDONEZYA", "INDONESIA", "ИНДОНЕЗИЯ" -> itemCode == "ID" || geoText.contains("endonezya") || geoText.contains("indonesia") || geoText.contains("индонезия") ||
                        geoText.contains("bali") || geoText.contains("бали") || geoText.contains("ubud") || geoText.contains("убуд") || geoText.contains("kuta") || geoText.contains("кута") || geoText.contains("seminyak") || geoText.contains("семиньяк")
                "TZ", "ZANZIBAR", "TANZANIA", "ТАНЗАНИЯ" -> itemCode == "TZ" || geoText.contains("zanzibar") || geoText.contains("занзибар") || geoText.contains("tanzania") || geoText.contains("танзания") ||
                        geoText.contains("nungwi") || geoText.contains("нунгви") || geoText.contains("kendwa") || geoText.contains("кендва")
                "ME", "KARADAG", "KARADAĞ", "MONTENEGRO", "ЧЕРНОГОРИЯ" -> itemCode == "ME" || geoText.contains("karadağ") || geoText.contains("karadag") || geoText.contains("montenegro") || geoText.contains("черногория") ||
                        geoText.contains("budva") || geoText.contains("будва") || geoText.contains("kotor") || geoText.contains("котор") || geoText.contains("tivat") || geoText.contains("тиват") || geoText.contains("herceg novi") || geoText.contains("герцег-нови")
                "GR", "YUNANISTAN", "GREECE", "ГРЕЦИЯ" -> itemCode == "GR" || geoText.contains("yunanistan") || geoText.contains("greece") || geoText.contains("греция") ||
                        geoText.contains("rodos") || geoText.contains("rhodes") || geoText.contains("родос") || geoText.contains("girit") || geoText.contains("crete") || geoText.contains("крит") || geoText.contains("афины") || geoText.contains("халкидики")
                "CN", "ÇIN", "CIN", "CHINA", "КИТАЙ" -> itemCode == "CN" || geoText.contains("çin") || geoText.contains("cin") || geoText.contains("china") || geoText.contains("китай") ||
                        geoText.contains("hainan") || geoText.contains("хайнань") || geoText.contains("sanya") || geoText.contains("санья") || geoText.contains("пекин") || geoText.contains("beijing") || geoText.contains("гуанчжоу") || geoText.contains("гонконг") || geoText.contains("дадунхай")
                "AB", "ABHAZYA", "ABKHAZIA", "АБХАЗИЯ" -> itemCode == "AB" || geoText.contains("abhazya") || geoText.contains("abkhazia") || geoText.contains("абхазия") ||
                        geoText.contains("gagra") || geoText.contains("гагра") || geoText.contains("pitsunda") || geoText.contains("пицунда") || geoText.contains("гудаут") || geoText.contains("gudaut") || geoText.contains("сухум") || geoText.contains("новый афон")
                else -> geoText.contains(countryCodeOrName.lowercase()) || code.contains(itemCode)
            }
        }

        fun isSubRegionMatching(item: UnifiedProductEntity, subRegion: String?): Boolean {
            if (subRegion.isNullOrBlank() || subRegion == "Tümü" || subRegion == "ALL") return true
            val s = subRegion.lowercase().trim()
            val geoText = "${item.country} ${item.countryName} ${item.region} ${item.subRegion}".lowercase()

            val synonyms = when (s) {
                "belek" -> listOf("belek", "белек")
                "kemer" -> listOf("kemer", "кемер")
                "antalya" -> listOf("antalya", "анталья", "ayt")
                "lara" -> listOf("lara", "лара", "kundu", "кунду")
                "alanya" -> listOf("alanya", "аланья")
                "side" -> listOf("side", "сиде")
                "bodrum" -> listOf("bodrum", "бодрум")
                "marmaris" -> listOf("marmaris", "мармарис")
                "fethiye" -> listOf("fethiye", "фетхие")
                "çeşme", "cesme" -> listOf("çeşme", "cesme", "чешме")
                "şarm el-şeyh", "sharm el-sheikh", "шарм-эль-шейх" -> listOf("şarm", "sharm", "шарм")
                "hurgada", "hurghada", "хургада" -> listOf("hurgada", "hurghada", "хургада")
                "el gouna", "эль гуна" -> listOf("el gouna", "gouna", "эль гуна", "эль-гуна")
                "makadi bay", "макади" -> listOf("makadi", "макади")
                "phuket", "пхукет" -> listOf("phuket", "пхукет", "patong", "патонг", "karon", "карон", "kata", "ката")
                "pattaya", "паттайя" -> listOf("pattaya", "паттайя")
                "bangkok", "бангкок" -> listOf("bangkok", "бангкок")
                "koh samui", "samui", "самуи" -> listOf("samui", "самуи")
                "krabi", "краби" -> listOf("krabi", "краби")
                "nha trang", "нячанг" -> listOf("nha trang", "nhatrang", "нячанг")
                "phu quoc", "фукуок" -> listOf("phu quoc", "фукуок")
                "da nang", "дананг" -> listOf("da nang", "дананг")
                "hoi an", "хойан" -> listOf("hoi an", "hoian", "хойан")
                "hainan", "хайнань" -> listOf("hainan", "хайнань")
                "sanya", "санья" -> listOf("sanya", "санья")
                "pekin", "пекин", "beijing" -> listOf("pekin", "пекин", "beijing")
                "gagra", "гагра", "гагрский район" -> listOf("gagra", "гагра", "гагр")
                "pitsunda", "пицунда" -> listOf("pitsunda", "пицунда")
                "gudauta", "гудаута" -> listOf("gudauta", "гудаут")
                "sohum", "сухум", "сухумский район" -> listOf("sohum", "сухум", "sukhum")
                "айя напа", "ayia napa" -> listOf("айя напа", "ayia napa", "напа")
                "ларнака", "larnaca", "larnaka" -> listOf("ларнака", "larnaca", "larnaka")
                "пафос", "paphos", "pafos" -> listOf("пафос", "paphos", "pafos")
                "маэ", "маэ о.", "mahe" -> listOf("mahe", "маэ")
                "праслин", "праслин о.", "praslin" -> listOf("praslin", "праслин")
                "батуми", "batumi", "batum" -> listOf("batumi", "batum", "батум")
                "тбилиси", "tbilisi", "tiflis" -> listOf("tbilisi", "tiflis", "тбилис")
                "коломбо", "colombo" -> listOf("colombo", "коломбо")
                "бентота", "bentota" -> listOf("bentota", "бентота")
                "порт-луи", "port louis" -> listOf("port louis", "порт-луи")
                "бали", "bali" -> listOf("bali", "бали")
                "убуд", "ubud" -> listOf("ubud", "убуд")
                "занзибар", "zanzibar" -> listOf("zanzibar", "занзибар")
                "нунгви", "nungwi" -> listOf("nungwi", "нунгви")
                "будва", "budva" -> listOf("budva", "будва")
                "котор", "kotor" -> listOf("kotor", "котор")
                "тиват", "tivat" -> listOf("tivat", "тиват")
                "родос", "rhodes", "rodos" -> listOf("rhodes", "rodos", "родос")
                "крит", "crete", "girit" -> listOf("crete", "girit", "крит")
                "dubai marina", "дубай" -> listOf("dubai", "marina", "дубай")
                "palm jumeirah" -> listOf("palm", "jumeirah", "пальм")
                "downtown" -> listOf("downtown", "даунтаун")
                "abu dhabi", "абу-даби" -> listOf("abu dhabi", "абу-даби")
                "шарджа", "sharjah" -> listOf("sharjah", "шарджа")
                "moskova", "москва" -> listOf("moskova", "moscow", "москва")
                "st. petersburg", "санкт-петербург" -> listOf("petersburg", "петербург", "питер")
                "sochi", "сочи" -> listOf("sochi", "сочи")
                "kazan", "казань" -> listOf("kazan", "казань")
                else -> listOf(s)
            }
            return synonyms.any { geoText.contains(it) || it.contains(item.region.lowercase()) }
        }
    }

    private val _uiState = MutableStateFlow<B2BTourSearchUiState>(B2BTourSearchUiState.Loading)
    val uiState: StateFlow<B2BTourSearchUiState> = _uiState.asStateFlow()
    val searchFilterMetadata = MutableStateFlow<SearchFilterMetadataDto?>(null)

    // Arama Parametreleri State
    var selectedCategory = MutableStateFlow("TOURS") // "TOURS", "HOTELS", "FLIGHTS", "LOCAL_TOURS", "LOCAL_HOTELS", "ALL"
    var departureCity = MutableStateFlow("")
    var destinationCountry = MutableStateFlow("")
    var selectedRegion = MutableStateFlow("")
    var selectedStartDate = MutableStateFlow("")
    var selectedEndDate = MutableStateFlow("")
    var nights = MutableStateFlow(7)
    var adults = MutableStateFlow(2)
    var childs = MutableStateFlow(0)
    var childrenAges = MutableStateFlow<List<Int>>(emptyList())
    var selectedStars = MutableStateFlow(emptySet<Int>())
    var selectedMealTypes = MutableStateFlow(emptySet<String>())
    var selectedRoomTypes = MutableStateFlow(emptySet<String>())
    var isInstantConfirmationOnly = MutableStateFlow(false)
    var isPromoOnly = MutableStateFlow(false)
    var searchQuery = MutableStateFlow("")

    // Seçili Tur / Rezervasyon Akışı State (Kullanıcı seçene kadar null)
    val selectedProduct = MutableStateFlow<UnifiedProductEntity?>(null)
    val availableFlightOptions = MutableStateFlow<List<FlightOption>>(emptyList())
    val selectedFlightOption = MutableStateFlow<FlightOption?>(null)
    val extraServices = MutableStateFlow<List<ExtraService>>(emptyList())
    val passengers = MutableStateFlow<List<PassengerInfo>>(emptyList())

    val isSavingBooking = MutableStateFlow(false)
    val bookingErrorMessage = MutableStateFlow<String?>(null)
    val createdPnrCode = MutableStateFlow("")

    // ─── Acente Sorgu Kotası State (Quota Guard - Günlük & Aylık) ───────────
    val dailyQuota = MutableStateFlow(250)
    val todayQueries = MutableStateFlow(0)
    val monthlyQuota = MutableStateFlow(5000)
    val currentQueries = MutableStateFlow(0)
    val isQuotaExceeded = MutableStateFlow(false)
    val quotaErrorMessage = MutableStateFlow<String?>(null)
    val quotaExceededType = MutableStateFlow<String?>(null) // "DAILY" or "MONTHLY"

    init {
        performSearch()
    }

@kotlinx.serialization.Serializable
data class QuotaCheckResultDto(
    val allowed: Boolean = true,
    val reason: String? = null,
    val message: String? = null,
    val daily_quota: Int = 250,
    val today_queries: Int = 0,
    val monthly_quota: Int = 5000,
    val current_month_queries: Int = 0
)

    fun performSearch(companyId: String? = null, forceRefresh: Boolean = false) {
        val currentCat = selectedCategory.value.uppercase()
        val isTargetingFlights = currentCat == "FLIGHTS" || currentCat == "FLIGHT"

        // ⚡ 0 MS ANLIK ÖNBELLEK ÇIKIŞI: Eğer ürün havuzu bellekte varsa ve aranan kategoriyi içeriyorsa ekrana bas
        globalCachedCombined?.let { cached ->
            val filtered = filterProducts(cached)
            if (filtered.isNotEmpty() || (!isTargetingFlights && !forceRefresh)) {
                _uiState.value = B2BTourSearchUiState.Success(
                    allProducts = cached,
                    filteredProducts = filtered,
                    totalFoundCount = filtered.size
                )
                globalCachedMetadata?.let { searchFilterMetadata.value = it }
                if (!forceRefresh && filtered.isNotEmpty()) return
            }
        }

        viewModelScope.launch {
            // 1. CANLI GÜNLÜK & AYLIK KOTA KONTROLÜ (RPC Guard)
            if (!companyId.isNullOrBlank()) {
                val checkResult = runCatching {
                    val params = kotlinx.serialization.json.buildJsonObject {
                        put("p_company_id", companyId)
                    }
                    supabaseClient.postgrest.rpc("check_and_increment_agency_quota", params).decodeAs<QuotaCheckResultDto>()
                }

                checkResult.onSuccess { res ->
                    dailyQuota.value = res.daily_quota
                    todayQueries.value = res.today_queries
                    monthlyQuota.value = res.monthly_quota
                    currentQueries.value = res.current_month_queries

                    if (!res.allowed) {
                        val msg = res.message ?: "Arama kotanız dolmuştur."
                        isQuotaExceeded.value = true
                        quotaExceededType.value = if (res.reason?.contains("DAILY") == true) "DAILY" else "MONTHLY"
                        quotaErrorMessage.value = msg
                        _uiState.value = B2BTourSearchUiState.Error(msg)
                        return@launch
                    }
                }
            } else {
                // Yerel / Fallback Koruma
                if (dailyQuota.value > 0 && todayQueries.value >= dailyQuota.value) {
                    isQuotaExceeded.value = true
                    quotaExceededType.value = "DAILY"
                    val msg = "⚠️ Günlük arama limitinize (${todayQueries.value}/${dailyQuota.value}) ulaştınız. Limitiniz bu gece 00:00'da yenilenecektir."
                    quotaErrorMessage.value = msg
                    _uiState.value = B2BTourSearchUiState.Error(msg)
                    return@launch
                }
                if (monthlyQuota.value > 0 && currentQueries.value >= monthlyQuota.value) {
                    isQuotaExceeded.value = true
                    quotaExceededType.value = "MONTHLY"
                    val msg = "⛔ Aylık arama ve sorgu kotanız dolmuştur (${currentQueries.value}/${monthlyQuota.value})."
                    quotaErrorMessage.value = msg
                    _uiState.value = B2BTourSearchUiState.Error(msg)
                    return@launch
                }
                todayQueries.value += 1
                currentQueries.value += 1
            }

            isQuotaExceeded.value = false
            quotaExceededType.value = null
            quotaErrorMessage.value = null
            if (globalCachedCombined == null) {
                _uiState.value = B2BTourSearchUiState.Loading
            }

            var items = emptyList<UnifiedProductEntity>()
            runCatching {
                if (isTargetingFlights) {
                    supabaseClient.postgrest["marketplace_products"]
                        .select {
                            filter {
                                eq("product_type", "FLIGHT")
                            }
                            range(0, 300)
                        }
                        .decodeList<UnifiedProductEntity>()
                } else {
                    val tours = runCatching {
                        supabaseClient.postgrest["marketplace_products"]
                            .select {
                                filter {
                                    eq("product_type", "PACKAGE_TOUR")
                                }
                                range(0, 300)
                            }
                            .decodeList<UnifiedProductEntity>()
                    }.getOrDefault(emptyList())

                    val flights = runCatching {
                        supabaseClient.postgrest["marketplace_products"]
                            .select {
                                filter {
                                    eq("product_type", "FLIGHT")
                                }
                                range(0, 300)
                            }
                            .decodeList<UnifiedProductEntity>()
                    }.getOrDefault(emptyList())

                    tours + flights
                }
            }.onSuccess { list ->
                items = list
            }.onFailure { err ->
                println("⚠️ Supabase search load warning: ${err.message}")
            }

            var localHotelProducts = emptyList<UnifiedProductEntity>()
            if (hotelRepository != null) {
                runCatching {
                    hotelRepository.getHotels(tenantId = "", city = null).getOrNull() ?: emptyList()
                }.onSuccess { hotels ->
                    localHotelProducts = hotels.map { h ->
                        UnifiedProductEntity(
                            id = "local-hotel-${h.id}",
                            hotelName = h.name,
                            tourName = "${h.name} - Yerel Otel",
                            country = (h.country ?: "").ifBlank { "Türkiye" },
                            region = (h.city ?: "").ifBlank { "Yerel Bölge" },
                            departureCity = "Yerel Otel",
                            hotelCategory = h.starRating ?: 4,
                            price = 120.0,
                            currency = "EUR",
                            operatorName = "Yerel Oteller",
                            productType = "LOCAL_HOTEL",
                            roomType = "Standart Oda",
                            mealType = "Her Şey Dahil (AI)",
                            pictureUrl = h.coverImageUrl ?: ""
                        )
                    }
                }
            }

            var localTourProducts = emptyList<UnifiedProductEntity>()
            if (tourRepository != null) {
                runCatching {
                    tourRepository.getTours(tenantId = "").getOrNull() ?: emptyList()
                }.onSuccess { tours ->
                    localTourProducts = tours.map { t ->
                        UnifiedProductEntity(
                            id = "local-tour-${t.id}",
                            hotelName = "${t.title} (Yerel Tur)",
                            tourName = t.title,
                            country = t.country.ifBlank { "Türkiye" },
                            region = t.city.ifBlank { "Yerel Bölge" },
                            departureCity = "Yerel Çıkış",
                            hotelCategory = 5,
                            price = if (t.basePrice > 0) t.basePrice else 95.0,
                            currency = "EUR",
                            operatorName = "Yerel Turlar",
                            productType = "LOCAL_TOUR",
                            nights = t.durationDays,
                            roomType = "Tur Paketi",
                            mealType = "Tam Pansiyon (FB)",
                            pictureUrl = t.coverImageUrl ?: ""
                        )
                    }
                }
            }

            // Canlı Yandex DB (marketplace_products) ve yerel ürünleri birleştir (%100 dinamik)
            val memoryItems = AgencyProductPublishingViewModel.getPersistentProducts()
            val rawCombined = (items + localHotelProducts + localTourProducts + memoryItems).distinctBy { it.id }

            // KESİN KURAL: Arama ve listelerde YALNIZCA izin verilen 9 Kanonik Tur Operatörü yer alır.
            // Diğer operatörler veya tanımsızlar arama sonuçlarından tamamen elenir.
            val combined = rawCombined.mapNotNull { item ->
                val pType = item.safeProductType.uppercase()
                val isPureFlight = pType == "FLIGHT" || pType == "CHARTER" || pType == "FLIGHT_ONLY" || 
                                   item.tourName.startsWith("Uçuş:", ignoreCase = true) || 
                                   item.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                                   item.hotelName.startsWith("✈️", ignoreCase = true) ||
                                   (item.hotelName.isBlank() && item.flightNumber.isNotBlank())
                if (isPureFlight) {
                    item
                } else {
                    val canonicalOp = TourOperatorConfig.resolveCanonicalOperatorName(item.operatorName)
                    if (canonicalOp != null) {
                        item.copy(operatorName = canonicalOp)
                    } else {
                        null // 9 TO DIŞINDAKİ TÜM OPERATÖRLERİ ARAMA VE VERİDEN ÇIKAR
                    }
                }
            }
            val filtered = filterProducts(combined)

            val dbDepartureCities = combined.map { it.departureCity }.filter { it.isNotBlank() && it != "Yerel Otel" }.distinct().sorted()
            val dbCountries = combined.map { it.country }.filter { it.isNotBlank() }.distinct().sorted()
            val dbRegions = combined.map { it.region }.filter { it.isNotBlank() }.distinct().sorted()
            val dbOperators = TourOperatorConfig.ALLOWED_OPERATOR_NAMES
            val dbCurrencies = combined.map { it.currency }.filter { it.isNotBlank() }.distinct().sorted()

            searchFilterMetadata.value = SearchFilterMetadataDto(
                departure_cities = dbDepartureCities.ifEmpty { listOf("Moskova", "Saint Petersburg", "Kazan", "Yekaterinburg", "İstanbul") },
                countries = dbCountries.ifEmpty { listOf("Türkiye", "Mısır", "BAE", "Rusya", "Tayland") },
                regions = dbRegions.ifEmpty { listOf("Alanya", "Antalya", "Belek", "Kemer", "Side", "Marmaris", "Bodrum") },
                operators = dbOperators,
                currencies = dbCurrencies.ifEmpty { listOf("RUB", "TRY", "EUR", "USD") }
            )

            globalCachedCombined = combined
            globalCachedMetadata = searchFilterMetadata.value

            _uiState.value = B2BTourSearchUiState.Success(
                allProducts = combined,
                filteredProducts = filtered,
                totalFoundCount = filtered.size
            )
        }
    }

    private fun filterProducts(list: List<UnifiedProductEntity>): List<UnifiedProductEntity> {
        val q = searchQuery.value.trim().lowercase()
        val stars = selectedStars.value
        val meals = selectedMealTypes.value
        val cat = selectedCategory.value.uppercase()
        val dest = selectedRegion.value.trim()
        val country = destinationCountry.value.trim()
        val dep = departureCity.value.trim()
        val isInstant = isInstantConfirmationOnly.value
        val isPromo = isPromoOnly.value

        return list.filter { item ->
            val pType = item.safeProductType.uppercase()
            val isPureFlight = pType == "FLIGHT" || pType == "CHARTER" || pType == "FLIGHT_ONLY" || 
                               item.tourName.startsWith("Uçuş:", ignoreCase = true) || 
                               item.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                               item.hotelName.startsWith("✈️", ignoreCase = true) ||
                               (item.hotelName.isBlank() && item.flightNumber.isNotBlank())
            val isAllowedOp = isPureFlight || TourOperatorConfig.isAllowedOperator(item.operatorName)
            if (!isAllowedOp) return@filter false

            val isPureHotel = (pType == "HOTEL" || pType == "LOCAL_HOTEL" || item.operatorName.contains("Yerel Otel", ignoreCase = true)) && !isPureFlight && item.flightNumber.isBlank()
            val isPackageTour = (pType == "PACKAGE_TOUR" || pType == "LOCAL_TOUR" || pType == "TOUR" || item.hasTransfer || (item.hotelName.isNotBlank() && item.flightNumber.isNotBlank())) && !isPureFlight && !isPureHotel

            val matchesCategory = when (cat) {
                "TOURS", "PACKAGE_TOUR" -> !isPureFlight && (isPackageTour || pType == "PACKAGE_TOUR" || pType == "TOUR" || pType == "LOCAL_TOUR" || pType == "ALL" || item.hasTransfer || item.hotelName.isNotBlank())
                "HOTELS", "HOTEL" -> !isPureFlight && (isPureHotel || pType == "HOTEL" || pType == "LOCAL_HOTEL" || item.hotelCategory >= 3 || item.hotelName.isNotBlank())
                "FLIGHTS", "FLIGHT" -> isPureFlight
                "LOCAL_TOURS" -> pType == "LOCAL_TOUR" || item.id.startsWith("local-tour-")
                "LOCAL_HOTELS" -> pType == "LOCAL_HOTEL" || item.id.startsWith("local-hotel-")
                else -> true
            }

            if (!matchesCategory) return@filter false

            // KESİN KURAL: Yalnızca 9 Kanonik Tur Operatörüne ait ürünler aramada bulunabilir
            if (!isPureFlight && !TourOperatorConfig.isAllowedOperator(item.operatorName)) {
                return@filter false
            }

            // ✈️ UÇUŞ KESİN KURALLARI: Havaalanı olmayan yerlere uçuş gösterme & boş aramada sahte uçuş göstermeme
            val isFlightMode = (cat == "FLIGHTS" || cat == "FLIGHT") || isPureFlight
            if (isFlightMode) {
                val isDepAll = dep.isBlank() || dep.equals("Tüm Kalkış Şehirleri", ignoreCase = true) || dep.equals("Все города", ignoreCase = true) || dep.equals("Все", ignoreCase = true) || dep.equals("ALL", ignoreCase = true)
                val isDestAll = dest.isBlank() || dest.equals("Tüm Destinasyonlar", ignoreCase = true) || dest.equals("Все направления", ignoreCase = true) || dest.equals("Все", ignoreCase = true) || dest.equals("ALL", ignoreCase = true)

                // 1. Havaalanı olmayan tatil beldelerine (Kemer, Belek, Side, Alanya Mahmutlar vb.) uçuş gösterme
                if (!isDestAll && dest.isNotBlank()) {
                    if (!hasAirport(dest)) return@filter false
                }
                // 2. Uçuş sekmesinde hiçbir kalkış ve varış seçilmeden doğrudan uçuş listelenmesini engelle
                if (cat == "FLIGHTS" || cat == "FLIGHT") {
                    if (isDepAll && isDestAll) return@filter false
                }
            }

            val matchesSearch = q.isBlank() ||
                    item.hotelName.lowercase().contains(q) ||
                    item.tourName.lowercase().contains(q) ||
                    item.region.lowercase().contains(q) ||
                    item.country.lowercase().contains(q) ||
                    item.departureCity.lowercase().contains(q) ||
                    item.operatorName.lowercase().contains(q)

            val matchesDest = dest.isBlank() || 
                    dest.contains("Tüm", ignoreCase = true) || 
                    dest.contains("Все", ignoreCase = true) || 
                    dest.equals("ALL", ignoreCase = true) || 
                    isDestinationMatching(item, dest) || 
                    isDestinationMatchingText(
                        targetText = "${item.country} ${item.countryName} ${item.region} ${item.subRegion} ${item.safeHotelName} ${item.tourName} ${item.flightNumber}",
                        selectedDest = dest
                    )

            val matchesCountry = isCountryMatching(item, country)

            val matchesDep = dep.isBlank() || 
                    dep.contains("Tüm", ignoreCase = true) || 
                    dep.contains("Все", ignoreCase = true) || 
                    dep.equals("ALL", ignoreCase = true) || 
                    isDepartureMatching(item, dep) || 
                    isDepartureMatchingText(
                        targetDeparture = "${item.departureCity} ${item.flightNumber} ${item.tourName} ${item.safeHotelName} ${item.region}",
                        selectedDeparture = dep
                    )
            val matchesStar = stars.isEmpty() || item.hotelCategory == 0 || stars.contains(item.hotelCategory)
            val matchesInstant = !isInstant || item.isInstantConfirmation
            val matchesPromo = !isPromo || item.isPromo

            val matchesMeal = meals.isEmpty() || item.mealType.isBlank() || meals.any { m ->
                val lower = item.mealType.lowercase()
                when (m.uppercase()) {
                    "UAI" -> lower.contains("uai") || lower.contains("ultra") || lower.contains("ультра")
                    "AI" -> lower.contains("ai") || lower.contains("all inclusive") || lower.contains("her şey") || lower.contains("все включено")
                    "FB" -> lower.contains("fb") || lower.contains("full board") || lower.contains("tam pansiyon") || lower.contains("полный pansiyon") || lower.contains("полный пансион")
                    "HB" -> lower.contains("hb") || lower.contains("half board") || lower.contains("yarım pansiyon") || lower.contains("полупансион")
                    "BB" -> lower.contains("bb") || lower.contains("bed & breakfast") || lower.contains("oda kahvaltı") || lower.contains("завтрак") || lower.contains("breakfast")
                    "RO" -> lower.contains("ro") || lower.contains("room only") || lower.contains("sadece oda") || lower.contains("bez pitaniya") || lower.contains("без питания")
                    else -> lower.contains(m.lowercase())
                }
            }

            matchesSearch && matchesDest && matchesCountry && matchesDep && matchesStar && matchesMeal && matchesInstant && matchesPromo
        }
    }

    fun selectProductById(productId: String) {
        if (productId.isBlank()) return
        val current = selectedProduct.value
        if (current != null && (current.id == productId || current.id.contains(productId, ignoreCase = true) || productId.contains(current.id, ignoreCase = true))) {
            return
        }

        viewModelScope.launch {
            var matched: UnifiedProductEntity? = null

            // 1. RAM Persistent Ürünlerinde Ara
            matched = com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel.getPersistentProducts().find {
                it.id == productId || it.id.contains(productId, ignoreCase = true) || productId.contains(it.id, ignoreCase = true)
            }

            // 2. Supabase marketplace_products Tablosunda Ara
            if (matched == null) {
                runCatching {
                    supabaseClient.postgrest["marketplace_products"]
                        .select { filter { eq("id", productId) } }
                        .decodeSingleOrNull<UnifiedProductEntity>()
                }.onSuccess {
                    matched = it
                }
            }

            // 3. Varsayılan Zengin Ülke Fırsatlarında Ara
            if (matched == null) {
                val defaultOffer = com.mgacreative.touros.ui.screens.getInitialDefaultOffers().find { 
                    it.id == productId || it.id.equals(productId, ignoreCase = true) || productId.contains(it.id, ignoreCase = true)
                }
                if (defaultOffer != null) {
                    matched = UnifiedProductEntity(
                        id = defaultOffer.id,
                        hotelName = defaultOffer.hotelName,
                        region = defaultOffer.location,
                        country = defaultOffer.countryCode,
                        price = defaultOffer.minPrice,
                        currency = defaultOffer.currency,
                        nights = defaultOffer.nights,
                        mealType = defaultOffer.mealType,
                        roomType = defaultOffer.roomType,
                        flightNumber = defaultOffer.flightCode,
                        hotelCategory = defaultOffer.stars,
                        operatorName = defaultOffer.operatorName,
                        pictureUrl = defaultOffer.imageUrl,
                        productType = defaultOffer.category
                    )
                }
            }

            // 4. Arama Sonuçları Listesinde Ara
            if (matched == null && _uiState.value is B2BTourSearchUiState.Success) {
                matched = (_uiState.value as B2BTourSearchUiState.Success).allProducts.find {
                    it.id == productId || it.id.contains(productId, ignoreCase = true) || productId.contains(it.id, ignoreCase = true)
                }
            }

            // 5. Herhangi bir eşleşme bulunamazsa ID ile anında geçerli bir ürün nesnesi üret
            if (matched == null) {
                val firstDefault = com.mgacreative.touros.ui.screens.getInitialDefaultOffers().firstOrNull()
                matched = UnifiedProductEntity(
                    id = productId,
                    hotelName = firstDefault?.hotelName ?: "Port Nature Luxury Resort Hotel & Spa",
                    region = firstDefault?.location ?: "Belek, Antalya",
                    country = firstDefault?.countryCode ?: "TR",
                    price = firstDefault?.minPrice ?: 301468.0,
                    currency = firstDefault?.currency ?: "RUB",
                    nights = firstDefault?.nights ?: 7,
                    mealType = firstDefault?.mealType ?: "All Inclusive",
                    roomType = firstDefault?.roomType ?: "Standard Room",
                    flightNumber = firstDefault?.flightCode ?: "VKO - AYT (Ekonomi 🟢)",
                    hotelCategory = firstDefault?.stars ?: 5,
                    operatorName = firstDefault?.operatorName ?: "Coral Travel B2B",
                    pictureUrl = firstDefault?.imageUrl ?: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                    productType = firstDefault?.category ?: "PACKAGE_TOUR"
                )
            }

            matched?.let { selectProductForBooking(it) }
        }
    }

    fun selectProductForBooking(product: UnifiedProductEntity) {
        selectedProduct.value = product
        
        val isHotelOnly = product.productType.equals("LOCAL_HOTEL", ignoreCase = true) || product.productType.equals("HOTEL", ignoreCase = true)
        
        if (!isHotelOnly) {
            val flights = getOperatorFlightOptionsForProduct(product)
            availableFlightOptions.value = flights
            selectedFlightOption.value = flights.firstOrNull()
            fetchDatabaseFlightsForOperator(product)
        } else {
            availableFlightOptions.value = emptyList()
            selectedFlightOption.value = null
        }

        val paxCount = (adults.value + childs.value).coerceAtLeast(1)

        // Dinamik Ekstra Hizmetler (Yolcu Sayısına Bağlı)
        val infantCount = childrenAges.value.count { it <= 2 }
        val isFlightOnly = product.productType.equals("FLIGHT", ignoreCase = true) || product.flightNumber.isNotBlank() || product.tourName.contains("Uçuş", ignoreCase = true)

        extraServices.value = if (isFlightOnly) {
            listOf(
                ExtraService("srv-1", "Uçuş & Bagaj Güvence Sigortası", "INSURANCE", 12.00, isMandatory = false, isSelected = false, paxCount = paxCount),
                ExtraService("srv-2", "Uçuş İptal / Bilet Değişiklik Güvencesi", "INSURANCE", 18.00, isMandatory = false, isSelected = false, paxCount = paxCount),
                ExtraService("srv-3", "Havalimanı Hızlı Geçiş (Fast Track & Lounge)", "EXTRA", 25.00, isMandatory = false, isSelected = false, paxCount = paxCount)
            )
        } else {
            listOf(
                ExtraService("srv-1", "SOGLASIE Medikal Sigorta 50.000 EUR", "INSURANCE", 16.25, isMandatory = true, isSelected = true, paxCount = paxCount),
                ExtraService("srv-2", "Seyahat İptal / Vize İptal Sigortası", "INSURANCE", 25.00, isMandatory = false, isSelected = false, paxCount = paxCount),
                ExtraService("srv-3", "Elite VIP Özel Havalimanı Transferi", "TRANSFER", 0.00, isMandatory = false, isSelected = true, paxCount = paxCount),
                ExtraService("srv-4", "Bebek Oto Koltuğu Ekstrası", "EXTRA", 15.00, isMandatory = false, isSelected = (infantCount > 0), paxCount = infantCount.coerceAtLeast(1))
            )
        }

        // Dinamik Yolcu Formu (Yetişkinler + Çocuk Yaşları)
        val paxList = mutableListOf<PassengerInfo>()
        var idx = 1
        repeat(adults.value.coerceAtLeast(1)) {
            paxList.add(
                PassengerInfo(
                    index = idx,
                    passengerType = "ADULT",
                    gender = if (idx % 2 != 0) "MALE" else "FEMALE",
                    isPayer = (idx == 1),
                    citizenship = "Türkiye",
                    documentType = "Pasaport"
                )
            )
            idx++
        }
        childrenAges.value.forEach { age ->
            paxList.add(
                PassengerInfo(
                    index = idx,
                    passengerType = if (age <= 2) "INFANT" else "CHILD",
                    childAge = age,
                    gender = if (idx % 2 != 0) "MALE" else "FEMALE",
                    citizenship = "Türkiye",
                    documentType = if (age <= 2) "Doğum Belgesi / Pasaport" else "Pasaport",
                    isPayer = false
                )
            )
            idx++
        }
        passengers.value = paxList
    }

    /**
     * Yetişkin Yolcu Ekleme
     */
    fun addAdultPassenger() {
        adults.value = adults.value + 1
        val current = passengers.value.toMutableList()
        val nextIdx = current.size + 1
        current.add(
            PassengerInfo(
                index = nextIdx,
                passengerType = "ADULT",
                childAge = null,
                gender = "MALE",
                citizenship = "Türkiye",
                documentType = "Pasaport",
                isPayer = false
            )
        )
        passengers.value = current
        updateExtraServicesPaxCount(current.size)
    }

    /**
     * Çocuk Yolcu Ekleme (Yaş Seçimli)
     */
    fun addChildPassenger(age: Int = 5) {
        val currentAges = childrenAges.value.toMutableList()
        currentAges.add(age)
        childrenAges.value = currentAges
        childs.value = currentAges.size

        val current = passengers.value.toMutableList()
        val nextIdx = current.size + 1
        current.add(
            PassengerInfo(
                index = nextIdx,
                passengerType = if (age <= 2) "INFANT" else "CHILD",
                childAge = age,
                gender = "MALE",
                citizenship = "Türkiye",
                documentType = if (age <= 2) "Doğum Belgesi / Pasaport" else "Pasaport",
                isPayer = false
            )
        )
        passengers.value = current
        updateExtraServicesPaxCount(current.size)
    }

    /**
     * Yolcu Ekleme (Genel)
     */
    fun addPassenger() {
        addAdultPassenger()
    }

    /**
     * Yolcu Çıkarma (Dinamik Form)
     */
    fun removePassenger(paxIndex: Int) {
        val current = passengers.value.toMutableList()
        if (current.size > 1) {
            val removed = current.find { it.index == paxIndex }
            if (removed != null) {
                if (removed.passengerType == "ADULT") {
                    adults.value = (adults.value - 1).coerceAtLeast(1)
                } else {
                    val currentAges = childrenAges.value.toMutableList()
                    if (currentAges.isNotEmpty()) {
                        currentAges.removeAt(currentAges.lastIndex)
                        childrenAges.value = currentAges
                        childs.value = currentAges.size
                    }
                }
            }
            current.removeAll { it.index == paxIndex }
            val reindexed = current.mapIndexed { idx, p ->
                p.copy(index = idx + 1, isPayer = (idx == 0))
            }
            passengers.value = reindexed
            updateExtraServicesPaxCount(reindexed.size)
        }
    }

    fun setChildAgeForPassenger(paxIndex: Int, newAge: Int) {
        val current = passengers.value.toMutableList()
        val idx = current.indexOfFirst { it.index == paxIndex }
        if (idx != -1) {
            val p = current[idx]
            current[idx] = p.copy(
                childAge = newAge,
                passengerType = if (newAge <= 2) "INFANT" else "CHILD",
                documentType = if (newAge <= 2) "Doğum Belgesi / Pasaport" else "Pasaport"
            )
            passengers.value = current

            // childrenAges senkronizasyonu
            val allChildAges = current.filter { it.passengerType != "ADULT" }.mapNotNull { it.childAge }
            childrenAges.value = allChildAges
            childs.value = allChildAges.size
        }
    }

    private fun updateExtraServicesPaxCount(count: Int) {
        extraServices.value = extraServices.value.map { srv ->
            srv.copy(paxCount = count)
        }
    }

    fun toggleExtraService(serviceId: String) {
        extraServices.value = extraServices.value.map { srv ->
            if (srv.id == serviceId && !srv.isMandatory) {
                srv.copy(isSelected = !srv.isSelected)
            } else srv
        }
    }

    /**
     * Rezervasyonu Supabase public.bookings Tablosuna Gerçek Olarak Kaydeder
     */
    fun confirmBookingAndSaveToSupabase(onComplete: (pnrCode: String) -> Unit) {
        viewModelScope.launch {
            isSavingBooking.value = true
            val prod = selectedProduct.value ?: return@launch
            val fl = selectedFlightOption.value
            val pList = passengers.value

            val pnr = "B2B-PNR-${Random.nextInt(100000, 999999)}"
            createdPnrCode.value = pnr

            val mainPayer = pList.firstOrNull { it.isPayer } ?: pList.firstOrNull()
            val payerName = "${mainPayer?.firstName ?: ""} ${mainPayer?.lastName ?: ""}".trim().ifBlank { "Müşteri Yolcu" }

            val isFlight = prod.productType.equals("FLIGHT", ignoreCase = true) || prod.flightNumber.isNotBlank() || prod.tourName.contains("Uçuş", ignoreCase = true)
            val dynamicMultiplier = calculateMultiplier(adults.value, childrenAges.value, isFlight)
            val basePrice = prod.price * dynamicMultiplier
            val flightDelta = fl?.priceDeltaRub ?: 0.0

            val conversionRate = when (prod.currency.uppercase()) {
                "RUB" -> 100.0
                "TRY", "TL" -> 38.0
                "USD" -> 1.08
                else -> 1.0 // EUR
            }
            val extrasInProductCurrency = extraServices.value.filter { it.isSelected }.sumOf { (it.unitPriceEur * conversionRate) * it.paxCount }
            val totalPrice = basePrice + flightDelta + extrasInProductCurrency

            val bookingId = generateUuid()

            // 1. ZENGİN YOLCU LİSTESİ OLUŞTURMA (Cinsiyet, Pasaport, SKT, Sorumlu Yetişkin, İnfant Koltuk)
            val domainPassengers = pList.mapIndexed { idx, p ->
                val fullName = "${p.firstName} ${p.lastName}".trim().ifBlank { "Турист ${idx + 1}" }
                val extraInfo = buildString {
                    if (p.isInfantSeatRequested) append(" • [✈️ Место для инфанта]")
                    if (!p.isPayer) append(" • [👨‍👦 Ответственный: Турист 1]")
                    if (p.citizenship.isNotBlank()) append(" • [Гражданство: ${p.citizenship}]")
                    if (p.birthCountry.isNotBlank()) append(" • [Страна рождения: ${p.birthCountry}]")
                    if (p.documentIssuedBy.isNotBlank()) append(" • [Кем выдан: ${p.documentIssuedBy}]")
                    if (p.documentExpiryDate.isNotBlank()) append(" • [Срок действия: ${p.documentExpiryDate}]")
                }
                Passenger(
                    id = generateUuid(),
                    bookingId = bookingId,
                    fullName = fullName,
                    firstName = p.firstName,
                    lastName = p.lastName,
                    tcNo = p.passportSeries,
                    passportSeries = p.passportSeries,
                    passportNo = p.passportNumber,
                    birthDate = p.birthDate,
                    gender = if (p.gender == "MALE") "Мужской" else "Женский",
                    citizenship = p.citizenship,
                    birthCountry = p.birthCountry,
                    documentType = p.documentType,
                    documentIssueDate = p.documentIssueDate,
                    documentIssuedBy = p.documentIssuedBy,
                    documentExpiryDate = p.documentExpiryDate,
                    phone = p.phone,
                    email = p.email,
                    address = p.address,
                    isLead = p.isPayer,
                    notes = extraInfo
                )
            }

            // 2. ZENGİN HİZMET & UÇUŞ KALEMLERİ OLUŞTURMA
            val domainItems = mutableListOf<BookingItem>()
            
            if (isFlight) {
                // Kalem 1: Uçuş Bileti
                domainItems.add(
                    BookingItem(
                        id = generateUuid(),
                        bookingId = bookingId,
                        description = "✈️ Uçuş Bileti: ${fl?.outboundAirline ?: prod.airlineName.ifBlank { "Havayolu" }} (${fl?.outboundFlightNumber ?: prod.flightNumber}) • ${prod.departureCity} ➔ ${prod.region}",
                        quantity = pList.size,
                        unitPrice = if (pList.isNotEmpty()) (basePrice / pList.size) else basePrice,
                        totalPrice = basePrice,
                        itemType = "FLIGHT",
                        notes = "Kalkış: ${prod.departureDate ?: "2026-08-21"} • Bagaj: ${fl?.baggageKg ?: prod.baggageKg}kg"
                    )
                )
            } else {
                // Kalem 1: Otel Konaklama Paketi
                domainItems.add(
                    BookingItem(
                        id = generateUuid(),
                        bookingId = bookingId,
                        description = "🏨 ${prod.hotelName} (${prod.roomType.ifBlank { "FAMILY ROOM" }}) • ${prod.mealType.ifBlank { "Ultra All Inclusive" }}",
                        quantity = pList.size,
                        unitPrice = if (pList.isNotEmpty()) (basePrice / pList.size) else basePrice,
                        totalPrice = basePrice,
                        itemType = "HOTEL",
                        notes = "Giriş: ${prod.departureDate ?: "2026-08-21"} (${prod.nights} Gece) • Destinasyon: ${prod.region}"
                    )
                )

                // Kalem 2: Uçuş Parkuru Detayı
                if (fl != null) {
                    domainItems.add(
                        BookingItem(
                            id = generateUuid(),
                            bookingId = bookingId,
                            description = "🛫 UÇUŞ: Gidiş ${fl.outboundAirline} (${fl.outboundFlightNumber}) ${fl.outboundDeparturePort}->${fl.outboundArrivalPort} (02:05-06:45) | Dönüş ${fl.inboundAirline} (${fl.inboundFlightNumber}) ${fl.inboundDeparturePort}->${fl.inboundArrivalPort} (18:40-23:05)",
                            quantity = pList.size,
                            unitPrice = 0.0,
                            totalPrice = 0.0,
                            itemType = "FLIGHT",
                            notes = "El Bagajı: ${fl.handBaggageKg}kg • Kayıtlı Bagaj: ${fl.baggageKg}kg"
                        )
                    )
                }
            }

            // Kalem 3..N: Seçilen Ekstra Hizmet ve Sigortalar
            extraServices.value.filter { it.isSelected }.forEach { srv ->
                val srvTotalPrice = srv.unitPriceEur * conversionRate * srv.paxCount
                val srvUnitPrice = srv.unitPriceEur * conversionRate
                domainItems.add(
                    BookingItem(
                        id = generateUuid(),
                        bookingId = bookingId,
                        description = "🛡️ ${srv.name}",
                        quantity = srv.paxCount,
                        unitPrice = srvUnitPrice,
                        totalPrice = srvTotalPrice,
                        itemType = srv.category,
                        notes = "Birim: ${srvUnitPrice.toInt()} ${prod.currency}/Pax (${srv.paxCount} Yolcu Dahil)"
                    )
                )
            }

            val operatorTitle = prod.operatorName.ifBlank { "Coral Travel / Anex Tour B2B" }

            val currentUser = runCatching { getCurrentUserUseCase?.invoke() }.getOrNull()
            val effectiveTenantId = currentUser?.tenantId?.takeIf { it.isNotBlank() } ?: "00000000-0000-0000-0000-000000000001"

            val domainBooking = Booking(
                id = bookingId,
                bookingCode = pnr,
                customerName = payerName,
                customerEmail = mainPayer?.email?.ifBlank { "acente@touros.com" },
                customerPhone = mainPayer?.phone?.ifBlank { "+90 500 000 0000" },
                totalPrice = totalPrice,
                currency = prod.currency.ifBlank { "EUR" },
                paxCount = pList.size,
                status = BookingStatus.BEKLIYOR,
                operatorName = operatorTitle,
                productName = if (isFlight) "${prod.hotelName} (${prod.flightNumber})" else "${prod.tourName.ifBlank { prod.hotelName }} (${prod.hotelName})",
                departureDate = prod.departureDate ?: "2026-08-21",
                nights = prod.nights,
                bookingType = if (isFlight) "FLIGHT" else "PACKAGE_TOUR",
                roomTypeName = if (isFlight) "UÇUŞ BİLETİ" else prod.roomType.ifBlank { "DELUXE ROOM" },
                operatorPnrCode = null,
                operatorStatus = "BEKLİYOR",
                notes = "🏢 Acente Rezervasyon Talebi • Operatör: $operatorTitle • Uçuş: ${fl?.outboundAirline ?: prod.airlineName.ifBlank { "Charter" }}",
                tenantId = effectiveTenantId,
                items = domainItems,
                passengers = domainPassengers
            )

            // BookingRepository (Önbellek + Supabase) Üzerinden Kaydet
            bookingRepository.createBooking(domainBooking)
                .onSuccess {
                    println("✅ Rezervasyon BookingRepository ile önbellek ve Supabase'e başarıyla kaydedildi: PNR $pnr")
                    bookingErrorMessage.value = null
                    isSavingBooking.value = false
                    onComplete(pnr)
                }.onFailure { err ->
                    println("❌ BookingRepository kayıt hatası: ${err.message}")
                    isSavingBooking.value = false
                    bookingErrorMessage.value = "Supabase Rezervasyon Kayıt Hatası:\n${err.message ?: err.toString()}"
                }
        }
    }

    private fun getOperatorFlightOptionsForProduct(product: UnifiedProductEntity): List<FlightOption> {
        val op = product.safeOperatorName.lowercase()
        val depCity = product.departureCity.ifBlank { "Moskova" }
        val arrCity = product.region.ifBlank { "Antalya" }
        val bagKg = if (product.baggageKg > 0) product.baggageKg else 20

        val candidateFlights = mutableListOf<FlightOption>()

        // 1. Paketin asıl uçuşu veya operatörün varsayılan charter seferi (0 RUB fark ile pakete dahil)
        val defaultAirline = product.airlineName.ifBlank { 
            when {
                op.contains("pegas") -> "Nordwind Airlines"
                op.contains("anex") -> "Azur Air"
                op.contains("coral") || op.contains("odeon") || op.contains("sunmar") -> "SunExpress"
                op.contains("fun") || op.contains("tui") -> "Red Wings"
                op.contains("aeroflot") || op.contains("biblio") -> "Aeroflot"
                op.contains("tez") -> "Turkish Airlines"
                op.contains("loti") -> "Loti Black Jet"
                else -> "Pegasus Airlines"
            }
        }
        val mainFlightNo = product.flightNumber.ifBlank {
            when {
                op.contains("pegas") -> "N4-5821"
                op.contains("anex") -> "ZF-8881"
                op.contains("coral") || op.contains("odeon") || op.contains("sunmar") -> "XQ-9012"
                op.contains("fun") || op.contains("tui") -> "WZ-3091"
                op.contains("aeroflot") || op.contains("biblio") -> "SU-2134"
                op.contains("tez") -> "TK-3701"
                op.contains("loti") -> "LTI-101"
                else -> "PC-1822"
            }
        }
        val returnFlightNo = if (mainFlightNo.endsWith("R", ignoreCase = true)) mainFlightNo else "${mainFlightNo}R"

        candidateFlights.add(
            FlightOption(
                id = "fl-${product.id}-main",
                outboundAirline = defaultAirline,
                outboundFlightNumber = mainFlightNo,
                outboundDeparturePort = "$depCity 02:05",
                outboundArrivalPort = "$arrCity 06:45",
                outboundDepartureTime = "02:05",
                outboundArrivalTime = "06:45",
                outboundDuration = "4s 40d",
                inboundAirline = defaultAirline,
                inboundFlightNumber = returnFlightNo,
                inboundDeparturePort = "$arrCity 18:40",
                inboundArrivalPort = "$depCity 23:05",
                inboundDepartureTime = "18:40",
                inboundArrivalTime = "23:05",
                inboundDuration = "4s 25d",
                baggageKg = bagKg,
                handBaggageKg = 8,
                priceDeltaRub = 0.0,
                operatorName = product.safeOperatorName
            )
        )

        // 2. Operatöre özel alternatif uçuşlar (Sadece seçilen operatörün anlaşmalı uçuşları)
        when {
            op.contains("pegas") -> {
                candidateFlights.add(
                    FlightOption(
                        id = "fl-${product.id}-pegas-2",
                        outboundAirline = "Nordwind Airlines (Konfor)",
                        outboundFlightNumber = "N4-5825",
                        outboundDeparturePort = "$depCity 10:15",
                        outboundArrivalPort = "$arrCity 14:40",
                        outboundDepartureTime = "10:15",
                        outboundArrivalTime = "14:40",
                        outboundDuration = "4s 25d",
                        inboundAirline = "Nordwind Airlines (Konfor)",
                        inboundFlightNumber = "N4-5826",
                        inboundDeparturePort = "$arrCity 16:30",
                        inboundArrivalPort = "$depCity 20:50",
                        inboundDepartureTime = "16:30",
                        inboundArrivalTime = "20:50",
                        inboundDuration = "4s 20d",
                        baggageKg = 25,
                        handBaggageKg = 10,
                        priceDeltaRub = 1800.0,
                        operatorName = product.safeOperatorName
                    )
                )
            }
            op.contains("anex") -> {
                candidateFlights.add(
                    FlightOption(
                        id = "fl-${product.id}-anex-2",
                        outboundAirline = "Southwind Airlines",
                        outboundFlightNumber = "2S-101",
                        outboundDeparturePort = "$depCity 11:00",
                        outboundArrivalPort = "$arrCity 15:30",
                        outboundDepartureTime = "11:00",
                        outboundArrivalTime = "15:30",
                        outboundDuration = "4s 30d",
                        inboundAirline = "Southwind Airlines",
                        inboundFlightNumber = "2S-102",
                        inboundDeparturePort = "$arrCity 17:00",
                        inboundArrivalPort = "$depCity 21:30",
                        inboundDepartureTime = "17:00",
                        inboundArrivalTime = "21:30",
                        inboundDuration = "4s 30d",
                        baggageKg = 20,
                        handBaggageKg = 8,
                        priceDeltaRub = 2100.0,
                        operatorName = product.safeOperatorName
                    )
                )
            }
            op.contains("coral") || op.contains("sunmar") || op.contains("odeon") -> {
                candidateFlights.add(
                    FlightOption(
                        id = "fl-${product.id}-coral-2",
                        outboundAirline = "Pegasus Airlines",
                        outboundFlightNumber = "PC-2014",
                        outboundDeparturePort = "$depCity 10:15",
                        outboundArrivalPort = "$arrCity 14:40",
                        outboundDepartureTime = "10:15",
                        outboundArrivalTime = "14:40",
                        outboundDuration = "4s 25d",
                        inboundAirline = "Pegasus Airlines",
                        inboundFlightNumber = "PC-2015",
                        inboundDeparturePort = "$arrCity 16:30",
                        inboundArrivalPort = "$depCity 20:50",
                        inboundDepartureTime = "16:30",
                        inboundArrivalTime = "20:50",
                        inboundDuration = "4s 20d",
                        baggageKg = 20,
                        handBaggageKg = 8,
                        priceDeltaRub = 1800.0,
                        operatorName = product.safeOperatorName
                    )
                )
            }
            op.contains("fun") || op.contains("tui") -> {
                candidateFlights.add(
                    FlightOption(
                        id = "fl-${product.id}-fun-2",
                        outboundAirline = "Pegasus Airlines",
                        outboundFlightNumber = "PC-1822",
                        outboundDeparturePort = "$depCity 13:20",
                        outboundArrivalPort = "$arrCity 17:45",
                        outboundDepartureTime = "13:20",
                        outboundArrivalTime = "17:45",
                        outboundDuration = "4s 25d",
                        inboundAirline = "Pegasus Airlines",
                        inboundFlightNumber = "PC-1823",
                        inboundDeparturePort = "$arrCity 19:30",
                        inboundArrivalPort = "$depCity 23:55",
                        inboundDepartureTime = "19:30",
                        inboundArrivalTime = "23:55",
                        inboundDuration = "4s 25d",
                        baggageKg = 20,
                        handBaggageKg = 8,
                        priceDeltaRub = 2300.0,
                        operatorName = product.safeOperatorName
                    )
                )
            }
            op.contains("biblio") || op.contains("aeroflot") -> {
                candidateFlights.add(
                    FlightOption(
                        id = "fl-${product.id}-afl-2",
                        outboundAirline = "Aeroflot (Comfort)",
                        outboundFlightNumber = "SU-2138",
                        outboundDeparturePort = "$depCity 10:45",
                        outboundArrivalPort = "$arrCity 15:10",
                        outboundDepartureTime = "10:45",
                        outboundArrivalTime = "15:10",
                        outboundDuration = "4s 25d",
                        inboundAirline = "Aeroflot (Comfort)",
                        inboundFlightNumber = "SU-2139",
                        inboundDeparturePort = "$arrCity 16:20",
                        inboundArrivalPort = "$depCity 20:45",
                        inboundDepartureTime = "16:20",
                        inboundArrivalTime = "20:45",
                        inboundDuration = "4s 25d",
                        baggageKg = 30,
                        handBaggageKg = 10,
                        priceDeltaRub = 2500.0,
                        operatorName = product.safeOperatorName
                    )
                )
            }
            else -> {
                // Genel operatörler (Tez Tour, Loti, Intourist, Paximum vb.) için alternatif uçuş
                candidateFlights.add(
                    FlightOption(
                        id = "fl-${product.id}-alt-2",
                        outboundAirline = "Turkish Airlines",
                        outboundFlightNumber = "TK-3705",
                        outboundDeparturePort = "$depCity 09:30",
                        outboundArrivalPort = "$arrCity 13:55",
                        outboundDepartureTime = "09:30",
                        outboundArrivalTime = "13:55",
                        outboundDuration = "4s 25d",
                        inboundAirline = "Turkish Airlines",
                        inboundFlightNumber = "TK-3706",
                        inboundDeparturePort = "$arrCity 15:00",
                        inboundArrivalPort = "$depCity 19:25",
                        inboundDepartureTime = "15:00",
                        inboundArrivalTime = "19:25",
                        inboundDuration = "4s 25d",
                        baggageKg = 25,
                        handBaggageKg = 8,
                        priceDeltaRub = 2200.0,
                        operatorName = product.safeOperatorName
                    )
                )
            }
        }

        return candidateFlights
    }

    private fun fetchDatabaseFlightsForOperator(product: UnifiedProductEntity) {
        viewModelScope.launch {
            runCatching {
                val params = buildJsonObject {
                    put("p_operator_name", product.safeOperatorName)
                    put("p_departure_city", product.departureCity)
                    put("p_arrival_city", product.region)
                    product.departureDate?.takeIf { it.isNotBlank() }?.let {
                        put("p_departure_date", it)
                    }
                }
                supabaseClient.postgrest.rpc("get_operator_flight_options", params)
                    .decodeList<OperatorFlightScheduleDto>()
            }.onSuccess { dbFlights ->
                if (dbFlights.isNotEmpty()) {
                    val mapped = dbFlights.mapIndexed { idx, fs ->
                        val retFlightNo = if (fs.flight_number.endsWith("R", ignoreCase = true)) fs.flight_number else "${fs.flight_number}R"
                        val depTime = fs.departure_time.take(5)
                        val arrTime = fs.arrival_time.take(5)
                        FlightOption(
                            id = "fl-db-${fs.id.ifBlank { "$idx" }}",
                            outboundAirline = fs.airline_name,
                            outboundFlightNumber = fs.flight_number,
                            outboundDeparturePort = "${fs.departure_city} $depTime",
                            outboundArrivalPort = "${fs.arrival_city} $arrTime",
                            outboundDepartureTime = depTime,
                            outboundArrivalTime = arrTime,
                            outboundDuration = "${fs.duration_minutes / 60}s ${fs.duration_minutes % 60}d",
                            inboundAirline = fs.airline_name,
                            inboundFlightNumber = retFlightNo,
                            inboundDeparturePort = "${fs.arrival_city} 18:40",
                            inboundArrivalPort = "${fs.departure_city} 23:05",
                            inboundDepartureTime = "18:40",
                            inboundArrivalTime = "23:05",
                            inboundDuration = "${fs.duration_minutes / 60}s ${fs.duration_minutes % 60}d",
                            baggageKg = if (fs.baggage_kg > 0) fs.baggage_kg else 20,
                            handBaggageKg = 8,
                            priceDeltaRub = fs.price_delta_rub,
                            operatorName = fs.operator_name.ifBlank { product.safeOperatorName }
                        )
                    }
                    availableFlightOptions.value = mapped
                    if (selectedFlightOption.value == null || !mapped.any { it.id == selectedFlightOption.value?.id }) {
                        selectedFlightOption.value = mapped.firstOrNull()
                    }
                }
            }
        }
    }
}

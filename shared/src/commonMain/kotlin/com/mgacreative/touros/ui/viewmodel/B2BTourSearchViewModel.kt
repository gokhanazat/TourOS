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
    val priceDeltaRub: Double = 0.0
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
    var citizenship: String = "Türkiye",
    var documentType: String = "Pasaport",
    var passportSeries: String = "",
    var passportNumber: String = "",
    var documentExpiryDate: String = "",
    var isPayer: Boolean = false,
    var phone: String = "",
    var email: String = "",
    var address: String = "",
    var parentIndex: Int? = null,
    var isInfantSeatRequested: Boolean = false
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
            val isTargetMoscow = target.contains("moskova") || target.contains("moscow") || target.contains("москва") || target.contains("svo") || target.contains("vko") || target.contains("dme") || target.contains("zia")
            val isQueryMoscow = query.contains("moskova") || query.contains("moscow") || query.contains("москва") || query.contains("svo") || query.contains("vko") || query.contains("dme") || query.contains("zia")
            if (isTargetMoscow && isQueryMoscow) return true

            val isTargetSpb = target.contains("petersburg") || target.contains("петербург") || target.contains("питер") || target.contains("led")
            val isQuerySpb = query.contains("petersburg") || query.contains("петербург") || query.contains("питер") || query.contains("led")
            if (isTargetSpb && isQuerySpb) return true

            val isTargetIst = target.contains("istanbul") || target.contains("istanbul") || target.contains("ist") || target.contains("saw") || target.contains("стамбул")
            val isQueryIst = query.contains("istanbul") || query.contains("istanbul") || query.contains("ist") || query.contains("saw") || query.contains("стамбул")
            if (isTargetIst && isQueryIst) return true

            val isTargetKzn = target.contains("kazan") || target.contains("казань") || target.contains("kzn")
            val isQueryKzn = query.contains("kazan") || query.contains("казань") || query.contains("kzn")
            if (isTargetKzn && isQueryKzn) return true

            val isTargetAntalya = target.contains("antalya") || target.contains("ayt") || target.contains("анталья")
            val isQueryAntalya = query.contains("antalya") || query.contains("ayt") || query.contains("анталья")
            if (isTargetAntalya && isQueryAntalya) return true

            return false
        }

        fun isDepartureMatching(item: UnifiedProductEntity, departure: String): Boolean {
            return isDepartureMatchingText(item.departureCity, departure)
        }

        fun isDestinationMatchingText(targetText: String, selectedDest: String): Boolean {
            val dest = selectedDest.trim()
            if (dest.isBlank() || dest.equals("Tüm Destinasyonlar", ignoreCase = true) || dest.equals("Tüm Varış Noktaları", ignoreCase = true) || dest.equals("Tüm", ignoreCase = true) || dest.equals("ALL", ignoreCase = true) || dest.equals("Все направления", ignoreCase = true) || dest.startsWith("Tüm", ignoreCase = true) || dest.startsWith("Все", ignoreCase = true)) {
                return true
            }

            val targetLower = targetText.lowercase()
            val destLower = dest.lowercase()

            // 1. Doğrudan tam içerik kontrolü
            if (targetLower.contains(destLower)) {
                return true
            }

            // 2. Belirli bir alt bölge / şehir / belde var mı kontrolü (Öncelikli Katı Eşleşme)
            val isBelek = destLower.contains("belek") || destLower.contains("белек") || destLower.contains("boğazkent") || destLower.contains("kadriye")
            if (isBelek) {
                return targetLower.contains("belek") || targetLower.contains("белек") || targetLower.contains("boğazkent") || targetLower.contains("kadriye")
            }

            val isKemer = destLower.contains("kemer") || destLower.contains("кемер") || destLower.contains("beldibi") || destLower.contains("tekirova") || destLower.contains("göynük") || destLower.contains("kiriş") || destLower.contains("çamyuva")
            if (isKemer) {
                return targetLower.contains("kemer") || targetLower.contains("кемер") || targetLower.contains("beldibi") || targetLower.contains("tekirova") || targetLower.contains("göynük") || targetLower.contains("kiriş") || targetLower.contains("çamyuva")
            }

            val isLara = destLower.contains("lara") || destLower.contains("лара") || destLower.contains("kundu")
            if (isLara) {
                return targetLower.contains("lara") || targetLower.contains("лара") || targetLower.contains("kundu")
            }

            val isSide = destLower.contains("side") || destLower.contains("сиде") || destLower.contains("manavgat") || destLower.contains("çolaklı") || destLower.contains("kumköy") || destLower.contains("sorgun") || destLower.contains("titreyengöl")
            if (isSide) {
                return targetLower.contains("side") || targetLower.contains("сиде") || targetLower.contains("manavgat") || targetLower.contains("çolaklı") || targetLower.contains("kumköy") || targetLower.contains("sorgun") || targetLower.contains("titreyengöl")
            }

            val isAlanya = destLower.contains("alanya") || destLower.contains("аланья") || destLower.contains("okurcalar") || destLower.contains("mahmutlar") || destLower.contains("avsallar") || destLower.contains("konaklı") || destLower.contains("oba")
            if (isAlanya) {
                return targetLower.contains("alanya") || targetLower.contains("аланья") || targetLower.contains("okurcalar") || targetLower.contains("mahmutlar") || targetLower.contains("avsallar") || targetLower.contains("konaklı") || targetLower.contains("oba")
            }

            val isBodrum = destLower.contains("bodrum") || destLower.contains("бодрум") || destLower.contains("yalıkavak") || destLower.contains("torba") || destLower.contains("gümbet") || destLower.contains("turgutreis") || destLower.contains("gündoğan")
            if (isBodrum) {
                return targetLower.contains("bodrum") || targetLower.contains("бодрум") || targetLower.contains("yalıkavak") || targetLower.contains("torba") || targetLower.contains("gümbet") || targetLower.contains("turgutreis") || targetLower.contains("gündoğan")
            }

            val isMarmaris = destLower.contains("marmaris") || destLower.contains("мармарис") || destLower.contains("içmeler") || destLower.contains("turunç")
            if (isMarmaris) {
                return targetLower.contains("marmaris") || targetLower.contains("мармарис") || targetLower.contains("içmeler") || targetLower.contains("turunç")
            }

            val isFethiye = destLower.contains("fethiye") || destLower.contains("фетхие") || destLower.contains("ölüdeniz") || destLower.contains("göcek") || destLower.contains("kayaköy")
            if (isFethiye) {
                return targetLower.contains("fethiye") || targetLower.contains("фетхие") || targetLower.contains("ölüdeniz") || targetLower.contains("göcek") || targetLower.contains("kayaköy")
            }

            val isCesme = destLower.contains("çeşme") || destLower.contains("cesme") || destLower.contains("alaçatı") || destLower.contains("чешме")
            if (isCesme) {
                return targetLower.contains("çeşme") || targetLower.contains("cesme") || targetLower.contains("alaçatı") || targetLower.contains("чешме")
            }

            val isAntalyaGenel = destLower.contains("antalya") || destLower.contains("анталья")
            if (isAntalyaGenel) {
                return targetLower.contains("antalya") || targetLower.contains("анталья") || targetLower.contains("belek") || targetLower.contains("kemer") || targetLower.contains("lara") || targetLower.contains("side") || targetLower.contains("alanya")
            }

            val isSharm = destLower.contains("şarm") || destLower.contains("sharm") || destLower.contains("шарм") || destLower.contains("nabq") || destLower.contains("naama")
            if (isSharm) {
                return targetLower.contains("şarm") || targetLower.contains("sharm") || targetLower.contains("шарм") || targetLower.contains("nabq") || targetLower.contains("naama")
            }

            val isHurghada = destLower.contains("hurgada") || destLower.contains("hurghada") || destLower.contains("хургада") || destLower.contains("el gouna") || destLower.contains("makadi") || destLower.contains("sahl hasheesh")
            if (isHurghada) {
                return targetLower.contains("hurgada") || targetLower.contains("hurghada") || targetLower.contains("хургада") || targetLower.contains("el gouna") || targetLower.contains("makadi") || targetLower.contains("sahl hasheesh")
            }

            val isPhuket = destLower.contains("phuket") || destLower.contains("пхукет")
            if (isPhuket) {
                return targetLower.contains("phuket") || targetLower.contains("пхукет")
            }

            val isPattaya = destLower.contains("pattaya") || destLower.contains("паттайя")
            if (isPattaya) {
                return targetLower.contains("pattaya") || targetLower.contains("паттайя")
            }

            val isBangkok = destLower.contains("bangkok") || destLower.contains("бангкок")
            if (isBangkok) {
                return targetLower.contains("bangkok") || targetLower.contains("бангкок")
            }

            val isSamui = destLower.contains("samui") || destLower.contains("самуи")
            if (isSamui) {
                return targetLower.contains("samui") || targetLower.contains("самуи")
            }

            val isDaNang = destLower.contains("da nang") || destLower.contains("danang") || destLower.contains("дананг")
            if (isDaNang) {
                return targetLower.contains("da nang") || targetLower.contains("danang") || targetLower.contains("дананг")
            }

            val isPhuQuoc = destLower.contains("phu quoc") || destLower.contains("phuquoc") || destLower.contains("фукуок")
            if (isPhuQuoc) {
                return targetLower.contains("phu quoc") || targetLower.contains("phuquoc") || targetLower.contains("фукуок")
            }

            val isNhaTrang = destLower.contains("nha trang") || destLower.contains("nhatrang") || destLower.contains("нячанг")
            if (isNhaTrang) {
                return targetLower.contains("nha trang") || targetLower.contains("nhatrang") || targetLower.contains("нячанг")
            }

            val isDubai = destLower.contains("dubai") || destLower.contains("дубай") || destLower.contains("jumeirah") || destLower.contains("marina") || destLower.contains("downtown")
            if (isDubai) {
                return targetLower.contains("dubai") || targetLower.contains("дубай") || targetLower.contains("jumeirah") || targetLower.contains("marina") || targetLower.contains("downtown")
            }

            val isAbuDhabi = destLower.contains("abu dhabi") || destLower.contains("абу-даби")
            if (isAbuDhabi) {
                return targetLower.contains("abu dhabi") || targetLower.contains("абу-даби")
            }

            val isSochi = destLower.contains("sochi") || destLower.contains("сочи") || destLower.contains("krasnaya polyana") || destLower.contains("красная поляна")
            if (isSochi) {
                return targetLower.contains("sochi") || targetLower.contains("сочи") || targetLower.contains("krasnaya polyana") || targetLower.contains("красная поляна")
            }

            // 3. Genel Ülke Eşleşmesi (Eğer belirli bir alt şehir seçilmemişse)
            val isTurkey = destLower.contains("türkiye") || destLower.contains("turkey") || destLower.contains("турция")
            if (isTurkey) {
                return targetLower.contains("türkiye") || targetLower.contains("turkey") || targetLower.contains("турция") || targetLower.contains("antalya") || targetLower.contains("belek") || targetLower.contains("kemer") || targetLower.contains("bodrum") || targetLower.contains("marmaris") || targetLower.contains("fethiye") || targetLower.contains("side") || targetLower.contains("alanya") || targetLower.contains("istanbul")
            }

            val isEgypt = destLower.contains("mısır") || destLower.contains("egypt") || destLower.contains("египет")
            if (isEgypt) {
                return targetLower.contains("mısır") || targetLower.contains("egypt") || targetLower.contains("египет") || targetLower.contains("sharm") || targetLower.contains("şarm") || targetLower.contains("hurgada") || targetLower.contains("hurghada") || targetLower.contains("gouna") || targetLower.contains("makadi")
            }

            val isThailand = destLower.contains("tayland") || destLower.contains("thailand") || destLower.contains("таиланд") || destLower.contains("тайланд")
            if (isThailand) {
                return targetLower.contains("tayland") || targetLower.contains("thailand") || targetLower.contains("таиланд") || targetLower.contains("тайланд") || targetLower.contains("phuket") || targetLower.contains("pattaya") || targetLower.contains("bangkok") || targetLower.contains("samui")
            }

            val isVietnam = destLower.contains("vietnam") || destLower.contains("вьетнам")
            if (isVietnam) {
                return targetLower.contains("vietnam") || targetLower.contains("вьетнам") || targetLower.contains("da nang") || targetLower.contains("phu quoc") || targetLower.contains("nha trang") || targetLower.contains("hoi an")
            }

            val isUAE = destLower.contains("bae") || destLower.contains("uae") || destLower.contains("оаэ")
            if (isUAE) {
                return targetLower.contains("bae") || targetLower.contains("uae") || targetLower.contains("оаэ") || targetLower.contains("dubai") || targetLower.contains("abu dhabi") || targetLower.contains("sharjah")
            }

            val isRussia = destLower.contains("rusya") || destLower.contains("russia") || destLower.contains("россия")
            if (isRussia) {
                return targetLower.contains("rusya") || targetLower.contains("russia") || targetLower.contains("россия") || targetLower.contains("sochi") || targetLower.contains("сочи") || targetLower.contains("petersburg") || targetLower.contains("kazan") || targetLower.contains("moskova")
            }

            // 4. Token bazlı fallback
            val tokens = dest.split('/', ',', '(', ')', '—', '-')
                .map { it.trim().lowercase() }
                .filter { it.length >= 3 && !it.startsWith("tüm") && !it.startsWith("все") }

            return tokens.any { targetLower.contains(it) }
        }

        fun isDestinationMatching(item: UnifiedProductEntity, selectedDest: String): Boolean {
            val targetText = "${item.country} ${item.countryName} ${item.countryCode} ${item.region} ${item.subRegion} ${item.safeHotelName} ${item.tourName} ${item.roomType}"
            return isDestinationMatchingText(targetText, selectedDest)
        }

        fun isCountryMatching(item: UnifiedProductEntity, countryCodeOrName: String): Boolean {
            if (countryCodeOrName.isBlank() || countryCodeOrName == "ALL" || countryCodeOrName.equals("Tüm", ignoreCase = true)) return true
            val code = countryCodeOrName.uppercase().trim()
            if (item.countryCode.isNotBlank() && item.countryCode.equals(code, ignoreCase = true)) return true

            val destText = "${item.country} ${item.countryName} ${item.countryCode} ${item.region} ${item.subRegion} ${item.safeHotelName}".lowercase().trim()

            return when (code) {
                "TR", "TÜRKIYE", "TURKEY", "ТУРЦИЯ" -> destText.contains("türkiye") || destText.contains("turkey") || destText.contains("турция") || destText.contains(" tr ") || destText.startsWith("tr ") || destText.endsWith(" tr") || destText == "tr" ||
                        destText.contains("antalya") || destText.contains("belek") || destText.contains("kemer") || destText.contains("lara") ||
                        destText.contains("alanya") || destText.contains("side") || destText.contains("bodrum") || destText.contains("marmaris") ||
                        destText.contains("fethiye") || destText.contains("çeşme") || destText.contains("белек") ||
                        destText.contains("кемер") || destText.contains("анталья") || destText.contains("аланья") || destText.contains("сиде") ||
                        destText.contains("бодрум") || destText.contains("мармарис") || destText.contains("фетхие") || (destText.contains("istanbul") && !destText.contains("sharm") && !destText.contains("dubai"))
                "EG", "MISIR", "EGYPT", "ЕГИПЕТ" -> destText.contains("mısır") || destText.contains("egypt") || destText.contains("египет") || destText.contains(" eg ") || destText.startsWith("eg ") || destText == "eg" ||
                        destText.contains("şarm") || destText.contains("sharm") || destText.contains("hurgada") || destText.contains("hurghada") ||
                        destText.contains("el gouna") || destText.contains("makadi") || destText.contains("шарм") || destText.contains("хургада") ||
                        destText.contains("эль гуна") || destText.contains("макади")
                "TH", "TAYLAND", "THAILAND", "ТАИЛАНД", "ТАЙЛАНД" -> destText.contains("tayland") || destText.contains("thailand") || destText.contains("таиланд") || destText.contains("тайланд") || destText.contains(" th ") || destText.startsWith("th ") || destText == "th" ||
                        destText.contains("phuket") || destText.contains("pattaya") || destText.contains("bangkok") || destText.contains("samui") ||
                        destText.contains("krabi") || destText.contains("пхукет") || destText.contains("паттайя") || destText.contains("бангкок") ||
                        destText.contains("самуи") || destText.contains("краби")
                "VN", "VIETNAM", "ВЬЕТНАМ" -> destText.contains("vietnam") || destText.contains("вьетнам") || destText.contains(" vn ") || destText.startsWith("vn ") || destText == "vn" ||
                        destText.contains("da nang") || destText.contains("phu quoc") || destText.contains("nha trang") || destText.contains("hoi an") ||
                        destText.contains("дананг") || destText.contains("фукуок") || destText.contains("нячанг") || destText.contains("хойан")
                "AE", "BAE", "DUBAI", "UAE", "ОАЭ" -> destText.contains("bae") || destText.contains("dubai") || destText.contains("uae") || destText.contains("оаэ") || destText.contains(" ae ") || destText.startsWith("ae ") || destText == "ae" ||
                        destText.contains("дубай") || destText.contains("abu dhabi") || destText.contains("абу-даби") || destText.contains("sharjah") ||
                        destText.contains("шарджа") || destText.contains("jumeirah") || destText.contains("marina")
                "RU", "RUSYA", "RUSSIA", "РОССИЯ" -> (destText.contains("rusya") || destText.contains("russia") || destText.contains("россия") || destText.contains("sochi") ||
                        destText.contains("сочи") || destText.contains("st. petersburg") || destText.contains("петербург") || destText.contains("kazan") ||
                        destText.contains("казань")) && !destText.contains("antalya") && !destText.contains("belek") && !destText.contains("kemer") && !destText.contains("lara")
                else -> destText.contains(countryCodeOrName.lowercase())
            }
        }

        fun isSubRegionMatching(item: UnifiedProductEntity, subRegion: String?): Boolean {
            if (subRegion.isNullOrBlank() || subRegion == "Tümü" || subRegion == "ALL") return true
            val s = subRegion.lowercase().trim()
            val fullText = "${item.country} ${item.countryName} ${item.region} ${item.subRegion} ${item.safeHotelName}".lowercase()

            val synonyms = when (s) {
                "belek" -> listOf("belek", "белек")
                "kemer" -> listOf("kemer", "кемер")
                "antalya" -> listOf("antalya", "анталья", "ayt")
                "alanya" -> listOf("alanya", "аланья")
                "side" -> listOf("side", "сиде")
                "bodrum" -> listOf("bodrum", "бодрум")
                "marmaris" -> listOf("marmaris", "мармарис")
                "fethiye" -> listOf("fethiye", "фетхие")
                "çeşme" -> listOf("çeşme", "cesme", "чешме")
                "şarm el-şeyh" -> listOf("şarm", "sharm", "шарм")
                "hurgada" -> listOf("hurgada", "hurghada", "хургада")
                "el gouna" -> listOf("el gouna", "gouna", "эль гуна")
                "makadi bay" -> listOf("makadi", "макади")
                "phuket" -> listOf("phuket", "пхукет")
                "pattaya" -> listOf("pattaya", "паттайя")
                "bangkok" -> listOf("bangkok", "бангкок")
                "koh samui" -> listOf("samui", "самуи")
                "krabi" -> listOf("krabi", "краби")
                "da nang" -> listOf("da nang", "danang", "дананг")
                "phu quoc" -> listOf("phu quoc", "phuquoc", "фукуок")
                "nha trang" -> listOf("nha trang", "nhatrang", "нячанг")
                "hoi an" -> listOf("hoi an", "hoian", "хойан")
                "dubai marina" -> listOf("dubai", "marina", "дубай")
                "palm jumeirah" -> listOf("palm", "jumeirah", "пальм")
                "downtown" -> listOf("downtown", "даунтаун")
                "abu dhabi" -> listOf("abu dhabi", "абу-даби")
                "moskova" -> listOf("moskova", "moscow", "москва")
                "st. petersburg" -> listOf("petersburg", "петербург", "питер")
                "sochi" -> listOf("sochi", "сочи")
                "kazan" -> listOf("kazan", "казань")
                else -> listOf(s)
            }
            return synonyms.any { fullText.contains(it) }
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

    fun performSearch(companyId: String? = null) {
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
            _uiState.value = B2BTourSearchUiState.Loading

            var items = emptyList<UnifiedProductEntity>()
            runCatching {
                supabaseClient.postgrest["marketplace_products"]
                    .select()
                    .decodeList<UnifiedProductEntity>()
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

            val sampleFlightProducts = listOf(
                UnifiedProductEntity(
                    id = "flight-sample-1",
                    productType = "FLIGHT",
                    tourName = "Moskova (SVO) - Antalya (AYT) Uçuş Seferi",
                    operatorName = "Turkish Airlines (THY)",
                    departureCity = "Moskova",
                    country = "Türkiye",
                    region = "Antalya",
                    flightNumber = "TK-3701",
                    airlineName = "Turkish Airlines",
                    isCharter = false,
                    price = 240.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800"
                ),
                UnifiedProductEntity(
                    id = "flight-sample-2",
                    productType = "FLIGHT",
                    tourName = "Moskova (DME) - Antalya (AYT) Direk Charter Uçuş",
                    operatorName = "Pegas Touristik (Nordwind)",
                    departureCity = "Moskova",
                    country = "Türkiye",
                    region = "Antalya",
                    flightNumber = "N4-5821",
                    airlineName = "Nordwind Airlines",
                    isCharter = true,
                    price = 185.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800"
                ),
                UnifiedProductEntity(
                    id = "flight-sample-3",
                    productType = "FLIGHT",
                    tourName = "Moskova (VKO) - İstanbul (IST) Tarifeli Uçuş",
                    operatorName = "Aeroflot",
                    departureCity = "Moskova",
                    country = "Türkiye",
                    region = "İstanbul",
                    flightNumber = "SU-2134",
                    airlineName = "Aeroflot",
                    isCharter = false,
                    price = 210.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800"
                ),
                UnifiedProductEntity(
                    id = "flight-sample-4",
                    productType = "FLIGHT",
                    tourName = "İstanbul (SAW) - Antalya (AYT) Direkt Uçuş",
                    operatorName = "Pegasus Airlines",
                    departureCity = "İstanbul",
                    country = "Türkiye",
                    region = "Antalya",
                    flightNumber = "PC-2014",
                    airlineName = "Pegasus Airlines",
                    isCharter = false,
                    price = 85.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1519074069444-1ba4eff56b61?w=800"
                )
            )

            val sampleMultiCountryTours = listOf(
                // 🇹🇷 TÜRKİYE (BELEK, BODRUM, KEMER, SİDE, ALANYA, MARMARİS, FETHİYE)
                UnifiedProductEntity(
                    id = "tour-seed-tr-belek-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Antalya (AYT) Belek Golf & Sahil Paketi",
                    operatorName = "Coral Travel",
                    price = 920.0,
                    currency = "EUR",
                    hotelName = "Maxx Royal Belek Golf Resort",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Antalya",
                    subRegion = "Belek",
                    roomType = "Suite Kara Manzaralı",
                    mealType = "Maxx All Inclusive",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Golf", "Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-tr-belek-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (VKO) - Antalya (AYT) Belek Lüks Aile Tatili",
                    operatorName = "Anex Tour",
                    price = 840.0,
                    currency = "EUR",
                    hotelName = "Rixos Premium Belek",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Antalya",
                    subRegion = "Belek",
                    roomType = "Deluxe Deniz Manzaralı Oda",
                    mealType = "Ultra Her Şey Dahil (UAI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("The Land of Legends Giriş", "Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-tr-bodrum-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Bodrum (BJV) Ege Rüyası Tatil Paketi",
                    operatorName = "Pegas Touristik",
                    price = 890.0,
                    currency = "EUR",
                    hotelName = "Titanic Luxury Collection Bodrum",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Bodrum",
                    subRegion = "Güvercinlik (Yalıkavak, Torba)",
                    roomType = "Superior Deniz Manzaralı",
                    mealType = "Ultra Her Şey Dahil (UAI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Özel İskele", "Wi-Fi", "SPA", "Kum Plaj", "Havuz", "Çocuk Kulübü")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-tr-bodrum-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (DME) - Bodrum (BJV) Yalıkavak & Torba Paketi",
                    operatorName = "Coral Travel",
                    price = 980.0,
                    currency = "EUR",
                    hotelName = "Rixos Premium Bodrum",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Bodrum",
                    subRegion = "Torba (Yalıkavak)",
                    roomType = "Deluxe Garden View",
                    mealType = "Ultra Her Şey Dahil (UAI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Aqua Park", "Wi-Fi", "SPA", "Özel Koy", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-tr-kemer-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (VKO) - Antalya (AYT) Kemer Doğa & Eğlence Paketi",
                    operatorName = "Fun & Sun",
                    price = 780.0,
                    currency = "EUR",
                    hotelName = "Rixos Sungate Kemer",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Antalya",
                    subRegion = "Kemer (Beldibi)",
                    roomType = "Standard Marine Room",
                    mealType = "Ultra Her Şey Dahil (UAI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Aquapark", "Wi-Fi", "SPA", "Plaj", "Konser Alanı")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-tr-side-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Antalya (AYT) Side Tarih & Kum Plaj Paketi",
                    operatorName = "Biblio-Globus",
                    price = 710.0,
                    currency = "EUR",
                    hotelName = "Barut Hemera Side",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Antalya",
                    subRegion = "Side (Kumköy)",
                    roomType = "Deluxe Room Garden View",
                    mealType = "Ultra Her Şey Dahil (UAI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Kum Plaj", "Wi-Fi", "SPA", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-tr-marmaris-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Dalaman (DLM) Marmaris Çam Kokulu Tatil",
                    operatorName = "Anex Tour",
                    price = 760.0,
                    currency = "EUR",
                    hotelName = "D-Resort Grand Azur Marmaris",
                    hotelCategory = 5,
                    country = "Türkiye",
                    countryCode = "TR",
                    countryName = "Türkiye",
                    region = "Marmaris",
                    subRegion = "İçmeler",
                    roomType = "Standard Deniz Manzaralı",
                    mealType = "Her Şey Dahil (AI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Mavi Bayrak Plaj", "Wi-Fi", "SPA", "Havuz")
                ),
                // 🇪🇬 MISIR
                UnifiedProductEntity(
                    id = "tour-seed-eg-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Şarm El-Şeyh (SSH) Lüks Plaj Paketi",
                    operatorName = "Coral Travel",
                    price = 850.0,
                    currency = "EUR",
                    hotelName = "Rixos Premium Seagate Sharm",
                    hotelCategory = 5,
                    country = "Mısır",
                    countryCode = "EG",
                    countryName = "Mısır",
                    region = "Şarm El-Şeyh",
                    subRegion = "Nabq Bay",
                    roomType = "Deluxe Aqua Sea View",
                    mealType = "Ultra Her Şey Dahil (UAI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-eg-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "İstanbul (IST) - Hurgada (HRG) Kızıldeniz Tatil Paketi",
                    operatorName = "Anex Tour",
                    price = 720.0,
                    currency = "EUR",
                    hotelName = "Steigenberger ALDAU Beach Hotel",
                    hotelCategory = 5,
                    country = "Mısır",
                    countryCode = "EG",
                    countryName = "Mısır",
                    region = "Hurgada",
                    subRegion = "El Gouna",
                    roomType = "Standard Sea Front Room",
                    mealType = "Her Şey Dahil (AI)",
                    departureCity = "İstanbul",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Wi-Fi", "SPA", "Kum Plaj", "Havuz", "Aquapark")
                ),

                // 🇹🇭 TAYLAND
                UnifiedProductEntity(
                    id = "tour-seed-th-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (VKO) - Phuket (HKT) Egzotik Ada Paketi",
                    operatorName = "Pegas Touristik",
                    price = 1150.0,
                    currency = "EUR",
                    hotelName = "JW Marriott Phuket Resort & Spa",
                    hotelCategory = 5,
                    country = "Tayland",
                    countryCode = "TH",
                    countryName = "Tayland",
                    region = "Phuket",
                    subRegion = "Mai Khao Beach",
                    roomType = "Deluxe Garden View",
                    mealType = "Oda Kahvaltı (BB)",
                    departureCity = "Moskova",
                    nights = 10,
                    pictureUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Wi-Fi", "SPA", "Kum Plaj", "Havuz", "Çocuk Kulübü")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-th-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "İstanbul (IST) - Pattaya (UTP) Eğlence & Sahil Turu",
                    operatorName = "Fun & Sun",
                    price = 890.0,
                    currency = "EUR",
                    hotelName = "Centara Grand Mirage Beach Resort",
                    hotelCategory = 5,
                    country = "Tayland",
                    countryCode = "TH",
                    countryName = "Tayland",
                    region = "Pattaya",
                    subRegion = "Naklua Beach",
                    roomType = "Deluxe Ocean Facing",
                    mealType = "Oda Kahvaltı (BB)",
                    departureCity = "İstanbul",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Havuz")
                ),

                // 🇻🇳 VİETNAM
                UnifiedProductEntity(
                    id = "tour-seed-vn-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Phu Quoc (PQC) Tropikal Cennet Paketi",
                    operatorName = "Anex Tour",
                    price = 980.0,
                    currency = "EUR",
                    hotelName = "Vinpearl Resort & Spa Phu Quoc",
                    hotelCategory = 5,
                    country = "Vietnam",
                    countryCode = "VN",
                    countryName = "Vietnam",
                    region = "Phu Quoc",
                    subRegion = "Long Beach",
                    roomType = "Deluxe King Room",
                    mealType = "Tam Pansiyon (FB)",
                    departureCity = "Moskova",
                    nights = 10,
                    pictureUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-vn-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "İstanbul (IST) - Da Nang (DAD) Sahil & Kültür Paketi",
                    operatorName = "Coral Travel",
                    price = 1290.0,
                    currency = "EUR",
                    hotelName = "InterContinental Danang Sun Peninsula Resort",
                    hotelCategory = 5,
                    country = "Vietnam",
                    countryCode = "VN",
                    countryName = "Vietnam",
                    region = "Da Nang",
                    subRegion = "Son Tra Peninsula",
                    roomType = "Classic Resort View",
                    mealType = "Oda Kahvaltı (BB)",
                    departureCity = "İstanbul",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = false,
                    amenities = listOf("Wi-Fi", "SPA", "Kum Plaj", "Havuz")
                ),

                // 🇦🇪 BAE (DUBAİ)
                UnifiedProductEntity(
                    id = "tour-seed-ae-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (DME) - Dubai (DXB) Lüks Tatil & Aquaventure",
                    operatorName = "Coral Travel",
                    price = 1450.0,
                    currency = "EUR",
                    hotelName = "Atlantis The Palm Dubai",
                    hotelCategory = 5,
                    country = "BAE (Dubai)",
                    countryCode = "AE",
                    countryName = "BAE",
                    region = "Dubai",
                    subRegion = "Palm Jumeirah",
                    roomType = "Ocean King Room",
                    mealType = "Yarım Pansiyon (HB)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Aquapark", "Wi-Fi", "SPA", "Kum Plaj", "Çocuk Kulübü", "Havuz")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-ae-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "İstanbul (IST) - Dubai (DXB) Şehir & Marina Paketi",
                    operatorName = "Anex Tour",
                    price = 990.0,
                    currency = "EUR",
                    hotelName = "Rixos Premium Dubai JBR",
                    hotelCategory = 5,
                    country = "BAE (Dubai)",
                    countryCode = "AE",
                    countryName = "BAE",
                    region = "Dubai",
                    subRegion = "Dubai Marina",
                    roomType = "Deluxe Walk View",
                    mealType = "Oda Kahvaltı (BB)",
                    departureCity = "İstanbul",
                    nights = 5,
                    pictureUrl = "https://images.unsplash.com/photo-1580674684081-7617fbf3d745?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Wi-Fi", "SPA", "Kum Plaj", "Havuz")
                ),

                // 🇷🇺 RUSYA
                UnifiedProductEntity(
                    id = "tour-seed-ru-1",
                    productType = "PACKAGE_TOUR",
                    tourName = "Moskova (SVO) - Soçi (AER) Karadeniz Sahil & Spa Turu",
                    operatorName = "Biblio-Globus",
                    price = 680.0,
                    currency = "EUR",
                    hotelName = "Radisson Collection Paradise Resort & Spa Sochi",
                    hotelCategory = 5,
                    country = "Rusya",
                    countryCode = "RU",
                    countryName = "Rusya",
                    region = "Sochi",
                    subRegion = "Adler",
                    roomType = "Superior Room Sea View",
                    mealType = "Her Şey Dahil (AI)",
                    departureCity = "Moskova",
                    nights = 7,
                    pictureUrl = "https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Wi-Fi", "SPA", "Kum Plaj", "Havuz", "Çocuk Kulübü")
                ),
                UnifiedProductEntity(
                    id = "tour-seed-ru-2",
                    productType = "PACKAGE_TOUR",
                    tourName = "Saint Petersburg (LED) - Soçi Dağ & Doğa Tatili",
                    operatorName = "Pegas Touristik",
                    price = 540.0,
                    currency = "EUR",
                    hotelName = "Krasnaya Polyana Mountain Resort Hotel",
                    hotelCategory = 5,
                    country = "Rusya",
                    countryCode = "RU",
                    countryName = "Rusya",
                    region = "Sochi",
                    subRegion = "Krasnaya Polyana",
                    roomType = "Standard Mountain View",
                    mealType = "Oda Kahvaltı (BB)",
                    departureCity = "Saint Petersburg",
                    nights = 6,
                    pictureUrl = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=800",
                    isInstantConfirmation = true,
                    hasTransfer = true,
                    isDirectFlight = true,
                    amenities = listOf("Wi-Fi", "SPA", "Havuz")
                )
            )

            // Hem Supabase'den gelenleri, Yerel Otelleri, Uçuşları hem de hafızadaki ürünleri birleştir
            val memoryItems = AgencyProductPublishingViewModel.getPersistentProducts()
            val combined = (items + localHotelProducts + localTourProducts + sampleFlightProducts + sampleMultiCountryTours + memoryItems).distinctBy { it.id }
            val filtered = filterProducts(combined)

            val dbDepartureCities = combined.map { it.departureCity }.filter { it.isNotBlank() && it != "Yerel Otel" }.distinct().sorted()
            val dbCountries = combined.map { it.country }.filter { it.isNotBlank() }.distinct().sorted()
            val dbRegions = combined.map { it.region }.filter { it.isNotBlank() }.distinct().sorted()
            val dbOperators = combined.map { it.operatorName }.filter { it.isNotBlank() }.distinct().sorted()
            val dbCurrencies = combined.map { it.currency }.filter { it.isNotBlank() }.distinct().sorted()

            searchFilterMetadata.value = SearchFilterMetadataDto(
                departure_cities = dbDepartureCities.ifEmpty { listOf("Moskova", "Saint Petersburg", "Kazan", "Yekaterinburg", "İstanbul") },
                countries = dbCountries.ifEmpty { listOf("Türkiye", "Mısır", "BAE", "Rusya", "Tayland") },
                regions = dbRegions.ifEmpty { listOf("Alanya", "Antalya", "Belek", "Kemer", "Side", "Marmaris", "Bodrum") },
                operators = dbOperators.ifEmpty { listOf("Coral Travel", "Pegas Touristik", "Anex Tour", "Biblio Globus", "Fun & Sun", "Tez Tour") },
                currencies = dbCurrencies.ifEmpty { listOf("RUB", "TRY", "EUR", "USD") }
            )

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
            val isPureFlight = pType == "FLIGHT" || item.airlineName.isNotBlank() || item.flightNumber.startsWith("TK-") || item.flightNumber.startsWith("N4-") || item.flightNumber.startsWith("SU-") || item.flightNumber.startsWith("PC-") || item.tourName.startsWith("Uçuş:", ignoreCase = true) || item.hotelName.startsWith("Uçuş:", ignoreCase = true) || item.hotelName.startsWith("✈️", ignoreCase = true)
            val isPureHotel = (pType == "HOTEL" || pType == "LOCAL_HOTEL" || item.operatorName.contains("Yerel Otel", ignoreCase = true)) && !isPureFlight && item.flightNumber.isBlank()
            val isPackageTour = (pType == "PACKAGE_TOUR" || pType == "LOCAL_TOUR" || pType == "TOUR" || item.hasTransfer) && !isPureFlight && !isPureHotel

            val matchesCategory = when (cat) {
                "TOURS", "PACKAGE_TOUR" -> isPackageTour
                "HOTELS", "HOTEL" -> isPureHotel
                "FLIGHTS", "FLIGHT" -> isPureFlight
                "LOCAL_TOURS" -> pType == "LOCAL_TOUR" || item.id.startsWith("local-tour-")
                "LOCAL_HOTELS" -> pType == "LOCAL_HOTEL" || item.id.startsWith("local-hotel-")
                else -> true
            }

            if (!matchesCategory) return@filter false

            val matchesSearch = q.isBlank() ||
                    item.hotelName.lowercase().contains(q) ||
                    item.tourName.lowercase().contains(q) ||
                    item.region.lowercase().contains(q) ||
                    item.country.lowercase().contains(q) ||
                    item.departureCity.lowercase().contains(q) ||
                    item.operatorName.lowercase().contains(q)

            val matchesDest = isDestinationMatching(item, dest)
            val matchesCountry = isCountryMatching(item, country)
            val matchesDep = isDepartureMatching(item, dep)
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
        
        val airline = product.airlineName.ifBlank { "Charter Flight" }
        val flightNo = product.flightNumber.ifBlank { "CH-${(1000..9999).random()}" }
        val depCity = product.departureCity.ifBlank { "Kalkış Şehri" }
        val arrCity = product.region.ifBlank { "Varış Bölgesi" }
        val bagKg = if (product.baggageKg > 0) product.baggageKg else 20

        val flights = listOf(
            FlightOption(
                id = "fl-${product.id}-1",
                outboundAirline = airline,
                outboundFlightNumber = flightNo,
                outboundDeparturePort = "$depCity 02:05",
                outboundArrivalPort = "$arrCity 06:45",
                outboundDepartureTime = "02:05",
                outboundArrivalTime = "06:45",
                outboundDuration = "4s 40d",
                inboundAirline = airline,
                inboundFlightNumber = "${flightNo}R",
                inboundDeparturePort = "$arrCity 18:40",
                inboundArrivalPort = "$depCity 23:05",
                inboundDepartureTime = "18:40",
                inboundArrivalTime = "23:05",
                inboundDuration = "4s 25d",
                baggageKg = bagKg,
                handBaggageKg = 8,
                priceDeltaRub = 0.0
            )
        )

        availableFlightOptions.value = flights
        selectedFlightOption.value = flights.first()

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
                val fullName = "${p.firstName} ${p.lastName}".trim().ifBlank { "Turist ${idx + 1}" }
                val extraInfo = buildString {
                    if (p.isInfantSeatRequested) append(" • [✈️ İnfant Koltuğu Talep Edildi]")
                    if (!p.isPayer) append(" • [👨‍👦 Çocuk Sorumlusu: Turist 1 (Yetişkin)]")
                    if (p.citizenship.isNotBlank()) append(" • [Uyruk: ${p.citizenship}]")
                    if (p.documentExpiryDate.isNotBlank()) append(" • [Pasaport SKT: ${p.documentExpiryDate}]")
                }
                Passenger(
                    id = generateUuid(),
                    bookingId = bookingId,
                    fullName = fullName,
                    tcNo = p.passportSeries.ifBlank { "8492" },
                    passportNo = p.passportNumber.ifBlank { "84920492" },
                    birthDate = p.birthDate.ifBlank { "12.05.1985" },
                    gender = if (p.gender == "MALE") "Bay (Мужской)" else "Bayan (Женский)",
                    phone = p.phone.ifBlank { "+90 532 100 2030" },
                    email = p.email.ifBlank { "ahmet@gmail.com" },
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

}

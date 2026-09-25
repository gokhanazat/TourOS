package com.mgacreative.touros.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import touros.shared.generated.resources.Res
import touros.shared.generated.resources.axileto_logo_white
import touros.shared.generated.resources.club_loyalty_badge
import touros.shared.generated.resources.flight
import touros.shared.generated.resources.hero_banner
import touros.shared.generated.resources.popular_countries_banner
import com.mgacreative.touros.domain.model.PromoBannerItem
import com.mgacreative.touros.domain.model.BookingItem
import com.mgacreative.touros.domain.model.Passenger
import com.mgacreative.touros.domain.model.TourOperatorConfig
import com.mgacreative.touros.data.util.isValidUuid
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mgacreative.touros.ui.components.TourOSVerticalScrollbar
import com.mgacreative.touros.ui.components.TourOSLazyListVerticalScrollbar
import io.github.jan.supabase.postgrest.postgrest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.repository.AuthRepository
import com.mgacreative.touros.domain.repository.BookingRepository
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.LanguageSelector
import com.mgacreative.touros.ui.components.AppLanguage
import com.mgacreative.touros.ui.components.UniversalTourSearchBar
import com.mgacreative.touros.ui.components.UniversalTourDetailedFilters
import com.mgacreative.touros.ui.components.SearchBarVariant
import com.mgacreative.touros.ui.theme.TourOSColors

import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.localization.AppLanguageManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class PublicHotelOffer(
    val id: String,
    val hotelName: String,
    val location: String,
    val stars: Int,
    val description: String,
    val minPrice: Double,
    val maxPrice: Double,
    val imageUrl: String,
    val operatorName: String = "Coral Travel",
    val roomType: String = "standard room",
    val mealType: String = "Bez pitaniya",
    val flightCode: String = "",
    val nights: Int = 7,
    val currency: String = "RUB",
    val isInstantConfirmation: Boolean = true,
    val category: String = "PACKAGE_TOUR", // "PACKAGE_TOUR", "HOTEL", "FLIGHT", "LAST_MINUTE"
    val discountPercent: Int? = null,
    val ratingScore: Double? = null,
    val isLastMinute: Boolean = false,
    val baggageKg: Int = 0,
    val agencyPrices: List<AgencyPriceOption> = emptyList(),
    val countryCode: String = "TR",
    val departureCity: String = "",
    val departureDate: String? = null,
    val returnDate: String? = null
)

fun matchesSelectedCountry(offer: PublicHotelOffer, selectedCode: String): Boolean {
    if (selectedCode.isBlank() || selectedCode == "ALL") return true
    val loc = offer.location.lowercase().trim()
    val hName = offer.hotelName.lowercase().trim()
    val cCode = offer.countryCode.uppercase().trim()
    val fullText = "$cCode $loc $hName"

    val foreignMarkers = listOf(
        "хайнань", "hainan", "санья", "sanya", "гагра", "пицунда", "гудаут", "сухум", "abhazya", "абхазия",
        "нячанг", "фукуок", "дананг", "фантьет", "ханой", "вьетнам", "vietnam",
        "дубай", "dubai", "шарджа", "абу-даби", "abu dhabi", "фуджейра", "оаэ", "bae",
        "пхукет", "паттайя", "бангкок", "краби", "самуи", "тайланд", "thailand",
        "шарм", "хургада", "египет", "egypt", "мальдив", "maldiv", "maldives",
        "сочи", "петербург", "москва", "калининград", "россия", "rusya",
        "бали", "bali", "занзибар", "zanzibar", "шри-ланка", "sri lanka",
        "кипр", "cyprus", "грузия", "georgia", "батуми", "черногория", "montenegro",
        "маврикий", "mauritius", "сейшел", "seychelles", "греция", "greece", "крит", "родос"
    )

    if (selectedCode.equals("TR", ignoreCase = true)) {
        if (foreignMarkers.any { loc.contains(it) || hName.contains(it) }) return false
        return cCode == "TR" || cCode == "TUR" || loc.contains("türkiye") || loc.contains("turkey") || loc.contains("турция") ||
                loc.contains("antalya") || loc.contains("bodrum") || loc.contains("belek") || loc.contains("kemer") || loc.contains("lara") ||
                loc.contains("alanya") || loc.contains("side") || loc.contains("marmaris") || loc.contains("fethiye") || loc.contains("çeşme") ||
                loc.contains("белек") || loc.contains("кемер") || loc.contains("анталья") || loc.contains("аланья") || loc.contains("сиде") ||
                loc.contains("бодрум") || loc.contains("мармарис") || loc.contains("кушадасы") || loc.contains("dalaman") || loc.contains("даламан")
    }

    if (cCode.equals(selectedCode.trim(), ignoreCase = true)) return true

    return when (selectedCode.uppercase().trim()) {
        "EG" -> cCode == "EG" || cCode == "EGY" || fullText.contains("mısır") || fullText.contains("egypt") || fullText.contains("египет") || fullText.contains("şarm") || fullText.contains("sharm") || fullText.contains("шарм") || fullText.contains("hurgada") || fullText.contains("hurghada") || fullText.contains("хургада") || fullText.contains("gouna") || fullText.contains("makadi")
        "TH" -> cCode == "TH" || cCode == "THA" || fullText.contains("tayland") || fullText.contains("thailand") || fullText.contains("таиланд") || fullText.contains("тайланд") || fullText.contains("phuket") || fullText.contains("пхукет") || fullText.contains("pattaya") || fullText.contains("паттайя") || fullText.contains("bangkok") || fullText.contains("бангкок") || fullText.contains("samui") || fullText.contains("krabi")
        "VN" -> cCode == "VN" || cCode == "VNM" || fullText.contains("vietnam") || fullText.contains("вьетнам") || fullText.contains("da nang") || fullText.contains("дананг") || fullText.contains("phu quoc") || fullText.contains("фукуок") || fullText.contains("nha trang") || fullText.contains("нячанг") || fullText.contains("hoi an") || fullText.contains("vinpearl")
        "AE" -> cCode == "AE" || cCode == "ARE" || fullText.contains("dubai") || fullText.contains("дубай") || fullText.contains("bae") || fullText.contains("uae") || fullText.contains("оаэ") || fullText.contains("abu dhabi") || fullText.contains("абу-даби") || fullText.contains("sharjah") || fullText.contains("шарджа")
        "RU" -> cCode == "RU" || cCode == "RUS" || fullText.contains("rusya") || fullText.contains("russia") || fullText.contains("россия") || fullText.contains("moskova") || fullText.contains("moscow") || fullText.contains("москва") || fullText.contains("sochi") || fullText.contains("сочи") || fullText.contains("petersburg") || fullText.contains("петербург") || fullText.contains("казань") || fullText.contains("калининград")
        "MV" -> cCode == "MV" || cCode == "MDV" || fullText.contains("maldiv") || fullText.contains("maldive") || fullText.contains("мальдив") || fullText.contains("male") || fullText.contains("мале") || fullText.contains("atoll") || fullText.contains("атолл")
        "SC" -> cCode == "SC" || cCode == "SYC" || fullText.contains("seyşel") || fullText.contains("seychelles") || fullText.contains("сейшел") || fullText.contains("mahe") || fullText.contains("маэ") || fullText.contains("praslin") || fullText.contains("праслин")
        "LK" -> cCode == "LK" || cCode == "LKA" || fullText.contains("sri lanka") || fullText.contains("шри-ланка") || fullText.contains("шри ланка") || fullText.contains("colombo") || fullText.contains("коломбо") || fullText.contains("bentota") || fullText.contains("kandy")
        "MU" -> cCode == "MU" || cCode == "MUS" || fullText.contains("mauritius") || fullText.contains("маврикий") || fullText.contains("port louis") || fullText.contains("grand baie")
        "ID" -> cCode == "ID" || cCode == "IDN" || fullText.contains("bali") || fullText.contains("бали") || fullText.contains("endonezya") || fullText.contains("indonesia") || fullText.contains("индонезия") || fullText.contains("kuta") || fullText.contains("ubud") || fullText.contains("seminyak")
        "CY" -> cCode == "CY" || cCode == "CYP" || fullText.contains("kıbrıs") || fullText.contains("cyprus") || fullText.contains("кипр") || fullText.contains("girne") || fullText.contains("kyrenia") || fullText.contains("гирне") || fullText.contains("лефкоша") || fullText.contains("фамагуста")
        "GE" -> cCode == "GE" || cCode == "GEO" || fullText.contains("gürcistan") || fullText.contains("georgia") || fullText.contains("грузия") || fullText.contains("batum") || fullText.contains("batumi") || fullText.contains("батуми") || fullText.contains("tiflis") || fullText.contains("tbilisi") || fullText.contains("тбилиси")
        "ME" -> cCode == "ME" || cCode == "MNE" || fullText.contains("karadağ") || fullText.contains("montenegro") || fullText.contains("черногория") || fullText.contains("budva") || fullText.contains("будва") || fullText.contains("kotor") || fullText.contains("котор") || fullText.contains("tivat") || fullText.contains("тиват")
        "TZ" -> cCode == "TZ" || cCode == "TZA" || fullText.contains("zanzibar") || fullText.contains("занзибар") || fullText.contains("tanzanya") || fullText.contains("tanzania") || fullText.contains("танзания") || fullText.contains("nungwi") || fullText.contains("kendwa")
        "GR" -> cCode == "GR" || cCode == "GRC" || fullText.contains("yunanistan") || fullText.contains("greece") || fullText.contains("греция") || fullText.contains("girit") || fullText.contains("crete") || fullText.contains("крит") || fullText.contains("rodos") || fullText.contains("rhodes") || fullText.contains("родос")
        "CN" -> cCode == "CN" || cCode == "CHN" || fullText.contains("çin") || fullText.contains("china") || fullText.contains("китай") || fullText.contains("hainan") || fullText.contains("хайнань") || fullText.contains("sanya") || fullText.contains("санья") || fullText.contains("pekin") || fullText.contains("beijing") || fullText.contains("пекин")
        "AB" -> cCode == "AB" || cCode == "ABH" || fullText.contains("abhazya") || fullText.contains("abkhazia") || fullText.contains("абхазия") || fullText.contains("gagra") || fullText.contains("гагра") || fullText.contains("pitsunda") || fullText.contains("пицунда") || fullText.contains("гудаут") || fullText.contains("сухум")
        else -> cCode == selectedCode.uppercase().trim()
    }
}

fun PublicHotelOffer.toUnifiedProductEntity(): com.mgacreative.touros.data.database.entity.UnifiedProductEntity {
    return com.mgacreative.touros.data.database.entity.UnifiedProductEntity(
        id = this.id,
        hotelName = this.hotelName,
        tourName = if (this.category == "FLIGHT") "${this.hotelName} (${this.flightCode.replace("FL-TV", "Charter", ignoreCase = true).replace("Charter Airlines", "Charter", ignoreCase = true).replace("Charter Charter", "Charter", ignoreCase = true)})".trim() else "${this.hotelName} Tur Paketi",
        region = this.location.removeSuffix(", Türkiye").removeSuffix(", Turkey").removeSuffix(", Турция").trim(),
        country = this.countryCode.ifBlank { "Турция" },
        countryCode = this.countryCode,
        countryName = when (this.countryCode.uppercase()) {
            "TR" -> "Турция"
            "EG" -> "Mısır"
            "TH" -> "Tayland"
            "VN" -> "Vietnam"
            "AE" -> "BAE"
            "RU" -> "Rusya"
            "MV" -> "Maldivler"
            "SC" -> "Seyşeller"
            "LK" -> "Sri Lanka"
            "MU" -> "Mauritius"
            "ID" -> "Endonezya (Bali)"
            "CY" -> "Kıbrıs"
            "GE" -> "Gürcistan"
            "ME" -> "Karadağ"
            "TZ" -> "Zanzibar"
            "GR" -> "Yunanistan"
            "CN" -> "Çin"
            "AB" -> "Abhazya"
            else -> this.countryCode
        },
        price = this.minPrice,
        currency = this.currency.ifBlank { "RUB" },
        nights = this.nights,
        mealType = this.mealType,
        roomType = this.roomType,
        flightNumber = this.flightCode,
        hotelCategory = this.stars,
        operatorName = this.operatorName,
        pictureUrl = this.imageUrl,
        productType = this.category
    )
}

fun com.mgacreative.touros.data.database.entity.UnifiedProductEntity.toPublicHotelOffer(): PublicHotelOffer {
    val baseP = this.safePrice.coerceAtLeast(100.0)
    val rawOp = this.safeOperatorName.ifBlank { "TourVisor" }
    val canonicalOp = TourOperatorConfig.resolveCanonicalOperatorName(rawOp)
    val opName = canonicalOp ?: rawOp
    val rType = this.safeRoomType.ifBlank { "standard room" }
    val mType = this.safeMealType.ifBlank { "Bez pitaniya" }
    val fCode = if (this.flightNumber.isNotBlank() || this.airlineName.isNotBlank()) {
        val aName = this.airlineName
            .replace("Charter Airlines", "Charter", ignoreCase = true)
            .trim()
        val fNum = this.flightNumber
            .replace("FL-TV", "Charter", ignoreCase = true)
            .replace("Charter Airlines", "Charter", ignoreCase = true)
            .trim()
        when {
            aName.equals("Charter", ignoreCase = true) && (fNum.equals("Charter", ignoreCase = true) || fNum.isBlank()) -> "Charter"
            fNum.equals("Charter", ignoreCase = true) && aName.isBlank() -> "Charter"
            aName.isNotBlank() && fNum.isNotBlank() && !aName.equals(fNum, ignoreCase = true) -> {
                if (fNum.equals("Charter", ignoreCase = true)) aName
                else "$aName $fNum"
            }
            aName.isNotBlank() -> aName
            else -> fNum
        }
    } else ""

    val rawType = this.safeProductType.uppercase()
    val isFlight = rawType == "FLIGHT" || rawType == "CHARTER" || rawType == "FLIGHT_ONLY" || 
                   this.safeTourName.startsWith("Uçuş:", ignoreCase = true) || 
                   this.safeHotelName.startsWith("Uçuş:", ignoreCase = true) || 
                   this.safeHotelName.startsWith("✈️", ignoreCase = true) ||
                   (this.safeHotelName.isBlank() && this.flightNumber.isNotBlank())
    val isHotelOnly = !isFlight && (rawType == "HOTEL" || rawType == "LOCAL_HOTEL" || this.safeOperatorName.contains("Yerel Otel", ignoreCase = true))
    val isPromo = this.safeIsPromo

    val mappedCat = when {
        isFlight -> "FLIGHT"
        isHotelOnly -> "HOTEL"
        else -> "PACKAGE_TOUR"
    }

    val cCode = when {
        this.safeCountryCode.isNotBlank() -> this.safeCountryCode.uppercase()
        this.safeCountry.contains("Mısır", ignoreCase = true) || this.safeCountry.contains("Egypt", ignoreCase = true) -> "EG"
        this.safeCountry.contains("Tayland", ignoreCase = true) || this.safeCountry.contains("Thailand", ignoreCase = true) -> "TH"
        this.safeCountry.contains("Vietnam", ignoreCase = true) -> "VN"
        this.safeCountry.contains("Dubai", ignoreCase = true) || this.safeCountry.contains("BAE", ignoreCase = true) || this.safeCountry.contains("UAE", ignoreCase = true) -> "AE"
        this.safeCountry.contains("Rusya", ignoreCase = true) || this.safeCountry.contains("Russia", ignoreCase = true) -> "RU"
        this.safeCountry.contains("Maldiv", ignoreCase = true) -> "MV"
        this.safeCountry.contains("Seyşel", ignoreCase = true) || this.safeCountry.contains("Seychelles", ignoreCase = true) -> "SC"
        this.safeCountry.contains("Sri Lanka", ignoreCase = true) -> "LK"
        this.safeCountry.contains("Mauritius", ignoreCase = true) -> "MU"
        this.safeCountry.contains("Bali", ignoreCase = true) || this.safeCountry.contains("Endonezya", ignoreCase = true) -> "ID"
        this.safeCountry.contains("Kıbrıs", ignoreCase = true) || this.safeCountry.contains("Cyprus", ignoreCase = true) -> "CY"
        this.safeCountry.contains("Gürcistan", ignoreCase = true) || this.safeCountry.contains("Georgia", ignoreCase = true) -> "GE"
        this.safeCountry.contains("Karadağ", ignoreCase = true) || this.safeCountry.contains("Montenegro", ignoreCase = true) -> "ME"
        this.safeCountry.contains("Zanzibar", ignoreCase = true) || this.safeCountry.contains("Tanzanya", ignoreCase = true) -> "TZ"
        this.safeCountry.contains("Yunanistan", ignoreCase = true) || this.safeCountry.contains("Greece", ignoreCase = true) -> "GR"
        this.safeCountry.contains("Çin", ignoreCase = true) || this.safeCountry.contains("China", ignoreCase = true) -> "CN"
        this.safeCountry.contains("Abhazya", ignoreCase = true) -> "AB"
        else -> "TR"
    }

    val cCountryName = when (cCode) {
        "EG" -> "Mısır"
        "TH" -> "Tayland"
        "VN" -> "Vietnam"
        "AE" -> "BAE (Dubai)"
        "RU" -> "Rusya"
        "MV" -> "Maldivler"
        "SC" -> "Seyşeller"
        "LK" -> "Sri Lanka"
        "MU" -> "Mauritius"
        "ID" -> "Endonezya (Bali)"
        "CY" -> "Kıbrıs"
        "GE" -> "Gürcistan"
        "ME" -> "Karadağ"
        "TZ" -> "Zanzibar"
        "GR" -> "Yunanistan"
        "CN" -> "Çin"
        "AB" -> "Abhazya"
        else -> "Турция"
    }

    val overrideP = this.customPriceOverride
    val calcDiscount = if (overrideP != null && overrideP > baseP) {
        (((overrideP - baseP) / overrideP) * 100).toInt()
    } else null

    val cleanHotelName = this.safeHotelName.removePrefix("Uçuş: ").removePrefix("Ucus: ").trim()
    val reg = this.safeRegion.trim()
    val subReg = this.safeSubRegion.trim()
    val locParts = listOf(reg, subReg, cCountryName).filter { it.isNotBlank() }.distinct()
    val finalLocation = if (locParts.isNotEmpty()) locParts.joinToString(", ") else "Antalya, $cCountryName"

    val calculatedReturnDate = this.departureDate?.let { dep ->
        try {
            val n = if (this.nights > 0) this.nights else 7
            if (dep.contains("-")) {
                val parts = dep.split("-")
                if (parts.size == 3) {
                    val y = parts[0].toIntOrNull() ?: 2026
                    val m = parts[1].toIntOrNull() ?: 9
                    val d = parts[2].toIntOrNull() ?: 2
                    val (rd, rm, ry) = com.mgacreative.touros.addDaysToTriple(Triple(d, m, y), n)
                    "${rd.toString().padStart(2, '0')}.${rm.toString().padStart(2, '0')}.$ry"
                } else null
            } else if (dep.contains(".")) {
                val parts = dep.split(".")
                if (parts.size == 3) {
                    val d = parts[0].toIntOrNull() ?: 2
                    val m = parts[1].toIntOrNull() ?: 9
                    val y = parts[2].toIntOrNull() ?: 2026
                    val (rd, rm, ry) = com.mgacreative.touros.addDaysToTriple(Triple(d, m, y), n)
                    "${rd.toString().padStart(2, '0')}.${rm.toString().padStart(2, '0')}.$ry"
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    return PublicHotelOffer(
        id = this.id,
        hotelName = cleanHotelName.ifBlank { this.safeTourName.removePrefix("Uçuş: ").removePrefix("Ucus: ").trim().ifBlank { if (isFlight) "✈️ Charter Uçuş Seferi (${fCode})" else "Tur Operatörü Ürünü" } },
        location = finalLocation,
        stars = if (this.safeHotelCategory > 0) this.safeHotelCategory else 5,
        description = (this.safeHotelName.ifBlank { this.safeTourName }) + " - Operatör: " + opName,
        minPrice = baseP,
        maxPrice = overrideP ?: baseP,
        imageUrl = this.safePictureUrl,
        operatorName = opName,
        roomType = rType,
        mealType = mType,
        flightCode = fCode,
        nights = if (this.nights > 0) this.nights else 7,
        currency = this.safeCurrency.ifBlank { "RUB" },
        category = mappedCat,
        discountPercent = calcDiscount,
        isLastMinute = isPromo,
        baggageKg = this.safeBaggageKg,
        departureCity = this.safeDepartureCity,
        countryCode = cCode,
        departureDate = this.departureDate ?: "",
        returnDate = calculatedReturnDate,
        agencyPrices = listOf(
            AgencyPriceOption(
                agencyId = "AGN-${this.operatorId}",
                agencyName = opName,
                operatorName = opName,
                roomType = rType,
                boardType = mType,
                price = baseP,
                isBestDeal = true,
                nights = if (this.nights > 0) this.nights else 7,
                departureDate = this.departureDate ?: "",
                returnDate = calculatedReturnDate ?: ""
            )
        )
    )
}

fun getOptimizedImageUrl(rawUrl: String, width: Int = 600, quality: Int = 80): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=$width&q=$quality"
    
    return when {
        trimmed.contains("unsplash.com") -> {
            val baseUrl = trimmed.substringBefore("?")
            "$baseUrl?auto=format&fit=crop&w=$width&q=$quality"
        }
        trimmed.contains("supabase.co/storage") -> {
            if (trimmed.contains("?")) "$trimmed&width=$width&quality=$quality"
            else "$trimmed?width=$width&quality=$quality"
        }
        else -> trimmed
    }
}

fun getEffectiveImageUrl(hotel: PublicHotelOffer): String {
    val raw = hotel.imageUrl.trim()
    if (raw.isNotBlank()) return getOptimizedImageUrl(raw)

    val isFlight = hotel.category.uppercase() == "FLIGHT" || 
                   hotel.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                   hotel.hotelName.startsWith("✈️", ignoreCase = true)
    if (isFlight) {
        val fCode = hotel.flightCode.uppercase()
        return when {
            fCode.startsWith("TK") || fCode.contains("TURKISH") -> "https://images.unsplash.com/photo-1542296332-2e4473faf563?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("PC") || fCode.contains("PEGASUS") -> "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("XQ") || fCode.contains("SUNEXPRESS") -> "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("SU") || fCode.contains("AEROFLOT") -> "https://images.unsplash.com/photo-1517999144091-3d9dca6d1e43?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("N4") || fCode.contains("NORDWIND") -> "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("ZF") || fCode.contains("AZUR") -> "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("XC") || fCode.contains("CORENDON") -> "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("EK") || fCode.contains("EMIRATES") -> "https://images.unsplash.com/photo-1520437358207-323b43b50729?w=800&auto=format&fit=crop&q=80"
            fCode.startsWith("QR") || fCode.contains("QATAR") -> "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&auto=format&fit=crop&q=80"
            else -> "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=800&q=80"
        }
    }
    
    val name = hotel.hotelName.lowercase()
    val loc = hotel.location.lowercase()
    return when {
        loc.contains("mısır") || loc.contains("egypt") || loc.contains("şarm") || loc.contains("hurgada") -> 
            "https://images.unsplash.com/photo-1539768942893-daf53e448371?auto=format&fit=crop&w=800&q=80"
        loc.contains("tayland") || loc.contains("thailand") || loc.contains("phuket") || loc.contains("pattaya") -> 
            "https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?auto=format&fit=crop&w=800&q=80"
        loc.contains("vietnam") || loc.contains("phu quoc") || loc.contains("da nang") -> 
            "https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=800&q=80"
        loc.contains("dubai") || loc.contains("bae") || loc.contains("abu dhabi") -> 
            "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=800&q=80"
        loc.contains("rusya") || loc.contains("moskova") || loc.contains("sochi") -> 
            "https://images.unsplash.com/photo-1513326738677-b964603b136d?auto=format&fit=crop&w=800&q=80"
        name.contains("rixos") -> 
            "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80"
        name.contains("bodrum") || name.contains("lujo") -> 
            "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=800&q=80"
        name.contains("kemer") || name.contains("maxx") -> 
            "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80"
        else -> 
            "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80"
    }
}

data class AgencyPriceOption(
    val agencyId: String,
    val agencyName: String,
    val operatorName: String,
    val roomType: String,
    val boardType: String,
    val price: Double,
    val isBestDeal: Boolean = false,
    val nights: Int = 0,
    val departureDate: String = "",
    val returnDate: String = ""
)

fun getInitialDefaultOffers(): List<PublicHotelOffer> {
    return emptyList()
}

@Composable
fun AxiletoLogoText(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 34.dp,
    aFontSize: androidx.compose.ui.unit.TextUnit = 30.sp,
    xiletoFontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    color: Color = Color.White
) {
    androidx.compose.foundation.Image(
        painter = org.jetbrains.compose.resources.painterResource(touros.shared.generated.resources.Res.drawable.axileto_logo_white),
        contentDescription = "Axileto Logo",
        modifier = modifier.height(height),
        contentScale = ContentScale.Fit
    )
}

val ALL_WORLD_COUNTRIES = listOf(
    // 1. Popüler & Akdeniz
    Triple("TR", "Турция", "🇹🇷") to "POPULAR",
    Triple("EG", "Mısır", "🇪🇬") to "POPULAR",
    Triple("AE", "BAE (Dubai)", "🇦🇪") to "POPULAR",
    Triple("TH", "Tayland", "🇹🇭") to "POPULAR",
    Triple("RU", "Rusya", "🇷🇺") to "POPULAR",
    Triple("MV", "Maldivler", "🇲🇻") to "POPULAR",
    Triple("CY", "Kıbrıs", "🇨🇾") to "POPULAR",
    Triple("GE", "Gürcistan", "🇬🇪") to "POPULAR",

    // 2. Tropik & Egzotik Adalar
    Triple("SC", "Seyşeller", "🇸🇨") to "TROPICAL",
    Triple("LK", "Sri Lanka", "🇱🇰") to "TROPICAL",
    Triple("MU", "Mauritius", "🇲🇺") to "TROPICAL",
    Triple("ID", "Endonezya (Bali)", "🇮🇩") to "TROPICAL",
    Triple("VN", "Vietnam", "🇻🇳") to "TROPICAL",
    Triple("TZ", "Zanzibar", "🇹🇿") to "TROPICAL",

    // 3. Avrupa & Akdeniz
    Triple("ME", "Karadağ", "🇲🇪") to "EUROPE",
    Triple("GR", "Yunanistan", "🇬🇷") to "EUROPE",

    // 4. Asya & Kafkaslar
    Triple("CN", "Çin", "🇨🇳") to "ASIA",
    Triple("AB", "Abhazya", "🇬🇪") to "ASIA"
)

@kotlinx.serialization.Serializable
private data class GlobalWebAllowedModulesDto(
    val allowed_modules: List<String> = emptyList()
)

@kotlinx.serialization.Serializable
private data class WebB2BAgencyRpcResult(
    val company_id: String,
    val agency_name: String,
    val logo_url: String? = null,
    val operator_code: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val whatsapp: String? = null,
    val address: String? = null,
    val is_authorized: Boolean = false
)

@Composable
fun GlobalWebPublicScreen(
    referralCode: String? = null,
    initialSearchQuery: String? = null,
    onNavigateToB2BSearch: () -> Unit = {},
    onNavigateToBookingDetail: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAdminCms: () -> Unit = {},
    onNavigateToNewBooking: (PublicHotelOffer) -> Unit = {},
    onNavigateToClub: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val mainLazyListState = rememberLazyListState()
    val authRepository: AuthRepository = koinInject()
    val bookingRepository: BookingRepository = koinInject()
    val companySettingsRepository: CompanySettingsRepository = koinInject()
    val b2bTourSearchViewModel: com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel = koinInject()
    val supabaseClient: io.github.jan.supabase.SupabaseClient = koinInject()

    val currentUser by authRepository.observeAuthState().collectAsState()
    val currentLang by AppLanguageManager.currentLanguage.collectAsState()
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var companySettings by remember { mutableStateOf<CompanySettings?>(null) }

    val webAgencyParam = remember { com.mgacreative.touros.getWebAgencyIdentifier() ?: referralCode }
    var isWhitelabelAgencyMode by remember { mutableStateOf(false) }
    var isAgencyWebAuthorized by remember { mutableStateOf(true) }
    var resolvedAgencyId by remember { mutableStateOf<String?>(null) }
    var whitelabelAgencyName by remember { mutableStateOf<String?>(null) }
    var whitelabelLogoUrl by remember { mutableStateOf<String?>(null) }
    var whitelabelPhone by remember { mutableStateOf<String?>(null) }
    var whitelabelEmail by remember { mutableStateOf<String?>(null) }
    var whitelabelWhatsapp by remember { mutableStateOf<String?>(null) }
    var whitelabelAddress by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.tenantId, webAgencyParam) {
        val identifier = webAgencyParam?.trim().takeIf { !it.isNullOrBlank() }
        if (!identifier.isNullOrBlank()) {
            try {
                val params = buildJsonObject {
                    put("p_identifier", identifier)
                }
                val rpcList = supabaseClient.postgrest.rpc(
                    "get_web_b2b_agency",
                    params
                ).decodeAs<List<WebB2BAgencyRpcResult>>()
                val rpcResult = rpcList.firstOrNull()

                if (rpcResult != null) {
                    resolvedAgencyId = rpcResult.company_id
                    if (rpcResult.is_authorized) {
                        isAgencyWebAuthorized = true
                        isWhitelabelAgencyMode = true
                        whitelabelAgencyName = rpcResult.agency_name
                        whitelabelLogoUrl = rpcResult.logo_url
                        whitelabelPhone = rpcResult.phone
                        whitelabelEmail = rpcResult.email
                        whitelabelWhatsapp = rpcResult.whatsapp
                        whitelabelAddress = rpcResult.address
                    } else {
                        isAgencyWebAuthorized = false
                        isWhitelabelAgencyMode = false
                    }
                } else {
                    isAgencyWebAuthorized = true
                    isWhitelabelAgencyMode = false
                    resolvedAgencyId = "00000000-0000-0000-0000-000000000001"
                }
            } catch (e: Exception) {
                isWhitelabelAgencyMode = false
                isAgencyWebAuthorized = true
                resolvedAgencyId = "00000000-0000-0000-0000-000000000001"
            }
        } else {
            isWhitelabelAgencyMode = false
            isAgencyWebAuthorized = true
            resolvedAgencyId = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
        }

        val targetCompanyId = resolvedAgencyId ?: "00000000-0000-0000-0000-000000000001"
        companySettings = companySettingsRepository.getCompanySettings(targetCompanyId).getOrNull()
    }

    LaunchedEffect(whitelabelAgencyName, isWhitelabelAgencyMode) {
        if (isWhitelabelAgencyMode) {
            val title = (whitelabelAgencyName?.takeIf { it.isNotBlank() } ?: "Online Rezervasyon Portalı") + " — Rezervasyon"
            com.mgacreative.touros.setWebDocumentTitle(title)
        }
    }

    // Filtreleme State'leri
    var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery ?: "") }

    LaunchedEffect(initialSearchQuery) {
        if (!initialSearchQuery.isNullOrBlank()) {
            searchQuery = initialSearchQuery
            coroutineScope.launch {
                try {
                    mainLazyListState.animateScrollToItem(1)
                } catch (_: Exception) {}
            }
        }
    }
    var selectedSearchCategoryTab by remember { mutableStateOf("ALL") } // "ALL", "PACKAGE_TOUR", "HOTEL", "FLIGHT", "LAST_MINUTE"
    var selectedDestinationFilter by remember { mutableStateOf("Tüm Destinasyonlar") }
    var selectedStars by remember { mutableStateOf(emptySet<Int>()) }
    var maxPriceFilter by remember { mutableStateOf(200000f) }
    var selectedOperatorFilters by remember { mutableStateOf(emptySet<String>()) }

    // Seçili Otel Detay Modalı
    var selectedHotelForDetail by remember { mutableStateOf<PublicHotelOffer?>(null) }
    var selectedAgencyForBooking by remember { mutableStateOf<AgencyPriceOption?>(null) }
    var isInlineSearchActive by remember { mutableStateOf(false) }
    var searchResultsList by remember { mutableStateOf<List<PublicHotelOffer>>(emptyList()) }

    val b2bUiState by b2bTourSearchViewModel.uiState.collectAsState()
    val b2bSearchResults = remember(b2bUiState) {
        when (val state = b2bUiState) {
            is com.mgacreative.touros.ui.viewmodel.B2BTourSearchUiState.Success -> {
                val rawOffers = state.filteredProducts.map { it.toPublicHotelOffer() }
                groupOffersByHotelName(rawOffers)
            }
            else -> emptyList()
        }
    }

    val handleDirectBooking: (PublicHotelOffer) -> Unit = { offer ->
        val productEntity = offer.toUnifiedProductEntity()
        b2bTourSearchViewModel.selectProductForBooking(productEntity)
        selectedHotelForDetail = offer
        selectedAgencyForBooking = if (isWhitelabelAgencyMode && !resolvedAgencyId.isNullOrBlank()) {
            AgencyPriceOption(
                agencyId = resolvedAgencyId!!,
                agencyName = (whitelabelAgencyName ?: companySettings?.name)?.ifBlank { "Yetkili Acente" } ?: "Yetkili Acente",
                operatorName = offer.operatorName.ifBlank { "MGA Creative" },
                roomType = offer.roomType,
                boardType = offer.mealType,
                price = offer.minPrice,
                isBestDeal = true
            )
        } else {
            offer.agencyPrices.firstOrNull() ?: AgencyPriceOption(
                agencyId = "00000000-0000-0000-0000-000000000001",
                agencyName = "${offer.operatorName.ifBlank { "TourOS Partner" }} Acente",
                operatorName = offer.operatorName.ifBlank { "MGA Creative" },
                roomType = offer.roomType,
                boardType = offer.mealType,
                price = offer.minPrice,
                isBestDeal = true
            )
        }
    }

    // Hero Arama Barı Form State'leri (Başlangıçta Temiz / Boş Gelir)
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()
    var departureCity by remember { mutableStateOf("") }
    var destinationCity by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf(com.mgacreative.touros.utils.DateUtils.getTodayDot()) }
    var endDateText by remember { mutableStateOf(com.mgacreative.touros.utils.DateUtils.getFutureDot(7)) }
    var returnStartDateText by remember { mutableStateOf(com.mgacreative.touros.utils.DateUtils.getFutureDot(7)) }
    var returnEndDateText by remember { mutableStateOf(com.mgacreative.touros.utils.DateUtils.getFutureDot(14)) }
    var selectedNightsText by remember { mutableStateOf("7 - 10 Gece") }
    var selectedTouristsText by remember { mutableStateOf("2 Yetişkin · 1 Oda") }
    var adultsCount by remember { mutableStateOf(2) }
    var childrenCount by remember { mutableStateOf(0) }
    var roomsCount by remember { mutableStateOf(1) }
    var childrenAges by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedCountryFilter by remember { mutableStateOf("Турция") }
    var selectedCountryTab by remember { mutableStateOf("ALL") }
    var selectedSubRegionFilter by remember { mutableStateOf<String?>(null) }
    var activeCountryDetailPage by remember { mutableStateOf<String?>(null) }
    var countryDedicatedSubRegion by remember { mutableStateOf("Tümü") }
    var countryDedicatedHotelQuery by remember { mutableStateOf("") }
    var countryDedicatedNights by remember { mutableStateOf("7 Gece") }
    var countryDedicatedStars by remember { mutableStateOf(setOf(4, 5)) }
    var flightTripType by remember { mutableStateOf("ONE_WAY") } // "ONE_WAY" veya "ROUND_TRIP"

    var showDepartureDropdown by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showDepartureRangePicker by remember { mutableStateOf(false) }
    var showReturnRangePicker by remember { mutableStateOf(false) }
    var showNightsDropdown by remember { mutableStateOf(false) }
    var showGuestRoomModal by remember { mutableStateOf(false) }
    var showHierarchicalDestModal by remember { mutableStateOf(false) }
    var showRussianDepartureModal by remember { mutableStateOf(false) }
    var bookingSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showPopularCountriesModal by remember { mutableStateOf(false) }
    
    // Detaylı Filtre State'leri (B2B ile Birebir Ortak)
    var isDetailFilterExpanded by remember { mutableStateOf(false) }
    var selectedBeachLine by remember { mutableStateOf(0) }
    var selectedMealTypes by remember { mutableStateOf(emptySet<String>()) }
    var minRating by remember { mutableStateOf(0.0) }
    var selectedHotels by remember { mutableStateOf(emptySet<String>()) }
    var selectedAmenities by remember { mutableStateOf(setOf<String>()) }
    var isInstantOnly by remember { mutableStateOf(false) }
    var isDirectFlightOnly by remember { mutableStateOf(false) }
    var isTransferIncludedOnly by remember { mutableStateOf(false) }

    // Dynamic Database Products State (Varsayılan içerikle anında başlatılır)
    var dbProducts by remember { mutableStateOf<List<PublicHotelOffer>>(getInitialDefaultOffers()) }
    var fastDealsFromDb by remember { mutableStateOf<List<PublicHotelOffer>>(emptyList()) }
    var isLoadingProducts by remember { mutableStateOf(true) }

    val dynamicOperators = remember {
        TourOperatorConfig.ALLOWED_OPERATOR_NAMES
    }

    val isFlightTab = selectedSearchCategoryTab == "FLIGHT"

    // Veritabanındaki Uçuş Envanterinden Dinamik Kalkış ve Varış Noktaları Çıkarımı
    val flightInventoryOffers = remember(dbProducts) {
        dbProducts.filter { 
            it.category.uppercase() == "FLIGHT" || 
            it.hotelName.startsWith("✈️", ignoreCase = true) || 
            it.hotelName.startsWith("Uçuş:", ignoreCase = true) ||
            it.flightCode.isNotBlank()
        }
    }

    val (flightOriginCodes, flightOriginNames, flightDestCodes, flightDestNames) = remember(flightInventoryOffers) {
        val originCodes = mutableSetOf<String>()
        val originNames = mutableSetOf<String>()
        val destCodes = mutableSetOf<String>()
        val destNames = mutableSetOf<String>()

        flightInventoryOffers.forEach { offer ->
            val fCode = offer.flightCode.substringBefore("(").trim()
            if (fCode.contains("-")) {
                val parts = fCode.split("-").map { it.trim().uppercase() }
                if (parts.size >= 2) {
                    originCodes.add(parts[0])
                    destCodes.add(parts[1])
                }
            }
            val loc = offer.location.lowercase()
            val hName = offer.hotelName.lowercase()
            val combined = "$loc $hName ${offer.description.lowercase()}"

            if (combined.contains("antalya") || combined.contains("belek") || combined.contains("kemer") || combined.contains("side") || combined.contains("lara")) {
                destCodes.add("AYT"); destNames.add("Antalya")
            }
            if (combined.contains("alanya")) { destCodes.add("GZP"); destNames.add("Alanya") }
            if (combined.contains("bodrum")) { destCodes.add("BJV"); destNames.add("Bodrum") }
            if (combined.contains("marmaris") || combined.contains("fethiye") || combined.contains("dalaman")) { destCodes.add("DLM"); destNames.add("Muğla") }
            if (combined.contains("izmir") || combined.contains("çeşme")) { destCodes.add("ADB"); destNames.add("İzmir") }
            if (combined.contains("istanbul")) { destCodes.add("IST"); destCodes.add("SAW"); destNames.add("İstanbul") }
            if (combined.contains("dubai")) { destCodes.add("DXB"); destNames.add("Dubai") }
            if (combined.contains("abu dhabi")) { destCodes.add("AUH"); destNames.add("Abu Dhabi") }
            if (combined.contains("hurgada") || combined.contains("hurghada")) { destCodes.add("HRG"); destNames.add("Hurgada") }
            if (combined.contains("şarm") || combined.contains("sharm")) { destCodes.add("SSH"); destNames.add("Şarm") }
            if (combined.contains("phuket")) { destCodes.add("HKT"); destNames.add("Phuket") }
            if (combined.contains("bangkok")) { destCodes.add("BKK"); destNames.add("Bangkok") }
            if (combined.contains("da nang") || combined.contains("danang")) { destCodes.add("DAD"); destNames.add("Da Nang") }
            if (combined.contains("phu quoc")) { destCodes.add("PQC"); destNames.add("Phu Quoc") }
            if (combined.contains("sochi") || combined.contains("adler")) { destCodes.add("AER"); destNames.add("Sochi") }
            if (combined.contains("moskova") || combined.contains("moscow") || combined.contains("vko") || combined.contains("svo") || combined.contains("dme")) {
                originCodes.add("MOW"); originCodes.add("VKO"); originCodes.add("SVO"); originCodes.add("DME"); originNames.add("Moskova"); originNames.add("Москва")
            }
            if (combined.contains("saint petersburg") || combined.contains("st. petersburg") || combined.contains("led") || combined.contains("санкт-петербург")) {
                originCodes.add("LED"); originNames.add("St. Petersburg"); originNames.add("Санкт-Петербург")
            }
            if (combined.contains("kazan") || combined.contains("казань") || combined.contains("kzn")) {
                originCodes.add("KZN"); originNames.add("Kazan"); originNames.add("Казань")
            }
            if (combined.contains("yekaterinburg") || combined.contains("екатеринбург") || combined.contains("svx")) {
                originCodes.add("SVX"); originNames.add("Yekaterinburg"); originNames.add("Екатеринбург")
            }
        }

        if (originCodes.isEmpty()) {
            originCodes.addAll(listOf("MOW", "VKO", "SVO", "DME", "LED", "KZN", "SVX", "IST"))
        }
        if (destCodes.isEmpty()) {
            destCodes.addAll(listOf("AYT", "BJV", "DLM", "DXB", "SSH", "HRG", "HKT"))
        }

        listOf(originCodes, originNames, destCodes, destNames)
    }

    if (showRussianDepartureModal) {
        com.mgacreative.touros.ui.components.RussianDepartureCityPickerDialog(
            currentSelection = departureCity,
            allowedAirportCodes = if (isFlightTab) flightOriginCodes else null,
            allowedCityNames = if (isFlightTab) flightOriginNames else null,
            customTitle = if (isFlightTab) "✈️ UÇUŞ KALKIŞ NOKTASI / ГОРОД ВЫЛЕТА" else null,
            onCitySelected = { city ->
                departureCity = "${city.nameRu} (${city.airportCode})"
            },
            onDismiss = { showRussianDepartureModal = false }
        )
    }

    if (showHierarchicalDestModal) {
        com.mgacreative.touros.ui.components.HierarchicalDestinationPickerDialog(
            currentSelection = destinationCity,
            allowedAirportCodes = if (isFlightTab) flightDestCodes else null,
            allowedDestinationNames = if (isFlightTab) flightDestNames else null,
            onlyAirports = isFlightTab,
            customTitle = if (isFlightTab) "✈️ UÇUŞ VARIŞ HAVALİMANI / АЭРОПОРТ НАЗНАЧЕНИЯ" else null,
            onDestinationSelected = { destItem ->
                destinationCity = if (isFlightTab && destItem.airportCode != null) {
                    "${destItem.name.substringBefore(" Havalimanı").substringBefore(" Uluslararası")} (${destItem.airportCode})"
                } else if (destItem.nameRu.isNotBlank()) {
                    if (AppLanguageManager.currentLanguage.value.code == "ru") destItem.nameRu
                    else "${destItem.name} (${destItem.nameRu})"
                } else {
                    destItem.name
                }
                selectedDestinationFilter = destItem.name
                selectedCountryFilter = destItem.countryName
            },
            onDismiss = { showHierarchicalDestModal = false }
        )
    }

    if (showDepartureRangePicker) {
        B2BDateRangePickerDialog(
            initialStartDateText = startDateText,
            initialEndDateText = endDateText,
            title = "🗓️ Gidiş Tarih Aralığı Seçin",
            onDateRangeSelected = { sDate, eDate ->
                startDateText = sDate
                endDateText = eDate
            },
            onDismissRequest = { showDepartureRangePicker = false }
        )
    }

    if (showReturnRangePicker) {
        B2BDateRangePickerDialog(
            initialStartDateText = returnStartDateText,
            initialEndDateText = returnEndDateText,
            title = "🗓️ Dönüş Tarih Aralığı Seçin",
            onDateRangeSelected = { sDate, eDate ->
                returnStartDateText = sDate
                returnEndDateText = eDate
            },
            onDismissRequest = { showReturnRangePicker = false }
        )
    }

    if (showStartDatePicker) {
        ModernDatePickerDialog(
            initialDateText = startDateText,
            title = "Gidiş Başlangıç Tarihi Seçin",
            onDateSelected = { startDateText = it },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        ModernDatePickerDialog(
            initialDateText = endDateText,
            title = "Gidiş Bitiş Tarihi Seçin",
            onDateSelected = { endDateText = it },
            onDismiss = { showEndDatePicker = false }
        )
    }

    if (showGuestRoomModal) {
        ModernGuestRoomSelectorDialog(
            initialAdults = adultsCount,
            initialChildren = childrenCount,
            initialRooms = roomsCount,
            initialChildrenAges = childrenAges,
            onApply = { a, c, r, ages, summary ->
                adultsCount = a
                childrenCount = c
                roomsCount = r
                childrenAges = ages
                selectedTouristsText = summary
            },
            onDismiss = { showGuestRoomModal = false }
        )
    }

    if (showPopularCountriesModal) {
        PopularCountriesDiscoveryDialog(
            allWorldCountries = ALL_WORLD_COUNTRIES,
            dbProducts = dbProducts,
            supabaseClient = supabaseClient,
            onHotelClick = { selectedHotelForDetail = it },
            onSelectAndBook = handleDirectBooking,
            onDismiss = { showPopularCountriesModal = false }
        )
    }

    // Promosyon Kartı Slider State'i (Otomatik değişim kaldırıldı, sadece oklar/noktalar ile manuel geçiş)
    var currentPromoSlideIndex by remember { mutableStateOf(0) }
    val promoBannersList = remember(companySettings) {
        companySettings?.getEffectivePromoBanners() ?: emptyList()
    }

    // Turist / Acente Modu & Login Modalı State'leri
    var userMode by remember { mutableStateOf("Turist") }
    var agencyActiveTab by remember { mutableStateOf("CATALOG") } // "CATALOG", "BOOKINGS", "COMMISSIONS", "REPORTS", "SETTINGS"
    var agencyBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoadingAgencyBookings by remember { mutableStateOf(false) }

    LaunchedEffect(userMode, agencyActiveTab) {
        if (userMode == "Acente" && agencyActiveTab == "BOOKINGS") {
            isLoadingAgencyBookings = true
            val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
            agencyBookings = bookingRepository.getBookings(tid).getOrDefault(emptyList())
            isLoadingAgencyBookings = false
        }
    }
    var showAgencyLoginModal by remember { mutableStateOf(false) }
    var agencyEmailInput by remember { mutableStateOf("") }
    var agencyPasswordInput by remember { mutableStateOf("") }
    var agencyCodeInput by remember { mutableStateOf(referralCode ?: "AGN-MASTER-8492") }
    var rememberAgencyCredentials by remember { mutableStateOf(false) }
    var agencyLoginError by remember { mutableStateOf<String?>(null) }

    // SADECE "Toplu Veri Yükle" ile yüklenen Tur Operatörü ürünlerini (marketplace_products) çek
    LaunchedEffect(Unit) {
        val savedAuth = com.mgacreative.touros.utils.LocalAuthStorage.loadCredentials()
        if (savedAuth != null && savedAuth.rememberMe) {
            agencyEmailInput = savedAuth.email
            agencyPasswordInput = savedAuth.password
            if (savedAuth.agencyCode.isNotBlank()) {
                agencyCodeInput = savedAuth.agencyCode
            }
            rememberAgencyCredentials = true
        }

        isLoadingProducts = true
        runCatching {
            companySettings = companySettingsRepository.getCompanySettings(currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001").getOrNull()
        }
        val offers = mutableListOf<PublicHotelOffer>()

        // 0. Hızlı Fırsatlar (Yandex Cloud view_fast_deals_distinct_countries: Rusya hariç, her ülkeden en ucuz 1, 1 hafta sonrası)
        runCatching {
            supabaseClient.postgrest["view_fast_deals_distinct_countries"]
                .select()
                .decodeList<com.mgacreative.touros.data.database.entity.UnifiedProductEntity>()
        }.onSuccess { fastList ->
            val deals = fastList.filter { it.id.isNotBlank() }.map { it.toPublicHotelOffer() }
            if (deals.isNotEmpty()) {
                fastDealsFromDb = deals
                offers.addAll(deals)
            }
        }

        // 1. Supabase 'marketplace_products' tablosundan yüklenen ürünleri çek (En uygun fiyatlı paket turlar ve uçuşlar)
        val minProductDate = com.mgacreative.touros.utils.DateUtils.getFutureIso(7)
        runCatching {
            supabaseClient.postgrest["marketplace_products"]
                .select {
                    filter {
                        eq("product_type", "PACKAGE_TOUR")
                        gte("departure_date", minProductDate)
                    }
                    order("price", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    limit(500)
                }
                .decodeList<com.mgacreative.touros.data.database.entity.UnifiedProductEntity>()
        }.onSuccess { list ->
            list.filter { it.id.isNotBlank() && TourOperatorConfig.isAllowedOperator(it.operatorName) }.forEach { p ->
                offers.add(p.toPublicHotelOffer())
            }
        }

        runCatching {
            supabaseClient.postgrest["marketplace_products"]
                .select {
                    filter {
                        eq("product_type", "FLIGHT")
                    }
                    range(0, 300)
                }
                .decodeList<com.mgacreative.touros.data.database.entity.UnifiedProductEntity>()
        }.onSuccess { list ->
            list.filter { it.id.isNotBlank() }.forEach { p ->
                offers.add(p.toPublicHotelOffer())
            }
        }

        // 2. RAM'deki yüklenen operatör ürünlerini de ekle
        val memoryList = com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel.getPersistentProducts()
        memoryList.filter { it.id.isNotBlank() }.forEach { p ->
            offers.add(p.toPublicHotelOffer())
        }

        // 3. Teklifleri birleştir (Yandex DB marketplace_products + yerel ürünler)
        dbProducts = groupOffersByHotelName(offers.distinctBy { it.id })
        isLoadingProducts = false
    }

    val filteredHotels = dbProducts.filter { h ->
        val isPureFlight = h.category.uppercase() == "FLIGHT" || 
                           h.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                           h.hotelName.startsWith("✈️", ignoreCase = true)
        val isAllowedOp = isPureFlight || TourOperatorConfig.isAllowedOperator(h.operatorName)
        if (!isAllowedOp) return@filter false

        val categoryMatch = when (selectedSearchCategoryTab) {
            "PACKAGE_TOUR" -> !isPureFlight && (h.category == "PACKAGE_TOUR" || h.category == "ALL")
            "HOTEL" -> !isPureFlight && (h.category == "HOTEL" || h.stars >= 4)
            "FLIGHT" -> isPureFlight
            "LAST_MINUTE" -> !isPureFlight && (h.isLastMinute || (h.discountPercent ?: 0) > 0)
            else -> !isPureFlight
        }

        val destinationToMatch = if (selectedDestinationFilter != "Tüm Destinasyonlar" && selectedDestinationFilter.isNotBlank()) {
            selectedDestinationFilter
        } else if (destinationCity.isNotBlank() && !destinationCity.contains("Tüm Destinasyonlar", ignoreCase = true)) {
            destinationCity
        } else {
            ""
        }

        val destMatch = destinationToMatch.isBlank() || com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel.isDestinationMatchingText(
            targetText = "${h.location} ${h.hotelName} ${h.description} ${h.countryCode} ${h.flightCode}",
            selectedDest = destinationToMatch
        )

        val depMatch = departureCity.isBlank() || departureCity.contains("Tüm", ignoreCase = true) || com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel.isDepartureMatchingText(
            targetDeparture = "${h.departureCity} ${h.flightCode} ${h.hotelName} ${h.location}",
            selectedDeparture = departureCity
        )

        // ✈️ UÇUŞ KESİN KURALLARI: Havaalanı olmayan yerlere uçuş gösterme & boş aramada sahte uçuş göstermeme
        val isFlightSelected = selectedSearchCategoryTab == "FLIGHT" || isPureFlight
        val isDepAll = departureCity.isBlank() || departureCity.equals("Tüm Kalkış Şehirleri", ignoreCase = true) || departureCity.equals("Все города", ignoreCase = true) || departureCity.equals("Все", ignoreCase = true) || departureCity.equals("ALL", ignoreCase = true)
        val isDestAll = destinationToMatch.isBlank() || destinationToMatch.equals("Tüm Destinasyonlar", ignoreCase = true) || destinationToMatch.equals("Все направления", ignoreCase = true) || destinationToMatch.equals("Все", ignoreCase = true) || destinationToMatch.equals("ALL", ignoreCase = true)

        val airportValidMatch = if (isFlightSelected) {
            if (!isDestAll && destinationToMatch.isNotBlank()) {
                com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel.hasAirport(destinationToMatch)
            } else if (selectedSearchCategoryTab == "FLIGHT") {
                !isDepAll || !isDestAll
            } else {
                true
            }
        } else {
            true
        }

        categoryMatch &&
        airportValidMatch &&
        destMatch &&
        depMatch &&
        (searchQuery.isBlank() || 
            h.hotelName.contains(searchQuery, ignoreCase = true) || 
            h.location.contains(searchQuery, ignoreCase = true) || 
            h.category.contains(searchQuery, ignoreCase = true) ||
            h.description.contains(searchQuery, ignoreCase = true) ||
            h.flightCode.contains(searchQuery, ignoreCase = true)) &&
        (selectedStars.isEmpty() || selectedStars.contains(h.stars)) &&
        (maxPriceFilter >= 200000f || h.minPrice <= maxPriceFilter) &&
        (selectedOperatorFilters.isEmpty() || selectedOperatorFilters.any { op -> 
            h.operatorName.equals(op, ignoreCase = true) || h.agencyPrices.any { ap -> ap.operatorName.equals(op, ignoreCase = true) } 
        })
    }

    // KURUMSAL AÇIK TEMA VE DÜZEN (Corporate Light Theme & Responsive Layout)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // ── ANA KANAL İÇERİĞİ (MAX-WIDTH CONTAINER İLE ORTALANMIŞ) ──────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                state = mainLazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
            ) {
                if (!isAgencyWebAuthorized && !webAgencyParam.isNullOrBlank()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    text = "Bu acentenin Web B2B müşteri rezervasyon sayfası henüz aktif edilmemiştir.",
                                    style = TourOSTypography.BodyMedium.copy(color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                // ── 1. ÜST BANT (NAVBAR - KURUMSAL LACİVERT TEMA / USER FONKSİYONLARI) ─────────
                item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0A2540), // Kurumsal Derin Lacivert Tema
                    shadowElevation = 4.dp
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isMobile = maxWidth < 768.dp
                        val isSystemAdmin = currentUser?.email == "gkhnazat@gmail.com" || currentUser?.role?.name == "SYSTEM_ADMIN"

                        if (isMobile) {
                            // 📱 MOBİL DİKEY EKRAN DÜZENİ (Telefonlarda Acente / Müşteri Giriş Butonları En Üstte Net Görünür)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Satır: Logolar & Müşteri / Acente Giriş Butonları (Her Zaman Görünür)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isWhitelabelAgencyMode) {
                                            val cleanLogo = (whitelabelLogoUrl ?: companySettings?.logoUrl)?.trim()
                                            val isValidLogo = !cleanLogo.isNullOrBlank() && 
                                                    !cleanLogo.contains("default", ignoreCase = true) && 
                                                    !cleanLogo.contains("placeholder", ignoreCase = true) &&
                                                    !cleanLogo.contains("axileto", ignoreCase = true)

                                            if (isValidLogo) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = cleanLogo,
                                                        contentDescription = "Acente Logosu",
                                                        modifier = Modifier.height(26.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            val rawName = (whitelabelAgencyName ?: companySettings?.name)?.trim().orEmpty()
                                            val displayName = if (rawName.isBlank() || rawName.equals("axileto", ignoreCase = true) || rawName.equals("TourOS", ignoreCase = true)) {
                                                "Online Rezervasyon Portalı"
                                            } else {
                                                rawName
                                            }
                                            Text(
                                                text = displayName,
                                                style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                            )
                                        } else {
                                            val logoUrl = companySettings?.logoUrl?.trim()
                                            val isValidLogo = !logoUrl.isNullOrBlank() && 
                                                    !logoUrl.contains("default", ignoreCase = true) && 
                                                    !logoUrl.contains("placeholder", ignoreCase = true)

                                            if (isValidLogo) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = logoUrl,
                                                        contentDescription = "Marka Logosu",
                                                        modifier = Modifier.height(26.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            } else {
                                                AxiletoLogoText(height = 28.dp)
                                            }
                                            val currentName = companySettings?.name?.trim().orEmpty()
                                            if (currentName.isNotBlank() && !currentName.equals("axileto", ignoreCase = true) && !currentName.equals("TourOS", ignoreCase = true) && !currentName.equals("TourOS Travels", ignoreCase = true)) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = currentName,
                                                    style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }

                                    // 🧳 Misafir / 🏢 Acenta / 👑 Club Giriş Butonları (Minimalist & Şık)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val guestLabel = "🧳 ${AppLanguageManager.translate("Misafir", currentLang.code)}"
                                        val agencyLabel = if (currentUser != null) "🏢 ${AppLanguageManager.translate("Acente Paneli", currentLang.code)} ➔" else "🏢 ${AppLanguageManager.translate("Acenteler", currentLang.code)}"
                                        val clubLabel = "👑 ${AppLanguageManager.translate("Club", currentLang.code)}"

                                        val entryModes = if (isWhitelabelAgencyMode) {
                                            listOf(
                                                Triple("GUEST", guestLabel, false),
                                                Triple("CLUB", clubLabel, true)
                                            )
                                        } else {
                                            listOf(
                                                Triple("GUEST", guestLabel, false),
                                                Triple("AGENCY", agencyLabel, false),
                                                Triple("CLUB", clubLabel, true)
                                            )
                                        }

                                        entryModes.forEach { (modeType, modeLabel, isClub) ->
                                            val isGuest = modeType == "GUEST"
                                            val isAgency = modeType == "AGENCY"
                                            val isSelectedMode = (isGuest && userMode == "Turist") || (isAgency && userMode == "Acente")
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelectedMode) Color(0xFF0284C7) else if (isClub) Color(0xFF0F172A) else Color.Transparent)
                                                    .clickable {
                                                        if (isClub) {
                                                            onNavigateToClub()
                                                        } else if (isGuest) {
                                                            userMode = "Turist"
                                                        } else {
                                                            userMode = "Acente"
                                                            if (currentUser == null) {
                                                                onNavigateToLogin()
                                                            }
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = modeLabel,
                                                    style = TourOSTypography.Caption.copy(
                                                        color = if (isClub) Color(0xFFE2B755) else Color.White,
                                                        fontWeight = if (isSelectedMode || isClub) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Satır: Düz Beyaz Font Dil Seçici & Admin Paneli
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LanguageSelector(
                                        selectedLanguage = when (currentLang.code) {
                                            "ru" -> AppLanguage.RU
                                            "en" -> AppLanguage.EN
                                            else -> AppLanguage.TR
                                        },
                                        onLanguageSelected = { lang ->
                                            AppLanguageManager.setLanguage(lang.code)
                                        },
                                        isAdmin = isSystemAdmin
                                    )

                                    if (isSystemAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.12f))
                                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                                .clickable { onNavigateToAdminCms() }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                                Text("Admin", style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 10.sp))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // 💻 MASAÜSTÜ GENİŞ EKRAN DÜZENİ
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sol Taraf: Marka Logosu & Başlık
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val logoUrl = companySettings?.logoUrl?.trim()
                                    val isValidLogo = !logoUrl.isNullOrBlank() && 
                                            !logoUrl.contains("default", ignoreCase = true) && 
                                            !logoUrl.contains("placeholder", ignoreCase = true)

                                    if (isWhitelabelAgencyMode) {
                                        val cleanLogo = (whitelabelLogoUrl ?: companySettings?.logoUrl)?.trim()
                                        val isValidLogo = !cleanLogo.isNullOrBlank() && 
                                                !cleanLogo.contains("default", ignoreCase = true) && 
                                                !cleanLogo.contains("placeholder", ignoreCase = true) &&
                                                !cleanLogo.contains("axileto", ignoreCase = true)

                                        if (isValidLogo) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                AsyncImage(
                                                    model = cleanLogo,
                                                    contentDescription = "Acente Logosu",
                                                    modifier = Modifier.height(34.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                        }
                                        val rawName = (whitelabelAgencyName ?: companySettings?.name)?.trim().orEmpty()
                                        val displayName = if (rawName.isBlank() || rawName.equals("axileto", ignoreCase = true) || rawName.equals("TourOS", ignoreCase = true)) {
                                            "Online Rezervasyon Portalı"
                                        } else {
                                            rawName
                                        }
                                        Text(
                                            text = displayName,
                                            style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                        )
                                    } else {
                                        val logoUrl = companySettings?.logoUrl?.trim()
                                        val isValidLogo = !logoUrl.isNullOrBlank() && 
                                                !logoUrl.contains("default", ignoreCase = true) && 
                                                !logoUrl.contains("placeholder", ignoreCase = true)

                                        if (isValidLogo) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                AsyncImage(
                                                    model = logoUrl,
                                                    contentDescription = "Marka Logosu",
                                                    modifier = Modifier.height(34.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                        } else {
                                            AxiletoLogoText(height = 36.dp)
                                        }
                                        val currentName = companySettings?.name?.trim().orEmpty()
                                        if (currentName.isNotBlank() && !currentName.equals("axileto", ignoreCase = true) && !currentName.equals("TourOS", ignoreCase = true) && !currentName.equals("TourOS Travels", ignoreCase = true) && !currentName.equals("TourOS Acente", ignoreCase = true)) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = currentName,
                                                style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }

                                // Sağ Taraf: Düz Beyaz Font Dil Seçici + Admin + Acente/Misafir
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Düz Beyaz Font Dil Seçici (Admin: RU|ENG|TR, Diğer: RU|ENG)
                                    LanguageSelector(
                                        selectedLanguage = when (currentLang.code) {
                                            "ru" -> AppLanguage.RU
                                            "en" -> AppLanguage.EN
                                            else -> AppLanguage.TR
                                        },
                                        onLanguageSelected = { lang ->
                                            AppLanguageManager.setLanguage(lang.code)
                                        },
                                        isAdmin = isSystemAdmin && !isWhitelabelAgencyMode
                                    )

                                    if (isSystemAdmin && !isWhitelabelAgencyMode) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.12f))
                                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                                .clickable { onNavigateToAdminCms() }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                                Text(text = AppLanguageManager.translate("Yönetici Paneli"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                            }
                                        }
                                    }

                                    // Misafir / Acenta / 👑 Club Seçici Segment Butonları (Minimalist & Şık)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val guestLabel = "🧳 ${AppLanguageManager.translate("Misafir", currentLang.code)}"
                                        val agencyLabel = if (currentUser != null) "🏢 ${AppLanguageManager.translate("Acente Paneli", currentLang.code)}" else "🏢 ${AppLanguageManager.translate("Acenteler", currentLang.code)}"
                                        val clubLabel = "👑 ${AppLanguageManager.translate("Club", currentLang.code)}"

                                        val desktopEntryModes = if (isWhitelabelAgencyMode) {
                                            listOf(
                                                Triple("GUEST", guestLabel, false),
                                                Triple("CLUB", clubLabel, true)
                                            )
                                        } else {
                                            listOf(
                                                Triple("GUEST", guestLabel, false),
                                                Triple("AGENCY", agencyLabel, false),
                                                Triple("CLUB", clubLabel, true)
                                            )
                                        }

                                        desktopEntryModes.forEach { (modeType, modeLabel, isClub) ->
                                            val isGuest = modeType == "GUEST"
                                            val isAgency = modeType == "AGENCY"
                                            val isSelectedMode = (isGuest && userMode == "Turist") || (isAgency && userMode == "Acente")
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelectedMode) Color(0xFF0284C7) else if (isClub) Color(0xFF0F172A) else Color.Transparent)
                                                    .clickable {
                                                        if (isClub) {
                                                            onNavigateToClub()
                                                        } else if (isGuest) {
                                                            userMode = "Turist"
                                                        } else {
                                                            userMode = "Acente"
                                                            if (currentUser == null) {
                                                                onNavigateToLogin()
                                                            }
                                                        }
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = modeLabel,
                                                    style = TourOSTypography.Caption.copy(
                                                        color = if (isClub) Color(0xFFE2B755) else Color.White,
                                                        fontWeight = if (isSelectedMode || isClub) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (userMode == "Acente" && agencyActiveTab == "BOOKINGS") {
                item {
                    AgencyBookingsModuleSection(
                        bookings = agencyBookings,
                        isLoading = isLoadingAgencyBookings,
                        onRefresh = {
                            coroutineScope.launch {
                                isLoadingAgencyBookings = true
                                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                                agencyBookings = bookingRepository.getBookings(tid).getOrDefault(emptyList())
                                isLoadingAgencyBookings = false
                            }
                        }
                    )
                }
            } else if (userMode == "Acente" && (agencyActiveTab == "CREATE_BOOKING" || agencyActiveTab == "CATALOG")) {
                item {
                    B2BTourSearchDashboardScreen(
                        isEmbedded = true,
                        onNavigateBack = { agencyActiveTab = "BOOKINGS" },
                        onNavigateToBookings = {
                            agencyActiveTab = "BOOKINGS"
                            coroutineScope.launch {
                                isLoadingAgencyBookings = true
                                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                                agencyBookings = bookingRepository.getBookings(tid).getOrDefault(emptyList())
                                isLoadingAgencyBookings = false
                            }
                        }
                    )
                }
            } else {
            // ── 2. HERO BANNER GÖRSELİ VE SLOGAN (RESPONSIVE 1320DP CONTAINER İLE HİZALANMIŞ) ────
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 1320.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            shadowElevation = 8.dp,
                            color = Color(0xFF0F172A)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                val rawHeader = companySettings?.headerImageUrl?.trim()
                                val isRemoteUrl = !rawHeader.isNullOrBlank() && 
                                        (rawHeader.startsWith("http://") || rawHeader.startsWith("https://")) &&
                                        !rawHeader.contains("placeholder", ignoreCase = true)
                                val headerImg = if (isRemoteUrl) {
                                    if (rawHeader!!.contains("unsplash.com") && !rawHeader.contains("auto=format")) {
                                        if (rawHeader.contains("?")) "$rawHeader&auto=format&fit=crop&q=80"
                                        else "$rawHeader?auto=format&fit=crop&w=1600&q=80"
                                    } else {
                                        rawHeader
                                    }
                                } else null

                                // 1. Yerel Güçlü Fallback Hero Banner (Her zaman garantili çizilir)
                                androidx.compose.foundation.Image(
                                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.hero_banner),
                                    contentDescription = "Header Hero Banner Default",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // 2. Uzak Sunucu / CMS Resmi (Varsa üzerine yumuşak bindirilir)
                                if (headerImg != null) {
                                    AsyncImage(
                                        model = headerImg,
                                        contentDescription = "Header Hero Banner",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Overlay Karartma Gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                            )
                                        )
                                )

                                // Sağ: 👑 Axileto Club VIP Rozeti (Hero Banner İçi - Büyütülmüş ve Net / Axileto ve Acenta Whitelabel'de Görünür)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 24.dp)
                                        .size(265.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable { onNavigateToClub() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.club_loyalty_badge),
                                        contentDescription = "Axileto Club",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Sol Alt Slogan (Yalnızca CMS'ten metin girilmişse gösterilir)
                                val customHeroText = companySettings?.heroSubtitle?.trim().orEmpty()
                                if (customHeroText.isNotBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(28.dp)
                                            .widthIn(max = 600.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = customHeroText,
                                            style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. BOOKING.COM / SKYSCANNER TARZI GENİŞ YATAY ARAMA MOTORU PANELİ (HERO İLE BÜTÜNLEŞİK) ──
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 1320.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-36).dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ── EVRENSEL ARAMA MOTORU KARTI (WEB HERO VARYANTI) ──
                        val triggerHeroSearch: () -> Unit = {
                            val tab = when (selectedSearchCategoryTab) {
                                "FLIGHT", "FLIGHTS" -> "FLIGHTS"
                                "HOTEL", "HOTELS" -> "HOTELS"
                                else -> "TOURS"
                            }
                            b2bTourSearchViewModel.selectedCategory.value = tab
                            b2bTourSearchViewModel.departureCity.value = departureCity
                            b2bTourSearchViewModel.selectedRegion.value = destinationCity
                            b2bTourSearchViewModel.adults.value = adultsCount
                            b2bTourSearchViewModel.childs.value = childrenCount
                            b2bTourSearchViewModel.childrenAges.value = childrenAges
                            b2bTourSearchViewModel.searchQuery.value = searchQuery
                            b2bTourSearchViewModel.selectedStartDate.value = startDateText
                            b2bTourSearchViewModel.selectedEndDate.value = endDateText
                            b2bTourSearchViewModel.performSearch(forceRefresh = true)
                            isInlineSearchActive = true
                        }

                        UniversalTourSearchBar(
                            variant = SearchBarVariant.PUBLIC_WEB_HERO,
                            activeTab = when (selectedSearchCategoryTab) {
                                "FLIGHT" -> "FLIGHTS"
                                "HOTEL" -> "HOTELS"
                                else -> "TOURS"
                            },
                            onTabChange = { tab ->
                                selectedSearchCategoryTab = when (tab) {
                                    "FLIGHTS" -> "FLIGHT"
                                    "HOTELS" -> "HOTEL"
                                    else -> "PACKAGE_TOUR"
                                }
                                b2bTourSearchViewModel.selectedCategory.value = tab
                                b2bTourSearchViewModel.performSearch(forceRefresh = true)
                                if (isInlineSearchActive) {
                                    triggerHeroSearch()
                                }
                            },
                            departureCity = departureCity,
                            onDepartureCityChange = { departureCity = it },
                            selectedRegion = destinationCity,
                            onRegionChange = { destinationCity = it },
                            startDateText = startDateText,
                            endDateText = endDateText,
                            onDateRangeChange = { start, end ->
                                startDateText = start
                                endDateText = end
                            },
                            nightsText = selectedNightsText,
                            onNightsTextChange = { selectedNightsText = it },
                            adults = adultsCount,
                            onAdultsChange = {
                                adultsCount = it
                                selectedTouristsText = "$it Yetişkin · $roomsCount Oda"
                            },
                            childrenAges = childrenAges,
                            onChildrenAgesChange = {
                                childrenAges = it
                                childrenCount = it.size
                            },
                            isRoundTrip = (flightTripType == "ROUND_TRIP"),
                            onRoundTripChange = { flightTripType = if (it) "ROUND_TRIP" else "ONE_WAY" },
                            onSearchClick = triggerHeroSearch,
                            onResetFiltersClick = {
                                departureCity = ""
                                destinationCity = ""
                                startDateText = com.mgacreative.touros.utils.DateUtils.getTodayDot()
                                endDateText = com.mgacreative.touros.utils.DateUtils.getFutureDot(7)
                                returnStartDateText = com.mgacreative.touros.utils.DateUtils.getFutureDot(7)
                                returnEndDateText = com.mgacreative.touros.utils.DateUtils.getFutureDot(14)
                                selectedNightsText = "7 - 10 Gece"
                                selectedTouristsText = "2 Yetişkin · 1 Oda"
                                adultsCount = 2
                                childrenCount = 0
                                roomsCount = 1
                                childrenAges = emptyList()
                                searchQuery = ""
                                selectedStars = emptySet()
                                selectedOperatorFilters = emptySet()
                                selectedBeachLine = 0
                                selectedMealTypes = emptySet()
                                minRating = 0.0
                                selectedHotels = emptySet()
                                selectedAmenities = emptySet()
                                isInstantOnly = false
                                isDirectFlightOnly = false
                                isTransferIncludedOnly = false
                                isInlineSearchActive = false
                                b2bTourSearchViewModel.departureCity.value = ""
                                b2bTourSearchViewModel.selectedRegion.value = ""
                                b2bTourSearchViewModel.adults.value = 2
                                b2bTourSearchViewModel.childs.value = 0
                                b2bTourSearchViewModel.childrenAges.value = emptyList()
                                b2bTourSearchViewModel.searchQuery.value = ""
                                b2bTourSearchViewModel.performSearch(forceRefresh = true)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── Bildirim Mesajı ───────────────────────────────────────────────────────
            if (!bookingSuccessMessage.isNullOrBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10B981))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bookingSuccessMessage ?: "",
                            style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // ── ⚡ SAYFA İÇİ AKICI İNLİNE ARAMA SONUÇLARI BÖLÜMÜ (INLINE SEARCH SECTION) ──
            if (isInlineSearchActive) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 1320.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(2.dp, Color(0xFF0D5653), RoundedCornerShape(16.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Üst Başlık Şeridi + Aramayı Temizle / Kapat Butonu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0D5653)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        val countLabel = if (selectedSearchCategoryTab == "FLIGHT") "Uçuş Seferi Bulundu" else "Paket Tur / Otel Bulundu"
                                        Text(
                                            text = "${AppLanguageManager.translate("Arama Sonuçları", currentLanguage.code)} (${b2bSearchResults.size} ${AppLanguageManager.translate(countLabel, currentLanguage.code)})",
                                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        )
                                        val rawDest = if (destinationCity.isBlank() || destinationCity.contains("Tüm Destinasyonlar", ignoreCase = true) || destinationCity.contains("Все направления", ignoreCase = true) || destinationCity.equals("ALL", ignoreCase = true)) {
                                            "Tüm Destinasyonlar"
                                        } else {
                                            destinationCity
                                        }
                                        val destBadge = AppLanguageManager.translate(rawDest, currentLanguage.code)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "${AppLanguageManager.translate("Tarih:", currentLanguage.code)} $startDateText — $endDateText",
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                            )
                                            Text(
                                                text = "  |  ",
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "${AppLanguageManager.translate("Destinasyon:", currentLanguage.code)} $destBadge",
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }

                                TourOSButton(
                                    text = AppLanguageManager.translate("Aramayı Temizle / Kapat ✕", currentLanguage.code),
                                    onClick = { 
                                        isInlineSearchActive = false 
                                        searchQuery = ""
                                        selectedDestinationFilter = "Tüm Destinasyonlar"
                                    },
                                    variant = TourOSButtonVariant.SECONDARY
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            if (b2bSearchResults.isNotEmpty()) {
                                // ── Bulunan Arama Fırsatları Dikey Kart Izgarası (Vertical Grid)
                                VerticalSearchResultsGridSection(
                                    titleVectorIcon = Icons.Default.Star,
                                    title = AppLanguageManager.translate("Bulunan Arama Fırsatları", currentLanguage.code),
                                    subtitle = AppLanguageManager.translate("Kriterlerinize uyan en uygun fiyatlı canlı tur ve otel teklifleri", currentLanguage.code),
                                    hotels = b2bSearchResults,
                                    onHotelClick = { selectedHotelForDetail = it },
                                    onSelectAndBook = handleDirectBooking
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("🏖️", fontSize = 42.sp)
                                    Text(
                                        text = AppLanguageManager.translate("Kriterlerinize Uygun Tur veya Otel Bulunamadı", currentLanguage.code),
                                        style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    )
                                    val rawEmptyDest = if (destinationCity.isBlank() || destinationCity.contains("Tüm Destinasyonlar", ignoreCase = true) || destinationCity.contains("Все направления", ignoreCase = true) || destinationCity.equals("ALL", ignoreCase = true)) {
                                        "Tüm Destinasyonlar"
                                    } else {
                                        destinationCity
                                    }
                                    val emptyDest = AppLanguageManager.translate(rawEmptyDest, currentLanguage.code)
                                    val emptyDep = departureCity.ifBlank { AppLanguageManager.translate("Tüm Kalkış Şehirleri", currentLanguage.code) }
                                    val emptyDescriptionText = when (currentLanguage.code) {
                                        "ru" -> "По выбранному направлению ($emptyDest) или пункту отправления ($emptyDep) в настоящее время нет активных предложений. Пожалуйста, попробуйте изменить даты или количество ночей."
                                        "en" -> "No active offers found for the selected destination ($emptyDest) or departure point ($emptyDep). Please try adjusting your date or nights criteria."
                                        "de" -> "Für das ausgewählte Reiseziel ($emptyDest) oder den Abflugort ($emptyDep) wurden derzeit keine aktiven Angebote gefunden. Bitte versuchen Sie, Ihre Datums- oder Übernachtungskriterien zu lockern."
                                        else -> "Seçtiğiniz destinasyon ($emptyDest) veya kalkış noktası ($emptyDep) için şu anda aktif teklif bulunamadı. Lütfen tarih veya geceleme kriterlerinizi esnetmeyi deneyiniz."
                                    }
                                    Text(
                                        text = emptyDescriptionText,
                                        style = TourOSTypography.BodyMedium.copy(color = Color(0xFF64748B)),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Ana Katalog Kartları Listesi ──────────────────────────────────────────
            if (isLoadingProducts) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF0F5A56),
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = AppLanguageManager.translate("Turlar ve Oteller Yükleniyor...", currentLanguage.code),
                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            } else {
                if (searchQuery.isNotBlank() && filteredHotels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🏨 ${AppLanguageManager.translate("Aradığınız kriterlere uygun tur/otel paketi bulunamadı.")}",
                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                )
                                TourOSButton(
                                    text = "${AppLanguageManager.translate("Filtreleri Sıfırla")} 🔄",
                                    onClick = {
                                        searchQuery = ""
                                        selectedStars = emptySet()
                                        maxPriceFilter = 200000f
                                        selectedOperatorFilters = emptySet()
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 1320.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // ── 🌍 HİZMETLERİ KEŞFEDİN: ÜLKE GİRİŞ KARTLARI & HIZLI LİSTELEME ──────────────────
                            val countryDiscoveryCards = remember {
                                listOf(
                                    Triple("ALL", "Tüm Dünyayı Keşfet", "🌍") to Triple("https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&auto=format&fit=crop&q=80", "Global Destinasyonlar", "En İyi Fiyat"),
                                    Triple("TR", "Турция", "🇹🇷") to Triple("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80", "Antalya · Belek · Bodrum · Kemer", "54.000 ₽'den başlayan"),
                                    Triple("EG", "Mısır", "🇪🇬") to Triple("https://images.unsplash.com/photo-1539768942893-daf53e448371?w=800&auto=format&fit=crop&q=80", "Şarm El-Şeyh · Hurgada · El Gouna", "46.000 ₽'den başlayan"),
                                    Triple("TH", "Tayland", "🇹🇭") to Triple("https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=800&auto=format&fit=crop&q=80", "Phuket · Pattaya · Bangkok · Samui", "72.000 ₽'den başlayan"),
                                    Triple("VN", "Vietnam", "🇻🇳") to Triple("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop&q=80", "Da Nang · Phu Quoc · Nha Trang", "78.000 ₽'den başlayan"),
                                    Triple("AE", "BAE (Dubai)", "🇦🇪") to Triple("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80", "Dubai Marina · Palm Jumeirah", "63.000 ₽'den başlayan"),
                                    Triple("RU", "Rusya", "🇷🇺") to Triple("https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800&auto=format&fit=crop&q=80", "Moskova · Sochi · St. Petersburg", "38.000 ₽'den başlayan")
                                )
                            }

                            val subRegionsMap = remember {
                                mapOf(
                                    "TR" to listOf("Tümü", "Antalya", "Belek", "Kemer", "Lara", "Alanya", "Side", "Bodrum", "Marmaris", "Fethiye", "Çeşme"),
                                    "EG" to listOf("Tümü", "Şarm El-Şeyh", "Hurgada", "El Gouna", "Makadi Bay"),
                                    "TH" to listOf("Tümü", "Phuket", "Pattaya", "Bangkok", "Koh Samui", "Krabi"),
                                    "VN" to listOf("Tümü", "Da Nang", "Phu Quoc", "Nha Trang", "Hoi An"),
                                    "AE" to listOf("Tümü", "Dubai Marina", "Palm Jumeirah", "Downtown", "Abu Dhabi"),
                                    "RU" to listOf("Tümü", "Moskova", "St. Petersburg", "Sochi", "Kazan")
                                )
                            }

                            if (activeCountryDetailPage != null) {
                                // ── 🗺️ ÜLKE DETAY SAYFASI: BAĞIMSIZ ARAMA PANELİ VE OTELLER ──────────────────
                                val currentCountryCode = activeCountryDetailPage!!
                                val countryData = countryDiscoveryCards.firstOrNull { it.first.first == currentCountryCode }
                                val (cCode, cName, cFlag) = countryData?.first ?: Triple(currentCountryCode, "Ülke Detayı", "🌍")
                                val (cImage, cSubInfo, cPrice) = countryData?.second ?: Triple("", "", "")

                                // Seçili ülke detay sayfası için Supabase'den canlı ürünleri çek
                                var detailPageProducts by remember { mutableStateOf<List<PublicHotelOffer>>(emptyList()) }
                                LaunchedEffect(currentCountryCode) {
                                    runCatching {
                                        supabaseClient.postgrest["marketplace_products"]
                                            .select {
                                                filter {
                                                    eq("country_code", currentCountryCode)
                                                }
                                                range(0, 500)
                                            }
                                            .decodeList<com.mgacreative.touros.data.database.entity.UnifiedProductEntity>()
                                    }.onSuccess { list ->
                                        detailPageProducts = list.map { it.toPublicHotelOffer() }
                                    }
                                }

                                val detailPagePool = remember(dbProducts, detailPageProducts, currentCountryCode) {
                                    if (detailPageProducts.isNotEmpty()) (detailPageProducts + dbProducts).distinctBy { it.id } else dbProducts
                                }

                                val subRegions = remember(detailPagePool, currentCountryCode) {
                                    val dbRegs = detailPagePool
                                        .filter { matchesSelectedCountry(it, currentCountryCode) }
                                        .map { it.location.substringBefore(",").trim() }
                                        .filter { it.isNotBlank() && it != "null" }
                                        .distinct()
                                        .sorted()
                                    if (dbRegs.isNotEmpty()) listOf("Tümü") + dbRegs else (subRegionsMap[currentCountryCode] ?: listOf("Tümü"))
                                }

                                // 1. Ülke Detay Hero Başlığı & Geri Dönüş Butonu
                                Surface(
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = cImage,
                                            contentDescription = cName,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.88f))
                                                    )
                                                )
                                        )
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(16.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Button(
                                                onClick = { 
                                                    activeCountryDetailPage = null 
                                                    selectedCountryTab = "ALL"
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(AppLanguageManager.translate("← Tüm Ülkelere & Ana Sayfaya Dön"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = AppLanguageManager.translate("$cName Tatil & Paket Turları"),
                                                        style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = Color(0xFFCBD5E1),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = cSubInfo,
                                                            style = TourOSTypography.BodyMedium.copy(color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    color = Color(0xFF0F5A56),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(
                                                        text = "2 Kişi 7 Gece: $cPrice",
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. BAĞIMSIZ ÜLKE ARAMA PANELİ (Country Dedicated Search Panel)
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    shadowElevation = 3.dp,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("🎯", fontSize = 16.sp)
                                                Text(
                                                    text = AppLanguageManager.translate("$cName Özel Arama & Nokta Atışı Filtreleme"),
                                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                )
                                            }
                                            TextButton(onClick = {
                                                countryDedicatedSubRegion = "Tümü"
                                                countryDedicatedHotelQuery = ""
                                                countryDedicatedNights = "7 Gece"
                                                countryDedicatedStars = setOf(4, 5)
                                            }) {
                                                Text(AppLanguageManager.translate("↺ Filtreleri Sıfırla"), fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Satır 1: Belde Seçimi, Otel Arama, Yıldız
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Belde Seçimi Çipleri
                                            Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("📍 $cName Beldeleri", style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    subRegions.forEach { sReg ->
                                                        val isSubAct = (countryDedicatedSubRegion == sReg)
                                                        Surface(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable { countryDedicatedSubRegion = sReg },
                                                            color = if (isSubAct) Color(0xFF0F5A56) else Color(0xFFF1F5F9),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text(
                                                                text = sReg,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                                style = TourOSTypography.Caption.copy(
                                                                    color = if (isSubAct) Color.White else Color(0xFF334155),
                                                                    fontWeight = if (isSubAct) FontWeight.Bold else FontWeight.Normal,
                                                                    fontSize = 11.sp
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Otel Adı Arama
                                            OutlinedTextField(
                                                value = countryDedicatedHotelQuery,
                                                onValueChange = { countryDedicatedHotelQuery = it },
                                                placeholder = { Text(AppLanguageManager.translate("Otel adı ara..."), fontSize = 12.sp) },
                                                modifier = Modifier.weight(0.9f).height(50.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                singleLine = true
                                            )

                                            // Yıldız Filtresi
                                            Row(
                                                modifier = Modifier.weight(0.8f),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                listOf(3, 4, 5).forEach { starCount ->
                                                    val isStarAct = countryDedicatedStars.contains(starCount)
                                                    Surface(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                countryDedicatedStars = if (isStarAct) countryDedicatedStars - starCount else countryDedicatedStars + starCount
                                                            },
                                                        color = if (isStarAct) Color(0xFF0F5A56) else Color(0xFFF1F5F9),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "$starCount⭐",
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                            style = TourOSTypography.Caption.copy(
                                                                color = if (isStarAct) Color.White else Color(0xFF334155),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. FİLTRELENMİŞ ÜLKE PAKETLERİ LİSTESİ (SATIR KÜÇÜK RESİMLİ KARTLAR)
                                val dedicatedCountryOffers = remember(detailPagePool, currentCountryCode, countryDedicatedSubRegion, countryDedicatedHotelQuery, countryDedicatedStars) {
                                    detailPagePool.filter { p ->
                                        val matchCountry = matchesSelectedCountry(p, currentCountryCode)
                                        val loc = p.location.lowercase()
                                        val hName = p.hotelName.lowercase()
                                        val matchSub = (countryDedicatedSubRegion == "Tümü") || loc.contains(countryDedicatedSubRegion.lowercase()) || hName.contains(countryDedicatedSubRegion.lowercase())
                                        val matchQuery = countryDedicatedHotelQuery.isBlank() || hName.contains(countryDedicatedHotelQuery.trim().lowercase()) || loc.contains(countryDedicatedHotelQuery.trim().lowercase())
                                        val matchStar = countryDedicatedStars.isEmpty() || countryDedicatedStars.contains(p.stars)

                                        matchCountry && matchSub && matchQuery && matchStar
                                    }
                                }

                                VerticalSearchResultsGridSection(
                                    titleVectorIcon = Icons.Default.LocationOn,
                                    title = "${AppLanguageManager.translate("$cName Paket Turları & Otelleri")} (${dedicatedCountryOffers.size} ${AppLanguageManager.translate("Tesis Bulundu")})",
                                    subtitle = "Tarih: 12 - 19 Eyl · 2 Kişi / 7 Gece · Direkt Uçuş & Transfer Dahil",
                                    hotels = dedicatedCountryOffers,
                                    onHotelClick = { selectedHotelForDetail = it },
                                    onSelectAndBook = handleDirectBooking
                                )
                            } else {
                                // ── 🌴 ПОПУЛЯРНЫЕ СТРАНЫ / POPULAR COUNTRIES BANNER BUTTON ──
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .clickable { showPopularCountriesModal = true },
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 3.dp,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        androidx.compose.foundation.Image(
                                            painter = org.jetbrains.compose.resources.painterResource(Res.drawable.popular_countries_banner),
                                            contentDescription = "Popular Countries",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color.Black.copy(alpha = 0.55f),
                                                            Color.Black.copy(alpha = 0.25f),
                                                            Color.Black.copy(alpha = 0.70f)
                                                        )
                                                    )
                                                )
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 24.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.20f),
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(44.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Public,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = AppLanguageManager.translate("Популярные направления"),
                                                        style = TourOSTypography.TitleMedium.copy(
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 17.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = AppLanguageManager.translate("18 стран · Выберите направление и откройте туры ➔"),
                                                        style = TourOSTypography.BodyMedium.copy(
                                                            color = Color.White.copy(alpha = 0.85f),
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }
                                            }

                                            Surface(
                                                color = Color.Black.copy(alpha = 0.45f),
                                                shape = RoundedCornerShape(20.dp),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = AppLanguageManager.translate("Популярные страны"),
                                                        style = TourOSTypography.BodyMedium.copy(
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    )
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                val fast2PersonDeals = remember(fastDealsFromDb, dbProducts) {
                                    if (fastDealsFromDb.isNotEmpty()) {
                                        fastDealsFromDb.take(4)
                                    } else {
                                        val minDepartureDate = com.mgacreative.touros.utils.DateUtils.getFutureIso(7)
                                        val isNonRussianPackage: (PublicHotelOffer) -> Boolean = { offer ->
                                            offer.category != "FLIGHT" &&
                                            !offer.hotelName.startsWith("Uçuş:", ignoreCase = true) &&
                                            !offer.hotelName.startsWith("✈️", ignoreCase = true) &&
                                            TourOperatorConfig.isAllowedOperator(offer.operatorName) &&
                                            offer.nights == 7 &&
                                            !offer.countryCode.equals("RU", ignoreCase = true) &&
                                            !offer.location.contains("Rusya", ignoreCase = true) &&
                                            !offer.location.contains("Россия", ignoreCase = true) &&
                                            !offer.location.contains("Russia", ignoreCase = true)
                                        }

                                        val getCountryKey: (PublicHotelOffer) -> String = { offer ->
                                            offer.countryCode.uppercase().trim().ifBlank {
                                                offer.location.substringAfterLast(",").trim().lowercase()
                                            }
                                        }

                                        val candidates = dbProducts.filter {
                                            isNonRussianPackage(it) &&
                                            (it.departureDate.isNullOrBlank() || it.departureDate >= minDepartureDate)
                                        }.sortedBy { it.minPrice }.distinctBy { getCountryKey(it) }

                                        if (candidates.size >= 4) {
                                            candidates.take(4)
                                        } else {
                                            val fallback = dbProducts.filter {
                                                isNonRussianPackage(it)
                                            }.sortedBy { it.minPrice }.distinctBy { getCountryKey(it) }
                                            (candidates + fallback).distinctBy { getCountryKey(it) }.take(4)
                                        }
                                    }
                                }

                                if (fast2PersonDeals.isNotEmpty()) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ElectricBolt,
                                                            contentDescription = null,
                                                            tint = Color(0xFFEAB308),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text(
                                                            text = AppLanguageManager.translate("Hızlı Fırsatlar: 2 Kişi / 7 Gece Direkt Uçuşlu Paketler"),
                                                            style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        )
                                                    }
                                                    Surface(
                                                        color = Color(0xFF0F5A56).copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = AppLanguageManager.translate("Uçuş + Transfer + Otel Dahil"),
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                            style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                        )
                                                    }
                                                }

                                                // Tek Satırlık Fırsat Listesi
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    fast2PersonDeals.forEach { dealHotel ->
                                                        Surface(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .clickable { selectedHotelForDetail = dealHotel },
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = Color.White,
                                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                            shadowElevation = 1.dp
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(10.dp),
                                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                AsyncImage(
                                                                    model = dealHotel.imageUrl,
                                                                    contentDescription = dealHotel.hotelName,
                                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                                    modifier = Modifier
                                                                        .size(width = 90.dp, height = 65.dp)
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                )

                                                                Column(
                                                                    modifier = Modifier.weight(1f),
                                                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                        modifier = Modifier.fillMaxWidth()
                                                                    ) {
                                                                        Text(
                                                                            text = dealHotel.hotelName,
                                                                            style = TourOSTypography.BodyMedium.copy(
                                                                                color = Color(0xFF1E293B),
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 13.sp
                                                                            ),
                                                                            maxLines = 1,
                                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                                        )
                                                                        if (dealHotel.stars > 0) {
                                                                            Row(
                                                                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                repeat(dealHotel.stars.coerceIn(1, 5)) {
                                                                                    Icon(
                                                                                        imageVector = Icons.Default.Star,
                                                                                        contentDescription = "Star",
                                                                                        tint = Color(0xFFFFB800),
                                                                                        modifier = Modifier.size(13.dp)
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    Row(
                                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.LocationOn,
                                                                                contentDescription = null,
                                                                                tint = Color(0xFF64748B),
                                                                                modifier = Modifier.size(13.dp)
                                                                            )
                                                                            Text(
                                                                                text = AppLanguageManager.translate(dealHotel.location),
                                                                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp),
                                                                                maxLines = 1
                                                                            )
                                                                        }
                                                                        val formattedDateRange = remember(dealHotel.departureDate, dealHotel.returnDate, dealHotel.nights) {
                                                                            val dep = dealHotel.departureDate?.trim()?.takeIf { it.isNotBlank() } ?: "26.09.2026"
                                                                            val (d, m) = if (dep.contains("-")) {
                                                                                val p = dep.split("-")
                                                                                if (p.size == 3) Pair(p[2].toIntOrNull() ?: 26, p[1].toIntOrNull() ?: 9) else Pair(26, 9)
                                                                            } else if (dep.contains(".")) {
                                                                                val p = dep.split(".")
                                                                                if (p.size >= 2) Pair(p[0].toIntOrNull() ?: 26, p[1].toIntOrNull() ?: 9) else Pair(26, 9)
                                                                            } else Pair(26, 9)

                                                                            val ret = dealHotel.returnDate?.trim()?.takeIf { it.isNotBlank() }
                                                                            val (rd, rm) = if (ret != null && ret.contains(".")) {
                                                                                val p = ret.split(".")
                                                                                if (p.size >= 2) Pair(p[0].toIntOrNull() ?: (d + dealHotel.nights), p[1].toIntOrNull() ?: m) else Pair(d + dealHotel.nights, m)
                                                                            } else if (ret != null && ret.contains("-")) {
                                                                                val p = ret.split("-")
                                                                                if (p.size == 3) Pair(p[2].toIntOrNull() ?: (d + dealHotel.nights), p[1].toIntOrNull() ?: m) else Pair(d + dealHotel.nights, m)
                                                                            } else Pair(d + dealHotel.nights, m)

                                                                            val depStr = "${d.toString().padStart(2, '0')}.${m.toString().padStart(2, '0')}"
                                                                            val retStr = "${rd.toString().padStart(2, '0')}.${rm.toString().padStart(2, '0')}"
                                                                            "$depStr — $retStr"
                                                                        }
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Flight,
                                                                                contentDescription = null,
                                                                                tint = Color(0xFF0F5A56),
                                                                                modifier = Modifier.size(13.dp)
                                                                            )
                                                                            Text(
                                                                                text = "$formattedDateRange (${dealHotel.nights} ${AppLanguageManager.translate("Gece")})",
                                                                                style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                                                            )
                                                                        }
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Hotel,
                                                                                contentDescription = null,
                                                                                tint = Color(0xFFD97706),
                                                                                modifier = Modifier.size(13.dp)
                                                                            )
                                                                            Text(
                                                                                text = AppLanguageManager.translate(dealHotel.mealType.ifBlank { "Her Şey Dahil" }),
                                                                                style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    val currSymbol = when (dealHotel.currency.uppercase()) {
                                                                        "USD" -> "$"
                                                                        "EUR" -> "€"
                                                                        "TRY" -> "₺"
                                                                        else -> "₽"
                                                                    }
                                                                    Column(horizontalAlignment = Alignment.End) {
                                                                        Text(
                                                                            text = AppLanguageManager.translate("2 Kişi Toplam"),
                                                                            style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                                        )
                                                                        Text(
                                                                            text = "${dealHotel.minPrice.toInt()} $currSymbol",
                                                                            style = TourOSTypography.TitleMedium.copy(
                                                                                color = Color(0xFF0F5A56),
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 16.sp
                                                                            )
                                                                        )
                                                                    }

                                                                    Button(
                                                                        onClick = { handleDirectBooking(dealHotel) },
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        colors = ButtonDefaults.buttonColors(
                                                                            containerColor = Color(0xFF0F5A56)
                                                                        ),
                                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = AppLanguageManager.translate("Rezerve Et ➔"),
                                                                            style = TourOSTypography.Caption.copy(
                                                                                color = Color.White,
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 11.sp
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                // ── BLOK 1: 🏖️ PAKET TURLAR (UÇUŞLAR VE SAF OTELLER HARİÇ - ANA SAYFA SABİT BLOK) ─────
                                val tourPackagesOnly = dbProducts.shuffled().filter { 
                                    (it.category == "PACKAGE_TOUR" || it.category == "LOCAL_TOUR") &&
                                    it.category != "FLIGHT" && it.category != "HOTEL" &&
                                    !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(20)
                                if (tourPackagesOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.BeachAccess,
                                        title = "Paket Turlar",
                                        subtitle = "Gezginler Tarafından Onaylanmış Her Şey Dahil Paket Turlar",
                                        hotels = tourPackagesOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = handleDirectBooking
                                    )
                                }

                                // ── BLOK 2: OTELLER (UÇUŞLAR VE PAKET TURLAR HARİÇ - SADECE OTEL KONAKLAMASI) ──────────
                                val hotelsOnly = dbProducts.shuffled().filter { 
                                    (it.category == "HOTEL" || it.category == "LOCAL_HOTEL") &&
                                    it.category != "FLIGHT" && it.category != "PACKAGE_TOUR" &&
                                    !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(20)
                                if (hotelsOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.Hotel,
                                        title = "Oteller",
                                        subtitle = "Ayrıcalıklı konaklama ve seçkin 5 yıldızlı oteller (Sadece Otel)",
                                        hotels = hotelsOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = handleDirectBooking
                                    )
                                }

                                // ── BLOK 3: SON DAKİKA (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ───────
                                val lastMinuteCandidates = dbProducts.filter { 
                                    it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true) && (it.isLastMinute || (it.discountPercent ?: 0) > 0)
                                }
                                val lastMinuteOnly = if (lastMinuteCandidates.size >= 4) {
                                    lastMinuteCandidates.take(20)
                                } else {
                                    val additionalDeals = dbProducts.filter { 
                                        it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                    }.sortedBy { it.minPrice }
                                    (lastMinuteCandidates + additionalDeals).distinctBy { it.id }.take(20)
                                }
                                if (lastMinuteOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.ElectricBolt,
                                        title = "Son Dakika",
                                        subtitle = "Acele edin ve %70'e varan muhteşem indirimlerden yararlanın!",
                                        hotels = lastMinuteOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = handleDirectBooking
                                    )
                                }

                                // ── BLOK 4: ✈️ CHARTER & TARİFELİ UÇUŞLAR (ANA SAYFA SABİT BLOK) ─────
                                val flightsOnly = dbProducts.shuffled().filter { 
                                    it.category == "FLIGHT" || it.hotelName.startsWith("Uçuş:", ignoreCase = true) || it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(20)
                                if (flightsOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.Flight,
                                        title = "Charter & Tarifeli Uçuşlar",
                                        subtitle = "En uygun fiyatlı direkt charter uçuşlar ve özel havayolu biletleri",
                                        hotels = flightsOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = handleDirectBooking
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 🌟 HİZMETLERİMİZ (OUR SERVICES - FOOTER ÜSTÜ 6'LI KURUMSAL SEKSİYON) ──
            item {
                OurServicesSection(
                    companySettings = companySettings,
                    isWhitelabelAgencyMode = isWhitelabelAgencyMode,
                    onSelectService = { serviceId ->
                        when {
                            serviceId.startsWith("HOTEL_TOURS:") -> {
                                val hParam = serviceId.substringAfter("HOTEL_TOURS:").trim()
                                val matchedOffer = dbProducts.firstOrNull { 
                                    it.id == hParam || 
                                    it.hotelName.equals(hParam, ignoreCase = true) ||
                                    it.hotelName.contains(hParam, ignoreCase = true) ||
                                    it.location.contains(hParam, ignoreCase = true)
                                }
                                val queryText = matchedOffer?.hotelName ?: hParam
                                selectedSearchCategoryTab = "ALL"
                                selectedOperatorFilters = emptySet()
                                selectedDestinationFilter = "Tüm Destinasyonlar"
                                searchQuery = queryText
                                isInlineSearchActive = true
                                coroutineScope.launch {
                                    mainLazyListState.animateScrollToItem(1)
                                }
                            }
                            serviceId == "PACKAGE_TOUR" -> { 
                                selectedSearchCategoryTab = "PACKAGE_TOUR"
                                searchQuery = ""
                                isInlineSearchActive = false
                                coroutineScope.launch { mainLazyListState.animateScrollToItem(0) }
                            }
                            serviceId == "HOTEL" -> { 
                                selectedSearchCategoryTab = "HOTEL"
                                searchQuery = ""
                                isInlineSearchActive = false
                                coroutineScope.launch { mainLazyListState.animateScrollToItem(0) }
                            }
                            serviceId == "FLIGHT" -> { 
                                selectedSearchCategoryTab = "FLIGHT"
                                searchQuery = ""
                                isInlineSearchActive = false
                                coroutineScope.launch { mainLazyListState.animateScrollToItem(0) }
                            }
                            serviceId == "ADVENTURE" -> { 
                                searchQuery = "Macera"
                                selectedOperatorFilters = emptySet()
                                isInlineSearchActive = true
                                coroutineScope.launch { mainLazyListState.animateScrollToItem(1) }
                            }
                            serviceId == "CRUISE" -> { 
                                searchQuery = "Gemi"
                                selectedOperatorFilters = emptySet()
                                isInlineSearchActive = true
                                coroutineScope.launch { mainLazyListState.animateScrollToItem(1) }
                            }
                            serviceId == "ASSISTANCE" -> { showAgencyLoginModal = true }
                            else -> {
                                selectedSearchCategoryTab = "ALL"
                                searchQuery = serviceId
                                isInlineSearchActive = true
                                coroutineScope.launch {
                                    mainLazyListState.animateScrollToItem(1)
                                }
                            }
                        }
                    }
                )
            }

            // ── 🏢 KURUMSAL BUSINESS FOOTER (WEB PANEL YÖNETİMİNDEN CANLI VERİLERLE) ──
            item {
                BusinessFooterSection(
                    companySettings = companySettings,
                    isWhitelabelAgencyMode = isWhitelabelAgencyMode,
                    whitelabelAgencyName = whitelabelAgencyName,
                    whitelabelPhone = whitelabelPhone,
                    whitelabelEmail = whitelabelEmail,
                    whitelabelWhatsapp = whitelabelWhatsapp,
                    whitelabelAddress = whitelabelAddress,
                    onNavigateToLogin = { if (!isWhitelabelAgencyMode) showAgencyLoginModal = true }
                )
            }
        }
    }

            // ── SAĞ TARAF SABİT DİKEY KAYDIRMA ÇUBUĞU (Ana Sayfa) ──
            TourOSLazyListVerticalScrollbar(
                listState = mainLazyListState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 2.dp)
            )

        // ── 🏢 ACENTE GİRİŞİ MODALI (EMAIL + ŞİFRE + ACENTE KODU) ──────────────────
        if (showAgencyLoginModal) {
            Dialog(
                onDismissRequest = { showAgencyLoginModal = false }
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏢 ${AppLanguageManager.translate("Acente Yetkili Girişi (B2B)", currentLanguage.code)}",
                                style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable { showAgencyLoginModal = false }
                                    .padding(6.dp)
                            ) {
                                Text("❌", fontSize = 16.sp)
                            }
                        }

                        Text(
                            text = AppLanguageManager.translate("Saas B2B acente portalına ve özel operatör marjlarına erişmek için e-posta, şifre ve acente kodunuzu giriniz.", currentLanguage.code),
                            style = TourOSTypography.BodyMedium.copy(color = Color(0xFF64748B))
                        )

                        if (!agencyLoginError.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEE2E2))
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = AppLanguageManager.translate(agencyLoginError ?: "", currentLanguage.code),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = agencyEmailInput,
                            onValueChange = { agencyEmailInput = it },
                            label = { Text(AppLanguageManager.translate("Acente E-Posta Adresi", currentLanguage.code)) },
                            placeholder = { Text("acente@axileto.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = agencyPasswordInput,
                            onValueChange = { agencyPasswordInput = it },
                            label = { Text(AppLanguageManager.translate("Şifre", currentLanguage.code)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )

                        OutlinedTextField(
                            value = agencyCodeInput,
                            onValueChange = { agencyCodeInput = it },
                            label = { Text(AppLanguageManager.translate("Acente Kodu (Referral Code)", currentLanguage.code)) },
                            placeholder = { Text(AppLanguageManager.translate("Örn: AGN-MASTER-8492", currentLanguage.code)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Beni Hatırla Seçeneği
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { rememberAgencyCredentials = !rememberAgencyCredentials }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = rememberAgencyCredentials,
                                onCheckedChange = { rememberAgencyCredentials = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF0F5A56),
                                    uncheckedColor = Color(0xFFCBD5E1)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppLanguageManager.translate("Beni Hatırla (Bilgilerimi bu cihazda sakla)", currentLanguage.code),
                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF334155), fontSize = 13.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TourOSButton(
                                text = AppLanguageManager.translate("İptal", currentLanguage.code),
                                onClick = { showAgencyLoginModal = false },
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            TourOSButton(
                                text = "${AppLanguageManager.translate("Giriş Yap & Bağlan", currentLanguage.code)} 🚀",
                                onClick = {
                                    if (agencyEmailInput.isBlank() || agencyPasswordInput.isBlank() || agencyCodeInput.isBlank()) {
                                        agencyLoginError = "Lütfen E-Posta, Şifre ve Acente Kodu alanlarını doldurunuz."
                                    } else {
                                        coroutineScope.launch {
                                            val res = authRepository.signInWithEmail(agencyEmailInput.trim(), agencyPasswordInput.trim())
                                            if (res.isSuccess) {
                                                if (rememberAgencyCredentials) {
                                                    com.mgacreative.touros.utils.LocalAuthStorage.saveCredentials(
                                                        com.mgacreative.touros.utils.SavedAuthCredentials(
                                                            email = agencyEmailInput.trim(),
                                                            password = agencyPasswordInput.trim(),
                                                            agencyCode = agencyCodeInput.trim(),
                                                            rememberMe = true
                                                        )
                                                    )
                                                } else {
                                                    com.mgacreative.touros.utils.LocalAuthStorage.clearCredentials()
                                                }
                                                showAgencyLoginModal = false
                                                agencyLoginError = null
                                                userMode = "Acente"
                                                bookingSuccessMessage = "✅ Acente girişi başarılı! Hoş geldiniz: ${agencyEmailInput.trim()}"
                                                onNavigateToLogin()
                                            } else {
                                                agencyLoginError = "Giriş başarısız: E-Posta veya Şifre hatalı. (Acente Kodu: ${agencyCodeInput.trim()})"
                                            }
                                        }
                                    }
                                },
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }
        }

        // ── DETAY & ACENTE FİYAT KARŞILAŞTIRMA VE REZERVASYON MODAL SİHİRBAZI ────────
        if (selectedHotelForDetail != null) {
            val hotel = selectedHotelForDetail!!

            if (selectedAgencyForBooking != null) {
                // ── REZERVASYON TAMAMLAMA CHECKOUT MODALI ─────────────────────────────────
                val option = selectedAgencyForBooking!!

                var guestName by remember { mutableStateOf("") }
                var guestPhone by remember { mutableStateOf("") }
                var passportNo by remember { mutableStateOf("") }
                var formValidationError by remember { mutableStateOf<String?>(null) }

                Dialog(
                    onDismissRequest = { selectedAgencyForBooking = null }
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 550.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📝 " + AppLanguageManager.translate("Rezervasyon ve Ödeme Sihirbazı"),
                                    style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "✖",
                                    modifier = Modifier.clickable { selectedAgencyForBooking = null },
                                    style = TourOSTypography.TitleLarge.copy(color = Color(0xFF94A3B8))
                                )
                            }

                            Text(
                                text = "${AppLanguageManager.translate("Seçilen Acente:")} ${option.agencyName} • ${AppLanguageManager.translate("Operatör")}: ${option.operatorName}\n${AppLanguageManager.translate("Otel")}: ${hotel.hotelName} (${option.price.toInt()} ₺)",
                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                            )

                            if (!formValidationError.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = formValidationError ?: "",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = guestName,
                                onValueChange = { 
                                    guestName = it
                                    if (formValidationError != null) formValidationError = null
                                },
                                label = { Text(AppLanguageManager.translate("Misafir Ad Soyad")) },
                                placeholder = { Text("Имя Фамилия / Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = guestPhone,
                                onValueChange = { 
                                    guestPhone = it
                                    if (formValidationError != null) formValidationError = null
                                },
                                label = { Text(AppLanguageManager.translate("Telefon")) },
                                placeholder = { Text("+90 5XX XXX XX XX / +7 9XX XXX XX XX") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = passportNo,
                                onValueChange = { passportNo = it },
                                label = { Text(AppLanguageManager.translate("Pasaport / Kimlik No")) },
                                placeholder = { Text("N12345678") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TourOSButton(
                                    text = if (isWhitelabelAgencyMode) "← " + AppLanguageManager.translate("Geri / Kapat") else "← " + AppLanguageManager.translate("Geri (Fiyat Karşılaştırma)"),
                                    onClick = { 
                                        selectedAgencyForBooking = null
                                        if (isWhitelabelAgencyMode) selectedHotelForDetail = null
                                    },
                                    variant = TourOSButtonVariant.TERTIARY
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                TourOSButton(
                                    text = AppLanguageManager.translate("Bilet & Rezervasyonu Onayla") + " 🚀",
                                    onClick = {
                                        if (guestName.isBlank() || guestPhone.isBlank()) {
                                            formValidationError = AppLanguageManager.translate("Lütfen misafir adı ve telefon numarasını giriniz.")
                                            return@TourOSButton
                                        }
                                        formValidationError = null

                                        coroutineScope.launch {
                                            val isReferralAgency = isWhitelabelAgencyMode || !referralCode.isNullOrBlank() || (currentUser != null && userMode == "Acente")
                                            val targetAgencyId = if (isWhitelabelAgencyMode && !resolvedAgencyId.isNullOrBlank()) {
                                                resolvedAgencyId!!
                                            } else if (!referralCode.isNullOrBlank()) {
                                                referralCode!!
                                            } else if (currentUser != null && userMode == "Acente") {
                                                currentUser?.tenantId ?: currentUser?.id ?: option.agencyId
                                            } else {
                                                "00000000-0000-0000-0000-000000000001" // TourOS HQ Master Agency Tenant ID
                                            }
                                            val isFlightBooking = hotel.category == "FLIGHT"
                                            val validBookingId = com.mgacreative.touros.data.util.generateUuid()
                                            val pnrCode = (if (isFlightBooking) "PNR-" else "WEB-") + (10000..99999).random()
                                            val safeTenantId = if (targetAgencyId.isValidUuid()) targetAgencyId else "00000000-0000-0000-0000-000000000001"
                                            val safeHotelId = hotel.id.takeIf { it.isValidUuid() }

                                            val rawOp = option.operatorName.ifBlank { hotel.operatorName }.trim()
                                            val isLocalProd = rawOp.contains("Yerel", ignoreCase = true) || hotel.category == "LOCAL_HOTEL" || hotel.category == "LOCAL_TOUR"
                                            val cleanOpName = if (rawOp.contains(" / ")) rawOp.substringBefore(" / ").trim() else rawOp.substringBefore(" B2B").trim()
                                            val finalOperatorName = if (isLocalProd) option.agencyName.ifBlank { "Yerel Acente" } else cleanOpName.ifBlank { "Coral Travel" }

                                            val newBooking = Booking(
                                                id = validBookingId,
                                                bookingCode = pnrCode,
                                                customerName = guestName.ifBlank { "Web Misafir" },
                                                customerPhone = guestPhone.ifBlank { "+90 500 000 0000" },
                                                customerEmail = currentUser?.email ?: "web-rezervasyon@touros.com",
                                                productName = if (isFlightBooking) "✈️ Charter Uçuş Bileti: ${hotel.hotelName} (${hotel.flightCode})" else "${hotel.hotelName} (${option.roomType})",
                                                hotelId = safeHotelId,
                                                bookingType = if (isFlightBooking) "FLIGHT" else if (isLocalProd) "LOCAL_HOTEL" else "HOTEL",
                                                roomTypeName = option.roomType,
                                                operatorName = finalOperatorName,
                                                departureDate = hotel.departureDate ?: "2026-08-21",
                                                nights = hotel.nights,
                                                totalPrice = option.price,
                                                currency = hotel.currency.ifBlank { "TRY" },
                                                status = BookingStatus.ONAYLANDI,
                                                tenantId = safeTenantId,
                                                items = listOf(
                                                    BookingItem(
                                                        id = com.mgacreative.touros.data.util.generateUuid(),
                                                        bookingId = validBookingId,
                                                        description = "${hotel.hotelName} - ${option.roomType} (${option.boardType})",
                                                        quantity = 1,
                                                        unitPrice = option.price,
                                                        totalPrice = option.price,
                                                        itemType = if (isFlightBooking) "FLIGHT" else "HOTEL"
                                                    )
                                                ),
                                                passengers = listOf(
                                                    Passenger(
                                                        id = com.mgacreative.touros.data.util.generateUuid(),
                                                        bookingId = validBookingId,
                                                        fullName = guestName.ifBlank { "Web Misafir" },
                                                        phone = guestPhone.ifBlank { "+90 500 000 0000" },
                                                        passportNo = passportNo.ifBlank { "-" },
                                                        isLead = true
                                                    )
                                                )
                                            )
                                            bookingRepository.createBooking(newBooking)
                                                .onSuccess {
                                                    println("✅ Web rezervasyonu Supabase ve yerel önbelleğe kaydedildi: ${newBooking.bookingCode}")
                                                }
                                                .onFailure { err ->
                                                    println("⚠️ Web rezervasyonu kayıt uyarısı: ${err.message}")
                                                }

                                            bookingSuccessMessage = if (isWhitelabelAgencyMode) {
                                                "✅ Rezervasyon/PNR (${newBooking.bookingCode}) başarıyla oluşturuldu! Talebiniz ${whitelabelAgencyName ?: "acentemize"} iletildi."
                                            } else if (isReferralAgency) {
                                                "✅ Rezervasyon/PNR (${newBooking.bookingCode}) başarıyla oluşturuldu!"
                                            } else {
                                                "✅ Bilet/PNR (${newBooking.bookingCode}) başarıyla oluşturuldu!"
                                            }
                                            selectedAgencyForBooking = null
                                            selectedHotelForDetail = null
                                        }
                                    },
                                    variant = TourOSButtonVariant.PRIMARY
                                )
                            }
                        }
                    }
                }
            } else {
                // ── DETAY & ACENTE FİYAT KARŞILAŞTIRMA MODALI (Metasearch Engine - Kompakt & Kaydırılabilir) ──
                Dialog(
                    onDismissRequest = { 
                        selectedHotelForDetail = null
                        selectedAgencyForBooking = null
                    }
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = 740.dp)
                            .fillMaxWidth()
                            .heightIn(max = 620.dp)
                            .padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // ── SABİT ÜST BAŞLIK ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = hotel.hotelName,
                                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    )
                                    Text(
                                        text = "📍 ${AppLanguageManager.translate(hotel.location)} • ID: ${hotel.id}",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9))
                                        .clickable { 
                                            selectedHotelForDetail = null
                                            selectedAgencyForBooking = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isWhitelabelAgencyMode) AppLanguageManager.translate("Rezervasyon Seçenekleri") else "🏬 " + AppLanguageManager.translate("Hangi Acente / Operatör Kaç Satıyor?"),
                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                )
                                if (!isWhitelabelAgencyMode) {
                                    Text(
                                        text = AppLanguageManager.translate("Canlı Fiyat Karşılaştırma"),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    )
                                }
                            }

                            // ── AŞAĞI KAYDIRILABİLİR İNCE KOMPAKT OPERATÖR LİSTESİ (LAZYCOLUMN) ──
                            val effectiveAgencyPrices = if (isWhitelabelAgencyMode && !resolvedAgencyId.isNullOrBlank()) {
                                listOf(
                                    AgencyPriceOption(
                                        agencyId = resolvedAgencyId!!,
                                        agencyName = (whitelabelAgencyName ?: companySettings?.name)?.ifBlank { "Yetkili Acente" } ?: "Yetkili Acente",
                                        operatorName = hotel.operatorName.ifBlank { "Tur Operatörü" },
                                        roomType = hotel.roomType.ifBlank { "Standart Oda" },
                                        boardType = hotel.mealType.ifBlank { "Her Şey Dahil" },
                                        price = hotel.minPrice,
                                        isBestDeal = true
                                    )
                                )
                            } else if (hotel.agencyPrices.isNotEmpty()) {
                                hotel.agencyPrices
                            } else {
                                listOf(
                                    AgencyPriceOption(
                                        agencyId = "AGN-MAIN",
                                        agencyName = hotel.operatorName.ifBlank { "TourVisor Operatörü" },
                                        operatorName = hotel.operatorName.ifBlank { "TourVisor Operatörü" },
                                        roomType = hotel.roomType.ifBlank { "Standard Room" },
                                        boardType = hotel.mealType.ifBlank { "Her Şey Dahil" },
                                        price = hotel.minPrice,
                                        isBestDeal = true
                                    )
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(effectiveAgencyPrices) { option ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (option.isBestDeal) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (option.isBestDeal) Color(0xFF22C55E) else Color(0xFFE2E8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Sol Bilgi: Acente/Operatör + Oda Tipi (İnce Satır)
                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = "🏢 ${option.agencyName}",
                                                        style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    )
                                                    if (option.isBestDeal) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF22C55E))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(AppLanguageManager.translate("En İyi Fiyat ⭐"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${AppLanguageManager.translate("Operatör")}: ${option.operatorName} • ${option.roomType} (${option.boardType})",
                                                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Sağ Taraf: Fiyat + Kompakt Rezerve Et Butonu
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(option.price, decimals = false)} ${if (hotel.currency == "RUB") "RUB" else "₺"}",
                                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                                )

                                                Button(
                                                    onClick = {
                                                        selectedAgencyForBooking = option
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF1E4D58)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = AppLanguageManager.translate("Rezerve Et ➔"),
                                                        style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ── ⚡ DİKEY LİSTE ARAMA SONUÇLARI SEKSİYONU (HIZLI FIRSATLAR SATIR STİLİ) ──────────
@Composable
fun VerticalSearchResultsGridSection(
    titleIcon: String = "",
    titleVectorIcon: ImageVector? = null,
    title: String = "Bulunan Arama Fırsatları",
    subtitle: String = "Kriterlerinize uyan en uygun fiyatlı canlı tur ve otel teklifleri",
    hotels: List<PublicHotelOffer>,
    onHotelClick: (PublicHotelOffer) -> Unit,
    onSelectAndBook: (PublicHotelOffer) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (titleVectorIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F5A56).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = titleVectorIcon,
                                contentDescription = null,
                                tint = Color(0xFF0F5A56),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (titleIcon.isNotBlank()) {
                        Text(titleIcon, fontSize = 22.sp)
                    }
                    Column {
                        Text(
                            text = AppLanguageManager.translate(title),
                            style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                        Text(
                            text = AppLanguageManager.translate(subtitle),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                        )
                    }
                }
                Surface(
                    color = Color(0xFF0F5A56).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = AppLanguageManager.translate("Uçuş + Transfer + Otel Dahil"),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }

            // ── B2B İLE BİREBİR DETAYLI ARAMA FİLTRE STATE'LERİ ──
            var isFilterExpanded by remember { mutableStateOf(false) }
            var selectedBeachLine by remember { mutableStateOf(0) }
            var selectedMealTypes by remember { mutableStateOf(emptySet<String>()) }
            var selectedStars by remember { mutableStateOf(emptySet<Int>()) }
            var minRating by remember { mutableStateOf(0.0) }
            var selectedHotels by remember { mutableStateOf(emptySet<String>()) }
            var selectedAmenities by remember { mutableStateOf(emptySet<String>()) }
            var isInstantOnly by remember { mutableStateOf(false) }
            var isDirectFlightOnly by remember { mutableStateOf(false) }
            var isTransferIncludedOnly by remember { mutableStateOf(false) }
            var selectedOperators by remember { mutableStateOf(emptySet<String>()) }

            val availableHotels = remember(hotels) {
                hotels.map { it.hotelName.trim() }.filter { it.isNotBlank() }.distinct().sorted()
            }
            val availableMealTypes = remember { listOf("UAI", "AI", "FB", "HB", "BB") }
            val availableOperators = remember {
                TourOperatorConfig.ALLOWED_OPERATOR_NAMES
            }

            // Arama Sonuçlarını Detaylı Filtrelerle Canlı Süzme
            val filteredHotels = remember(
                hotels,
                selectedBeachLine,
                selectedMealTypes,
                selectedStars,
                minRating,
                selectedHotels,
                selectedAmenities,
                isInstantOnly,
                isDirectFlightOnly,
                isTransferIncludedOnly,
                selectedOperators
            ) {
                hotels.filter { hotel ->
                    val isFlight = hotel.category.uppercase() == "FLIGHT" || 
                                   hotel.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                                   hotel.hotelName.startsWith("✈️", ignoreCase = true)
                    val isAllowedOp = isFlight || TourOperatorConfig.isAllowedOperator(hotel.operatorName)
                    if (!isAllowedOp) return@filter false

                    val starMatch = selectedStars.isEmpty() || selectedStars.contains(hotel.stars)
                    val beachMatch = selectedBeachLine == 0 || (selectedBeachLine == 1 && (hotel.hotelName.contains("Beach", ignoreCase = true) || hotel.hotelName.contains("Plaj", ignoreCase = true) || hotel.hotelName.contains("Resort", ignoreCase = true) || hotel.description.contains("Sahil", ignoreCase = true) || hotel.description.contains("Plaj", ignoreCase = true) || hotel.description.contains("1.", ignoreCase = true))) || (selectedBeachLine == 2 && (hotel.description.contains("2.", ignoreCase = true) || hotel.description.contains("500", ignoreCase = true))) || (selectedBeachLine == 3)
                    val mealMatch = selectedMealTypes.isEmpty() || selectedMealTypes.any { mt -> hotel.mealType.contains(mt, ignoreCase = true) }
                    val ratingMatch = minRating <= 0.0 || (hotel.ratingScore ?: (hotel.stars.toDouble())) >= minRating
                    val hotelMatch = selectedHotels.isEmpty() || selectedHotels.contains(hotel.hotelName.trim())
                    val amenityMatch = selectedAmenities.isEmpty() || selectedAmenities.all { am ->
                        val amLower = am.lowercase()
                        val inDesc = hotel.description.contains(am, ignoreCase = true)
                        val inHotelName = when {
                            amLower.contains("aqua") || amLower.contains("su kaydırağı") || amLower.contains("аква") -> hotel.hotelName.contains("Aqua", ignoreCase = true) || hotel.description.contains("Aqua", ignoreCase = true)
                            amLower.contains("spa") || amLower.contains("спа") -> hotel.hotelName.contains("Spa", ignoreCase = true) || hotel.description.contains("Spa", ignoreCase = true)
                            amLower.contains("plaj") || amLower.contains("beach") || amLower.contains("пляж") -> hotel.hotelName.contains("Beach", ignoreCase = true) || hotel.hotelName.contains("Plaj", ignoreCase = true) || hotel.description.contains("Plaj", ignoreCase = true)
                            amLower.contains("havuz") || amLower.contains("pool") || amLower.contains("бассейн") -> hotel.hotelName.contains("Resort", ignoreCase = true) || hotel.hotelName.contains("Hotel", ignoreCase = true) || hotel.description.contains("Havuz", ignoreCase = true)
                            amLower.contains("wifi") || amLower.contains("wi-fi") || amLower.contains("вайфай") -> true
                            else -> inDesc
                        }
                        inHotelName || inDesc
                    }
                    val directFlightMatch = !isDirectFlightOnly || !hotel.flightCode.contains("-") || hotel.flightCode.isNotBlank()
                    val transferMatch = !isTransferIncludedOnly || true
                    val instantMatch = !isInstantOnly || hotel.isInstantConfirmation
                    val operatorMatch = selectedOperators.isEmpty() || selectedOperators.any { op ->
                        hotel.operatorName.equals(op, ignoreCase = true) ||
                        hotel.agencyPrices.any { ap -> ap.operatorName.equals(op, ignoreCase = true) }
                    }

                    starMatch && beachMatch && mealMatch && ratingMatch && hotelMatch && amenityMatch && directFlightMatch && transferMatch && instantMatch && operatorMatch
                }
            }

            // ── DETAYLI FİLTRELER BİLEŞENİ (ARAMA SONUÇLARI PENCERESİNİN ÜST KISMI) ──
            UniversalTourDetailedFilters(
                activeSearchTab = "TOURS",
                isExpanded = isFilterExpanded,
                onExpandedToggle = { isFilterExpanded = !isFilterExpanded },
                onResetAllFilters = {
                    selectedBeachLine = 0
                    selectedMealTypes = emptySet()
                    selectedStars = emptySet()
                    minRating = 0.0
                    selectedHotels = emptySet()
                    selectedAmenities = emptySet()
                    isInstantOnly = false
                    isDirectFlightOnly = false
                    isTransferIncludedOnly = false
                    selectedOperators = emptySet()
                },
                selectedBeachLine = selectedBeachLine,
                onBeachLineChange = { selectedBeachLine = it },
                dbMealTypes = availableMealTypes,
                selectedMealTypes = selectedMealTypes,
                onMealTypesChange = { selectedMealTypes = it },
                selectedStars = selectedStars,
                onStarsChange = { selectedStars = it },
                minRating = minRating,
                onMinRatingChange = { minRating = it },
                dbProductHotels = availableHotels,
                selectedHotels = selectedHotels,
                onHotelsChange = { selectedHotels = it },
                selectedAmenities = selectedAmenities,
                onAmenitiesChange = { selectedAmenities = it },
                isInstantOnly = isInstantOnly,
                onInstantOnlyChange = { isInstantOnly = it },
                isDirectFlightOnly = isDirectFlightOnly,
                onDirectFlightOnlyChange = { isDirectFlightOnly = it },
                isTransferIncludedOnly = isTransferIncludedOnly,
                onTransferIncludedOnlyChange = { isTransferIncludedOnly = it },
                dbOperators = availableOperators,
                selectedOperators = selectedOperators,
                onOperatorsChange = { selectedOperators = it }
            )

            val pageSize = 15
            var currentPage by remember(filteredHotels) { mutableStateOf(1) }
            val totalPages = maxOf(1, (filteredHotels.size + pageSize - 1) / pageSize)
            val safeCurrentPage = currentPage.coerceIn(1, totalPages)
            val pagedHotels = remember(filteredHotels, safeCurrentPage) {
                filteredHotels.drop((safeCurrentPage - 1) * pageSize).take(pageSize)
            }

            val resultsScrollState = rememberScrollState()
            LaunchedEffect(safeCurrentPage) {
                resultsScrollState.scrollTo(0)
            }

            // Tek Satırlık Fırsat / Arama Sonuçları Listesi (İç Kaydırma Çubuğu ile Sabit Yükseklikte Pencere)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 580.dp)
            ) {
                if (filteredHotels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔍", fontSize = 32.sp)
                            Text(
                                text = AppLanguageManager.translate("Seçilen filtrelere uygun otel veya tur bulunamadı."),
                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(resultsScrollState)
                            .padding(end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pagedHotels.forEach { hotel ->
                            HotelSearchResultAccordionCard(
                                hotel = hotel,
                                onHotelClick = onHotelClick,
                                onSelectAndBook = onSelectAndBook
                            )
                        }
                    }

                    TourOSVerticalScrollbar(
                        scrollState = resultsScrollState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(end = 2.dp)
                    )
                }
            }

            // ── 📄 ALT SAYFALAMA (PAGINATION) KONTROLLERİ ──
            if (totalPages > 1) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fromIndex = (safeCurrentPage - 1) * pageSize + 1
                        val toIndex = minOf(safeCurrentPage * pageSize, filteredHotels.size)
                        val totalCount = filteredHotels.size
                        val paginationText = when (AppLanguageManager.currentLanguage.value.code) {
                            "ru" -> "Показано $fromIndex - $toIndex из $totalCount результатов"
                            "en" -> "Showing $fromIndex - $toIndex of $totalCount results"
                            else -> "Toplam $totalCount sonuçtan $fromIndex - $toIndex gösteriliyor"
                        }
                        Text(
                            text = paginationText,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Medium, fontSize = 11.5.sp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Önceki Butonu
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = safeCurrentPage > 1) { currentPage = safeCurrentPage - 1 },
                                color = if (safeCurrentPage > 1) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (safeCurrentPage > 1) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "◀ ${AppLanguageManager.translate("Önceki")}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = TourOSTypography.Caption.copy(
                                        color = if (safeCurrentPage > 1) Color(0xFF0F5A56) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            // Sayfa Numaraları
                            val startPage = maxOf(1, safeCurrentPage - 2)
                            val endPage = minOf(totalPages, startPage + 4)
                            (startPage..endPage).forEach { pageNum ->
                                val isSelected = (pageNum == safeCurrentPage)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { currentPage = pageNum },
                                    color = if (isSelected) Color(0xFF0F5A56) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF0F5A56) else Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$pageNum",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = TourOSTypography.Caption.copy(
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Sonraki Butonu
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(enabled = safeCurrentPage < totalPages) { currentPage = safeCurrentPage + 1 },
                                color = if (safeCurrentPage < totalPages) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (safeCurrentPage < totalPages) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${AppLanguageManager.translate("Sonraki")} ▶",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = TourOSTypography.Caption.copy(
                                        color = if (safeCurrentPage < totalPages) Color(0xFF0F5A56) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 🏨 AKORDİYON ODA LİSTELİ OTEL KART BİLEŞENİ (EXPANDABLE ROOM ACCORDION) ───
@Composable
private fun HotelSearchResultAccordionCard(
    hotel: PublicHotelOffer,
    onHotelClick: (PublicHotelOffer) -> Unit,
    onSelectAndBook: (PublicHotelOffer) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isFlight = hotel.category.uppercase() == "FLIGHT" || 
                   hotel.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                   hotel.hotelName.startsWith("✈️", ignoreCase = true)
    val isHotelOnly = !isFlight && (hotel.category.uppercase() == "HOTEL" || hotel.category.uppercase() == "OTEL")
    val hasMultipleRooms = !isFlight && hotel.agencyPrices.size > 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isExpanded) Color(0xFF0F5A56).copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
        shadowElevation = if (isExpanded) 2.dp else 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ─── ANA ÜST SATIR (Otel Bilgileri + Fiyat + Butonlar) ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (hasMultipleRooms) {
                            isExpanded = !isExpanded 
                        } else {
                            onHotelClick(hotel)
                        }
                    }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Küçük Resim (Sol)
                if (isFlight) {
                    androidx.compose.foundation.Image(
                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.flight),
                        contentDescription = hotel.hotelName,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 95.dp, height = 68.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    AsyncImage(
                        model = getEffectiveImageUrl(hotel),
                        contentDescription = hotel.hotelName,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 95.dp, height = 68.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Orta Detaylar
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. ÜST SATIR (Başlık + Yıldız + Rozetler)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = hotel.hotelName,
                            style = TourOSTypography.BodyMedium.copy(
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (!isFlight && hotel.stars > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(hotel.stars.coerceIn(1, 5)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Operatör Rozeti
                        if (hotel.operatorName.isNotBlank()) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "💼 ${hotel.operatorName}",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                    style = TourOSTypography.Caption.copy(
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.5.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }

                        // Kategoriye Özel Rozetler
                        if (isFlight) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = "🟢 ${AppLanguageManager.translate("Direkt Charter")}",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp),
                                    maxLines = 1
                                )
                            }
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = "🧳 ${hotel.baggageKg} ${AppLanguageManager.translate("kg Bagaj")}",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.Medium, fontSize = 9.5.sp),
                                    maxLines = 1
                                )
                            }
                        } else {
                            if (hotel.nights > 0) {
                                Surface(
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFBBF7D0))
                                ) {
                                    Text(
                                        text = "🌙 ${hotel.nights} ${AppLanguageManager.translate("Gece")}",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                            val tourDateRange = remember(hotel.departureDate, hotel.returnDate) {
                                val dep = hotel.departureDate?.takeIf { it.isNotBlank() } ?: "02.09.2026"
                                val ret = hotel.returnDate?.takeIf { it.isNotBlank() }
                                if (!ret.isNullOrBlank() && ret != dep) "$dep — $ret" else dep
                            }
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = "📅 $tourDateRange",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp),
                                    maxLines = 1
                                )
                            }
                            if (hotel.departureCity.isNotBlank()) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                                ) {
                                    Text(
                                        text = "🛫 ${AppLanguageManager.translate(hotel.departureCity)}",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                            if (isHotelOnly) {
                                Surface(
                                    color = Color(0xFFFAF5FF),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFE9D5FF))
                                ) {
                                    Text(
                                        text = "🏨 ${AppLanguageManager.translate("Sadece Otel")}",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF7E22CE), fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (hotel.discountPercent != null && hotel.discountPercent > 0) {
                            Surface(
                                color = Color(0xFFDC2626),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "%${hotel.discountPercent} ${AppLanguageManager.translate("İNDİRİM")}",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                )
                            }
                        }
                    }

                    // 2. ALT DETAY SATIRI
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isFlight) {
                            Text(
                                text = "🛫 ${hotel.flightCode.ifBlank { "VKO ➔ AYT (Direkt)" }}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            Text(
                                text = "📍 ${hotel.location}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            Text(
                                text = "🎟️ ${AppLanguageManager.translate("Ekonomi Kabin")}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontSize = 11.sp),
                                maxLines = 1
                            )
                            Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            Text(
                                text = "⚡ ${AppLanguageManager.translate("Anında PNR")}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Medium, fontSize = 11.sp),
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = "📍 ${hotel.location}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (hotel.mealType.isNotBlank()) {
                                Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "🍴 ${AppLanguageManager.translate(hotel.mealType)}",
                                    style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.Medium, fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            if (hasMultipleRooms) {
                                Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = "🛏️ ${hotel.agencyPrices.size} ${AppLanguageManager.translate("Farklı Seçenek")}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                        maxLines = 1
                                    )
                                }
                            } else if (hotel.roomType.isNotBlank()) {
                                Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "🛏️ ${AppLanguageManager.translate(hotel.roomType)}",
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            if (!isHotelOnly && hotel.flightCode.isNotBlank()) {
                                val cleanFlightCode = hotel.flightCode
                                    .replace("FL-TV", "Charter", ignoreCase = true)
                                    .replace("Charter Airlines", "Charter", ignoreCase = true)
                                    .replace("Charter Charter", "Charter", ignoreCase = true)
                                    .trim()
                                Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "✈️ ${AppLanguageManager.translate(cleanFlightCode)}",
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Sağ Taraf (Fiyat ve Butonlar)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        val guestsCount = 2
                        val nightsCount = if (hotel.nights > 0) hotel.nights else 7
                        val priceSubtext = when {
                            isFlight -> "$guestsCount ${AppLanguageManager.translate("Yolcu Toplam")}"
                            isHotelOnly -> "$guestsCount ${AppLanguageManager.translate("Kişi")} • ${nightsCount}${AppLanguageManager.translate("G")} ${AppLanguageManager.translate("Otel")}"
                            else -> "$guestsCount ${AppLanguageManager.translate("Kişi")} • ${nightsCount}${AppLanguageManager.translate("G")} ${AppLanguageManager.translate("Toplam")}"
                        }
                        Text(
                            text = priceSubtext,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        val pricePrefix = if (hasMultipleRooms) "${AppLanguageManager.translate("Başlayan")} " else ""
                        Text(
                            text = "$pricePrefix${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.minPrice, decimals = false)} ${hotel.currency}",
                            style = TourOSTypography.TitleMedium.copy(
                                color = Color(0xFF0F5A56),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    // + / ⌄ Butonu (Odaları Göster / Gizle)
                    if (hasMultipleRooms) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isExpanded = !isExpanded },
                            color = if (isExpanded) Color(0xFF0F5A56) else Color(0xFF0F5A56).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF0F5A56).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) "▲" else "▼",
                                    color = if (isExpanded) Color.White else Color(0xFF0F5A56),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isExpanded) AppLanguageManager.translate("Kapat") else "${AppLanguageManager.translate("Odalar")} (${hotel.agencyPrices.size})",
                                    color = if (isExpanded) Color.White else Color(0xFF0F5A56),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onSelectAndBook(hotel) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A56)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        val buttonText = when {
                            isFlight -> "${AppLanguageManager.translate("Uçuş Seç")} ➔"
                            isHotelOnly -> "${AppLanguageManager.translate("Oda Seç")} ➔"
                            else -> "${AppLanguageManager.translate("Rezerve Et")} ➔"
                        }
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ─── 🛏️ AÇILIR AKORDİYON ODA LİSTESİ (ACCORDION ROOM LIST) ───
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                    Text(
                        text = "🛏️ ${AppLanguageManager.translate("Mevcut Oda ve Pansiyon Seçenekleri")} (${hotel.agencyPrices.size}):",
                        style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    hotel.agencyPrices.forEach { option ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, if (option.isBestDeal) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                            shadowElevation = 0.5.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sol: Oda + Pansiyon + Operatör + Gece/Tarih
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "🛏️ ${AppLanguageManager.translate(option.roomType)}",
                                        style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    )
                                    Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                    Text(
                                        text = "🍴 ${AppLanguageManager.translate(option.boardType)}",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    )
                                    if (option.operatorName.isNotBlank()) {
                                        Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                        Text(
                                            text = "💼 ${option.operatorName}",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.5.sp)
                                        )
                                    }
                                    if (option.nights > 0) {
                                        Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                        Text(
                                            text = "🌙 ${option.nights} ${AppLanguageManager.translate("Gece")}",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Medium, fontSize = 10.5.sp)
                                        )
                                    }
                                    if (option.departureDate.isNotBlank()) {
                                        Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                        val optDates = if (option.returnDate.isNotBlank() && option.returnDate != option.departureDate) "${option.departureDate} — ${option.returnDate}" else option.departureDate
                                        Text(
                                            text = "📅 $optDates",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF1E40AF), fontSize = 10.5.sp)
                                        )
                                    }
                                    if (option.isBestDeal) {
                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFFA7F3D0))
                                        ) {
                                            Text(
                                                text = "✨ ${AppLanguageManager.translate("En İyi Fiyat")}",
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            )
                                        }
                                    }
                                }

                                // Sağ: Fiyat + Seç & Rezerve Et Butonu
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(option.price, decimals = false)} ${hotel.currency}",
                                        style = TourOSTypography.TitleSmall.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    )

                                    Button(
                                        onClick = {
                                            val selectedOffer = hotel.copy(
                                                minPrice = option.price,
                                                roomType = option.roomType,
                                                mealType = option.boardType,
                                                operatorName = option.operatorName,
                                                nights = if (option.nights > 0) option.nights else hotel.nights,
                                                departureDate = option.departureDate.ifBlank { hotel.departureDate },
                                                returnDate = option.returnDate.ifBlank { hotel.returnDate }
                                            )
                                            onSelectAndBook(selectedOffer)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A56)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${AppLanguageManager.translate("Rezerve Et")} ➔",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 🌟 YATAY KAYDIRILABİLİR TEMATİK ÜRÜN SEKSİYONU (SCREENSHOT BİREBİR) ────────
@Composable
fun HorizontalProductSection(
    titleVectorIcon: ImageVector? = null,
    title: String,
    subtitle: String,
    hotels: List<PublicHotelOffer>,
    onHotelClick: (PublicHotelOffer) -> Unit,
    onSelectAndBook: (PublicHotelOffer) -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row (Vektör İkon + Başlık + Alt Başlık + Vektör Oklar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (titleVectorIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = titleVectorIcon,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = AppLanguageManager.translate(title),
                            style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        )
                        Text(
                            text = AppLanguageManager.translate(subtitle),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sol Vektör Ok (←)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable {
                                scope.launch {
                                    scrollState.animateScrollBy(-340f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Önceki",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Sağ Vektör Ok (→)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7))
                            .clickable {
                                scope.launch {
                                    scrollState.animateScrollBy(340f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Sonraki",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Yatay Ürün Kartları Listesi (LazyRow)
            LazyRow(
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(hotels) { hotel ->
                    HorizontalHotelCard(
                        hotel = hotel,
                        onClick = { onHotelClick(hotel) },
                        onSelectAndBook = onSelectAndBook
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalHotelCard(
    hotel: PublicHotelOffer,
    modifier: Modifier = Modifier.width(350.dp),
    onClick: () -> Unit,
    onSelectAndBook: ((PublicHotelOffer) -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Üst Görsel Alanı + Rozetler
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                val isFlightItem = hotel.category.uppercase() == "FLIGHT" || 
                                   hotel.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                                   hotel.hotelName.startsWith("✈️", ignoreCase = true)
                if (isFlightItem) {
                    androidx.compose.foundation.Image(
                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.flight),
                        contentDescription = hotel.hotelName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = getEffectiveImageUrl(hotel),
                        contentDescription = hotel.hotelName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Sol Üst Gerçek İndirim Rozeti (% İndirim)
                if (hotel.discountPercent != null && hotel.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEF4444)) // Canlı Kırmızı
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "%${hotel.discountPercent} ${AppLanguageManager.translate("İNDİRİM")}",
                            style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                        )
                    }
                }

                // Sağ Üst Mavi Yıldız Rozeti (Vektör Yıldızlar ⭐)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E3A8A).copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(hotel.stars.coerceIn(1, 5)) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            // Alt Detay Alanı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isFlightCard = hotel.category == "FLIGHT"

                // Başlık + Sağ Rozet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cleanHeaderFlight = hotel.flightCode
                        .replace("FL-TV", "Charter", ignoreCase = true)
                        .replace("Charter Airlines", "Charter", ignoreCase = true)
                        .replace("Charter Charter", "Charter", ignoreCase = true)
                        .trim()
                    Text(
                        text = if (isFlightCard) AppLanguageManager.translate(cleanHeaderFlight.ifBlank { "Charter" }) else hotel.hotelName,
                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isFlightCard) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Direkt Uçuş"),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Operatör Çipi ve Uçuş Kodu / Bagaj Çipi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val operatorChipText = if (hotel.agencyPrices.size > 1) {
                        "${hotel.agencyPrices.size} ${AppLanguageManager.translate("Operatör Teklifi")}"
                    } else {
                        hotel.operatorName
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (hotel.agencyPrices.size > 1) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = operatorChipText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TourOSTypography.Caption.copy(
                                color = if (hotel.agencyPrices.size > 1) Color(0xFF15803D) else Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isFlightCard) {
                        val baggageText = if (hotel.baggageKg > 0) "${hotel.baggageKg} ${AppLanguageManager.translate("kg Bagaj")}" else AppLanguageManager.translate("El Bagajı")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = baggageText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (hotel.flightCode.isNotBlank()) {
                        val cleanFlightCode = hotel.flightCode
                            .replace("FL-TV", "Charter", ignoreCase = true)
                            .replace("Charter Airlines", "Charter", ignoreCase = true)
                            .replace("Charter Charter", "Charter", ignoreCase = true)
                            .trim()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = AppLanguageManager.translate(cleanFlightCode),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                val publicDestinationText = remember(hotel.location, hotel.hotelName) {
                    val loc = hotel.location.trim()
                    when {
                        loc.contains("Kemer", ignoreCase = true) -> "🇹🇷 Турция · Antalya · Kemer"
                        loc.contains("Belek", ignoreCase = true) -> "🇹🇷 Турция · Antalya · Belek"
                        loc.contains("Lara", ignoreCase = true) || loc.contains("Kundu", ignoreCase = true) -> "🇹🇷 Турция · Antalya · Lara"
                        loc.contains("Alanya", ignoreCase = true) -> "🇹🇷 Турция · Antalya · Alanya"
                        loc.contains("Side", ignoreCase = true) || loc.contains("Manavgat", ignoreCase = true) || loc.contains("Çolaklı", ignoreCase = true) || loc.contains("Kumköy", ignoreCase = true) -> "🇹🇷 Турция · Antalya · Side"
                        loc.contains("Bodrum", ignoreCase = true) -> "🇹🇷 Турция · Muğla · Bodrum"
                        loc.contains("Marmaris", ignoreCase = true) -> "🇹🇷 Турция · Muğla · Marmaris"
                        loc.contains("Fethiye", ignoreCase = true) -> "🇹🇷 Турция · Muğla · Fethiye"
                        loc.contains("Moskova", ignoreCase = true) || loc.contains("Rusya", ignoreCase = true) -> "🇷🇺 Rusya · Moskova"
                        loc.contains("Dubai", ignoreCase = true) || loc.contains("BAE", ignoreCase = true) -> "🇦🇪 BAE · Dubai"
                        loc.contains("Şarm", ignoreCase = true) || loc.contains("Hurgada", ignoreCase = true) || loc.contains("Mısır", ignoreCase = true) -> "🇪🇬 Mısır · Şarm El-Şeyh"
                        loc.contains("Phuket", ignoreCase = true) || loc.contains("Пхукет", ignoreCase = true) || loc.contains("Tayland", ignoreCase = true) -> "🇹🇭 Tayland · Phuket"
                        loc.contains("Vietnam", ignoreCase = true) || loc.contains("Nha Trang", ignoreCase = true) -> "🇻🇳 Vietnam · Nha Trang"
                        loc.isNotBlank() -> loc
                        else -> "🇹🇷 Турция · Antalya"
                    }
                }

                // Lokasyon & Tur / Uçuş Bilgisi
                val flightDepartureInfo = if (hotel.departureCity.isNotBlank()) {
                    val depCityPhrase = when {
                        hotel.departureCity.contains("Moskova", ignoreCase = true) || hotel.departureCity.contains("Moscow", ignoreCase = true) || hotel.departureCity.contains("Москва", ignoreCase = true) ->
                            AppLanguageManager.translate("Moskova Kalkışlı")
                        hotel.departureCity.contains("Antalya", ignoreCase = true) || hotel.departureCity.contains("Анталья", ignoreCase = true) ->
                            AppLanguageManager.translate("Antalya Kalkışlı")
                        hotel.departureCity.contains("İstanbul", ignoreCase = true) || hotel.departureCity.contains("Istanbul", ignoreCase = true) || hotel.departureCity.contains("Стамбул", ignoreCase = true) ->
                            AppLanguageManager.translate("İstanbul Kalkışlı")
                        else ->
                            "${AppLanguageManager.translate("Kalkış")}: ${AppLanguageManager.translate(hotel.departureCity)}"
                    }
                    "$depCityPhrase · ${AppLanguageManager.translate("Ekonomi Sınıfı")} · ${AppLanguageManager.translate("Gidiş-Dönüş")}"
                } else {
                    "${AppLanguageManager.translate("Direkt Uçuş")} · ${AppLanguageManager.translate("Ekonomi Sınıfı")} · ${AppLanguageManager.translate("Gidiş-Dönüş")}"
                }
                Text(
                    text = if (isFlightCard) flightDepartureInfo else "${AppLanguageManager.translate(publicDestinationText)} · ${hotel.nights} ${AppLanguageManager.translate("Gece")} · ${hotel.mealType}",
                    style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── GERÇEK FİYAT VE TEKLİF ALANI ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val currSymbol = if (hotel.currency == "RUB") "RUB" else "₺"
                        val hasMultiPrice = hotel.maxPrice > hotel.minPrice && hotel.agencyPrices.size > 1
                        if (hasMultiPrice) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${AppLanguageManager.translate("En Düşük Fiyat")}:", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                Text("${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.minPrice, decimals = false)} $currSymbol", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${AppLanguageManager.translate("En Yüksek Fiyat")}:", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                Text("${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.maxPrice, decimals = false)} $currSymbol", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp))
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${AppLanguageManager.translate("Fiyat")}:", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                Text("${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.minPrice, decimals = false)} $currSymbol", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp))
                            }
                        }
                    }

                    // Turu / Uçuşu Seç & Detaylandır Butonu
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (onSelectAndBook != null) {
                                    onSelectAndBook(hotel)
                                } else {
                                    onClick()
                                }
                            },
                        color = Color(0xFF1E4D58) // Teal Dark Button
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Rezerve Et"),
                                style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessFooterSection(
    companySettings: CompanySettings?,
    isWhitelabelAgencyMode: Boolean = false,
    whitelabelAgencyName: String? = null,
    whitelabelPhone: String? = null,
    whitelabelEmail: String? = null,
    whitelabelWhatsapp: String? = null,
    whitelabelAddress: String? = null,
    onNavigateToLogin: () -> Unit
) {
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F172A) // Dark Slate 900 Business Background
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1320.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // ── ÜST KOLON LİSTESİ (Sadece Kurumsal İletişim ve Yasal & Lisans) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Kolon 1: İletişim Bilgileri (Web Panel Yönetiminden Canlı)
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = AppLanguageManager.translate("Kurumsal İletişim", currentLanguage.code),
                            style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )

                        val phoneVal = (if (isWhitelabelAgencyMode && !whitelabelPhone.isNullOrBlank()) whitelabelPhone else companySettings?.webPhone).orEmpty()
                        val emailVal = (if (isWhitelabelAgencyMode && !whitelabelEmail.isNullOrBlank()) whitelabelEmail else companySettings?.webEmail).orEmpty()
                        val whatsappVal = (if (isWhitelabelAgencyMode && !whitelabelWhatsapp.isNullOrBlank()) whitelabelWhatsapp else companySettings?.webWhatsapp).orEmpty()
                        val addressVal = (if (isWhitelabelAgencyMode && !whitelabelAddress.isNullOrBlank()) whitelabelAddress else companySettings?.webAddress).orEmpty()

                        if (phoneVal.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                                Text("${AppLanguageManager.translate("Tel:", currentLanguage.code)} $phoneVal", style = TourOSTypography.Caption.copy(color = Color(0xFFCBD5E1), fontSize = 12.sp))
                            }
                        }
                        if (whatsappVal.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Text("WhatsApp: $whatsappVal", style = TourOSTypography.Caption.copy(color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                            }
                        }
                        if (emailVal.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                                Text("${AppLanguageManager.translate("E-Posta:", currentLanguage.code)} $emailVal", style = TourOSTypography.Caption.copy(color = Color(0xFFCBD5E1), fontSize = 12.sp))
                            }
                        }
                        if (addressVal.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Text("${AppLanguageManager.translate("Adres:", currentLanguage.code)} $addressVal", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp), maxLines = 2)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Kolon 2: Yasal Bilgiler & Güvenlik
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = AppLanguageManager.translate("Yasal & Lisans", currentLanguage.code),
                            style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                        val mersis = companySettings?.webMersisNo.orEmpty()
                        val taxNo = companySettings?.webTaxNumber.orEmpty()
                        val taxOffice = companySettings?.webTaxOffice.orEmpty()

                        if (mersis.isNotBlank()) Text("${AppLanguageManager.translate("MERSİS No:", currentLanguage.code)} $mersis", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp))
                        if (taxOffice.isNotBlank()) Text("${AppLanguageManager.translate("Vergi D.:", currentLanguage.code)} $taxOffice", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp))
                        if (taxNo.isNotBlank()) Text("${AppLanguageManager.translate("Vergi No:", currentLanguage.code)} $taxNo", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                                Text("SSL 256-Bit", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = TourOSTypography.Caption.copy(color = Color(0xFF10B981), fontSize = 9.sp))
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                                Text(AppLanguageManager.translate("TURSAB A-Grubu", currentLanguage.code), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = TourOSTypography.Caption.copy(color = Color(0xFF38BDF8), fontSize = 9.sp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // ── ALT BAR: TELİF & MGA CREATIVE HAKLARI ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val compName = if (isWhitelabelAgencyMode) {
                        whitelabelAgencyName?.takeIf { it.isNotBlank() && !it.equals("axileto", ignoreCase = true) && !it.equals("TourOS", ignoreCase = true) }
                            ?: companySettings?.name?.takeIf { it.isNotBlank() && !it.equals("axileto", ignoreCase = true) && !it.equals("TourOS", ignoreCase = true) }
                            ?: "Online Rezervasyon Portalı"
                    } else {
                        companySettings?.name?.ifBlank { "Axileto" } ?: "Axileto"
                    }
                    val rightsReserved = AppLanguageManager.translate("Tüm hakları saklıdır.", currentLanguage.code)
                    val rawFooter = if (isWhitelabelAgencyMode) "" else companySettings?.footerText?.trim().orEmpty()
                    val footerCopyright = if (rawFooter.isNotBlank()) {
                        rawFooter
                            .replace("Tüm hakları saklıdır.", rightsReserved, ignoreCase = true)
                            .replace("All rights reserved.", rightsReserved, ignoreCase = true)
                    } else {
                        "© 2026 $compName. $rightsReserved"
                    }
                    Text(
                        text = footerCopyright,
                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                    )
                    if (!isWhitelabelAgencyMode) {
                        Text(
                            text = "Powered by MGA Creative Software Architecture",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    }
}

// ── 🌟 HİZMETLERİMİZ (OUR SERVICES - SCREENSHOT BİREBİR KART GRID SEKSİYONU) ────────
@Composable
fun OurServicesSection(
    companySettings: CompanySettings? = null,
    isWhitelabelAgencyMode: Boolean = false,
    onSelectService: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1320.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                if (!isWhitelabelAgencyMode) {
                    // 📥 Uygulama İndirme Butonları (Windows Desktop .exe & Android .apk)
                    val uriHandler = LocalUriHandler.current
                    val desktopUrl = companySettings?.desktopAppUrl?.trim().takeIf { !it.isNullOrBlank() } ?: "https://axileto.com/downloads/TourOS-Desktop.exe"
                    val apkUrl = companySettings?.androidApkUrl?.trim().takeIf { !it.isNullOrBlank() } ?: "https://storage.yandexcloud.net/axileto-downloads/TourOS-Mobile.apk"

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .widthIn(min = 200.dp, max = 240.dp)
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                runCatching { uriHandler.openUri(desktopUrl) }
                            },
                        color = Color(0xFF0D5653),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Windows Masaüstü (.exe)"),
                                style = TourOSTypography.BodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = AppLanguageManager.translate("Hemen İndir & Kur"),
                                style = TourOSTypography.Caption.copy(
                                    color = Color(0xFFB0ECE4),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        modifier = Modifier
                            .widthIn(min = 200.dp, max = 240.dp)
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                runCatching { uriHandler.openUri(apkUrl) }
                            },
                        color = Color(0xFF1E293B),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppLanguageManager.translate("Android Mobil (.apk)"),
                                style = TourOSTypography.BodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = AppLanguageManager.translate("Doğrudan İndir & Yükle"),
                                style = TourOSTypography.Caption.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

                // 6'lı Kart Grid Düzeni (3 Kolon x 2 Satır) - Şimdilik UI'da gizlendi
                val showServiceCardsGrid = false
                if (showServiceCardsGrid) {
                    val services = companySettings?.getEffectiveServiceCards() ?: listOf(
                        com.mgacreative.touros.domain.model.ServiceCardItem("1", "Paket Turlar / Tour Packages", "Gezginler için özel seçilmiş her şey dahil paket tur seçenekleri ve rehberli geziler.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800", "PACKAGE_TOUR"),
                        com.mgacreative.touros.domain.model.ServiceCardItem("2", "Otel Rezervasyonları / Hotel Reservations", "En uygun fiyat garantili seçkin 5 yıldızlı oteller, tatil köyleri ve ayrıcalıklı konaklama.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800", "HOTEL"),
                        com.mgacreative.touros.domain.model.ServiceCardItem("3", "Macera Turları / Adventure Tours", "Safari, trekking, kültür turları ve heyecan dolu özel tatil rotaları.", "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800", "ADVENTURE"),
                        com.mgacreative.touros.domain.model.ServiceCardItem("4", "Seyahat Desteği / Travel Assistance", "Sorunsuz bir seyahat deneyimi için 7/24 canlı müşteri desteği ve acente danışmanlığı.", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800", "ASSISTANCE"),
                        com.mgacreative.touros.domain.model.ServiceCardItem("5", "Uçuş Rezervasyonu / Flight Booking", "Hızlı, uygun fiyatlı yurt içi ve yurt dışı charter ve tarifeli uçuş biletleri.", "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800", "FLIGHT"),
                        com.mgacreative.touros.domain.model.ServiceCardItem("6", "Mavi Yolculuk & Cruise / Cruise Trips", "Lüks cruise gemileri ve büyüleyici koyları keşfedeceğiniz mavi yolculuk paketleri.", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800", "CRUISE")
                    )

                    // 2 Satırlı Grid (Her Satırda 3 Kart)
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        services.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                rowItems.forEach { service ->
                                    val targetUrl = if (!service.hotelName.isNullOrBlank()) {
                                        "HOTEL_TOURS:${service.hotelName}"
                                    } else {
                                        service.targetUrl.ifBlank { service.hotelId?.let { "HOTEL_TOURS:$it" } ?: service.id }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(175.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onSelectService(targetUrl) },
                                        color = Color(0xFF0D5653), // Screenshot ile Birebir Koyu Teal
                                        shadowElevation = 6.dp
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            val imgUrl = service.imageUrl.trim()
                                            if (imgUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = imgUrl,
                                                    contentDescription = service.title,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                // Dark Overlay Gradient
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                                colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.85f))
                                                            )
                                                        )
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = service.title,
                                                        style = TourOSTypography.TitleMedium.copy(
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        ),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                    Text(
                                                        text = service.subtitle,
                                                        style = TourOSTypography.Caption.copy(
                                                            color = Color(0xFFE2E8F0),
                                                            fontSize = 11.sp,
                                                            lineHeight = 15.sp
                                                        ),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        maxLines = 3,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ServiceCardData(
    val id: String,
    val icon: String,
    val title: String,
    val description: String
)

// ── 🏨 TEK KART METASEARCH GRUPLAMA FONKSİYONU ───────────────────────────────
fun groupOffersByHotelName(rawOffers: List<PublicHotelOffer>): List<PublicHotelOffer> {
    if (rawOffers.isEmpty()) return emptyList()

    val validOffers = rawOffers.filter { offer ->
        val isFlight = offer.category.uppercase() == "FLIGHT" || 
                       offer.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                       offer.hotelName.startsWith("✈️", ignoreCase = true)
        isFlight || TourOperatorConfig.isAllowedOperator(offer.operatorName)
    }
    if (validOffers.isEmpty()) return emptyList()

    val grouped = validOffers.groupBy { offer ->
        if (offer.category.uppercase() == "FLIGHT") {
            val dep = offer.departureDate ?: ""
            val ret = offer.returnDate ?: ""
            "FLIGHT_${offer.flightCode}_${dep}_${ret}_${offer.location}"
        } else {
            val normName = offer.hotelName.trim().lowercase()
                .replace(" hotel", "")
                .replace(" resort", "")
                .replace(" spa", "")
                .replace(" &", "")
                .trim()
            val cCode = offer.countryCode.ifBlank { "TR" }
            "HOTEL_${cCode}_$normName"
        }
    }

    return grouped.map { (_, list) ->
        val first = list.first()

        val combinedAgencyPrices = mutableListOf<AgencyPriceOption>()
        list.forEach { item ->
            val isFlight = item.category.uppercase() == "FLIGHT" || 
                           item.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                           item.hotelName.startsWith("✈️", ignoreCase = true)
            if (item.agencyPrices.isNotEmpty()) {
                item.agencyPrices.forEach { p ->
                    val canonical = TourOperatorConfig.resolveCanonicalOperatorName(p.operatorName)
                    if (isFlight || canonical != null) {
                        val finalOp = canonical ?: p.operatorName
                        combinedAgencyPrices.add(
                            p.copy(
                                agencyName = finalOp,
                                operatorName = finalOp,
                                nights = if (p.nights > 0) p.nights else item.nights,
                                departureDate = p.departureDate.ifBlank { item.departureDate ?: "" },
                                returnDate = p.returnDate.ifBlank { item.returnDate ?: "" }
                            )
                        )
                    }
                }
            } else {
                val canonical = TourOperatorConfig.resolveCanonicalOperatorName(item.operatorName)
                if (isFlight || canonical != null) {
                    val finalOp = canonical ?: item.operatorName
                    combinedAgencyPrices.add(
                        AgencyPriceOption(
                            agencyId = item.id,
                            agencyName = finalOp,
                            operatorName = finalOp,
                            roomType = item.roomType,
                            boardType = item.mealType,
                            price = item.minPrice,
                            isBestDeal = false,
                            nights = item.nights,
                            departureDate = item.departureDate ?: "",
                            returnDate = item.returnDate ?: ""
                        )
                    )
                }
            }
        }

        val distinctPrices = combinedAgencyPrices.distinctBy { 
            "${it.operatorName}_${it.roomType}_${it.boardType}_${it.nights}_${it.departureDate}_${it.price.toInt()}" 
        }

        val minPrice = distinctPrices.minOfOrNull { it.price } ?: first.minPrice
        val maxPrice = distinctPrices.maxOfOrNull { it.price }?.coerceAtLeast(minPrice) ?: first.maxPrice

        val updatedPrices = distinctPrices.map { opt ->
            opt.copy(isBestDeal = (opt.price == minPrice))
        }.sortedBy { it.price }

        val hasLastMinute = list.any { it.isLastMinute }
        val maxDiscount = list.mapNotNull { it.discountPercent }.maxOrNull()

        val topOp = updatedPrices.firstOrNull()?.operatorName ?: (TourOperatorConfig.resolveCanonicalOperatorName(first.operatorName) ?: first.operatorName)

        first.copy(
            minPrice = minPrice,
            maxPrice = maxPrice,
            operatorName = topOp,
            roomType = updatedPrices.firstOrNull()?.roomType ?: first.roomType,
            mealType = updatedPrices.firstOrNull()?.boardType ?: first.mealType,
            isLastMinute = hasLastMinute || first.isLastMinute,
            discountPercent = maxDiscount ?: first.discountPercent,
            agencyPrices = updatedPrices
        )
    }
}

// ── 📜 ACENTA REZERVASYONLARI MODÜLÜ (PNR & BİLET YÖNETİMİ) ─────────────────
@Composable
fun AgencyBookingsModuleSection(
    bookings: List<Booking>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("HEPSİ") }

    val filteredBookings = bookings.filter { b ->
        (selectedTypeFilter == "HEPSİ" || 
            (selectedTypeFilter == "FLIGHT" && (b.bookingType == "FLIGHT" || b.bookingCode.startsWith("PNR"))) ||
            (selectedTypeFilter == "HOTEL" && (b.bookingType != "FLIGHT" && !b.bookingCode.startsWith("PNR")))) &&
        (searchQuery.isBlank() || 
            b.bookingCode.contains(searchQuery, ignoreCase = true) || 
            b.customerName.contains(searchQuery, ignoreCase = true) || 
            b.productName.contains(searchQuery, ignoreCase = true))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Başlık ve Yenileme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📜 Gelen Acente Rezervasyonları & PNR Yönetimi",
                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Acentenize ve referans kodunuza bağlı tüm bilet, tur ve otel rezervasyonlarının canlı listesi",
                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B))
                    )
                }

                TourOSButton(
                    text = "🔄 Canlı Yenile",
                    onClick = onRefresh,
                    variant = TourOSButtonVariant.SECONDARY
                )
            }

            // Özet İstatistik Rozetleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatMiniBadge("📋 Toplam Satış", "${bookings.size} Adet", Color(0xFF0284C7), Modifier.weight(1f))
                StatMiniBadge("✈️ Uçuş (PNR)", "${bookings.count { it.bookingType == "FLIGHT" || it.bookingCode.startsWith("PNR") }} Adet", Color(0xFF0D5653), Modifier.weight(1f))
                StatMiniBadge("🏨 Otel & Tur", "${bookings.count { it.bookingType != "FLIGHT" && !it.bookingCode.startsWith("PNR") }} Adet", Color(0xFFD97706), Modifier.weight(1f))
                StatMiniBadge("🟢 Onaylanan", "${bookings.count { it.status == BookingStatus.ONAYLANDI || it.status.name == "CONFIRMED" }} Adet", Color(0xFF16A34A), Modifier.weight(1f))
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Arama ve Kategori Filtresi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(AppLanguageManager.translate("PNR / Bilet Kodu veya Müşteri Adı ile Ara...")) },
                    modifier = Modifier.width(360.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HEPSİ", "FLIGHT", "HOTEL").forEach { fType ->
                        val label = when (fType) {
                            "FLIGHT" -> AppLanguageManager.translate("✈️ Uçuşlar")
                            "HOTEL" -> AppLanguageManager.translate("🏨 Oteller & Turlar")
                            else -> AppLanguageManager.translate("Tüm Biletler")
                        }
                        val isSel = (selectedTypeFilter == fType)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTypeFilter = fType },
                            color = if (isSel) Color(0xFF0D5653) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSel) Color.White else Color(0xFF475569),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Bilet Listesi
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(AppLanguageManager.translate("⏳ Rezervasyonlar yükleniyor..."), style = TourOSTypography.TitleMedium.copy(color = Color(0xFF64748B)))
                }
            } else if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(AppLanguageManager.translate("📭 Aradığınız kriterlere uygun henüz bir acente rezervasyonu bulunmamaktadır."), style = TourOSTypography.BodyMedium.copy(color = Color(0xFF94A3B8)))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredBookings.forEach { booking ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (booking.bookingType == "FLIGHT" || booking.bookingCode.startsWith("PNR")) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (booking.bookingType == "FLIGHT" || booking.bookingCode.startsWith("PNR")) "✈️" else "🏨",
                                            fontSize = 20.sp
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "PNR / Kod: ${booking.bookingCode}",
                                                style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFDCFCE7))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ONAYLANDI ✅", style = TourOSTypography.Caption.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                            }
                                        }

                                        Text(
                                            text = "👤 ${booking.customerName} • 📞 ${booking.customerPhone}",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontSize = 12.sp)
                                        )

                                        Text(
                                            text = "📦 Ürün: ${booking.productName} • Operatör: ${booking.operatorName}",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${booking.totalPrice.toInt()} ${booking.currency}",
                                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    )
                                    Text(
                                        text = "Acente Tenant: ${booking.tenantId.take(12)}...",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniBadge(title: String, value: String, accentColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp))
            Text(value, style = TourOSTypography.TitleLarge.copy(color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
        }
    }
}

@Composable
fun ModernDatePickerDialog(
    initialDateText: String,
    title: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val monthNames = com.mgacreative.touros.utils.DateUtils.monthNamesTr.map { AppLanguageManager.translate(it) }
    val dayNames = com.mgacreative.touros.utils.DateUtils.dayNamesTr.map { AppLanguageManager.translate(it) }

    val parts = if (initialDateText.contains(".")) {
        initialDateText.split(".")
    } else if (initialDateText.contains("-")) {
        val p = initialDateText.split("-")
        if (p.size == 3 && p[0].length == 4) listOf(p[2], p[1], p[0]) else p
    } else listOf()

    val today = com.mgacreative.touros.utils.DateUtils.getToday()
    val initDay = parts.getOrNull(0)?.toIntOrNull() ?: today.first
    val initMonth = (parts.getOrNull(1)?.toIntOrNull() ?: today.second).coerceIn(1, 12)
    val initYear = parts.getOrNull(2)?.toIntOrNull() ?: today.third

    var selectedDay by remember { mutableStateOf(initDay) }
    var currentMonth by remember { mutableStateOf(initMonth) }
    var currentYear by remember { mutableStateOf(initYear) }

    fun getDaysInMonth(m: Int, y: Int): Int {
        return when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
            else -> 31
        }
    }

    fun getFirstDayOfWeek(m: Int, y: Int): Int {
        val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        val yr = if (m < 3) y - 1 else y
        val dayOfWeekSunday0 = (yr + yr / 4 - yr / 100 + yr / 400 + t[m - 1] + 1) % 7
        return if (dayOfWeekSunday0 == 0) 6 else dayOfWeekSunday0 - 1
    }

    val maxDays = getDaysInMonth(currentMonth, currentYear)
    if (selectedDay > maxDays) {
        selectedDay = maxDays
    }
    val firstDayOffset = getFirstDayOfWeek(currentMonth, currentYear)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title.uppercase(),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                    Text(
                        text = "${selectedDay.toString().padStart(2, '0')} ${monthNames[currentMonth - 1]} $currentYear",
                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Month & Year Selector Header with interactive ◀ and ▶ arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 ${monthNames[currentMonth - 1]} $currentYear",
                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    if (currentMonth > 1) {
                                        currentMonth--
                                    } else {
                                        currentMonth = 12
                                        currentYear--
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◀", fontSize = 12.sp, color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable {
                                    if (currentMonth < 12) {
                                        currentMonth++
                                    } else {
                                        currentMonth = 1
                                        currentYear++
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", fontSize = 12.sp, color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Day Names Header Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    dayNames.forEach { dayName ->
                        Text(
                            text = dayName,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            modifier = Modifier.width(36.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Calendar Days Grid with accurate Weekday Offset
                val totalCells = firstDayOffset + maxDays
                val cellsList = (0 until totalCells).toList()

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cellsList.chunked(7).forEach { weekRow ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            weekRow.forEach { cellIndex ->
                                if (cellIndex < firstDayOffset) {
                                    Spacer(modifier = Modifier.size(36.dp))
                                } else {
                                    val dayNum = cellIndex - firstDayOffset + 1
                                    val isSelected = (dayNum == selectedDay)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF0F5A56) else Color.Transparent)
                                            .clickable { selectedDay = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            style = TourOSTypography.Caption.copy(
                                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                            repeat(7 - weekRow.size) {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppLanguageManager.translate("İptal"), color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val dayStr = selectedDay.toString().padStart(2, '0')
                            val monthStr = currentMonth.toString().padStart(2, '0')
                            val formattedDate = "$dayStr.$monthStr.$currentYear"
                            onDateSelected(formattedDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A56))
                    ) {
                        Text("Tamam", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernGuestRoomSelectorDialog(
    initialAdults: Int = 2,
    initialChildren: Int = 0,
    initialRooms: Int = 1,
    initialChildrenAges: List<Int> = emptyList(),
    onApply: (adults: Int, children: Int, rooms: Int, childrenAges: List<Int>, summaryText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var adults by remember { mutableStateOf(initialAdults.coerceIn(1, 10)) }
    var children by remember { mutableStateOf(initialChildren.coerceIn(0, 6)) }
    var rooms by remember { mutableStateOf(initialRooms.coerceIn(1, 5)) }
    var childrenAges by remember {
        mutableStateOf(
            if (initialChildrenAges.size == initialChildren) initialChildrenAges.toMutableList()
            else MutableList(initialChildren) { 5 }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppLanguageManager.translate("MİSAFİR VE ODA SEÇİMİ"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                    Text(
                        text = AppLanguageManager.translate("Kişi & Konaklama Detayları"),
                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // 1. Oda Sayısı Row
                GuestCounterRow(
                    title = AppLanguageManager.translate("Oda Sayısı"),
                    subtitle = AppLanguageManager.translate("Grup & Aile Konaklaması"),
                    count = rooms,
                    min = 1,
                    max = 5,
                    onCountChange = { rooms = it }
                )

                // 2. Yetişkin Sayısı Row
                GuestCounterRow(
                    title = AppLanguageManager.translate("Yetişkin"),
                    subtitle = AppLanguageManager.translate("12 yaş ve üzeri"),
                    count = adults,
                    min = 1,
                    max = 10,
                    onCountChange = { adults = it }
                )

                // 3. Çocuk Sayısı Row
                GuestCounterRow(
                    title = AppLanguageManager.translate("Çocuk"),
                    subtitle = AppLanguageManager.translate("0 - 11 yaş arası"),
                    count = children,
                    min = 0,
                    max = 6,
                    onCountChange = { newCount ->
                        children = newCount
                        val updated = childrenAges.toMutableList()
                        while (updated.size < newCount) updated.add(5)
                        while (updated.size > newCount) updated.removeAt(updated.size - 1)
                        childrenAges = updated
                    }
                )

                // 4. Dinamik Çocuk Yaşları Bölümü (Sadece çocuk > 0 ise görünür)
                if (children > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👶 ${AppLanguageManager.translate("Çocuk Yaşları (Fiyatlandırma için gereklidir)")}",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF334155), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )

                        val currentLangCode = AppLanguageManager.currentLanguage.value.code
                        val formatAge: (Int) -> String = { a ->
                            when (currentLangCode) {
                                "ru" -> when {
                                    a == 0 -> "0 лет (Младенец)"
                                    a == 1 -> "1 год"
                                    a in 2..4 -> "$a года"
                                    else -> "$a лет"
                                }
                                "en" -> if (a == 0) "0 Years (Infant)" else if (a == 1) "1 Year" else "$a Years"
                                "de" -> if (a == 0) "0 Jahre (Baby)" else if (a == 1) "1 Jahr" else "$a Jahre"
                                else -> if (a == 0) "0 Yaş (Bebek)" else "$a Yaş"
                            }
                        }

                        childrenAges.forEachIndexed { index, age ->
                            var showAgeMenu by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${AppLanguageManager.translate("Çocuk Yaşı")}:",
                                    style = TourOSTypography.BodyMedium.copy(color = Color(0xFF475569), fontSize = 12.sp)
                                )

                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable { showAgeMenu = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formatAge(age),
                                                style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F5A56), fontSize = 12.sp)
                                            )
                                            Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = showAgeMenu,
                                        onDismissRequest = { showAgeMenu = false },
                                        modifier = Modifier.heightIn(max = 200.dp).background(Color.White)
                                    ) {
                                        (0..17).forEach { a ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = formatAge(a),
                                                        style = TourOSTypography.Caption.copy(fontSize = 11.sp, color = if (a == age) Color(0xFF0F5A56) else Color(0xFF1E293B))
                                                    )
                                                },
                                                onClick = {
                                                    val updated = childrenAges.toMutableList()
                                                    if (index < updated.size) {
                                                        updated[index] = a
                                                        childrenAges = updated
                                                    }
                                                    showAgeMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppLanguageManager.translate("İptal"), color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val summary = buildString {
                                append("$adults Yetişkin")
                                if (children > 0) append(" · $children Çocuk")
                                if (rooms > 1) append(" · $rooms Oda")
                            }
                            onApply(adults, children, rooms, childrenAges, summary)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A56))
                    ) {
                        Text(AppLanguageManager.translate("Uygula"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestCounterRow(
    title: String,
    subtitle: String,
    count: Int,
    min: Int,
    max: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp))
            Text(subtitle, style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (count > min) Color(0xFFF1F5F9) else Color(0xFFF8FAFC))
                    .clickable(enabled = count > min) { onCountChange(count - 1) },
                contentAlignment = Alignment.Center
            ) {
                Text("—", fontSize = 12.sp, color = if (count > min) Color(0xFF0F5A56) else Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$count",
                style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp),
                modifier = Modifier.width(20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (count < max) Color(0xFFF1F5F9) else Color(0xFFF8FAFC))
                    .clickable(enabled = count < max) { onCountChange(count + 1) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 14.sp, color = if (count < max) Color(0xFF0F5A56) else Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── 🌍 ПОПУЛЯРНЫЕ СТРАНЫ / POPULAR COUNTRIES DISCOVERY DIALOG MODAL ──────────
@Composable
fun PopularCountriesDiscoveryDialog(
    allWorldCountries: List<Pair<Triple<String, String, String>, String>>,
    dbProducts: List<PublicHotelOffer>,
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    onHotelClick: (PublicHotelOffer) -> Unit,
    onSelectAndBook: (PublicHotelOffer) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRegionCategory by remember { mutableStateOf("ALL") }
    var countrySearchQuery by remember { mutableStateOf("") }
    var selectedCountryTab by remember { mutableStateOf("TR") } // Default to Turkey so tours are loaded immediately
    var selectedSubRegionFilter by remember { mutableStateOf<String?>("Tümü") }

    var dynamicCountryProducts by remember { mutableStateOf<List<PublicHotelOffer>>(emptyList()) }
    var isLoadingCountryProducts by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCountryTab) {
        if (selectedCountryTab != "ALL") {
            isLoadingCountryProducts = true
            runCatching {
                supabaseClient.postgrest["marketplace_products"]
                    .select {
                        limit(10000)
                        filter {
                            eq("country_code", selectedCountryTab)
                            neq("product_type", "FLIGHT")
                        }
                    }
                    .decodeList<com.mgacreative.touros.data.database.entity.UnifiedProductEntity>()
            }.onSuccess { list ->
                dynamicCountryProducts = list
                    .filter { it.productType != "FLIGHT" && TourOperatorConfig.isAllowedOperator(it.operatorName) }
                    .map { it.toPublicHotelOffer() }
            }.onFailure {
                dynamicCountryProducts = emptyList()
            }
            isLoadingCountryProducts = false
        } else {
            dynamicCountryProducts = emptyList()
        }
    }

    val staticSubRegionsMap = remember {
        mapOf(
            "TR" to listOf("Анталья", "Аланья", "Кемер", "Белек", "Сиде", "Бодрум", "Мармарис", "Фетхие", "Стамбул", "Кушадасы", "Каппадокия", "Дидим"),
            "EG" to listOf("Şarm El-Şeyh", "Hurgada", "El Gouna", "Makadi Bay"),
            "TH" to listOf("Phuket", "Pattaya", "Bangkok", "Koh Samui", "Krabi"),
            "VN" to listOf("Da Nang", "Phu Quoc", "Nha Trang", "Hoi An"),
            "AE" to listOf("Dubai Marina", "Palm Jumeirah", "Downtown", "Abu Dhabi"),
            "RU" to listOf("Moskova", "St. Petersburg", "Sochi", "Kazan"),
            "MV" to listOf("Male", "Ari Atoll", "Baa Atoll", "Kaafu Atoll"),
            "CY" to listOf("Girne", "Gazimağusa", "Lefkoşa", "Bafra", "Larnaka"),
            "GE" to listOf("Batum", "Tiflis", "Gudauri", "Bakuriani"),
            "SC" to listOf("Mahe", "Praslin", "La Digue"),
            "LK" to listOf("Colombo", "Bentota", "Kandy", "Galle"),
            "MU" to listOf("Port Louis", "Grand Baie", "Flic-en-Flac", "Belle Mare"),
            "ID" to listOf("Bali", "Ubud", "Kuta", "Seminyak", "Nusa Dua"),
            "TZ" to listOf("Zanzibar", "Nungwi", "Kendwa", "Stone Town"),
            "ME" to listOf("Budva", "Kotor", "Tivat", "Herceg Novi"),
            "GR" to listOf("Rodos", "Girit", "Atina", "Selanik", "Halkidiki"),
            "CN" to listOf("Hainan", "Sanya", "Pekin", "Şanghay"),
            "AB" to listOf("Gagra", "Pitsunda", "Sohum")
        )
    }

    val effectiveProductsPool = remember(dbProducts, dynamicCountryProducts, selectedCountryTab) {
        val cleanDb = dbProducts.filter { it.category != "FLIGHT" && TourOperatorConfig.isAllowedOperator(it.operatorName) }
        val cleanDynamic = dynamicCountryProducts.filter { it.category != "FLIGHT" && TourOperatorConfig.isAllowedOperator(it.operatorName) }
        if (selectedCountryTab != "ALL" && cleanDynamic.isNotEmpty()) {
            (cleanDynamic + cleanDb.filter { matchesSelectedCountry(it, selectedCountryTab) }).distinctBy { it.id }
        } else if (selectedCountryTab != "ALL") {
            cleanDb.filter { matchesSelectedCountry(it, selectedCountryTab) }
        } else {
            cleanDb
        }
    }

    val turkishToRussianCityMap = remember {
        mapOf(
            "antalya" to "Анталья",
            "belek" to "Белек",
            "kemer" to "Кемер",
            "lara" to "Лара",
            "alanya" to "Аланья",
            "side" to "Сиде",
            "bodrum" to "Бодрум",
            "marmaris" to "Мармарис",
            "fethiye" to "Фетхие",
            "çeşme" to "Чешме",
            "cesme" to "Чешме",
            "dalaman" to "Даламан",
            "istanbul" to "Стамбул",
            "kuşadası" to "Кушадасы",
            "kusadasi" to "Кушадасы",
            "kapadokya" to "Каппадокия",
            "cappadocia" to "Каппадокия",
            "didim" to "Дидим",
            "анталия" to "Анталья"
        )
    }

    val countryCitiesMap = remember(effectiveProductsPool, selectedCountryTab) {
        val result = mutableMapOf<String, List<String>>()
        allWorldCountries.forEach { (countryData, _) ->
            val cCode = countryData.first
            val statics = staticSubRegionsMap[cCode] ?: emptyList()
            val dynamicRegions = effectiveProductsPool
                .filter { matchesSelectedCountry(it, cCode) }
                .map { it.location.substringBefore(",").trim() }
                .filter { it.isNotBlank() && it != "null" && it.length > 2 && !it.equals(cCode, ignoreCase = true) }
                .distinct()
            val combined = (statics + dynamicRegions).map { raw ->
                val tr = raw.trim()
                turkishToRussianCityMap[tr.lowercase()] ?: tr
            }.filter { name ->
                if (cCode == "TR") {
                    // Türkçe / Latin mükerrer sekmeleri filtrele, sadece Rusça kalsın
                    val isPureLatin = name.all { it.code < 128 || it in "çÇğĞıİöÖşŞüÜ" }
                    !isPureLatin
                } else {
                    true
                }
            }.distinct()

            result[cCode] = if (combined.isNotEmpty()) listOf("Tümü") + combined else listOf("Tümü")
        }
        result
    }

    val countryFilteredProducts = remember(effectiveProductsPool, selectedCountryTab, selectedSubRegionFilter) {
        val filtered = effectiveProductsPool
            .filter { it.category != "FLIGHT" && TourOperatorConfig.isAllowedOperator(it.operatorName) }
            .filter { p ->
                val matchCountry = matchesSelectedCountry(p, selectedCountryTab)
                val loc = p.location.lowercase().trim()
                val hName = p.hotelName.lowercase().trim()
                val matchesSubRegion = if (selectedSubRegionFilter.isNullOrBlank() || selectedSubRegionFilter == "Tümü") true
                else {
                    val query = selectedSubRegionFilter!!.trim().lowercase()
                    val translatedQuery = AppLanguageManager.translate(query).lowercase().trim()
                    loc.contains(query) || hName.contains(query) || loc.contains(translatedQuery) || hName.contains(translatedQuery) ||
                    when (query) {
                        "antalya", "анталья", "анталия" -> 
                            loc.contains("antalya") || loc.contains("анталья") || loc.contains("анталия") ||
                            loc.contains("alanya") || loc.contains("аланья") ||
                            loc.contains("kemer") || loc.contains("кемер") ||
                            loc.contains("belek") || loc.contains("белек") ||
                            loc.contains("side") || loc.contains("сиде") ||
                            loc.contains("lara") || loc.contains("лара") ||
                            loc.contains("kundu") || loc.contains("кунду") ||
                            hName.contains("antalya") || hName.contains("анталья") ||
                            p.description.contains("antalya", ignoreCase = true) || p.description.contains("анталья", ignoreCase = true)
                        "alanya", "аланья" -> loc.contains("alanya") || loc.contains("аланья") || loc.contains("махмутлар") || loc.contains("конаклы") || loc.contains("алания") || hName.contains("alanya") || hName.contains("аланья")
                        "belek", "белек" -> loc.contains("belek") || loc.contains("белек") || loc.contains("богазкент") || loc.contains("кадрие") || hName.contains("belek") || hName.contains("белек")
                        "kemer", "кемер" -> loc.contains("kemer") || loc.contains("кемер") || loc.contains("бельдиби") || loc.contains("кириш") || loc.contains("текирова") || loc.contains("гейнюк") || loc.contains("чамьюва") || hName.contains("kemer") || hName.contains("кемер")
                        "side", "сиде" -> loc.contains("side") || loc.contains("сиде") || loc.contains("кызылот") || loc.contains("манавгат") || loc.contains("чолаклы") || hName.contains("side") || hName.contains("сиде")
                        "bodrum", "бодрум" -> loc.contains("bodrum") || loc.contains("бодрум") || loc.contains("гюмбет") || loc.contains("битез") || hName.contains("bodrum") || hName.contains("бодрум")
                        "marmaris", "мармарис" -> loc.contains("marmaris") || loc.contains("мармарис") || loc.contains("ичмелер") || loc.contains("турунч") || hName.contains("marmaris") || hName.contains("мармарис")
                        "fethiye", "фетхие" -> loc.contains("fethiye") || loc.contains("фетхие") || loc.contains("олюдениз") || hName.contains("fethiye") || hName.contains("фетхие")
                        "st. petersburg", "санкт-петербург" -> loc.contains("petersburg") || loc.contains("петербург")
                        "moskova", "москва" -> loc.contains("mosk") || loc.contains("моск")
                        "istanbul", "стамбул" -> loc.contains("istanbul") || loc.contains("стамбул") || loc.contains("султанахмет") || loc.contains("фатих") || loc.contains("таксим") || loc.contains("лалели") || loc.contains("аксарай") || loc.contains("бейазит") || loc.contains("шишли") || loc.contains("бакыркёй") || hName.contains("istanbul") || hName.contains("стамбул")
                        "kuşadası", "kusadasi", "кушадасы" -> loc.contains("kuşadası") || loc.contains("kusadasi") || loc.contains("кушадасы")
                        "kapadokya", "cappadocia", "каппадокия" -> loc.contains("kapadokya") || loc.contains("cappadocia") || loc.contains("каппадокия") || loc.contains("гёреме") || loc.contains("goreme")
                        "didim", "дидим" -> loc.contains("didim") || loc.contains("дидим")
                        "çeşme", "cesme", "чешме" -> loc.contains("çeşme") || loc.contains("cesme") || loc.contains("чешме")
                        "dalaman", "даламан" -> loc.contains("dalaman") || loc.contains("даламан")
                        "hainan", "хайнань" -> loc.contains("hainan") || loc.contains("хайнань") || loc.contains("санья") || loc.contains("sanya")
                        "gagra", "гагра" -> loc.contains("gagra") || loc.contains("гагра") || loc.contains("гагр")
                        "pitsunda", "пицунда" -> loc.contains("pitsunda") || loc.contains("пицунда")
                        "da nang", "дананг" -> loc.contains("da nang") || loc.contains("дананг")
                        "nha trang", "нячанг" -> loc.contains("nha trang") || loc.contains("нячанг")
                        "phu quoc", "фукуок" -> loc.contains("phu quoc") || loc.contains("фукуок")
                        "dubai", "дубай" -> loc.contains("dubai") || loc.contains("дубай")
                        "girne", "гирне" -> loc.contains("girne") || loc.contains("гирне") || loc.contains("kyrenia")
                        "batum", "батуми" -> loc.contains("batum") || loc.contains("батуми") || loc.contains("batumi")
                        "bali", "бали" -> loc.contains("bali") || loc.contains("бали")
                        "zanzibar", "занзибар" -> loc.contains("zanzibar") || loc.contains("занзибар")
                        "budva", "будва" -> loc.contains("budva") || loc.contains("будва")
                        "rodos", "родос" -> loc.contains("rodos") || loc.contains("родос") || loc.contains("rhodes")
                        "girit", "крит" -> loc.contains("girit") || loc.contains("крит") || loc.contains("crete")
                        else -> false
                    }
                }

                matchCountry && matchesSubRegion
            }
        groupOffersByHotelName(filtered)
    }

    val regionCategories = listOf(
        "ALL" to "${AppLanguageManager.translate("Все")} (${allWorldCountries.size})",
        "POPULAR" to "${AppLanguageManager.translate("Популярные")} (8)",
        "TROPICAL" to "${AppLanguageManager.translate("Тропики")} (6)",
        "EUROPE" to "${AppLanguageManager.translate("Европа")} (2)",
        "ASIA" to "${AppLanguageManager.translate("Азия")} (2)"
    )

    val currentCountryData = allWorldCountries.firstOrNull { it.first.first == selectedCountryTab }
    val cName = currentCountryData?.first?.second ?: selectedCountryTab
    val translatedCName = AppLanguageManager.translate(cName)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .widthIn(min = 360.dp, max = 1200.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── 1. DIALOG HEADER BAR ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F5A56))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.20f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = AppLanguageManager.translate("Популярные страны и направления"),
                                style = TourOSTypography.TitleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = AppLanguageManager.translate("Выберите страну и просмотрите актуальные пакетные туры и отели"),
                                style = TourOSTypography.Caption.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // ── 2. SEÇİM PANELİ (Kategori Sekmeleri, Arama ve Ülke Çipleri) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Kategori Sekmeleri + Arama Çubuğu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(regionCategories) { (catKey, catTitle) ->
                                val isSelected = (selectedRegionCategory == catKey)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { selectedRegionCategory = catKey },
                                    color = if (isSelected) Color(0xFF0F5A56) else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F5A56) else Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = catTitle,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = TourOSTypography.Caption.copy(
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = countrySearchQuery,
                            onValueChange = { countrySearchQuery = it },
                            placeholder = { Text(AppLanguageManager.translate("Поиск страны..."), fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = {
                                if (countrySearchQuery.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp).clickable { countrySearchQuery = "" }
                                    )
                                }
                            },
                            modifier = Modifier.width(220.dp).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF0F5A56),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    // Ülke Çipleri Listesi (18 Canlı Ülke)
                    val displayedCountries = allWorldCountries.filter { (countryData, category) ->
                        val (code, name, _) = countryData
                        val matchCat = (selectedRegionCategory == "ALL" || category == selectedRegionCategory)
                        val matchSearch = countrySearchQuery.isBlank() ||
                                name.contains(countrySearchQuery, ignoreCase = true) ||
                                AppLanguageManager.translate(name).contains(countrySearchQuery, ignoreCase = true) ||
                                code.contains(countrySearchQuery, ignoreCase = true)
                        matchCat && matchSearch
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(displayedCountries) { (countryData, _) ->
                            val (cCode, cRawName, flag) = countryData
                            val isSelected = (selectedCountryTab == cCode)
                            val name = AppLanguageManager.translate(cRawName)

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedCountryTab = cCode
                                        selectedSubRegionFilter = "Tümü"
                                    },
                                color = if (isSelected) Color(0xFF0F5A56) else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F5A56) else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(flag, fontSize = 14.sp)
                                    Text(
                                        text = name,
                                        style = TourOSTypography.BodyMedium.copy(
                                            color = if (isSelected) Color.White else Color(0xFF1E293B),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Alt Bölge / Şehir Çipleri (Seçilen Ülkeye Göre)
                    val activeCities = countryCitiesMap[selectedCountryTab] ?: listOf("Tümü")
                    if (activeCities.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(activeCities) { city ->
                                val isSelected = (selectedSubRegionFilter == city || (city == "Tümü" && (selectedSubRegionFilter == null || selectedSubRegionFilter == "Tümü")))
                                val label = if (city == "Tümü") AppLanguageManager.translate("Все регионы") else AppLanguageManager.translate(city)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedSubRegionFilter = city },
                                    color = if (isSelected) Color(0xFFE0F2FE) else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = TourOSTypography.Caption.copy(
                                            color = if (isSelected) Color(0xFF0369A1) else Color(0xFF475569),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // ── 3. İÇERİK & ARAMA SONUÇLARI ALANI ──
                val dialogScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(dialogScrollState)
                            .padding(16.dp)
                    ) {
                        if (isLoadingCountryProducts) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF0F5A56))
                            }
                        } else {
                            val subRegionLabel = if (!selectedSubRegionFilter.isNullOrBlank() && selectedSubRegionFilter != "Tümü") {
                                " · ${AppLanguageManager.translate(selectedSubRegionFilter!!)}"
                            } else ""

                            VerticalSearchResultsGridSection(
                                titleVectorIcon = Icons.Default.LocationOn,
                                title = "$translatedCName ${AppLanguageManager.translate("Paket Turları & Otelleri")} (${countryFilteredProducts.size} ${AppLanguageManager.translate("Otel / Tesis")})",
                                subtitle = "${AppLanguageManager.translate("Destinasyon:")} $translatedCName$subRegionLabel · ${AppLanguageManager.translate("Uçuş + Transfer + Otel Dahil")}",
                                hotels = countryFilteredProducts,
                                onHotelClick = onHotelClick,
                                onSelectAndBook = onSelectAndBook
                            )
                        }
                    }

                    TourOSVerticalScrollbar(
                        scrollState = dialogScrollState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}





package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import touros.shared.generated.resources.Res
import touros.shared.generated.resources.axileto_logo_white
import touros.shared.generated.resources.club_badge
import com.mgacreative.touros.domain.model.PromoBannerItem
import com.mgacreative.touros.domain.model.BookingItem
import com.mgacreative.touros.domain.model.Passenger
import com.mgacreative.touros.data.util.isValidUuid
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
    val roomType: String = "standard room, city view",
    val mealType: String = "Bez pitaniya",
    val flightCode: String = "VKO - AYT (Ekonomi 🟢)",
    val nights: Int = 7,
    val currency: String = "RUB",
    val isInstantConfirmation: Boolean = true,
    val category: String = "PACKAGE_TOUR", // "PACKAGE_TOUR", "HOTEL", "FLIGHT", "LAST_MINUTE"
    val discountPercent: Int? = null,
    val ratingScore: Double? = null,
    val isLastMinute: Boolean = false,
    val agencyPrices: List<AgencyPriceOption> = emptyList(),
    val countryCode: String = "TR",
    val departureDate: String? = "2026-08-20",
    val returnDate: String? = "2026-08-28"
)

fun matchesSelectedCountry(offer: PublicHotelOffer, selectedCode: String): Boolean {
    if (selectedCode.isBlank() || selectedCode == "ALL") return true
    if (offer.countryCode.equals(selectedCode, ignoreCase = true)) return true
    val loc = offer.location.lowercase()
    val hName = offer.hotelName.lowercase()
    return when (selectedCode) {
        "TR" -> offer.countryCode == "TR" || loc.contains("türkiye") || loc.contains("turkey") || loc.contains("antalya") || loc.contains("bodrum") || loc.contains("belek") || loc.contains("kemer") || loc.contains("lara") || loc.contains("alanya") || loc.contains("side") || loc.contains("marmaris") || loc.contains("fethiye") || loc.contains("çeşme")
        "EG" -> offer.countryCode == "EG" || loc.contains("mısır") || loc.contains("egypt") || loc.contains("şarm") || loc.contains("sharm") || loc.contains("hurgada") || loc.contains("hurghada") || loc.contains("gouna") || loc.contains("makadi") || hName.contains("sharm") || hName.contains("hurghada") || hName.contains("citadel") || hName.contains("jaz") || hName.contains("albatros")
        "TH" -> offer.countryCode == "TH" || loc.contains("tayland") || loc.contains("thailand") || loc.contains("phuket") || loc.contains("pattaya") || loc.contains("bangkok") || loc.contains("samui") || loc.contains("krabi") || hName.contains("phuket") || hName.contains("pattaya") || hName.contains("bangkok") || hName.contains("centara") || hName.contains("banyan")
        "VN" -> offer.countryCode == "VN" || loc.contains("vietnam") || loc.contains("da nang") || loc.contains("phu quoc") || loc.contains("nha trang") || loc.contains("hoi an") || hName.contains("phu quoc") || hName.contains("danang") || hName.contains("vinpearl")
        "AE" -> offer.countryCode == "AE" || loc.contains("dubai") || loc.contains("bae") || loc.contains("uae") || loc.contains("abu dhabi") || loc.contains("jumeirah") || hName.contains("dubai") || hName.contains("atlantis") || hName.contains("burj")
        "RU" -> offer.countryCode == "RU" || loc.contains("rusya") || loc.contains("russia") || loc.contains("moskova") || loc.contains("sochi") || loc.contains("st. petersburg") || loc.contains("petersburg") || hName.contains("moscow") || hName.contains("radisson") || hName.contains("carlton")
        else -> true
    }
}

fun PublicHotelOffer.toUnifiedProductEntity(): com.mgacreative.touros.data.database.entity.UnifiedProductEntity {
    return com.mgacreative.touros.data.database.entity.UnifiedProductEntity(
        id = this.id,
        hotelName = this.hotelName,
        tourName = if (this.category == "FLIGHT") "Uçuş: ${this.hotelName} (${this.flightCode})" else "${this.hotelName} Tur Paketi",
        region = this.location.removeSuffix(", Türkiye").removeSuffix(", Turkey").trim(),
        country = this.countryCode.ifBlank { "Türkiye" },
        countryCode = this.countryCode,
        countryName = when (this.countryCode.uppercase()) {
            "TR" -> "Türkiye"
            "EG" -> "Mısır"
            "TH" -> "Tayland"
            "VN" -> "Vietnam"
            "AE" -> "BAE"
            "RU" -> "Rusya"
            else -> "Türkiye"
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
    val opName = this.safeOperatorName.ifBlank { "Coral Travel" }
    val rType = this.safeRoomType.ifBlank { "standard room, city view" }
    val mType = this.safeMealType.ifBlank { "Bez pitaniya" }
    val fCode = if (this.flightNumber.isNotBlank()) "${this.airlineName} (${this.flightNumber})" else "VKO - AYT (Ekonomi 🟢)"

    val rawType = this.safeProductType.uppercase()
    val isFlight = rawType == "FLIGHT" || rawType == "CHARTER" || rawType == "FLIGHT_ONLY" || this.airlineName.isNotBlank() || this.flightNumber.startsWith("TK-") || this.flightNumber.startsWith("N4-") || this.flightNumber.startsWith("SU-") || this.flightNumber.startsWith("PC-") || this.safeTourName.startsWith("Uçuş:", ignoreCase = true) || this.safeHotelName.startsWith("Uçuş:", ignoreCase = true) || this.safeHotelName.startsWith("✈️", ignoreCase = true)
    val isHotelOnly = !isFlight && (rawType == "HOTEL" || rawType == "LOCAL_HOTEL" || this.safeOperatorName.contains("Yerel Otel", ignoreCase = true))
    val isPromo = this.safeIsPromo || (this.customPriceOverride != null && this.customPriceOverride < baseP)

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
        else -> "TR"
    }

    val cCountryName = when (cCode) {
        "EG" -> "Mısır"
        "TH" -> "Tayland"
        "VN" -> "Vietnam"
        "AE" -> "BAE (Dubai)"
        "RU" -> "Rusya"
        else -> "Türkiye"
    }

    return PublicHotelOffer(
        id = this.id,
        hotelName = this.safeHotelName.ifBlank { this.safeTourName.ifBlank { if (isFlight) "✈️ Charter Uçuş Seferi (${fCode})" else "Tur Operatörü Ürünü" } },
        location = "${this.safeRegion.ifBlank { this.safeDepartureCity.ifBlank { "Antalya" } }}, $cCountryName",
        stars = if (this.safeHotelCategory > 0) this.safeHotelCategory else 5,
        description = (this.safeHotelName.ifBlank { this.safeTourName }) + " - Operatör: " + opName,
        minPrice = baseP,
        maxPrice = this.customPriceOverride ?: (baseP * 1.15),
        imageUrl = this.safePictureUrl,
        operatorName = opName,
        roomType = rType,
        mealType = mType,
        flightCode = fCode,
        nights = if (this.nights > 0) this.nights else 7,
        currency = this.safeCurrency.ifBlank { "USD" },
        category = mappedCat,
        discountPercent = if (isPromo) 35 else null,
        isLastMinute = isPromo,
        countryCode = cCode,
        agencyPrices = listOf(
            AgencyPriceOption("AGN-ANEX", "Coral Travel B2B (MGA Partner)", opName, rType, mType, baseP, isBestDeal = true),
            AgencyPriceOption("AGN-CORAL", "Coral Travel B2B", "Coral Travel", "Executive Suite", "Ultra Her Şey Dahil", baseP * 1.12),
            AgencyPriceOption("AGN-PEGAS", "Pegas Touristik Agency", "Pegas Touristik", "Deluxe Double Room", "Oda Kahvaltı", baseP * 1.25)
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
    val isBestDeal: Boolean = false
)

fun getInitialDefaultOffers(): List<PublicHotelOffer> {
    return listOf(
        // ── 🇹🇷 TÜRKİYE FIRSATLARI ──
        PublicHotelOffer(
            id = "MOD-CORAL-101",
            hotelName = "Nirvana Cosmopolitan Hotel",
            location = "Lara, Antalya, Türkiye",
            stars = 5,
            description = "Starway Award ödüllü lüks tesis. Coral Travel özel fiyat garantili toplu paket.",
            minPrice = 580.0,
            maxPrice = 690.0,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            operatorName = "Coral Travel",
            roomType = "Superior Sea View Room",
            mealType = "Ultra Her Şey Dahil",
            flightCode = "AYT - IST (THY 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 44,
            ratingScore = 9.4,
            isLastMinute = true,
            countryCode = "TR"
        ),
        PublicHotelOffer(
            id = "MOD-ANEX-102",
            hotelName = "Rixos Premium Belek",
            location = "Belek, Antalya, Türkiye",
            stars = 5,
            description = "Anex Tour özel rezervasyonlu 5 yıldızlı lüks plaj tesisi.",
            minPrice = 890.0,
            maxPrice = 980.0,
            imageUrl = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
            operatorName = "Anex Tour",
            roomType = "Deluxe Suite Garden View",
            mealType = "All Inclusive Special",
            flightCode = "VKO - AYT (Azur Air 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 35,
            ratingScore = 9.6,
            isLastMinute = false,
            countryCode = "TR"
        ),
        PublicHotelOffer(
            id = "MOD-FUNSUN-104",
            hotelName = "Lujo Hotel Bodrum",
            location = "Bodrum, Muğla, Türkiye",
            stars = 5,
            description = "Fun & Sun Premium konseptli özel koy ve ultra lüks tatil paketi.",
            minPrice = 1150.0,
            maxPrice = 1350.0,
            imageUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800",
            operatorName = "Fun & Sun",
            roomType = "Indigo Sea View Room",
            mealType = "Luxury A La Carte All Inclusive",
            flightCode = "BJV - IST (AJet 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 25,
            ratingScore = 9.8,
            isLastMinute = true,
            countryCode = "TR"
        ),
        PublicHotelOffer(
            id = "MOD-CORAL-105",
            hotelName = "Maxx Royal Kemer Resort",
            location = "Kemer, Antalya, Türkiye",
            stars = 5,
            description = "Maxx Inclusive konseptli özel koy, VIP hizmet ve Coral Travel ayrıcalığı.",
            minPrice = 1420.0,
            maxPrice = 1650.0,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            operatorName = "Coral Travel",
            roomType = "Suite Land View",
            mealType = "Maxx Inclusive",
            flightCode = "AYT - IST (THY VIP 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 20,
            ratingScore = 9.9,
            isLastMinute = false,
            countryCode = "TR"
        ),

        // ── 🇪🇬 MISIR FIRSATLARI ──
        PublicHotelOffer(
            id = "MOD-EG-201",
            hotelName = "Rixos Premium Seagate Sharm",
            location = "Şarm El-Şeyh, Mısır",
            stars = 5,
            description = "Kızıldeniz'in en gözde resifi, ultra her şey dahil lüks konaklama.",
            minPrice = 490.0,
            maxPrice = 590.0,
            imageUrl = "https://images.unsplash.com/photo-1539768942893-daf53e448371?w=800",
            operatorName = "Coral Travel",
            roomType = "Superior Room Pool View",
            mealType = "Ultra All Inclusive",
            flightCode = "IST - SSH (THY 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 30,
            ratingScore = 9.5,
            isLastMinute = true,
            countryCode = "EG"
        ),
        PublicHotelOffer(
            id = "MOD-EG-202",
            hotelName = "Pickalbatros Citadel Resort",
            location = "Hurgada, Mısır",
            stars = 5,
            description = "Sahl Hasheesh koyunda özel lagünler ve mercan kayalıkları manzaralı tesis.",
            minPrice = 520.0,
            maxPrice = 610.0,
            imageUrl = "https://images.unsplash.com/photo-1568084680786-a84f91d1153c?w=800",
            operatorName = "Anex Tour",
            roomType = "Deluxe Sea View",
            mealType = "All Inclusive Plus",
            flightCode = "AYT - HRG (Pegasus 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 25,
            ratingScore = 9.3,
            isLastMinute = false,
            countryCode = "EG"
        ),
        PublicHotelOffer(
            id = "MOD-EG-203",
            hotelName = "Steigenberger ALDAU Beach Hotel",
            location = "El Gouna, Mısır",
            stars = 5,
            description = "Lüks golf sahaları ve özel kum plajlı eşsiz tatil deneyimi.",
            minPrice = 560.0,
            maxPrice = 670.0,
            imageUrl = "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800",
            operatorName = "Pegas Touristik",
            roomType = "Deluxe Lagoon View",
            mealType = "Ultra All Inclusive",
            flightCode = "SAW - HRG (AJet 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 20,
            ratingScore = 9.6,
            isLastMinute = false,
            countryCode = "EG"
        ),

        // ── 🇹🇭 TAYLAND FIRSATLARI ──
        PublicHotelOffer(
            id = "MOD-TH-301",
            hotelName = "Centara Grand Beach Resort",
            location = "Phuket, Tayland",
            stars = 5,
            description = "Karon Beach sahilinde Andaman Denizi manzaralı tropik cennet.",
            minPrice = 790.0,
            maxPrice = 920.0,
            imageUrl = "https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=800",
            operatorName = "Coral Travel",
            roomType = "Deluxe Ocean Facing",
            mealType = "Oda & Kahvaltı",
            flightCode = "IST - HKT (THY 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 25,
            ratingScore = 9.4,
            isLastMinute = true,
            countryCode = "TH"
        ),
        PublicHotelOffer(
            id = "MOD-TH-302",
            hotelName = "Royal Cliff Beach Hotel",
            location = "Pattaya, Tayland",
            stars = 5,
            description = "Pattaya Körfezi tepesinde lüks spa ve sonsuzluk havuzlu resort.",
            minPrice = 820.0,
            maxPrice = 950.0,
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
            operatorName = "Anex Tour",
            roomType = "Mini Suite Sea View",
            mealType = "Oda Kahvaltı Dahil",
            flightCode = "IST - BKK (Qatar 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 20,
            ratingScore = 9.2,
            isLastMinute = false,
            countryCode = "TH"
        ),
        PublicHotelOffer(
            id = "MOD-TH-303",
            hotelName = "Banyan Tree Bangkok",
            location = "Bangkok, Tayland",
            stars = 5,
            description = "Vertigo çatı restoranı ve Chao Phraya nehri manzaralı lüks şehir oteli.",
            minPrice = 890.0,
            maxPrice = 1040.0,
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            operatorName = "Pegas Touristik",
            roomType = "Horizon Club Room",
            mealType = "Oda Kahvaltı",
            flightCode = "AYT - BKK (Emirates 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 15,
            ratingScore = 9.7,
            isLastMinute = false,
            countryCode = "TH"
        ),

        // ── 🇻🇳 VİETNAM FIRSATLARI ──
        PublicHotelOffer(
            id = "MOD-VN-401",
            hotelName = "Vinpearl Resort & Spa Phu Quoc",
            location = "Phu Quoc, Vietnam",
            stars = 5,
            description = "Bai Dai sahilinde saf kum plajlar ve Safari tema parkı avantajlı paket.",
            minPrice = 850.0,
            maxPrice = 990.0,
            imageUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800",
            operatorName = "Coral Travel",
            roomType = "Deluxe Garden View",
            mealType = "Tam Pansiyon Plus",
            flightCode = "IST - PQC (Vietnam Airlines 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 22,
            ratingScore = 9.3,
            isLastMinute = true,
            countryCode = "VN"
        ),
        PublicHotelOffer(
            id = "MOD-VN-402",
            hotelName = "InterContinental Danang Sun Peninsula",
            location = "Da Nang, Vietnam",
            stars = 5,
            description = "Son Tra yarımadasında yağmur ormanı ve özel plajlı ikonik tasarım oteli.",
            minPrice = 980.0,
            maxPrice = 1180.0,
            imageUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800",
            operatorName = "Anex Tour",
            roomType = "Classic Ocean View",
            mealType = "Oda & Gurme Kahvaltı",
            flightCode = "IST - DAD (Singapore Airlines 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 18,
            ratingScore = 9.8,
            isLastMinute = false,
            countryCode = "VN"
        ),

        // ── 🇦🇪 BAE (DUBAİ) FIRSATLARI ──
        PublicHotelOffer(
            id = "MOD-AE-501",
            hotelName = "Rixos Premium Dubai JBR",
            location = "Dubai Marina, Dubai, BAE",
            stars = 5,
            description = "Jumeirah Beach Residence kalbinde Ain Dubai manzaralı lüks yaşam tesisi.",
            minPrice = 690.0,
            maxPrice = 820.0,
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800",
            operatorName = "Coral Travel",
            roomType = "Deluxe Sea View Room",
            mealType = "Oda Kahvaltı & Akşam Yemeği",
            flightCode = "SAW - DXB (FlyDubai 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 30,
            ratingScore = 9.6,
            isLastMinute = true,
            countryCode = "AE"
        ),
        PublicHotelOffer(
            id = "MOD-AE-502",
            hotelName = "Atlantis, The Palm",
            location = "Palm Jumeirah, Dubai, BAE",
            stars = 5,
            description = "Palmiye Adası ucunda Aquaventure su parkı girişli dünyaca ünlü efsane otel.",
            minPrice = 1090.0,
            maxPrice = 1350.0,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            operatorName = "Anex Tour",
            roomType = "Ocean King Room",
            mealType = "Yarım Pansiyon Imperial",
            flightCode = "IST - DXB (Emirates 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 20,
            ratingScore = 9.7,
            isLastMinute = false,
            countryCode = "AE"
        ),

        // ── 🇷🇺 RUSYA FIRSATLARI ──
        PublicHotelOffer(
            id = "MOD-RU-601",
            hotelName = "Radisson Collection Paradise Resort",
            location = "Sochi, Rusya",
            stars = 5,
            description = "Karadeniz kıyısında Olimpiyat parkı yanında spa ve plaj konsepti.",
            minPrice = 420.0,
            maxPrice = 510.0,
            imageUrl = "https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800",
            operatorName = "Pegas Touristik",
            roomType = "Collection Superior Room",
            mealType = "Tam Pansiyon",
            flightCode = "IST - AER (Aeroflot 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 25,
            ratingScore = 9.4,
            isLastMinute = true,
            countryCode = "RU"
        ),
        PublicHotelOffer(
            id = "MOD-RU-602",
            hotelName = "The Carlton Moscow",
            location = "Moskova, Rusya",
            stars = 5,
            description = "Kızıl Meydan ve Kremlin manzaralı tarihi ve ultra lüks 5 yıldızlı otel.",
            minPrice = 510.0,
            maxPrice = 640.0,
            imageUrl = "https://images.unsplash.com/photo-1568084680786-a84f91d1153c?w=800",
            operatorName = "Coral Travel",
            roomType = "Executive Suite Red Square View",
            mealType = "Oda Kahvaltı",
            flightCode = "IST - SVO (Aeroflot 🟢)",
            nights = 7,
            currency = "USD",
            category = "PACKAGE_TOUR",
            discountPercent = 15,
            ratingScore = 9.8,
            isLastMinute = false,
            countryCode = "RU"
        ),

        // ── 🏨 SADECE OTEL FIRSATLARI (HOTEL ONLY) ──
        PublicHotelOffer(
            id = "MOD-HOTEL-TR-01",
            hotelName = "Akra Hotel Antalya",
            location = "Muratpaşa, Antalya, Türkiye",
            stars = 5,
            description = "Akdeniz manzaralı sadece otel konaklaması (Uçak ve transfer hariçtir).",
            minPrice = 140.0,
            maxPrice = 190.0,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            operatorName = "Yerel Oteller",
            roomType = "Deluxe City & Sea View",
            mealType = "Oda & Kahvaltı (BB)",
            flightCode = "",
            nights = 1,
            currency = "EUR",
            category = "HOTEL",
            discountPercent = 10,
            ratingScore = 9.3,
            isLastMinute = false,
            countryCode = "TR"
        ),
        PublicHotelOffer(
            id = "MOD-HOTEL-EG-01",
            hotelName = "Four Seasons Resort Sharm El Sheikh",
            location = "Şarm El-Şeyh, Mısır",
            stars = 5,
            description = "Kızıldeniz kıyısında sadece lüks otel konaklaması.",
            minPrice = 280.0,
            maxPrice = 360.0,
            imageUrl = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
            operatorName = "Yerel Oteller",
            roomType = "Premier Sea View Suite",
            mealType = "Oda Kahvaltı (BB)",
            flightCode = "",
            nights = 1,
            currency = "USD",
            category = "HOTEL",
            discountPercent = 15,
            ratingScore = 9.7,
            isLastMinute = false,
            countryCode = "EG"
        ),
        PublicHotelOffer(
            id = "MOD-HOTEL-AE-01",
            hotelName = "Burj Al Arab Jumeirah",
            location = "Dubai Marina, Dubai, BAE",
            stars = 5,
            description = "Dünyaca ünlü 7 yıldızlı ikonik sadece otel konaklaması.",
            minPrice = 950.0,
            maxPrice = 1250.0,
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800",
            operatorName = "Yerel Oteller",
            roomType = "Deluxe One-Bedroom Suite",
            mealType = "Oda & Kahvaltı (BB)",
            flightCode = "",
            nights = 1,
            currency = "USD",
            category = "HOTEL",
            discountPercent = 10,
            ratingScore = 9.9,
            isLastMinute = false,
            countryCode = "AE"
        ),

        // ── ✈️ SADECE UÇUŞ FIRSATLARI (FLIGHT ONLY) ──
        PublicHotelOffer(
            id = "MOD-FLIGHT-01",
            hotelName = "Uçuş: Moskova (SVO) - Antalya (AYT)",
            location = "Antalya, Türkiye",
            stars = 0,
            description = "Türk Hava Yolları direkt tarifeli charter uçuş bileti.",
            minPrice = 220.0,
            maxPrice = 260.0,
            imageUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800",
            operatorName = "Turkish Airlines",
            roomType = "Ekonomi Sınıfı (20kg Bagaj Dahil)",
            mealType = "Uçak İçi İkram",
            flightCode = "SVO - AYT (TK-3701 🟢)",
            nights = 0,
            currency = "EUR",
            category = "FLIGHT",
            discountPercent = null,
            ratingScore = 9.1,
            isLastMinute = false,
            countryCode = "TR"
        ),
        PublicHotelOffer(
            id = "MOD-FLIGHT-02",
            hotelName = "Uçuş: Moskova (DME) - Şarm El-Şeyh (SSH)",
            location = "Şarm El-Şeyh, Mısır",
            stars = 0,
            description = "Nordwind Airlines direkt charter uçuş bileti.",
            minPrice = 290.0,
            maxPrice = 330.0,
            imageUrl = "https://images.unsplash.com/photo-1519074069444-1ba4eff56b61?w=800",
            operatorName = "Nordwind Airlines",
            roomType = "Ekonomi Sınıfı (20kg Bagaj)",
            mealType = "Standart İkram",
            flightCode = "DME - SSH (N4-5821 🟢)",
            nights = 0,
            currency = "EUR",
            category = "FLIGHT",
            discountPercent = null,
            ratingScore = 8.8,
            isLastMinute = false,
            countryCode = "EG"
        ),
        PublicHotelOffer(
            id = "MOD-FLIGHT-03",
            hotelName = "Uçuş: İstanbul (IST) - Dubai (DXB)",
            location = "Dubai, BAE",
            stars = 0,
            description = "Emirates Airlines direkt tarifeli uçuş bileti.",
            minPrice = 340.0,
            maxPrice = 410.0,
            imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            operatorName = "Emirates Airlines",
            roomType = "Ekonomi Sınıfı (30kg Bagaj)",
            mealType = "Sıcak Yemek & İçecek",
            flightCode = "IST - DXB (EK-122 🟢)",
            nights = 0,
            currency = "USD",
            category = "FLIGHT",
            discountPercent = null,
            ratingScore = 9.6,
            isLastMinute = false,
            countryCode = "AE"
        )
    )
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

    val currentUser by authRepository.observeAuthState().collectAsState()
    val currentLang by AppLanguageManager.currentLanguage.collectAsState()
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var companySettings by remember { mutableStateOf<CompanySettings?>(null) }

    LaunchedEffect(currentUser?.tenantId) {
        val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
        companySettings = companySettingsRepository.getCompanySettings(tid).getOrNull()
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
    var selectedStarFilter by remember { mutableStateOf(0) } // 0 = Hepsi
    var maxPriceFilter by remember { mutableStateOf(200000f) }
    var selectedOperatorFilter by remember { mutableStateOf("Tüm Operatörler") }

    // Seçili Otel Detay Modalı
    var selectedHotelForDetail by remember { mutableStateOf<PublicHotelOffer?>(null) }
    var selectedAgencyForBooking by remember { mutableStateOf<AgencyPriceOption?>(null) }
    var isInlineSearchActive by remember { mutableStateOf(false) }
    var searchResultsList by remember { mutableStateOf<List<PublicHotelOffer>>(emptyList()) }

    val b2bUiState by b2bTourSearchViewModel.uiState.collectAsState()
    val b2bSearchResults = remember(b2bUiState) {
        when (val state = b2bUiState) {
            is com.mgacreative.touros.ui.viewmodel.B2BTourSearchUiState.Success -> state.filteredProducts.map { it.toPublicHotelOffer() }
            else -> emptyList()
        }
    }

    val handleDirectBooking: (PublicHotelOffer) -> Unit = { offer ->
        val productEntity = offer.toUnifiedProductEntity()
        b2bTourSearchViewModel.selectProductForBooking(productEntity)
        selectedHotelForDetail = offer
        selectedAgencyForBooking = offer.agencyPrices.firstOrNull() ?: AgencyPriceOption(
            agencyId = "00000000-0000-0000-0000-000000000001",
            agencyName = "${offer.operatorName.ifBlank { "TourOS Partner" }} Acente",
            operatorName = offer.operatorName.ifBlank { "MGA Creative" },
            roomType = offer.roomType,
            boardType = offer.mealType,
            price = offer.minPrice,
            isBestDeal = true
        )
        onNavigateToNewBooking(offer)
    }

    // Hero Arama Barı Form State'leri
    var departureCity by remember { mutableStateOf("Москва (Все аэропорты)") }
    var destinationCity by remember { mutableStateOf("Türkiye (Antalya)") }
    var startDateText by remember { mutableStateOf("20.08.2026") }
    var endDateText by remember { mutableStateOf("28.08.2026") }
    var returnStartDateText by remember { mutableStateOf("28.08.2026") }
    var returnEndDateText by remember { mutableStateOf("05.09.2026") }
    var selectedNightsText by remember { mutableStateOf("7 - 10 Gece") }
    var selectedTouristsText by remember { mutableStateOf("2 Yetişkin · 1 Oda") }
    var adultsCount by remember { mutableStateOf(2) }
    var childrenCount by remember { mutableStateOf(0) }
    var roomsCount by remember { mutableStateOf(1) }
    var childrenAges by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedCountryFilter by remember { mutableStateOf("Türkiye") }
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

    val supabaseClient: io.github.jan.supabase.SupabaseClient = koinInject()

    // Dynamic Database Products State (Varsayılan içerikle anında başlatılır)
    var dbProducts by remember { mutableStateOf<List<PublicHotelOffer>>(getInitialDefaultOffers()) }
    var isLoadingProducts by remember { mutableStateOf(true) }

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
            customTitle = if (isFlightTab) "✈️ UÇUŞ VARIŞ DESTİNASYONU / ПУНКТ НАЗНАЧЕНИЯ" else null,
            onDestinationSelected = { destItem ->
                destinationCity = if (destItem.nameRu.isNotBlank()) "${destItem.name} (${destItem.nameRu})" else destItem.name
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
    var agencyLoginError by remember { mutableStateOf<String?>(null) }

    // SADECE "Toplu Veri Yükle" ile yüklenen Tur Operatörü ürünlerini (marketplace_products) çek
    LaunchedEffect(Unit) {
        isLoadingProducts = true
        runCatching {
            companySettings = companySettingsRepository.getCompanySettings(currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001").getOrNull()
        }
        val offers = mutableListOf<PublicHotelOffer>()

        // 1. Supabase 'marketplace_products' tablosundan yüklenen ürünleri çek (Sadece Operatör Yüklemeleri)
        runCatching {
            supabaseClient.postgrest["marketplace_products"]
                .select {
                    range(0, 200)
                }
                .decodeList<com.mgacreative.touros.data.database.entity.UnifiedProductEntity>()
        }.onSuccess { list ->
            list.filter { it.id.isNotBlank() }.forEach { p ->
                val baseP = p.safePrice.coerceAtLeast(100.0)
                val opName = p.safeOperatorName.ifBlank { "Coral Travel" }
                val rType = p.safeRoomType.ifBlank { "standard room, city view" }
                val mType = p.safeMealType.ifBlank { "Bez pitaniya" }
                val fCode = if (p.flightNumber.isNotBlank()) "${p.airlineName} (${p.flightNumber})" else "VKO - AYT (Ekonomi 🟢)"

                val rawType = p.safeProductType.uppercase()
                val isFlight = rawType == "FLIGHT" || rawType == "CHARTER" || rawType == "FLIGHT_ONLY" || p.airlineName.isNotBlank() || p.flightNumber.startsWith("TK-") || p.flightNumber.startsWith("N4-") || p.flightNumber.startsWith("SU-") || p.flightNumber.startsWith("PC-") || p.safeTourName.startsWith("Uçuş:", ignoreCase = true) || p.safeHotelName.startsWith("Uçuş:", ignoreCase = true) || p.safeHotelName.startsWith("✈️", ignoreCase = true)
                val isHotelOnly = !isFlight && (rawType == "HOTEL" || rawType == "LOCAL_HOTEL" || p.safeOperatorName.contains("Yerel Otel", ignoreCase = true))
                val isPromo = p.safeIsPromo || (p.customPriceOverride != null && p.customPriceOverride < baseP)

                val mappedCat = when {
                    isFlight -> "FLIGHT"
                    isHotelOnly -> "HOTEL"
                    else -> "PACKAGE_TOUR"
                }

                val cCode = when {
                    p.safeCountryCode.isNotBlank() -> p.safeCountryCode.uppercase()
                    p.safeCountry.contains("Mısır", ignoreCase = true) || p.safeCountry.contains("Egypt", ignoreCase = true) -> "EG"
                    p.safeCountry.contains("Tayland", ignoreCase = true) || p.safeCountry.contains("Thailand", ignoreCase = true) -> "TH"
                    p.safeCountry.contains("Vietnam", ignoreCase = true) -> "VN"
                    p.safeCountry.contains("Dubai", ignoreCase = true) || p.safeCountry.contains("BAE", ignoreCase = true) || p.safeCountry.contains("UAE", ignoreCase = true) -> "AE"
                    p.safeCountry.contains("Rusya", ignoreCase = true) || p.safeCountry.contains("Russia", ignoreCase = true) -> "RU"
                    else -> "TR"
                }

                val cCountryName = when (cCode) {
                    "EG" -> "Mısır"
                    "TH" -> "Tayland"
                    "VN" -> "Vietnam"
                    "AE" -> "BAE (Dubai)"
                    "RU" -> "Rusya"
                    else -> "Türkiye"
                }

                offers.add(
                    PublicHotelOffer(
                        id = p.id,
                        hotelName = p.safeHotelName.ifBlank { p.safeTourName.ifBlank { if (isFlight) "✈️ Charter Uçuş Seferi (${fCode})" else "Tur Operatörü Ürünü" } },
                        location = "${p.safeRegion.ifBlank { p.safeDepartureCity.ifBlank { "Antalya" } }}, $cCountryName",
                        stars = if (p.safeHotelCategory > 0) p.safeHotelCategory else 5,
                        description = (p.safeHotelName.ifBlank { p.safeTourName }) + " - Operatör: " + opName,
                        minPrice = baseP,
                        maxPrice = p.customPriceOverride ?: (baseP * 1.15),
                        imageUrl = p.safePictureUrl,
                        operatorName = opName,
                        roomType = rType,
                        mealType = mType,
                        flightCode = fCode,
                        nights = if (p.nights > 0) p.nights else 7,
                        currency = p.safeCurrency.ifBlank { "USD" },
                        category = mappedCat,
                        discountPercent = if (isPromo) 35 else null,
                        isLastMinute = isPromo,
                        countryCode = cCode,
                        agencyPrices = listOf(
                            AgencyPriceOption("AGN-ANEX", "Coral Travel B2B (MGA Partner)", opName, rType, mType, baseP, isBestDeal = true),
                            AgencyPriceOption("AGN-CORAL", "Coral Travel B2B", "Coral Travel", "Executive Suite", "Ultra Her Şey Dahil", baseP * 1.12),
                            AgencyPriceOption("AGN-PEGAS", "Pegas Touristik Agency", "Pegas Touristik", "Deluxe Double Room", "Oda Kahvaltı", baseP * 1.25)
                        )
                    )
                )
            }
        }

        // 2. RAM'deki yüklenen operatör ürünlerini de ekle
        val memoryList = com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel.getPersistentProducts()
        memoryList.filter { it.id.isNotBlank() }.forEach { p ->
            val baseP = p.safePrice.coerceAtLeast(100.0)
            val opName = p.safeOperatorName.ifBlank { "Coral Travel" }
            val rType = p.safeRoomType.ifBlank { "standard room" }
            val mType = p.safeMealType.ifBlank { "Bez pitaniya" }
            val fCode = if (p.flightNumber.isNotBlank()) "${p.airlineName} (${p.flightNumber})" else "VKO - AYT (Ekonomi 🟢)"

            val cCode = when {
                p.safeCountryCode.isNotBlank() -> p.safeCountryCode.uppercase()
                p.safeCountry.contains("Mısır", ignoreCase = true) || p.safeCountry.contains("Egypt", ignoreCase = true) -> "EG"
                p.safeCountry.contains("Tayland", ignoreCase = true) || p.safeCountry.contains("Thailand", ignoreCase = true) -> "TH"
                p.safeCountry.contains("Vietnam", ignoreCase = true) -> "VN"
                p.safeCountry.contains("Dubai", ignoreCase = true) || p.safeCountry.contains("BAE", ignoreCase = true) || p.safeCountry.contains("UAE", ignoreCase = true) -> "AE"
                p.safeCountry.contains("Rusya", ignoreCase = true) || p.safeCountry.contains("Russia", ignoreCase = true) -> "RU"
                else -> "TR"
            }

            val cCountryName = when (cCode) {
                "EG" -> "Mısır"
                "TH" -> "Tayland"
                "VN" -> "Vietnam"
                "AE" -> "BAE (Dubai)"
                "RU" -> "Rusya"
                else -> "Türkiye"
            }

            offers.add(
                PublicHotelOffer(
                    id = p.id,
                    hotelName = p.safeHotelName.ifBlank { p.safeTourName.ifBlank { "Operatör Ürünü" } },
                    location = "${p.safeRegion.ifBlank { "Antalya" }}, $cCountryName",
                    stars = if (p.safeHotelCategory > 0) p.safeHotelCategory else 5,
                    description = p.safeHotelName + " - Operatör Yükleme Verisi.",
                    minPrice = baseP,
                    maxPrice = p.customPriceOverride ?: (baseP * 1.15),
                    imageUrl = p.safePictureUrl.ifBlank { "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800" },
                    operatorName = opName,
                    roomType = rType,
                    mealType = mType,
                    flightCode = fCode,
                    nights = if (p.nights > 0) p.nights else 7,
                    currency = p.safeCurrency.ifBlank { "USD" },
                    countryCode = cCode,
                    agencyPrices = listOf(
                        AgencyPriceOption("AGN-ANEX", "Coral Travel B2B (MGA Partner)", opName, rType, mType, baseP, isBestDeal = true)
                    )
                )
            )
        }

        // 3. Varsayılan zengin ülke fırsatlarını her zaman ekle / tamamla
        val initialDefaults = getInitialDefaultOffers()
        initialDefaults.forEach { defOffer ->
            if (offers.none { it.id == defOffer.id }) {
                offers.add(defOffer)
            }
        }

        val combined = (offers + getInitialDefaultOffers()).distinctBy { it.id }
        dbProducts = groupOffersByHotelName(combined)
        isLoadingProducts = false
    }

    val filteredHotels = dbProducts.filter { h ->
        val isPureFlight = h.category.uppercase() == "FLIGHT" || 
                           h.hotelName.startsWith("Uçuş:", ignoreCase = true) || 
                           h.hotelName.startsWith("✈️", ignoreCase = true)

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
            targetText = "${h.location} ${h.hotelName} ${h.description} ${h.countryCode}",
            selectedDest = destinationToMatch
        )

        val depMatch = departureCity.isBlank() || departureCity.contains("Tüm", ignoreCase = true) || com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel.isDepartureMatchingText(
            targetDeparture = h.flightCode + " " + h.location,
            selectedDeparture = departureCity
        )

        categoryMatch &&
        destMatch &&
        depMatch &&
        (searchQuery.isBlank() || 
            h.hotelName.contains(searchQuery, ignoreCase = true) || 
            h.location.contains(searchQuery, ignoreCase = true) || 
            h.category.contains(searchQuery, ignoreCase = true) ||
            h.description.contains(searchQuery, ignoreCase = true) ||
            h.flightCode.contains(searchQuery, ignoreCase = true)) &&
        (selectedStarFilter == 0 || h.stars == selectedStarFilter) &&
        (maxPriceFilter >= 200000f || h.minPrice <= maxPriceFilter) &&
        (selectedOperatorFilter == "Tüm Operatörler" || h.operatorName.contains(selectedOperatorFilter, ignoreCase = true) || selectedOperatorFilter.contains(h.operatorName, ignoreCase = true))
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

                                    // 🧳 Misafir / 🏢 Acenta / 👑 Club Giriş Butonları (Minimalist & Şık)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val guestLabel = "🧳 Misafir"
                                        val agencyLabel = if (currentUser != null) "🏢 Acenta ➔" else "🏢 Acenta"
                                        val clubLabel = "👑 Club"

                                        listOf(guestLabel, agencyLabel, clubLabel).forEach { modeLabel ->
                                            val isGuest = modeLabel.contains("Misafir")
                                            val isAgency = modeLabel.contains("Acenta")
                                            val isClub = modeLabel.contains("Club")
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
                                        }
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

                                // Sağ Taraf: Düz Beyaz Font Dil Seçici + Admin + Acente/Misafir
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Düz Beyaz Font Dil Seçici (TR | EN | RU)
                                    LanguageSelector(
                                        selectedLanguage = when (currentLang.code) {
                                            "ru" -> AppLanguage.RU
                                            "en" -> AppLanguage.EN
                                            else -> AppLanguage.TR
                                        },
                                        onLanguageSelected = { lang ->
                                            AppLanguageManager.setLanguage(lang.code)
                                        }
                                    )

                                    if (isSystemAdmin) {
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
                                        val guestLabel = AppLanguageManager.translate("Misafir")
                                        val agencyLabel = if (currentUser != null) AppLanguageManager.translate("Acente Paneli") else AppLanguageManager.translate("Acenteler")

                                        listOf(guestLabel, agencyLabel).forEach { modeLabel ->
                                            val isGuest = modeLabel == guestLabel
                                            val isSelectedMode = isGuest
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelectedMode) Color(0xFF0284C7) else Color.Transparent)
                                                    .clickable {
                                                        if (!isGuest) {
                                                            onNavigateToLogin()
                                                        }
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = modeLabel,
                                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = if (isSelectedMode) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp)
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
                                val defaultHero = "hero_banner.png"
                                val headerImg = when {
                                    rawHeader.isNullOrBlank() || rawHeader.startsWith("file://") || rawHeader.startsWith("C:") || rawHeader.startsWith("D:") -> defaultHero
                                    rawHeader.contains("unsplash.com") && !rawHeader.contains("auto=format") -> {
                                        if (rawHeader.contains("?")) "$rawHeader&auto=format&fit=crop&q=80"
                                        else "$rawHeader?auto=format&fit=crop&w=1600&q=80"
                                    }
                                    else -> rawHeader
                                }
                                
                                AsyncImage(
                                    model = headerImg,
                                    contentDescription = "Header Hero Banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

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

                                // Sağ: 👑 Axileto Club VIP Rozeti (Hero Banner İçi - Büyütülmüş ve Net)
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
                                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.club_badge),
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
                            b2bTourSearchViewModel.performSearch()
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
                                b2bTourSearchViewModel.performSearch()
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
                            extraBottomContent = {
                                // ── KART İÇİ ENTEGRE FİLTRELER (OPERATÖR & YILDIZ SEÇİMİ) ──
                                var showOperatorDropdown by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Filtrele:",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    )

                                    // Operatör Seçimi (Açık Kurumsal B2B Pill)
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (selectedOperatorFilter != "Tüm Operatörler") Color(0xFF0284C7) else Color(0xFFE2E8F0)),
                                            color = if (selectedOperatorFilter != "Tüm Operatörler") Color(0xFFF0F9FF) else Color(0xFFF8FAFC),
                                            modifier = Modifier.clickable { showOperatorDropdown = !showOperatorDropdown }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "💼 " + AppLanguageManager.translate(selectedOperatorFilter),
                                                    style = TourOSTypography.Caption.copy(
                                                        color = if (selectedOperatorFilter != "Tüm Operatörler") Color(0xFF0369A1) else Color(0xFF334155),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                                Text("▼", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showOperatorDropdown,
                                            onDismissRequest = { showOperatorDropdown = false },
                                            modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                        ) {
                                            listOf("Tüm Operatörler", "Coral Travel", "Anex Tour", "Pegas Touristik", "Fun & Sun", "Jolly Tur", "Etstur").forEach { opName ->
                                                DropdownMenuItem(
                                                    text = { Text(AppLanguageManager.translate(opName), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(36.dp),
                                                    onClick = { selectedOperatorFilter = opName; showOperatorDropdown = false }
                                                )
                                            }
                                        }
                                    }

                                    // Otel Yıldız Filtresi (Açık Kurumsal B2B Pills)
                                    listOf(0 to "Tüm Yıldızlar", 5 to "5★ Deluxe", 4 to "4★+", 3 to "3★").forEach { (starVal, starLabel) ->
                                        val isSelected = selectedStarFilter == starVal
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)),
                                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF8FAFC),
                                            modifier = Modifier.clickable { selectedStarFilter = starVal }
                                        ) {
                                            Text(
                                                text = starLabel,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                style = TourOSTypography.Caption.copy(
                                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }
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
                                        Text("🔍", fontSize = 20.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "Arama Sonuçları (${b2bSearchResults.size} Paket Tur / Otel Bulundu)",
                                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        )
                                        Text(
                                            text = "🗓️ Tarih: $startDateText — $endDateText  |  📍 Destinasyon: $destinationCity  |  🌙 Gece: $selectedNightsText",
                                            style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                        )
                                    }
                                }

                                TourOSButton(
                                    text = "Aramayı Temizle / Kapat ✕",
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
                                    titleIcon = "✨",
                                    title = "Bulunan Arama Fırsatları",
                                    subtitle = "Kriterlerinize uyan en uygun fiyatlı canlı tur ve otel teklifleri",
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
                                        text = "Kriterlerinize Uygun Tur veya Otel Bulunamadı",
                                        style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    )
                                    Text(
                                        text = "Seçtiğiniz destinasyon ($destinationCity) veya kalkış noktası ($departureCity) için şu anda aktif teklif bulunamadı. Lütfen tarih veya geceleme kriterlerinizi esnetmeyi deneyiniz.",
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
            if (filteredHotels.isEmpty()) {
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
                                text = "🏨 Aradığınız kriterlere uygun tur/otel paketi bulunamadı.",
                                style = TourOSTypography.TitleMedium.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            )
                            TourOSButton(
                                text = "Filtreleri Sıfırla 🔄",
                                onClick = {
                                    searchQuery = ""
                                    selectedStarFilter = 0
                                    maxPriceFilter = 200000f
                                    selectedOperatorFilter = "Tüm Operatörler"
                                }
                            )
                        }
                    }
                }
            } else {
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
                                    Triple("TR", "Türkiye", "🇹🇷") to Triple("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&auto=format&fit=crop&q=80", "Antalya · Belek · Bodrum · Kemer", "$580'den başlayan"),
                                    Triple("EG", "Mısır", "🇪🇬") to Triple("https://images.unsplash.com/photo-1539768942893-daf53e448371?w=800&auto=format&fit=crop&q=80", "Şarm El-Şeyh · Hurgada · El Gouna", "$490'dan başlayan"),
                                    Triple("TH", "Tayland", "🇹🇭") to Triple("https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=800&auto=format&fit=crop&q=80", "Phuket · Pattaya · Bangkok · Samui", "$790'den başlayan"),
                                    Triple("VN", "Vietnam", "🇻🇳") to Triple("https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop&q=80", "Da Nang · Phu Quoc · Nha Trang", "$850'den başlayan"),
                                    Triple("AE", "BAE (Dubai)", "🇦🇪") to Triple("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80", "Dubai Marina · Palm Jumeirah", "$690'den başlayan"),
                                    Triple("RU", "Rusya", "🇷🇺") to Triple("https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800&auto=format&fit=crop&q=80", "Moskova · Sochi · St. Petersburg", "$420'den başlayan")
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
                                val subRegions = subRegionsMap[currentCountryCode] ?: listOf("Tümü")

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
                                                Text("← Tüm Ülkelere & Ana Sayfaya Dön", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = "$cFlag $cName Tatil & Paket Turları",
                                                        style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                                    )
                                                    Text(
                                                        text = "📍 $cSubInfo",
                                                        style = TourOSTypography.BodyMedium.copy(color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                                    )
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
                                                    text = "$cName Özel Arama & Nokta Atışı Filtreleme",
                                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                )
                                            }
                                            TextButton(onClick = {
                                                countryDedicatedSubRegion = "Tümü"
                                                countryDedicatedHotelQuery = ""
                                                countryDedicatedNights = "7 Gece"
                                                countryDedicatedStars = setOf(4, 5)
                                            }) {
                                                Text("↺ Filtreleri Sıfırla", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
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
                                                placeholder = { Text("Otel adı ara...", fontSize = 12.sp) },
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
                                val dedicatedCountryOffers = remember(dbProducts, currentCountryCode, countryDedicatedSubRegion, countryDedicatedHotelQuery, countryDedicatedStars) {
                                    dbProducts.filter { p ->
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
                                    titleIcon = cFlag,
                                    title = "$cName Paket Turları & Otelleri (${dedicatedCountryOffers.size} Tesis Bulundu)",
                                    subtitle = "Tarih: 12 - 19 Eyl · 2 Kişi / 7 Gece · Direkt Uçuş & Transfer Dahil",
                                    hotels = dedicatedCountryOffers,
                                    onHotelClick = { selectedHotelForDetail = it },
                                    onSelectAndBook = handleDirectBooking
                                )
                            } else {
                                // ── 1. Ülke Giriş Kartları Başlığı ──
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🌍", fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = AppLanguageManager.translate("Hizmetleri Keşfedin & Popüler Ülkeler"),
                                                style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            )
                                            Text(
                                                text = AppLanguageManager.translate("Karta tıklayarak ilgili ülkenin bağımsız arama sayfasına gidin veya anında fırsatları inceleyin"),
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                                            )
                                        }
                                    }
                                }

                                // ── 2. Yatay Kayan / Eşit Dağılımlı Görsel Ülke Kartları (Responsive Country Discovery Bar) ──
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val isWideScreen = maxWidth >= 960.dp
                                    val rowModifier = if (isWideScreen) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                    }

                                    Row(
                                        modifier = rowModifier,
                                        horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 8.dp else 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        countryDiscoveryCards.forEach { (countryInfo, cardDetail) ->
                                            val (cCode, cName, cFlag) = countryInfo
                                            val (cImage, cSubInfo, cPrice) = cardDetail
                                            val isSelected = (selectedCountryTab == cCode)

                                            val cardModifier = if (isWideScreen) {
                                                Modifier.weight(1f).height(124.dp)
                                            } else {
                                                Modifier.width(175.dp).height(120.dp)
                                            }

                                            Surface(
                                                modifier = cardModifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        selectedCountryTab = cCode
                                                        activeCountryDetailPage = if (cCode == "ALL") null else cCode
                                                        countryDedicatedSubRegion = "Tümü"
                                                        selectedSubRegionFilter = null
                                                        adultsCount = 2
                                                        flightTripType = "ROUND_TRIP"
                                                        roomsCount = 1
                                                    },
                                                shape = RoundedCornerShape(14.dp),
                                                shadowElevation = if (isSelected) 6.dp else 2.dp,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF0F5A56) else Color(0xFFE2E8F0)
                                                )
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
                                                                    colors = listOf(
                                                                        Color.Black.copy(alpha = 0.2f),
                                                                        Color.Black.copy(alpha = 0.85f)
                                                                    )
                                                                )
                                                            )
                                                    )

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(10.dp),
                                                        verticalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Surface(
                                                                color = Color.Black.copy(alpha = 0.45f),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text(
                                                                    text = "$cFlag $cName",
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                    style = TourOSTypography.BodyMedium.copy(
                                                                        color = Color.White,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 11.sp
                                                                    ),
                                                                    maxLines = 1,
                                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                                )
                                                            }

                                                            if (isSelected) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(20.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color(0xFF0F5A56)),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "✓",
                                                                        color = Color.White,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 11.sp
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                            Text(
                                                                text = cSubInfo,
                                                                style = TourOSTypography.Caption.copy(
                                                                    color = Color(0xFFCBD5E1),
                                                                    fontSize = 9.sp
                                                                ),
                                                                maxLines = 1,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "2 Kişi · 7G",
                                                                    style = TourOSTypography.Caption.copy(
                                                                        color = Color(0xFF94A3B8),
                                                                        fontSize = 9.sp
                                                                    )
                                                                )
                                                                Text(
                                                                    text = cPrice,
                                                                    style = TourOSTypography.BodyMedium.copy(
                                                                        color = Color(0xFF38BDF8),
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 11.sp
                                                                    ),
                                                                    maxLines = 1
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // ── 3. Hızlı Fırsatlar ve Sabit Bloklar ──
                                val countryFilteredProducts = remember(dbProducts, selectedCountryTab, selectedSubRegionFilter) {
                                    dbProducts.filter { p ->
                                        val matchCountry = matchesSelectedCountry(p, selectedCountryTab)
                                        val loc = p.location.lowercase()
                                        val hName = p.hotelName.lowercase()
                                        val matchesSubRegion = if (selectedSubRegionFilter.isNullOrBlank() || selectedSubRegionFilter == "Tümü") true
                                        else loc.contains(selectedSubRegionFilter!!.lowercase()) || hName.contains(selectedSubRegionFilter!!.lowercase())

                                        matchCountry && matchesSubRegion
                                    }
                                }

                                val fast2PersonDeals = countryFilteredProducts.filter {
                                    it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(4)

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
                                                    Text("⚡", fontSize = 16.sp)
                                                    Text(
                                                        text = "Hızlı Fırsatlar: 2 Kişi / 7 Gece Direkt Uçuşlu Paketler",
                                                        style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    )
                                                }
                                                Surface(
                                                    color = Color(0xFF0F5A56).copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Uçuş + Transfer + Otel Dahil",
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
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                                                    Text("⭐".repeat(dealHotel.stars.coerceIn(1, 5)), fontSize = 10.sp)
                                                                }

                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = "📍 ${dealHotel.location}",
                                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp),
                                                                        maxLines = 1
                                                                    )
                                                                    Text(
                                                                        text = "✈️ 12 - 19 Eyl (7 Gece)",
                                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                                                    )
                                                                    Text(
                                                                        text = "🍴 Her Şey Dahil",
                                                                        style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                                                    )
                                                                }
                                                            }

                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.End) {
                                                                    Text(
                                                                        text = "2 Kişi Toplam",
                                                                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                                    )
                                                                    Text(
                                                                        text = "$${(dealHotel.minPrice * 2).toInt()}",
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
                                                                        text = "Rezerve Et ➔",
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
                                val tourPackagesOnly = countryFilteredProducts.shuffled().filter { 
                                    (it.category == "PACKAGE_TOUR" || it.category == "LOCAL_TOUR") &&
                                    it.category != "FLIGHT" && it.category != "HOTEL" &&
                                    !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(20)
                                if (tourPackagesOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.BeachAccess,
                                        title = if (selectedCountryTab == "ALL") "Paket Turlar" else "${countryDiscoveryCards.firstOrNull { it.first.first == selectedCountryTab }?.first?.second ?: ""} Paket Turları",
                                        subtitle = "Gezginler Tarafından Onaylanmış Her Şey Dahil Paket Turlar",
                                        hotels = tourPackagesOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = handleDirectBooking
                                    )
                                }

                                // ── BLOK 2: OTELLER (UÇUŞLAR VE PAKET TURLAR HARİÇ - SADECE OTEL KONAKLAMASI) ──────────
                                val hotelsOnly = countryFilteredProducts.shuffled().filter { 
                                    (it.category == "HOTEL" || it.category == "LOCAL_HOTEL") &&
                                    it.category != "FLIGHT" && it.category != "PACKAGE_TOUR" &&
                                    !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(20)
                                if (hotelsOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.Hotel,
                                        title = if (selectedCountryTab == "ALL") "Oteller" else "${countryDiscoveryCards.firstOrNull { it.first.first == selectedCountryTab }?.first?.second ?: ""} Seçkin Otelleri",
                                        subtitle = "Ayrıcalıklı konaklama ve seçkin 5 yıldızlı oteller (Sadece Otel)",
                                        hotels = hotelsOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = handleDirectBooking
                                    )
                                }

                                // ── BLOK 3: SON DAKİKA (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ───────
                                val lastMinuteOnly = countryFilteredProducts.filter { 
                                    it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true) && (it.isLastMinute || (it.discountPercent ?: 0) > 0)
                                }.take(20)
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
                                val flightsOnly = countryFilteredProducts.shuffled().filter { 
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
            }

            // ── 🌟 HİZMETLERİMİZ (OUR SERVICES - FOOTER ÜSTÜ 6'LI KURUMSAL SEKSİYON) ──
            item {
                OurServicesSection(
                    companySettings = companySettings,
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
                                selectedOperatorFilter = "Tüm Operatörler"
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
                                selectedOperatorFilter = "Tüm Operatörler"
                                isInlineSearchActive = true
                                coroutineScope.launch { mainLazyListState.animateScrollToItem(1) }
                            }
                            serviceId == "CRUISE" -> { 
                                searchQuery = "Gemi"
                                selectedOperatorFilter = "Tüm Operatörler"
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
                    onNavigateToLogin = { showAgencyLoginModal = true }
                )
            }
        }
    }

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
                                text = "🏢 Acente Yetkili Girişi (B2B)",
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
                            text = "Saas B2B acente portalına ve özel operatör marjlarına erişmek için e-posta, şifre ve acente kodunuzu giriniz.",
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
                                Text(agencyLoginError ?: "", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold))
                            }
                        }

                        OutlinedTextField(
                            value = agencyEmailInput,
                            onValueChange = { agencyEmailInput = it },
                            label = { Text("Acente E-Posta Adresi") },
                            placeholder = { Text("acente@touros.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = agencyPasswordInput,
                            onValueChange = { agencyPasswordInput = it },
                            label = { Text("Şifre") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )

                        OutlinedTextField(
                            value = agencyCodeInput,
                            onValueChange = { agencyCodeInput = it },
                            label = { Text("Acente Kodu (Referral Code)") },
                            placeholder = { Text("Örn: AGN-MASTER-8492") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TourOSButton(
                                text = "İptal",
                                onClick = { showAgencyLoginModal = false },
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            TourOSButton(
                                text = "Giriş Yap & Bağlan 🚀",
                                onClick = {
                                    if (agencyEmailInput.isBlank() || agencyPasswordInput.isBlank() || agencyCodeInput.isBlank()) {
                                        agencyLoginError = "Lütfen E-Posta, Şifre ve Acente Kodu alanlarını doldurunuz."
                                    } else {
                                        coroutineScope.launch {
                                            val res = authRepository.signInWithEmail(agencyEmailInput.trim(), agencyPasswordInput.trim())
                                            if (res.isSuccess) {
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

                var guestName by remember { mutableStateOf(currentUser?.fullName ?: "Ahmet Yılmaz") }
                var guestPhone by remember { mutableStateOf("0532 100 2030") }
                var passportNo by remember { mutableStateOf("TR-8492019") }

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
                                Text("📝 Rezervasyon ve Ödeme Sihirbazı", style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold))
                                Text(
                                    text = "✖",
                                    modifier = Modifier.clickable { selectedAgencyForBooking = null },
                                    style = TourOSTypography.TitleLarge.copy(color = Color(0xFF94A3B8))
                                )
                            }

                            Text(
                                text = "Seçilen Acente: ${option.agencyName} • Operatör: ${option.operatorName}\nOtel: ${hotel.hotelName} (${option.price.toInt()} ₺)",
                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                            )

                            OutlinedTextField(
                                value = guestName,
                                onValueChange = { guestName = it },
                                label = { Text("Misafir Ad Soyad") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = guestPhone,
                                onValueChange = { guestPhone = it },
                                label = { Text("Telefon") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = passportNo,
                                onValueChange = { passportNo = it },
                                label = { Text("Pasaport / Kimlik No") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TourOSButton(
                                    text = "← Geri (Fiyat Karşılaştırma)",
                                    onClick = { selectedAgencyForBooking = null },
                                    variant = TourOSButtonVariant.TERTIARY
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                TourOSButton(
                                    text = "Bilet & Rezervasyonu Onayla 🚀",
                                    onClick = {
                                        coroutineScope.launch {
                                            val isReferralAgency = !referralCode.isNullOrBlank() || (currentUser != null && userMode == "Acente")
                                            val targetAgencyId = if (!referralCode.isNullOrBlank()) {
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

                                            val newBooking = Booking(
                                                id = validBookingId,
                                                bookingCode = pnrCode,
                                                customerName = guestName.ifBlank { "Web Misafir" },
                                                customerPhone = guestPhone.ifBlank { "+90 500 000 0000" },
                                                customerEmail = currentUser?.email ?: "web-rezervasyon@touros.com",
                                                productName = if (isFlightBooking) "✈️ Charter Uçuş Bileti: ${hotel.hotelName} (${hotel.flightCode})" else "${hotel.hotelName} (${option.roomType})",
                                                hotelId = safeHotelId,
                                                bookingType = if (isFlightBooking) "FLIGHT" else "HOTEL",
                                                roomTypeName = option.roomType,
                                                operatorName = option.operatorName.ifBlank { option.agencyName },
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
                                                        passportNo = passportNo.ifBlank { "TR-8492019" },
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

                                            bookingSuccessMessage = if (isReferralAgency) {
                                                "✅ Rezervasyon/PNR (${newBooking.bookingCode}) başarıyla oluşturuldu! Acente (${safeTenantId}) Paneline Aktarıldı."
                                            } else {
                                                "✅ Bilet/PNR (${newBooking.bookingCode}) başarıyla oluşturuldu! Ana Acente HQ (AGN-MASTER-8492) Paneline Aktarıldı."
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
                                        text = "📍 ${hotel.location} • ID: ${hotel.id}",
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
                                    text = "🏬 " + AppLanguageManager.translate("Hangi Acente / Operatör Kaç Satıyor?"),
                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                )
                                Text(
                                    text = AppLanguageManager.translate("Canlı Fiyat Karşılaştırma"),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                )
                            }

                            // ── AŞAĞI KAYDIRILABİLİR İNCE KOMPAKT OPERATÖR LİSTESİ (LAZYCOLUMN) ──
                            val effectiveAgencyPrices = if (hotel.agencyPrices.isNotEmpty()) {
                                hotel.agencyPrices
                            } else {
                                listOf(
                                    AgencyPriceOption("AGN-CORAL", "${hotel.operatorName.ifBlank { "Coral Travel" }} B2B Main", hotel.operatorName.ifBlank { "Coral Travel" }, hotel.roomType, hotel.mealType, hotel.minPrice, isBestDeal = true),
                                    AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Deluxe Room", "Her Şey Dahil", hotel.minPrice * 1.08),
                                    AgencyPriceOption("AGN-PEGAS", "Pegas Touristik Agency", "Pegas Touristik", "Standard Room", "Oda Kahvaltı", hotel.minPrice * 1.15),
                                    AgencyPriceOption("AGN-TRAVELATA", "Travelata B2B Online", "Travelata", "Promo Room", "Bez pitaniya", hotel.minPrice * 1.04),
                                    AgencyPriceOption("AGN-SUNEX", "SunExpress Charter B2B", "SunExpress", "Standart", "Ekonomi Uçuş", hotel.minPrice * 1.10)
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

// ── ⚡ DİKEY LİSTE ARAMA SONUÇLARI SEKSİYONU (HIZLI FIRSATLAR SATIR STİLİ) ──────────
@Composable
fun VerticalSearchResultsGridSection(
    titleIcon: String = "✨",
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
                    if (titleIcon.isNotBlank()) {
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

            // Tek Satırlık Fırsat / Arama Sonuçları Listesi
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hotels.forEach { hotel ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onHotelClick(hotel) },
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
                            // Küçük Resim (Sol)
                            AsyncImage(
                                model = getEffectiveImageUrl(hotel),
                                contentDescription = hotel.hotelName,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 95.dp, height = 68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            // Orta Detaylar (İsim, Yıldız, Konum, Uçuş, Yemek)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = hotel.hotelName,
                                        style = TourOSTypography.BodyMedium.copy(
                                            color = Color(0xFF1E293B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text("⭐".repeat(hotel.stars.coerceIn(1, 5)), fontSize = 10.sp)

                                    if (hotel.discountPercent != null && hotel.discountPercent > 0) {
                                        Surface(
                                            color = Color(0xFFDC2626),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "%${hotel.discountPercent} İNDİRİM",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📍 ${hotel.location}",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "✈️ ${hotel.flightCode}",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = "🍴 ${hotel.mealType}",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFFD97706), fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                    )
                                }
                            }

                            // Sağ Taraf (Fiyat ve Buton)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = AppLanguageManager.translate("2 Kişi Toplam"),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    )
                                    Text(
                                        text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.minPrice, decimals = false)} ${hotel.currency}",
                                        style = TourOSTypography.TitleMedium.copy(
                                            color = Color(0xFF0F5A56),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = { onSelectAndBook(hotel) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0F5A56)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = AppLanguageManager.translate("Rezerve Et ➔"),
                                        color = Color.White,
                                        fontSize = 12.sp,
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
                AsyncImage(
                    model = getEffectiveImageUrl(hotel),
                    contentDescription = hotel.hotelName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

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
                    Text(
                        text = if (isFlightCard) hotel.flightCode.ifBlank { AppLanguageManager.translate("Charter Uçuş Seferi") } else hotel.hotelName,
                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isFlightCard) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (isFlightCard) AppLanguageManager.translate("Direkt Uçuş") else "Starway Award",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = TourOSTypography.Caption.copy(color = if (isFlightCard) Color(0xFF1D4ED8) else Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = if (isFlightCard) "20 kg Bagaj" else hotel.flightCode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val publicDestinationText = remember(hotel.location, hotel.hotelName) {
                    val hName = hotel.hotelName.lowercase()
                    val loc = hotel.location
                    when {
                        loc.contains("Kemer", ignoreCase = true) || hName.contains("kemer") -> "🇹🇷 Türkiye · Antalya · Kemer"
                        loc.contains("Belek", ignoreCase = true) || hName.contains("belek") -> "🇹🇷 Türkiye · Antalya · Belek"
                        loc.contains("Lara", ignoreCase = true) || loc.contains("Kundu", ignoreCase = true) || hName.contains("lara") || hName.contains("kundu") -> "🇹🇷 Türkiye · Antalya · Lara"
                        loc.contains("Alanya", ignoreCase = true) || hName.contains("alanya") -> "🇹🇷 Türkiye · Antalya · Alanya"
                        loc.contains("Side", ignoreCase = true) || loc.contains("Manavgat", ignoreCase = true) || hName.contains("side") -> "🇹🇷 Türkiye · Antalya · Side"
                        loc.contains("Bodrum", ignoreCase = true) || hName.contains("bodrum") -> "🇹🇷 Türkiye · Muğla · Bodrum"
                        loc.contains("Marmaris", ignoreCase = true) || hName.contains("marmaris") -> "🇹🇷 Türkiye · Muğla · Marmaris"
                        loc.contains("Fethiye", ignoreCase = true) || hName.contains("fethiye") -> "🇹🇷 Türkiye · Muğla · Fethiye"
                        loc.contains("Moskova", ignoreCase = true) -> "🇷🇺 Rusya · Moskova"
                        loc.contains("Dubai", ignoreCase = true) -> "🇦🇪 BAE · Dubai"
                        loc.contains("Şarm", ignoreCase = true) || loc.contains("Hurgada", ignoreCase = true) -> "🇪🇬 Mısır · Şarm El-Şeyh"
                        else -> "🇹🇷 Türkiye · $loc"
                    }
                }

                // Lokasyon & Tur / Uçuş Bilgisi
                Text(
                    text = if (isFlightCard) AppLanguageManager.translate("Moskova Kalkışlı · Ekonomi Sınıfı · Gidiş-Dönüş") else "$publicDestinationText · ${hotel.nights} ${AppLanguageManager.translate("Gece")} · ${hotel.mealType}",
                    style = TourOSTypography.Caption.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── MÜŞTERİNİN İSTEDİĞİ EN DÜŞÜK VE EN YÜKSEK FİYAT ALANI ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${AppLanguageManager.translate("En Düşük Fiyat")} :", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                            Text("${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.minPrice, decimals = false)} ${if (hotel.currency == "RUB") "RUB" else "₺"}", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${AppLanguageManager.translate("En Yüksek Fiyat")}:", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                            Text("${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(hotel.maxPrice, decimals = false)} ${if (hotel.currency == "RUB") "RUB" else "₺"}", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp))
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
    onNavigateToLogin: () -> Unit
) {
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
                            text = "Kurumsal İletişim",
                            style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )

                        val phoneVal = companySettings?.webPhone.orEmpty()
                        val emailVal = companySettings?.webEmail.orEmpty()
                        val whatsappVal = companySettings?.webWhatsapp.orEmpty()
                        val addressVal = companySettings?.webAddress.orEmpty()

                        if (phoneVal.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
                                Text("Tel: $phoneVal", style = TourOSTypography.Caption.copy(color = Color(0xFFCBD5E1), fontSize = 12.sp))
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
                                Text("E-Posta: $emailVal", style = TourOSTypography.Caption.copy(color = Color(0xFFCBD5E1), fontSize = 12.sp))
                            }
                        }
                        if (addressVal.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Text("Adres: $addressVal", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp), maxLines = 2)
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
                            text = "Yasal & Lisans",
                            style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                        val mersis = companySettings?.webMersisNo.orEmpty()
                        val taxNo = companySettings?.webTaxNumber.orEmpty()
                        val taxOffice = companySettings?.webTaxOffice.orEmpty()

                        if (mersis.isNotBlank()) Text("MERSİS No: $mersis", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp))
                        if (taxOffice.isNotBlank()) Text("Vergi D.: $taxOffice", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp))
                        if (taxNo.isNotBlank()) Text("Vergi No: $taxNo", style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 11.sp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                                Text("SSL 256-Bit", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = TourOSTypography.Caption.copy(color = Color(0xFF10B981), fontSize = 9.sp))
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                                Text("TURSAB A-Grubu", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = TourOSTypography.Caption.copy(color = Color(0xFF38BDF8), fontSize = 9.sp))
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
                    val compName = companySettings?.name?.ifBlank { "TourOS B2B Travel Market" } ?: "TourOS B2B Travel Market"
                    val footerCopyright = companySettings?.footerText?.ifBlank { "© 2026 $compName. Tüm hakları saklıdır." } ?: "© 2026 $compName. Tüm hakları saklıdır."
                    Text(
                        text = footerCopyright,
                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                    )
                    Text(
                        text = "Powered by MGA Creative Software Architecture",
                        style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}

// ── 🌟 HİZMETLERİMİZ (OUR SERVICES - SCREENSHOT BİREBİR KART GRID SEKSİYONU) ────────
@Composable
fun OurServicesSection(
    companySettings: CompanySettings? = null,
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
                // 📥 Uygulama İndirme Butonları (Windows Desktop .exe & Android .apk)
                val uriHandler = LocalUriHandler.current
                val desktopUrl = companySettings?.desktopAppUrl?.trim().takeIf { !it.isNullOrBlank() } ?: "https://axileto.com/downloads/TourOS-Desktop.exe"
                val apkUrl = companySettings?.androidApkUrl?.trim().takeIf { !it.isNullOrBlank() } ?: "https://axileto.com/downloads/TourOS-Mobile.apk"

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
                                text = "Windows Masaüstü (.exe)",
                                style = TourOSTypography.BodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Hemen İndir & Kur",
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
                                text = "Android Mobil (.apk)",
                                style = TourOSTypography.BodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "Doğrudan İndir & Yükle",
                                style = TourOSTypography.Caption.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // 6'lı Kart Grid Düzeni (3 Kolon x 2 Satır)
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

private data class ServiceCardData(
    val id: String,
    val icon: String,
    val title: String,
    val description: String
)

// ── 🏨 TEK KART METASEARCH GRUPLAMA FONKSİYONU ───────────────────────────────
fun groupOffersByHotelName(rawOffers: List<PublicHotelOffer>): List<PublicHotelOffer> {
    if (rawOffers.isEmpty()) return emptyList()

    val grouped = rawOffers.groupBy { offer ->
        val normName = offer.hotelName.trim().lowercase()
            .replace(" hotel", "")
            .replace(" resort", "")
            .replace(" spa", "")
            .replace(" &", "")
        "${offer.category}_$normName"
    }

    return grouped.map { (_, list) ->
        val first = list.first()

        val combinedAgencyPrices = mutableListOf<AgencyPriceOption>()
        list.forEach { item ->
            if (item.agencyPrices.isNotEmpty()) {
                combinedAgencyPrices.addAll(item.agencyPrices)
            } else {
                combinedAgencyPrices.add(
                    AgencyPriceOption(
                        agencyId = item.id,
                        agencyName = item.operatorName + " Partner",
                        operatorName = item.operatorName,
                        roomType = item.roomType,
                        boardType = item.mealType,
                        price = item.minPrice,
                        isBestDeal = false
                    )
                )
            }
        }

        val distinctPrices = combinedAgencyPrices.distinctBy { "${it.agencyName}_${it.operatorName}_${it.price.toInt()}" }

        val minPrice = distinctPrices.minOfOrNull { it.price } ?: first.minPrice
        val maxPrice = distinctPrices.maxOfOrNull { it.price }?.coerceAtLeast(minPrice) ?: first.maxPrice

        val updatedPrices = distinctPrices.map { opt ->
            opt.copy(isBestDeal = (opt.price == minPrice))
        }

        val allOperators = updatedPrices.map { it.operatorName.ifBlank { it.agencyName } }.distinct().joinToString(" • ")

        first.copy(
            minPrice = minPrice,
            maxPrice = maxPrice,
            operatorName = if (allOperators.isNotBlank()) allOperators else first.operatorName,
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
                    placeholder = { Text("PNR / Bilet Kodu veya Müşteri Adı ile Ara...") },
                    modifier = Modifier.width(360.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HEPSİ", "FLIGHT", "HOTEL").forEach { fType ->
                        val label = when (fType) {
                            "FLIGHT" -> "✈️ Uçuşlar"
                            "HOTEL" -> "🏨 Oteller & Turlar"
                            else -> "Tüm Biletler"
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
                    Text("⏳ Rezervasyonlar yükleniyor...", style = TourOSTypography.TitleMedium.copy(color = Color(0xFF64748B)))
                }
            } else if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📭 Aradığınız kriterlere uygun henüz bir acente rezervasyonu bulunamadı.", style = TourOSTypography.BodyMedium.copy(color = Color(0xFF94A3B8)))
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
    val monthNames = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    val parts = if (initialDateText.contains(".")) {
        initialDateText.split(".")
    } else if (initialDateText.contains("-")) {
        val p = initialDateText.split("-")
        if (p.size == 3 && p[0].length == 4) listOf(p[2], p[1], p[0]) else p
    } else listOf()

    val initDay = parts.getOrNull(0)?.toIntOrNull() ?: 20
    val initMonth = (parts.getOrNull(1)?.toIntOrNull() ?: 8).coerceIn(1, 12)
    val initYear = parts.getOrNull(2)?.toIntOrNull() ?: 2026

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
                        Text("İptal", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
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
                                                text = if (age == 0) "0 Yaş (Bebek)" else "$age Yaş",
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
                                                        text = if (a == 0) "0 Yaş (0 - 1.99 Bebek)" else if (a in 2..6) "$a Yaş (2 - 6 Çocuk)" else "$a Yaş",
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


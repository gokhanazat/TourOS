package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import touros.shared.generated.resources.Res
import touros.shared.generated.resources.axileto_logo_white
import com.mgacreative.touros.domain.model.PromoBannerItem
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
        region = this.location.removeSuffix(", Türkiye").removeSuffix(", Turkey").trim(),
        price = this.minPrice,
        currency = this.currency,
        nights = this.nights,
        mealType = this.mealType,
        roomType = this.roomType,
        flightNumber = this.flightCode,
        hotelCategory = this.stars,
        operatorName = this.operatorName,
        pictureUrl = this.imageUrl
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
    onNavigateToBookingDetail: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAdminCms: () -> Unit = {},
    onNavigateToNewBooking: (PublicHotelOffer) -> Unit = {},
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
    var searchQuery by remember { mutableStateOf("") }
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

    if (showRussianDepartureModal) {
        com.mgacreative.touros.ui.components.RussianDepartureCityPickerDialog(
            currentSelection = departureCity,
            onCitySelected = { city ->
                departureCity = "${city.nameRu} (${city.airportCode})"
            },
            onDismiss = { showRussianDepartureModal = false }
        )
    }

    if (showHierarchicalDestModal) {
        com.mgacreative.touros.ui.components.HierarchicalDestinationPickerDialog(
            currentSelection = destinationCity,
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

    val supabaseClient: io.github.jan.supabase.SupabaseClient = koinInject()

    // Dynamic Database Products State (Varsayılan içerikle anında başlatılır)
    var dbProducts by remember { mutableStateOf<List<PublicHotelOffer>>(getInitialDefaultOffers()) }
    var isLoadingProducts by remember { mutableStateOf(true) }

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
                val isFlight = rawType.contains("FLIGHT") || rawType.contains("CHARTER") || (p.flightNumber.isNotBlank() && p.hotelName.isBlank())
                val isHotelOnly = rawType.contains("HOTEL") || (p.hotelName.isNotBlank() && p.flightNumber.isBlank())
                val isPromo = p.safeIsPromo || (p.customPriceOverride != null && p.customPriceOverride < baseP)

                val mappedCat = when {
                    isFlight -> "FLIGHT"
                    isHotelOnly -> "HOTEL"
                    isPromo -> "LAST_MINUTE"
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

        categoryMatch &&
        (selectedDestinationFilter == "Tüm Destinasyonlar" || 
            h.location.contains(selectedDestinationFilter.substringBefore(" "), ignoreCase = true) || 
            h.hotelName.contains(selectedDestinationFilter.substringBefore(" "), ignoreCase = true) ||
            h.description.contains(selectedDestinationFilter.substringBefore(" "), ignoreCase = true)) &&
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

                                    // 🧳 Misafir / 🏢 Acenta Giriş Butonları (Minimalist & Şık)
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

                                        listOf(guestLabel, agencyLabel).forEach { modeLabel ->
                                            val isGuest = modeLabel.contains("Misafir")
                                            val isSelectedMode = (isGuest && userMode == "Turist") || (!isGuest && userMode == "Acente")
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelectedMode) Color(0xFF0284C7) else Color.Transparent)
                                                    .clickable {
                                                        if (isGuest) {
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
                                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = if (isSelectedMode) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp)
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
                                                Text(text = "Admin Paneli", style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                            }
                                        }
                                    }

                                    // Misafir / Acenta Seçici Segment Butonları (Minimalist & Şık)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val guestLabel = "Misafir"
                                        val agencyLabel = if (currentUser != null) "Acenta Paneli" else "Acenta"

                                        listOf(guestLabel, agencyLabel).forEach { modeLabel ->
                                            val isGuest = modeLabel.contains("Misafir")
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
                                val headerImg = when {
                                    rawHeader.isNullOrBlank() -> null
                                    rawHeader.contains("unsplash.com") && !rawHeader.contains("auto=format") -> {
                                        if (rawHeader.contains("?")) "$rawHeader&auto=format&fit=crop&q=80"
                                        else "$rawHeader?auto=format&fit=crop&w=1200&q=80"
                                    }
                                    else -> rawHeader
                                }
                                
                                if (!headerImg.isNullOrBlank()) {
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

            // ── 3. YENİ NESİL ÇİFTLİ HERO & DİKEY TEAL ARAMA MOTORU PANALİ (SCREENSHOT BİREBİR) ──
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 1320.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp)
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // ── SOL TARAF: ÖZEL TANITIM SLIDER PROMOSYON BANNER (SAĞ KUTU İLE BİREBİR AYNI HİZADA) ──
                        val uriHandler = LocalUriHandler.current
                        val activeSlide = promoBannersList.getOrNull(currentPromoSlideIndex % (promoBannersList.size.coerceAtLeast(1)))
                        val rawUrl = activeSlide?.imageUrl?.trim().orEmpty()
                        val slideImgUrl = when {
                            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
                            rawUrl.startsWith("file://") -> rawUrl
                            rawUrl.length >= 2 && rawUrl[1] == ':' -> "file:///" + rawUrl.replace("\\", "/")
                            rawUrl.startsWith("/") -> "file://" + rawUrl
                            else -> rawUrl
                        }
                        val slideTitleText = activeSlide?.title ?: ""
                        val slideTargetLink = activeSlide?.targetUrl

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0F2942), Color(0xFF0A1C30))
                                    )
                                )
                                .clickable(enabled = !slideTargetLink.isNullOrBlank()) {
                                    slideTargetLink?.let { link ->
                                        runCatching { uriHandler.openUri(link) }
                                    }
                                },
                            contentAlignment = Alignment.BottomStart
                        ) {
                            key(currentPromoSlideIndex, slideImgUrl) {
                                AsyncImage(
                                    model = slideImgUrl,
                                    contentDescription = slideTitleText,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier.padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = slideTitleText,
                                    style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 38.sp)
                                )
                                if (!slideTargetLink.isNullOrBlank()) {
                                    Text(
                                        text = "Fırsatı İncele ➔",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }
                            }

                            // 🔘 Birden fazla slayt varsa Sol/Sağ Butonları ve Nokta İndikatörleri
                            if (promoBannersList.size > 1) {
                                // Sol & Sağ Oklar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .clickable {
                                                val safeSize = promoBannersList.size.coerceAtLeast(1)
                                                currentPromoSlideIndex = (currentPromoSlideIndex - 1 + safeSize) % safeSize
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("❮", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .clickable {
                                                val safeSize = promoBannersList.size.coerceAtLeast(1)
                                                currentPromoSlideIndex = (currentPromoSlideIndex + 1) % safeSize
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("❯", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }

                                // Üst Sağ Slayt İndikatör Noktaları
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    promoBannersList.indices.forEach { idx ->
                                        val safeSize = promoBannersList.size.coerceAtLeast(1)
                                        val isSel = (idx == (currentPromoSlideIndex % safeSize))
                                        Box(
                                            modifier = Modifier
                                                .height(8.dp)
                                                .width(if (isSel) 24.dp else 8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSel) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f))
                                                .clickable { currentPromoSlideIndex = idx }
                                        )
                                    }
                                }
                            }
                        }

                        // ── SAĞ TARAF: DİKEY KURUMSAL LACİVERT ARAMA MOTORU KUTUSU (DARALTILMIŞ / KOMPAKT & MODERN) ──
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp)),
                            color = Color(0xFF0A2540), // Kurumsal Derin Lacivert
                            shadowElevation = 10.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // ── SEKMELER (TABS) ──
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF061B2E))
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(
                                        "PACKAGE_TOUR" to AppLanguageManager.translate("Tur Paketi"),
                                        "HOTEL" to AppLanguageManager.translate("Oteller"),
                                        "FLIGHT" to AppLanguageManager.translate("Uçuşlar")
                                    ).forEach { (tabKey, tabTitle) ->

                                        val isSelected = selectedSearchCategoryTab == tabKey || (tabKey == "PACKAGE_TOUR" && selectedSearchCategoryTab == "ALL")
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFF0284C7) else Color.Transparent)
                                                .clickable { selectedSearchCategoryTab = tabKey }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tabTitle,
                                                style = TourOSTypography.Caption.copy(
                                                    color = Color.White,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                if (selectedSearchCategoryTab == "FLIGHT") {
                                    // ── ✈️ UÇUŞLAR SEKMESİNE ÖZEL FİLTRE DÜZENİ (MOCKUP BİREBİR) ──

                                    // 1. Destinasyon (1. Ülke & Destinasyon / 2. Rusya Kalkış Şehri)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(AppLanguageManager.translate("Destinasyon"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // 1. Nereye (Ülke & Destinasyon) - ÖNCE ÜLKEDEN BAŞLAMA
                                            Box(
                                                modifier = Modifier
                                                    .weight(1.15f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showHierarchicalDestModal = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("1. Nereye (Ülke / Şehir)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(destinationCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                            }

                                            // 2. Nereden (Rusya Kalkış Şehri - Sadece Rusya)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showRussianDepartureModal = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text("2. " + AppLanguageManager.translate("Kalkış (Rusya)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(departureCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. Tek Yön / Gidiş & Dönüş Seçenekleri
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.clickable { flightTripType = "ONE_WAY" }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (flightTripType == "ONE_WAY") {
                                                    Text("✓", color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                                }
                                            }
                                            Text(AppLanguageManager.translate("Tek Yön"), style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.clickable { flightTripType = "ROUND_TRIP" }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (flightTripType == "ROUND_TRIP") {
                                                    Text("✓", color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                                }
                                            }
                                            Text(AppLanguageManager.translate("Gidiş & Dönüş"), style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                                        }
                                    }

                                    // 3. Gidiş Tarihi & Dönüş Tarihi Seçimi
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(AppLanguageManager.translate("Gidiş Tarihi"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .clickable { showStartDatePicker = true }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text(startDateText, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp))
                                                    Text("📅", fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(AppLanguageManager.translate("Dönüş Tarihi"), style = TourOSTypography.Caption.copy(color = Color.White.copy(alpha = if (flightTripType == "ROUND_TRIP") 1f else 0.6f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (flightTripType == "ROUND_TRIP") Color.White else Color.White.copy(alpha = 0.5f))
                                                    .clickable(enabled = flightTripType == "ROUND_TRIP") { showEndDatePicker = true }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = if (flightTripType == "ROUND_TRIP") endDateText else AppLanguageManager.translate("Dönüş Ekleyin"),
                                                        style = TourOSTypography.Caption.copy(
                                                            color = if (flightTripType == "ROUND_TRIP") Color(0xFF0F172A) else Color(0xFF64748B),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                    Text("📅", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // ── TÜM DİĞER SEKMELER (Tur Paketi & Oteller) FİLTRELERİ ──
                                    // 1. DESTİNASYON (1. Ülke & Destinasyon / 2. Rusya Kalkış Şehri)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(AppLanguageManager.translate("Destinasyon"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // 1. Nereye (Ülke & Destinasyon) - ÖNCE ÜLKEDEN BAŞLAMA
                                            Box(
                                                modifier = Modifier
                                                    .weight(1.15f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showHierarchicalDestModal = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("1. Nereye (Ülke / Şehir)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(destinationCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                            }

                                            // 2. Nereden (Rusya Kalkış Şehri - Sadece Rusya)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showRussianDepartureModal = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text("2. " + AppLanguageManager.translate("Kalkış (Rusya)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(departureCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. TARİH ARALIĞI (GİDİŞ & DÖNÜŞ B2B FORMATI)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(AppLanguageManager.translate("Tarih"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Gidiş Tarih Aralığı
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showDepartureRangePicker = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Gidiş Tarih Aralığı"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text("$startDateText — $endDateText", style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("📅", fontSize = 10.sp)
                                                    }
                                                }
                                            }

                                            // Dönüş Tarih Aralığı
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showReturnRangePicker = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Dönüş Tarih Aralığı"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text("$returnStartDateText — $returnEndDateText", style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("📅", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 3. OPSİYON
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(AppLanguageManager.translate("Opsiyon"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Gece Sayısı
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showNightsDropdown = !showNightsDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Gece Sayısı"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(AppLanguageManager.translate(selectedNightsText), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showNightsDropdown,
                                                    onDismissRequest = { showNightsDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("1 - 4 Gece", "5 - 7 Gece", "7 - 10 Gece", "10 - 14 Gece", "14 - 21 Gece", "Tüm Geceler (1 - 30)").forEach { nightOpt ->
                                                        DropdownMenuItem(
                                                            text = { Text(AppLanguageManager.translate(nightOpt), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { selectedNightsText = nightOpt; showNightsDropdown = false }
                                                        )
                                                    }
                                                }
                                            }

                                            // Turist ve Oda Sayısı (Booking.com Formatı Modal Tetikleyici)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showGuestRoomModal = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Misafir & Oda"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(selectedTouristsText, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 4. OPERATÖR VE ÜLKE
                                    var showOperatorDropdown by remember { mutableStateOf(false) }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(AppLanguageManager.translate("Tur Operatörü"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .clickable { showOperatorDropdown = !showOperatorDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(AppLanguageManager.translate(selectedOperatorFilter), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                }
                                                DropdownMenu(
                                                    expanded = showOperatorDropdown,
                                                    onDismissRequest = { showOperatorDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("Tüm Operatörler", "Coral Travel", "Anex Tour", "Pegas Touristik", "Fun & Sun").forEach { opName ->
                                                        DropdownMenuItem(
                                                            text = { Text(AppLanguageManager.translate(opName), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { selectedOperatorFilter = opName; showOperatorDropdown = false }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(AppLanguageManager.translate("Ülke"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .clickable { showCountryDropdown = !showCountryDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(AppLanguageManager.translate(selectedCountryFilter), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                }
                                                DropdownMenu(
                                                    expanded = showCountryDropdown,
                                                    onDismissRequest = { showCountryDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("Türkiye", "Mısır", "Dubai (BAE)", "Yunanistan", "İspanya", "İtalya", "Tayland", "Tüm Ülkeler").forEach { country ->
                                                        DropdownMenuItem(
                                                            text = { Text(AppLanguageManager.translate(country), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { selectedCountryFilter = country; showCountryDropdown = false }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // ── 5. BÜYÜK BEYAZ BUTON (ARAMA BAŞLAT) ──
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color.White)
                                        .clickable {
                                            val destClean = destinationCity.substringBefore("(").trim()
                                            val (minNights, maxNights) = when {
                                                selectedNightsText.contains("1 - 4") -> 1 to 4
                                                selectedNightsText.contains("5 - 7") -> 5 to 7
                                                selectedNightsText.contains("7 - 10") -> 7 to 10
                                                selectedNightsText.contains("10 - 14") -> 10 to 14
                                                selectedNightsText.contains("14 - 21") -> 14 to 21
                                                selectedNightsText.contains("3") -> 3 to 3
                                                selectedNightsText.contains("5") -> 5 to 5
                                                selectedNightsText.contains("7") -> 7 to 7
                                                selectedNightsText.contains("10") -> 10 to 10
                                                selectedNightsText.contains("14") -> 14 to 14
                                                else -> 1 to 30
                                            }

                                            fun parseDateToInt(dStr: String?): Int {
                                                if (dStr.isNullOrBlank()) return 0
                                                val parts = if (dStr.contains(".")) dStr.split(".")
                                                else if (dStr.contains("-")) {
                                                    val p = dStr.split("-")
                                                    if (p.size == 3 && p[0].length == 4) listOf(p[2], p[1], p[0]) else p
                                                } else emptyList()
                                                val d = parts.getOrNull(0)?.toIntOrNull() ?: 1
                                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
                                                val y = parts.getOrNull(2)?.toIntOrNull() ?: 2026
                                                return y * 10000 + m * 100 + d
                                            }

                                            val startInt = parseDateToInt(startDateText)
                                            val endInt = parseDateToInt(endDateText)

                                            fun getOfferDatePriorityScore(h: PublicHotelOffer): Int {
                                                var score = 0
                                                val depInt = parseDateToInt(h.departureDate)
                                                if (depInt in startInt..endInt && startInt > 0) {
                                                    score += 10000 // Seçili Gidiş Tarih Aralığı Tam Eşleşmesi (En Yüksek Öncelik)
                                                } else if (depInt > 0 && startInt > 0) {
                                                    val diff = kotlin.math.abs(depInt - startInt)
                                                    if (diff <= 3) score += 5000 // ±3 Gün Esneklik
                                                    else score += (2000 - diff * 10).coerceAtLeast(0)
                                                }
                                                if (selectedOperatorFilter != "Tüm Operatörler" && h.operatorName.equals(selectedOperatorFilter, ignoreCase = true)) {
                                                    score += 500
                                                }
                                                // Assuming matchesSelectedCountry is available in the scope or logic
                                                if (selectedCountryFilter != "Tüm Ülkeler" && (h.location.contains(selectedCountryFilter, ignoreCase = true))) {
                                                    score += 500
                                                }
                                                return score
                                            }

                                            // 1. Aşama: Tam Eşleşme
                                            var matches = dbProducts.filter { h ->
                                                val isPureFlight = h.category.uppercase() == "FLIGHT" || h.hotelName.startsWith("Uçuş:", ignoreCase = true) || h.hotelName.startsWith("✈️", ignoreCase = true)
                                                if (selectedSearchCategoryTab == "FLIGHT" && !isPureFlight) return@filter false
                                                if (selectedSearchCategoryTab != "FLIGHT" && isPureFlight) return@filter false

                                                val matchesDest = destClean.isBlank() || destClean.startsWith("Tüm", ignoreCase = true) || destClean.startsWith("Nereye", ignoreCase = true) ||
                                                    h.location.contains(destClean, ignoreCase = true) ||
                                                    h.hotelName.contains(destClean, ignoreCase = true)

                                                val matchesNights = (h.nights in minNights..maxNights || h.nights <= 0)
                                                if (selectedSearchCategoryTab == "FLIGHT") matchesDest else (matchesDest && matchesNights)
                                            }

                                            // 2. Aşama: Esnek Gece / Destinasyon Düşüşü
                                            if (matches.isEmpty()) {
                                                matches = dbProducts.filter { h ->
                                                    val isPureFlight = h.category.uppercase() == "FLIGHT" || h.hotelName.startsWith("Uçuş:", ignoreCase = true) || h.hotelName.startsWith("✈️", ignoreCase = true)
                                                    if (selectedSearchCategoryTab == "FLIGHT" && !isPureFlight) return@filter false
                                                    if (selectedSearchCategoryTab != "FLIGHT" && isPureFlight) return@filter false

                                                    destClean.isBlank() || destClean.startsWith("Tüm", ignoreCase = true) || destClean.startsWith("Nereye", ignoreCase = true) ||
                                                    h.location.contains(destClean, ignoreCase = true) ||
                                                    h.hotelName.contains(destClean, ignoreCase = true)
                                                }
                                            }

                                            val pureFlightFallback = dbProducts.filter { h -> 
                                                h.category.uppercase() == "FLIGHT" || h.hotelName.startsWith("✈️", ignoreCase = true) || h.hotelName.startsWith("Uçuş:", ignoreCase = true)
                                            }
                                            val nonFlightFallback = dbProducts.filter { h -> 
                                                !(h.category.uppercase() == "FLIGHT" || h.hotelName.startsWith("✈️", ignoreCase = true) || h.hotelName.startsWith("Uçuş:", ignoreCase = true))
                                            }

                                            val sortedMatches = matches.sortedByDescending { getOfferDatePriorityScore(it) }
                                            val sortedPureFlightFallback = pureFlightFallback.sortedByDescending { getOfferDatePriorityScore(it) }
                                            val sortedNonFlightFallback = nonFlightFallback.sortedByDescending { getOfferDatePriorityScore(it) }

                                            searchResultsList = sortedMatches.ifEmpty { if (selectedSearchCategoryTab == "FLIGHT") sortedPureFlightFallback else sortedNonFlightFallback }
                                            isInlineSearchActive = true
                                            coroutineScope.launch {
                                                runCatching { mainLazyListState.animateScrollToItem(3) }
                                            }
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = AppLanguageManager.translate("ARAMA BAŞLAT"),
                                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0A2540), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    )
                                }
                            }
                        }
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
                                            text = "Arama Sonuçları (${searchResultsList.ifEmpty { filteredHotels }.size} Paket Tur / Otel Bulundu)",
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

                            // ── Bulunan Arama Fırsatları Dikey Kart Izgarası (Vertical Grid)
                            VerticalSearchResultsGridSection(
                                titleIcon = "✨",
                                title = "Bulunan Arama Fırsatları",
                                subtitle = "Kriterlerinize uyan en uygun fiyatlı canlı tur ve otel teklifleri",
                                hotels = searchResultsList.ifEmpty { filteredHotels },
                                onHotelClick = { selectedHotelForDetail = it },
                                onSelectAndBook = { selectedHotelForDetail = it }
                            )
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
                                    Triple("TH", "Tayland", "🇹🇭") to Triple("https://images.unsplash.com/photo-1589394815804-964ed0be2eb5?w=800&auto=format&fit=crop&q=80", "Phuket · Pattaya · Bangkok · Samui", "$790'dan başlayan"),
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
                                    onSelectAndBook = { selectedHotelForDetail = it }
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
                                                text = "Hizmetleri Keşfedin & Popüler Ülkeler",
                                                style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            )
                                            Text(
                                                text = "Karta tıklayarak ilgili ülkenin bağımsız arama sayfasına gidin veya anında fırsatları inceleyin",
                                                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                                            )
                                        }
                                    }
                                }

                                // ── 2. Yatay Kayan Görsel Ülke Kartları (Country Photo Carousel) ──
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    countryDiscoveryCards.forEach { (countryInfo, cardDetail) ->
                                        val (cCode, cName, cFlag) = countryInfo
                                        val (cImage, cSubInfo, cPrice) = cardDetail
                                        val isSelected = (selectedCountryTab == cCode)

                                        Surface(
                                            modifier = Modifier
                                                .width(220.dp)
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    selectedCountryTab = cCode
                                                    activeCountryDetailPage = if (cCode == "ALL") null else cCode
                                                    countryDedicatedSubRegion = "Tümü"
                                                    selectedSubRegionFilter = null
                                                    adultsCount = 2
                                                    flightTripType = "ROUND_TRIP"
                                                    roomsCount = 1
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            shadowElevation = if (isSelected) 8.dp else 2.dp,
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
                                                        .padding(12.dp),
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Surface(
                                                            color = Color.Black.copy(alpha = 0.4f),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text(
                                                                text = "$cFlag $cName",
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                style = TourOSTypography.BodyMedium.copy(
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp
                                                                )
                                                            )
                                                        }

                                                        if (isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(22.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFF0F5A56)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "✓",
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(
                                                            text = cSubInfo,
                                                            style = TourOSTypography.Caption.copy(
                                                                color = Color(0xFFCBD5E1),
                                                                fontSize = 10.sp
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
                                                                text = "2 Kişi · 7 Gece",
                                                                style = TourOSTypography.Caption.copy(
                                                                    color = Color(0xFF94A3B8),
                                                                    fontSize = 10.sp
                                                                )
                                                            )
                                                            Text(
                                                                text = cPrice,
                                                                style = TourOSTypography.BodyMedium.copy(
                                                                    color = Color(0xFF38BDF8),
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp
                                                                )
                                                            )
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
                                                                    onClick = { selectedHotelForDetail = dealHotel },
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

                                // ── BLOK 1: 🏖️ PAKET TURLAR (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ─────
                                val tourPackagesOnly = countryFilteredProducts.shuffled().filter { 
                                    it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                                }.take(20)
                                if (tourPackagesOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.BeachAccess,
                                        title = if (selectedCountryTab == "ALL") "Paket Turlar" else "${countryDiscoveryCards.firstOrNull { it.first.first == selectedCountryTab }?.first?.second ?: ""} Paket Turları",
                                        subtitle = "Gezginler Tarafından Onaylanmış Her Şey Dahil Paket Turlar",
                                        hotels = tourPackagesOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = { selectedHotelForDetail = it }
                                    )
                                }

                                // ── BLOK 2: OTELLER (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ──────────
                                val hotelsOnly = countryFilteredProducts.shuffled().filter { 
                                    it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true) && (it.category == "HOTEL" || it.stars >= 4)
                                }.ifEmpty { tourPackagesOnly }.take(20)
                                if (hotelsOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.Hotel,
                                        title = if (selectedCountryTab == "ALL") "Oteller" else "${countryDiscoveryCards.firstOrNull { it.first.first == selectedCountryTab }?.first?.second ?: ""} Seçkin Otelleri",
                                        subtitle = "Ayrıcalıklı konaklama ve seçkin 5 yıldızlı oteller",
                                        hotels = hotelsOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = { selectedHotelForDetail = it }
                                    )
                                }

                                // ── BLOK 3: SON DAKİKA (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ───────
                                val lastMinuteOnly = countryFilteredProducts.filter { 
                                    it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true) && (it.isLastMinute || (it.discountPercent ?: 0) > 0)
                                }.ifEmpty { tourPackagesOnly }.take(20)
                                if (lastMinuteOnly.isNotEmpty()) {
                                    HorizontalProductSection(
                                        titleVectorIcon = Icons.Default.ElectricBolt,
                                        title = "Son Dakika",
                                        subtitle = "Acele edin ve %70'e varan muhteşem indirimlerden yararlanın!",
                                        hotels = lastMinuteOnly,
                                        onHotelClick = { selectedHotelForDetail = it },
                                        onSelectAndBook = { selectedHotelForDetail = it }
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
                                        onSelectAndBook = { selectedHotelForDetail = it }
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
                                            val newBooking = Booking(
                                                id = "BK-WEB-" + (1000..9999).random(),
                                                bookingCode = (if (isFlightBooking) "PNR-" else "WEB-") + (10000..99999).random(),
                                                customerName = guestName,
                                                customerPhone = guestPhone,
                                                productName = if (isFlightBooking) "✈️ Charter Uçuş Bileti: ${hotel.hotelName} (${hotel.flightCode})" else hotel.hotelName,
                                                hotelId = hotel.id,
                                                bookingType = if (isFlightBooking) "FLIGHT" else "HOTEL",
                                                operatorName = option.agencyName,
                                                totalPrice = option.price,
                                                currency = hotel.currency.ifBlank { "TRY" },
                                                status = BookingStatus.ONAYLANDI,
                                                tenantId = targetAgencyId
                                            )
                                            runCatching {
                                                bookingRepository.createBooking(newBooking)
                                            }
                                            bookingSuccessMessage = if (isReferralAgency) {
                                                "✅ Rezervasyon/PNR (${newBooking.bookingCode}) başarıyla oluşturuldu! Acente (${targetAgencyId}) Paneline Aktarıldı."
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
                                                    text = "${option.price.toInt()} ${if (hotel.currency == "RUB") "RUB" else "₺"}",
                                                    style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                                )

                                                Button(
                                                    onClick = {
                                                        val selectedProductEntity = hotel.toUnifiedProductEntity().copy(
                                                            operatorName = option.operatorName,
                                                            price = option.price
                                                        )
                                                        b2bTourSearchViewModel.selectProductForBooking(selectedProductEntity)
                                                        selectedHotelForDetail = null
                                                        selectedAgencyForBooking = null
                                                        onNavigateToNewBooking(hotel.copy(minPrice = option.price, operatorName = option.operatorName))
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
                        text = "Uçuş + Transfer + Otel Dahil",
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
                                        text = "2 Kişi Toplam",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    )
                                    Text(
                                        text = "${hotel.minPrice.toInt()} ${hotel.currency}",
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
                                        text = "Rezerve Et ➔",
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
                            Text("${hotel.minPrice.toInt()} ${if (hotel.currency == "RUB") "RUB" else "₺"}", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${AppLanguageManager.translate("En Yüksek Fiyat")}:", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 10.sp))
                            Text("${hotel.maxPrice.toInt()} ${if (hotel.currency == "RUB") "RUB" else "₺"}", style = TourOSTypography.Caption.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp))
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


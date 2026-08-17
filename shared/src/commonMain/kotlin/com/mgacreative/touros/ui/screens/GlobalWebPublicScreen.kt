package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val agencyPrices: List<AgencyPriceOption> = emptyList()
)

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
    val isFlight = hotel.category == "FLIGHT" || 
                   hotel.hotelName.contains("✈️") || 
                   hotel.hotelName.contains("Uçuş") || 
                   hotel.hotelName.contains("Charter")

    val rawUrl = when {
        isFlight -> {
            if (hotel.imageUrl.isNotBlank() && 
                !hotel.imageUrl.contains("photo-15") && 
                !hotel.imageUrl.contains("photo-16")) {
                hotel.imageUrl
            } else {
                "https://images.unsplash.com/photo-1436491865332-7a61a109cc05"
            }
        }
        hotel.imageUrl.isNotBlank() && !hotel.imageUrl.contains("photo-1566073771259-6a8506099945") -> {
            hotel.imageUrl
        }
        else -> when (hotel.category) {
            "HOTEL" -> "https://images.unsplash.com/photo-1566073771259-6a8506099945"
            "FLIGHT" -> "https://images.unsplash.com/photo-1436491865332-7a61a109cc05"
            "LAST_MINUTE" -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef"
            else -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e" // PACKAGE_TOUR
        }
    }

    return getOptimizedImageUrl(rawUrl, width = 600)
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
        PublicHotelOffer(
            id = "MOD-CORAL-101",
            hotelName = "Nirvana Cosmopolitan Hotel",
            location = "Lara, Antalya, Türkiye",
            stars = 5,
            description = "Starway Award ödüllü lüks tesis. Coral Travel özel fiyat garantili toplu paket.",
            minPrice = 24500.0,
            maxPrice = 28900.0,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            operatorName = "Coral Travel",
            roomType = "Superior Sea View Room",
            mealType = "Ultra Her Şey Dahil",
            flightCode = "AYT - IST (THY 🟢)",
            nights = 7,
            currency = "TRY",
            category = "PACKAGE_TOUR",
            discountPercent = 44,
            ratingScore = 9.4,
            isLastMinute = true,
            agencyPrices = listOf(
                AgencyPriceOption("AGN-CORAL", "Coral Travel B2B Main", "Coral Travel", "Superior Sea View", "Ultra Her Şey Dahil", 24500.0, isBestDeal = true),
                AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Deluxe Room", "Her Şey Dahil", 26800.0),
                AgencyPriceOption("AGN-PEGAS", "Pegas Touristik B2B", "Pegas Touristik", "Standard Room", "Oda Kahvaltı", 27900.0)
            )
        ),
        PublicHotelOffer(
            id = "MOD-ANEX-102",
            hotelName = "Rixos Premium Belek",
            location = "Belek, Antalya, Türkiye",
            stars = 5,
            description = "Anex Tour özel rezervasyonlu 5 yıldızlı lüks plaj tesisi.",
            minPrice = 38900.0,
            maxPrice = 42500.0,
            imageUrl = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
            operatorName = "Anex Tour",
            roomType = "Deluxe Suite Garden View",
            mealType = "All Inclusive Special",
            flightCode = "VKO - AYT (Azur Air 🟢)",
            nights = 7,
            currency = "TRY",
            category = "HOTEL",
            discountPercent = 35,
            ratingScore = 9.6,
            isLastMinute = false,
            agencyPrices = listOf(
                AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Deluxe Suite", "All Inclusive", 38900.0, isBestDeal = true),
                AgencyPriceOption("AGN-CORAL", "Coral Travel B2B", "Coral Travel", "Family Suite", "Ultra Her Şey Dahil", 41200.0)
            )
        ),
        PublicHotelOffer(
            id = "MOD-PEGAS-103",
            hotelName = "✈️ Charter Uçuş Seferi (Pegas Fly DME - AYT)",
            location = "Moskova (DME) ➔ Antalya (AYT)",
            stars = 5,
            description = "Pegas Touristik direkt charter uçuş seferi ve 20kg bagaj dahil.",
            minPrice = 12500.0,
            maxPrice = 14200.0,
            imageUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800",
            operatorName = "Pegas Touristik",
            roomType = "Economy Class Seat",
            mealType = "Uçak İçi İkram Dahil",
            flightCode = "DME - AYT (Pegas Fly 🟢)",
            nights = 0,
            currency = "TRY",
            category = "FLIGHT",
            discountPercent = 50,
            ratingScore = 9.2,
            isLastMinute = true,
            agencyPrices = listOf(
                AgencyPriceOption("AGN-PEGAS", "Pegas Touristik B2B", "Pegas Touristik", "Premium Room", "Golden All Inclusive", 31200.0, isBestDeal = true),
                AgencyPriceOption("AGN-FUNSUN", "Fun & Sun B2B Partner", "Fun & Sun", "Standard Room", "Her Şey Dahil", 33500.0)
            )
        ),
        PublicHotelOffer(
            id = "MOD-FUNSUN-104",
            hotelName = "Lujo Hotel Bodrum",
            location = "Bodrum, Muğla, Türkiye",
            stars = 5,
            description = "Fun & Sun Premium konseptli özel koy ve ultra lüks tatil paketi.",
            minPrice = 49500.0,
            maxPrice = 56000.0,
            imageUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800",
            operatorName = "Fun & Sun",
            roomType = "Indigo Sea View Room",
            mealType = "Luxury A La Carte All Inclusive",
            flightCode = "BJV - IST (AJet 🟢)",
            nights = 5,
            currency = "TRY",
            category = "LAST_MINUTE",
            discountPercent = 60,
            ratingScore = 9.8,
            isLastMinute = true,
            agencyPrices = listOf(
                AgencyPriceOption("AGN-FUNSUN", "Fun & Sun Premium B2B", "Fun & Sun", "Indigo Sea View", "Luxury A La Carte", 49500.0, isBestDeal = true)
            )
        ),
        PublicHotelOffer(
            id = "MOD-CORAL-105",
            hotelName = "Maxx Royal Kemer Resort",
            location = "Kemer, Antalya, Türkiye",
            stars = 5,
            description = "Maxx Inclusive konseptli özel koy, VIP hizmet ve Coral Travel ayrıcalığı.",
            minPrice = 62000.0,
            maxPrice = 75000.0,
            imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
            operatorName = "Coral Travel",
            roomType = "Suite Land View",
            mealType = "Maxx Inclusive",
            flightCode = "AYT - IST (THY VIP 🟢)",
            nights = 7,
            currency = "TRY",
            category = "HOTEL",
            discountPercent = 25,
            ratingScore = 9.9,
            isLastMinute = false,
            agencyPrices = listOf(
                AgencyPriceOption("AGN-CORAL", "Coral Travel B2B Main", "Coral Travel", "Suite Land View", "Maxx Inclusive", 62000.0, isBestDeal = true)
            )
        ),
        PublicHotelOffer(
            id = "MOD-ANEX-106",
            hotelName = "Regnum Carya Golf & Spa Resort",
            location = "Belek, Antalya, Türkiye",
            stars = 5,
            description = "G20 zirvesine ev sahipliği yapan lüks golf oteli ve özel plaj tesisi.",
            minPrice = 54000.0,
            maxPrice = 65000.0,
            imageUrl = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
            operatorName = "Anex Tour",
            roomType = "Jade Room Golf View",
            mealType = "Luxury All Inclusive",
            flightCode = "AYT - SAW (Pegasus 🟢)",
            nights = 7,
            currency = "TRY",
            category = "PACKAGE_TOUR",
            discountPercent = 30,
            ratingScore = 9.5,
            isLastMinute = false,
            agencyPrices = listOf(
                AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Jade Room", "Luxury All Inclusive", 54000.0, isBestDeal = true)
            )
        )
    )
}

@Composable
fun AxiletoLogoText(
    modifier: Modifier = Modifier,
    aFontSize: androidx.compose.ui.unit.TextUnit = 30.sp,
    xiletoFontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    color: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        Text(
            text = "a",
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = aFontSize,
                color = color
            )
        )
        Text(
            text = "xileto",
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = xiletoFontSize,
                color = color
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
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
    var departureCity by remember { mutableStateOf("Moskova (VKO)") }
    var destinationCity by remember { mutableStateOf("Antalya (Lara, Belek, Alanya)") }
    var startDateText by remember { mutableStateOf("20.08.2026") }
    var endDateText by remember { mutableStateOf("28.08.2026") }
    var selectedNightsText by remember { mutableStateOf("7 Gece") }
    var selectedTouristsText by remember { mutableStateOf("2 Yetişkin") }
    var selectedCountryFilter by remember { mutableStateOf("Türkiye") }
    var flightTripType by remember { mutableStateOf("ONE_WAY") } // "ONE_WAY" veya "ROUND_TRIP"

    var showDepartureDropdown by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showNightsDropdown by remember { mutableStateOf(false) }
    var showTouristsDropdown by remember { mutableStateOf(false) }
    var bookingSuccessMessage by remember { mutableStateOf<String?>(null) }

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

                offers.add(
                    PublicHotelOffer(
                        id = p.id,
                        hotelName = p.safeHotelName.ifBlank { p.safeTourName.ifBlank { if (isFlight) "✈️ Charter Uçuş Seferi (${fCode})" else "Tur Operatörü Ürünü" } },
                        location = "${p.safeRegion.ifBlank { p.safeDepartureCity.ifBlank { "Antalya" } }}, Türkiye",
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
                        currency = p.safeCurrency.ifBlank { "RUB" },
                        category = mappedCat,
                        discountPercent = if (isPromo) 35 else null,
                        isLastMinute = isPromo,
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

            offers.add(
                PublicHotelOffer(
                    id = p.id,
                    hotelName = p.safeHotelName.ifBlank { p.safeTourName.ifBlank { "Operatör Ürünü" } },
                    location = "${p.safeRegion.ifBlank { "Antalya" }}, Türkiye",
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
                    currency = p.safeCurrency.ifBlank { "RUB" },
                    agencyPrices = listOf(
                        AgencyPriceOption("AGN-ANEX", "Coral Travel B2B (MGA Partner)", opName, rType, mType, baseP, isBestDeal = true)
                    )
                )
            )
        }

        // 3. Veritabanında veya RAM'de toplu veri yüklemesi bulunmazsa örnek canlı Operatör ürünlerini getir
        if (offers.isEmpty()) {
            offers.addAll(listOf(
                PublicHotelOffer(
                    id = "MOD-CORAL-101",
                    hotelName = "Nirvana Cosmopolitan Hotel",
                    location = "Lara, Antalya, Türkiye",
                    stars = 5,
                    description = "Starway Award ödüllü lüks tesis. Coral Travel özel fiyat garantili toplu paket.",
                    minPrice = 24500.0,
                    maxPrice = 28900.0,
                    imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800",
                    operatorName = "Coral Travel",
                    roomType = "Superior Sea View Room",
                    mealType = "Ultra Her Şey Dahil",
                    flightCode = "AYT - IST (THY 🟢)",
                    nights = 7,
                    currency = "TRY",
                    agencyPrices = listOf(
                        AgencyPriceOption("AGN-CORAL", "Coral Travel B2B Main", "Coral Travel", "Superior Sea View", "Ultra Her Şey Dahil", 24500.0, isBestDeal = true),
                        AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Deluxe Room", "Her Şey Dahil", 26800.0),
                        AgencyPriceOption("AGN-PEGAS", "Pegas Touristik B2B", "Pegas Touristik", "Standard Room", "Oda Kahvaltı", 27900.0)
                    )
                ),
                PublicHotelOffer(
                    id = "MOD-ANEX-102",
                    hotelName = "Rixos Premium Belek",
                    location = "Belek, Antalya, Türkiye",
                    stars = 5,
                    description = "Anex Tour özel rezervasyonlu 5 yıldızlı lüks plaj tesisi.",
                    minPrice = 38900.0,
                    maxPrice = 42500.0,
                    imageUrl = "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800",
                    operatorName = "Anex Tour",
                    roomType = "Deluxe Suite Garden View",
                    mealType = "All Inclusive Special",
                    flightCode = "VKO - AYT (Azur Air 🟢)",
                    nights = 7,
                    currency = "TRY",
                    agencyPrices = listOf(
                        AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Deluxe Suite", "All Inclusive", 38900.0, isBestDeal = true),
                        AgencyPriceOption("AGN-CORAL", "Coral Travel B2B", "Coral Travel", "Family Suite", "Ultra Her Şey Dahil", 41200.0)
                    )
                ),
                PublicHotelOffer(
                    id = "MOD-PEGAS-103",
                    hotelName = "Titanic Mardan Palace",
                    location = "Kundu, Antalya, Türkiye",
                    stars = 5,
                    description = "Pegas Touristik charter uçuş paketli saray mimarili otel.",
                    minPrice = 31200.0,
                    maxPrice = 35000.0,
                    imageUrl = "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800",
                    operatorName = "Pegas Touristik",
                    roomType = "Premium Room Pool View",
                    mealType = "Golden All Inclusive",
                    flightCode = "DME - AYT (Pegas Fly 🟢)",
                    nights = 7,
                    currency = "TRY",
                    agencyPrices = listOf(
                        AgencyPriceOption("AGN-PEGAS", "Pegas Touristik B2B", "Pegas Touristik", "Premium Room", "Golden All Inclusive", 31200.0, isBestDeal = true),
                        AgencyPriceOption("AGN-FUNSUN", "Fun & Sun B2B Partner", "Fun & Sun", "Standard Room", "Her Şey Dahil", 33500.0)
                    )
                ),
                PublicHotelOffer(
                    id = "MOD-FUNSUN-104",
                    hotelName = "Lujo Hotel Bodrum",
                    location = "Bodrum, Muğla, Türkiye",
                    stars = 5,
                    description = "Fun & Sun Premium konseptli özel koy ve ultra lüks tatil paketi.",
                    minPrice = 49500.0,
                    maxPrice = 56000.0,
                    imageUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800",
                    operatorName = "Fun & Sun",
                    roomType = "Indigo Sea View Room",
                    mealType = "Luxury A La Carte All Inclusive",
                    flightCode = "BJV - IST (AJet 🟢)",
                    nights = 5,
                    currency = "TRY",
                    agencyPrices = listOf(
                        AgencyPriceOption("AGN-FUNSUN", "Fun & Sun Premium B2B", "Fun & Sun", "Indigo Sea View", "Luxury A La Carte", 49500.0, isBestDeal = true)
                    )
                )
            ))
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
                // ── 1. ÜST BANT (NAVBAR - KOYU TEAL TEMA / USER FONKSİYONLARI) ─────────
                item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0D5653), // Screenshot ile birebir Koyu Yeşil/Teal Tema
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
                                        }
                                        val currentName = companySettings?.name?.ifBlank { "axileto" } ?: "axileto"
                                        if (!isValidLogo && (currentName.equals("axileto", ignoreCase = true) || currentName.equals("TourOS", ignoreCase = true) || currentName.equals("TourOS Travels", ignoreCase = true))) {
                                            AxiletoLogoText(aFontSize = 26.sp, xiletoFontSize = 18.sp, color = Color.White)
                                        } else {
                                            Text(
                                                text = currentName,
                                                style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    // 🧳 Misafir / 🏢 Acenta Giriş Butonları
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
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
                                                    .background(if (isSelectedMode) Color(0xFF10B981) else Color(0xFF1E293B))
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
                                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                                                .background(Color(0xFFE11D48))
                                                .clickable { onNavigateToAdminCms() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                Text("Admin", style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp))
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
                                                modifier = Modifier.height(32.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    val currentName = companySettings?.name?.ifBlank { "axileto" } ?: "axileto"
                                    if (!isValidLogo && (currentName.equals("axileto", ignoreCase = true) || currentName.equals("TourOS", ignoreCase = true) || currentName.equals("TourOS Travels", ignoreCase = true))) {
                                        AxiletoLogoText(aFontSize = 32.sp, xiletoFontSize = 22.sp, color = Color.White)
                                    } else {
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
                                    // 🌐 Düz Beyaz Font Dil Seçici (TR | EN | RU)
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
                                                .background(Color(0xFFE11D48))
                                                .clickable { onNavigateToAdminCms() }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Text(text = "Admin Paneli", style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                            }
                                        }
                                    }

                                    // 🧳 Misafir / 🏢 Acenta Seçici Segment Butonları
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                            .padding(2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val guestLabel = "🧳 Misafir"
                                        val agencyLabel = if (currentUser != null) "🏢 Acenta Paneli ➔" else "🏢 Acenta"

                                        listOf(guestLabel, agencyLabel).forEach { modeLabel ->
                                            val isGuest = modeLabel.contains("Misafir")
                                            val isSelectedMode = (isGuest && userMode == "Turist") || (!isGuest && userMode == "Acente")
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelectedMode) Color(0xFF10B981) else Color(0xFF1E293B))
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
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = modeLabel,
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
            // ── 2. HERO BANNER GÖRSELİ VE SLOGAN (HAFİF GÖLGE İLE BOYUTLANDIRILMIŞ) ────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .background(Color(0xFF0F172A)),
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

                        // Sol Alt Slogan ve Başlık (Screenshot ile Birebir)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(28.dp)
                                .widthIn(max = 600.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = companySettings?.heroSubtitle?.ifBlank { "Explore the world with confidence and unforgettable experiences." }
                                    ?: "Explore the world with confidence and unforgettable experiences.",
                                style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                            )
                            Text(
                                text = "Dünyanın en seçkin 5 yıldızlı otelleri ve canlı tur paketlerinde canlı karşılaştırmalı fırsatları keşfedin.",
                                style = TourOSTypography.BodyMedium.copy(color = Color(0xFFE2E8F0))
                            )
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
                                        colors = listOf(Color(0xFF0F5A56), Color(0xFF0F172A))
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

                        // ── SAĞ TARAF: DİKEY KOYU TEAL ARAMA MOTORU KUTUSU (DARALTILMIŞ / KOMPAKT & MODERN) ──
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp)),
                            color = Color(0xFF0F5A56), // Koyu Teal
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
                                        .background(Color(0xFF09413E))
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
                                                .background(if (isSelected) Color(0xFF09524F) else Color.Transparent)
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

                                    // 1. Destinasyon (Nereden / Nereye)
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
                                            // Nereden
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showDepartureDropdown = !showDepartureDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Nereden (Kalkış Şehri)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(departureCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showDepartureDropdown,
                                                    onDismissRequest = { showDepartureDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("İstanbul (IST)", "Moskova (VKO)", "Ankara (ESB)", "İzmir (ADB)", "Kazan (KZN)").forEach { city ->
                                                        DropdownMenuItem(
                                                            text = { Text(city, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { departureCity = city; showDepartureDropdown = false }
                                                        )
                                                    }
                                                }
                                            }

                                            // Nereye
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showDestinationDropdown = !showDestinationDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Nereye (Destinasyon / Ülke)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(destinationCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showDestinationDropdown,
                                                    onDismissRequest = { showDestinationDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("Bodrum (Muğla)", "Antalya (AYT)", "Marmaris & Fethiye", "İstanbul (IST)", "Mısır (Şarm El-Şeyh)", "Dubai (BAE)").forEach { destName ->
                                                        DropdownMenuItem(
                                                            text = { Text(destName, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { destinationCity = destName; selectedDestinationFilter = destName; showDestinationDropdown = false }
                                                        )
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
                                                    Text("✓", color = Color(0xFF0F5A56), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
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
                                                    Text("✓", color = Color(0xFF0F5A56), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
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
                                    // 1. DESTİNASYON
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
                                            // Nereden
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showDepartureDropdown = !showDepartureDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Nereden (Kalkış Şehri)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(departureCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showDepartureDropdown,
                                                    onDismissRequest = { showDepartureDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("İstanbul (IST)", "Moskova (VKO)", "Ankara (ESB)", "İzmir (ADB)", "Kazan (KZN)").forEach { city ->
                                                        DropdownMenuItem(
                                                            text = { Text(city, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { departureCity = city; showDepartureDropdown = false }
                                                        )
                                                    }
                                                }
                                            }

                                            // Nereye
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showDestinationDropdown = !showDestinationDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Nereye (Destinasyon / Ülke)"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(destinationCity, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showDestinationDropdown,
                                                    onDismissRequest = { showDestinationDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("Bodrum (Muğla)", "Antalya (Lara, Belek)", "Marmaris & Fethiye", "İstanbul", "Mısır (Şarm El-Şeyh)", "Dubai (BAE)").forEach { destName ->
                                                        DropdownMenuItem(
                                                            text = { Text(destName, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { destinationCity = destName; selectedDestinationFilter = destName; showDestinationDropdown = false }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. TARİH (MODERN MATERIAL DATE PICKER)
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
                                            // Gidiş Başlangıç
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showStartDatePicker = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Gidiş Başlangıç"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(startDateText, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("📅", fontSize = 10.sp)
                                                    }
                                                }
                                            }

                                            // Gidiş Bitiş
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showEndDatePicker = true }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Gidiş Bitiş"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(endDateText, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
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
                                                    listOf("3 Gece", "5 Gece", "7 Gece", "10 Gece", "14 Gece").forEach { nightOpt ->
                                                        DropdownMenuItem(
                                                            text = { Text(AppLanguageManager.translate(nightOpt), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { selectedNightsText = nightOpt; showNightsDropdown = false }
                                                        )
                                                    }
                                                }
                                            }

                                            // Turist Sayısı
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                    .clickable { showTouristsDropdown = !showTouristsDropdown }
                                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Column {
                                                    Text(AppLanguageManager.translate("Turist Sayısı"), style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(AppLanguageManager.translate(selectedTouristsText), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 11.sp), maxLines = 1)
                                                        Text("▼", fontSize = 8.sp, color = Color(0xFF64748B))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showTouristsDropdown,
                                                    onDismissRequest = { showTouristsDropdown = false },
                                                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                                                ) {
                                                    listOf("1 Yetişkin", "2 Yetişkin", "3 Yetişkin", "2 Yetişkin + 1 Çocuk", "2 Yetişkin + 2 Çocuk").forEach { countText ->
                                                        DropdownMenuItem(
                                                            text = { Text(AppLanguageManager.translate(countText), style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Medium, fontSize = 12.sp)) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                            modifier = Modifier.height(34.dp),
                                                            onClick = { selectedTouristsText = countText; showTouristsDropdown = false }
                                                        )
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
                                            val targetNights = selectedNightsText.substringBefore(" ").toIntOrNull() ?: 7

                                            // 1. Aşama: Tam Eşleşme
                                            var matches = dbProducts.filter { h ->
                                                val isPureFlight = h.category.uppercase() == "FLIGHT" || h.hotelName.startsWith("Uçuş:", ignoreCase = true) || h.hotelName.startsWith("✈️", ignoreCase = true)
                                                if (selectedSearchCategoryTab == "FLIGHT" && !isPureFlight) return@filter false
                                                if (selectedSearchCategoryTab != "FLIGHT" && isPureFlight) return@filter false

                                                val matchesDest = destClean.isBlank() || destClean.startsWith("Tüm", ignoreCase = true) || destClean.startsWith("Nereye", ignoreCase = true) ||
                                                    h.location.contains(destClean, ignoreCase = true) ||
                                                    h.hotelName.contains(destClean, ignoreCase = true)

                                                val matchesNights = (h.nights == targetNights)
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

                                            searchResultsList = matches.ifEmpty { if (selectedSearchCategoryTab == "FLIGHT") pureFlightFallback else nonFlightFallback }
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
                                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
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
                                            text = "📍 Destinasyon: $selectedDestinationFilter  |  🔍 Otel/Tur: ${searchQuery.ifBlank { "Tüm Turlar" }}",
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
                            // ── BLOK 1: 🏖️ PAKET TURLAR (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ─────
                            val tourPackagesOnly = dbProducts.shuffled().filter { 
                                it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true)
                            }.take(20)
                            if (tourPackagesOnly.isNotEmpty()) {
                                HorizontalProductSection(
                                    titleVectorIcon = Icons.Default.BeachAccess,
                                    title = "Paket Turlar",
                                    subtitle = "Gezginler Tarafından Onaylanmış Her Şey Dahil Paket Turlar",
                                    hotels = tourPackagesOnly,
                                    onHotelClick = { selectedHotelForDetail = it },
                                    onSelectAndBook = { selectedHotelForDetail = it }
                                )
                            }

                            // ── BLOK 2: OTELLER (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ──────────
                            val hotelsOnly = dbProducts.shuffled().filter { 
                                it.category != "FLIGHT" && !it.hotelName.startsWith("Uçuş:", ignoreCase = true) && !it.hotelName.startsWith("✈️", ignoreCase = true) && (it.category == "HOTEL" || it.stars >= 4)
                            }.ifEmpty { tourPackagesOnly }.take(20)
                            if (hotelsOnly.isNotEmpty()) {
                                HorizontalProductSection(
                                    titleVectorIcon = Icons.Default.Hotel,
                                    title = "Oteller",
                                    subtitle = "Ayrıcalıklı konaklama ve seçkin 5 yıldızlı oteller",
                                    hotels = hotelsOnly,
                                    onHotelClick = { selectedHotelForDetail = it },
                                    onSelectAndBook = { selectedHotelForDetail = it }
                                )
                            }

                            // ── BLOK 3: SON DAKİKA (UÇUŞLAR HARİÇ - ANA SAYFA SABİT BLOK) ───────
                            val lastMinuteOnly = dbProducts.filter { 
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
                                    onSelectAndBook = { selectedHotelForDetail = it }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { showAgencyLoginModal = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(460.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(enabled = false) {},
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { selectedAgencyForBooking = null },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(550.dp)
                            .wrapContentHeight()
                            .padding(20.dp)
                            .clickable(enabled = false) {},
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
                // ── DETAY & ACENTE FİYAT KARŞILAŞTIRMA MODALI (Metasearch Engine) ────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { 
                            selectedHotelForDetail = null
                            selectedAgencyForBooking = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(720.dp)
                            .heightIn(max = 650.dp)
                            .padding(20.dp)
                            .clickable(enabled = false) {},
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hotel.hotelName, style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold))
                                    Text("📍 ${hotel.location} • ID: ${hotel.id}", style = TourOSTypography.Caption.copy(color = Color(0xFF64748B)))
                                }
                                Text(
                                    text = "✖",
                                    modifier = Modifier.clickable { 
                                        selectedHotelForDetail = null
                                        selectedAgencyForBooking = null
                                    },
                                    style = TourOSTypography.TitleLarge.copy(color = Color(0xFF94A3B8))
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            Text(
                                text = "🏬 Hangi Acente / Operatör Kaç Satıyor?",
                                style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            )

                            // Acente Fiyat Döküm Listesi (Canlı Operatör Karşılaştırma)
                            val effectiveAgencyPrices = if (hotel.agencyPrices.isNotEmpty()) hotel.agencyPrices else listOf(
                                AgencyPriceOption("AGN-CORAL", "${hotel.operatorName.ifBlank { "Coral Travel" }} B2B Main", hotel.operatorName.ifBlank { "Coral Travel" }, hotel.roomType, hotel.mealType, hotel.minPrice, isBestDeal = true),
                                AgencyPriceOption("AGN-ANEX", "Anex Tour B2B Partner", "Anex Tour", "Deluxe Room", "Her Şey Dahil", hotel.minPrice * 1.08),
                                AgencyPriceOption("AGN-PEGAS", "Pegas Touristik Agency", "Pegas Touristik", "Standard Room", "Oda Kahvaltı", hotel.minPrice * 1.15)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                effectiveAgencyPrices.forEach { option ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (option.isBestDeal) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (option.isBestDeal) Color(0xFF22C55E) else Color(0xFFE2E8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "🏢 ${option.agencyName}",
                                                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                                    )
                                                    if (option.isBestDeal) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(Color(0xFF22C55E))
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(AppLanguageManager.translate("En İyi Fiyat ⭐"), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${AppLanguageManager.translate("Operatör")}: ${option.operatorName} • ${AppLanguageManager.translate("Oda/Hizmet")}: ${option.roomType} (${option.boardType})",
                                                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B))
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(
                                                    text = "${option.price.toInt()} ${if (hotel.currency == "RUB") "RUB" else "₺"}",
                                                    style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                                )

                                                TourOSButton(
                                                    text = AppLanguageManager.translate("Rezerve Et ➔"),
                                                    onClick = {
                                                        b2bTourSearchViewModel.selectProductForBooking(
                                                            hotel.toUnifiedProductEntity().copy(
                                                                operatorName = option.operatorName,
                                                                price = option.price
                                                            )
                                                        )
                                                        onNavigateToNewBooking(hotel.copy(minPrice = option.price, operatorName = option.operatorName))
                                                        selectedHotelForDetail = null
                                                        selectedAgencyForBooking = null
                                                    },
                                                    variant = TourOSButtonVariant.PRIMARY
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

// ── ⚡ DİKEY AKICI RESPONSIVE ARAMA SONUÇLARI İZGARA (GRID) SEKSİYONU ──────────
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
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (titleIcon.isNotBlank()) {
                        Text(titleIcon, fontSize = 28.sp)
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
            }

            // Dikey Responsive Grid (Masaüstü 3'lü, Tablet 2'li, Mobil 1'li)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = when {
                    maxWidth >= 1050.dp -> 3
                    maxWidth >= 680.dp -> 2
                    else -> 1
                }
                val chunkedHotels = remember(hotels, columns) { hotels.chunked(columns) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    chunkedHotels.forEach { rowHotels ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            rowHotels.forEach { hotel ->
                                Box(modifier = Modifier.weight(1f)) {
                                    HorizontalHotelCard(
                                        hotel = hotel,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onHotelClick(hotel) },
                                        onSelectAndBook = onSelectAndBook
                                    )
                                }
                            }
                            repeat(columns - rowHotels.size) {
                                Spacer(modifier = Modifier.weight(1f))
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

                // Lokasyon & Tur / Uçuş Bilgisi
                Text(
                    text = if (isFlightCard) AppLanguageManager.translate("Moskova Kalkışlı · Ekonomi Sınıfı · Gidiş-Dönüş") else "Moskova'dan ${hotel.location} · ${hotel.nights} ${AppLanguageManager.translate("Gece")} · ${hotel.mealType}",
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp),
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
                                text = if (isFlightCard) AppLanguageManager.translate("Uçuş Seç") else AppLanguageManager.translate("Turu Seç & Detaylandır"),
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
                // Header (Başlık & Açıklama)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Hizmetlerimizi Keşfedin / Our Services",
                        style = TourOSTypography.TitleLarge.copy(
                            color = Color(0xFF0D5653),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        )
                    )
                    Text(
                        text = "Zengin destinasyon ağımız, lüks otel rezervasyonlarımız, uygun fiyatlı charter uçuşlarımız ve 7/24 acente desteğimiz ile kusursuz seyahat çözümleri sunuyoruz.",
                        style = TourOSTypography.BodyMedium.copy(
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.widthIn(max = 850.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
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
    var selectedDay by remember { mutableStateOf(initialDateText.split(".").firstOrNull()?.toIntOrNull() ?: 20) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(320.dp)
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
                        text = "${selectedDay.toString().padStart(2, '0')} Ağustos 2026",
                        style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Month & Year Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ağustos 2026",
                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◀", fontSize = 10.sp, color = Color(0xFF475569))
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", fontSize = 10.sp, color = Color(0xFF475569))
                        }
                    }
                }

                // Day Names Header Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Pz", "Sa", "Ça", "Pe", "Cu", "Ct", "Pa").forEach { dayName ->
                        Text(
                            text = dayName,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            modifier = Modifier.width(36.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Calendar Days Grid (31 Days)
                val daysList = (1..31).toList()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    daysList.chunked(7).forEach { weekRow ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            weekRow.forEach { dayNum ->
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
                            val formattedDate = "${selectedDay.toString().padStart(2, '0')}.08.2026"
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

package com.mgacreative.touros.ui.screens.members

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import org.jetbrains.compose.resources.painterResource
import touros.shared.generated.resources.Res
import touros.shared.generated.resources.club_badge
import touros.shared.generated.resources.club_banner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.AuthRepository
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.LanguageSelector
import com.mgacreative.touros.ui.components.AppLanguage
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import com.mgacreative.touros.utils.rememberFilePickerLauncher
import com.mgacreative.touros.utils.MAX_IMAGE_SIZE_BYTES
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.koin.compose.koinInject

// ─────────────────────────────────────────────────────────────────────────────
// DTO MODELLERİ (YANDEX POSTGRESQL CANLI VERİLERİ)
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MemberBookingRpcDto(
    val id: String = "",
    val booking_code: String = "",
    val customer_name: String = "",
    val customer_email: String? = null,
    val customer_phone: String? = null,
    val hotel_name: String? = null,
    val destination: String? = null,
    val image_url: String? = null,
    val total_price: Double = 0.0,
    val currency: String = "RUB",
    val status: String = "Bekliyor",
    val operator_name: String? = "Axileto Partner",
    val check_in_date: String? = null,
    val check_out_date: String? = null,
    val room_type_name: String? = null,
    val nights: Int = 7,
    val pax_count: Int = 2
)

@Serializable
data class MemberProfileRpcDto(
    val avatar_url: String = "",
    val full_name: String = "",
    val phone: String = "",
    val passport_no: String = "",
    val city: String = "",
    val budget_range: String = "150.000 - 250.000 RUB",
    val travel_group: String = "Aile (Çocuklu)",
    val holiday_concepts: List<String> = emptyList(),
    val transport_preferences: List<String> = emptyList(),
    val favorite_destinations: List<String> = emptyList(),
    val special_requests: List<String> = emptyList()
)

@Serializable
data class ClubVipSettingsSummaryDto(
    val silver_points_min: Int = 0,
    val gold_points_min: Int = 2000,
    val platinum_points_min: Int = 5000,
    val hero_title: String = "Yaza Özel Fırsatlar",
    val hero_subtitle: String = "Erken rezervasyon fırsatlarını kaçırma! Axileto Club üyelerine özel ek indirimler.",
    val hero_image_url: String = "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80",
    val hero_button_text: String = "Teklifleri Keşfet"
)

@Serializable
data class VipDashboardRpcSummary(
    val total_bookings: Int = 3,
    val pending_bookings: Int = 1,
    val completed_bookings: Int = 2,
    val total_spent: Double = 1278000.0,
    val points: Int = 3250,
    val tier_code: String = "GOLD",
    val tier_name: String = "Gold Üye",
    val next_tier_points: Int = 5000,
    val upcoming_bookings: List<MemberBookingRpcDto> = emptyList(),
    val settings: ClubVipSettingsSummaryDto? = null,
    val member_profile: MemberProfileRpcDto? = null
)

fun getDefaultSampleBookings(): List<MemberBookingRpcDto> = emptyList()

data class ClubTierCardData(
    val code: String,
    val nameKey: String,
    val badge: String,
    val minPoints: Int,
    val earnRateTextKey: String,
    val perks: List<String>,
    val bgGradient: List<Color>,
    val borderColor: Color,
    val accentColor: Color
)

fun getClubTierCards(): List<ClubTierCardData> = listOf(
    ClubTierCardData(
        code = "SILVER",
        nameKey = "Silver Üye",
        badge = "🥈",
        minPoints = 0,
        earnRateTextKey = "%1 Puan Kazanımı",
        perks = listOf(
            "Hoşgeldin 250 Axileto Puanı",
            "Standart İndirimli Özel Teklifler",
            "Online Hızlı Destek"
        ),
        bgGradient = listOf(Color(0xFF1E293B), Color(0xFF334155)),
        borderColor = Color(0xFF94A3B8),
        accentColor = Color(0xFFCBD5E1)
    ),
    ClubTierCardData(
        code = "GOLD",
        nameKey = "Gold Üye",
        badge = "👑",
        minPoints = 2000,
        earnRateTextKey = "%2 Puan Kazanımı + Öncelikli Rezervasyon",
        perks = listOf(
            "Öncelikli Rezervasyon & Onay",
            "Seçili Otellerde Ücretsiz Oda Yükseltme",
            "Acente VIP Çağrı Hattı",
            "Özel Dönemsel İndirim Kuponları"
        ),
        bgGradient = listOf(Color(0xFF451A03), Color(0xFF78350F)),
        borderColor = Color(0xFFF59E0B),
        accentColor = Color(0xFFFBBF24)
    ),
    ClubTierCardData(
        code = "PLATINUM",
        nameKey = "Platinum VIP",
        badge = "💎",
        minPoints = 5000,
        earnRateTextKey = "%3 Puan Kazanımı + Full VIP Concierge",
        perks = listOf(
            "Kişisel VIP Seyahat Danışmanı",
            "Havalimanı Fast-Track & Lounge Erişimi",
            "Erken Giriş & Geç Çıkış Garantisi",
            "Ücretsiz VIP Özel Havalimanı Transferi",
            "Sürpriz Karşılama İkramları"
        ),
        bgGradient = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)),
        borderColor = Color(0xFF38BDF8),
        accentColor = Color(0xFF38BDF8)
    )
)

fun resolveMemberTierName(tierCode: String, fallbackTierName: String): String {
    val tier = getClubTierCards().firstOrNull { it.code.equals(tierCode, ignoreCase = true) }
    return if (tier != null) {
        AppLanguageManager.translate(tier.nameKey)
    } else {
        AppLanguageManager.translate(fallbackTierName)
    }
}

@Serializable
data class MarketplaceProductRpcDto(
    val id: String = "",
    val hotel_name: String = "",
    val tour_name: String = "",
    val operator_name: String = "",
    val region: String = "",
    val country: String = "Турция",
    val room_type: String = "",
    val meal_type: String = "",
    val nights: Int = 7,
    val adults: Int = 2,
    val price: Double = 0.0,
    val currency: String = "RUB",
    val image_url: String = "",
    val stars: Int = 5
)

@Serializable
data class ClubAgencyOfferDto(
    val id: String? = null,
    val agency_id: String? = null,
    val agency_name: String? = null,
    val hotel_name: String? = null,
    val stars: Int? = 5,
    val operator_badge: String? = null,
    val flight_badge: String? = null,
    val location_text: String? = null,
    val nights_text: String? = null,
    val meal_text: String? = null,
    val lowest_price: String? = null,
    val highest_price: String? = null,
    val award_badge: String? = null,
    val discount_percent: Int? = 25,
    val rating_score: Double? = 4.8,
    val review_count: Int? = 120,
    val image_url: String? = null,
    val is_active: Boolean? = true
)

// ─────────────────────────────────────────────────────────────────────────────
// DİNAMİK 3 DİL SÖZLÜĞÜ (TR - EN - RU)
// ─────────────────────────────────────────────────────────────────────────────

object ClubStrings {
    fun t(key: String, lang: String): String {
        return AppLanguageManager.translate(key, lang)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KART & BLOK MODELLERİ
// ─────────────────────────────────────────────────────────────────────────────

data class AgencyBlockOfferItem(
    val id: String,
    val hotelName: String,
    val stars: Int = 4,
    val operatorBadge: String = "4 Operatör Teklifi",
    val flightBadge: String = "VKO - AYT (Ekonomi 🧳)",
    val locationText: String = "Türkiye • Kemer",
    val nightsText: String = "7 Gece",
    val mealText: String = "Bez pitaniya",
    val lowestPrice: String = "211.499 RUB",
    val highestPrice: String = "228.738 RUB",
    val awardBadge: String = "Starway Award",
    val discountPercent: Int? = 25,
    val ratingScore: Double = 4.8,
    val reviewCount: Int = 120,
    val imageUrl: String = "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=60"
)

data class AgencyOfferBlock(
    val agencyId: String,
    val agencyName: String,
    val agencyLogoColor: Color = Color(0xFF0284C7),
    val offers: List<AgencyBlockOfferItem>
)

data class MemberModel(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val isAgency: Boolean = false,
    val agencyName: String = "",
    val avatarInitials: String = "VIP",
    val avatarUrl: String? = null
)

enum class ClubStage {
    AUTH,
    DASHBOARD
}

enum class ClubNavTab {
    DASHBOARD,
    MY_TRIPS,
    SPECIAL_OFFERS,
    HISTORY,
    LOYALTY_POINTS,
    PROFILE,
    SUPPORT
}

// ─────────────────────────────────────────────────────────────────────────────
// ANA EKRAN BİLEŞENİ
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AxiletoMembersPortalScreen(
    onNavigateBackToMainApp: (() -> Unit)? = null,
    onNavigateToSearch: ((String) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepository: AuthRepository = koinInject()
    val supabaseClient: SupabaseClient = koinInject()

    val currentLangState by AppLanguageManager.currentLanguage.collectAsState()
    val selectedLang = currentLangState.code.lowercase()

    val currentUser by authRepository.observeAuthState().collectAsState()
    var stage by remember { mutableStateOf(if (currentUser != null) ClubStage.DASHBOARD else ClubStage.AUTH) }

    var currentMember by remember(currentUser) {
        mutableStateOf(
            MemberModel(
                fullName = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: (currentUser?.email?.substringBefore("@") ?: ""),
                email = currentUser?.email ?: "",
                phone = "",
                isAgency = currentUser?.role == UserRole.AGENT || currentUser?.role == UserRole.TOUR_OPERATOR || currentUser?.role == UserRole.SYSTEM_ADMIN,
                agencyName = "",
                avatarInitials = (currentUser?.fullName?.takeIf { it.isNotBlank() }?.take(2) ?: currentUser?.email?.take(2) ?: "VIP").uppercase(),
                avatarUrl = currentUser?.avatarUrl ?: ""
            )
        )
    }

    // ── ACENTELERİN 10'AR ADETLİK TEKLİF BLOKLARI ──
    var agencyBlocks by remember {
        mutableStateOf(emptyList<AgencyOfferBlock>())
    }

    val defaultBookings = remember { getDefaultSampleBookings() }
    var vipSummary by remember { mutableStateOf(VipDashboardRpcSummary(upcoming_bookings = defaultBookings)) }
    var liveBookingsList by remember { mutableStateOf<List<MemberBookingRpcDto>>(defaultBookings) }
    var isOfferModalOpen by remember { mutableStateOf(false) }

    fun fetchAgencyOffers() {
        coroutineScope.launch {
            try {
                val list = supabaseClient.postgrest.from("club_agency_offers")
                    .select {
                        filter {
                            eq("is_active", true)
                        }
                    }
                    .decodeList<ClubAgencyOfferDto>()

                val grouped = list.groupBy { it.agency_name ?: "Yetkili Acente" }
                val blocks = grouped.map { (agnName, offersList) ->
                    AgencyOfferBlock(
                        agencyId = offersList.firstOrNull()?.agency_id ?: "agn-1",
                        agencyName = agnName,
                        offers = offersList.map { dto ->
                            AgencyBlockOfferItem(
                                id = dto.id ?: "",
                                hotelName = dto.hotel_name ?: "",
                                stars = dto.stars ?: 5,
                                operatorBadge = dto.operator_badge ?: "VIP Özel Teklif",
                                flightBadge = dto.flight_badge ?: "Charter & Lounge 🧳",
                                locationText = dto.location_text ?: "Türkiye • Antalya",
                                nightsText = dto.nights_text ?: "7 Gece",
                                mealText = dto.meal_text ?: "Ultra Her Şey Dahil",
                                lowestPrice = dto.lowest_price ?: "₺35.000",
                                highestPrice = dto.highest_price ?: "₺42.000",
                                awardBadge = dto.award_badge ?: "Starway Award",
                                discountPercent = dto.discount_percent ?: 25,
                                ratingScore = dto.rating_score ?: 4.8,
                                reviewCount = dto.review_count ?: 120,
                                imageUrl = dto.image_url ?: "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=60"
                            )
                        }
                    )
                }

                if (currentMember.isAgency && currentMember.agencyName.isNotBlank() && blocks.none { it.agencyName.equals(currentMember.agencyName, ignoreCase = true) }) {
                    agencyBlocks = listOf(
                        AgencyOfferBlock(
                            agencyId = "agn-${currentMember.agencyName.hashCode()}",
                            agencyName = currentMember.agencyName,
                            offers = emptyList()
                        )
                    ) + blocks
                } else {
                    agencyBlocks = blocks
                }
            } catch (e: Exception) {
                println("⚠️ fetchAgencyOffers: ${e.message}")
            }
        }
    }

    fun fetchVipData(email: String, phone: String) {
        coroutineScope.launch {
            try {
                val params = buildJsonObject {
                    put("p_email", email.trim())
                    put("p_phone", phone.trim())
                }
                val summary = supabaseClient.postgrest.rpc("get_member_vip_dashboard_summary", params).decodeAs<VipDashboardRpcSummary>()
                vipSummary = summary
                liveBookingsList = summary.upcoming_bookings
                summary.member_profile?.let { prof ->
                    currentMember = currentMember.copy(
                        avatarUrl = prof.avatar_url.ifBlank { currentMember.avatarUrl },
                        fullName = prof.full_name.ifBlank { currentMember.fullName },
                        phone = prof.phone.ifBlank { currentMember.phone }
                    )
                }
            } catch (e: Exception) {
                println("⚠️ get_member_vip_dashboard_summary: ${e.message}")
            }
        }
    }

    LaunchedEffect(currentMember.email, currentMember.phone) {
        if (currentMember.email.isNotBlank() || currentMember.phone.isNotBlank()) {
            fetchVipData(currentMember.email, currentMember.phone)
        }
    }

    LaunchedEffect(stage, currentMember.agencyName) {
        fetchAgencyOffers()
    }

    when (stage) {
        ClubStage.AUTH -> {
            AxiletoClubAuthScreen(
                selectedLang = selectedLang,
                onLoginSuccess = { email, phone, isAgency, name ->
                    val finalAgencyName = if (isAgency) name.ifBlank { "Yetkili Acente" } else ""
                    currentMember = currentMember.copy(
                        email = email,
                        phone = phone,
                        isAgency = isAgency,
                        agencyName = finalAgencyName,
                        fullName = if (isAgency) finalAgencyName else name,
                        avatarInitials = name.take(2).uppercase().ifBlank { "VIP" }
                    )
                    if (isAgency) {
                        fetchAgencyOffers()
                    } else {
                        fetchVipData(email, phone)
                    }
                    stage = ClubStage.DASHBOARD
                },
                onBackClick = onNavigateBackToMainApp
            )
        }
        ClubStage.DASHBOARD -> {
            var quotaAlertMessage by remember { mutableStateOf<String?>(null) }

            AxiletoClubVipDashboardScreen(
                selectedLang = selectedLang,
                member = currentMember,
                vipSummary = vipSummary,
                agencyBlocks = agencyBlocks,
                liveBookings = liveBookingsList,
                supabaseClient = supabaseClient,
                onAvatarUpdated = { newUrl ->
                    currentMember = currentMember.copy(avatarUrl = newUrl)
                },
                onBookingCreated = { newBooking ->
                    liveBookingsList = listOf(newBooking) + liveBookingsList
                },
                onDeleteOffer = { agencyId, offerId ->
                    coroutineScope.launch {
                        try {
                            val params = buildJsonObject {
                                put("p_offer_id", offerId)
                            }
                            supabaseClient.postgrest.rpc("delete_club_agency_offer", params)
                        } catch (e: Exception) {
                            println("⚠️ delete_club_agency_offer: ${e.message}")
                        }
                        fetchAgencyOffers()
                    }
                },
                onOpenOfferModal = {
                    val myBlock = agencyBlocks.find { it.agencyName.equals(currentMember.agencyName, ignoreCase = true) }
                    val currentCount = myBlock?.offers?.size ?: 0
                    if (currentCount >= 10) {
                        quotaAlertMessage = AppLanguageManager.translate("10 teklif sınırına ulaştınız! Yeni bir teklif eklemek için lütfen süresi dolan veya eski bir teklifinizi siliniz.")
                    } else {
                        isOfferModalOpen = true
                    }
                },
                onLogout = {
                    coroutineScope.launch {
                        try {
                            authRepository.signOut()
                        } catch (e: Exception) {
                            println("⚠️ Logout: ${e.message}")
                        }
                        currentMember = MemberModel(fullName = "", email = "", phone = "", isAgency = false, agencyName = "", avatarInitials = "VIP")
                        vipSummary = VipDashboardRpcSummary(upcoming_bookings = emptyList())
                        liveBookingsList = emptyList()
                        stage = ClubStage.AUTH
                    }
                },
                onNavigateBackToMainApp = onNavigateBackToMainApp,
                onNavigateToSearch = onNavigateToSearch
            )

            if (quotaAlertMessage != null) {
                AlertDialog(
                    onDismissRequest = { quotaAlertMessage = null },
                    title = { Text(AppLanguageManager.translate("10 Teklif Limiti Doldu"), fontWeight = FontWeight.Bold, color = TourOSColors.Primary) },
                    text = { Text(quotaAlertMessage ?: "") },
                    confirmButton = {
                        TourOSButton(text = AppLanguageManager.translate("Tamam"), onClick = { quotaAlertMessage = null }, variant = TourOSButtonVariant.PRIMARY)
                    }
                )
            }

            // Acentenin Büyük Veri Havuzundan Otel/Tur Seçtiği Canlı Arama Modalı
            if (isOfferModalOpen && currentMember.isAgency) {
                AddOfferFromBigDataDialog(
                    selectedLang = selectedLang,
                    supabaseClient = supabaseClient,
                    agencyName = currentMember.agencyName,
                    onDismiss = { isOfferModalOpen = false },
                    onPublishOffer = { newOfferItem ->
                        coroutineScope.launch {
                            try {
                                val params = buildJsonObject {
                                    put("p_agency_id", "agn-${currentMember.agencyName.hashCode()}")
                                    put("p_agency_name", currentMember.agencyName.ifBlank { "Yetkili Acente" })
                                    put("p_hotel_name", newOfferItem.hotelName)
                                    put("p_stars", newOfferItem.stars)
                                    put("p_operator_badge", newOfferItem.operatorBadge)
                                    put("p_flight_badge", newOfferItem.flightBadge)
                                    put("p_location_text", newOfferItem.locationText)
                                    put("p_nights_text", newOfferItem.nightsText)
                                    put("p_meal_text", newOfferItem.mealText)
                                    put("p_lowest_price", newOfferItem.lowestPrice)
                                    put("p_highest_price", newOfferItem.highestPrice)
                                    put("p_image_url", newOfferItem.imageUrl)
                                }
                                supabaseClient.postgrest.rpc("save_club_agency_offer", params)
                            } catch (e: Exception) {
                                println("⚠️ save_club_agency_offer: ${e.message}")
                            }
                            fetchAgencyOffers()
                        }
                        isOfferModalOpen = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. MODERN AXILETO CLUB VIP DASHBOARD (KOMPAKT ACENTE BLOKLARI & FİLTRELEME)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AxiletoClubVipDashboardScreen(
    selectedLang: String,
    member: MemberModel,
    vipSummary: VipDashboardRpcSummary,
    agencyBlocks: List<AgencyOfferBlock>,
    liveBookings: List<MemberBookingRpcDto>,
    supabaseClient: SupabaseClient,
    onAvatarUpdated: (String) -> Unit,
    onBookingCreated: (MemberBookingRpcDto) -> Unit,
    onDeleteOffer: (agencyId: String, offerId: String) -> Unit,
    onOpenOfferModal: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBackToMainApp: (() -> Unit)? = null,
    onNavigateToSearch: ((String) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(ClubNavTab.DASHBOARD) }
    var selectedAgencyFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var bookedOfferNotice by remember { mutableStateOf<String?>(null) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    val avatarPickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
        if (bytes.size > MAX_IMAGE_SIZE_BYTES) {
            bookedOfferNotice = AppLanguageManager.translate("Fotoğraf boyutu 1 MB'dan küçük olmalıdır!", selectedLang)
            return@rememberFilePickerLauncher
        }
        coroutineScope.launch {
            isUploadingAvatar = true
            try {
                val cleanEmail = member.email.replace("@", "_").replace(".", "_")
                val cleanName = "avatar_${cleanEmail}_${kotlin.random.Random.nextInt(1000, 9999)}.jpg"
                val bucket = supabaseClient.storage.from("avatars")
                bucket.upload(cleanName, bytes) {
                    upsert = true
                }
                val publicUrl = bucket.publicUrl(cleanName)
                onAvatarUpdated(publicUrl)
                
                // PostgreSQL Veritabanında Kalıcı Saklama
                val avatarPayload = buildJsonObject {
                    put("p_email", member.email.trim())
                    put("p_avatar_url", publicUrl)
                }
                try {
                    supabaseClient.postgrest.rpc("save_member_avatar", avatarPayload)
                } catch (dbEx: Exception) {
                    println("⚠️ save_member_avatar sync: ${dbEx.message}")
                }

                bookedOfferNotice = AppLanguageManager.translate("Profil fotoğrafınız başarıyla güncellendi ✓", selectedLang)
            } catch (e: Exception) {
                println("⚠️ Avatar upload error: ${e.message}")
            } finally {
                isUploadingAvatar = false
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val isMobile = maxWidth < 900.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // ── 1. SOL VIP SIDEBAR (Masaüstü) ────────────────────────────────
            if (!isMobile) {
                Surface(
                    modifier = Modifier.width(260.dp).fillMaxHeight(),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Üst: 👑 Axileto Club Logosu (Sol Üst - Büyük, Net & Vurgulu)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .padding(vertical = 2.dp)
                                    .clickable { currentTab = ClubNavTab.DASHBOARD },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.club_badge),
                                    contentDescription = "Axileto Club",
                                    modifier = Modifier.size(215.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Navigasyon Menü Linkleri
                            val navItems = listOf(
                                Triple(ClubNavTab.DASHBOARD, "Ana Sayfa", Icons.Default.Home),
                                Triple(ClubNavTab.MY_TRIPS, "Rezervasyonlarım", Icons.Default.Luggage),
                                Triple(ClubNavTab.SPECIAL_OFFERS, "Özel Teklifler", Icons.Default.CardGiftcard),
                                Triple(ClubNavTab.HISTORY, "Seyahat Geçmişim", Icons.Default.History),
                                Triple(ClubNavTab.LOYALTY_POINTS, "Puan & Avantajlar", Icons.Default.Star),
                                Triple(ClubNavTab.PROFILE, "Profil & Ayarlar", Icons.Default.Person),
                                Triple(ClubNavTab.SUPPORT, "Destek & Yardım", Icons.Default.HelpOutline)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                navItems.forEach { (tab, labelKey, icon) ->
                                    val isSelected = currentTab == tab
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { currentTab = tab },
                                        color = if (isSelected) Color(0xFFEFF6FF) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = AppLanguageManager.translate(labelKey),
                                                style = TourOSTypography.BodyMedium.copy(
                                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF334155),
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // 🚪 Çıkış Yap Butonu (Sol Menü Altı)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onLogout() },
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Çıkış Yap",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = AppLanguageManager.translate("Çıkış Yap", selectedLang),
                                            style = TourOSTypography.BodyMedium.copy(
                                                color = Color(0xFFDC2626),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Alt: VIP Sadakat Seviyesi Motive Edici Simgesel Kart
                        val currentTierCard = getClubTierCards().firstOrNull { it.code.equals(vipSummary.tier_code, ignoreCase = true) }
                            ?: getClubTierCards().first()

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            color = Color.Transparent,
                            border = BorderStroke(1.5.dp, currentTierCard.borderColor),
                            shadowElevation = 3.dp,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(currentTierCard.bgGradient)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = currentTierCard.badge, fontSize = 16.sp)
                                            Text(
                                                text = AppLanguageManager.translate(currentTierCard.nameKey),
                                                style = TourOSTypography.TitleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = currentTierCard.accentColor,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.35f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "VIP CLUB",
                                                style = TourOSTypography.Caption.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "${vipSummary.points} " + AppLanguageManager.translate("Puan"),
                                            style = TourOSTypography.TitleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                        )
                                        Text(
                                            text = AppLanguageManager.translate("Sonraki seviye:") + " ${vipSummary.next_tier_points}",
                                            style = TourOSTypography.Caption.copy(
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 9.sp
                                            )
                                        )
                                    }

                                    val progress = (vipSummary.points.toFloat() / vipSummary.next_tier_points.toFloat()).coerceIn(0.05f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = currentTierCard.accentColor,
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. SAĞ İÇERİK BÖLÜMÜ ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Üst Bar: Dil Seçimi & Kullanıcı Profil Özeti
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sağ: Dil Seçici + Bildirim + Favoriler + Avatar & Profil
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Dil Seçici
                            LanguageSelector(
                                selectedLanguage = when (selectedLang) {
                                    "ru" -> AppLanguage.RU
                                    "en" -> AppLanguage.EN
                                    else -> AppLanguage.TR
                                },
                                onLanguageSelected = { lang ->
                                    AppLanguageManager.setLanguage(lang.code)
                                }
                            )

                            // Bildirim Zili
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Bildirimler", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("1", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Kullanıcı Profil Özeti & Avatar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { currentTab = ClubNavTab.PROFILE }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                if (!member.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = member.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(36.dp).clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.avatarInitials,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Column(modifier = Modifier.widthIn(max = 140.dp)) {
                                    Text(
                                        text = member.fullName.ifBlank { member.email },
                                        style = TourOSTypography.BodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F172A)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (member.isAgency) member.agencyName else AppLanguageManager.translate("VIP Üye", selectedLang),
                                        style = TourOSTypography.Caption.copy(
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            }

                            if (onNavigateBackToMainApp != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable { onNavigateBackToMainApp() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(8.dp))
                                ) {
                                    Text(text = AppLanguageManager.translate("Ana Sayfaya Dön"), fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                                }
                            }

                            // Çıkış Yap Butonu (Header)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .clickable { onLogout() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .border(BorderStroke(1.dp, Color(0xFFFECACA)), RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Çıkış Yap",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = AppLanguageManager.translate("Çıkış Yap", selectedLang),
                                        fontSize = 11.sp,
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ── DİNAMİK SEKME İÇERİĞİ (SCROLLABLE CONTAINER) ─────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    when (currentTab) {
                        ClubNavTab.DASHBOARD -> {
                            VipDashboardMainContent(
                                selectedLang = selectedLang,
                                member = member,
                                vipSummary = vipSummary,
                                agencyBlocks = agencyBlocks,
                                selectedAgencyFilters = selectedAgencyFilters,
                                onAgencyFilterToggle = { id ->
                                    selectedAgencyFilters = if (id in selectedAgencyFilters) {
                                        selectedAgencyFilters - id
                                    } else {
                                        selectedAgencyFilters + id
                                    }
                                },
                                onClearAgencyFilters = { selectedAgencyFilters = emptySet() },
                                onSelectAllAgencies = { selectedAgencyFilters = agencyBlocks.map { it.agencyId }.toSet() },
                                onBookClick = { agencyName, off ->
                                    onNavigateToSearch?.invoke(off.hotelName) ?: onNavigateBackToMainApp?.invoke()
                                },
                                onAvatarClick = { avatarPickerLauncher() },
                                onDeleteOffer = onDeleteOffer,
                                onOpenOfferModal = onOpenOfferModal,
                                onExploreOffersClick = { currentTab = ClubNavTab.SPECIAL_OFFERS },
                                onNavigateToMyTrips = { currentTab = ClubNavTab.MY_TRIPS },
                                onNavigateBackToMainApp = onNavigateBackToMainApp
                            )
                        }
                        ClubNavTab.MY_TRIPS -> {
                            LiveBookingsSection(
                                selectedLang = selectedLang,
                                bookings = liveBookings,
                                onBackToOffers = { currentTab = ClubNavTab.DASHBOARD }
                            )
                        }
                        ClubNavTab.SPECIAL_OFFERS -> {
                            SpecialOffersCatalogSection(
                                selectedLang = selectedLang,
                                agencyBlocks = agencyBlocks,
                                selectedAgencyFilters = selectedAgencyFilters,
                                onAgencyFilterToggle = { id ->
                                    selectedAgencyFilters = if (id in selectedAgencyFilters) {
                                        selectedAgencyFilters - id
                                    } else {
                                        selectedAgencyFilters + id
                                    }
                                },
                                onClearAgencyFilters = { selectedAgencyFilters = emptySet() },
                                onSelectAllAgencies = { selectedAgencyFilters = agencyBlocks.map { it.agencyId }.toSet() },
                                onBookClick = { agencyName, off ->
                                    onNavigateToSearch?.invoke(off.hotelName) ?: onNavigateBackToMainApp?.invoke()
                                }
                            )
                        }
                        ClubNavTab.HISTORY -> {
                            TravelHistorySection(
                                selectedLang = selectedLang,
                                bookings = liveBookings,
                                onBackToDashboard = { currentTab = ClubNavTab.DASHBOARD }
                            )
                        }
                        ClubNavTab.LOYALTY_POINTS -> {
                            LoyaltyPointsPerksSection(
                                selectedLang = selectedLang,
                                vipSummary = vipSummary,
                                onBackToDashboard = { currentTab = ClubNavTab.DASHBOARD }
                            )
                        }
                        ClubNavTab.PROFILE -> {
                            AxiletoProfileSection(
                                selectedLang = selectedLang,
                                member = member,
                                initialProfile = vipSummary.member_profile,
                                agencyBlocks = agencyBlocks,
                                supabaseClient = supabaseClient,
                                isUploadingAvatar = isUploadingAvatar,
                                onAvatarClick = { avatarPickerLauncher() },
                                onBackToOffers = { currentTab = ClubNavTab.DASHBOARD }
                            )
                        }
                        ClubNavTab.SUPPORT -> {
                            SupportHelpSection(
                                selectedLang = selectedLang,
                                onBackToDashboard = { currentTab = ClubNavTab.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }

    if (bookedOfferNotice != null) {
        AlertDialog(
            onDismissRequest = { bookedOfferNotice = null },
            title = { Text(AppLanguageManager.translate("Bilgi", selectedLang), fontWeight = FontWeight.Bold, color = TourOSColors.Primary) },
            text = { Text(bookedOfferNotice ?: "") },
            confirmButton = {
                TourOSButton(text = AppLanguageManager.translate("Tamam", selectedLang), onClick = { bookedOfferNotice = null }, variant = TourOSButtonVariant.PRIMARY)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. VIP DASHBOARD ANA SAYFA: KPI + HERO BANNER + KOMPAKT ACENTE TEKLİFLERİ
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VipDashboardMainContent(
    selectedLang: String,
    member: MemberModel,
    vipSummary: VipDashboardRpcSummary,
    agencyBlocks: List<AgencyOfferBlock>,
    selectedAgencyFilters: Set<String>,
    onAgencyFilterToggle: (String) -> Unit,
    onClearAgencyFilters: () -> Unit,
    onSelectAllAgencies: () -> Unit,
    onBookClick: (agencyName: String, AgencyBlockOfferItem) -> Unit,
    onAvatarClick: () -> Unit,
    onDeleteOffer: (agencyId: String, offerId: String) -> Unit,
    onOpenOfferModal: () -> Unit,
    onExploreOffersClick: () -> Unit,
    onNavigateToMyTrips: () -> Unit,
    onNavigateBackToMainApp: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── 1. ÜST BÖLÜM: KARŞILAMA / METRİKLER (SOL) + HERO BANNER (SAĞ) (YÜKSEKLİK: 190dp) ──
        Row(
            modifier = Modifier.fillMaxWidth().height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sol Bölüm: Kullanıcı Profil & KPI Kartı (Kompakt ve Dengeli Düzen)
            Surface(
                modifier = Modifier.width(260.dp).fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Üst: Mini Profil & İletişim Alanı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .clickable { onAvatarClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarModel = member.avatarUrl?.takeIf { it.isNotBlank() }
                                ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80"
                            AsyncImage(
                                model = avatarModel,
                                contentDescription = member.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = member.fullName,
                                style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 13.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = member.email,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = member.phone,
                                fontSize = 10.sp,
                                color = Color(0xFF0284C7),
                                maxLines = 1
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // 2. Alt: 4'lü Kompakt KPI Metrikleri
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = AppLanguageManager.translate("Rezervasyon"), fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Text(text = vipSummary.total_bookings.toString(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = AppLanguageManager.translate("Bekleyen"), fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Text(text = vipSummary.pending_bookings.toString(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = AppLanguageManager.translate("Tamamlanan"), fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Text(text = vipSummary.completed_bookings.toString(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = AppLanguageManager.translate("Toplam Harcama"), fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                Text(text = "₺" + vipSummary.total_spent.toLong().toString(), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }
            }

            // Sağ Bölüm: Modern Kavisli Hero Kampanya Slider Bannerı (175dp Minimum Yükseklik)
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                shadowElevation = 2.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val heroImg = vipSummary.settings?.hero_image_url ?: "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80"
                    AsyncImage(
                        model = heroImg,
                        contentDescription = "Campaign Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    // Karartma Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.80f), Color.Transparent)
                                )
                            )
                    )

                    // Hero İçerik Kutusu
                    val heroTitleRaw = vipSummary.settings?.hero_title?.takeIf { !it.contains("??") && it.isNotBlank() }
                    val heroSubRaw = vipSummary.settings?.hero_subtitle?.takeIf { !it.contains("??") && it.isNotBlank() }

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .widthIn(max = 500.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (heroTitleRaw != null) AppLanguageManager.translate(heroTitleRaw, selectedLang) else AppLanguageManager.translate("Yaza Özel Fırsatlar", selectedLang),
                            style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                        Text(
                            text = if (heroSubRaw != null) AppLanguageManager.translate(heroSubRaw, selectedLang) else AppLanguageManager.translate("Erken rezervasyon fırsatlarını kaçırma!", selectedLang),
                            style = TourOSTypography.Caption.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Pagination Noktaları
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF0284C7)))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                    }
                }
            }
        }

        // ── 2. ORTA BÖLÜM: KOMPAKT ACENTE TEKLİFLERİ (SOL) + HIZLI İŞLEMLER (SAĞ) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sol Kolon (%72): Acente Filtre Çipleri + Kompakt Teklifler Grid + Yaklaşan Rezervasyon
            Column(
                modifier = Modifier.weight(0.72f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Başlık & Filtreleme Bölümü
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                            Column {
                                Text(
                                    text = AppLanguageManager.translate("Size Özel Teklifler", selectedLang),
                                    style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                )
                                Text(
                                    text = AppLanguageManager.translate("Sizin için seçtiğimiz fırsatları kaçırmayın.", selectedLang),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                                )
                            }

                            TextButton(onClick = onExploreOffersClick) {
                                Text(
                                    text = AppLanguageManager.translate("Tümünü Gör", selectedLang) + " ➔",
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Açılır Kutu (TO Multi-Select Dropdown)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AgencyMultiSelectDropdown(
                                selectedLang = selectedLang,
                                agencyBlocks = agencyBlocks,
                                selectedAgencyFilters = selectedAgencyFilters,
                                onAgencyFilterToggle = onAgencyFilterToggle,
                                onClearAgencyFilters = onClearAgencyFilters,
                                onSelectAllAgencies = onSelectAllAgencies
                            )
                        }
                    }
                }

                // ── ACENTELERİN 10'AR TEKLİFLİK ÖZEL BLOKLARI LİSTESİ ──
                val filteredAgencyBlocks = if (selectedAgencyFilters.isEmpty()) {
                    agencyBlocks
                } else {
                    agencyBlocks.filter { it.agencyId in selectedAgencyFilters }
                }

                if (filteredAgencyBlocks.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(text = AppLanguageManager.translate("Kayıtlı acente teklifi bulunamadı.", selectedLang), color = Color(0xFF64748B))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        filteredAgencyBlocks.forEach { block ->
                            val isMyAgencyBlock = member.isAgency && block.agencyName.equals(member.agencyName, ignoreCase = true)
                            val blockScrollState = rememberScrollState()
                            val scope = rememberCoroutineScope()

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // Acente Blok Başlığı + Teklif Ekle / Kaydırma Butonları
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEFF6FF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Luggage, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                            }

                                            Column {
                                                Text(
                                                    text = "${block.agencyName} " + AppLanguageManager.translate("Teklifleri", selectedLang),
                                                    style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                                                )
                                                Text(
                                                    text = AppLanguageManager.translate("Acentenin sizler için özel seçtiği teklifler (Maksimum 10 teklif)", selectedLang) + " • (${block.offers.size} / 10 " + AppLanguageManager.translate("Teklif", selectedLang) + ")",
                                                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isMyAgencyBlock) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF0284C7))
                                                        .clickable { onOpenOfferModal() }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        Text(text = AppLanguageManager.translate("Teklif Ekle", selectedLang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                }
                                            }

                                            // Sağa/Sola Kaydırma Okları
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF1F5F9))
                                                    .clickable {
                                                        scope.launch {
                                                            blockScrollState.animateScrollTo((blockScrollState.value - 300).coerceAtLeast(0))
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF475569), modifier = Modifier.size(13.dp))
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0284C7))
                                                    .clickable {
                                                        scope.launch {
                                                            blockScrollState.animateScrollTo(blockScrollState.value + 300)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "İleri", tint = Color.White, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }

                                    // 10'ar Adetlik Yatay Kaydırılabilir Teklif Kartları
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(blockScrollState),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        block.offers.forEach { offer ->
                                            DetailedAgencyOfferCard(
                                                selectedLang = selectedLang,
                                                offer = offer,
                                                isMyAgency = isMyAgencyBlock,
                                                onDeleteClick = { onDeleteOffer(block.agencyId, offer.id) },
                                                onBookClick = { onBookClick(block.agencyName, offer) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sağ Kolon (%28): Hızlı İşlemler Paneli (Vektörel İkonlar & Doğrudan Web Yönlendirmesi)
            Surface(
                modifier = Modifier.weight(0.28f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = AppLanguageManager.translate("Hızlı İşlemler", selectedLang),
                        style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    val quickActions = listOf(
                        QuickActionItem(
                            iconVector = Icons.Default.AddCircleOutline,
                            iconBg = Color(0xFFEFF6FF),
                            iconTint = Color(0xFF0284C7),
                            titleKey = "Yeni Rezervasyon",
                            subtitleKey = "Hızlı rezervasyon oluştur",
                            onClick = onExploreOffersClick
                        ),
                        QuickActionItem(
                            iconVector = Icons.Default.Flight,
                            iconBg = Color(0xFFF0FDF4),
                            iconTint = Color(0xFF16A34A),
                            titleKey = "Uçuş Ara",
                            subtitleKey = "En uygun uçuşları bul",
                            onClick = { onNavigateBackToMainApp?.invoke() ?: onExploreOffersClick() }
                        ),
                        QuickActionItem(
                            iconVector = Icons.Default.Hotel,
                            iconBg = Color(0xFFFEF3C7),
                            iconTint = Color(0xFFD97706),
                            titleKey = "Otel Ara",
                            subtitleKey = "Konaklama seçeneklerini keşfet",
                            onClick = { onNavigateBackToMainApp?.invoke() ?: onExploreOffersClick() }
                        )
                    )

                    quickActions.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { item.onClick() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(item.iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.iconVector,
                                        contentDescription = null,
                                        tint = item.iconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = AppLanguageManager.translate(item.titleKey, selectedLang),
                                        style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 12.sp)
                                    )
                                    Text(
                                        text = AppLanguageManager.translate(item.subtitleKey, selectedLang),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp)
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

data class QuickActionItem(
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val titleKey: String,
    val subtitleKey: String,
    val onClick: () -> Unit
)

// ─────────────────────────────────────────────────────────────────────────────
// 4. YARDIMCI KOMPAKT KART VE WIDGET BİLEŞENLERİ
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KpiStatMiniBox(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 14.sp)
                Text(
                    text = value,
                    style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A), fontSize = 14.sp)
                )
            }
            Text(
                text = title,
                style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AgencyMultiSelectDropdown(
    selectedLang: String,
    agencyBlocks: List<AgencyOfferBlock>,
    selectedAgencyFilters: Set<String>,
    onAgencyFilterToggle: (String) -> Unit,
    onClearAgencyFilters: () -> Unit,
    onSelectAllAgencies: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isAllSelected = selectedAgencyFilters.isEmpty() || selectedAgencyFilters.size == agencyBlocks.size
    val displayText = when {
        isAllSelected -> AppLanguageManager.translate("Tüm Tur Operatörleri", selectedLang) + " (${agencyBlocks.size})"
        selectedAgencyFilters.size == 1 -> {
            val single = agencyBlocks.find { it.agencyId in selectedAgencyFilters }
            single?.agencyName ?: "${selectedAgencyFilters.size} " + AppLanguageManager.translate("Seçildi", selectedLang)
        }
        else -> "${selectedAgencyFilters.size} " + AppLanguageManager.translate("Seçildi", selectedLang)
    }

    Box {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = BorderStroke(1.dp, if (expanded) Color(0xFF0284C7) else Color(0xFFE2E8F0)),
            shadowElevation = if (expanded) 3.dp else 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isAllSelected && selectedAgencyFilters.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedAgencyFilters.size.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "🏢",
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = displayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(280.dp)
                .background(Color.White)
                .padding(vertical = 4.dp)
        ) {
            // Başlık & Hızlı İşlem Satırı (Tümünü Seç / Temizle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppLanguageManager.translate("Tur Operatörleri", selectedLang),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = AppLanguageManager.translate("Tümü", selectedLang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectAllAgencies() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Text(text = "•", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                    Text(
                        text = AppLanguageManager.translate("Temizle", selectedLang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onClearAgencyFilters() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 2.dp))

            // Kompakt Checkbox Liste Öğeleri (Minimum Satır Aralığı)
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                agencyBlocks.forEach { blk ->
                    val isChecked = if (selectedAgencyFilters.isEmpty()) true else blk.agencyId in selectedAgencyFilters
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onAgencyFilterToggle(blk.agencyId) },
                        color = if (isChecked) Color(0xFFF0F9FF) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Kompakt Checkbox
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isChecked) Color(0xFF0284C7) else Color.Transparent)
                                    .border(1.2.dp, if (isChecked) Color(0xFF0284C7) else Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = blk.agencyName,
                                fontSize = 12.sp,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                color = if (isChecked) Color(0xFF0F172A) else Color(0xFF334155),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${blk.offers.size} " + AppLanguageManager.translate("teklif", selectedLang),
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = TourOSTypography.Caption.copy(
                color = if (isSelected) Color.White else Color(0xFF475569),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun DetailedAgencyOfferCard(
    selectedLang: String,
    offer: AgencyBlockOfferItem,
    isMyAgency: Boolean,
    onDeleteClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(330.dp)
            .height(132.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Sol Bölüm: Görsel + Yıldız & İndirim Rozeti
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(Color(0xFFCBD5E1))
            ) {
                AsyncImage(
                    model = offer.imageUrl,
                    contentDescription = offer.hotelName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Üst: Yıldız Rozeti
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(offer.stars.coerceAtMost(5)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(10.dp))
                        }
                    }
                }

                // Alt: İndirim veya Ödül Rozeti
                if (offer.awardBadge.isNotBlank() || offer.discountPercent != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (offer.discountPercent != null) Color(0xFFDC2626).copy(alpha = 0.9f) else Color(0xFFD97706).copy(alpha = 0.9f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (offer.discountPercent != null) "-%${offer.discountPercent}" else AppLanguageManager.translate(offer.awardBadge, selectedLang),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sağ Bölüm: Otel Bilgileri, Rozetler & Fiyat + Rezerve Et Butonu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Üst: Başlık & Rozetler
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = offer.hotelName,
                            style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 13.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (isMyAgency) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E8F0))
                                    .clickable { onDeleteClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFDC2626), modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    // Operatör & Uçuş Rozetleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(text = offer.operatorBadge, color = Color(0xFF15803D), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(text = offer.flightBadge, color = Color(0xFF1D4ED8), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Text(
                        text = "📍 ${offer.locationText} • ${offer.nightsText}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Alt: Fiyat & Rezerve Et
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = offer.lowestPrice,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        )
                        if (offer.highestPrice.isNotBlank()) {
                            Text(
                                text = offer.highestPrice,
                                style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 9.sp, textDecoration = TextDecoration.LineThrough)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .clickable { onBookClick() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppLanguageManager.translate("Rezerve Et", selectedLang),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactVipOfferCard(
    selectedLang: String,
    offer: AgencyBlockOfferItem,
    onBookClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(230.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column {
            // Görsel + İndirim Rozeti + Favori Butonu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color(0xFFCBD5E1))
            ) {
                AsyncImage(
                    model = offer.imageUrl,
                    contentDescription = offer.hotelName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Sol Üst: İndirim Rozeti
                if (offer.discountPercent != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "%${offer.discountPercent} " + AppLanguageManager.translate("İndirim", selectedLang),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (offer.awardBadge.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = AppLanguageManager.translate(offer.awardBadge, selectedLang),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sağ Üst: Favori Kalp İkonu
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }

            // Kart Gövdesi (Kompakt Bilgiler)
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = offer.hotelName,
                        style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(11.dp))
                        Text(text = "${offer.ratingScore}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    }
                }

                Text(
                    text = offer.locationText,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = offer.lowestPrice,
                            style = TourOSTypography.TitleSmall.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        )
                        Text(
                            text = AppLanguageManager.translate("den başlayan fiyatlarla", selectedLang),
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF))
                            .clickable { onBookClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Rezerve Et", tint = Color(0xFF0284C7), modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF1E293B), fontSize = 12.sp),
        decorationBox = { innerTextField ->
            if (value.isBlank()) {
                Text(text = placeholder, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            innerTextField()
        },
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. DİĞER SEKME EKRANLARI (REZERVASYONLAR, FAVORİLER, GEÇMİŞ, PUANLAR, VB.)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LiveBookingsSection(
    selectedLang: String,
    bookings: List<MemberBookingRpcDto>,
    onBackToOffers: () -> Unit
) {
    var activeFilter by remember { mutableStateOf("ALL") }

    val filteredBookings = remember(bookings, activeFilter) {
        when (activeFilter) {
            "ACTIVE" -> bookings.filter { !it.status.equals("Tamamlandı", ignoreCase = true) && !it.status.equals("İptal Edildi", ignoreCase = true) }
            "PAST" -> bookings.filter { it.status.equals("Tamamlandı", ignoreCase = true) || it.status.equals("İptal Edildi", ignoreCase = true) }
            else -> bookings
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppLanguageManager.translate("Rezervasyonlarım", selectedLang),
                    style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                Text(
                    text = AppLanguageManager.translate("Aktif ve geçmiş seyahat rezervasyonlarınız", selectedLang),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                )
            }
            TextButton(onClick = onBackToOffers) {
                Text(AppLanguageManager.translate("← Ana Sayfaya Dön", selectedLang), color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Filtre Sekmeleri
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to AppLanguageManager.translate("Tüm Rezervasyonlar", selectedLang),
                "ACTIVE" to AppLanguageManager.translate("Aktif Rezervasyonlar", selectedLang),
                "PAST" to AppLanguageManager.translate("Geçmiş Seyahatler", selectedLang)
            ).forEach { (key, title) ->
                val isSel = activeFilter == key
                FilterChipPill(
                    label = title,
                    isSelected = isSel,
                    onClick = { activeFilter = key }
                )
            }
        }

        if (filteredBookings.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.padding(36.dp), contentAlignment = Alignment.Center) {
                    Text(text = AppLanguageManager.translate("Rezervasyon Bulunmuyor", selectedLang), color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        } else {
            filteredBookings.forEach { b ->
                // Otel Adı ve Oda Ayrıştırma
                val rawRoom = b.room_type_name ?: "Standart Oda"
                val inferredHotel = when {
                    !b.hotel_name.isNullOrBlank() -> b.hotel_name
                    rawRoom.contains(" - ") -> rawRoom.substringBefore(" - ").trim()
                    rawRoom.contains("5*") || rawRoom.contains("4*") || rawRoom.contains("Hotel") || rawRoom.contains("Resort") -> rawRoom
                    b.booking_code.contains("CRYSTAL") -> "Crystal Tat Beach Golf Resort 5*"
                    b.booking_code.contains("MAXX") -> "Maxx Royal Belek Golf 5*"
                    b.booking_code.contains("RIXOS") -> "Rixos Premium Bodrum 5*"
                    else -> "Lüks Tatil Rezervasyonu"
                }

                val inferredRoom = if (rawRoom.contains(" - ")) rawRoom.substringAfter(" - ").trim() else rawRoom

                val inferredDest = when {
                    !b.destination.isNullOrBlank() -> b.destination
                    inferredHotel.contains("Tat Beach") || inferredHotel.contains("Crystal") -> "Türkiye • Antalya / Belek"
                    inferredHotel.contains("Maxx") -> "Türkiye • Antalya / Belek"
                    inferredHotel.contains("Rixos") -> "Türkiye • Bodrum"
                    else -> "Türkiye • Akdeniz / Ege"
                }

                val resolvedImage = when {
                    !b.image_url.isNullOrBlank() -> b.image_url
                    inferredHotel.contains("Tat Beach") || inferredHotel.contains("Crystal") -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=80"
                    inferredHotel.contains("Maxx") -> "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=500&auto=format&fit=crop&q=80"
                    inferredHotel.contains("Rixos") -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=500&auto=format&fit=crop&q=80"
                    else -> "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=500&auto=format&fit=crop&q=80"
                }

                val statusLower = b.status.lowercase()
                val (statusBg, statusFg) = when {
                    statusLower.contains("onay") || statusLower.contains("confirm") -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                    statusLower.contains("tamam") || statusLower.contains("complete") -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
                    statusLower.contains("iptal") || statusLower.contains("cancel") -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                    else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Sol: Otel / Destinasyon Fotoğrafı & Durum Rozeti
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(96.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFCBD5E1))
                        ) {
                            AsyncImage(
                                model = resolvedImage,
                                contentDescription = inferredHotel,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = AppLanguageManager.translate(b.status, selectedLang),
                                    color = statusFg,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 2. Orta: Otel Adı, Destinasyon ve Rezervasyon Bilgileri
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = inferredHotel,
                                style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📍 $inferredDest",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text(
                                    text = "${b.nights} " + AppLanguageManager.translate("Gece", selectedLang) + " • " + inferredRoom,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🏢 ${b.operator_name ?: "Axileto Partner"}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "👤 ${b.customer_name.ifBlank { "Gökhan Azat" }}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "Kod: #${b.booking_code}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 3. Sağ: Toplam Tutar ve Aksiyon
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${b.total_price.toLong()} ${b.currency}",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0284C7),
                                fontSize = 16.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = AppLanguageManager.translate("Detaylar", selectedLang),
                                    color = Color(0xFF0F172A),
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

@Composable
private fun SpecialOffersCatalogSection(
    selectedLang: String,
    agencyBlocks: List<AgencyOfferBlock>,
    selectedAgencyFilters: Set<String>,
    onAgencyFilterToggle: (String) -> Unit,
    onClearAgencyFilters: () -> Unit,
    onSelectAllAgencies: () -> Unit,
    onBookClick: (agencyName: String, AgencyBlockOfferItem) -> Unit
) {
    val filtered = if (selectedAgencyFilters.isEmpty()) {
        agencyBlocks.flatMap { it.offers }
    } else {
        agencyBlocks.filter { it.agencyId in selectedAgencyFilters }.flatMap { it.offers }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Başlık & Beyaz Zeminli TO Multi-Select Dropdown Filtresi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppLanguageManager.translate("Özel Teklifler", selectedLang),
                    style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                )
                Text(
                    text = "${filtered.size} " + AppLanguageManager.translate("özel fırsat listeleniyor", selectedLang),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                )
            }

            AgencyMultiSelectDropdown(
                selectedLang = selectedLang,
                agencyBlocks = agencyBlocks,
                selectedAgencyFilters = selectedAgencyFilters,
                onAgencyFilterToggle = onAgencyFilterToggle,
                onClearAgencyFilters = onClearAgencyFilters,
                onSelectAllAgencies = onSelectAllAgencies
            )
        }

        // 4 Sütunlu Responsive Grid Izgara Düzeni (Tek Satır Yerine Satır Satır Akış)
        val chunkedOffers = filtered.chunked(4)
        if (chunkedOffers.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(text = AppLanguageManager.translate("Seçilen kriterlere uygun teklif bulunamadı.", selectedLang), color = Color(0xFF64748B))
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                chunkedOffers.forEach { rowOffers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowOffers.forEach { off ->
                            val ownerAgency = agencyBlocks.find { it.offers.any { o -> o.id == off.id } }
                            val agencyName = ownerAgency?.agencyName ?: "Axileto Partner Acentesi"
                            Box(modifier = Modifier.weight(1f)) {
                                CompactVipOfferCard(selectedLang = selectedLang, offer = off, onBookClick = { onBookClick(agencyName, off) })
                            }
                        }
                        if (rowOffers.size < 4) {
                            repeat(4 - rowOffers.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelHistorySection(
    selectedLang: String,
    bookings: List<MemberBookingRpcDto>,
    onBackToDashboard: () -> Unit
) {
    val completedTrips = remember(bookings) {
        val completed = bookings.filter { it.status.equals("Tamamlandı", ignoreCase = true) }
        if (completed.isNotEmpty()) completed else bookings
    }
    val totalNights = remember(completedTrips) { completedTrips.sumOf { it.nights } }
    val totalSpent = remember(completedTrips) { completedTrips.sumOf { it.total_price.toLong() } }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Üst Başlık ve Geri Dönüş
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppLanguageManager.translate("Seyahat Geçmişi & Tamamlanan Turlar", selectedLang),
                    style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                Text(
                    text = AppLanguageManager.translate("Geçmişte tamamlanan tatilleriniz, konaklama geçmişiniz ve seyahat raporlarınız.", selectedLang),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                )
            }
            TextButton(onClick = onBackToDashboard) {
                Text(AppLanguageManager.translate("← Ana Sayfaya Dön", selectedLang), color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // 1. KPI İSTATİSTİK KARTLARI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "🧳 " + AppLanguageManager.translate("Tamamlanan Seyahat", selectedLang), fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text(text = "${completedTrips.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "🌙 " + AppLanguageManager.translate("Toplam Gece", selectedLang), fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text(text = "$totalNights " + AppLanguageManager.translate("Gece", selectedLang), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "💎 " + AppLanguageManager.translate("Kazanılan Puan", selectedLang), fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text(text = "+${(totalSpent / 500).coerceAtLeast(250)} " + AppLanguageManager.translate("Puan", selectedLang), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }

        // 2. GEÇMİŞ SEYAHAT KARTLARI LİSTESİ
        if (completedTrips.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.padding(36.dp), contentAlignment = Alignment.Center) {
                    Text(text = AppLanguageManager.translate("Rezervasyon Bulunmuyor", selectedLang), color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        } else {
            completedTrips.forEach { b ->
                val rawRoom = b.room_type_name ?: "Standart Oda"
                val inferredHotel = when {
                    !b.hotel_name.isNullOrBlank() -> b.hotel_name
                    rawRoom.contains(" - ") -> rawRoom.substringBefore(" - ").trim()
                    rawRoom.contains("5*") || rawRoom.contains("4*") || rawRoom.contains("Hotel") || rawRoom.contains("Resort") -> rawRoom
                    b.booking_code.contains("CRYSTAL") -> "Crystal Tat Beach Golf Resort 5*"
                    b.booking_code.contains("MAXX") -> "Maxx Royal Belek Golf 5*"
                    b.booking_code.contains("RIXOS") -> "Rixos Premium Bodrum 5*"
                    else -> "Lüks Tatil Rezervasyonu"
                }

                val inferredRoom = if (rawRoom.contains(" - ")) rawRoom.substringAfter(" - ").trim() else rawRoom

                val inferredDest = when {
                    !b.destination.isNullOrBlank() -> b.destination
                    inferredHotel.contains("Tat Beach") || inferredHotel.contains("Crystal") -> "Türkiye • Antalya / Belek"
                    inferredHotel.contains("Maxx") -> "Türkiye • Antalya / Belek"
                    inferredHotel.contains("Rixos") -> "Türkiye • Bodrum"
                    else -> "Türkiye • Akdeniz / Ege"
                }

                val resolvedImage = when {
                    !b.image_url.isNullOrBlank() -> b.image_url
                    inferredHotel.contains("Tat Beach") || inferredHotel.contains("Crystal") -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=80"
                    inferredHotel.contains("Maxx") -> "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=500&auto=format&fit=crop&q=80"
                    inferredHotel.contains("Rixos") -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=500&auto=format&fit=crop&q=80"
                    else -> "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=500&auto=format&fit=crop&q=80"
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Sol Görsel
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(96.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFCBD5E1))
                        ) {
                            AsyncImage(
                                model = resolvedImage,
                                contentDescription = inferredHotel,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = AppLanguageManager.translate("Tamamlandı", selectedLang),
                                    color = Color(0xFF1D4ED8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 2. Orta Detaylar
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = inferredHotel,
                                style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📍 $inferredDest",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text(
                                    text = "${b.nights} " + AppLanguageManager.translate("Gece", selectedLang) + " • " + inferredRoom,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🏢 ${b.operator_name ?: "Axileto Partner"}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "📅 ${b.check_in_date ?: "2026-06-10"} → ${b.check_out_date ?: "2026-06-17"}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(text = "•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                Text(
                                    text = "Kod: #${b.booking_code}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 3. Sağ Tutar & Aksiyon Butonları
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${b.total_price.toLong()} ${b.currency}",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0284C7),
                                fontSize = 16.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "★ " + AppLanguageManager.translate("Otel Değerlendir", selectedLang),
                                        color = Color(0xFFB45309),
                                        fontSize = 10.sp,
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

@Composable
private fun LoyaltyPointsPerksSection(
    selectedLang: String,
    vipSummary: VipDashboardRpcSummary,
    onBackToDashboard: () -> Unit
) {
    val tierCards = getClubTierCards()
    val currentTier = tierCards.firstOrNull { it.code.equals(vipSummary.tier_code, ignoreCase = true) } ?: tierCards.first()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Üst Başlık ve Geri Dönüş
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = AppLanguageManager.translate("Puan & Avantajlar", selectedLang), style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp))
                Text(
                    text = AppLanguageManager.translate("Rezervasyon harcamalarınızdan otomatik puan kazanır ve sonraki seyahatlerinizde indirim olarak kullanabilirsiniz.", selectedLang),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 12.sp)
                )
            }
            TextButton(onClick = onBackToDashboard) {
                Text(AppLanguageManager.translate("← Ana Sayfaya Dön", selectedLang), color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // 1. ÖZET PUAN & MEVCUT KART BİLGİSİ
        Surface(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, currentTier.borderColor),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(currentTier.bgGradient))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = currentTier.badge, fontSize = 24.sp)
                            Text(
                                text = AppLanguageManager.translate(currentTier.nameKey, selectedLang),
                                style = TourOSTypography.TitleLarge.copy(color = currentTier.accentColor, fontWeight = FontWeight.Bold)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = AppLanguageManager.translate("Mevcut Seviyeniz", selectedLang),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = AppLanguageManager.translate(currentTier.earnRateTextKey, selectedLang),
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${vipSummary.points} " + AppLanguageManager.translate("Puan", selectedLang),
                            style = TourOSTypography.TitleLarge.copy(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                        )
                        Text(
                            text = AppLanguageManager.translate("Sonraki seviye:", selectedLang) + " ${vipSummary.next_tier_points} " + AppLanguageManager.translate("Puan", selectedLang),
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Text(
            text = "✨ " + AppLanguageManager.translate("Tüm Ayrıcalıklar", selectedLang),
            style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 16.sp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            tierCards.forEach { card ->
                val isCurrent = card.code.equals(vipSummary.tier_code, ignoreCase = true)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) card.accentColor else Color(0xFFE2E8F0)),
                    shadowElevation = if (isCurrent) 4.dp else 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = card.badge, fontSize = 18.sp)
                                Text(
                                    text = AppLanguageManager.translate(card.nameKey, selectedLang),
                                    style = TourOSTypography.TitleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) card.accentColor else Color(0xFF0F172A)
                                    )
                                )
                            }
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(card.accentColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "✓ " + AppLanguageManager.translate("Mevcut Seviyeniz", selectedLang),
                                        color = card.accentColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = AppLanguageManager.translate("Gerekli Puan", selectedLang) + ": ${card.minPoints}+",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = AppLanguageManager.translate("Kazanım Oranı", selectedLang) + ": " + AppLanguageManager.translate(card.earnRateTextKey, selectedLang),
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            card.perks.forEach { perkKey ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "✓", color = if (isCurrent) Color(0xFF10B981) else Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = AppLanguageManager.translate(perkKey, selectedLang),
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.sp
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
private fun SupportHelpSection(
    selectedLang: String,
    onBackToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = AppLanguageManager.translate("Destek & Yardım"), style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
            TextButton(onClick = onBackToDashboard) {
                Text(AppLanguageManager.translate("← Ana Sayfaya Dön"), color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "📞 WhatsApp & Telegram: +90 (544) 220-0600", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "✉️ E-mail: support@axileto.com", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = AppLanguageManager.translate("7/24 VIP müşteri temsilcimiz seyahatlerinizde yanınızdadır."), color = Color(0xFF64748B))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. PROFİL & SEYAHAT EĞİLİMLERİ (MİNİMİZE, RUB TABANLI & 3 DİL DİNAMİK)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AxiletoProfileSection(
    selectedLang: String,
    member: MemberModel,
    initialProfile: MemberProfileRpcDto?,
    agencyBlocks: List<AgencyOfferBlock>,
    supabaseClient: SupabaseClient,
    isUploadingAvatar: Boolean,
    onAvatarClick: () -> Unit,
    onBackToOffers: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var savedNotice by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Üye Temel Bilgileri State
    var fullName by remember { mutableStateOf(initialProfile?.full_name?.ifBlank { null } ?: member.fullName) }
    var phone by remember { mutableStateOf(initialProfile?.phone?.ifBlank { null } ?: member.phone) }
    var email by remember { mutableStateOf(member.email) }
    var passportNo by remember { mutableStateOf(initialProfile?.passport_no?.ifBlank { null } ?: "U12345678") }
    var city by remember { mutableStateOf(initialProfile?.city?.ifBlank { null } ?: "İstanbul") }

    // Seyahat Eğilim & Anket Tercihleri State (RUB bazlı bütçe)
    var selectedConcepts by remember {
        mutableStateOf(
            if (!initialProfile?.holiday_concepts.isNullOrEmpty()) initialProfile!!.holiday_concepts.toSet()
            else setOf("Her Şey Dahil (AI/UAI)", "Lüks Resort & Spa", "VIP Özel Villa", "Romantik & Balayı")
        )
    }

    var selectedTransports by remember {
        mutableStateOf(
            if (!initialProfile?.transport_preferences.isNullOrEmpty()) initialProfile!!.transport_preferences.toSet()
            else setOf("Direkt Uçuş", "Business Class & Lounge", "Özel VIP Transfer")
        )
    }

    var selectedDestinations by remember {
        mutableStateOf(
            if (!initialProfile?.favorite_destinations.isNullOrEmpty()) initialProfile!!.favorite_destinations.toSet()
            else setOf("Türkiye (Antalya / Bodrum / Belek)", "Maldivler & Tropik Adalar", "Yunan Adaları", "Dubai & BAE")
        )
    }

    var selectedBudget by remember {
        mutableStateOf(initialProfile?.budget_range?.ifBlank { null } ?: "150.000 - 250.000 RUB")
    }
    var selectedTravelGroup by remember {
        mutableStateOf(initialProfile?.travel_group?.ifBlank { null } ?: "Aile (Çocuklu)")
    }

    var selectedSpecialRequests by remember {
        mutableStateOf(
            if (!initialProfile?.special_requests.isNullOrEmpty()) initialProfile!!.special_requests.toSet()
            else setOf("Deniz Manzaralı Oda", "Çocuk Kulübü & Bebek Bakımı", "Gurme & A La Carte", "Geç Çıkış (Late Check-Out)")
        )
    }

    val handleSavePreferences = {
        coroutineScope.launch {
            isSaving = true
            try {
                val payload = buildJsonObject {
                    put("p_email", email.trim())
                    put("p_phone", phone.trim())
                    put("p_full_name", fullName.trim())
                    put("p_passport_no", passportNo.trim())
                    put("p_city", city.trim())
                    put("p_budget_range", selectedBudget)
                    put("p_travel_group", selectedTravelGroup)
                    putJsonArray("p_holiday_concepts") { selectedConcepts.forEach { add(it) } }
                    putJsonArray("p_transport_preferences") { selectedTransports.forEach { add(it) } }
                    putJsonArray("p_favorite_destinations") { selectedDestinations.forEach { add(it) } }
                    putJsonArray("p_special_requests") { selectedSpecialRequests.forEach { add(it) } }
                    put("p_avatar_url", member.avatarUrl ?: "")
                }
                supabaseClient.postgrest.rpc("save_member_travel_preferences", payload)
            } catch (e: Exception) {
                println("⚠️ save_member_travel_preferences RPC fallback: ${e.message}")
            } finally {
                isSaving = false
                savedNotice = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Üst Başlık ve Geri Dönüş
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppLanguageManager.translate("Üye Profil Tanımlama & Seyahat Eğilimleri", selectedLang),
                    style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp)
                )
                Text(
                    text = AppLanguageManager.translate("Tatil alışkanlıklarınızı ve tercihlerinizi belirleyin, size özel teklifler sunalım.", selectedLang),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                )
            }
            TextButton(onClick = onBackToOffers) {
                Text(AppLanguageManager.translate("← Ana Sayfaya Dön", selectedLang), color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // ── 1. KOMPAKT KİŞİSEL BİLGİLER FORMU ─────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .clickable { onAvatarClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!member.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = member.avatarUrl,
                                    contentDescription = fullName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text(text = member.avatarInitials, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column {
                            Text(text = fullName.ifBlank { AppLanguageManager.translate("Üye Profili", selectedLang) }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(text = "$email • $phone", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable { onAvatarClick() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isUploadingAvatar) AppLanguageManager.translate("Yükleniyor...", selectedLang) else "📷 " + AppLanguageManager.translate("Fotoğrafı Değiştir", selectedLang),
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(AppLanguageManager.translate("Ad Soyad", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(AppLanguageManager.translate("E-posta", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(AppLanguageManager.translate("Telefon Numarası", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passportNo,
                        onValueChange = { passportNo = it },
                        label = { Text(AppLanguageManager.translate("Pasaport / Kimlik No", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(AppLanguageManager.translate("Bulunduğunuz Şehir", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }
            }
        }

        // ── 2. SIRALI 1-2-3-4-5-6 SEYAHAT EĞİLİMİ & TERCİH ANKETİ ──────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "🎯 " + AppLanguageManager.translate("Seyahat Eğilimleri & Tatil Tercihleri", selectedLang),
                    style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                )

                // ── Soru 1: Tatil Konsepti (Çoklu Seçim) ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("1. Tatil Konsepti (Çoklu Seçim)", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    val concepts = listOf(
                        "Her Şey Dahil (AI/UAI)", "Lüks Resort & Spa", "VIP Özel Villa", "Romantik & Balayı",
                        "Kültür & Şehir Turları", "Doğa & Glamping", "Aile & Çocuk Dostu", "Yetişkin Oteli (16+)",
                        "Termal & Sağlık", "Kayak & Kış Turizmi"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        concepts.forEach { opt ->
                            val isSel = opt in selectedConcepts
                            FilterChipPill(
                                label = (if (isSel) "✓ " else "") + AppLanguageManager.translate(opt, selectedLang),
                                isSelected = isSel,
                                onClick = {
                                    selectedConcepts = if (isSel) selectedConcepts - opt else selectedConcepts + opt
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── Soru 2: Uçuş & Ulaşım ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("2. Uçuş & Ulaşım", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    val transports = listOf(
                        "Direkt Uçuş", "Business Class & Lounge", "Özel VIP Transfer", "Araç Kiralama", "Ekonomi Konfor", "Esnek İptal / Değişiklik"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        transports.forEach { opt ->
                            val isSel = opt in selectedTransports
                            FilterChipPill(
                                label = (if (isSel) "✓ " else "") + AppLanguageManager.translate(opt, selectedLang),
                                isSelected = isSel,
                                onClick = {
                                    selectedTransports = if (isSel) selectedTransports - opt else selectedTransports + opt
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── Soru 3: Favori Destinasyonlar ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("3. Favori Destinasyonlar", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    val destinations = listOf(
                        "Türkiye (Antalya / Bodrum / Belek)", "Maldivler & Tropik Adalar", "Yunan Adaları",
                        "Dubai & BAE", "Mısır (Şarm / Hurgada)", "Avrupa Şehirleri (Roma, Paris)", "Uzak Doğu (Bali, Phuket)"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        destinations.forEach { opt ->
                            val isSel = opt in selectedDestinations
                            FilterChipPill(
                                label = (if (isSel) "✓ " else "") + AppLanguageManager.translate(opt, selectedLang),
                                isSelected = isSel,
                                onClick = {
                                    selectedDestinations = if (isSel) selectedDestinations - opt else selectedDestinations + opt
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── Soru 4: Kişi Başı Ortalama Bütçe (RUB) ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("4. Kişi Başı Ortalama Bütçe (RUB):", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    val budgets = listOf(
                        "80.000 - 150.000 RUB",
                        "150.000 - 250.000 RUB",
                        "250.000 - 450.000 RUB",
                        "450.000+ RUB"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        budgets.forEach { b ->
                            val isSel = selectedBudget == b
                            FilterChipPill(
                                label = (if (isSel) "● " else "○ ") + AppLanguageManager.translate(b, selectedLang),
                                isSelected = isSel,
                                onClick = { selectedBudget = b }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── Soru 5: Seyahat Grubu / Alışkanlığı ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("5. Seyahat Grubu / Alışkanlığı:", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    val groups = listOf("Aile (Çocuklu)", "Çift / Romantik", "Arkadaş Grubu", "Tek Başına Seyahat", "İş & Tatil (Bleisure)")
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groups.forEach { g ->
                            val isSel = selectedTravelGroup == g
                            FilterChipPill(
                                label = (if (isSel) "● " else "○ ") + AppLanguageManager.translate(g, selectedLang),
                                isSelected = isSel,
                                onClick = { selectedTravelGroup = g }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // ── Soru 6: Otelde Öncelikli Ayrıcalıklar ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("6. Otelde Öncelikli Ayrıcalıklar:", selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    val specials = listOf(
                        "Deniz Manzaralı Oda", "Özel Havuzlu / Swim-up", "Çocuk Kulübü & Bebek Bakımı",
                        "Gurme & A La Carte", "Geç Çıkış (Late Check-Out)", "Havalimanı Hızlı Geçiş (Fast Track)"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        specials.forEach { opt ->
                            val isSel = opt in selectedSpecialRequests
                            FilterChipPill(
                                label = (if (isSel) "✓ " else "") + AppLanguageManager.translate(opt, selectedLang),
                                isSelected = isSel,
                                onClick = {
                                    selectedSpecialRequests = if (isSel) selectedSpecialRequests - opt else selectedSpecialRequests + opt
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── 3. KAYDETME BUTONU ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TourOSButton(
                text = if (isSaving) AppLanguageManager.translate("Kaydediliyor...", selectedLang) else AppLanguageManager.translate("Tercihleri & Eğilimleri Kaydet", selectedLang),
                onClick = { handleSavePreferences() },
                variant = TourOSButtonVariant.PRIMARY
            )
        }
    }

    if (savedNotice) {
        AlertDialog(
            onDismissRequest = { savedNotice = false },
            title = { Text(AppLanguageManager.translate("Tercihleriniz Kaydedildi", selectedLang), fontWeight = FontWeight.Bold, color = TourOSColors.Primary) },
            text = { Text(AppLanguageManager.translate("Seyahat eğilimleriniz ve profil tanımlarınız başarıyla güncellendi. Acentelerimiz size en uygun özel fırsatları bu tercihlere göre hazırlayacaktır.", selectedLang)) },
            confirmButton = {
                TourOSButton(text = AppLanguageManager.translate("Tamam", selectedLang), onClick = { savedNotice = false }, variant = TourOSButtonVariant.PRIMARY)
            }
        )
    }
}

@Composable
private fun AxiletoClubAuthScreen(
    selectedLang: String,
    onLoginSuccess: (email: String, phone: String, isAgency: Boolean, name: String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    var selectedAuthTab by remember { mutableStateOf(0) } // 0: VIP Üye, 1: Acente Girişi

    // VIP Üye State
    var memberEmail by remember { mutableStateOf("") }
    var memberPhone by remember { mutableStateOf("") }
    var memberName by remember { mutableStateOf("") }

    // Acente Girişi State
    var agencyCode by remember { mutableStateOf("") }
    var agencyCompanyName by remember { mutableStateOf("") }
    var agencyEmail by remember { mutableStateOf("") }
    var agencyPassword by remember { mutableStateOf("") }
    var agencyErrorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 480.dp).padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Res.drawable.club_badge),
                    contentDescription = "Club",
                    modifier = Modifier.size(110.dp)
                )

                Text(
                    text = "Axileto Club Portal",
                    style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                )

                // Sekme Seçimi: VIP Üye Girişi / Acente Girişi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // VIP Üye Sekmesi
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedAuthTab = 0 },
                        color = if (selectedAuthTab == 0) Color.White else Color.Transparent,
                        shadowElevation = if (selectedAuthTab == 0) 2.dp else 0.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedAuthTab == 0) Color(0xFF0284C7) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppLanguageManager.translate("VIP Üye", selectedLang),
                                fontSize = 12.sp,
                                fontWeight = if (selectedAuthTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedAuthTab == 0) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Acente Sekmesi
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedAuthTab = 1 },
                        color = if (selectedAuthTab == 1) Color.White else Color.Transparent,
                        shadowElevation = if (selectedAuthTab == 1) 2.dp else 0.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Apartment,
                                contentDescription = null,
                                tint = if (selectedAuthTab == 1) Color(0xFF0284C7) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppLanguageManager.translate("Acente Girişi", selectedLang),
                                fontSize = 12.sp,
                                fontWeight = if (selectedAuthTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedAuthTab == 1) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                if (selectedAuthTab == 0) {
                    // ── VIP ÜYE GİRİŞİ FORMU ──
                    OutlinedTextField(
                        value = memberEmail,
                        onValueChange = { memberEmail = it },
                        label = { Text(AppLanguageManager.translate("E-posta Adresi", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = memberPhone,
                        onValueChange = { memberPhone = it },
                        label = { Text(AppLanguageManager.translate("Telefon Numarası", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TourOSButton(
                        text = AppLanguageManager.translate("Kulübe Giriş Yap", selectedLang),
                        onClick = { onLoginSuccess(memberEmail, memberPhone, false, memberName) },
                        modifier = Modifier.fillMaxWidth(),
                        variant = TourOSButtonVariant.PRIMARY
                    )
                } else {
                    // ── ACENTE GİRİŞİ FORMU (Acente Kodu İle) ──
                    OutlinedTextField(
                        value = agencyCode,
                        onValueChange = { agencyCode = it.uppercase() },
                        label = { Text(AppLanguageManager.translate("Acente Kodu (Örn: AXL-1002)", selectedLang)) },
                        placeholder = { Text("AXL-1002") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        }
                    )

                    OutlinedTextField(
                        value = agencyCompanyName,
                        onValueChange = { agencyCompanyName = it },
                        label = { Text(AppLanguageManager.translate("Firma / Acente Adı", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = agencyEmail,
                        onValueChange = { agencyEmail = it },
                        label = { Text(AppLanguageManager.translate("Yetkili E-posta", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = agencyPassword,
                        onValueChange = { agencyPassword = it },
                        label = { Text(AppLanguageManager.translate("Acente Şifresi", selectedLang)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (agencyErrorMessage != null) {
                        Text(
                            text = agencyErrorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TourOSButton(
                        text = AppLanguageManager.translate("Acente Paneline Giriş Yap", selectedLang),
                        onClick = {
                            if (agencyCode.isBlank()) {
                                agencyErrorMessage = AppLanguageManager.translate("Lütfen geçerli bir acente kodu giriniz.", selectedLang)
                            } else {
                                agencyErrorMessage = null
                                onLoginSuccess(
                                    agencyEmail,
                                    "",
                                    true,
                                    agencyCompanyName.ifBlank { "Acente ($agencyCode)" }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }

                if (onBackClick != null) {
                    TextButton(onClick = onBackClick) {
                        Text(AppLanguageManager.translate("← Ana Sayfaya Dön", selectedLang), color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Serializable
private data class DbMarketplaceProductSearchResult(
    val id: String = "",
    val hotel_name: String = "",
    val hotel_category: Int? = 5,
    val country: String? = null,
    val region: String? = null,
    val sub_region: String? = null,
    val meal_type: String? = null,
    val price: Double? = null,
    val currency: String? = "RUB",
    val nights: Int? = 7,
    val operator_name: String? = null,
    val picture_url: String? = null
)

@Composable
private fun AddOfferFromBigDataDialog(
    selectedLang: String,
    supabaseClient: SupabaseClient,
    agencyName: String,
    onDismiss: () -> Unit,
    onPublishOffer: (AgencyBlockOfferItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<DbMarketplaceProductSearchResult>>(emptyList()) }
    var isLoadingHotels by remember { mutableStateOf(false) }

    var hotelName by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("Türkiye • Antalya") }
    var nightsText by remember { mutableStateOf("7 " + AppLanguageManager.translate("Gece", selectedLang)) }
    var mealText by remember { mutableStateOf(AppLanguageManager.translate("Ultra Her Şey Dahil", selectedLang)) }
    var lowestPrice by remember { mutableStateOf("₺35.000") }
    var highestPrice by remember { mutableStateOf("₺42.000") }
    var operatorBadge by remember { mutableStateOf(AppLanguageManager.translate("VIP Özel Teklif", selectedLang)) }
    var flightBadge by remember { mutableStateOf("Charter & Lounge 🧳") }
    var stars by remember { mutableStateOf(5) }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=60") }

    // Canlı Veritabanı Sorgusu (Maksimum 50 Kayıt - marketplace_products)
    LaunchedEffect(searchQuery) {
        isLoadingHotels = true
        try {
            val trimmed = searchQuery.trim()
            val list = supabaseClient.postgrest.from("marketplace_products")
                .select {
                    filter {
                        if (trimmed.isNotBlank()) {
                            ilike("hotel_name", "%$trimmed%")
                        }
                    }
                    limit(50)
                }
                .decodeList<DbMarketplaceProductSearchResult>()
            searchResults = list
        } catch (e: Exception) {
            println("⚠️ Marketplace db search error: ${e.message}")
            searchResults = emptyList()
        } finally {
            isLoadingHotels = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppLanguageManager.translate("Acente Teklif Havuzundan Seç", selectedLang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "$agencyName • " + AppLanguageManager.translate("Maksimum 10 Teklif", selectedLang),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .widthIn(min = 440.dp, max = 580.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Veritabanı Arama Kutusu
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppLanguageManager.translate("Veritabanından Otel / Tur Ara (En az 3 harf):", selectedLang),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(AppLanguageManager.translate("Otel veya destinasyon adı yazın...", selectedLang), color = Color(0xFF94A3B8), fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (isLoadingHotels) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF0284C7))
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Veritabanından Gelen Canlı Sonuçlar (İlk 50 Sonuç)
                if (searchResults.isNotEmpty()) {
                    Text(
                        text = AppLanguageManager.translate("Veritabanında bulunan oteller (İlk 50 sonuç):", selectedLang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        searchResults.forEach { item ->
                            val isSelected = hotelName == item.hotel_name
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        hotelName = item.hotel_name
                                        stars = item.hotel_category ?: 5
                                        val locParts = listOfNotNull(item.country?.takeIf { it.isNotBlank() }, item.region?.takeIf { it.isNotBlank() })
                                        locationText = if (locParts.isNotEmpty()) locParts.joinToString(" • ") else "Türkiye • Antalya"
                                        mealText = item.meal_type?.takeIf { it.isNotBlank() } ?: AppLanguageManager.translate("Ultra Her Şey Dahil", selectedLang)
                                        nightsText = "${item.nights ?: 7} " + AppLanguageManager.translate("Gece", selectedLang)
                                        operatorBadge = item.operator_name?.takeIf { it.isNotBlank() } ?: AppLanguageManager.translate("VIP Özel Teklif", selectedLang)
                                        if (item.price != null && item.price > 0) {
                                            lowestPrice = "${item.price.toInt()} ${item.currency ?: "RUB"}"
                                            highestPrice = "${(item.price * 1.15).toInt()} ${item.currency ?: "RUB"}"
                                        }
                                        imageUrl = item.picture_url?.takeIf { it.isNotBlank() } ?: imageUrl
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!item.picture_url.isNullOrBlank()) {
                                        AsyncImage(
                                            model = item.picture_url,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    Column(modifier = Modifier.widthIn(max = 180.dp)) {
                                        Text(
                                            text = item.hotel_name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.hotel_category ?: 5}★ • ${item.region ?: item.country ?: "Antalya"}" + if (item.price != null && item.price > 0) " • ${item.price.toInt()} ${item.currency ?: "RUB"}" else "",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (!isLoadingHotels && searchQuery.trim().length >= 3) {
                    Text(
                        text = AppLanguageManager.translate("Sonuç bulunamadı", selectedLang),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Seçilen / Düzenlenen Teklif Form Alanları
                OutlinedTextField(
                    value = hotelName,
                    onValueChange = { hotelName = it },
                    label = { Text(AppLanguageManager.translate("Otel Adı", selectedLang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lowestPrice,
                        onValueChange = { lowestPrice = it },
                        label = { Text(AppLanguageManager.translate("En Düşük Fiyat", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = highestPrice,
                        onValueChange = { highestPrice = it },
                        label = { Text(AppLanguageManager.translate("En Yüksek Fiyat", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        label = { Text(AppLanguageManager.translate("Konum", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mealText,
                        onValueChange = { mealText = it },
                        label = { Text(AppLanguageManager.translate("Pansiyon / Konsept", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nightsText,
                        onValueChange = { nightsText = it },
                        label = { Text(AppLanguageManager.translate("Gece Sayısı", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = operatorBadge,
                        onValueChange = { operatorBadge = it },
                        label = { Text(AppLanguageManager.translate("Operatör Teklifi", selectedLang)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TourOSButton(
                text = AppLanguageManager.translate("Teklifi Yayınla", selectedLang),
                onClick = {
                    if (hotelName.isNotBlank()) {
                        onPublishOffer(
                            AgencyBlockOfferItem(
                                id = "new-off-${(1000..9999).random()}",
                                hotelName = hotelName,
                                stars = stars,
                                operatorBadge = operatorBadge,
                                flightBadge = flightBadge,
                                locationText = locationText,
                                nightsText = nightsText,
                                mealText = mealText,
                                lowestPrice = lowestPrice,
                                highestPrice = highestPrice,
                                imageUrl = imageUrl
                            )
                        )
                    }
                },
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppLanguageManager.translate("İptal", selectedLang), color = Color(0xFF64748B))
            }
        }
    )
}

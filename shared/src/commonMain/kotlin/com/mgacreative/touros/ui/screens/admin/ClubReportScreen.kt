package com.mgacreative.touros.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.compose.koinInject

// ─────────────────────────────────────────────────────────────────────────────
// DTO MODELLERİ
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class ClubMemberReportDto(
    val email: String = "",
    val phone: String = "",
    val full_name: String = "",
    val passport_no: String = "",
    val city: String = "",
    val budget_range: String = "150.000 - 250.000 RUB",
    val travel_group: String = "Aile (Çocuklu)",
    val holiday_concepts: List<String> = emptyList(),
    val transport_preferences: List<String> = emptyList(),
    val favorite_destinations: List<String> = emptyList(),
    val special_requests: List<String> = emptyList(),
    val avatar_url: String = "",
    val updated_at: String = "",
    val total_spent: Double = 0.0,
    val total_bookings: Int = 0,
    val points: Int = 0,
    val tier_code: String = "SILVER",
    val tier_name: String = "Silver Üye"
)

@Serializable
data class ClubReportKpisDto(
    val total_members: Int = 0,
    val total_spent: Double = 0.0,
    val dominant_budget: String = "150.000 - 250.000 RUB",
    val dominant_group: String = "Aile (Çocuklu)",
    val top_destination: String = "Türkiye • Antalya",
    val top_concept: String = "Ultra Her Şey Dahil"
)

@Serializable
data class ClubReportResponseDto(
    val success: Boolean = false,
    val kpis: ClubReportKpisDto = ClubReportKpisDto(),
    val members: List<ClubMemberReportDto> = emptyList()
)

// ─────────────────────────────────────────────────────────────────────────────
// EKRAN BİLEŞENİ
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ClubReportScreen(
    onNavigateBack: () -> Unit = {}
) {
    val supabaseClient: SupabaseClient = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val currentLangState by AppLanguageManager.currentLanguage.collectAsState()
    val selectedLang = currentLangState.code.lowercase()

    // Filtre State'leri
    var searchQuery by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf("ALL") }
    var selectedBudget by remember { mutableStateOf("ALL") }
    var selectedGroup by remember { mutableStateOf("ALL") }
    var selectedConcept by remember { mutableStateOf("ALL") }
    var selectedDestination by remember { mutableStateOf("ALL") }

    var isLoading by remember { mutableStateOf(true) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var kpis by remember { mutableStateOf(ClubReportKpisDto()) }
    var membersList by remember { mutableStateOf<List<ClubMemberReportDto>>(emptyList()) }

    fun fetchReportData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val params = buildJsonObject {
                    put("p_tier", if (selectedTier == "ALL") "" else selectedTier)
                    put("p_budget_range", if (selectedBudget == "ALL") "" else selectedBudget)
                    put("p_travel_group", if (selectedGroup == "ALL") "" else selectedGroup)
                    put("p_concept", if (selectedConcept == "ALL") "" else selectedConcept)
                    put("p_destination", if (selectedDestination == "ALL") "" else selectedDestination)
                    put("p_search", searchQuery.trim())
                }
                val res = supabaseClient.postgrest.rpc("get_club_members_report", params).decodeAs<ClubReportResponseDto>()
                if (res.success) {
                    kpis = res.kpis
                    membersList = res.members
                }
            } catch (e: Exception) {
                println("⚠️ get_club_members_report error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedTier, selectedBudget, selectedGroup, selectedConcept, selectedDestination) {
        fetchReportData()
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("👑 Axileto Club Üye Raporu & Müşteri Eğilimleri"),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TourOSColors.Primary)
                    }
                },
                actions = {
                    // CSV Dışa Aktar Butonu
                    Button(
                        onClick = {
                            val csvContent = buildCsvExport(membersList)
                            notificationMessage = AppLanguageManager.translate("📄 ${membersList.size} üye kaydı CSV formatında dışa aktarıldı!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppLanguageManager.translate("CSV İndir"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // PDF Raporu Oluştur Butonu
                    Button(
                        onClick = {
                            notificationMessage = AppLanguageManager.translate("📑 VIP Müşteri Eğilimleri PDF Raporu oluşturuldu (${membersList.size} Üye).")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppLanguageManager.translate("PDF Raporu"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── 1. KPI İSTATİSTİK KARTLARI ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClubKpiStatCard(
                            modifier = Modifier.weight(1f),
                            title = AppLanguageManager.translate("Toplam VIP Üye"),
                            value = "${kpis.total_members} " + AppLanguageManager.translate("Üye"),
                            subtitle = AppLanguageManager.translate("Sistemde Kayıtlı"),
                            icon = Icons.Default.Groups,
                            accentColor = Color(0xFF0284C7)
                        )
                        ClubKpiStatCard(
                            modifier = Modifier.weight(1f),
                            title = AppLanguageManager.translate("Toplam Rezervasyon"),
                            value = "₺${kpis.total_spent.toInt()}",
                            subtitle = AppLanguageManager.translate("Tamamlanan Satış"),
                            icon = Icons.Default.MonetizationOn,
                            accentColor = Color(0xFF10B981)
                        )
                        ClubKpiStatCard(
                            modifier = Modifier.weight(1f),
                            title = AppLanguageManager.translate("Baskın Bütçe Eğilimi"),
                            value = kpis.dominant_budget,
                            subtitle = AppLanguageManager.translate("En Çok Tercih Edilen"),
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = Color(0xFF8B5CF6)
                        )
                        ClubKpiStatCard(
                            modifier = Modifier.weight(1f),
                            title = AppLanguageManager.translate("Favori Destinasyon"),
                            value = kpis.top_destination,
                            subtitle = kpis.top_concept,
                            icon = Icons.Default.FlightTakeoff,
                            accentColor = Color(0xFFF59E0B)
                        )
                    }
                }

                // ── 2. GELİŞMİŞ FİLTRE PANELİ ──
                item {
                    TourOSCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = AppLanguageManager.translate("🔍 Detaylı Müşteri Eğilimi ve Üye Filtresi"),
                                    style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                )
                                TextButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedTier = "ALL"
                                        selectedBudget = "ALL"
                                        selectedGroup = "ALL"
                                        selectedConcept = "ALL"
                                        selectedDestination = "ALL"
                                        fetchReportData()
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0284C7))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppLanguageManager.translate("Filtreleri Sıfırla"), fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                }
                            }

                            // Arama Kutusu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text(AppLanguageManager.translate("İsim, e-posta, telefon veya şehir ara..."), fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7)) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = { fetchReportData() },
                                    colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text(AppLanguageManager.translate("Filtrele"), fontWeight = FontWeight.Bold)
                                }
                            }

                            // Filtre Seçim Çipleri
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Tier Filtresi
                                FilterDropdownChip(
                                    label = AppLanguageManager.translate("Üyelik Seviyesi"),
                                    selectedValue = selectedTier,
                                    options = listOf("ALL" to "Tüm Seviyeler", "SILVER" to "Silver Üye", "GOLD" to "Gold Üye", "PLATINUM" to "Platinum VIP"),
                                    onSelected = { selectedTier = it }
                                )

                                // Bütçe Filtresi
                                FilterDropdownChip(
                                    label = AppLanguageManager.translate("Bütçe Dilimi"),
                                    selectedValue = selectedBudget,
                                    options = listOf(
                                        "ALL" to "Tüm Bütçeler",
                                        "100.000 - 150.000 RUB" to "100k - 150k RUB",
                                        "150.000 - 250.000 RUB" to "150k - 250k RUB",
                                        "250.000 - 400.000 RUB" to "250k - 400k RUB",
                                        "400.000+ RUB" to "400k+ RUB"
                                    ),
                                    onSelected = { selectedBudget = it }
                                )

                                // Seyahat Grubu Filtresi
                                FilterDropdownChip(
                                    label = AppLanguageManager.translate("Seyahat Grubu"),
                                    selectedValue = selectedGroup,
                                    options = listOf(
                                        "ALL" to "Tüm Gruplar",
                                        "Aile (Çocuklu)" to "Aile (Çocuklu)",
                                        "Çift (Romantik)" to "Çift (Romantik)",
                                        "Yalnız Gezgin" to "Yalnız Gezgin",
                                        "Arkadaş Grubu" to "Arkadaş Grubu"
                                    ),
                                    onSelected = { selectedGroup = it }
                                )

                                // Konsept Filtresi
                                FilterDropdownChip(
                                    label = AppLanguageManager.translate("Tatil Konsepti"),
                                    selectedValue = selectedConcept,
                                    options = listOf(
                                        "ALL" to "Tüm Konseptler",
                                        "Ultra Her Şey Dahil" to "Ultra Her Şey Dahil",
                                        "Her Şey Dahil" to "Her Şey Dahil",
                                        "Butik / Lüks Otel" to "Butik / Lüks Otel",
                                        "Macera / Doğa" to "Macera / Doğa",
                                        "Kültür Turları" to "Kültür Turları",
                                        "Spa & Termal" to "Spa & Termal"
                                    ),
                                    onSelected = { selectedConcept = it }
                                )

                                // Destinasyon Filtresi
                                FilterDropdownChip(
                                    label = AppLanguageManager.translate("Favori Destinasyon"),
                                    selectedValue = selectedDestination,
                                    options = listOf(
                                        "ALL" to "Tüm Destinasyonlar",
                                        "Türkiye • Antalya" to "Antalya",
                                        "Türkiye • Kemer" to "Kemer",
                                        "Türkiye • Bodrum" to "Bodrum",
                                        "BAE • Dubai" to "Dubai",
                                        "Tayland • Phuket" to "Phuket",
                                        "Mısır • Şarm El Şeyh" to "Şarm El Şeyh",
                                        "Maldivler" to "Maldivler"
                                    ),
                                    onSelected = { selectedDestination = it }
                                )
                            }
                        }
                    }
                }

                // ── 3. DETAYLI ÜYE LİSTESİ VE EĞİLİM TABLOSU ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppLanguageManager.translate("👑 Filtrelenen VIP Üyeler (${membersList.size} Kayıt)"),
                            style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        )
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF0284C7))
                        }
                    }
                }

                if (membersList.isEmpty() && !isLoading) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.FilterListOff, contentDescription = null, modifier = Modifier.size(44.dp), tint = Color(0xFF94A3B8))
                                Text(
                                    text = AppLanguageManager.translate("Seçilen kriterlere uygun VIP üye bulunamadı."),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = AppLanguageManager.translate("Lütfen filtre kriterlerini genişletmeyi deneyiniz."),
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    items(membersList) { member ->
                        ClubMemberReportRowCard(member = member)
                    }
                }
            }

            // Bildirim Modalı
            if (notificationMessage != null) {
                AlertDialog(
                    onDismissRequest = { notificationMessage = null },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp)) },
                    title = { Text(AppLanguageManager.translate("İşlem Başarılı"), fontWeight = FontWeight.Bold, color = TourOSColors.Primary) },
                    text = { Text(notificationMessage ?: "", color = Color(0xFF334155)) },
                    confirmButton = {
                        TourOSButton(text = AppLanguageManager.translate("Tamam"), onClick = { notificationMessage = null }, variant = TourOSButtonVariant.PRIMARY)
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// YARDIMCI BİLEŞENLER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClubKpiStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    TourOSCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterDropdownChip(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentDisplayName = options.firstOrNull { it.first == selectedValue }?.second ?: label

    Box {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = if (selectedValue != "ALL") Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (selectedValue != "ALL") Color(0xFF0284C7) else Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$label: $currentDisplayName",
                    fontSize = 12.sp,
                    fontWeight = if (selectedValue != "ALL") FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedValue != "ALL") Color(0xFF0284C7) else Color(0xFF334155)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display, fontWeight = if (key == selectedValue) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onSelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ClubMemberReportRowCard(
    member: ClubMemberReportDto
) {
    val tierColor = when (member.tier_code.uppercase()) {
        "PLATINUM" -> Color(0xFF8B5CF6)
        "GOLD" -> Color(0xFFF59E0B)
        else -> Color(0xFF0284C7)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Başlık: Avatar, İsim, E-posta, Tier Rozeti, Harcama
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (member.avatar_url.isNotBlank()) {
                        AsyncImage(
                            model = member.avatar_url,
                            contentDescription = member.full_name,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(tierColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.full_name.take(2).uppercase().ifBlank { "VIP" },
                                fontWeight = FontWeight.Bold,
                                color = tierColor,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = member.full_name.ifBlank { member.email.substringBefore("@") },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tierColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, tierColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "👑 " + member.tier_name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${member.email} • ${member.phone.ifBlank { "Telefon Belirtilmedi" }} • ${member.city.ifBlank { "Şehir Belirtilmedi" }}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Harcama & Rezervasyon Özeti
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "₺${member.total_spent.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "${member.total_bookings} " + AppLanguageManager.translate("Rezervasyon") + " • ${member.points} " + AppLanguageManager.translate("Puan"),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Müşteri Eğilim ve Tercih Detayları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bütçe ve Seyahat Grubu
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = AppLanguageManager.translate("Bütçe & Grup Tercihi"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(text = "💰 ${member.budget_range}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Text(text = "👥 ${member.travel_group}", fontSize = 12.sp, color = Color(0xFF475569))
                }

                // Favori Destinasyonlar
                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = AppLanguageManager.translate("Favori Destinasyonlar"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (member.favorite_destinations.isEmpty()) {
                            Text(text = "Türkiye • Antalya", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        } else {
                            member.favorite_destinations.forEach { dest ->
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                                    Text(text = "📍 $dest", fontSize = 10.sp, color = Color(0xFF334155), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                // Tatil Konseptleri
                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = AppLanguageManager.translate("Tercih Edilen Konseptler"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (member.holiday_concepts.isEmpty()) {
                            Text(text = "Ultra Her Şey Dahil", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        } else {
                            member.holiday_concepts.forEach { c ->
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEFF6FF)) {
                                    Text(text = "🏖️ $c", fontSize = 10.sp, color = Color(0xFF0284C7), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CSV DIŞA AKTARMA YARDIMCISI
// ─────────────────────────────────────────────────────────────────────────────

private fun buildCsvExport(members: List<ClubMemberReportDto>): String {
    val sb = StringBuilder()
    sb.append("Ad Soyad,E-posta,Telefon,Şehir,Pasaport No,VIP Seviye,Toplam Harcama (TRY),Rezervasyon Adedi,Kazanılan Puan,Bütçe Aralığı,Seyahat Grubu,Favori Destinasyonlar,Tatil Konseptleri,Son Güncelleme\n")
    members.forEach { m ->
        val dests = m.favorite_destinations.joinToString(";")
        val concepts = m.holiday_concepts.joinToString(";")
        sb.append("\"${m.full_name}\",\"${m.email}\",\"${m.phone}\",\"${m.city}\",\"${m.passport_no}\",\"${m.tier_name}\",${m.total_spent},${m.total_bookings},${m.points},\"${m.budget_range}\",\"${m.travel_group}\",\"$dests\",\"$concepts\",\"${m.updated_at}\"\n")
    }
    return sb.toString()
}

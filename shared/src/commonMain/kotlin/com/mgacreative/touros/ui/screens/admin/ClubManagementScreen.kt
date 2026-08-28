package com.mgacreative.touros.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.compose.koinInject

@Serializable
data class ClubVipSettingsDto(
    val id: String = "c10b0000-0000-0000-0000-000000000001",
    val silver_points_min: Int = 0,
    val gold_points_min: Int = 2000,
    val platinum_points_min: Int = 5000,
    val point_earning_rate: Double = 0.05,
    val hero_title: String = "Yaza Özel Fırsatlar",
    val hero_subtitle: String = "Erken rezervasyon fırsatlarını kaçırma! Axileto Club üyelerine özel ek indirimler.",
    val hero_image_url: String = "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80",
    val hero_button_text: String = "Teklifleri Keşfet",
    val is_active: Boolean = true
)

@Composable
fun ClubManagementScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToClubPreview: () -> Unit = {}
) {
    val supabaseClient: SupabaseClient = koinInject()
    val coroutineScope = rememberCoroutineScope()

    var heroTitle by remember { mutableStateOf("Yaza Özel Fırsatlar") }
    var heroSubtitle by remember { mutableStateOf("Erken rezervasyon fırsatlarını kaçırma! Axileto Club üyelerine özel ek indirimler.") }
    var heroImageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1600&q=80") }
    var heroButtonText by remember { mutableStateOf("Teklifleri Keşfet") }

    var silverPoints by remember { mutableStateOf("0") }
    var goldPoints by remember { mutableStateOf("2000") }
    var platinumPoints by remember { mutableStateOf("5000") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = supabaseClient.from("club_vip_settings")
                .select {
                    limit(1)
                }.decodeList<ClubVipSettingsDto>()

            val settings = response.firstOrNull()
            if (settings != null) {
                heroTitle = settings.hero_title
                heroSubtitle = settings.hero_subtitle
                heroImageUrl = settings.hero_image_url
                heroButtonText = settings.hero_button_text
                silverPoints = settings.silver_points_min.toString()
                goldPoints = settings.gold_points_min.toString()
                platinumPoints = settings.platinum_points_min.toString()
            }
        } catch (_: Exception) {
            // Varsayılan değerler kullanılır
        } finally {
            isLoading = false
        }
    }

    val handleSave = {
        coroutineScope.launch {
            isSaving = true
            statusMessage = null
            try {
                val updatePayload = buildJsonObject {
                    put("hero_title", heroTitle.trim())
                    put("hero_subtitle", heroSubtitle.trim())
                    put("hero_image_url", heroImageUrl.trim())
                    put("hero_button_text", heroButtonText.trim())
                    put("silver_points_min", silverPoints.toIntOrNull() ?: 0)
                    put("gold_points_min", goldPoints.toIntOrNull() ?: 2000)
                    put("platinum_points_min", platinumPoints.toIntOrNull() ?: 5000)
                }

                supabaseClient.from("club_vip_settings")
                    .update(updatePayload) {
                        filter {
                            eq("id", "c10b0000-0000-0000-0000-000000000001")
                        }
                    }

                statusMessage = AppLanguageManager.translate("Ayarlar başarıyla kaydedildi ✓")
            } catch (e: Exception) {
                statusMessage = AppLanguageManager.translate("Kaydedilirken hata oluştu:") + " ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("👑 Axileto Club VIP Yönetimi"),
                subtitle = AppLanguageManager.translate("VIP Dashboard Kampanya Bannerları, Sadakat Seviyeleri ve Puan Baremleri")
            )
        },
        containerColor = TourOSColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Canlı Durum / Bildirim Barı
            statusMessage?.let { msg ->
                val isSuccess = msg.contains("✓")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = msg,
                            style = TourOSTypography.BodyMedium.copy(
                                color = if (isSuccess) Color(0xFF047857) else Color(0xFFB91C1C),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // ── KART 1: VIP HERO KAMPANYA BANNER YÖNETİMİ ──
            TourOSCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🌴 " + AppLanguageManager.translate("VIP Kampanya & Hero Slider Yönetimi"),
                        style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                    )
                    Text(
                        text = AppLanguageManager.translate("VIP üyelerin ana ekranındaki sağ üst kampanya bannerında gösterilen başlık ve görseli güncelleyin."),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )

                    HorizontalDivider(color = TourOSColors.Border)

                    TourOSTextField(
                        value = heroTitle,
                        onValueChange = { heroTitle = it },
                        label = AppLanguageManager.translate("Banner Başlığı"),
                        placeholder = AppLanguageManager.translate("Yaza Özel Fırsatlar")
                    )

                    TourOSTextField(
                        value = heroSubtitle,
                        onValueChange = { heroSubtitle = it },
                        label = AppLanguageManager.translate("Banner Alt Metni / Slogan"),
                        placeholder = AppLanguageManager.translate("Erken rezervasyon fırsatlarını kaçırma!")
                    )

                    TourOSTextField(
                        value = heroImageUrl,
                        onValueChange = { heroImageUrl = it },
                        label = AppLanguageManager.translate("Banner Görsel URL (CDN / Supabase Storage)"),
                        placeholder = "https://..."
                    )

                    TourOSTextField(
                        value = heroButtonText,
                        onValueChange = { heroButtonText = it },
                        label = AppLanguageManager.translate("Aksiyon Butonu Metni (CTA)"),
                        placeholder = AppLanguageManager.translate("Teklifleri Keşfet")
                    )
                }
            }

            // ── KART 2: VIP SADAKAT VE PUAN BAREMLERİ ──
            TourOSCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🎖️ " + AppLanguageManager.translate("VIP Sadakat Seviyeleri (Tier Baremleri)"),
                        style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                    )
                    Text(
                        text = AppLanguageManager.translate("Müşterilerin rezervasyon harcamalarıyla otomatik kazanacakları VIP statü puan eşikleri."),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )

                    HorizontalDivider(color = TourOSColors.Border)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = silverPoints,
                                onValueChange = { silverPoints = it },
                                label = AppLanguageManager.translate("Silver Üye Min Puan"),
                                placeholder = "0"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = goldPoints,
                                onValueChange = { goldPoints = it },
                                label = AppLanguageManager.translate("Gold Üye Min Puan"),
                                placeholder = "2000"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = platinumPoints,
                                onValueChange = { platinumPoints = it },
                                label = AppLanguageManager.translate("Platinum VIP Min Puan"),
                                placeholder = "5000"
                            )
                        }
                    }
                }
            }

            // ── AKSİYON BUTONLARI ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSButton(
                    text = "👑 " + AppLanguageManager.translate("Club Portalını Önizle"),
                    onClick = onNavigateToClubPreview,
                    variant = TourOSButtonVariant.SECONDARY
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TourOSButton(
                        text = AppLanguageManager.translate("İptal"),
                        onClick = onNavigateBack,
                        variant = TourOSButtonVariant.TERTIARY
                    )
                    TourOSButton(
                        text = if (isSaving) AppLanguageManager.translate("Kaydediliyor...") else AppLanguageManager.translate("Değişiklikleri Kaydet ✓"),
                        onClick = { handleSave() },
                        enabled = !isSaving
                    )
                }
            }
        }
    }
}

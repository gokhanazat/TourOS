package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.CampaignCouponViewModel

private data class CampaignItemData(
    val id: String,
    val title: String,
    val typeName: String,
    val typeIcon: String,
    val discountPercent: Int,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean
)

private val sampleCampaignList = listOf(
    CampaignItemData("c1", "Erken Rezervasyon İndirimi", "Erken Rezervasyon", "🎟️", 15, "01.08.2026", "31.08.2026", true),
    CampaignItemData("c2", "Yaz Sonu Flaş Fırsat", "Flaş İndirim", "⚡", 20, "15.08.2026", "25.08.2026", true),
    CampaignItemData("c3", "B2B Acente Özel İskontosu", "Acente İskontosu", "🏢", 10, "01.01.2026", "31.12.2026", true),
    CampaignItemData("c4", "Kış Sezonu Erken Kampanyası", "Sezon Sonu", "☀️", 25, "01.05.2026", "31.05.2026", false)
)

/**
 * Kampanya Yönetimi Ekranı — TourOS 0.3
 *
 * Kampanya kartları listesi (başlangıç/bitiş tarihi, tür rozeti).
 * Sağ üstte '+ Yeni Kampanya' butonu.
 * Yeni kampanya oluşturmak için modal form.
 */
@Composable
fun CampaignCouponScreen(
    viewModel: CampaignCouponViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var campaignList by remember { mutableStateOf(sampleCampaignList) }
    var isNewCampaignModalOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Kampanya Yönetimi",
                subtitle = "Aktif indirim kampanyaları ve promosyon kuponları",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    // SAĞ ÜSTTE '+ YENİ KAMPANYA' BUTONU (Strict Rule)
                    TourOSButton(
                        text = "+ Yeni Kampanya",
                        onClick = { isNewCampaignModalOpen = true },
                        variant = TourOSButtonVariant.PRIMARY,
                        modifier = Modifier.padding(end = TourOSSpacing.small)
                    )
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isExpanded = maxWidth >= 768.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bildirim Mesajı
                if (state.notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }

                // KAMPANYA KARTLARI LİSTESİ Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📋 Kampanya Listesi (${campaignList.size})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )

                    TourOSStatusBadge(
                        text = "${campaignList.count { it.isActive }} Aktif",
                        backgroundColor = TourOSColors.SuccessContainer,
                        textColor = TourOSColors.Success
                    )
                }

                // KAMPANYA KARTLARI LİSTESİ
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(campaignList) { campaign ->
                        CampaignCardItem(campaign = campaign)
                    }
                }
            }

            // ── YENİ KAMPANYA OLUŞTURMA MODAL FORMU ────────────────────────────
            if (isNewCampaignModalOpen) {
                NewCampaignModalDialog(
                    onDismiss = { isNewCampaignModalOpen = false },
                    onSave = { newCampaign ->
                        campaignList = listOf(newCampaign) + campaignList
                        isNewCampaignModalOpen = false
                    }
                )
            }
        }
    }
}

// ─── KAMPANYA KARTI BİLEŞENİ (BAŞLANGIÇ/BİTİŞ TARİHİ, TÜR ROZETİ) ─────────────

@Composable
private fun CampaignCardItem(campaign: CampaignItemData) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TÜR ROZETİ (Strict Rule: Erken Rezervasyon, Flaş Fırsat vb.)
                TourOSStatusBadge(
                    text = "${campaign.typeIcon} ${campaign.typeName}",
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )

                // AKTİF / DÜŞTÜ DURUMU
                TourOSStatusBadge(
                    text = if (campaign.isActive) "● AKTİF" else "○ SÜRESİ DOLDU",
                    backgroundColor = if (campaign.isActive) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer,
                    textColor = if (campaign.isActive) TourOSColors.Success else TourOSColors.Secondary
                )
            }

            Text(
                campaign.title,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "İndirim Oranı: %${campaign.discountPercent}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )

                Text(
                    "📅 ${campaign.startDate} - ${campaign.endDate}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    TourOSButton(
                        text = "✏️ Düzenle",
                        onClick = { },
                        variant = TourOSButtonVariant.TERTIARY
                    )
                    TourOSButton(
                        text = "📊 Raporu Gör",
                        onClick = { },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }
            }
        }
    }
}

// ─── YENİ KAMPANYA OLUŞTURMA MODAL FORMU DIALOGU ──────────────────────────────

@Composable
private fun NewCampaignModalDialog(
    onDismiss: () -> Unit,
    onSave: (CampaignItemData) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Erken Rezervasyon") }
    var selectedIcon by remember { mutableStateOf("🎟️") }
    var discountPercent by remember { mutableStateOf("15") }
    var startDate by remember { mutableStateOf("01.09.2026") }
    var endDate by remember { mutableStateOf("30.09.2026") }

    val campaignTypes = listOf(
        "Erken Rezervasyon" to "🎟️",
        "Flaş İndirim" to "⚡",
        "Acente İskontosu" to "🏢",
        "Sezon Sonu" to "☀️"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "➕ Yeni Kampanya Oluştur",
                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                TourOSTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Kampanya Adı",
                    placeholder = "Örn: Sonbahar Kapadokya VIP Fırsatı",
                    modifier = Modifier.fillMaxWidth()
                )

                // KAMPANYA TÜRÜ SEÇİMİ CHIPLERİ
                Text("Kampanya Türü Seçin:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    campaignTypes.forEach { (tName, tIcon) ->
                        val isSelected = selectedType == tName
                        OutlinedButton(
                            onClick = {
                                selectedType = tName
                                selectedIcon = tIcon
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                "$tIcon $tName",
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                                )
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    TourOSTextField(
                        value = discountPercent,
                        onValueChange = { discountPercent = it },
                        label = "İndirim (%)",
                        placeholder = "15",
                        modifier = Modifier.weight(1f)
                    )

                    TourOSTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = "Başlangıç Tarihi",
                        placeholder = "01.09.2026",
                        modifier = Modifier.weight(1f)
                    )
                }

                TourOSTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "Bitiş Tarihi",
                    placeholder = "30.09.2026",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TourOSButton(
                text = "💾 Kaydet & Yayınla",
                onClick = {
                    if (title.isNotBlank()) {
                        val newItem = CampaignItemData(
                            id = "c_${kotlin.random.Random.nextInt(100000, 999999)}",
                            title = title,
                            typeName = selectedType,
                            typeIcon = selectedIcon,
                            discountPercent = discountPercent.toIntOrNull() ?: 15,
                            startDate = startDate,
                            endDate = endDate,
                            isActive = true
                        )
                        onSave(newItem)
                    }
                },
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TourOSButton(
                text = "İptal",
                onClick = onDismiss,
                variant = TourOSButtonVariant.TERTIARY
            )
        },
        containerColor = TourOSColors.Surface,
        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
    )
}

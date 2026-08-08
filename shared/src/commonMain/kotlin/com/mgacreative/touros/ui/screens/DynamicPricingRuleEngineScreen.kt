package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.DynamicPricingRule
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DynamicPricingRuleEngineViewModel

/**
 * Dinamik Fiyat Kuralları Ekranı — TourOS 0.3
 *
 * Kural listesini öncelik sırasına göre sürükle-bırak / sıralanabilir liste olarak gösterir.
 * Sağda seçili kural için canlı fiyat simülasyon paneli (Responsive Row/Column).
 */
@Composable
fun DynamicPricingRuleEngineScreen(
    viewModel: DynamicPricingRuleEngineViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val eval = state.evaluationResult

    var rulesList by remember(state.rules) { mutableStateOf(state.rules) }
    var selectedRuleId by remember { mutableStateOf(state.rules.firstOrNull()?.ruleId ?: "") }


    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Dinamik Fiyat Kuralları",
                subtitle = "Öncelikli kural motoru ve canlı fiyat simülasyon paneli",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isExpanded = maxWidth >= 840.dp

            if (isExpanded) {
                // ── TABLET / DESKTOP (ÇİFT SÜTUN: SOL KURAL LİSTESİ, SAĞ SİMÜLASYON) ──────
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(TourOSSpacing.large),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
                ) {
                    // SOL SÜTUN: ÖNCELİK SIRALI KURAL LİSTESİ (%50 GENİŞLİK)
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        PriorityRuleHeader(ruleCount = rulesList.size)

                        PriorityRuleList(
                            rules = rulesList,
                            selectedRuleId = selectedRuleId,
                            onRuleSelect = { id -> selectedRuleId = id },
                            onMoveUp = { idx ->
                                if (idx > 0) {
                                    val mutable = rulesList.toMutableList()
                                    val item = mutable.removeAt(idx)
                                    mutable.add(idx - 1, item)
                                    rulesList = mutable
                                }
                            },
                            onMoveDown = { idx ->
                                if (idx < rulesList.size - 1) {
                                    val mutable = rulesList.toMutableList()
                                    val item = mutable.removeAt(idx)
                                    mutable.add(idx + 1, item)
                                    rulesList = mutable
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // SAĞ SÜTUN: CANLI FİYAT SİMÜLASYON PANELİ (%50 GENİŞLİK)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        LivePriceSimulationPanel(
                            selectedRule = rulesList.find { it.ruleId == selectedRuleId } ?: rulesList.firstOrNull(),

                            occupancyRate = state.occupancyRate,
                            selectedSeason = state.selectedSeason,
                            selectedAgencyTier = state.selectedAgencyTier,
                            selectedCountry = state.selectedCountry,
                            basePrice = eval.basePrice,
                            adjustedPrice = eval.adjustedPrice,
                            totalAdjustmentPercent = eval.totalAdjustmentPercent,
                            onOccupancyChange = { viewModel.updateOccupancyRate(it) },
                            onSeasonChange = { viewModel.updateSeason(it) },
                            onAgencyTierChange = { viewModel.updateAgencyTier(it) },
                            onCountryChange = { viewModel.updateCountry(it) }
                        )
                    }
                }
            } else {
                // ── MOBİL DÜZEN (SEKMELİ ROW VEYA DİKEY AKIŞ) ───────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    SecondaryTabRow(
                        selectedTabIndex = state.selectedTab,
                        containerColor = TourOSColors.Surface,
                        contentColor = TourOSColors.Primary
                    ) {
                        Tab(
                            selected = state.selectedTab == 0,
                            onClick = { viewModel.selectTab(0) }
                        ) {
                            Text(
                                "⚡ Fiyat Simülasyonu",
                                modifier = Modifier.padding(TourOSSpacing.medium),
                                style = TourOSTypography.Label.copy(
                                    color = if (state.selectedTab == 0) TourOSColors.Primary else TourOSColors.TextSecondary
                                )
                            )
                        }

                        Tab(
                            selected = state.selectedTab == 1,
                            onClick = { viewModel.selectTab(1) }
                        ) {
                            Text(
                                "📋 Kural Listesi (#${rulesList.size})",
                                modifier = Modifier.padding(TourOSSpacing.medium),
                                style = TourOSTypography.Label.copy(
                                    color = if (state.selectedTab == 1) TourOSColors.Primary else TourOSColors.TextSecondary
                                )
                            )
                        }
                    }

                    if (state.selectedTab == 0) {
                        // MOBİL TAB 0: CANLI FİYAT SİMÜLASYON PANELİ
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            LivePriceSimulationPanel(
                                selectedRule = rulesList.find { it.ruleId == selectedRuleId } ?: rulesList.firstOrNull(),

                                occupancyRate = state.occupancyRate,
                                selectedSeason = state.selectedSeason,
                                selectedAgencyTier = state.selectedAgencyTier,
                                selectedCountry = state.selectedCountry,
                                basePrice = eval.basePrice,
                                adjustedPrice = eval.adjustedPrice,
                                totalAdjustmentPercent = eval.totalAdjustmentPercent,
                                onOccupancyChange = { viewModel.updateOccupancyRate(it) },
                                onSeasonChange = { viewModel.updateSeason(it) },
                                onAgencyTierChange = { viewModel.updateAgencyTier(it) },
                                onCountryChange = { viewModel.updateCountry(it) }
                            )
                        }
                    } else {
                        // MOBİL TAB 1: SIRALANABİLİR KURAL LİSTESİ
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            PriorityRuleHeader(ruleCount = rulesList.size)

                            PriorityRuleList(
                                rules = rulesList,
                                selectedRuleId = selectedRuleId,
                                onRuleSelect = { id -> selectedRuleId = id },
                                onMoveUp = { idx ->
                                    if (idx > 0) {
                                        val mutable = rulesList.toMutableList()
                                        val item = mutable.removeAt(idx)
                                        mutable.add(idx - 1, item)
                                        rulesList = mutable
                                    }
                                },
                                onMoveDown = { idx ->
                                    if (idx < rulesList.size - 1) {
                                        val mutable = rulesList.toMutableList()
                                        val item = mutable.removeAt(idx)
                                        mutable.add(idx + 1, item)
                                        rulesList = mutable
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── ÖNCELİK SIRALI KURAL LİSTESİ ÜST BAŞLIĞI ────────────────────────────────

@Composable
private fun PriorityRuleHeader(ruleCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "📋 Öncelik Sıralı Kurallar ($ruleCount)",
            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
        )

        TourOSStatusBadge(
            text = "⚡ Öncelik Motoru Aktif",
            backgroundColor = TourOSColors.PrimaryContainer,
            textColor = TourOSColors.Primary
        )
    }
}

// ─── SIRALANABİLİR KURAL LİSTESİ (REORDERABLE PRIORITY RULE LIST) ──────────

@Composable
private fun PriorityRuleList(
    rules: List<DynamicPricingRule>,
    selectedRuleId: String,
    onRuleSelect: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(rules) { index, rule ->
            val isSelected = rule.ruleId == selectedRuleId
            val priorityBadge = "#${index + 1}"

            TourOSCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRuleSelect(rule.ruleId) },

                backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.4f) else TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SÜRÜKLE-BIRAK VE SIRA DEĞİŞTİRME BUTONLARI (Strict Rule)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "⋮⋮",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary)
                        )
                        IconButton(
                            onClick = { onMoveUp(index) },
                            enabled = index > 0,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("▲", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                        }
                        IconButton(
                            onClick = { onMoveDown(index) },
                            enabled = index < rules.size - 1,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("▼", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                        }
                    }

                    // KURAL DETAY KARTI
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TourOSStatusBadge(
                                text = "ÖNCELİK $priorityBadge",
                                backgroundColor = TourOSColors.Primary,
                                textColor = TourOSColors.OnPrimary
                            )

                            val sign = if (rule.priceAdjustmentPercent >= 0) "+" else ""
                            Text(
                                "$sign%${rule.priceAdjustmentPercent}",
                                style = TourOSTypography.TitleMedium.copy(
                                    color = if (rule.priceAdjustmentPercent >= 0) TourOSColors.Secondary else TourOSColors.Success
                                )
                            )
                        }

                        Text(
                            rule.ruleName,
                            style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                            Text(
                                "Sezon: ${rule.season}  ·  Doluluk: >%${rule.minOccupancyRate.toInt()}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── SAĞDA SEÇİLİ KURAL İÇİN CANLI FİYAT SİMÜLASYON PANELİ ────────────────────

@Composable
private fun LivePriceSimulationPanel(
    selectedRule: DynamicPricingRule?,
    occupancyRate: Double,
    selectedSeason: String,
    selectedAgencyTier: String,
    selectedCountry: String,
    basePrice: Double,
    adjustedPrice: Double,
    totalAdjustmentPercent: Double,
    onOccupancyChange: (Double) -> Unit,
    onSeasonChange: (String) -> Unit,
    onAgencyTierChange: (String) -> Unit,
    onCountryChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. CANLI SİMÜLASYON SONUÇ KARTI
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.Surface,
            contentPadding = TourOSSpacing.large
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⚡ Canlı Fiyat Simülasyonu",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )

                    TourOSStatusBadge(
                        text = "● SIMULATION LIVE",
                        backgroundColor = TourOSColors.SuccessContainer,
                        textColor = TourOSColors.Success
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Etkin Öncelikli Kural:",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                        Text(
                            selectedRule?.ruleName ?: "Varsayılan Fiyat Kuralı",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Baz Tur Fiyatı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatPriceMoney(basePrice)}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Net Değişim Oranı:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))

                    val sign = if (totalAdjustmentPercent >= 0) "+" else ""
                    Text(
                        "$sign%$totalAdjustmentPercent",
                        style = TourOSTypography.Label.copy(
                            color = if (totalAdjustmentPercent >= 0) TourOSColors.Secondary else TourOSColors.Success
                        )
                    )
                }

                HorizontalDivider(color = TourOSColors.Divider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dinamik Son Fiyat:", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                    Text(
                        "₺ ${formatPriceMoney(adjustedPrice)}",
                        style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                    )
                }
            }
        }

        // 2. PARAMETRE AYARLARI KARTI (DOLULUK, SEZON, ACENTE TIER)
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.Surface,
            contentPadding = TourOSSpacing.large
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Text(
                    "🎛️ Simülatör Koşul Parametreleri",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )

                Text(
                    "Doluluk Oranı: %${occupancyRate.toInt()}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )

                Slider(
                    value = occupancyRate.toFloat(),
                    onValueChange = { onOccupancyChange(it.toDouble()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = TourOSColors.Primary,
                        activeTrackColor = TourOSColors.Primary
                    )
                )

                // Sezon Seçim Chipleri
                Text("Sezon Türü:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    listOf("HIGH_SEASON" to "Yüksek", "MID_SEASON" to "Orta", "LOW_SEASON" to "Düşük").forEach { (code, label) ->
                        val isSelected = selectedSeason == code
                        OutlinedButton(
                            onClick = { onSeasonChange(code) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                label,
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                                )
                            )
                        }
                    }
                }

                // Acente Tier Chipleri
                Text("Acente Seviyesi:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    listOf("VIP_AGENCY" to "VIP Acente", "REGULAR_AGENCY" to "Standart", "ALL" to "Tümü").forEach { (code, label) ->
                        val isSelected = selectedAgencyTier == code
                        OutlinedButton(
                            onClick = { onAgencyTierChange(code) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = TourOSColors.PrimaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                label,
                                style = TourOSTypography.Caption.copy(
                                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPriceMoney(amount: Double): String {

    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

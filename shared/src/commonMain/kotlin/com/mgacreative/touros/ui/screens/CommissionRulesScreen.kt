package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.mgacreative.touros.domain.model.CommissionRule
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.CommissionRulesUiState
import com.mgacreative.touros.ui.viewmodel.CommissionRulesViewModel

/**
 * Komisyon Ayarları & Kuralları Ekranı — TourOS 0.3
 *
 * Kural listesi tablo halinde gösterilir.
 * Yeni kural eklemek için modal form (Oran / Sabit Tutar seçimi RadioButton ile yapılmıştır).
 */
@Composable
fun CommissionRulesScreen(
    viewModel: CommissionRulesViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Komisyon Ayarları",
                subtitle = "Acente ve tur komisyon kuralları motoru",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is CommissionRulesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is CommissionRulesUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is CommissionRulesUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── 1. Canlı Simülatör Kartı ──────────────────────────
                        item {
                            CommissionSimulatorCard(
                                rules = state.rules,
                                simulatedResultText = state.simulatedResultText,
                                onSimulate = { price, rule -> viewModel.simulateCommission(price, rule) }
                            )
                        }

                        // ── 2. Başlık ve Buton ───────────────────────────────
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "📋 Yapılandırılmış Komisyon Kuralları (${state.rules.size})",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )
                                TourOSButton(
                                    text = "+ Yeni Kural Ekle",
                                    onClick = { showCreateDialog = true },
                                    variant = TourOSButtonVariant.PRIMARY
                                )
                            }
                        }

                        // ── 3. Tablo veya Kart Listesi ──────────────────────────
                        if (state.rules.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Tanımlı komisyon kuralı bulunamadı.",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (isExpanded) {
                            // Expanded: Kural Listesi Tablosu
                            item {
                                CommissionRulesTable(
                                    rules = state.rules,
                                    onSimulateClick = { viewModel.simulateCommission(10000.0, it) }
                                )
                            }
                        } else {
                            // Compact: Kart Listesi
                            items(state.rules) { rule ->
                                CommissionRuleCard(
                                    rule = rule,
                                    onSimulateClick = { viewModel.simulateCommission(10000.0, rule) }
                                )
                            }
                        }
                    }

                    // ── 4. Yeni Kural Ekleme Modal Formu (RadioButton Seçimli) ──
                    if (showCreateDialog) {
                        CreateCommissionRuleDialog(
                            onDismiss = { showCreateDialog = false },
                            onCreate = { name, agent, tour, type, rate, fixed ->
                                viewModel.saveNewRule(name, agent, tour, type, rate, fixed)
                                showCreateDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Canlı Komisyon Hesaplama Simülatörü ─────────────────────────────────────

@Composable
private fun CommissionSimulatorCard(
    rules: List<CommissionRule>,
    simulatedResultText: String?,
    onSimulate: (price: Double, rule: CommissionRule) -> Unit
) {
    var priceStr by remember { mutableStateOf("10000") }
    val activeRule = rules.firstOrNull()

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "🧮 Canlı Komisyon Simülatörü",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = "Satış Tutarı (₺)",
                    placeholder = "10000",
                    modifier = Modifier.weight(1f)
                )

                TourOSButton(
                    text = "Hesapla",
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: 0.0
                        if (activeRule != null) onSimulate(p, activeRule)
                    },
                    variant = TourOSButtonVariant.PRIMARY
                )
            }

            if (simulatedResultText != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.SuccessContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Text(
                        simulatedResultText,
                        style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                    )
                }
            }
        }
    }
}

// ─── Expanded: Kural Listesi Tablosu ──────────────────────────────────────────

@Composable
private fun CommissionRulesTable(
    rules: List<CommissionRule>,
    onSimulateClick: (CommissionRule) -> Unit
) {
    val headers = listOf("Kural Adı", "Acente / Hedef", "Hesaplama Türü", "Komisyon Değeri", "Durum", "İşlem")
    val weights = listOf(1.5f, 1.4f, 1.2f, 1.1f, 0.9f, 1.0f)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Primary)
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        h,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                        modifier = Modifier.weight(weights[i])
                    )
                }
            }

            // Satırlar
            rules.forEachIndexed { idx, r ->
                val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                val isPercentage = r.calculationType == "percentage"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kural Adı
                    Text(r.ruleName, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[0]))

                    // Acente / Hedef
                    Text(
                        text = r.agentName ?: r.tourName ?: "🌐 Genel Kural",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.weight(weights[1])
                    )

                    // Hesaplama Türü Badgesi
                    Box(modifier = Modifier.weight(weights[2])) {
                        TourOSStatusBadge(
                            text = if (isPercentage) "% Oranlı" else "₺ Sabit Tutar",
                            backgroundColor = if (isPercentage) TourOSColors.PrimaryContainer else TourOSColors.SecondaryContainer,
                            textColor = if (isPercentage) TourOSColors.Primary else TourOSColors.Secondary
                        )
                    }

                    // Değer
                    Text(
                        text = if (isPercentage) "%${r.rateValue}" else "₺ ${r.fixedAmount}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                        modifier = Modifier.weight(weights[3])
                    )

                    // Durum
                    Box(modifier = Modifier.weight(weights[4])) {
                        TourOSStatusBadge(
                            text = if (r.isActive) "Aktif" else "Pasif",
                            backgroundColor = if (r.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                            textColor = if (r.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                        )
                    }

                    // İşlem
                    Row(modifier = Modifier.weight(weights[5])) {
                        TourOSButton("Test Et", { onSimulateClick(r) }, variant = TourOSButtonVariant.TERTIARY)
                    }
                }

                if (idx < rules.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ─── Compact: Kural Kartı ─────────────────────────────────────────────────────

@Composable
private fun CommissionRuleCard(
    rule: CommissionRule,
    onSimulateClick: () -> Unit
) {
    val isPercentage = rule.calculationType == "percentage"

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(rule.ruleName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                TourOSStatusBadge(
                    text = if (isPercentage) "%${rule.rateValue} Oran" else "₺ ${rule.fixedAmount} Sabit",
                    backgroundColor = if (isPercentage) TourOSColors.PrimaryContainer else TourOSColors.SecondaryContainer,
                    textColor = if (isPercentage) TourOSColors.Primary else TourOSColors.Secondary
                )
            }

            Text(
                "Hedef: ${rule.agentName ?: rule.tourName ?: "🌐 Genel Kural (Tüm Rezervasyonlar)"}",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSStatusBadge(
                    text = "🟢 Aktif",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )

                TourOSButton("🧮 Test Et", onSimulateClick, variant = TourOSButtonVariant.TERTIARY)
            }
        }
    }
}

// ─── MODAL FORM (Radio Button ile Oran / Sabit Tutar Seçimi) ──────────────────

@Composable
private fun CreateCommissionRuleDialog(
    onDismiss: () -> Unit,
    onCreate: (ruleName: String, agentName: String?, tourName: String?, calculationType: String, rateValue: Double, fixedAmount: Double) -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var agentName by remember { mutableStateOf("") }
    var tourName by remember { mutableStateOf("") }
    var calculationType by remember { mutableStateOf("percentage") } // "percentage" veya "fixed_amount"
    var valueStr by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "➕ Yeni Komisyon Kuralı Ekle",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = "Kural Adı",
                    placeholder = "Örn: Acente X Özel Oranı",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    label = "Acente Adı (Opsiyonel)",
                    placeholder = "Örn: Jolly Tur",
                    modifier = Modifier.fillMaxWidth()
                )

                TourOSTextField(
                    value = tourName,
                    onValueChange = { tourName = it },
                    label = "Tur Adı (Opsiyonel)",
                    placeholder = "Örn: Kapadokya Balon Turu",
                    modifier = Modifier.fillMaxWidth()
                )

                // RADIO BUTTON SEÇİMİ (ORAN / SABİT TUTAR)
                Text(
                    "Hesaplama Tipi Seçimi:",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                )

                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { calculationType = "percentage" }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = calculationType == "percentage",
                            onClick = { calculationType = "percentage" },
                            colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary)
                        )
                        Spacer(Modifier.width(TourOSSpacing.xSmall))
                        Text(
                            "% Oranlı Komisyon (Yüzde Hesaplama)",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { calculationType = "fixed_amount" }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = calculationType == "fixed_amount",
                            onClick = { calculationType = "fixed_amount" },
                            colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary)
                        )
                        Spacer(Modifier.width(TourOSSpacing.xSmall))
                        Text(
                            "₺ Sabit Tutar Komisyonu (Sabit TL)",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                        )
                    }
                }

                // Değer Giriş Alanı
                TourOSTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = if (calculationType == "percentage") "Komisyon Oranı (%)" else "Sabit Komisyon Tutarı (₺)",
                    placeholder = if (calculationType == "percentage") "10.0" else "500.0",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TourOSButton(
                text = "💾 Kuralı Kaydet",
                onClick = {
                    val v = valueStr.toDoubleOrNull() ?: 0.0
                    val rate = if (calculationType == "percentage") v else 0.0
                    val fixed = if (calculationType == "fixed_amount") v else 0.0
                    onCreate(ruleName, agentName.ifBlank { null }, tourName.ifBlank { null }, calculationType, rate, fixed)
                },
                enabled = ruleName.isNotBlank() && valueStr.toDoubleOrNull() != null,
                variant = TourOSButtonVariant.PRIMARY
            )
        },
        dismissButton = {
            TourOSButton(
                text = "İptal",
                onClick = onDismiss,
                variant = TourOSButtonVariant.SECONDARY
            )
        }
    )
}

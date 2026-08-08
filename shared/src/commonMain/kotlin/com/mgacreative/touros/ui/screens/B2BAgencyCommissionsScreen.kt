package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.mgacreative.touros.domain.model.B2BAgencyCommissionItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2BAgencyCommissionsViewModel

private data class PeriodFilterOption(val key: String, val label: String)

private val periodOptions = listOf(
    PeriodFilterOption("30_DAYS", "📅 Son 30 Gün"),
    PeriodFilterOption("THIS_MONTH", "🗓️ Bu Ay"),
    PeriodFilterOption("THIS_YEAR", "📊 Bu Yıl"),
    PeriodFilterOption("ALL", "🌐 Tüm Dönemler")
)

/**
 * B2B Komisyon Görüntüleme Ekranı — TourOS 0.3
 *
 * Üstte Dönemi Değiştirmek İçin Tarih Aralığı Seçici.
 * Üstte Toplam Komisyon Özet Kartı (Ödenecek, Ödenen, Bekleyen).
 * Altta Tur/Dönem Bazlı Komisyon Tablosu / Kart Listesi.
 */
@Composable
fun B2BAgencyCommissionsScreen(
    viewModel: B2BAgencyCommissionsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var selectedPeriodFilter by remember { mutableStateOf("THIS_MONTH") }

    val filteredCommissions = remember(state.commissions, selectedPeriodFilter) {
        if (selectedPeriodFilter == "ALL") state.commissions
        else state.commissions
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "B2B Komisyon Dökümü",
                subtitle = "Tur ve dönem bazlı acente kazanç takibi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isExpanded = maxWidth >= 768.dp

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ── 1. ÜSTTE TARİH ARALIĞI SEÇİCİ (DÖNEM FİLTRESİ) ────────────
                item {
                    TopPeriodFilterBar(
                        selectedPeriod = selectedPeriodFilter,
                        onPeriodSelect = { selectedPeriodFilter = it }
                    )
                }

                // ── 2. ÜSTTE TOPLAM KOMİSYON ÖZET KARTI ─────────────────────
                item {
                    TotalCommissionSummaryCard(
                        totalEarned = state.totalEarnedCommission,
                        paidAmount = state.paidCommission,
                        pendingAmount = state.pendingCommission
                    )
                }

                // ── 3. ALTA TUR / DÖNEM BAZLI TABLO VEYA KART LİSTESİ ─────────
                item {
                    Text(
                        "📋 Tur Bazlı Komisyon Dökümü (${filteredCommissions.size})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }

                if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TourOSColors.Primary)
                        }
                    }
                } else if (filteredCommissions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Seçili dönem için komisyon kaydı bulunamadı.",
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }
                } else if (isExpanded) {
                    // Expanded: Tablo Düzeni
                    item {
                        CommissionDetailsTable(commissions = filteredCommissions)
                    }
                } else {
                    // Compact: Kart Listesi
                    items(filteredCommissions) { item ->
                        CommissionItemCard(item = item)
                    }
                }
            }
        }
    }
}

// ─── Üstte Tarih Aralığı Seçici (Dönem Filtre Barı) ───────────────────────────

@Composable
private fun TopPeriodFilterBar(
    selectedPeriod: String,
    onPeriodSelect: (String) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                "📅 Hakediş Dönemi Seçin",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                items(periodOptions) { opt ->
                    FilterChip(
                        selected = selectedPeriod == opt.key,
                        onClick = { onPeriodSelect(opt.key) },
                        label = { Text(opt.label, style = TourOSTypography.Caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TourOSColors.Primary,
                            selectedLabelColor = TourOSColors.OnPrimary
                        )
                    )
                }
            }
        }
    }
}

// ─── Üstte Toplam Komisyon Özet Kartı ─────────────────────────────────────────

@Composable
private fun TotalCommissionSummaryCard(
    totalEarned: Double,
    paidAmount: Double,
    pendingAmount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // Toplam Kazanılan Komisyon
        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.PrimaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "₺ ${formatMoney(totalEarned)}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
                Text(
                    "Toplam Kazanılan",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary.copy(alpha = 0.8f))
                )
            }
        }

        // Ödenen Komisyon
        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.SuccessContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "₺ ${formatMoney(paidAmount)}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Success)
                )
                Text(
                    "Hesaba Ödenen",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Success.copy(alpha = 0.8f))
                )
            }
        }

        // Bekleyen Hak Ediş
        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.SecondaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "₺ ${formatMoney(pendingAmount)}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary)
                )
                Text(
                    "Bekleyen Hak Ediş",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary.copy(alpha = 0.8f))
                )
            }
        }
    }
}

// ─── Expanded: Tur / Dönem Bazlı Komisyon Tablosu ────────────────────────────

@Composable
private fun CommissionDetailsTable(commissions: List<B2BAgencyCommissionItem>) {
    val headers = listOf("Tur Adı", "Dönem", "Satış Adedi", "Brüt Ciro (₺)", "Komisyon (%)", "Kazanılan Net (₺)", "Durum")
    val weights = listOf(1.6f, 1.0f, 0.9f, 1.1f, 1.0f, 1.2f, 1.0f)

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
            commissions.forEachIndexed { idx, item ->
                val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                val isPaid = item.status == "ODENDI"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.tourTitle, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[0]))
                    Text(item.periodName ?: "Ağustos 2026", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(weights[1]))
                    Text("${item.bookingCount} Pax", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[2]))
                    Text("₺ ${formatMoney(item.grossSalesAmount)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[3]))
                    Text("%${item.commissionRate}", style = TourOSTypography.Label.copy(color = TourOSColors.Secondary), modifier = Modifier.weight(weights[4]))
                    Text("₺ ${formatMoney(item.commissionAmount)}", style = TourOSTypography.Label.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[5]))
                    Box(modifier = Modifier.weight(weights[6])) {
                        TourOSStatusBadge(
                            text = if (isPaid) "Ödendi" else "Bekliyor",
                            backgroundColor = if (isPaid) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer,
                            textColor = if (isPaid) TourOSColors.Success else TourOSColors.Secondary
                        )
                    }
                }

                if (idx < commissions.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ─── Compact: Komisyon İtem Kartı ──────────────────────────────────────────────

@Composable
private fun CommissionItemCard(item: B2BAgencyCommissionItem) {
    val isPaid = item.status == "ODENDI"

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.tourTitle,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )

                TourOSStatusBadge(
                    text = if (isPaid) "✅ ÖDENDİ" else "⏳ BEKLİYOR",
                    backgroundColor = if (isPaid) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer,
                    textColor = if (isPaid) TourOSColors.Success else TourOSColors.Secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Satış Adedi: ${item.bookingCount} Rezervasyon",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    "Komisyon Oranı: %${item.commissionRate}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Top. Brüt Ciro", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(item.grossSalesAmount)}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Kazanılan Net Komisyon", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(item.commissionAmount)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.CustomerCrmDetail
import com.mgacreative.touros.ui.viewmodel.CustomerSegmentationViewModel
import com.mgacreative.touros.ui.viewmodel.SegmentCategory

/**
 * Müşteri Segmentleri & CRM Ekranı — TourOS Canlı Veri Sürümü
 *
 * Veritabanındaki gerçek rezervasyon ve müşteri verilerine dayalı dinamik segmentasyon.
 * Segment kartları (canlı sayılar ve ortalama LTV) ve arama yapılabilir müşteri detay listesi.
 */
@Composable
fun CustomerSegmentationScreen(
    viewModel: CustomerSegmentationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    val selectedSegment = state.segments.find { it.id == state.selectedSegmentId } ?: state.segments.firstOrNull()

    val filteredCustomers = remember(selectedSegment, state.searchQuery) {
        val list = selectedSegment?.customers ?: emptyList()
        val q = state.searchQuery.trim().lowercase()
        if (q.isBlank()) list
        else list.filter {
            it.name.lowercase().contains(q) ||
            it.email.lowercase().contains(q) ||
            it.phone.lowercase().contains(q)
        }
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri Segmentleri & CRM"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kullanıcı davranışlarına göre canlı segmentasyon ve müşteri dökümü"),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val gridColumns = if (maxWidth >= 1024.dp) 3 else if (maxWidth >= 600.dp) 2 else 1

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // BAŞLIK VE ÖZET SAYICI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📊 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri Segment Kartları")} (${state.segments.size})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )

                    TourOSStatusBadge(
                        text = "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam")} ${state.totalCustomerCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri")}",
                        backgroundColor = TourOSColors.PrimaryContainer,
                        textColor = TourOSColors.Primary
                    )
                }

                // ── 1. DİNAMİK SEGMENT KARTLARI GRID'İ ──────────────────────────
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                } else if (state.segments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Henüz sisteme kayıtlı müşteri veya rezervasyon bulunamadı."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        items(state.segments) { segment ->
                            SegmentGridCardItem(
                                segment = segment,
                                isSelected = segment.id == state.selectedSegmentId,
                                onClick = { viewModel.selectSegment(segment.id) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = TourOSColors.Divider)

                // ── 2. SEÇİLİ SEGMENT MÜŞTERİ TABLOSU VE ARAMA ─────────────────
                if (selectedSegment != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedSegment.icon} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(selectedSegment.name)} (${filteredCustomers.size} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Gösteriliyor")})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )

                        TourOSTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri Ara..."),
                            modifier = Modifier.width(260.dp)
                        )
                    }

                    if (filteredCustomers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bu segmentte veya arama kriterinde müşteri bulunmuyor."),
                                style = TourOSTypography.BodyMedium,
                                color = TourOSColors.TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredCustomers) { customer ->
                                DynamicCustomerRowCard(customer = customer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentGridCardItem(
    segment: SegmentCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.5f) else TourOSColors.Background,
        borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(segment.icon, style = TourOSTypography.TitleMedium)
                    Text(
                        com.mgacreative.touros.ui.localization.AppLanguageManager.translate(segment.name),
                        style = TourOSTypography.BodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.TextPrimary
                    )
                }

                TourOSStatusBadge(
                    text = "${segment.customerCount} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri")}",
                    backgroundColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface,
                    textColor = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                )
            }

            Text(
                com.mgacreative.touros.ui.localization.AppLanguageManager.translate(segment.description),
                style = TourOSTypography.Caption,
                color = TourOSColors.TextSecondary,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ort. LTV")}: ${formatLtvCurrency(segment.avgLtv)} ₺",
                    style = TourOSTypography.Caption,
                    color = TourOSColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    if (isSelected) "● ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("AÇIK")}" else "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tabloyu Aç")} →",
                    style = TourOSTypography.Caption,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DynamicCustomerRowCard(customer: CustomerCrmDetail) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: Müşteri Bilgileri
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xxSmall)) {
                Text(
                    text = customer.name,
                    style = TourOSTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customer.email.isNotBlank() && customer.email != "-") {
                        Text("✉ ${customer.email}", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                    }
                    if (customer.phone.isNotBlank() && customer.phone != "-") {
                        Text("📞 ${customer.phone}", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tamamlanan")}: ${customer.totalBookings} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate(customer.bookingTypeStr)} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Rezervasyonu")}",
                        style = TourOSTypography.Caption,
                        color = TourOSColors.TextPrimary
                    )
                    Text(
                        "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Son İşlem")}: ${customer.lastActivityDate}",
                        style = TourOSTypography.Caption,
                        color = TourOSColors.TextSecondary
                    )
                }
            }

            // Sağ Taraf: LTV Tutarı ve Aksiyon Butonları
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("LTV (Toplam Tutar)"), style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                    Text(
                        "${formatLtvCurrency(customer.ltvAmount)} ₺",
                        style = TourOSTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    TourOSButton(
                        text = "📞 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ara")}",
                        onClick = { /* Ara aksiyonu */ },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                    TourOSButton(
                        text = "💬 WhatsApp",
                        onClick = { /* WhatsApp aksiyonu */ },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

private fun formatLtvCurrency(value: Double): String {
    val longVal = value.toLong()
    return if (value % 1.0 == 0.0) {
        longVal.toString()
    } else {
        val whole = (value.toInt()).toString()
        val decimal = ((value - value.toInt()) * 100).toInt()
        "$whole.${if (decimal < 10) "0$decimal" else decimal}"
    }
}

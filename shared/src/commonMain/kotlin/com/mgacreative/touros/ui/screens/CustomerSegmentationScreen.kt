package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

private data class CustomerSegmentItem(
    val id: String,
    val name: String,
    val icon: String,
    val customerCount: Int,
    val description: String,
    val avgLtv: Double,
    val badgeColor: Color,
    val textColor: Color
)

private data class SegmentCustomerDetail(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalBookings: Int,
    val ltvAmount: Double,
    val lastActivityDate: String
)

private val sampleSegments = listOf(
    CustomerSegmentItem("seg-1", "VIP Sadık Müşteriler", "👑", 142, "5+ tamamlanan tur ve yüksek memnuniyet skoru", 85400.0, TourOSColors.PrimaryContainer, TourOSColors.Primary),
    CustomerSegmentItem("seg-2", "Yüksek Harcamalı Gezginler", "💎", 88, "LTV > ₺100,000 olan premium paket alıcıları", 124000.0, TourOSColors.SecondaryContainer, TourOSColors.Secondary),
    CustomerSegmentItem("seg-3", "Balon & Doğa Tutkunları", "🎈", 215, "Macera, balon ve doğa turlarını tercih edenler", 62000.0, TourOSColors.SuccessContainer, TourOSColors.Success),
    CustomerSegmentItem("seg-4", "Yeni Kayıt Olanlar", "🌟", 310, "Son 30 gün içinde platforma üye olan yeni gezginler", 14500.0, TourOSColors.PrimaryContainer, TourOSColors.Primary),
    CustomerSegmentItem("seg-5", "Riskli / İnaktif Müşteriler", "⚠️", 34, "6+ aydır yeni rezervasyon yapmayan kitle", 32000.0, TourOSColors.SecondaryContainer, TourOSColors.Secondary),
    CustomerSegmentItem("seg-6", "B2B Kurumsal İrtibatlar", "🏢", 64, "Acente temsilcileri ve VIP grup yetkilileri", 195000.0, TourOSColors.SuccessContainer, TourOSColors.Success)
)

private val sampleSegmentCustomers = mapOf(
    "seg-1" to listOf(
        SegmentCustomerDetail("c101", "Elif Yılmaz", "elif.yilmaz@email.com", "+90 532 111 2233", 8, 98400.0, "15.07.2026"),
        SegmentCustomerDetail("c102", "Ahmet Kaya", "ahmet.kaya@email.com", "+90 533 222 3344", 6, 78200.0, "02.08.2026"),
        SegmentCustomerDetail("c103", "Zeynep Demir", "zeynep.demir@email.com", "+90 535 444 5566", 7, 86000.0, "28.06.2026")
    ),
    "seg-2" to listOf(
        SegmentCustomerDetail("c201", "Mehmet Öztürk", "mehmet.ozturk@email.com", "+90 542 333 4455", 11, 145000.0, "01.08.2026"),
        SegmentCustomerDetail("c202", "Selin Arslan", "selin.arslan@email.com", "+90 544 555 6677", 9, 118000.0, "22.07.2026")
    ),
    "seg-3" to listOf(
        SegmentCustomerDetail("c301", "Burak Celik", "burak.celik@email.com", "+90 536 777 8899", 4, 54000.0, "05.08.2026"),
        SegmentCustomerDetail("c302", "Ayşe Polat", "ayse.polat@email.com", "+90 538 999 0011", 5, 68000.0, "18.07.2026")
    )
)

/**
 * Müşteri Segmentleri Ekranı — TourOS 0.3
 *
 * Segment kartları grid'i (segment adı + müşteri sayısı).
 * Karta tıklayınca o segmentteki müşteri tablosu açılır.
 */
@Composable
fun CustomerSegmentationScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedSegment by remember { mutableStateOf(sampleSegments.first()) }
    var searchQuery by remember { mutableStateOf("") }

    val customersForSelectedSegment = remember(selectedSegment, searchQuery) {
        val list = sampleSegmentCustomers[selectedSegment.id] ?: sampleSegmentCustomers["seg-1"] ?: emptyList()
        if (searchQuery.isBlank()) list
        else list.filter { it.name.lowercase().contains(searchQuery.lowercase()) || it.email.lowercase().contains(searchQuery.lowercase()) }
    }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Müşteri Segmentleri & CRM",
                subtitle = "Kullanıcı davranışlarına göre segmentasyonu ve müşteri dökümü",
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
                        "📊 Müşteri Segment Kartları (${sampleSegments.size})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )

                    TourOSStatusBadge(
                        text = "Toplam 853 Müşteri",
                        backgroundColor = TourOSColors.PrimaryContainer,
                        textColor = TourOSColors.Primary
                    )
                }

                // ── 1. SEGMENT KARTLARI GRID'İ (SEGMENT ADI + MÜŞTERİ SAYISI) (Strict Rule) ──
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(sampleSegments) { segment ->
                        SegmentGridCardItem(
                            segment = segment,
                            isSelected = segment.id == selectedSegment.id,
                            onClick = { selectedSegment = segment }
                        )
                    }
                }

                HorizontalDivider(color = TourOSColors.Divider)

                // ── 2. KARTA TIKLAYINCA AÇILAN SEGMENT MÜŞTERİ TABLOSU ───────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedSegment.icon, style = TourOSTypography.TitleLarge)
                        Text(
                            "${selectedSegment.name} (${customersForSelectedSegment.size} Gösteriliyor)",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }

                    TourOSTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Müşteri Ara...",
                        modifier = Modifier.width(220.dp)
                    )
                }

                // SEGMENT MÜŞTERİ TABLOSU LİSTESİ
                if (customersForSelectedSegment.isEmpty()) {
                    TourOSEmptyState(
                        title = "Segment Müşterisi Bulunamadı",
                        description = "Bu segmentte henüz kayıtlı müşteri verisi bulunmuyor.",
                        icon = { Text(selectedSegment.icon, style = TourOSTypography.DisplaySmall) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(customersForSelectedSegment) { customer ->
                            CustomerRowCardItem(customer = customer)
                        }
                    }
                }
            }
        }
    }
}

// ─── SEGMENT KARTI GRID İTEMİ (Strict Rule: Segment Adı + Müşteri Sayısı) ───────

@Composable
private fun SegmentGridCardItem(
    segment: CustomerSegmentItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.45f) else TourOSColors.Surface,
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
                        segment.name,
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )
                }

                // MÜŞTERİ SAYISI ROZETİ (Strict Rule)
                TourOSStatusBadge(
                    text = "${segment.customerCount} Müşteri",
                    backgroundColor = segment.badgeColor,
                    textColor = segment.textColor
                )
            }

            Text(
                segment.description,
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ort. LTV: ₺ ${formatSegMoney(segment.avgLtv)}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                )

                Text(
                    if (isSelected) "● AÇIK" else "Tabloyu Aç →",
                    style = TourOSTypography.Caption.copy(
                        color = if (isSelected) TourOSColors.Primary else TourOSColors.TextSecondary
                    )
                )
            }
        }
    }
}

// ─── SEGMENT MÜŞTERİ TABLOSU SATIR BİLEŞENİ ──────────────────────────────────

@Composable
private fun CustomerRowCardItem(customer: SegmentCustomerDetail) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    customer.name,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
                Text(
                    "📧 ${customer.email}  ·  📞 ${customer.phone}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
                Text(
                    "Tamamlanan: ${customer.totalBookings} Tur  ·  Son İşlem: ${customer.lastActivityDate}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "LTV: ₺ ${formatSegMoney(customer.ltvAmount)}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    TourOSButton(
                        text = "📞 Ara",
                        onClick = { },
                        variant = TourOSButtonVariant.TERTIARY
                    )
                    TourOSButton(
                        text = "💬 WhatsApp",
                        onClick = { },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }
            }
        }
    }
}

private fun formatSegMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

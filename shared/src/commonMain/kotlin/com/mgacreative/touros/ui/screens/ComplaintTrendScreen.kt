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
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

private data class ComplaintCategoryData(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val complaintCount: Int,
    val increasePercentage: Int,
    val isCriticalSurge: Boolean,
    val isWarning: Boolean,
    val summaryText: String
)

private val sampleComplaintCategories = listOf(
    ComplaintCategoryData(
        categoryId = "c1",
        categoryName = "Transfer & Araç Hizmeti",
        categoryIcon = "🚌",
        complaintCount = 38,
        increasePercentage = 42,
        isCriticalSurge = true,
        isWarning = true,
        summaryText = "Son 7 günde havalimanı rötarı ve araç klima arızası kaynaklı 38 yeni şikayet kaydedildi."
    ),
    ComplaintCategoryData(
        categoryId = "c2",
        categoryName = "Tur Saati & Rota Değişikliği",
        categoryIcon = "⏰",
        complaintCount = 24,
        increasePercentage = 28,
        isCriticalSurge = false,
        isWarning = true,
        summaryText = "Yoğun trafik sebebiyle ören yeri ziyaret sürelerinin kısalması şikayet artışına yol açtı."
    ),
    ComplaintCategoryData(
        categoryId = "c3",
        categoryName = "Otel & Konaklama Kalitesi",
        categoryIcon = "🏨",
        complaintCount = 18,
        increasePercentage = 12,
        isCriticalSurge = false,
        isWarning = false,
        summaryText = "Giriş işlemleri ve oda temizliği ile ilgili rutin şikayet seviyesi."
    ),
    ComplaintCategoryData(
        categoryId = "c4",
        categoryName = "Yemek & Restoran İkramı",
        categoryIcon = "🍽️",
        complaintCount = 12,
        increasePercentage = 4,
        isCriticalSurge = false,
        isWarning = false,
        summaryText = "Öğle yemeği menü çeşitliliği şikayetleri kontrol altında."
    ),
    ComplaintCategoryData(
        categoryId = "c5",
        categoryName = "Rehberlik Hizmeti",
        categoryIcon = "👨‍🌾",
        complaintCount = 8,
        increasePercentage = -15,
        isCriticalSurge = false,
        isWarning = false,
        summaryText = "Rehber anlatım kalitesi yüksek memnuniyet oranını koruyor."
    )
)

/**
 * Şikayet Trend Paneli — TourOS 0.3
 *
 * Kategori bazlı bar grafik.
 * Altında kritik artış gösteren kategoriler için Warning/Error rozetli uyarı kartları.
 */
@Composable
fun ComplaintTrendScreen(
    onNavigateBack: () -> Unit = {}
) {
    val categoriesList by remember { mutableStateOf(sampleComplaintCategories) }
    val criticalAlerts = remember(categoriesList) { categoriesList.filter { it.isWarning || it.isCriticalSurge } }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Şikayet Trend Paneli",
                subtitle = "Kategori bazlı müşteri geri bildirim ve risk analiz grafiği",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // ÖZET İSTATİSTİK BARI
            item {
                ComplaintOverviewMetricsBar(
                    totalComplaints = categoriesList.sumOf { it.complaintCount },
                    criticalCategoryCount = criticalAlerts.size
                )
            }

            // ── 1. KATEGORİ BAZLI BAR GRAFİK KARTI (Strict Rule) ─────────────────
            item {
                CategoryComplaintBarChartCard(categories = categoriesList)
            }

            // ── 2. KRİTİK ARTIŞ GÖSTEREN KATEGORİLER İÇİN UYARI KARTLARI (Strict Rule) ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚨", style = TourOSTypography.TitleLarge)
                        Text(
                            "Kritik Artış Gösteren Kategoriler (${criticalAlerts.size})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                        )
                    }

                    TourOSStatusBadge(
                        text = "ACİL EYLEM GEREKLİ",
                        backgroundColor = TourOSColors.SecondaryContainer,
                        textColor = TourOSColors.Secondary
                    )
                }
            }

            items(criticalAlerts) { alertCategory ->
                CriticalComplaintAlertCardItem(category = alertCategory)
            }
        }
    }
}

// ─── ÖZET İSTATİSTİK BARI ─────────────────────────────────────────────────────

@Composable
private fun ComplaintOverviewMetricsBar(
    totalComplaints: Int,
    criticalCategoryCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.PrimaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Toplam Şikayet Kaydı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("$totalComplaints Kayıt", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            }
        }

        TourOSCard(
            modifier = Modifier.weight(1f),
            backgroundColor = TourOSColors.SecondaryContainer,
            contentPadding = TourOSSpacing.medium
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Kritik Yükselen Kategori", style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary))
                Text("$criticalCategoryCount Kategori ⚠️", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary))
            }
        }
    }
}

// ─── KATEGORİ BAZLI BAR GRAFİK KARTI ──────────────────────────────────────────

@Composable
private fun CategoryComplaintBarChartCard(categories: List<ComplaintCategoryData>) {
    val maxCount = (categories.maxOfOrNull { it.complaintCount } ?: 1).coerceAtLeast(1)

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
                Column {
                    Text(
                        "📊 Kategori Bazlı Şikayet Dağılım Grafiği",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                    Text(
                        "Son 30 gün içinde alınan müşteri bildirim hacmi",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                TourOSStatusBadge(
                    text = "CANLI VERİ",
                    backgroundColor = TourOSColors.PrimaryContainer,
                    textColor = TourOSColors.Primary
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Bar Grafiği Görselleştirme (Category Bars)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                categories.forEach { cat ->
                    val ratio = (cat.complaintCount.toFloat() / maxCount).coerceIn(0.1f, 1.0f)
                    val barHeight = (120 * ratio).dp
                    val barColor = if (cat.isCriticalSurge || cat.isWarning) TourOSColors.Secondary else TourOSColors.Primary

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${cat.complaintCount}",
                            style = TourOSTypography.Label.copy(color = barColor)
                        )

                        Spacer(Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(barColor)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = cat.categoryIcon,
                            style = TourOSTypography.TitleMedium
                        )
                    }
                }
            }
        }
    }
}

// ─── KRİTİK ARTIŞ GÖSTEREN KATEGORİLER İÇİN WARNING / ERROR ROZETLİ UYARI KARTI ─

@Composable
private fun CriticalComplaintAlertCardItem(category: ComplaintCategoryData) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.35f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // ÜST KISIM: KATEGORİ ADI VE WARNING / ERROR ROZETİ (Strict Rule)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category.categoryIcon, style = TourOSTypography.TitleLarge)
                    Text(
                        category.categoryName,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary)
                    )
                }

                // WARNING / ERROR ROZETİ (Strict Rule)
                TourOSStatusBadge(
                    text = if (category.isCriticalSurge) "🚨 KRİTİK ARTIŞ +%${category.increasePercentage}" else "⚠️ YÜKSEK RİSK +%${category.increasePercentage}",
                    backgroundColor = TourOSColors.SecondaryContainer,
                    textColor = TourOSColors.Secondary
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // İÇERİK METNİ
            Text(
                category.summaryText,
                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Toplam Şikayet: ${category.complaintCount} Adet",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )

                TourOSButton(
                    text = "🛠️ İncele & Önlem Al",
                    onClick = { },
                    variant = TourOSButtonVariant.SECONDARY
                )
            }
        }
    }
}

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
import com.mgacreative.touros.domain.model.GuidePerformanceItem
import com.mgacreative.touros.domain.model.GuidePerformanceSummary
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.GuidePerformanceReportViewModel
import com.mgacreative.touros.ui.viewmodel.GuidePerformanceUiState

private enum class GuideSortOption(val label: String) {
    RATING("⭐ Puana Göre"),
    TOURS("🚩 Tur Sayısına Göre"),
    NAME("🔤 İsme Göre")
}

/**
 * Rehber Performans Raporu — TourOS 0.3
 *
 * Basit ve sıralanabilir tablo/liste (Rehber adı, Tamamlanan tur sayısı, Yıldız gösterimli ortalama puan).
 * Expanded: Sıralanabilir Tablo  |  Compact: Kart Listesi
 */
@Composable
fun GuidePerformanceReportScreen(
    viewModel: GuidePerformanceReportViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Rehber Performans Raporu",
                subtitle = "Rehber kadrosu tur tamamlama ve memnuniyet analizleri",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is GuidePerformanceUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is GuidePerformanceUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is GuidePerformanceUiState.Success -> {
                var currentSort by remember { mutableStateOf(GuideSortOption.RATING) }

                // Sıralanmış rehber listesi
                val sortedGuides = remember(state.summary.guides, currentSort) {
                    when (currentSort) {
                        GuideSortOption.RATING -> state.summary.guides.sortedByDescending { it.rating }
                        GuideSortOption.TOURS -> state.summary.guides.sortedByDescending { it.totalToursCompleted }
                        GuideSortOption.NAME -> state.summary.guides.sortedBy { it.fullName }
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── 1. KPI Özet Kartları ──────────────────────────────
                        item {
                            GuidePerformanceKpiSection(summary = state.summary)
                        }

                        // ── 2. Sıralama Butonları ──────────────────────────────
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🏆 Performance Sıralaması",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                    GuideSortOption.entries.forEach { option ->
                                        FilterChip(
                                            selected = currentSort == option,
                                            onClick = { currentSort = option },
                                            label = { Text(option.label, style = TourOSTypography.Caption) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                selectedLabelColor = TourOSColors.Primary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── 3. Tablo veya Kart Listesi ──────────────────────────
                        if (sortedGuides.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Kayıtlı rehber performans verisi bulunamadı.",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (isExpanded) {
                            // Expanded: Sıralanabilir Tablo
                            item {
                                GuidePerformanceTable(
                                    guides = sortedGuides,
                                    currentSort = currentSort,
                                    onSortChange = { currentSort = it }
                                )
                            }
                        } else {
                            // Compact: Kart Listesi
                            items(sortedGuides) { item ->
                                GuidePerformanceCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── KPI Bölümü ──────────────────────────────────────────────────────────────

@Composable
private fun GuidePerformanceKpiSection(summary: GuidePerformanceSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        PerformanceKpiCard(
            label = "Aktif Kadro",
            value = "${summary.totalActiveGuides} Rehber",
            bgColor = TourOSColors.PrimaryContainer,
            textColor = TourOSColors.Primary,
            modifier = Modifier.weight(1f)
        )
        PerformanceKpiCard(
            label = "Ortalama Kadro Puanı",
            value = "⭐ ${summary.avgFleetRating} / 5.0",
            bgColor = TourOSColors.SecondaryContainer,
            textColor = TourOSColors.Secondary,
            modifier = Modifier.weight(1f)
        )
        PerformanceKpiCard(
            label = "Tamamlanan Tur",
            value = "${summary.totalToursExecuted} Tur",
            bgColor = TourOSColors.SuccessContainer,
            textColor = TourOSColors.Success,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PerformanceKpiCard(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier
) {
    TourOSCard(modifier = modifier, backgroundColor = bgColor, contentPadding = TourOSSpacing.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = TourOSTypography.TitleLarge.copy(color = textColor))
            Text(label, style = TourOSTypography.Caption.copy(color = textColor.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
        }
    }
}

// ─── Expanded: Sıralanabilir Tablo ────────────────────────────────────────────

@Composable
private fun GuidePerformanceTable(
    guides: List<GuidePerformanceItem>,
    currentSort: GuideSortOption,
    onSortChange: (GuideSortOption) -> Unit
) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            // Tablo Başlık Satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Primary)
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sıra",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    "Rehber Adı",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                    modifier = Modifier.weight(1.5f).clickable { onSortChange(GuideSortOption.NAME) }
                )
                Text(
                    "Uzmanlık",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    "Tamamlanan Tur",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                    modifier = Modifier.weight(1.0f).clickable { onSortChange(GuideSortOption.TOURS) }
                )
                Text(
                    "Ortalama Puan",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                    modifier = Modifier.weight(1.0f).clickable { onSortChange(GuideSortOption.RATING) }
                )
                Text(
                    "Performans Derecesi",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                    modifier = Modifier.weight(1.1f)
                )
            }

            // Satırlar
            guides.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                val isStarGuide = item.performanceLevel == "Yıldız Rehber"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sıra #
                    Text(
                        "#${index + 1}",
                        style = TourOSTypography.Label.copy(
                            color = if (index == 0) TourOSColors.Secondary else TourOSColors.TextSecondary
                        ),
                        modifier = Modifier.width(40.dp)
                    )

                    // Rehber Adı
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(item.fullName, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                        Text("Kokart: ${item.licenseNumber ?: "—"}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    }

                    // Uzmanlık
                    Text(
                        item.specialization ?: "Genel Kültür",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.weight(1.2f)
                    )

                    // Tamamlanan Tur Sayısı
                    Text(
                        "🚩 ${item.totalToursCompleted} Tur",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                        modifier = Modifier.weight(1.0f)
                    )

                    // Ortalama Puan (Yıldız Gösterimi)
                    Row(
                        modifier = Modifier.weight(1.0f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⭐", style = TourOSTypography.Caption)
                        Text(
                            "${item.rating} / 5.0",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                        )
                    }

                    // Performans Derecesi Badgesi
                    Box(modifier = Modifier.weight(1.1f)) {
                        TourOSStatusBadge(
                            text = if (isStarGuide) "🏆 Yıldız Rehber" else "🌟 ${item.performanceLevel}",
                            backgroundColor = if (isStarGuide) TourOSColors.SecondaryContainer else TourOSColors.PrimaryContainer,
                            textColor = if (isStarGuide) TourOSColors.Secondary else TourOSColors.Primary
                        )
                    }
                }

                if (index < guides.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ─── Compact: Kart Görünümü ───────────────────────────────────────────────────

@Composable
private fun GuidePerformanceCard(item: GuidePerformanceItem) {
    val isStarGuide = item.performanceLevel == "Yıldız Rehber"

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header: Seviye & Yıldız Puanı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TourOSStatusBadge(
                    text = if (isStarGuide) "🏆 Yıldız Rehber" else "🌟 ${item.performanceLevel}",
                    backgroundColor = if (isStarGuide) TourOSColors.SecondaryContainer else TourOSColors.PrimaryContainer,
                    textColor = if (isStarGuide) TourOSColors.Secondary else TourOSColors.Primary
                )

                // Yıldız İkonlu Puan Rozeti
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TourOSColors.SecondaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                ) {
                    Text(
                        "⭐ ${item.rating} / 5.0",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                    )
                }
            }

            // Rehber İsim & Detay
            Text(
                item.fullName,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            Text(
                "Kokart No: ${item.licenseNumber ?: "—"}  ·  Uzmanlık: ${item.specialization ?: "Genel Kültür"}",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            // Puan İlerleme Çubuğu
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Puan Performansı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("%${(item.rating * 20).toInt()}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                }
                LinearProgressIndicator(
                    progress = { (item.rating / 5.0).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (isStarGuide) TourOSColors.Secondary else TourOSColors.Primary,
                    trackColor = TourOSColors.PrimaryContainer.copy(alpha = 0.5f)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Alt Satır: Tamamlanan Tur & Değerlendirme Sayısı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🚩 Tamamlanan Tur: ${item.totalToursCompleted}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )
                Text(
                    "💬 ${item.totalReviews} Değerlendirme (${item.fiveStarReviews} ⭐ 5/5)",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }
        }
    }
}

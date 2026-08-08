package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.GuideRecommendation
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.DepartureInfo
import com.mgacreative.touros.ui.viewmodel.GuideAssignmentUiState
import com.mgacreative.touros.ui.viewmodel.GuideAssignmentViewModel

private data class LanguageFilterOption(val key: String?, val label: String)

private val languageFilters = listOf(
    LanguageFilterOption(null, "Tüm Diller"),
    LanguageFilterOption("İngilizce", "İngilizce"),
    LanguageFilterOption("Almanca", "Almanca"),
    LanguageFilterOption("Fransızca", "Fransızca"),
    LanguageFilterOption("İspanyolca", "İspanyolca")
)

/**
 * Akıllı Rehber Atama ve Öneri Ekranı — TourOS 0.3
 *
 * Öneri sıralı liste (% Uyum skoru, #1, #2, #3 sıralama rozeti).
 * Dil eşleşmesi chip ile yeşil/vurgulu olarak gösterilir.
 * Tek tıkla kolay atama butonları ve kart tıklaması.
 */
@Composable
fun GuideAssignmentScreen(
    viewModel: GuideAssignmentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Tur Rehberi Atama",
                subtitle = "Akıllı öneri motoru ve dil eşleşmeli rehber seçimi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is GuideAssignmentUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is GuideAssignmentUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is GuideAssignmentUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // ── 1. Tur Kalkış Özet Kartı ──────────────────────────────
                    item {
                        DepartureSummaryCard(departure = state.departure)
                    }

                    // ── Atama Başarı Bildirimi ──────────────────────────────────
                    if (state.assignedSuccessMessage != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.SuccessContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    text = state.assignedSuccessMessage,
                                    style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                                )
                            }
                        }
                    }

                    // ── 2. Filtreler ──────────────────────────────────────────
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            Text(
                                "🗣️ Dil Filtresi & Müsaitlik",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                itemsIndexed(languageFilters) { _, filter ->
                                    FilterChip(
                                        selected = state.selectedLanguage == filter.key,
                                        onClick = { viewModel.setLanguageFilter(filter.key) },
                                        label = {
                                            Text(filter.label, style = TourOSTypography.Caption)
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                            selectedLabelColor = TourOSColors.Primary
                                        )
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                Switch(
                                    checked = state.onlyAvailableFilter,
                                    onCheckedChange = { viewModel.toggleOnlyAvailable(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TourOSColors.Primary,
                                        checkedTrackColor = TourOSColors.PrimaryContainer
                                    )
                                )
                                Text(
                                    "🟢 Sadece Müsait Rehberleri Göster",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                )
                            }
                        }
                    }

                    // ── 3. Rehber Öneri Listesi ──────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🌟 Öneri Sıralı Rehber Listesi (${state.recommendations.size})",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                "En Yüksek Uyum Üstte",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                            )
                        }
                    }

                    if (state.recommendations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Seçilen kriterlere uygun rehber bulunamadı.",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        itemsIndexed(state.recommendations) { index, rec ->
                            val isAssigned = state.departure.currentGuideName == rec.guide.fullName
                            GuideRecommendationCard(
                                rankIndex = index + 1,
                                recommendation = rec,
                                requiredLanguage = state.departure.requiredLanguage,
                                isCurrentlyAssigned = isAssigned,
                                onAssignClick = { viewModel.assignGuide(rec.guide) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Tur Kalkış Özet Kartı ───────────────────────────────────────────────────

@Composable
private fun DepartureSummaryCard(departure: DepartureInfo) {
    val hasGuide = departure.currentGuideName != null

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    departure.tourTitle,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.SecondaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 3.dp)
                ) {
                    Text(
                        "🗣️ Gerekli Dil: ${departure.requiredLanguage}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                    )
                }
            }

            Text(
                "📅 Kalkış: ${departure.departureDate}  ·  👥 Yolcu: ${departure.bookedPax}/${departure.capacity} Pax",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    Text("🚩 Atanmış Rehber:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        departure.currentGuideName ?: "Henüz rehber atanmadı",
                        style = TourOSTypography.Label.copy(
                            color = if (hasGuide) TourOSColors.Success else TourOSColors.Error
                        )
                    )
                }
                TourOSStatusBadge(
                    text = if (hasGuide) "✅ Atandı" else "⚠️ Atama Bekliyor",
                    backgroundColor = if (hasGuide) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer,
                    textColor = if (hasGuide) TourOSColors.Success else TourOSColors.Error
                )
            }
        }
    }
}

// ─── Öneri Sıralı Rehber Kartı ────────────────────────────────────────────────

@Composable
private fun GuideRecommendationCard(
    rankIndex: Int,
    recommendation: GuideRecommendation,
    requiredLanguage: String,
    isCurrentlyAssigned: Boolean,
    onAssignClick: () -> Unit
) {
    val guide = recommendation.guide
    val isAvailable = recommendation.isAvailable

    val cardBg = when {
        isCurrentlyAssigned -> TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
        !isAvailable -> TourOSColors.Surface.copy(alpha = 0.6f)
        else -> TourOSColors.Background
    }

    val borderColor = when {
        isCurrentlyAssigned -> TourOSColors.Primary
        rankIndex == 1 -> TourOSColors.Secondary
        else -> TourOSColors.Border
    }

    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrentlyAssigned) { onAssignClick() }
            .border(
                width = if (isCurrentlyAssigned || rankIndex == 1) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            ),
        backgroundColor = cardBg,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header: Sıra #, % Uyum, Puan & Müsaitlik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    // Öneri Sırası (#1, #2...)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (rankIndex == 1) TourOSColors.Secondary else TourOSColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("#$rankIndex", style = TourOSTypography.Caption.copy(color = Color.White))
                    }

                    // % Uyum Skoru Badgesi
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                    ) {
                        Text(
                            "🎯 %${recommendation.matchScore} Uyum",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    // ⭐ Puan Rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SecondaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                    ) {
                        Text(
                            "⭐ ${guide.rating}",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                        )
                    }

                    // Müsaitlik Rozeti
                    TourOSStatusBadge(
                        text = if (isAvailable) "🟢 Müsait" else "🔴 Görevde",
                        backgroundColor = if (isAvailable) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer,
                        textColor = if (isAvailable) TourOSColors.Success else TourOSColors.Error
                    )
                }
            }

            // Rehber İsim & Detay
            Column {
                Text(
                    guide.fullName,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
                Text(
                    "Kokart: ${guide.licenseNumber ?: "Lisanssız"}  ·  📞 ${guide.phone ?: "—"}  ·  🚩 ${guide.totalToursCompleted} Tur",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            // Öneri Gerekçesi kutusu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.Surface)
                    .border(0.5.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .padding(TourOSSpacing.small)
            ) {
                Text(
                    "💡 ${recommendation.recommendationReason}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            // Diller Chip Grubu (Vurgulu Eşleşme)
            if (!guide.languages.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    Text("🗣️ Diller:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(guide.languages ?: emptyList()) { lang ->
                            val isLanguageMatch = lang.lowercase() == requiredLanguage.lowercase() || recommendation.languageMatch
                            LanguageMatchChip(language = lang, isMatched = isLanguageMatch)
                        }
                    }

                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Tek Tıkla Atama Butonu
            TourOSButton(
                text = if (isCurrentlyAssigned) "✅ Tura Atanmış Rehber" else "👆 Tek Tıkla Tura Ata",
                onClick = onAssignClick,
                enabled = !isCurrentlyAssigned,
                variant = if (isCurrentlyAssigned) TourOSButtonVariant.TERTIARY else if (rankIndex == 1) TourOSButtonVariant.SECONDARY else TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Dil Eşleşmeli Chip ───────────────────────────────────────────────────────

@Composable
private fun LanguageMatchChip(language: String, isMatched: Boolean) {
    val bg = if (isMatched) TourOSColors.SuccessContainer else TourOSColors.PrimaryContainer.copy(alpha = 0.4f)
    val textCol = if (isMatched) TourOSColors.Success else TourOSColors.Primary
    val borderCol = if (isMatched) TourOSColors.Success else TourOSColors.Border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isMatched) "✓ $language" else language,
            style = TourOSTypography.Caption.copy(color = textCol)
        )
    }
}

package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.mgacreative.touros.domain.model.B2CTourItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2CTourSearchViewModel

private data class CategoryFilterOption(val name: String, val icon: String)

private val categoryOptions = listOf(
    CategoryFilterOption("Tümü", "🌟"),
    CategoryFilterOption("Balon & Vadi", "🎈"),
    CategoryFilterOption("Kültür & Tarih", "🏛️"),
    CategoryFilterOption("Günübirlik", "☀️"),
    CategoryFilterOption("VIP Transfer", "🚐")
)

/**
 * B2C Tur Arama & Keşfet Ekranı — TourOS 0.3
 *
 * Üstte Arama Çubuğu + Kategori Filtre Chip'leri.
 * Altta Görsel Ağırlıklı Tur Kartları Grid'i (Expanded: Çok Sütunlu Grid, Compact: Tek Sütun Liste).
 */
@Composable
fun B2CTourSearchScreen(
    viewModel: B2CTourSearchViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Tur Bul & Keşfet",
                subtitle = "Eşsiz rotalar ve unutulmaz seyahat deneyimleri",
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TourOSSpacing.large),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ── 1. ÜSTTE ARAMA ÇUBUĞU VE FİLTRE CHIP'LERİ ──────────────────
                TourOSTextField(
                    value = state.filter.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = "Nereye gitmek istersiniz?",
                    placeholder = "Örn: Kapadokya, Ege, Pamukkale...",
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryOptions) { cat ->
                        val isSelected = (cat.name == "Tümü" && state.filter.category == null) || state.filter.category == cat.name

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(cat.name) },
                            label = { Text("${cat.icon} ${cat.name}", style = TourOSTypography.Caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TourOSColors.Primary,
                                selectedLabelColor = TourOSColors.OnPrimary
                            )
                        )
                    }
                }

                Text(
                    "🎉 Öne Çıkan Turlar (${state.tours.size})",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )

                // ── 2. ALTA GÖRSEL AĞIRLIKLI TUR KARTLARI (GRID / LIST) ──────────
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                } else if (state.tours.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "Aramanıza uygun tur bulunamadı.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                } else if (isExpanded) {
                    // Expanded: Çok Sütunlu Grid (2 Sütun)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.tours) { tour ->
                            VisualTourCard(tour = tour, onClick = { onNavigateToDetail(tour.tourId) })
                        }
                    }
                } else {
                    // Compact: Tek Sütun Kaydırmalı Liste
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.tours) { tour ->
                            VisualTourCard(tour = tour, onClick = { onNavigateToDetail(tour.tourId) })
                        }
                    }
                }
            }
        }
    }
}

// ─── GÖRSEL AĞIRLIKLI TUR KARTI BİLEŞENİ ──────────────────────────────────────

@Composable
private fun VisualTourCard(
    tour: B2CTourItem,
    onClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = TourOSColors.Surface,
        contentPadding = 0.dp
    ) {
        Column {
            // GÖRSEL AĞIRLIKLI ÜST BANNER (CARD MEDIA MOCKUP)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(TourOSColors.PrimaryContainer)
                    .padding(TourOSSpacing.medium)
            ) {
                // Kategori Rozeti
                TourOSStatusBadge(
                    text = tour.category,
                    backgroundColor = TourOSColors.Primary,
                    textColor = TourOSColors.OnPrimary,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Değerlendirme Rozeti
                TourOSStatusBadge(
                    text = "⭐ ${tour.rating} (${tour.reviewCount})",
                    backgroundColor = TourOSColors.SecondaryContainer,
                    textColor = TourOSColors.Secondary,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Text(
                    "🏔️ ${tour.destinationCountry}",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

            // KART İÇERİK ALANI
            Column(
                modifier = Modifier.padding(TourOSSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                Text(
                    tour.title,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📍 ${tour.destinationCountry}  ·  ⏱️ ${tour.durationDays} Gün",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        "🗓️ ${tour.nextDepartureDate}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                }

                HorizontalDivider(color = TourOSColors.Divider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Kişi Başı Başlangıç",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                        Text(
                            "₺ ${formatTourMoney(tour.price)}",
                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                        )
                    }

                    TourOSButton(
                        text = "Turu İncele →",
                        onClick = onClick,
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

private fun formatTourMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

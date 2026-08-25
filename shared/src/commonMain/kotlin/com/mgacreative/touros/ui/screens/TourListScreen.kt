package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSColumn
import com.mgacreative.touros.ui.components.TourOSDataTable
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.TourListUiState
import com.mgacreative.touros.ui.viewmodel.TourListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Adaptif Tur Listesi Ekranı.
 * - Üstte Arama + Kategori/Durum Filtre Çubuğu.
 * - Expanded: Sütunlu TourOSDataTable Tablo Görünümü.
 * - Compact: Görsel ağırlıklı Tur Kartları (Kapak resmi + Başlık + Süre/Lokasyon).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TourListScreen(
    onNavigateToCreateTour: () -> Unit = {},
    onNavigateToEditTour: (String) -> Unit = {},
    viewModel: TourListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadTours()
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur Kataloğu"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif turlarınızı, paketleri ve rotaları yönetin"),
                actions = {
                    TourOSButton(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("+ Yeni Tur Ekle"),
                        onClick = onNavigateToCreateTour,
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // Arama ve Filtreleme Çubuğu Kartı
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                val successState = uiState as? TourListUiState.Success

                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    TourOSTextField(
                        value = successState?.searchQuery ?: "",
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tur adı, tur kodu veya şehir ile ara..."),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kategori Çipleri (Tek Primary Tonlu)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            val selectedCategory = successState?.selectedCategoryFilter
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { viewModel.onCategoryFilterSelected(null) },
                                label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tüm Kategoriler"), style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.PrimaryContainer,
                                    selectedLabelColor = TourOSColors.Primary
                                )
                            )

                            TourCategory.entries.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onCategoryFilterSelected(cat) },
                                    label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(cat.displayName), style = TourOSTypography.BodyMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                        selectedLabelColor = TourOSColors.Primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))

                        // Durum Filtresi (Aktif / Pasif)
                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            val selectedStatus = successState?.selectedStatusFilter
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { viewModel.onStatusFilterSelected(null) },
                                label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tümü"), style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.PrimaryContainer,
                                    selectedLabelColor = TourOSColors.Primary
                                )
                            )
                            FilterChip(
                                selected = selectedStatus == true,
                                onClick = { viewModel.onStatusFilterSelected(true) },
                                label = { Text("🟢 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif"), style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.SuccessContainer,
                                    selectedLabelColor = TourOSColors.Success
                                )
                            )
                        }
                    }
                }
            }

            // Adaptif Tablo / Kart Görünümü
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val isCompact = maxWidth < 768.dp

                when (val state = uiState) {
                    is TourListUiState.Loading -> {
                        TourOSLoadingIndicator(message = "Turlar yükleniyor...")
                    }
                    is TourListUiState.Error -> {
                        TourOSEmptyState(
                            title = "Hata Oluştu",
                            description = state.message,
                            actionButtonText = "Yeniden Dene",
                            onActionClick = { viewModel.onSearchQueryChanged("") }
                        )
                    }
                    is TourListUiState.Success -> {
                        if (state.tours.isEmpty()) {
                            TourOSEmptyState(
                                title = "Tur Bulunamadı",
                                description = "Filtre kriterlerinize uygun tur bulunmamaktadır.",
                                actionButtonText = "+ Yeni Tur Ekle",
                                onActionClick = onNavigateToCreateTour
                            )
                        } else {
                            val tourColumns = listOf(
                                TourOSColumn<Tour>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("TUR KODU & ADI"), weight = 2.5f) { tour ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TourThumbnail(imageUrl = tour.coverImageUrl, title = tour.title)
                                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                        Column {
                                            Text(text = tour.title, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                            Text(text = "Kod: ${tour.code}", style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary))
                                        }
                                    }
                                },
                                TourOSColumn<Tour>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("KATEGORİ"), weight = 1.5f) { tour ->
                                    TourOSStatusBadge(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate(tour.category.displayName),
                                        backgroundColor = TourOSColors.PrimaryContainer,
                                        textColor = TourOSColors.Primary
                                    )
                                },
                                TourOSColumn<Tour>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("LOKASYON & SÜRE"), weight = 1.8f) { tour ->
                                    Column {
                                        Text(text = "${tour.city}, ${tour.country}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                                        Text(text = "${tour.durationDays} Gün • Kapasite: ${tour.capacity}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    }
                                },
                                TourOSColumn<Tour>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("DURUM"), weight = 1.2f) { tour ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = tour.isActive,
                                            onCheckedChange = { viewModel.onToggleTourStatus(tour.id, tour.isActive) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = TourOSColors.Background,
                                                checkedTrackColor = TourOSColors.Primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                        Text(
                                            text = if (tour.isActive) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Pasif"),
                                            style = TourOSTypography.BodyMedium.copy(
                                                color = if (tour.isActive) TourOSColors.Success else TourOSColors.TextDisabled
                                            )
                                        )
                                    }
                                },
                                TourOSColumn<Tour>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İŞLEM"), weight = 1f) { tour ->
                                    TourOSButton(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Düzenle"),
                                        onClick = { onNavigateToEditTour(tour.id) },
                                        variant = TourOSButtonVariant.TERTIARY
                                    )
                                }
                            )

                            TourOSDataTable(
                                items = state.tours,
                                columns = tourColumns,
                                isCompact = isCompact,
                                modifier = Modifier.fillMaxSize(),
                                onItemClick = { onNavigateToEditTour(it.id) },
                                compactCardContent = { tour ->
                                    // COMPACT MOBİL GÖRSEL AĞIRLIKLI KART
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                        // Kapak Görsel Alanı
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                .background(TourOSColors.PrimaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!tour.coverImageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = tour.coverImageUrl,
                                                    contentDescription = tour.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = "🗺️", style = TourOSTypography.DisplaySmall)
                                                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                                    Text(
                                                        text = tour.category.displayName,
                                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = tour.title, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary))
                                                Text(
                                                    text = "📍 ${tour.city}, ${tour.country} • ⏱️ ${tour.durationDays} Gün",
                                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                                )
                                            }

                                            Switch(
                                                checked = tour.isActive,
                                                onCheckedChange = { viewModel.onToggleTourStatus(tour.id, tour.isActive) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = TourOSColors.Background,
                                                    checkedTrackColor = TourOSColors.Primary
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TourThumbnail(imageUrl: String?, title: String) {
    val initial = title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "T"
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(TourOSColors.PrimaryContainer)
            .border(TourOSSpacing.borderWidth, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                style = TourOSTypography.TitleMedium.copy(
                    color = TourOSColors.Primary, 
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }
}

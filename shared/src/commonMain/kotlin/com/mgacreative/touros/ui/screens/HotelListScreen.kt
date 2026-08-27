package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mgacreative.touros.domain.model.Hotel
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
import com.mgacreative.touros.ui.viewmodel.HotelListUiState
import com.mgacreative.touros.ui.viewmodel.HotelListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Otel Listeleme ve Yönetim Ekranı.
 * - Tur listesiyle birebir aynı tasarım dili ve adaptif tablo/kart yapısı.
 * - Üstte Arama ve Yıldız/Durum Filtre Çubuğu.
 * - Expanded: TourOSDataTable, Compact: Görsel ağırlıklı Otel Kartları.
 */
@Composable
fun HotelListScreen(
    onAddHotelClick: () -> Unit = {},
    onEditHotelClick: (String) -> Unit = {},
    viewModel: HotelListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHotels()
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Otel Portföy Yönetimi"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Konaklama tesislerini, kontrat şartlarını ve otel bilgilerini yönetin"),
                actions = {
                    TourOSButton(
                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("+ Yeni Otel Ekle"),
                        onClick = onAddHotelClick,
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
                val successState = uiState as? HotelListUiState.Success

                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    TourOSTextField(
                        value = successState?.searchQuery ?: "",
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("🔍 Otel adı, şehir, adres veya telefon ile ara..."),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Yıldız Çipleri
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            val selectedStar = successState?.selectedStarFilter
                            FilterChip(
                                selected = selectedStar == null,
                                onClick = { viewModel.onStarFilterSelected(null) },
                                label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tüm Yıldızlar"), style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.PrimaryContainer,
                                    selectedLabelColor = TourOSColors.Primary
                                )
                            )

                            listOf(5, 4, 3, 2).forEach { star ->
                                val isSelected = selectedStar == star
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onStarFilterSelected(star) },
                                    label = { Text("$star ⭐ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Otel")}", style = TourOSTypography.BodyMedium) },
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
                                label = { Text("🟢 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif")}", style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.SuccessContainer,
                                    selectedLabelColor = TourOSColors.Success
                                )
                            )
                            FilterChip(
                                selected = selectedStatus == false,
                                onClick = { viewModel.onStatusFilterSelected(false) },
                                label = { Text("⚪ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Pasif")}", style = TourOSTypography.BodyMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TourOSColors.SecondaryContainer,
                                    selectedLabelColor = TourOSColors.Secondary
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
                    is HotelListUiState.Loading -> {
                        TourOSLoadingIndicator(message = "Oteller yükleniyor...")
                    }
                    is HotelListUiState.Error -> {
                        TourOSEmptyState(
                            title = "Hata Oluştu",
                            description = state.message,
                            actionButtonText = "Yeniden Dene",
                            onActionClick = { viewModel.loadHotels() }
                        )
                    }
                    is HotelListUiState.Success -> {
                        if (state.filteredHotels.isEmpty()) {
                            TourOSEmptyState(
                                title = "Otel Bulunamadı",
                                description = "Filtre kriterlerinize uygun konaklama tesisi bulunmamaktadır.",
                                actionButtonText = "+ Yeni Otel Ekle",
                                onActionClick = onAddHotelClick
                            )
                        } else {
                            val hotelColumns = listOf(
                                TourOSColumn<Hotel>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("OTEL ADI & KONUM"), weight = 2.5f) { hotel ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        HotelThumbnail(imageUrl = hotel.coverImageUrl, name = hotel.name)
                                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                        Column {
                                            Text(text = hotel.name, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                                            Text(
                                                text = "📍 ${hotel.city ?: com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Şehir Belirtilmedi")}, ${hotel.country}",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.Secondary)
                                            )
                                        }
                                    }
                                },
                                TourOSColumn<Hotel>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("DERECE"), weight = 1.3f) { hotel ->
                                    val stars = "⭐".repeat(hotel.starRating ?: 4)
                                    TourOSStatusBadge(
                                        text = "$stars ${hotel.starRating ?: 4} ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Yıldız")}",
                                        backgroundColor = TourOSColors.SecondaryContainer,
                                        textColor = TourOSColors.Secondary
                                    )
                                },
                                TourOSColumn<Hotel>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İLETİŞİM"), weight = 1.8f) { hotel ->
                                    Column {
                                        Text(text = "📞 ${hotel.phone ?: "-"}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                                        if (!hotel.email.isNullOrBlank()) {
                                            Text(text = hotel.email, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                        }
                                    }
                                },
                                TourOSColumn<Hotel>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("DURUM"), weight = 1.2f) { hotel ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = hotel.isActive,
                                            onCheckedChange = { viewModel.onToggleHotelStatus(hotel.id, hotel.isActive) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = TourOSColors.Background,
                                                checkedTrackColor = TourOSColors.Primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                        Text(
                                            text = if (hotel.isActive) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Aktif") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Pasif"),
                                            style = TourOSTypography.BodyMedium.copy(
                                                color = if (hotel.isActive) TourOSColors.Success else TourOSColors.TextDisabled
                                            )
                                        )
                                    }
                                },
                                TourOSColumn<Hotel>(title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İŞLEM"), weight = 1f) { hotel ->
                                    TourOSButton(
                                        text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Düzenle ›"),
                                        onClick = { onEditHotelClick(hotel.id) },
                                        variant = TourOSButtonVariant.TERTIARY
                                    )
                                }
                            )

                            TourOSDataTable(
                                items = state.filteredHotels,
                                columns = hotelColumns,
                                isCompact = isCompact,
                                modifier = Modifier.fillMaxSize(),
                                onItemClick = { onEditHotelClick(it.id) },
                                compactCardContent = { hotel ->
                                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                HotelThumbnail(imageUrl = hotel.coverImageUrl, name = hotel.name)
                                                Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                                Column {
                                                    Text(text = hotel.name, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary))
                                                    Text(
                                                        text = "📍 ${hotel.city ?: "Şehir Belirtilmedi"}, ${hotel.country}",
                                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                                    )
                                                }
                                            }

                                            val stars = "⭐".repeat(hotel.starRating ?: 4)
                                            TourOSStatusBadge(
                                                text = stars,
                                                backgroundColor = TourOSColors.SecondaryContainer,
                                                textColor = TourOSColors.Secondary
                                            )
                                        }

                                        if (!hotel.description.isNullOrBlank()) {
                                            Text(
                                                text = hotel.description,
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                                maxLines = 2
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📞 ${hotel.phone ?: "-"}",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Switch(
                                                    checked = hotel.isActive,
                                                    onCheckedChange = { viewModel.onToggleHotelStatus(hotel.id, hotel.isActive) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = TourOSColors.Background,
                                                        checkedTrackColor = TourOSColors.Primary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                                TourOSButton(
                                                    text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Düzenle"),
                                                    onClick = { onEditHotelClick(hotel.id) },
                                                    variant = TourOSButtonVariant.SECONDARY
                                                )
                                            }
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
private fun HotelThumbnail(imageUrl: String?, name: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(TourOSColors.PrimaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "🏨",
                style = TourOSTypography.TitleLarge
            )
        }
    }
}

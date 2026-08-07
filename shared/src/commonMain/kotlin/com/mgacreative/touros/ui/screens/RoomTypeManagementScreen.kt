package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.RoomType
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
import com.mgacreative.touros.ui.viewmodel.RoomTypeManagementViewModel
import com.mgacreative.touros.ui.viewmodel.RoomTypeUiState

/**
 * TourOS 0.3 Tasarım Sistemine uygun Oda Tipleri ve Kontenjan Yönetim Ekranı.
 * - Otel detay sayfası içinde alt sekme olarak kullanılır.
 * - Oda tiplerini tablo/kart listesinde; kontenjanı sayısal badge ile gösterir.
 * - Expanded: TourOSDataTable, Compact: TourOSCard kart listesi.
 */
@Composable
fun RoomTypeManagementScreen(
    viewModel: RoomTypeManagementViewModel,
    hotelId: String = "1",
    hotelName: String = "Otel",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    var isFormVisible by remember { mutableStateOf(false) }

    LaunchedEffect(hotelId) {
        viewModel.initForHotel(hotelId)
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Oda Tipleri & Kontenjan",
                subtitle = "$hotelName • Oda allotment takibi ve fiyat tanımları",
                actions = {
                    TourOSButton(
                        text = if (isFormVisible) "✕ Kapat" else "+ Oda Tipi Ekle",
                        onClick = { isFormVisible = !isFormVisible },
                        variant = if (isFormVisible) TourOSButtonVariant.TERTIARY else TourOSButtonVariant.PRIMARY
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
            // Genişleyen Oda Tipi Ekleme Formu
            AnimatedVisibility(
                visible = isFormVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.5f),
                    borderColor = TourOSColors.Primary.copy(alpha = 0.2f),
                    contentPadding = TourOSSpacing.large
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        Text(text = "➕ Yeni Oda Tipi Tanımla", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))

                        TourOSTextField(
                            value = formState.name,
                            onValueChange = { viewModel.updateFormName(it) },
                            label = "Oda Tipi Adı",
                            placeholder = "Örn: Deluxe Suite, Standart Çift Kişilik",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = formState.basePricePerNight,
                                onValueChange = { viewModel.updateFormPrice(it) },
                                label = "Gecelik Fiyat (₺)",
                                placeholder = "2500",
                                modifier = Modifier.weight(1f)
                            )

                            TourOSTextField(
                                value = formState.allotment,
                                onValueChange = { viewModel.updateFormAllotment(it) },
                                label = "Acente Kontenjanı (Allotment)",
                                placeholder = "20",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = formState.totalRooms,
                                onValueChange = { viewModel.updateFormTotalRooms(it) },
                                label = "Toplam Oda Sayısı",
                                placeholder = "30",
                                modifier = Modifier.weight(1f)
                            )

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                Text(text = "Max Kişi Sayısı: ${formState.maxOccupancy} Pax", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                    (1..4).forEach { occ ->
                                        FilterChip(
                                            selected = formState.maxOccupancy == occ,
                                            onClick = { viewModel.updateFormMaxOccupancy(occ) },
                                            label = { Text("$occ", style = TourOSTypography.Caption) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                selectedLabelColor = TourOSColors.Primary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        TourOSButton(
                            text = "💾 Kaydet ve Listeye Ekle",
                            onClick = {
                                viewModel.saveRoomType()
                                isFormVisible = false
                            },
                            variant = TourOSButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Oda Tipleri & Kontenjan Listesi
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is RoomTypeUiState.Loading -> {
                        TourOSLoadingIndicator(message = "Oda tipleri yükleniyor...")
                    }
                    is RoomTypeUiState.Error -> {
                        TourOSEmptyState(
                            title = "Hata Oluştu",
                            description = state.message,
                            actionButtonText = "Yeniden Dene",
                            onActionClick = { viewModel.initForHotel(hotelId) }
                        )
                    }
                    is RoomTypeUiState.Success -> {
                        if (state.roomTypes.isEmpty()) {
                            TourOSEmptyState(
                                title = "Oda Tipi Tanımlanmamış",
                                description = "Bu otele henüz oda tipi eklenmemiş. Yukarıdaki butonu kullanarak oda tipi ekleyebilirsiniz.",
                                actionButtonText = "+ İlk Oda Tipini Ekle",
                                onActionClick = { isFormVisible = true }
                            )
                        } else {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val isCompact = maxWidth < 768.dp

                                val roomColumns = listOf(
                                    TourOSColumn<RoomType>(title = "ODA TİPİ", weight = 2f) { room ->
                                        Column {
                                            Text(text = room.name, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                                            Text(
                                                text = "👥 Max ${room.maxOccupancy} Kişi",
                                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                            )
                                        }
                                    },
                                    TourOSColumn<RoomType>(title = "GECELİK FİYAT", weight = 1.2f) { room ->
                                        Text(
                                            text = "${room.basePricePerNight.toInt()} ${room.currency}",
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                        )
                                    },
                                    // Sayısal Kontenjan Badge'i
                                    TourOSColumn<RoomType>(title = "KONTENJAN (ALLOTMENT)", weight = 1.8f) { room ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TourOSStatusBadge(
                                                text = "${room.bookedRooms} Rezerve",
                                                backgroundColor = TourOSColors.Secondary.copy(alpha = 0.15f),
                                                textColor = TourOSColors.Secondary
                                            )
                                            TourOSStatusBadge(
                                                text = "${room.availableRooms} Kalan",
                                                backgroundColor = if (room.availableRooms > 0) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer,
                                                textColor = if (room.availableRooms > 0) TourOSColors.Success else TourOSColors.Error
                                            )
                                        }
                                    },
                                    TourOSColumn<RoomType>(title = "DOLULUK %", weight = 1.2f) { room ->
                                        val ratio = if (room.allotment > 0) room.bookedRooms.toFloat() / room.allotment.toFloat() else 0f
                                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xxSmall)) {
                                            Text(text = "%${(ratio * 100).toInt()}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                                            LinearProgressIndicator(
                                                progress = { ratio.coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                                                color = if (ratio >= 1f) TourOSColors.Error else TourOSColors.Primary,
                                                trackColor = TourOSColors.PrimaryContainer
                                            )
                                        }
                                    }
                                )

                                TourOSDataTable(
                                    items = state.roomTypes,
                                    columns = roomColumns,
                                    isCompact = isCompact,
                                    modifier = Modifier.fillMaxSize(),
                                    onItemClick = { },
                                    compactCardContent = { room ->
                                        val ratio = if (room.allotment > 0) room.bookedRooms.toFloat() / room.allotment.toFloat() else 0f

                                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = room.name, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                                                    Text(text = "👥 Max ${room.maxOccupancy} Kişi", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                }
                                                Text(
                                                    text = "${room.basePricePerNight.toInt()} ${room.currency} / Gece",
                                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                                )
                                            }

                                            // Sayısal Kontenjan Badge'leri
                                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                                TourOSStatusBadge(
                                                    text = "${room.allotment} Toplam Kontenjan",
                                                    backgroundColor = TourOSColors.PrimaryContainer,
                                                    textColor = TourOSColors.Primary
                                                )
                                                TourOSStatusBadge(
                                                    text = "${room.bookedRooms} Rezerve",
                                                    backgroundColor = TourOSColors.Secondary.copy(alpha = 0.15f),
                                                    textColor = TourOSColors.Secondary
                                                )
                                                TourOSStatusBadge(
                                                    text = "${room.availableRooms} Kalan",
                                                    backgroundColor = if (room.availableRooms > 0) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer,
                                                    textColor = if (room.availableRooms > 0) TourOSColors.Success else TourOSColors.Error
                                                )
                                            }

                                            // Doluluk İlerleme Çubuğu
                                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xxSmall)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = "Doluluk Oranı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                                    Text(text = "%${(ratio * 100).toInt()}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                                                }
                                                LinearProgressIndicator(
                                                    progress = { ratio.coerceIn(0f, 1f) },
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)),
                                                    color = if (ratio >= 1f) TourOSColors.Error else TourOSColors.Primary,
                                                    trackColor = TourOSColors.PrimaryContainer
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
}

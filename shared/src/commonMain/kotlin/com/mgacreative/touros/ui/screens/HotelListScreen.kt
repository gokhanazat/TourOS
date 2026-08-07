package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

/**
 * TourOS 0.3 Tasarım Sistemine uygun Otel Listeleme ve Yönetim Ekranı.
 * - Tur listesiyle tutarlı kart ve tablo düzeni (Expanded: TourOSDataTable, Compact: TourOSCard).
 * - Otel kartında yıldız derecelendirmesi ikon ile, konumu ikincil metin renginde (TextSecondary).
 */
@Composable
fun HotelListScreen(
    viewModel: HotelListViewModel,
    onAddHotelClick: () -> Unit = {},
    onEditHotelClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Otel Portföy Yönetimi",
                subtitle = "Konaklama tesislerini, kontrat şartlarını ve otel bilgilerini yönetin",
                actions = {
                    TourOSButton(
                        text = "+ Yeni Otel Ekle",
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
            // Arama ve Filtre Barı
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.medium
            ) {
                TourOSTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "🔍 Otel adı, şehir veya adres bilgisi ile arayın...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Adaptif Tablo / Kart Listesi
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val isCompact = maxWidth < 768.dp

                when (val state = uiState) {
                    is HotelListUiState.Loading -> {
                        TourOSLoadingIndicator(message = "Otel listesi yükleniyor...")
                    }
                    is HotelListUiState.Error -> {
                        TourOSEmptyState(
                            title = "Hata Oluştu",
                            description = state.message,
                            actionButtonText = "Yeniden Dene",
                            onActionClick = { }
                        )
                    }
                    is HotelListUiState.Success -> {
                        val filteredHotels = state.hotels.filter {
                            searchQuery.isBlank() ||
                                    it.name.contains(searchQuery, ignoreCase = true) ||
                                    (it.city ?: "").contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredHotels.isEmpty()) {
                            TourOSEmptyState(
                                title = "Kayıtlı Otel Bulunamadı",
                                description = "Henüz sisteme eklenmiş bir konaklama tesisi yok veya arama kriterine uygun otel bulunamadı.",
                                actionButtonText = "+ Yeni Otel Ekle",
                                onActionClick = onAddHotelClick
                            )
                        } else {
                            val hotelColumns = listOf(
                                TourOSColumn<Hotel>(title = "OTEL ADI & KONUM", weight = 2.5f) { hotel ->
                                    Column {
                                        Text(text = hotel.name, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                                        Text(
                                            text = "📍 ${hotel.city ?: "Şehir Belirtilmedi"}, ${hotel.country}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }
                                },
                                TourOSColumn<Hotel>(title = "DERECE", weight = 1.2f) { hotel ->
                                    val stars = "⭐".repeat(hotel.starRating ?: 4)
                                    TourOSStatusBadge(
                                        text = "$stars ${hotel.starRating ?: 4} Yıldız",
                                        backgroundColor = TourOSColors.Secondary.copy(alpha = 0.15f),
                                        textColor = TourOSColors.Secondary
                                    )
                                },
                                TourOSColumn<Hotel>(title = "İLETİŞİM", weight = 1.5f) { hotel ->
                                    Text(
                                        text = "📞 ${hotel.phone ?: "-"}",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                    )
                                },
                                TourOSColumn<Hotel>(title = "İŞLEM", weight = 1f) { hotel ->
                                    TourOSButton(
                                        text = "Düzenle ›",
                                        onClick = { onEditHotelClick(hotel.id) },
                                        variant = TourOSButtonVariant.TERTIARY
                                    )
                                }
                            )

                            TourOSDataTable(
                                items = filteredHotels,
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = hotel.name, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                                                // Konum İkincil Metin Renginde (TextSecondary)
                                                Text(
                                                    text = "📍 ${hotel.city ?: "Şehir Belirtilmedi"}, ${hotel.country}",
                                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                                )
                                            }

                                            // Yıldız Derecesi İkon ile
                                            val stars = "⭐".repeat(hotel.starRating ?: 4)
                                            TourOSStatusBadge(
                                                text = stars,
                                                backgroundColor = TourOSColors.Secondary.copy(alpha = 0.15f),
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

                                            TourOSButton(
                                                text = "Düzenle",
                                                onClick = { onEditHotelClick(hotel.id) },
                                                variant = TourOSButtonVariant.SECONDARY
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

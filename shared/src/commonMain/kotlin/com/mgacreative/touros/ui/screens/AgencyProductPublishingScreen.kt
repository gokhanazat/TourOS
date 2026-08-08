package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.data.database.entity.AgencyPublishedTourEntity
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingUiState
import com.mgacreative.touros.ui.viewmodel.AgencyProductPublishingViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * 4.6.7 Ürün Seçimi / Yayınlama Ekranı (Acente Tarafı).
 * Operatör kataloğunu listeleyip hangi turların kendi sitesinde yayınlanacağını seçme (publish/unpublish toggle + fiyat override önizlemesi).
 */
@Composable
fun AgencyProductPublishingScreen(
    viewModel: AgencyProductPublishingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
            .padding(TourOSSpacing.large)
    ) {
        Text(
            text = "Katalog & Ürün Yayınlama Yönetimi",
            style = TourOSTypography.DisplaySmall,
            color = TourOSColors.TextPrimary
        )
        Text(
            text = "Bağlı operatör turlarını kendi sitenizde yayınlayın veya gizleyin.",
            style = TourOSTypography.BodyMedium,
            color = TourOSColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        when (val state = uiState) {
            is AgencyProductPublishingUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is AgencyProductPublishingUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TourOSColors.Error)
                }
            }
            is AgencyProductPublishingUiState.Success -> {
                if (state.tours.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Henüz senkronize edilmiş operatör turu bulunmamaktadır.",
                            color = TourOSColors.TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        items(state.tours) { tourItem ->
                            PublishingTourCard(
                                item = tourItem,
                                onTogglePublish = { isPublished ->
                                    viewModel.togglePublishStatus(tourItem.tourId, isPublished)
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
private fun PublishingTourCard(
    item: AgencyPublishedTourEntity,
    onTogglePublish: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TourOSSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.tourTitle,
                        style = TourOSTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "(${item.tourCode})",
                        style = TourOSTypography.Label,
                        color = TourOSColors.Primary
                    )
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Text(
                    text = "Operatör: ${item.operatorName}",
                    style = TourOSTypography.BodyMedium,
                    color = TourOSColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Row {
                    Text(
                        text = "Operatör Taban Fiyatı: ${item.basePrice} ₺  ➔  ",
                        style = TourOSTypography.BodyMedium
                    )
                    Text(
                        text = "Sitenizde Satış Fiyatı: ${item.calculatedPrice} ₺",
                        style = TourOSTypography.BodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Success
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (item.isPublished) "YAYINDA" else "PASİF",
                    style = TourOSTypography.Label,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPublished) TourOSColors.Success else TourOSColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Switch(
                    checked = item.isPublished,
                    onCheckedChange = onTogglePublish,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TourOSColors.Success,
                        checkedTrackColor = TourOSColors.Success.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

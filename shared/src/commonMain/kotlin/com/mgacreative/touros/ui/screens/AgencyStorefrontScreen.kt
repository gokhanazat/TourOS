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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.data.database.entity.AgencyStorefrontTourItem
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AgencyStorefrontUiState
import com.mgacreative.touros.ui.viewmodel.AgencyStorefrontViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * 4.6.8 Acente Storefront (Kendi Web Sitesi) — Temel Yapı.
 * Travelata.ru tarzı çoklu operatör karşılaştırmalı tur agregatörü ekranı.
 * Hangi operatörden geldiği iç bilgide tutulur, 'X operatörden karşılaştırıldı' rozeti ile kullanıcıya sunulur.
 */
@Composable
fun AgencyStorefrontScreen(
    onNavigateToTourDetail: (String) -> Unit = {},
    viewModel: AgencyStorefrontViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var countryInput by remember { mutableStateOf("") }
    var maxBudgetInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
    ) {
        when (val state = uiState) {
            is AgencyStorefrontUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is AgencyStorefrontUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TourOSColors.Error)
                }
            }
            is AgencyStorefrontUiState.Success -> {
                // Browser URL Preview Strip
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔒 https://", style = TourOSTypography.Label, color = TourOSColors.Success)
                            Text(text = "acente-web.touros.app/storefront", style = TourOSTypography.Label, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                        }
                        Text(text = "🌐 Acente Canlı Web Sitesi Önizlemesi", style = TourOSTypography.Label, color = TourOSColors.TextSecondary)
                    }
                }

                // Hero Header (Travelata.ru Branding)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TourOSColors.Primary)
                        .padding(vertical = TourOSSpacing.xxLarge, horizontal = TourOSSpacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.branding.heroTitle,
                            style = TourOSTypography.DisplaySmall,
                            color = TourOSColors.OnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(TourOSSpacing.small))
                        Text(
                            text = state.branding.heroSubtitle,
                            style = TourOSTypography.BodyMedium,
                            color = TourOSColors.OnPrimary.copy(alpha = 0.85f)
                        )
                    }
                }

                // Search Bar & Filter Strip
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(TourOSSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = countryInput,
                                onValueChange = { countryInput = it },
                                label = "Ülke / Şehir Ara (örn. Türkiye, İtalya)"
                            )
                        }
                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = maxBudgetInput,
                                onValueChange = { maxBudgetInput = it },
                                label = "Maks. Bütçe (₺)"
                            )
                        }
                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                        TourOSButton(
                            text = "Turları Karşılaştır 🔍",
                            onClick = {
                                val b = maxBudgetInput.toDoubleOrNull() ?: 100000.0
                                viewModel.loadStorefront(countryFilter = countryInput, maxBudget = b)
                            }
                        )
                    }
                }

                // Aggregated Tour List (Travelata style)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    items(state.tours) { tourItem ->
                        StorefrontTourCard(
                            item = tourItem,
                            onClickDetail = { onNavigateToTourDetail(tourItem.tourId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorefrontTourCard(
    item: AgencyStorefrontTourItem,
    onClickDetail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = TourOSTypography.TitleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.TextPrimary
                    )
                }

                // Comparison Badge (Travelata.ru style)
                Box(
                    modifier = Modifier
                        .background(
                            TourOSColors.Secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ ${item.comparedOperatorCount} Operatörden Karşılaştırıldı",
                        style = TourOSTypography.Label,
                        color = TourOSColors.Secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Konum: ${item.city}, ${item.country} • ${item.nights} Gece",
                        style = TourOSTypography.BodyMedium,
                        color = TourOSColors.TextSecondary
                    )
                    Text(
                        text = "Operatör Kaynağı: ${item.operatorName}",
                        style = TourOSTypography.Label,
                        color = TourOSColors.TextSecondary.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "En Uygun Teklif",
                        style = TourOSTypography.Label,
                        color = TourOSColors.TextSecondary
                    )
                    Text(
                        text = "${item.finalPrice} ₺",
                        style = TourOSTypography.TitleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    TourOSButton(
                        text = "Detayları Gör & Rezerve Et ➔",
                        onClick = onClickDetail
                    )
                }
            }
        }
    }
}

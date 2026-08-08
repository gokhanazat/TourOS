package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * 4.6.8 Acente Storefront (Sletat.ru Konsepti).
 * Sletat.ru tarzında çoklu tur operatörü arama motoru, kalkış şehri / varış noktası seçicisi,
 * sıcak fırsatlar (Hot Deals) ve popüler destinasyonlar grid'i.
 */
@Composable
fun AgencyStorefrontScreen(
    onNavigateToTourDetail: (String) -> Unit = {},
    viewModel: AgencyStorefrontViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var departureCityInput by remember { mutableStateOf("İstanbul") }
    var destinationInput by remember { mutableStateOf("") }
    var maxBudgetInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Turlar, 1: Sıcak Fırsatlar, 2: Oteller

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Sletat.ru Top Header / Brand Bar
                    item {
                        Card(
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(TourOSColors.Primary, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = "SLETAT", style = TourOSTypography.TitleMedium, color = TourOSColors.OnPrimary, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.width(TourOSSpacing.small))
                                    Text(text = "| ${state.branding.heroTitle}", style = TourOSTypography.BodyMedium, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📞 0850 300 00 00", style = TourOSTypography.BodyMedium, fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                                    Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, TourOSColors.Border, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "🔒 Güvenli Ödeme", style = TourOSTypography.Label, color = TourOSColors.Success)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Sletat.ru Hero Search Module
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Primary)
                                .padding(vertical = TourOSSpacing.xLarge, horizontal = TourOSSpacing.large)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "80+ Tur Operatöründen En Uygun Fiyatları Karşılaştırın",
                                    style = TourOSTypography.TitleLarge,
                                    color = TourOSColors.OnPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                                // Search Tabs (Turlar / Sıcak Fırsatlar / Oteller)
                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    SletatTabButton("✈️ Turlar", selectedTab == 0) { selectedTab = 0 }
                                    SletatTabButton("🔥 Sıcak Fırsatlar", selectedTab == 1) { selectedTab = 1 }
                                    SletatTabButton("🏨 Oteller", selectedTab == 2) { selectedTab = 2 }
                                }
                                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                                // Multi-Field Search Strip
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                TourOSTextField(
                                                    value = departureCityInput,
                                                    onValueChange = { departureCityInput = it },
                                                    label = "Kalkış Şehri (Nereden)"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1.5f)) {
                                                TourOSTextField(
                                                    value = destinationInput,
                                                    onValueChange = { destinationInput = it },
                                                    label = "Nereye (Ülke / Bölge / Otel)"
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                TourOSTextField(
                                                    value = maxBudgetInput,
                                                    onValueChange = { maxBudgetInput = it },
                                                    label = "Maks. Bütçe (₺)"
                                                )
                                            }
                                            TourOSButton(
                                                text = "TURLARI ARA 🔍",
                                                onClick = {
                                                    val b = maxBudgetInput.toDoubleOrNull() ?: 100000.0
                                                    viewModel.loadStorefront(countryFilter = destinationInput, maxBudget = b)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Sletat.ru Operator Trust Strip
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Surface)
                                .padding(vertical = TourOSSpacing.medium, horizontal = TourOSSpacing.large)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Güvenilir Tur Operatörleri:", style = TourOSTypography.Label, color = TourOSColors.TextSecondary, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)) {
                                    OperatorBadge("Coral Travel")
                                    OperatorBadge("Anex Tour")
                                    OperatorBadge("Pegas Touristik")
                                    OperatorBadge("Fun & Sun")
                                    OperatorBadge("Sunmar")
                                }
                            }
                        }
                    }

                    // 4. Popular Country Categories (Sletat.ru Quick Filter Cards)
                    item {
                        Column(modifier = Modifier.padding(TourOSSpacing.large)) {
                            Text(
                                text = "Popüler Destinasyonlar",
                                style = TourOSTypography.TitleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                items(
                                    listOf(
                                        "Türkiye 🇹🇷" to "12.500 ₺'den",
                                        "Mısır 🇪🇬" to "18.900 ₺'den",
                                        "BAE (Dubai) 🇦🇪" to "24.000 ₺'den",
                                        "Tayland 🇹🇭" to "32.000 ₺'den",
                                        "İtalya 🇮🇹" to "28.500 ₺'den"
                                    )
                                ) { (dest, price) ->
                                    DestinationCard(destination = dest, startingPrice = price) {
                                        destinationInput = dest.split(" ")[0]
                                        viewModel.loadStorefront(countryFilter = destinationInput, maxBudget = 100000.0)
                                    }
                                }
                            }
                        }
                    }

                    // 5. Search Results Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Karşılaştırmalı Tur Sonuçları (${state.tours.size} Tur Bulundu)",
                                style = TourOSTypography.TitleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TourOSColors.TextPrimary
                            )
                        }
                    }

                    // 6. Sletat.ru Style Tour List
                    items(state.tours) { tourItem ->
                        Box(modifier = Modifier.padding(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.xSmall)) {
                            SletatTourCard(
                                item = tourItem,
                                onClickDetail = { onNavigateToTourDetail(tourItem.tourId) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge)) }
                }
            }
        }
    }
}

@Composable
private fun SletatTabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(if (isSelected) TourOSColors.Surface else TourOSColors.OnPrimary.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
    ) {
        Text(
            text = text,
            style = TourOSTypography.Label,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TourOSColors.Primary else TourOSColors.OnPrimary
        )
    }
}

@Composable
private fun OperatorBadge(name: String) {
    Box(
        modifier = Modifier
            .background(TourOSColors.Surface, RoundedCornerShape(4.dp))
            .border(1.dp, TourOSColors.Border, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = name, style = TourOSTypography.Label, fontSize = 11.sp, color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DestinationCard(destination: String, startingPrice: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
            Text(text = destination, style = TourOSTypography.TitleMedium, fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = startingPrice, style = TourOSTypography.Label, color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SletatTourCard(
    item: AgencyStorefrontTourItem,
    onClickDetail: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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

                // Sletat.ru Comparison & Discount Badge
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    Box(
                        modifier = Modifier
                            .background(TourOSColors.Error.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "🔥 Hot Deal %15", style = TourOSTypography.Label, color = TourOSColors.Error, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(TourOSColors.Secondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "⚡ ${item.comparedOperatorCount} Operatör Karşılaştırıldı", style = TourOSTypography.Label, color = TourOSColors.Secondary, fontWeight = FontWeight.Bold)
                    }
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
                        text = "✈️ Kalkış: İstanbul • Konum: ${item.city}, ${item.country} • ${item.nights} Gece",
                        style = TourOSTypography.BodyMedium,
                        color = TourOSColors.TextSecondary
                    )
                    Text(
                        text = "Sağlayıcı Operatör: ${item.operatorName} • Her Şey Dahil",
                        style = TourOSTypography.Label,
                        color = TourOSColors.TextSecondary.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${(item.finalPrice * 1.15).toInt()} ₺",
                        style = TourOSTypography.Label,
                        color = TourOSColors.TextSecondary,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text(
                        text = "${item.finalPrice} ₺",
                        style = TourOSTypography.TitleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TourOSColors.Primary
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    TourOSButton(
                        text = "TURU İNCELE ➔",
                        onClick = onClickDetail
                    )
                }
            }
        }
    }
}

package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.ExchangeRate
import com.mgacreative.touros.ui.viewmodel.ExchangeRatesViewModel

import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors

/**
 * 3.2.5 Çoklu Para Birimi ve Kur Servisi Ekranı (TRY, EUR, USD, GBP, AED, RUB).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRatesScreen(
    viewModel: ExchangeRatesViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var inputAmountStr by remember { mutableStateOf("1000") }
    var selectedFrom by remember { mutableStateOf("EUR") }
    var selectedTo by remember { mutableStateOf("TRY") }

    val currencies = listOf("TRY", "EUR", "USD", "GBP", "AED", "RUB")

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "💱 Çoklu Para Birimi & Kur Servisi",
                subtitle = "TCMB ve serbest piyasa canlı döviz kurları",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Otomatik Kur Servisi Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("🔄 Periyodik Kur Güncelleme Servisi: AKTİF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Kaynak: TCMB (Merkez Bankası) | Son Güncelleme: ${state.lastUpdateDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { viewModel.syncTcmbExchangeRates() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🔄 Güncelle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 2. Canlı Döviz Çevirici Hesaplayıcı (Converter)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🧮 Anlık Çoklu Döviz Çevirici", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = inputAmountStr,
                            onValueChange = {
                                inputAmountStr = it
                                val amt = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateConversion(amt, selectedFrom, selectedTo)
                            },
                            label = { Text("Miktar") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // From Currency Dropdown / Chip Choice
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kaynak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = selectedFrom == "EUR",
                                    onClick = {
                                        selectedFrom = "EUR"
                                        val amt = inputAmountStr.toDoubleOrNull() ?: 0.0
                                        viewModel.updateConversion(amt, "EUR", selectedTo)
                                    },
                                    label = { Text("EUR €", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = selectedFrom == "USD",
                                    onClick = {
                                        selectedFrom = "USD"
                                        val amt = inputAmountStr.toDoubleOrNull() ?: 0.0
                                        viewModel.updateConversion(amt, "USD", selectedTo)
                                    },
                                    label = { Text("USD $", fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Result Display
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Çevrilen Tutar (${selectedTo}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${state.convertedAmount} $selectedTo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }
            }

            // 3. TCMB Güncel Döviz Kurları Tablosu
            Text("📊 TCMB Güncel Döviz Kurları Tablosu (TRY Bazlı):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.rates) { rate ->
                    ExchangeRateCard(rate = rate)
                }
            }
        }
    }
}

@Composable
fun ExchangeRateCard(rate: ExchangeRate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(rate.targetCurrency, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
                Column {
                    Text("1 ${rate.targetCurrency} / ${rate.baseCurrency}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Kaynak: ${rate.source} | ${rate.rateDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Döviz Alış", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${rate.buyingRate} ₺", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Döviz Satış", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${rate.sellingRate} ₺", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Efektif Kur", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${rate.effectiveRate} ₺", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

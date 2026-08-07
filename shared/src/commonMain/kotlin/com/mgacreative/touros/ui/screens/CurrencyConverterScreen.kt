package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.util.KmpCurrencyFormatter
import com.mgacreative.touros.ui.viewmodel.CurrencyConverterViewModel

/**
 * 4.4.2 CurrencyFormatter (TRY, EUR, USD, GBP, AED, RUB) Anlık Çeviri Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    viewModel: CurrencyConverterViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val res = state.conversionResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💱 CurrencyFormatter", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 1. Tutar & Para Birimi Seçim Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("💰 Çevrilecek Tutar ve Para Birimleri", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = "${state.amount}",
                        onValueChange = { viewModel.updateAmount(it.toDoubleOrNull() ?: 1000.0) },
                        label = { Text("Çevrilecek Tutar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Kaynak Para Birimi
                    Text("Kaynak Para Birimi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.supportedCurrencies.forEach { curr ->
                            FilterChip(
                                selected = state.fromCurrency == curr,
                                onClick = { viewModel.updateFromCurrency(curr) },
                                label = { Text("${KmpCurrencyFormatter.getSymbol(curr)} $curr", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Hedef Para Birimi
                    Text("Hedef Para Birimi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.supportedCurrencies.forEach { curr ->
                            FilterChip(
                                selected = state.toCurrency == curr,
                                onClick = { viewModel.updateToCurrency(curr) },
                                label = { Text("${KmpCurrencyFormatter.getSymbol(curr)} $curr", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Anlık Çeviri ve Format Çıktı Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("💱 Anlık Çeviri Çıktısı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${res.fromCurrency} ➔ ${res.toCurrency}", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Formatlanmış Sonuç", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(res.formattedResult, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Text("Anlık Çeviri Oranı: 1 ${res.fromCurrency} = ${res.exchangeRate} ${res.toCurrency}", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("✅ KmpCurrencyFormatter: String.format() kullanmayan pure Kotlin Multiplatform sayı biçimlendirici.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

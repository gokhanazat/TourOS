package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.gateway.PaymentGatewayStatus
import com.mgacreative.touros.ui.viewmodel.PaymentGatewayUiState
import com.mgacreative.touros.ui.viewmodel.PaymentGatewayViewModel

/**
 * 3.2.1 Sağlayıcı Bağımsız Ödeme Geçidi Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayScreen(
    viewModel: PaymentGatewayViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var cardHolder by remember { mutableStateOf("Ahmet Yılmaz") }
    var cardNumber by remember { mutableStateOf("5890 0400 1234 5678") }
    var expireMonth by remember { mutableStateOf("12") }
    var expireYear by remember { mutableStateOf("28") }
    var cvc by remember { mutableStateOf("321") }
    var amountStr by remember { mutableStateOf("7500") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💳 Multi-Provider Ödeme Geçidi", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (val state = uiState) {
                is PaymentGatewayUiState.Processing -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator()
                            Text("💳 Ödeme Sağlayıcısı İle Güvenli Bağlantı Kuruluyor...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                is PaymentGatewayUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is PaymentGatewayUiState.Success -> {
                    // 1. Sağlayıcı Seçim Çipleri
                    Text("⚙️ Aktif Ödeme Sağlayıcı Seçimi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.activeProvider == "iyzico",
                            onClick = { viewModel.setProvider("iyzico") },
                            label = { Text("🇹🇷 İyzico (TR)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.activeProvider == "stripe",
                            onClick = { viewModel.setProvider("stripe") },
                            label = { Text("🌐 Stripe (Global)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.activeProvider == "mock",
                            onClick = { viewModel.setProvider("mock") },
                            label = { Text("🧪 Mock Test POS", fontSize = 11.sp) }
                        )
                    }

                    if (state.notificationMessage != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = if (state.lastPaymentResponse?.isSuccess == true) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                            Text(state.notificationMessage, color = if (state.lastPaymentResponse?.isSuccess == true) Color(0xFF15803D) else MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }

                    // 2. Kredi Kartı Ödeme Formu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("💳 Kredi / Banka Kartı Bilgileri (${state.activeProvider.uppercase()})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            OutlinedTextField(value = cardHolder, onValueChange = { cardHolder = it }, label = { Text("Kart Üzerindeki İsim") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("Kart Numarası") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = expireMonth, onValueChange = { expireMonth = it }, label = { Text("Ay") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = expireYear, onValueChange = { expireYear = it }, label = { Text("Yıl") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = cvc, onValueChange = { cvc = it }, label = { Text("CVC") }, singleLine = true, modifier = Modifier.weight(1f))
                            }

                            OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Çekilecek Tutar (TRY)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                            Button(
                                onClick = {
                                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                                    viewModel.executePayment(cardNumber, cardHolder, expireMonth, expireYear, cvc, amt)
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("💳 ${state.activeProvider.uppercase()} Üzerinden Ödemeyi Çek", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. Son İşlem Detayı
                    if (state.lastPaymentResponse != null) {
                        val resp = state.lastPaymentResponse
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🧾 Son İşlem Dekontu:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Sağlayıcı: ${resp.providerName.uppercase()} | Durum: ${resp.status}", fontSize = 11.sp)
                                Text("İşlem ID: ${resp.transactionId ?: "-"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

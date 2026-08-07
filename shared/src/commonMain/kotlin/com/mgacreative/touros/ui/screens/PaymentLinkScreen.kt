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
import com.mgacreative.touros.domain.gateway.PaymentLinkInfo
import com.mgacreative.touros.ui.viewmodel.PaymentLinkUiState
import com.mgacreative.touros.ui.viewmodel.PaymentLinkViewModel

/**
 * 3.2.3 Link ile Ödeme Entegrasyonu Ekranı (Stripe / Sanal POS).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentLinkScreen(
    viewModel: PaymentLinkViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var bookingId by remember { mutableStateOf("B-202608-001") }
    var amountStr by remember { mutableStateOf("12000") }
    var customerEmail by remember { mutableStateOf("hans@example.com") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔗 Link ile Ödeme Entegrasyonu", fontWeight = FontWeight.Bold) },
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
                is PaymentLinkUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PaymentLinkUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is PaymentLinkUiState.Success -> {
                    // 1. Ödeme Sağlayıcısı Seçimi
                    Text("🌐 Ödeme Geçidi Seçimi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.selectedProvider == "stripe",
                            onClick = { viewModel.setProvider("stripe") },
                            label = { Text("🌐 Stripe Checkout", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedProvider == "iyzico",
                            onClick = { viewModel.setProvider("iyzico") },
                            label = { Text("🇹🇷 İyzico Link", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedProvider == "mock",
                            onClick = { viewModel.setProvider("mock") },
                            label = { Text("🧪 Mock POS", fontSize = 11.sp) }
                        )
                    }

                    if (state.notificationMessage != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                            Text(state.notificationMessage, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }

                    // 2. Link Oluşturma Formu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔗 Müşteriye Ödeme Linki Gönder (${state.selectedProvider.uppercase()})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            OutlinedTextField(value = bookingId, onValueChange = { bookingId = it }, label = { Text("Rezervasyon Kodu / ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Link Tutarı (TRY)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = customerEmail, onValueChange = { customerEmail = it }, label = { Text("Müşteri E-Posta") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                            Button(
                                onClick = {
                                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                                    viewModel.generateLink(bookingId, amt, customerEmail)
                                },
                                enabled = amountStr.toDoubleOrNull() != null && amountStr.toDoubleOrNull()!! > 0,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🚀 Güvenli Ödeme Linki Oluştur", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. Aktif Ödeme Linkleri Listesi
                    Text("📋 Üretilen Ödeme Linkleri (${state.links.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.links) { link ->
                            PaymentLinkCard(link = link)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentLinkCard(link: PaymentLinkInfo) {
    val (statusText, statusBg, statusFg) = when (link.status) {
        "PAID" -> Triple("✅ ÖDENDİ", Color(0xFFDCFCE7), Color(0xFF15803D))
        "EXPIRED" -> Triple("🔴 SÜRESİ DOLDU", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        else -> Triple("🟡 ÖDEME BEKLİYOR", Color(0xFFFEF3C7), Color(0xFF92400E))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("🌐 ${link.gatewayProvider.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }

                Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                    Text(statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text("📌 Rezervasyon: ${link.bookingId} | Müşteri: ${link.customerEmail ?: "-"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Text(link.checkoutUrl, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Tahsil Edilecek Tutar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${link.amount} ${link.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = {}, shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Text("📋 Kopyala", fontSize = 10.sp)
                    }
                    Button(onClick = {}, shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                        Text("💬 WhatsApp", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

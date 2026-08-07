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
import com.mgacreative.touros.ui.viewmodel.PaymentWebhookUiState
import com.mgacreative.touros.ui.viewmodel.PaymentWebhookViewModel
import com.mgacreative.touros.ui.viewmodel.WebhookLogItem

/**
 * 3.2.4 Webhook/Callback Onayı & Booking/Invoice Senkronizasyonu Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWebhookScreen(
    viewModel: PaymentWebhookViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var linkCode by remember { mutableStateOf("cs_live_981238") }
    var selectedProvider by remember { mutableStateOf("stripe") }
    var txId by remember { mutableStateOf("pi_3M98123490") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Webhook & Callback Senkronizasyonu", fontWeight = FontWeight.Bold) },
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
                is PaymentWebhookUiState.Processing -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator()
                            Text("⚡ Edge Function Webhook İsteği İşleniyor & Senkronize Ediliyor...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                is PaymentWebhookUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is PaymentWebhookUiState.Success -> {
                    // 1. Supabase Edge Function Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🌐 Supabase Edge Function Webhook Endpoint:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
                                Text("https://touros.supabase.co/functions/v1/payment-webhook", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                            }
                            Text("⚡ Stripe ve İyzico callback bildirimleri otomatik olarak Booking & Invoice durumlarını 'PAID' yapar.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (state.notificationMessage != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                            Text(state.notificationMessage, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }

                    // 2. Webhook Callback Test Tetikleme Formu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🧪 Webhook Tetikleme Test Paneli", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = selectedProvider == "stripe",
                                    onClick = { selectedProvider = "stripe" },
                                    label = { Text("Stripe Webhook", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = selectedProvider == "iyzico",
                                    onClick = { selectedProvider = "iyzico" },
                                    label = { Text("İyzico Callback", fontSize = 11.sp) }
                                )
                            }

                            OutlinedTextField(value = linkCode, onValueChange = { linkCode = it }, label = { Text("Ödeme Link Kodu (Link Code)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = txId, onValueChange = { txId = it }, label = { Text("Transaction ID (Ödeme Tx)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                            Button(
                                onClick = { viewModel.triggerWebhookCallback(linkCode, selectedProvider, txId) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("⚡ Webhook Callback Tetikle (Booking & Invoice Senkronize Et)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. Webhook Logları ve Senkronizasyon Akışı
                    Text("📜 İşlenen Webhook Logları ve Senkronizasyon Durumu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.logs) { log ->
                            WebhookLogCard(log = log)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun WebhookLogCard(log: WebhookLogItem) {
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${log.timestamp} | ${log.provider.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Link: ${log.linkCode} | Tx: ${log.transactionId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                Text(log.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}

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
import com.mgacreative.touros.domain.model.Payment
import com.mgacreative.touros.ui.viewmodel.BookingPaymentViewModel

/**
 * 3.2.2 Nakit/Kart/Havale & Kısmi Ödeme (Depozito) Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingPaymentScreen(
    viewModel: BookingPaymentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var paymentAmountStr by remember { mutableStateOf("") }
    var referenceNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💵 Rezervasyon Ödemesi & Depozito", fontWeight = FontWeight.Bold) },
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
            // 1. Rezervasyon Özet Finans Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("📌 ${state.bookingCode} | ${state.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        val (statusText, statusColor) = when (state.paymentStatus) {
                            "PAID" -> "🟢 Tam Ödendi" to Color(0xFF15803D)
                            "PARTIALLY_PAID" -> "🟡 Kısmi Ödendi (Depozito)" to Color(0xFFB45309)
                            else -> "🔴 Ödenmedi" to MaterialTheme.colorScheme.error
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                            Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Toplam Fiyat", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.totalPrice} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Önceden Ödenen", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.totalPaid} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                        Column {
                            Text("Kalan Borç Bakiyesi", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.remainingBalance} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 2. Ödeme Yöntemi Seçimi
            Text("⚙️ 1. Ödeme Yöntemini Seçin:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = state.selectedMethod == "cash",
                    onClick = { viewModel.setPaymentMethod("cash") },
                    label = { Text("💵 Nakit", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.selectedMethod == "credit_card",
                    onClick = { viewModel.setPaymentMethod("credit_card") },
                    label = { Text("💳 Kredi Kartı", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.selectedMethod == "bank_transfer",
                    onClick = { viewModel.setPaymentMethod("bank_transfer") },
                    label = { Text("🏦 Havale/EFT", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = state.selectedMethod == "online",
                    onClick = { viewModel.setPaymentMethod("online") },
                    label = { Text("🌐 Online", fontSize = 11.sp) }
                )
            }

            // 3. Hızlı Depozito Seçenekleri & Tutar Girişi
            Text("💰 2. Tahsil Edilecek Tutar (Kısmi / Depozito):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val dep25 = ((state.remainingBalance * 0.25) * 100).toInt() / 100.0
                val dep50 = ((state.remainingBalance * 0.50) * 100).toInt() / 100.0

                SuggestionChip(
                    onClick = { paymentAmountStr = dep25.toString() },
                    label = { Text("%25 Depozito ($dep25 TRY)", fontSize = 10.sp) }
                )
                SuggestionChip(
                    onClick = { paymentAmountStr = dep50.toString() },
                    label = { Text("%50 Depozito ($dep50 TRY)", fontSize = 10.sp) }
                )
                SuggestionChip(
                    onClick = { paymentAmountStr = state.remainingBalance.toString() },
                    label = { Text("Tamamı (${state.remainingBalance} TRY)", fontSize = 10.sp) }
                )
            }

            OutlinedTextField(
                value = paymentAmountStr,
                onValueChange = { paymentAmountStr = it },
                label = { Text("Tahsil Edilecek Tutar (TRY)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("Dekont / Referans No") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notlar") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val amt = paymentAmountStr.toDoubleOrNull() ?: 0.0
                    viewModel.processPayment(amt, referenceNo.ifBlank { null }, notes.ifBlank { null })
                    paymentAmountStr = ""
                    referenceNo = ""
                    notes = ""
                },
                enabled = paymentAmountStr.toDoubleOrNull() != null && paymentAmountStr.toDoubleOrNull()!! > 0,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("💵 Ödemeyi Al & Rezervasyona İşle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // 4. Geçmiş Ödemeler Listesi
            Text("📜 Rezervasyon Ödeme Geçmişi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.paymentHistory) { payment ->
                    PaymentHistoryCard(payment = payment)
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryCard(payment: Payment) {
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
                Text("Yöntem: ${payment.paymentMethod.uppercase()} | Tarih: ${payment.paymentDate}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (payment.referenceNo != null) {
                    Text("Dekont: ${payment.referenceNo}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (payment.notes != null) {
                    Text(payment.notes, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text("+${payment.amount} ${payment.currency}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
        }
    }
}

package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.viewmodel.QRTicketViewModel

/**
 * 4.2.3 Dijital QR Bilet Gösterimi ve Giriş Kontrol Tarama Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRTicketScreen(
    viewModel: QRTicketViewModel,
    bookingId: String = "b2c-101",
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val ticket = state.qrTicket

    LaunchedEffect(bookingId) {
        viewModel.loadTicket(bookingId)
    }

    var manualQrInput by remember { mutableStateOf("QR-TKT-9A8B7C6D5E4F") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎟️ Dijital QR Bilet & Geçiş", fontWeight = FontWeight.Bold) },
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
            // Tab Selector
            SecondaryTabRow(selectedTabIndex = state.selectedTab) {
                Tab(selected = state.selectedTab == 0, onClick = { viewModel.selectTab(0) }) {
                    Text("1. Dijital Bilet", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                    Text("2. 📷 QR Geçiş Kontrol", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (state.selectedTab == 0) {
                // TAB 0: Dijital QR Bilet Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(ticket.bookingCode, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text(ticket.passengerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            val (statusColor, statusText) = if (ticket.checkinStatus == "CHECKED_IN") Color(0xFF15803D) to "✅ GİRİŞ YAPILDI" else Color(0xFFEA580C) to "⏳ BEKLİYOR"
                            Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                                Text(statusText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Text("Tur: ${ticket.tourTitle} (${ticket.paxCount} Kişi)", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                        }

                        // Visual QR Matrix Kutu Simülasyonu
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("████  ██  ████", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("██  ██████  ██", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("████  ██  ████", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(ticket.ticketHash, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text("📱 Giriş sırasında görevliye bu QR kodu okutunuz.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(" Apple / Google Wallet'a Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // TAB 1: QR Tarama & Geçiş Kontrol Simülatörü
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("📷 Kamera / Bilet Okutma Paneli", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        // Kamera Vizör Çerçevesi
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷 Kamera QR Kodu Bekleniyor...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = manualQrInput,
                            onValueChange = { manualQrInput = it },
                            label = { Text("Tarayıcı QR Kod / Hash Metni") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { viewModel.scanQRCode(manualQrInput) },
                            enabled = !state.isLoading && manualQrInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            else Text("🔍 QR Kodu Tara ve Girişi Onayla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        state.checkInResult?.let { res ->
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(res.message, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                                    Text("Yolcu: ${res.passengerName} (${res.paxCount} Kişi) | Kodu: ${res.bookingCode}", fontSize = 11.sp, color = Color(0xFF15803D))
                                    Text("Giriş Zamanı: ${res.checkinTime}", fontSize = 10.sp, color = Color(0xFF15803D).copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

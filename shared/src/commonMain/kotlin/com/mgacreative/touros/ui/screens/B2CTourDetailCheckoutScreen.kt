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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.viewmodel.B2CTourDetailCheckoutViewModel

/**
 * 4.2.2 B2C Müşteri Mobil Tur Detay ve 3D Secure Ödeme Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B2CTourDetailCheckoutScreen(
    viewModel: B2CTourDetailCheckoutViewModel,
    tourId: String = "t101",
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(tourId) {
        viewModel.loadTourDetail(tourId)
    }

    var passengerName by remember { mutableStateOf("Elif Yılmaz") }
    var passengerPhone by remember { mutableStateOf("+90 532 111 2233") }
    var passengerEmail by remember { mutableStateOf("elif.yilmaz@email.com") }

    var cardHolder by remember { mutableStateOf("ELIF YILMAZ") }
    var cardNumber by remember { mutableStateOf("4543 2100 8899 4242") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cvv by remember { mutableStateOf("321") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✈️ Tur Detay & Ödeme", fontWeight = FontWeight.Bold) },
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
                    Text("1. Tur Programı", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                    Text("2. Ödeme & Onay", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (state.selectedTab == 0) {
                // TAB 0: Tur Detayları
                val detail = state.tourDetail

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(detail.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Text("⭐ ${detail.rating} Müşteri Puanı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                        }

                        Text(detail.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(detail.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Text("🗓️ Tur Program Özeti", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(detail.itinerarySummary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Text("✅ Fiyata Dahil Hizmetler", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                        detail.includedServices.forEach { s -> Text("• $s", fontSize = 11.sp) }

                        Text("❌ Fiyata Dahil Olmayanlar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                        detail.excludedServices.forEach { s -> Text("• $s", fontSize = 11.sp) }

                        Button(
                            onClick = { viewModel.selectTab(1) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🛒 Ödemeye İlerle (${detail.price} TRY)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // TAB 1: Mobil Checkout Formu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("👤 Yolcu & İletişim Bilgileri", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        OutlinedTextField(
                            value = passengerName,
                            onValueChange = { passengerName = it },
                            label = { Text("Yolcu Adı Soyadı") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = passengerPhone,
                                onValueChange = { passengerPhone = it },
                                label = { Text("Telefon") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = passengerEmail,
                                onValueChange = { passengerEmail = it },
                                label = { Text("E-Posta") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Pax Counter
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Kişi Sayısı:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.updatePaxCount(state.paxCount - 1) }, shape = RoundedCornerShape(8.dp)) { Text("-", fontSize = 16.sp) }
                                Text("${state.paxCount} Kişi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                OutlinedButton(onClick = { viewModel.updatePaxCount(state.paxCount + 1) }, shape = RoundedCornerShape(8.dp)) { Text("+", fontSize = 16.sp) }
                            }
                        }
                    }
                }

                // Kredi Kartı Formu
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("💳 3D Secure Kredi Kartı Ödemesi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                Text("🔒 256-Bit SSL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("Kart Üzerindeki İsim") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Kart Numarası") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it },
                                label = { Text("SKT (AA/YY)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it },
                                label = { Text("CVV") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Toplam Tahsilat Tutarı:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${state.totalPrice} TRY", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = { viewModel.processCheckout(passengerName, passengerPhone, passengerEmail, cardHolder, cardNumber, cardExpiry, cvv) },
                            enabled = !state.isLoading && passengerName.isNotBlank() && cardHolder.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            else Text("💳 ${state.totalPrice} TRY Öde & Rezervasyonu Tamamla", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

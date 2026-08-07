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
import com.mgacreative.touros.ui.viewmodel.CentralPricingHubViewModel

/**
 * 4.3.4 B2C, B2B ve Admin İçin Merkezi PricingEngine Entegrasyon Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentralPricingHubScreen(
    viewModel: CentralPricingHubViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val res = state.response

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Merkezi PricingEngine Hub", fontWeight = FontWeight.Bold) },
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
            // Channel Selector Tabs
            SecondaryTabRow(selectedTabIndex = when (state.selectedChannel) {
                "B2B_AGENCY" -> 1
                "ADMIN_PANEL" -> 2
                else -> 0
            }) {
                Tab(selected = state.selectedChannel == "B2C", onClick = { viewModel.selectChannel("B2C") }) {
                    Text("📱 B2C Mobil", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedChannel == "B2B_AGENCY", onClick = { viewModel.selectChannel("B2B_AGENCY") }) {
                    Text("🏢 B2B Acente", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedChannel == "ADMIN_PANEL", onClick = { viewModel.selectChannel("ADMIN_PANEL") }) {
                    Text("📊 Admin Panel", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Input Parameters Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🎛️ Kanal Girdi Parametreleri", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = "${state.basePrice}",
                                onValueChange = { viewModel.updateInputs(basePrice = it.toDoubleOrNull() ?: 2500.0) },
                                label = { Text("Baz Fiyat (TRY)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = "${state.paxCount}",
                                onValueChange = { viewModel.updateInputs(paxCount = it.toIntOrNull() ?: 1) },
                                label = { Text("Kişi (Pax)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = state.couponCode,
                            onValueChange = { viewModel.updateInputs(couponCode = it) },
                            label = { Text("Kupon Kodu") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Doluluk Oranı: %${state.occupancyRate.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = state.occupancyRate.toFloat(),
                            onValueChange = { viewModel.updateInputs(occupancyRate = it.toDouble()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Central Pricing Response Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("🎯 Merkezi PricingEngine Çıktısı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(state.selectedChannel, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Text(res.appliedRulesSummary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Brüt Toplam Tutar:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${res.grossAmount} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dinamik Doluluk Ayarlaması:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+${res.dynamicAdjustmentAmount} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kampanya & Kupon İndirimi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-${res.campaignDiscountAmount} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }

                        if (state.selectedChannel == "B2B_AGENCY") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Acente Hakediş Komisyonu:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${res.agencyCommissionAmount} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Net Tahsil Edilecek Tutar:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${res.netPayableAmount} TRY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

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
import com.mgacreative.touros.ui.viewmodel.DynamicPricingRuleFormViewModel

/**
 * 4.3.3 Dinamik Fiyatlandırma Kural Yönetimi ve Canlı Önizleme Simülasyon Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicPricingRuleFormScreen(
    viewModel: DynamicPricingRuleFormViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Kural Yönetimi & Simülasyon", fontWeight = FontWeight.Bold) },
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
            // Tab Selector
            SecondaryTabRow(selectedTabIndex = state.selectedTab) {
                Tab(selected = state.selectedTab == 0, onClick = { viewModel.selectTab(0) }) {
                    Text("1. 📝 Kural Tanımla", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                    Text("2. 🔮 Fiyat Önizleme", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (state.selectedTab == 0) {
                // TAB 0: Kural Formu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("📝 Yeni Dinamik Fiyat Kuralı", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            OutlinedTextField(
                                value = state.ruleName,
                                onValueChange = { viewModel.updateForm(ruleName = it) },
                                label = { Text("Kural Adı / Açıklaması") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = "${state.priority}",
                                    onValueChange = { viewModel.updateForm(priority = it.toIntOrNull() ?: 1) },
                                    label = { Text("Öncelik (1=En Yüksek)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = "${state.priceAdjustmentPercent}",
                                    onValueChange = { viewModel.updateForm(priceAdjustmentPercent = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text("Fiyat Değişimi (%)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text("Minimum Doluluk: %${state.minOccupancyRate.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = state.minOccupancyRate.toFloat(),
                                onValueChange = { viewModel.updateForm(minOccupancyRate = it.toDouble()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Sezon Seçimi
                            Text("Sezon Koşulu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("HIGH_SEASON" to "Yüksek", "MID_SEASON" to "Orta", "LOW_SEASON" to "Düşük", "ALL" to "Tümü").forEach { (code, label) ->
                                    FilterChip(
                                        selected = state.season == code,
                                        onClick = { viewModel.updateForm(season = code) },
                                        label = { Text(label, fontSize = 10.sp) }
                                    )
                                }
                            }

                            // Acente Seviyesi
                            Text("Acente Koşulu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("VIP_AGENCY" to "VIP", "REGULAR_AGENCY" to "Standart", "ALL" to "Tümü").forEach { (code, label) ->
                                    FilterChip(
                                        selected = state.agencyTier == code,
                                        onClick = { viewModel.updateForm(agencyTier = code) },
                                        label = { Text(label, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.saveRule() },
                                enabled = !state.isLoading && state.ruleName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                else Text("💾 Kuralı Kaydet & Aktifleştir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Realtime Simulation Preview Tool
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🔮 Canlı Fiyat Simülasyon Aracı", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            OutlinedTextField(
                                value = "${state.sampleBasePrice}",
                                onValueChange = { viewModel.updateForm(sampleBasePrice = it.toDoubleOrNull() ?: 3000.0) },
                                label = { Text("Örnek Tur Baz Fiyatı (TRY)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Simulated Price Breakdown Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Simülasyon Çıktısı", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Baz Fiyat:", fontSize = 12.sp)
                                        Text("${state.sampleBasePrice} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Kural Etkisi:", fontSize = 12.sp)
                                        Text("+${state.priceAdjustmentPercent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Simüle Edilen Satış Fiyatı:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${state.simulatedPrice} TRY", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

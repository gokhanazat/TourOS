package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mgacreative.touros.domain.model.DynamicPricingRule
import com.mgacreative.touros.ui.viewmodel.DynamicPricingRuleEngineViewModel

/**
 * 4.3.2 Dinamik Fiyatlandırma Öncelikli Kural Motoru Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicPricingRuleEngineScreen(
    viewModel: DynamicPricingRuleEngineViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val eval = state.evaluationResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Dinamik Fiyat Kural Motoru", fontWeight = FontWeight.Bold) },
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
                    Text("1. ⚡ Rule Simülatörü", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                    Text("2. 📋 Kural Listesi (${state.rules.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            if (state.selectedTab == 0) {
                // TAB 0: Rule Engine Simulator
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
                            Text("🎛️ Canlı Kural Koşul Parametreleri", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Text("Doluluk Oranı: %${state.occupancyRate.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = state.occupancyRate.toFloat(),
                                onValueChange = { viewModel.updateOccupancyRate(it.toDouble()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Sezon Seçimi
                            Text("Sezon Türü:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("HIGH_SEASON" to "Yüksek", "MID_SEASON" to "Orta", "LOW_SEASON" to "Düşük").forEach { (code, label) ->
                                    FilterChip(
                                        selected = state.selectedSeason == code,
                                        onClick = { viewModel.updateSeason(code) },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Acente Seviyesi Seçimi
                            Text("Acente Tier:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("VIP_AGENCY" to "VIP Acente", "REGULAR_AGENCY" to "Standart", "ALL" to "Tümü").forEach { (code, label) ->
                                    FilterChip(
                                        selected = state.selectedAgencyTier == code,
                                        onClick = { viewModel.updateAgencyTier(code) },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Ülke Pazar Seçimi
                            Text("Hedef Ülke Pazar:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("GERMANY" to "Almanya/AB", "JAPAN" to "Japonya", "DOMESTIC" to "İç Pazar", "ALL" to "Tümü").forEach { (code, label) ->
                                    FilterChip(
                                        selected = state.selectedCountry == code,
                                        onClick = { viewModel.updateCountry(code) },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Rule Engine Dynamic Price Output Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("⚡ Öncelikli Kural Motoru Hesaplama Sonucu", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                Text("Eşleşen Öncelikli Kural: ${eval.matchedRuleName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baz Tur Fiyatı:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${eval.basePrice} TRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Fiyat Değişim Oranı:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val sign = if (eval.totalAdjustmentPercent >= 0) "+" else ""
                                Text("$sign${eval.totalAdjustmentPercent}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (eval.totalAdjustmentPercent >= 0) Color(0xFFD97706) else Color(0xFF15803D))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Dinamik Son Fiyat:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${eval.adjustedPrice} TRY", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            Text("Uygulanan Kurallar: ${eval.appliedRulesSummary}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                // TAB 1: Rules List
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.rules) { rule ->
                            DynamicPricingRuleCardItem(rule = rule)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicPricingRuleCardItem(rule: DynamicPricingRule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("ÖNCELİK #${rule.priority}", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                val sign = if (rule.priceAdjustmentPercent >= 0) "+" else ""
                Text("$sign${rule.priceAdjustmentPercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (rule.priceAdjustmentPercent >= 0) Color(0xFFD97706) else Color(0xFF15803D))
            }

            Text(rule.ruleName, fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Sezon: ${rule.season}", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Doluluk: >%${rule.minOccupancyRate.toInt()}", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Acente: ${rule.agencyTier}", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

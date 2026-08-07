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
import com.mgacreative.touros.domain.model.CommissionRule
import com.mgacreative.touros.ui.viewmodel.CommissionRulesUiState
import com.mgacreative.touros.ui.viewmodel.CommissionRulesViewModel

/**
 * 3.1.6 Komisyon Kuralları & Canlı Hesaplayıcı Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionRulesScreen(
    viewModel: CommissionRulesViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Komisyon Kuralları & Motoru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("➕ Yeni Kural Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                is CommissionRulesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CommissionRulesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is CommissionRulesUiState.Success -> {
                    // 1. Canlı Hesaplama Simülatör Kartı
                    CommissionSimulatorCard(
                        rules = state.rules,
                        simulatedResultText = state.simulatedResultText,
                        onSimulate = { price, rule -> viewModel.simulateCalculation(price, rule) }
                    )

                    // 2. Kural Listesi
                    Text("📋 Yapılandırılmış Komisyon Kuralları (${state.rules.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.rules) { rule ->
                            CommissionRuleCard(
                                rule = rule,
                                onSimulateClick = { viewModel.simulateCalculation(10000.0, rule) }
                            )
                        }
                    }

                    // Yeni Kural Ekleme Dialog
                    if (showCreateDialog) {
                        CreateCommissionRuleDialog(
                            onDismiss = { showCreateDialog = false },
                            onCreate = { name, agent, tour, type, rate, fixed ->
                                viewModel.saveRule(name, agent, tour, type, rate, fixed)
                                showCreateDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommissionSimulatorCard(
    rules: List<CommissionRule>,
    simulatedResultText: String?,
    onSimulate: (price: Double, rule: CommissionRule) -> Unit
) {
    var priceStr by remember { mutableStateOf("10000") }
    var selectedRule by remember { mutableStateOf(rules.firstOrNull()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("🧮 Canlı Komisyon Hesaplama Simülatörü", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Satış Tutarı (TRY)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: 0.0
                        val r = selectedRule ?: rules.firstOrNull()
                        if (r != null) onSimulate(p, r)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Hesapla", fontWeight = FontWeight.Bold)
                }
            }

            if (simulatedResultText != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(simulatedResultText, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Composable
fun CommissionRuleCard(
    rule: CommissionRule,
    onSimulateClick: () -> Unit
) {
    val (typeText, typeBg, typeFg) = if (rule.calculationType == "percentage") {
        Triple("%${rule.rateValue} Oran", Color(0xFFDBEAFE), Color(0xFF1E40AF))
    } else {
        Triple("${rule.fixedAmount} TRY Sabit", Color(0xFFFEF3C7), Color(0xFF92400E))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(rule.ruleName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(shape = RoundedCornerShape(6.dp), color = typeBg) {
                    Text(typeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = typeFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (rule.agentName != null) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("🏢 Acente: ${rule.agentName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                if (rule.tourName != null) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text("🏔️ Tur: ${rule.tourName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                if (rule.agentName == null && rule.tourName == null) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("🌐 Genel Kural (Tüm Rezervasyonlar)", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Durum: 🟢 Aktif", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)

                TextButton(onClick = onSimulateClick) {
                    Text("🧮 Simülatörde Test Et", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateCommissionRuleDialog(
    onDismiss: () -> Unit,
    onCreate: (ruleName: String, agentName: String?, tourName: String?, calculationType: String, rateValue: Double, fixedAmount: Double) -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var agentName by remember { mutableStateOf("") }
    var tourName by remember { mutableStateOf("") }
    var calculationType by remember { mutableStateOf("percentage") }
    var valueStr by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ Yeni Komisyon Kuralı", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = ruleName, onValueChange = { ruleName = it }, label = { Text("Kural Adı") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = agentName, onValueChange = { agentName = it }, label = { Text("Acente Adı (Opsiyonel)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tourName, onValueChange = { tourName = it }, label = { Text("Tur Adı (Opsiyonel)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = calculationType == "percentage", onClick = { calculationType = "percentage" }, label = { Text("% Oran") })
                    FilterChip(selected = calculationType == "fixed_amount", onClick = { calculationType = "fixed_amount" }, label = { Text("Sabit Tutar") })
                }

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = { Text(if (calculationType == "percentage") "Oran (%)" else "Sabit Tutar (TRY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = valueStr.toDoubleOrNull() ?: 0.0
                    val rate = if (calculationType == "percentage") v else 0.0
                    val fixed = if (calculationType == "fixed_amount") v else 0.0
                    onCreate(ruleName, agentName.ifBlank { null }, tourName.ifBlank { null }, calculationType, rate, fixed)
                },
                enabled = ruleName.isNotBlank() && valueStr.toDoubleOrNull() != null
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

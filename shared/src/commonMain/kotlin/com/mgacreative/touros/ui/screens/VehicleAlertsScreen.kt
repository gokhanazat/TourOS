package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import com.mgacreative.touros.domain.model.VehicleMaintenanceAlert
import com.mgacreative.touros.ui.viewmodel.VehicleAlertsUiState
import com.mgacreative.touros.ui.viewmodel.VehicleAlertsViewModel

/**
 * 2.4.4 Araç Bakım/Sigorta/Muayene Uyarı Mekanizması Yönetim Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleAlertsScreen(
    viewModel: VehicleAlertsViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 Filo Bakım & Sigorta Uyarıları", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is VehicleAlertsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VehicleAlertsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is VehicleAlertsUiState.Success -> {
                    // 1. Özet Sayacı (Kritik ve Yaklaşan Uvarılar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🔴 Kritik Uvarılar (≤7 Gün)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("${state.criticalCount} Araç", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🟡 Yaklaşan (≤30 Gün)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("${state.warningCount} Araç", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    // 2. Kategori Çipleri (Tümü, Sigorta, Muayene, Bakım)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedFilterType == null,
                            onClick = { viewModel.setFilterType(null) },
                            label = { Text("Tüm Uyarılar", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedFilterType == "INSURANCE_EXPIRING",
                            onClick = { viewModel.setFilterType("INSURANCE_EXPIRING") },
                            label = { Text("🛡️ Sigorta", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedFilterType == "INSPECTION_EXPIRING",
                            onClick = { viewModel.setFilterType("INSPECTION_EXPIRING") },
                            label = { Text("🔍 Muayene", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedFilterType == "MAINTENANCE_DUE",
                            onClick = { viewModel.setFilterType("MAINTENANCE_DUE") },
                            label = { Text("🛠️ Bakım", fontSize = 11.sp) }
                        )
                    }

                    if (state.alerts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("Seçilen kategoride acil uyarı bulunmamaktadır. Tüm filo güncel!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(state.alerts) { alert ->
                                VehicleAlertItemCard(alert = alert)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleAlertItemCard(alert: VehicleMaintenanceAlert) {
    val isCritical = alert.severity == "CRITICAL" || alert.daysLeft <= 7

    val (titleLabel, icon) = when (alert.alertType) {
        "INSURANCE_EXPIRING" -> "Sigorta Bitiş Tarihi Yaklaştı" to "🛡️"
        "INSPECTION_EXPIRING" -> "Araç Muayene Tarihi Yaklaştı" to "🔍"
        else -> "Periyodik Bakım Zamanı Geldi" to "🛠️"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = alert.plateNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (isCritical) "🚨 KRİTİK: ${alert.daysLeft} Gün Kaldı" else "⚠️ UYARI: ${alert.daysLeft} Gün Kaldı",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "$icon $titleLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Araç: ${alert.brandModel} | Son Geçerlilik: ${alert.expiryDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { /* Bakım Randevusu / Yenileme */ }, shape = RoundedCornerShape(8.dp)) {
                    Text("🔧 Randevu Al / Yenile", fontSize = 11.sp)
                }
            }
        }
    }
}

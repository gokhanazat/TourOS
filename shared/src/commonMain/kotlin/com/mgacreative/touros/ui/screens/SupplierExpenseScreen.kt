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
import com.mgacreative.touros.domain.model.SupplierTransaction
import com.mgacreative.touros.ui.viewmodel.SupplierExpenseUiState
import com.mgacreative.touros.ui.viewmodel.SupplierExpenseViewModel

/**
 * 3.1.3 Tedarikçi Cari & Otomatik Gider Akışı Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierExpenseScreen(
    viewModel: SupplierExpenseViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏢 Tedarikçi Cari & Gider Akışı", fontWeight = FontWeight.Bold) },
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
                is SupplierExpenseUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SupplierExpenseUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is SupplierExpenseUiState.Success -> {
                    // 1. KPI Özet Borç Kartları
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🏨 Otel Borçları", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.totalHotelDebt} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🚌 Araç Borçları", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.totalVehicleDebt} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🚩 Rehber Borçları", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.totalGuideDebt} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }

                    if (state.notificationMessage != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                            Text(state.notificationMessage, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }

                    // 2. Kategori Filtre Çipleri
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = state.selectedCategoryFilter == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("Tüm Tedarikçiler", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedCategoryFilter == "hotel",
                            onClick = { viewModel.setCategoryFilter("hotel") },
                            label = { Text("🏨 Oteller", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedCategoryFilter == "vehicle",
                            onClick = { viewModel.setCategoryFilter("vehicle") },
                            label = { Text("🚌 Araçlar", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedCategoryFilter == "guide",
                            onClick = { viewModel.setCategoryFilter("guide") },
                            label = { Text("🚩 Rehberler", fontSize = 11.sp) }
                        )
                    }

                    // 3. Tedarikçi Cari Defteri & Gider Kartları Listesi
                    Text("📖 Tedarikçi Cari Defteri & Otomatik Gider İşlemleri:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.transactions) { item ->
                            SupplierLedgerCard(
                                item = item,
                                onSettleClick = { viewModel.settleTransaction(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierLedgerCard(
    item: SupplierTransaction,
    onSettleClick: () -> Unit
) {
    val categoryIcon = when (item.supplierType) {
        "hotel" -> "🏨 Otel"
        "vehicle" -> "🚌 Araç"
        "guide" -> "🚩 Rehber"
        else -> "🏢 Tedarikçi"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(categoryIcon, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }

                Surface(shape = RoundedCornerShape(6.dp), color = if (item.isSettled) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        text = if (item.isSettled) "🟢 Ödendi & Giderleşti" else "🔴 Bekleyen Borç",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isSettled) Color(0xFF15803D) else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(item.supplierName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("📝 ${item.description}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Cari Borç Tutarı", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${item.amount} ${item.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = onSettleClick,
                    enabled = !item.isSettled,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (item.isSettled) "✅ Gider Kaydı İşlendi" else "⚡ Ödeme Yap & Gider Kaydı Oluştur", fontSize = 11.sp)
                }
            }
        }
    }
}

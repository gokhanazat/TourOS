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
import com.mgacreative.touros.domain.model.ReportExportResult
import com.mgacreative.touros.ui.viewmodel.ReportFilterExportUiState
import com.mgacreative.touros.ui.viewmodel.ReportFilterExportViewModel

import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors

/**
 * 3.3.4 Rapor Filtreleme ve PDF/Excel Export Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterExportScreen(
    viewModel: ReportFilterExportViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val filter = state.filter

    var startDate by remember { mutableStateOf(filter.startDate) }
    var endDate by remember { mutableStateOf(filter.endDate) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "📄 Rapor Filtreleme & Export",
                subtitle = "Tarih ve modül bazlı dinamik rapor çıktısı alma",
                onNavigateBack = onNavigateBack
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
            if (state.notificationMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(state.notificationMessage!!, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // 1. Filtreleme Paneli Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚙️ Rapor Filtreleme Parametreleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Firma Seçimi
                    Text("🏢 Firma Seçimi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.availableCompanies.take(3).forEach { comp ->
                            FilterChip(
                                selected = filter.companyName == comp,
                                onClick = { viewModel.updateFilter(company = comp) },
                                label = { Text(comp, fontSize = 10.sp) }
                            )
                        }
                    }

                    // Para Birimi Seçimi
                    Text("💱 Para Birimi Filtresi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.availableCurrencies.forEach { curr ->
                            FilterChip(
                                selected = filter.currency == curr,
                                onClick = { viewModel.updateFilter(currency = curr) },
                                label = { Text(curr, fontSize = 10.sp) }
                            )
                        }
                    }

                    // Tarih Aralığı
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {
                                startDate = it
                                viewModel.updateFilter(startDate = it)
                            },
                            label = { Text("Başlangıç Tarihi") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {
                                endDate = it
                                viewModel.updateFilter(endDate = it)
                            },
                            label = { Text("Bitiş Tarihi") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Export Butonları
                    Text("🚀 Dışa Aktarma Formatı (Export):", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.exportReport("pdf") },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("📄 PDF Üret & İndir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.exportReport("excel") },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                        ) {
                            Text("📊 Excel (XLSX) İndir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Üretilen Rapor Belgeleri Geçmişi
            Text("📜 İndirilen Rapor Belgeleri Geçmişi:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(state.exportHistory) { item ->
                    ExportHistoryCard(item = item)
                }
            }
        }
    }
}

@Composable
fun ExportHistoryCard(item: ReportExportResult) {
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(item.documentName, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${item.createdAt} | ${item.recordCount} Kayıt İçeriyor", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(shape = RoundedCornerShape(6.dp), color = if (item.formatType == "PDF") MaterialTheme.colorScheme.errorContainer else Color(0xFFDCFCE7)) {
                Text(item.formatType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (item.formatType == "PDF") MaterialTheme.colorScheme.onErrorContainer else Color(0xFF15803D), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

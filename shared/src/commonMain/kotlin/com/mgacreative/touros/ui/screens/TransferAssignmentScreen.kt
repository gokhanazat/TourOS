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
import com.mgacreative.touros.domain.model.Driver
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.TransferTask
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.ui.viewmodel.AssignmentDialogState
import com.mgacreative.touros.ui.viewmodel.TransferAssignmentUiState
import com.mgacreative.touros.ui.viewmodel.TransferAssignmentViewModel

/**
 * 2.4.2 Şoför ve Rehber Transfer Görevi Atama Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferAssignmentScreen(
    viewModel: TransferAssignmentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚐 Şoför & Rehber Transfer Atama", fontWeight = FontWeight.Bold) },
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
                is TransferAssignmentUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TransferAssignmentUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is TransferAssignmentUiState.Success -> {
                    // Atama Dialog Açıksa
                    if (dialogState.isOpen && dialogState.transfer != null) {
                        TransferAssignmentDialog(
                            dialogState = dialogState,
                            drivers = state.drivers,
                            guides = state.guides,
                            vehicles = state.vehicles,
                            onSelectDriver = { viewModel.selectDriver(it) },
                            onSelectGuide = { viewModel.selectGuide(it) },
                            onSelectVehicle = { viewModel.selectVehicle(it) },
                            onSave = { viewModel.saveAssignment() },
                            onCancel = { viewModel.closeAssignmentDialog() }
                        )
                    }

                    // Durum Filtre Çipleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedStatusFilter == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("Tüm Görevler", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedStatusFilter == "planned",
                            onClick = { viewModel.setStatusFilter("planned") },
                            label = { Text("📋 Planlanan", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedStatusFilter == "assigned",
                            onClick = { viewModel.setStatusFilter("assigned") },
                            label = { Text("👤 Atanan", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedStatusFilter == "completed",
                            onClick = { viewModel.setStatusFilter("completed") },
                            label = { Text("✅ Tamamlanan", fontSize = 11.sp) }
                        )
                    }

                    if (state.transfers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("Bu filtreye uygun transfer görevi bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(state.transfers) { transfer ->
                                val driver = state.drivers.find { it.id == transfer.driverId }
                                val guide = state.guides.find { it.id == transfer.guideId }
                                val vehicle = state.vehicles.find { it.id == transfer.vehicleId }

                                TransferTaskCard(
                                    transfer = transfer,
                                    driver = driver,
                                    guide = guide,
                                    vehicle = vehicle,
                                    onAssignClick = { viewModel.openAssignmentDialog(transfer) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferTaskCard(
    transfer: TransferTask,
    driver: Driver?,
    guide: Guide?,
    vehicle: Vehicle?,
    onAssignClick: () -> Unit
) {
    val transferTypeLabel = when (transfer.transferType) {
        "airport" -> "✈️ Havalimanı Transferi"
        "tour" -> "🏞️ Tur Transferi"
        "intercity" -> "🛣️ Şehirler Arası Transfer"
        else -> "🚐 Özel Transfer"
    }

    val isFullyAssigned = driver != null && guide != null && vehicle != null

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
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = transferTypeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isFullyAssigned) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isFullyAssigned) "✅ Atama Tamamlandı" else "⚠️ Atama Bekliyor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullyAssigned) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Güzergah & Zaman
            Column {
                Text("📍 ${transfer.origin}  ➜  ${transfer.destination}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("📅 Alış Tarihi: ${transfer.pickupTime ?: "Tarih Belirtilmedi"} | 👥 ${transfer.paxCount} Yolcu (Pax)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Atanan Şoför, Rehber ve Araç Özeti
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Şoför Kartı
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (driver != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("👨‍✈️ Şoför", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(driver?.fullName ?: "Atanmadı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (driver != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                        if (driver?.phone != null) {
                            Text(driver.phone, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Rehber Kartı
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (guide != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("🚩 Kokartlı Rehber", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(guide?.fullName ?: "Atanmadı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (guide != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                        if (guide?.languages != null) {
                            Text(guide.languages.joinToString(", "), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Araç Kartı
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (vehicle != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("🚌 Araç", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(vehicle?.plateNumber ?: "Atanmadı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (vehicle != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                        if (vehicle?.model != null) {
                            Text(vehicle.model, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (!transfer.notes.isNullOrBlank()) {
                Text("📝 Not: ${transfer.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = onAssignClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isFullyAssigned) "✏️ Atamayı Güncelle" else "👤 Şoför / Rehber Atama Yap")
            }
        }
    }
}

@Composable
fun TransferAssignmentDialog(
    dialogState: AssignmentDialogState,
    drivers: List<Driver>,
    guides: List<Guide>,
    vehicles: List<Vehicle>,
    onSelectDriver: (String?) -> Unit,
    onSelectGuide: (String?) -> Unit,
    onSelectVehicle: (String?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val transfer = dialogState.transfer ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("👤 Transfer Görevi Atama Formu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onCancel) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Text("📍 Güzergah: ${transfer.origin} ➜ ${transfer.destination}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            // Şoför Seçimi
            Text("👨‍✈️ Şoför Seçin:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = dialogState.selectedDriverId == null,
                    onClick = { onSelectDriver(null) },
                    label = { Text("Atama Yapma", fontSize = 11.sp) }
                )
                drivers.forEach { d ->
                    FilterChip(
                        selected = dialogState.selectedDriverId == d.id,
                        onClick = { onSelectDriver(d.id) },
                        label = { Text("${d.fullName} (${d.licenseClass ?: "D1"})", fontSize = 11.sp) }
                    )
                }
            }

            // Rehber Seçimi
            Text("🚩 Kokartlı Rehber Seçin:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = dialogState.selectedGuideId == null,
                    onClick = { onSelectGuide(null) },
                    label = { Text("Atama Yapma", fontSize = 11.sp) }
                )
                guides.forEach { g ->
                    FilterChip(
                        selected = dialogState.selectedGuideId == g.id,
                        onClick = { onSelectGuide(g.id) },
                        label = { Text(g.fullName, fontSize = 11.sp) }
                    )
                }
            }

            // Araç Seçimi
            Text("🚌 Filodan Araç Seçin:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = dialogState.selectedVehicleId == null,
                    onClick = { onSelectVehicle(null) },
                    label = { Text("Atama Yapma", fontSize = 11.sp) }
                )
                vehicles.forEach { v ->
                    FilterChip(
                        selected = dialogState.selectedVehicleId == v.id,
                        onClick = { onSelectVehicle(v.id) },
                        label = { Text("${v.plateNumber} (${v.capacity} Pax)", fontSize = 11.sp) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("İptal")
                }
                Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                    Text("💾 Atamayı Kaydet")
                }
            }
        }
    }
}

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
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.ui.viewmodel.VehicleFormState
import com.mgacreative.touros.ui.viewmodel.VehicleManagementViewModel
import com.mgacreative.touros.ui.viewmodel.VehicleUiState

/**
 * 2.4.1 Araç Veri Modeli ve Filo CRUD Yönetim Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleManagementScreen(
    viewModel: VehicleManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚌 Araç Parkı & Filo Yönetimi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!formState.isFormOpen) {
                FloatingActionButton(
                    onClick = { viewModel.openNewForm() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("+ Araç Ekle", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
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
                is VehicleUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VehicleUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is VehicleUiState.Success -> {
                    if (formState.isFormOpen) {
                        // Araç Ekleme ve Düzenleme Formu
                        VehicleFormCard(
                            formState = formState,
                            onPlateNumberChange = { viewModel.updatePlateNumber(it) },
                            onBrandChange = { viewModel.updateBrand(it) },
                            onModelChange = { viewModel.updateModel(it) },
                            onYearChange = { viewModel.updateYear(it) },
                            onCapacityChange = { viewModel.updateCapacity(it) },
                            onVehicleTypeChange = { viewModel.updateVehicleType(it) },
                            onColorChange = { viewModel.updateColor(it) },
                            onIsOwnedChange = { viewModel.updateIsOwned(it) },
                            onOwnerInfoChange = { viewModel.updateOwnerInfo(it) },
                            onInsuranceExpiryChange = { viewModel.updateInsuranceExpiry(it) },
                            onInspectionExpiryChange = { viewModel.updateInspectionExpiry(it) },
                            onLastMaintenanceChange = { viewModel.updateLastMaintenanceDate(it) },
                            onNextMaintenanceChange = { viewModel.updateNextMaintenanceDate(it) },
                            onMaintenanceNotesChange = { viewModel.updateMaintenanceNotes(it) },
                            onIsActiveChange = { viewModel.updateIsActive(it) },
                            onSave = { viewModel.saveVehicle() },
                            onCancel = { viewModel.closeForm() }
                        )
                    } else {
                        // Araç Tipi Filtre Çipleri (Tümü, Otobüs, Minibüs, VIP Araç)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedFilterType == null,
                                onClick = { viewModel.setVehicleTypeFilter(null) },
                                label = { Text("Tüm Filo", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = state.selectedFilterType == "bus",
                                onClick = { viewModel.setVehicleTypeFilter("bus") },
                                label = { Text("🚌 Otobüs", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = state.selectedFilterType == "minibus",
                                onClick = { viewModel.setVehicleTypeFilter("minibus") },
                                label = { Text("🚐 Minibüs", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = state.selectedFilterType == "vip",
                                onClick = { viewModel.setVehicleTypeFilter("vip") },
                                label = { Text("🚘 VIP Araç", fontSize = 12.sp) }
                            )
                        }

                        if (state.vehicles.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Bu tipe ait araç kaydı bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(state.vehicles) { vehicle ->
                                    VehicleItemCard(
                                        vehicle = vehicle,
                                        onEditClick = { viewModel.openEditForm(vehicle) },
                                        onDeleteClick = { viewModel.deleteVehicle(vehicle.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleItemCard(
    vehicle: Vehicle,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val vehicleTypeLabel = when (vehicle.vehicleType) {
        "bus" -> "🚌 Otobüs"
        "minibus" -> "🚐 Minibüs"
        "vip" -> "🚘 VIP Araç"
        else -> "🚗 Binek/Diğer"
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
                // Plaka Rozeti
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = vehicle.plateNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = vehicleTypeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "${vehicle.brand ?: ""} ${vehicle.model ?: "Belirtilmedi"} (${vehicle.year ?: "-"})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "💺 Koltuk Kapasitesi: ${vehicle.capacity} Pax | 🎨 Renk: ${vehicle.color ?: "Belirtilmedi"} | ${if (vehicle.isOwned) "🏢 Öz Mal Firma Aracı" else "🤝 Kiralık Araç: ${vehicle.ownerInfo ?: "-"}"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Sigorta, Muayene ve Bakım Takip Bilgileri
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("🛡️ Sigorta Bitiş", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(vehicle.insuranceExpiry ?: "Belirtilmedi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("🔍 Muayene Bitiş", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(vehicle.inspectionExpiry ?: "Belirtilmedi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("🛠️ Gelecek Bakım", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(vehicle.nextMaintenanceDate ?: "Planlanmadı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (!vehicle.maintenanceNotes.isNullOrBlank()) {
                Text(
                    text = "🔧 Bakım Notu: ${vehicle.maintenanceNotes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDeleteClick) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onEditClick, shape = RoundedCornerShape(8.dp)) {
                    Text("Düzenle", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun VehicleFormCard(
    formState: VehicleFormState,
    onPlateNumberChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIsOwnedChange: (Boolean) -> Unit,
    onOwnerInfoChange: (String) -> Unit,
    onInsuranceExpiryChange: (String) -> Unit,
    onInspectionExpiryChange: (String) -> Unit,
    onLastMaintenanceChange: (String) -> Unit,
    onNextMaintenanceChange: (String) -> Unit,
    onMaintenanceNotesChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (formState.isEditing) "✏️ Araç Bilgilerini Düzenle" else "➕ Yeni Araç Ekle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCancel) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            // Araç Tipi Seçimi (Otobüs, Minibüs, VIP)
            Text("Araç Tipi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("bus" to "🚌 Otobüs", "minibus" to "🚐 Minibüs", "vip" to "🚘 VIP Araç").forEach { (code, label) ->
                    FilterChip(
                        selected = formState.vehicleType == code,
                        onClick = { onVehicleTypeChange(code) },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.plateNumber,
                    onValueChange = onPlateNumberChange,
                    label = { Text("Plaka (Örn: 34 TOUR 01)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.capacity,
                    onValueChange = onCapacityChange,
                    label = { Text("Koltuk Sayısı (Kapasite)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.brand,
                    onValueChange = onBrandChange,
                    label = { Text("Marka (Örn: Mercedes)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.model,
                    onValueChange = onModelChange,
                    label = { Text("Model (Örn: Travego / Sprinter)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.year,
                    onValueChange = onYearChange,
                    label = { Text("Model Yılı") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.color,
                    onValueChange = onColorChange,
                    label = { Text("Renk") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Sahiplik Durumu (Öz Mal / Kiralık)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(checked = formState.isOwned, onCheckedChange = onIsOwnedChange)
                Text("Firma Öz Mal Aracı", fontSize = 12.sp)
            }

            if (!formState.isOwned) {
                OutlinedTextField(
                    value = formState.ownerInfo,
                    onValueChange = onOwnerInfoChange,
                    label = { Text("Kiralık Firma Bilgisi / Tedarikçi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Sigorta, Muayene ve Bakım Tarihleri
            Text("🛡️ Sigorta, Muayene ve Bakım Takibi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.insuranceExpiry,
                    onValueChange = onInsuranceExpiryChange,
                    label = { Text("Sigorta Bitiş (YYYY-AA-GG)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.inspectionExpiry,
                    onValueChange = onInspectionExpiryChange,
                    label = { Text("Muayene Bitiş (YYYY-AA-GG)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.lastMaintenanceDate,
                    onValueChange = onLastMaintenanceChange,
                    label = { Text("Son Bakım Tarihi") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.nextMaintenanceDate,
                    onValueChange = onNextMaintenanceChange,
                    label = { Text("Gelecek Bakım Tarihi") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = formState.maintenanceNotes,
                onValueChange = onMaintenanceNotesChange,
                label = { Text("Bakım Notları & Detaylar") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = formState.isActive, onCheckedChange = onIsActiveChange)
                Text("Aracı Aktif Filoda Göster", fontSize = 12.sp)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.plateNumber.isNotBlank() && formState.capacity.isNotBlank()
            ) {
                Text("💾 Aracı Kaydet ve Filoya Ekle")
            }
        }
    }
}

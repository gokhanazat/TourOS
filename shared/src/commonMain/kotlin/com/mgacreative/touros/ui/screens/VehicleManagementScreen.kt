package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.VehicleFormState
import com.mgacreative.touros.ui.viewmodel.VehicleManagementViewModel
import com.mgacreative.touros.ui.viewmodel.VehicleUiState

private data class VehicleTypeFilter(val key: String?, val label: String, val icon: String)

private val vehicleFilters = listOf(
    VehicleTypeFilter(null, "Tüm Filo", "🚗"),
    VehicleTypeFilter("bus", "Otobüs", "🚌"),
    VehicleTypeFilter("minibus", "Minibüs", "🚐"),
    VehicleTypeFilter("vip", "VIP Araç", "🚘"),
    VehicleTypeFilter("other", "Diğer", "🚗")
)

/** Tarih YYYY-AA-GG formatında ise yaklaşan tarih uyarısı verir. */
private fun isDateExpiringSoon(dateStr: String?): Boolean {
    if (dateStr.isNullOrBlank()) return false
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            year <= 2026 && month <= 9
        } else false
    } catch (e: Exception) {
        false
    }
}

/** Warning rozeti composable’ ı */
@Composable
private fun WarningBadge(label: String = "Bakım/Sigorta Yaklaşıyor") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(TourOSColors.WarningContainer)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(TourOSColors.Warning)
        )
        Text("⚠️ $label", style = TourOSTypography.Caption.copy(color = TourOSColors.Warning))
    }
}

/**
 * Araç Parkı & Filo Yönetimi — TourOS 0.3
 *
 * Expanded: tablo görünümü  |  Compact: kart listesi
 * Üstte filtre chip'leri + KPI satırı, formlar AnimatedVisibility ile açılır
 */
@Composable
fun VehicleManagementScreen(
    viewModel: VehicleManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Araç Parkı & Filo",
                subtitle = "Araç yönetimi, sigorta, muayene ve bakım takibi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is VehicleUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is VehicleUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is VehicleUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── KPI ────────────────────────────────────────────────
                        item {
                            VehicleKpiRow(vehicles = state.vehicles)
                        }

                        // ── Filtre Chip'leri ────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    items(vehicleFilters) { filter ->
                                        FilterChip(
                                            selected = state.selectedFilterType == filter.key,
                                            onClick = { viewModel.setVehicleTypeFilter(filter.key) },
                                            label = {
                                                Text("${filter.icon} ${filter.label}", style = TourOSTypography.Caption)
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                selectedLabelColor = TourOSColors.Primary
                                            )
                                        )
                                    }
                                }
                                if (!formState.isFormOpen) {
                                    TourOSButton(
                                        text = "+ Araç Ekle",
                                        onClick = { viewModel.openNewForm() },
                                        variant = TourOSButtonVariant.PRIMARY
                                    )
                                }
                            }
                        }

                        // ── Form ────────────────────────────────────────────────
                        item {
                            AnimatedVisibility(
                                visible = formState.isFormOpen,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                VehicleForm(
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
                            }
                        }

                        // ── Liste / Tablo ───────────────────────────────────────
                        if (state.vehicles.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Kayıtlı araç bulunamadı.\n+ Araç Ekle butonu ile filo oluşturun.",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else if (isExpanded) {
                            // Expanded: Tablo
                            item { VehicleTable(vehicles = state.vehicles, onEditClick = { viewModel.openEditForm(it) }, onDeleteClick = { viewModel.deleteVehicle(it.id) }) }
                        } else {
                            // Compact: Kartlar
                            items(state.vehicles) { vehicle ->
                                VehicleCard(
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

// ─── KPI ─────────────────────────────────────────────────────────────────────

@Composable
private fun VehicleKpiRow(vehicles: List<Vehicle>) {
    val active = vehicles.count { it.isActive }
    val owned = vehicles.count { it.isOwned }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
        VehicleKpi("Toplam Araç", vehicles.size.toString(), TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
        VehicleKpi("Aktif", active.toString(), TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        VehicleKpi("Öz Mal", owned.toString(), TourOSColors.SecondaryContainer, TourOSColors.Secondary, Modifier.weight(1f))
        VehicleKpi("Kiralık", (vehicles.size - owned).toString(), TourOSColors.Surface, TourOSColors.TextSecondary, Modifier.weight(1f))
    }
}

@Composable
private fun VehicleKpi(label: String, value: String, bg: Color, text: Color, modifier: Modifier) {
    TourOSCard(modifier = modifier, backgroundColor = bg, contentPadding = TourOSSpacing.small) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = TourOSTypography.TitleLarge.copy(color = text))
            Text(label, style = TourOSTypography.Caption.copy(color = text.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
        }
    }
}

// ─── Expanded: Tablo ─────────────────────────────────────────────────────────

@Composable
private fun VehicleTable(
    vehicles: List<Vehicle>,
    onEditClick: (Vehicle) -> Unit,
    onDeleteClick: (Vehicle) -> Unit
) {
    val headers = listOf("Plaka", "Marka / Model", "Tip", "Kapasite", "Sahiplik", "Sigorta Bitiş", "Muayene Bitiş", "Sonraki Bakım", "Durum", "İşlem")
    val weights  = listOf(1.0f, 1.4f, 1.0f, 0.8f, 0.9f, 1.1f, 1.1f, 1.1f, 0.8f, 1.0f)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Primary)
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        h,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary),
                        modifier = Modifier.weight(weights[i])
                    )
                }
            }

            // Satırlar
            vehicles.forEachIndexed { idx, v ->
                val bg = if (idx % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                val typeLabel = vehicleTypeLabel(v.vehicleType)
                Row(
                    modifier = Modifier.fillMaxWidth().background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plaka
                    Box(
                        modifier = Modifier
                            .weight(weights[0])
                            .clip(RoundedCornerShape(4.dp))
                            .background(TourOSColors.TextPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(v.plateNumber, style = TourOSTypography.Label.copy(color = Color.White))
                    }
                    // Marka / Model
                    Text("${v.brand ?: ""} ${v.model ?: ""} (${v.year ?: "-"})", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[1]))
                    // Tip
                    Text(typeLabel, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), modifier = Modifier.weight(weights[2]))
                    // Kapasite
                    Text("${v.capacity} Pax", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(weights[3]))
                    // Sahiplik
                    Text(if (v.isOwned) "Öz Mal" else "Kiralık", style = TourOSTypography.Caption.copy(color = if (v.isOwned) TourOSColors.Success else TourOSColors.Secondary), modifier = Modifier.weight(weights[4]))
                    // Sigorta
                    Row(modifier = Modifier.weight(weights[5]), verticalAlignment = Alignment.CenterVertically) {
                        val isWarn = isDateExpiringSoon(v.insuranceExpiry)
                        Text(
                            v.insuranceExpiry ?: "-",
                            style = TourOSTypography.Caption.copy(color = if (isWarn) TourOSColors.Warning else TourOSColors.TextSecondary)
                        )
                        if (isWarn) {
                            Spacer(Modifier.width(2.dp))
                            Text("⚠️", style = TourOSTypography.Caption)
                        }
                    }
                    // Muayene
                    Row(modifier = Modifier.weight(weights[6]), verticalAlignment = Alignment.CenterVertically) {
                        val isWarn = isDateExpiringSoon(v.inspectionExpiry)
                        Text(
                            v.inspectionExpiry ?: "-",
                            style = TourOSTypography.Caption.copy(color = if (isWarn) TourOSColors.Warning else TourOSColors.TextSecondary)
                        )
                        if (isWarn) {
                            Spacer(Modifier.width(2.dp))
                            Text("⚠️", style = TourOSTypography.Caption)
                        }
                    }
                    // Bakım
                    Row(modifier = Modifier.weight(weights[7]), verticalAlignment = Alignment.CenterVertically) {
                        val isWarn = isDateExpiringSoon(v.nextMaintenanceDate)
                        Text(
                            v.nextMaintenanceDate ?: "-",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Warning)
                        )
                        if (isWarn) {
                            Spacer(Modifier.width(2.dp))
                            Text("⚠️", style = TourOSTypography.Caption)
                        }
                    }
                    // Durum
                    Box(modifier = Modifier.weight(weights[8])) {
                        TourOSStatusBadge(
                            text = if (v.isActive) "Aktif" else "Pasif",
                            backgroundColor = if (v.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                            textColor = if (v.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                        )
                    }
                    // İşlem
                    Row(modifier = Modifier.weight(weights[9]), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TourOSButton("Düzenle", { onEditClick(v) }, variant = TourOSButtonVariant.TERTIARY)
                        TourOSButton("Sil", { onDeleteClick(v) }, variant = TourOSButtonVariant.DESTRUCTIVE)
                    }
                }
                if (idx < vehicles.size - 1) HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
            }
        }
    }
}

// ─── Compact: Kart ───────────────────────────────────────────────────────────

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val typeLabel = vehicleTypeLabel(vehicle.vehicleType)
    val hasExpiringDate = isDateExpiringSoon(vehicle.insuranceExpiry) ||
            isDateExpiringSoon(vehicle.inspectionExpiry) ||
            isDateExpiringSoon(vehicle.nextMaintenanceDate)

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    // Plaka rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.TextPrimary)
                            .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                    ) {
                        Text(vehicle.plateNumber, style = TourOSTypography.Label.copy(color = Color.White))
                    }
                    // Tip rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                    ) {
                        Text(typeLabel, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasExpiringDate) {
                        WarningBadge("Yaklaşıyor")
                    }
                    TourOSStatusBadge(
                        text = if (vehicle.isActive) "Aktif" else "Pasif",
                        backgroundColor = if (vehicle.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                        textColor = if (vehicle.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                    )
                }
            }

            // Araç bilgisi
            Text(
                "${vehicle.brand ?: ""} ${vehicle.model ?: "Belirtilmedi"} (${vehicle.year ?: "-"})",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
            Text(
                "💺 ${vehicle.capacity} Pax  ·  🎨 ${vehicle.color ?: "—"}  ·  ${if (vehicle.isOwned) "🏢 Öz Mal" else "🤝 Kiralık"}",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            // Sigorta / Muayene / Bakım çizelgesi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.4f))
                    .padding(TourOSSpacing.small),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                VehicleDateCell("🛡️ Sigorta", vehicle.insuranceExpiry, TourOSColors.Info)
                VehicleDateCell("🔍 Muayene", vehicle.inspectionExpiry, TourOSColors.Info)
                VehicleDateCell("🛠️ Bakım", vehicle.nextMaintenanceDate, TourOSColors.Warning)
            }

            if (!vehicle.maintenanceNotes.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.WarningContainer)
                        .padding(TourOSSpacing.small)
                ) {
                    Text(
                        "🔧 ${vehicle.maintenanceNotes}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Warning)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TourOSButton("Sil", onDeleteClick, variant = TourOSButtonVariant.DESTRUCTIVE)
                Spacer(Modifier.width(TourOSSpacing.small))
                TourOSButton("Düzenle", onEditClick, variant = TourOSButtonVariant.TERTIARY)
            }
        }
    }
}

@Composable
private fun VehicleDateCell(label: String, date: String?, defaultColor: Color) {
    val isWarn = isDateExpiringSoon(date)
    val color = if (isWarn) TourOSColors.Warning else defaultColor
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            if (isWarn) {
                Spacer(Modifier.width(2.dp))
                Text("⚠️", style = TourOSTypography.Caption)
            }
        }
        Text(date ?: "—", style = TourOSTypography.Label.copy(color = color))
    }
}

private fun vehicleTypeLabel(type: String?) = when (type) {
    "bus"     -> "🚌 Otobüs"
    "minibus" -> "🚐 Minibüs"
    "vip"     -> "🚘 VIP Araç"
    else      -> "🚗 Diğer"
}

// ─── Form ─────────────────────────────────────────────────────────────────────

@Composable
private fun VehicleForm(
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
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (formState.isEditing) "✏️ Araç Düzenle" else "➕ Yeni Araç Ekle",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
                IconButton(onClick = onCancel) {
                    Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Araç Tipi
            Text("Araç Tipi:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                listOf("bus" to "🚌 Otobüs", "minibus" to "🚐 Minibüs", "vip" to "🚘 VIP Araç", "other" to "🚗 Diğer").forEach { (code, label) ->
                    FilterChip(
                        selected = formState.vehicleType == code,
                        onClick = { onVehicleTypeChange(code) },
                        label = { Text(label, style = TourOSTypography.Caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TourOSColors.PrimaryContainer,
                            selectedLabelColor = TourOSColors.Primary
                        )
                    )
                }
            }

            // Plaka + Kapasite
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                TourOSTextField(value = formState.plateNumber, onValueChange = onPlateNumberChange, label = "Plaka", modifier = Modifier.weight(1f), placeholder = "34 TOUR 01")
                TourOSTextField(value = formState.capacity, onValueChange = onCapacityChange, label = "Koltuk Sayısı", modifier = Modifier.weight(1f), placeholder = "50")
            }

            // Marka + Model
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                TourOSTextField(value = formState.brand, onValueChange = onBrandChange, label = "Marka", modifier = Modifier.weight(1f), placeholder = "Mercedes")
                TourOSTextField(value = formState.model, onValueChange = onModelChange, label = "Model", modifier = Modifier.weight(1f), placeholder = "Travego")
            }

            // Yıl + Renk
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                TourOSTextField(value = formState.year, onValueChange = onYearChange, label = "Yıl", modifier = Modifier.weight(1f), placeholder = "2022")
                TourOSTextField(value = formState.color, onValueChange = onColorChange, label = "Renk", modifier = Modifier.weight(1f), placeholder = "Beyaz")
            }

            // Sahiplik
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = formState.isOwned,
                    onCheckedChange = onIsOwnedChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = TourOSColors.Primary, checkedTrackColor = TourOSColors.PrimaryContainer)
                )
                Spacer(Modifier.width(TourOSSpacing.small))
                Text(if (formState.isOwned) "Firma Öz Mal Aracı" else "Kiralık Araç", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
            }

            if (!formState.isOwned) {
                TourOSTextField(value = formState.ownerInfo, onValueChange = onOwnerInfoChange, label = "Kiralık Firma / Tedarikçi", modifier = Modifier.fillMaxWidth())

            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Sigorta / Muayene / Bakım
            Text("🛡️ Sigorta, Muayene & Bakım:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                TourOSTextField(value = formState.insuranceExpiry, onValueChange = onInsuranceExpiryChange, label = "Sigorta Bitiş", modifier = Modifier.weight(1f), placeholder = "YYYY-AA-GG")
                TourOSTextField(value = formState.inspectionExpiry, onValueChange = onInspectionExpiryChange, label = "Muayene Bitiş", modifier = Modifier.weight(1f), placeholder = "YYYY-AA-GG")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                TourOSTextField(value = formState.lastMaintenanceDate, onValueChange = onLastMaintenanceChange, label = "Son Bakım", modifier = Modifier.weight(1f), placeholder = "YYYY-AA-GG")
                TourOSTextField(value = formState.nextMaintenanceDate, onValueChange = onNextMaintenanceChange, label = "Gelecek Bakım", modifier = Modifier.weight(1f), placeholder = "YYYY-AA-GG")
            }
            TourOSTextField(value = formState.maintenanceNotes, onValueChange = onMaintenanceNotesChange, label = "Bakım Notları", modifier = Modifier.fillMaxWidth())

            // Aktif switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = formState.isActive,
                    onCheckedChange = onIsActiveChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = TourOSColors.Primary, checkedTrackColor = TourOSColors.PrimaryContainer)
                )
                Spacer(Modifier.width(TourOSSpacing.small))
                Text("Aracı Aktif Filoda Göster", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                TourOSButton("İptal", onCancel, variant = TourOSButtonVariant.SECONDARY, modifier = Modifier.weight(1f))
                TourOSButton(
                    "💾 Aracı Kaydet",
                    onSave,
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    enabled = formState.plateNumber.isNotBlank() && formState.capacity.isNotBlank()
                )
            }
        }
    }
}

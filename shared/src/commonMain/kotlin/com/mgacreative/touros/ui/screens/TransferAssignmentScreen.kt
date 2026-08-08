package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.mgacreative.touros.domain.model.Driver
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.TransferTask
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AssignmentDialogState
import com.mgacreative.touros.ui.viewmodel.TransferAssignmentUiState
import com.mgacreative.touros.ui.viewmodel.TransferAssignmentViewModel

private data class StatusFilterItem(val key: String?, val label: String, val icon: String)

private val statusFilters = listOf(
    StatusFilterItem(null, "Tüm Görevler", "📋"),
    StatusFilterItem("planned", "Planlanan", "⏱️"),
    StatusFilterItem("assigned", "Atanan", "👤"),
    StatusFilterItem("completed", "Tamamlanan", "✅")
)

/**
 * Transfer Görev Atama Ekranı — TourOS 0.3
 *
 * İki Panelli Düzen (Expanded: Sol Transfer Detayı, Sağ Müsait Personel/Araç Kartları)
 * Compact: Üstte Transfer Listesi/Detayı, Altta Müsait Personel Seçimi.
 */
@Composable
fun TransferAssignmentScreen(
    viewModel: TransferAssignmentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Transfer Görev Atama",
                subtitle = "Şoför, rehber ve araç görevlendirme yönetimi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is TransferAssignmentUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is TransferAssignmentUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is TransferAssignmentUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 840.dp
                    var selectedTransferId by remember { mutableStateOf<String?>(state.transfers.firstOrNull()?.id) }

                    // Aktif seçili transfer görevi
                    val activeTransfer = state.transfers.find { it.id == selectedTransferId } ?: state.transfers.firstOrNull()

                    Column(
                        modifier = Modifier.fillMaxSize().padding(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── Filtre Çipleri ────────────────────────────────────
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            items(statusFilters) { filter ->
                                FilterChip(
                                    selected = state.selectedStatusFilter == filter.key,
                                    onClick = { viewModel.setStatusFilter(filter.key) },
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

                        if (state.transfers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Filtreye uygun transfer görevi bulunamadı.",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (isExpanded) {
                            // ── Expanded: İKİ PANELLİ DÜZEN ─────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                // SOL PANEL: Transfer Liste & Detayı (Kapsamlı)
                                Column(
                                    modifier = Modifier.weight(1.1f).fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                ) {
                                    Text(
                                        "📌 Transfer Görevleri (${state.transfers.size})",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                    )

                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(state.transfers) { transfer ->
                                            val isSelected = transfer.id == activeTransfer?.id
                                            val driver = state.drivers.find { it.id == transfer.driverId }
                                            val guide = state.guides.find { it.id == transfer.guideId }
                                            val vehicle = state.vehicles.find { it.id == transfer.vehicleId }

                                            TransferSelectableCard(
                                                transfer = transfer,
                                                driver = driver,
                                                guide = guide,
                                                vehicle = vehicle,
                                                isSelected = isSelected,
                                                onClick = {
                                                    selectedTransferId = transfer.id
                                                    viewModel.openAssignmentDialog(transfer)
                                                }
                                            )
                                        }
                                    }
                                }

                                VerticalDivider(color = TourOSColors.Divider, thickness = 1.dp)

                                // SAĞ PANEL: Müsait Şoför, Rehber & Araç Seçim Paneli
                                Column(
                                    modifier = Modifier.weight(1.3f).fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                ) {
                                    if (activeTransfer != null) {
                                        RightAssignmentPanel(
                                            activeTransfer = activeTransfer,
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
                                }
                            }
                        } else {
                            // ── Compact: TEK SÜTUNLU AKIŞ ──────────────────────
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                items(state.transfers) { transfer ->
                                    val driver = state.drivers.find { it.id == transfer.driverId }
                                    val guide = state.guides.find { it.id == transfer.guideId }
                                    val vehicle = state.vehicles.find { it.id == transfer.vehicleId }

                                    TransferCompactCard(
                                        transfer = transfer,
                                        driver = driver,
                                        guide = guide,
                                        vehicle = vehicle,
                                        onAssignClick = {
                                            selectedTransferId = transfer.id
                                            viewModel.openAssignmentDialog(transfer)
                                        }
                                    )
                                }

                                item {
                                    AnimatedVisibility(
                                        visible = dialogState.isOpen && dialogState.transfer != null,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        dialogState.transfer?.let { transfer ->
                                            RightAssignmentPanel(
                                                activeTransfer = transfer,
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

// ─── Sol Panel: Seçilebilir Transfer Kartı (Expanded) ─────────────────────────

@Composable
private fun TransferSelectableCard(
    transfer: TransferTask,
    driver: Driver?,
    guide: Guide?,
    vehicle: Vehicle?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isFullyAssigned = driver != null && guide != null && vehicle != null
    val borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border
    val bgColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.3f) else TourOSColors.Background

    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            ),
        backgroundColor = bgColor,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transferTypeLabel(transfer.transferType),
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                )
                TourOSStatusBadge(
                    text = if (isFullyAssigned) "✅ Tamam" else "⚠️ Atama Bekliyor",
                    backgroundColor = if (isFullyAssigned) TourOSColors.SuccessContainer else TourOSColors.WarningContainer,
                    textColor = if (isFullyAssigned) TourOSColors.Success else TourOSColors.Warning
                )
            }

            Text(
                text = "📍 ${transfer.origin} → ${transfer.destination}",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            Text(
                text = "📅 ${transfer.pickupTime ?: "—"}  ·  👥 ${transfer.paxCount} Pax",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )
        }
    }
}

// ─── Sağ Panel: Şoför, Rehber & Araç Seçim Ekranı ────────────────────────────

@Composable
private fun RightAssignmentPanel(
    activeTransfer: TransferTask,
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
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.4f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            // Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "👤 Görev Atama Paneli",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                    Text(
                        "📍 ${activeTransfer.origin} → ${activeTransfer.destination}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                }
                IconButton(onClick = onCancel) {
                    Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // 1. MÜSAİT ŞOFÖR SEÇİM KARTLARI
            Text(
                "👨‍✈️ Müsait Şoför Listesi",
                style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                item {
                    SelectableChipCard(
                        title = "Atama Yapma",
                        subtitle = "Şoförsüz",
                        isSelected = dialogState.selectedDriverId == null,
                        onClick = { onSelectDriver(null) }
                    )
                }
                items(drivers) { d ->
                    SelectableChipCard(
                        title = d.fullName,
                        subtitle = "Ehliyet: ${d.licenseClass ?: "D1"} · 📞 ${d.phone ?: "—"}",
                        isSelected = dialogState.selectedDriverId == d.id,
                        onClick = { onSelectDriver(d.id) }
                    )
                }
            }

            // 2. MÜSAİT REHBER SEÇİM KARTLARI
            Text(
                "🚩 Kokartlı Rehber Listesi",
                style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                item {
                    SelectableChipCard(
                        title = "Atama Yapma",
                        subtitle = "Rehbersiz",
                        isSelected = dialogState.selectedGuideId == null,
                        onClick = { onSelectGuide(null) }
                    )
                }
                items(guides) { g ->
                    SelectableChipCard(
                        title = g.fullName,
                        subtitle = "Diller: ${g.languages?.joinToString(", ") ?: "—"}",

                        isSelected = dialogState.selectedGuideId == g.id,
                        onClick = { onSelectGuide(g.id) }
                    )
                }
            }

            // 3. MÜSAİT ARAÇ SEÇİM KARTLARI
            Text(
                "🚌 Müsait Filo Araçları",
                style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                item {
                    SelectableChipCard(
                        title = "Atama Yapma",
                        subtitle = "Araçsız",
                        isSelected = dialogState.selectedVehicleId == null,
                        onClick = { onSelectVehicle(null) }
                    )
                }
                items(vehicles) { v ->
                    SelectableChipCard(
                        title = v.plateNumber,
                        subtitle = "${v.brand ?: ""} ${v.model ?: ""} (${v.capacity} Pax)",
                        isSelected = dialogState.selectedVehicleId == v.id,
                        onClick = { onSelectVehicle(v.id) }
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Kaydet / İptal Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSButton(
                    text = "İptal",
                    onClick = onCancel,
                    variant = TourOSButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                TourOSButton(
                    text = "💾 Atamayı Kaydet",
                    onClick = onSave,
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── Seçilebilir Personel / Araç Chip Kartı ─────────────────────────────────

@Composable
private fun SelectableChipCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border
    val bgColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface

    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            )
            .clickable { onClick() }
            .padding(TourOSSpacing.small)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = TourOSTypography.Label.copy(
                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary
                )
            )
            Text(
                subtitle,
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                maxLines = 1
            )
        }
    }
}

// ─── Compact Kart Görünümü ───────────────────────────────────────────────────

@Composable
private fun TransferCompactCard(
    transfer: TransferTask,
    driver: Driver?,
    guide: Guide?,
    vehicle: Vehicle?,
    onAssignClick: () -> Unit
) {
    val isFullyAssigned = driver != null && guide != null && vehicle != null

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    transferTypeLabel(transfer.transferType),
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                )
                TourOSStatusBadge(
                    text = if (isFullyAssigned) "✅ Atama Tamam" else "⚠️ Atama Bekliyor",
                    backgroundColor = if (isFullyAssigned) TourOSColors.SuccessContainer else TourOSColors.WarningContainer,
                    textColor = if (isFullyAssigned) TourOSColors.Success else TourOSColors.Warning
                )
            }

            Text(
                "📍 ${transfer.origin} → ${transfer.destination}",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )
            Text(
                "📅 Tarih: ${transfer.pickupTime ?: "—"}  ·  👥 Yolcu: ${transfer.paxCount} Pax",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            // Mevcut Atamalar (3 Kolonlu Özet)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                AssignmentMiniBox("👨‍✈️ Şoför", driver?.fullName ?: "Atanmadı", driver != null, Modifier.weight(1f))
                AssignmentMiniBox("🚩 Rehber", guide?.fullName ?: "Atanmadı", guide != null, Modifier.weight(1f))
                AssignmentMiniBox("🚌 Araç", vehicle?.plateNumber ?: "Atanmadı", vehicle != null, Modifier.weight(1f))
            }

            TourOSButton(
                text = if (isFullyAssigned) "✏️ Atamayı Güncelle" else "👤 Atama Yap",
                onClick = onAssignClick,
                variant = if (isFullyAssigned) TourOSButtonVariant.TERTIARY else TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AssignmentMiniBox(label: String, value: String, isAssigned: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(if (isAssigned) TourOSColors.PrimaryContainer.copy(alpha = 0.4f) else TourOSColors.ErrorContainer.copy(alpha = 0.3f))
            .padding(TourOSSpacing.small)
    ) {
        Column {
            Text(label, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            Text(
                value,
                style = TourOSTypography.Label.copy(
                    color = if (isAssigned) TourOSColors.TextPrimary else TourOSColors.Error
                ),
                maxLines = 1
            )
        }
    }
}

private fun transferTypeLabel(type: String?) = when (type) {
    "airport"   -> "✈️ Havalimanı Transferi"
    "tour"      -> "🏞️ Tur Transferi"
    "intercity" -> "🛣️ Şehirler Arası Transfer"
    else        -> "🚐 Özel Transfer"
}

package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.StopSaleFormState
import com.mgacreative.touros.ui.viewmodel.StopSaleReleaseUiState
import com.mgacreative.touros.ui.viewmodel.StopSaleReleaseViewModel

/**
 * Stop Sale & Release Yönetim Ekranı — TourOS 0.3
 *
 * Üstte: Aktif durdurma listesi (ErrorContainer satırlar)
 * Orta:  Tarih aralığı seçici form (AnimatedVisibility)
 * Alt:   Stop Sale (Error) + Release (Success) aksiyon butonları
 */
@Composable
fun StopSaleReleaseScreen(
    viewModel: StopSaleReleaseViewModel,
    hotelId: String = "1",
    hotelName: String = "Grand Cave Suites",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(hotelId) { viewModel.initForHotel(hotelId) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Stop Sale & Release",
                subtitle = hotelName,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is StopSaleReleaseUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }

            is StopSaleReleaseUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }

            is StopSaleReleaseUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // ── KPI Kartları ───────────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            StopSaleKpiCard(
                                label = "Aktif Stop Sale",
                                value = state.activeStopSaleCount.toString(),
                                bgColor = TourOSColors.ErrorContainer,
                                textColor = TourOSColors.Error,
                                icon = "⛔",
                                modifier = Modifier.weight(1f)
                            )
                            StopSaleKpiCard(
                                label = "Aktif Release",
                                value = state.activeReleaseCount.toString(),
                                bgColor = TourOSColors.SuccessContainer,
                                textColor = TourOSColors.Success,
                                icon = "🔓",
                                modifier = Modifier.weight(1f)
                            )
                            StopSaleKpiCard(
                                label = "Toplam Kayıt",
                                value = state.stopSales.size.toString(),
                                bgColor = TourOSColors.PrimaryContainer,
                                textColor = TourOSColors.Primary,
                                icon = "📋",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ── Aktif Stop Sale'ler (üstte) ───────────────────────────
                    val activeStopSales = state.stopSales.filter { it.isActive && it.actionType == "STOP_SALE" }
                    if (activeStopSales.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(TourOSColors.ErrorContainer)
                                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⛔", style = TourOSTypography.TitleMedium)
                                Spacer(Modifier.width(TourOSSpacing.small))
                                Text(
                                    "Aktif Satış Durdurmaları (${activeStopSales.size})",
                                    style = TourOSTypography.Label.copy(color = TourOSColors.Error)
                                )
                            }
                        }

                        items(activeStopSales) { item ->
                            val roomName = state.roomTypes.find { it.id == item.roomTypeId }?.name ?: "Tüm Odalar"
                            StopSaleCard(
                                item = item,
                                roomTypeName = roomName,
                                onToggleStatus = { viewModel.toggleStatus(item.id, item.isActive) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                        }

                        item { HorizontalDivider(color = TourOSColors.Divider) }
                    }

                    // ── Aksiyon Butonları ──────────────────────────────────────
                    item {
                        AnimatedVisibility(
                            visible = !formState.isFormOpen,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                // Stop Sale — Error tonlu
                                TourOSButton(
                                    text = "⛔ Stop Sale",
                                    onClick = { viewModel.openNewForm("STOP_SALE") },
                                    variant = TourOSButtonVariant.DESTRUCTIVE,
                                    modifier = Modifier.weight(1f)
                                )
                                // Release — Success tonlu
                                TourOSButton(
                                    text = "🔓 Release",
                                    onClick = { viewModel.openNewForm("RELEASE") },
                                    variant = TourOSButtonVariant.SUCCESS,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // ── Tarih Seçici Form ──────────────────────────────────────
                    item {
                        AnimatedVisibility(
                            visible = formState.isFormOpen,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            StopSaleForm(
                                formState = formState,
                                roomTypes = state.roomTypes,
                                onRoomTypeChange = { viewModel.updateRoomTypeId(it) },
                                onActionTypeChange = { viewModel.updateActionType(it) },
                                onStartDateChange = { viewModel.updateStartDate(it) },
                                onEndDateChange = { viewModel.updateEndDate(it) },
                                onReasonChange = { viewModel.updateReason(it) },
                                onIsActiveChange = { viewModel.updateIsActive(it) },
                                onSave = { viewModel.applyStopSaleOrRelease() },
                                onCancel = { viewModel.closeForm() }
                            )
                        }
                    }

                    // ── Tüm Kayıtlar listesi (aktif olmayanlar dahil) ──────────
                    val otherItems = state.stopSales.filter { !(it.isActive && it.actionType == "STOP_SALE") }
                    if (otherItems.isNotEmpty()) {
                        item {
                            Text(
                                "📋 Tüm Kayıtlar",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )
                        }
                        items(otherItems) { item ->
                            val roomName = state.roomTypes.find { it.id == item.roomTypeId }?.name ?: "Tüm Odalar"
                            StopSaleCard(
                                item = item,
                                roomTypeName = roomName,
                                onToggleStatus = { viewModel.toggleStatus(item.id, item.isActive) },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                        }
                    }

                    if (state.stopSales.isEmpty() && !formState.isFormOpen) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Kayıtlı işlem bulunamadı.\nSatış durdurmak için ⛔ Stop Sale butonuna basın.",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── KPI Kart ────────────────────────────────────────────────────────────────

@Composable
private fun StopSaleKpiCard(
    label: String,
    value: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    TourOSCard(modifier = modifier, backgroundColor = bgColor, contentPadding = TourOSSpacing.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(icon, style = TourOSTypography.TitleLarge)
            Text(value, style = TourOSTypography.TitleLarge.copy(color = textColor))
            Text(label, style = TourOSTypography.Caption.copy(color = textColor.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
        }
    }
}

// ─── Stop Sale / Release Kart ─────────────────────────────────────────────────

@Composable
private fun StopSaleCard(
    item: HotelStopSale,
    roomTypeName: String,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isStopSale = item.actionType == "STOP_SALE"
    val accentBg = if (isStopSale) TourOSColors.ErrorContainer else TourOSColors.SuccessContainer
    val accentText = if (isStopSale) TourOSColors.Error else TourOSColors.Success

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                ) {
                    // Tip rozeti
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(accentBg)
                            .padding(horizontal = TourOSSpacing.small, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isStopSale) "⛔ STOP SALE" else "🔓 RELEASE",
                            style = TourOSTypography.Caption.copy(color = accentText)
                        )
                    }
                    Text(roomTypeName, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
                }
                Switch(
                    checked = item.isActive,
                    onCheckedChange = { onToggleStatus() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentText,
                        checkedTrackColor = accentBg
                    )
                )
            }

            // Tarih aralığı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.4f))
                    .padding(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                Text("📅", style = TourOSTypography.BodyMedium)
                Text(
                    "${item.startDate}  →  ${item.endDate}",
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )
            }

            // Gerekçe
            if (!item.reason.isNullOrBlank()) {
                Text(
                    "📝 ${item.reason}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            // Etki açıklaması
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(accentBg.copy(alpha = 0.4f))
                    .border(0.5.dp, accentText.copy(alpha = 0.3f), RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .padding(TourOSSpacing.small)
            ) {
                Text(
                    text = if (isStopSale)
                        "⚠️ Seçilen tarihlerde bu otelin satışı durduruldu. Tur kalkışları ve rezervasyon sihirbazından kaldırıldı."
                    else
                        "ℹ️ Serbest bırakılan kontenjan genel stoka devredildi.",
                    style = TourOSTypography.Caption.copy(color = accentText.copy(alpha = 0.9f))
                )
            }

            // Sil butonu
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TourOSButton(
                    text = "Sil",
                    onClick = onDelete,
                    variant = TourOSButtonVariant.DESTRUCTIVE
                )
            }
        }
    }
}

// ─── Form ─────────────────────────────────────────────────────────────────────

@Composable
private fun StopSaleForm(
    formState: StopSaleFormState,
    roomTypes: List<RoomType>,
    onRoomTypeChange: (String?) -> Unit,
    onActionTypeChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val isStopSale = formState.actionType == "STOP_SALE"
    val accentBg = if (isStopSale) TourOSColors.ErrorContainer else TourOSColors.SuccessContainer
    val accentText = if (isStopSale) TourOSColors.Error else TourOSColors.Success

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = accentBg.copy(alpha = 0.3f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            // Form başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isStopSale) "⛔ Satış Durdurma Tanımla" else "🔓 Kontenjan Serbest Bırak",
                    style = TourOSTypography.TitleMedium.copy(color = accentText)
                )
                IconButton(onClick = onCancel) {
                    Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                }
            }

            HorizontalDivider(color = accentText.copy(alpha = 0.2f))

            // İşlem tipi toggle
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                FilterChip(
                    selected = isStopSale,
                    onClick = { onActionTypeChange("STOP_SALE") },
                    label = { Text("⛔ Stop Sale", style = TourOSTypography.Caption) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TourOSColors.ErrorContainer,
                        selectedLabelColor = TourOSColors.Error
                    )
                )
                FilterChip(
                    selected = !isStopSale,
                    onClick = { onActionTypeChange("RELEASE") },
                    label = { Text("🔓 Release", style = TourOSTypography.Caption) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TourOSColors.SuccessContainer,
                        selectedLabelColor = TourOSColors.Success
                    )
                )
            }

            // Oda tipi
            Text("Kapsam:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                FilterChip(
                    selected = formState.roomTypeId == null,
                    onClick = { onRoomTypeChange(null) },
                    label = { Text("Tüm Odalar", style = TourOSTypography.Caption) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TourOSColors.PrimaryContainer,
                        selectedLabelColor = TourOSColors.Primary
                    )
                )
                roomTypes.forEach { room ->
                    FilterChip(
                        selected = formState.roomTypeId == room.id,
                        onClick = { onRoomTypeChange(room.id) },
                        label = { Text(room.name, style = TourOSTypography.Caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TourOSColors.PrimaryContainer,
                            selectedLabelColor = TourOSColors.Primary
                        )
                    )
                }
            }

            // Tarih aralığı seçici
            Text("📅 Tarih Aralığı:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = formState.startDate,
                    onValueChange = onStartDateChange,
                    label = "Başlangıç",
                    placeholder = "YYYY-AA-GG",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.endDate,
                    onValueChange = onEndDateChange,
                    label = "Bitiş",
                    placeholder = "YYYY-AA-GG",
                    modifier = Modifier.weight(1f)
                )
            }

            // Gerekçe
            TourOSTextField(
                value = formState.reason,
                onValueChange = onReasonChange,
                label = "Gerekçe / Not",
                placeholder = "Örn: Otel Bakımda, Kapasita Dolu",
                modifier = Modifier.fillMaxWidth()
            )

            // Anında aktif switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = formState.isActive,
                    onCheckedChange = onIsActiveChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = accentText, checkedTrackColor = accentBg)
                )
                Spacer(Modifier.width(TourOSSpacing.small))
                Text("Anında Aktifleştir", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
            }

            HorizontalDivider(color = accentText.copy(alpha = 0.2f))

            // Kaydet / İptal
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
                if (isStopSale) {
                    TourOSButton(
                        text = "⛔ Satışı Durdur",
                        onClick = onSave,
                        variant = TourOSButtonVariant.DESTRUCTIVE,
                        enabled = formState.startDate.isNotBlank() && formState.endDate.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    TourOSButton(
                        text = "🔓 Release Et",
                        onClick = onSave,
                        variant = TourOSButtonVariant.SUCCESS,
                        enabled = formState.startDate.isNotBlank() && formState.endDate.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

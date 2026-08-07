package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.HotelContract
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSColumn
import com.mgacreative.touros.ui.components.TourOSDataTable
import com.mgacreative.touros.ui.components.TourOSEmptyState
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTabs
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.ContractFilterTab
import com.mgacreative.touros.ui.viewmodel.HotelContractFormState
import com.mgacreative.touros.ui.viewmodel.HotelContractUiState
import com.mgacreative.touros.ui.viewmodel.HotelContractViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Otel Kontrat Yönetim Ekranı.
 * - Otel detayında 'Kontratlar' alt sekmesi olarak kullanılır.
 * - Kontrat listesini tarih aralığı ve durum bilgisiyle satır satır gösterir.
 * - Expanded: TourOSDataTable, Compact: TourOSCard kart listesi.
 */
@Composable
fun HotelContractScreen(
    viewModel: HotelContractViewModel,
    hotelId: String = "1",
    hotelName: String = "Grand Cave Suites",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(hotelId) {
        viewModel.initForHotel(hotelId)
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Kontrat Yönetimi",
                subtitle = "$hotelName • Sezon kontratları ve fiyat anlaşmaları",
                actions = {
                    if ((uiState as? HotelContractUiState.Success)?.let { !formState.isFormOpen } == true) {
                        TourOSButton(
                            text = "+ Yeni Kontrat",
                            onClick = { viewModel.openNewContractForm() },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            when (val state = uiState) {
                is HotelContractUiState.Loading -> {
                    TourOSLoadingIndicator(message = "Kontratlar yükleniyor...")
                }

                is HotelContractUiState.Error -> {
                    TourOSEmptyState(
                        title = "Hata Oluştu",
                        description = state.message,
                        actionButtonText = "Yeniden Dene",
                        onActionClick = { viewModel.initForHotel(hotelId) }
                    )
                }

                is HotelContractUiState.Success -> {
                    // Yeni/Düzenle Kontrat Formu — Accordion
                    AnimatedVisibility(
                        visible = formState.isFormOpen,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ContractFormCard(
                            formState = formState,
                            roomTypes = state.roomTypes,
                            onSeasonNameChange = { viewModel.updateFormSeasonName(it) },
                            onRoomTypeChange = { viewModel.updateFormRoomTypeId(it) },
                            onStartDateChange = { viewModel.updateFormStartDate(it) },
                            onEndDateChange = { viewModel.updateFormEndDate(it) },
                            onPriceChange = { viewModel.updateFormPrice(it) },
                            onCurrencyChange = { viewModel.updateFormCurrency(it) },
                            onAllotmentChange = { viewModel.updateFormAllotment(it) },
                            onReleaseDaysChange = { viewModel.updateFormReleaseDays(it) },
                            onMealPlanChange = { viewModel.updateFormMealPlan(it) },
                            onNotesChange = { viewModel.updateFormNotes(it) },
                            onIsActiveChange = { viewModel.updateFormIsActive(it) },
                            onSave = { viewModel.saveContract() },
                            onCancel = { viewModel.closeForm() }
                        )
                    }

                    // Sekme: Kontrat Geçmişi / Analiz
                    TourOSTabs(
                        tabs = listOf("📋 Kontrat Geçmişi", "📊 Kontrat Analizi"),
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    if (selectedTab == 0) {
                        // Durum Filtre Chip'leri — TourOSStatusBadge renkleriyle eşleşen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            ContractFilterTab.entries.forEach { filterTab ->
                                val isSelected = state.activeFilter == filterTab
                                val (bgColor, textColor) = when (filterTab) {
                                    ContractFilterTab.ALL -> Pair(TourOSColors.PrimaryContainer, TourOSColors.Primary)
                                    ContractFilterTab.ACTIVE -> Pair(TourOSColors.SuccessContainer, TourOSColors.Success)
                                    ContractFilterTab.PAST -> Pair(TourOSColors.ErrorContainer, TourOSColors.Error)
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setFilterTab(filterTab) },
                                    label = {
                                        Text(filterTab.title, style = TourOSTypography.Caption.copy(
                                            color = if (isSelected) textColor else TourOSColors.TextSecondary
                                        ))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = bgColor,
                                        selectedLabelColor = textColor
                                    )
                                )
                            }
                        }

                        val filteredContracts = when (state.activeFilter) {
                            ContractFilterTab.ALL -> state.contracts
                            ContractFilterTab.ACTIVE -> state.contracts.filter { it.isActive }
                            ContractFilterTab.PAST -> state.contracts.filter { !it.isActive }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (filteredContracts.isEmpty()) {
                                TourOSEmptyState(
                                    title = "Kontrat Bulunamadı",
                                    description = "Bu filtreye ait kontrat kaydı bulunamadı.",
                                    actionButtonText = "+ Yeni Kontrat Ekle",
                                    onActionClick = { viewModel.openNewContractForm() }
                                )
                            } else {
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val isCompact = maxWidth < 768.dp

                                    val columns = listOf(
                                        TourOSColumn<HotelContract>(title = "SEZON / ODA TİPİ", weight = 2f) { c ->
                                            val roomName = state.roomTypes.find { it.id == c.roomTypeId }?.name ?: "Tüm Oda Tipleri"
                                            Column {
                                                Text(c.seasonName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                                                Text("🛏️ $roomName", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                            }
                                        },
                                        TourOSColumn<HotelContract>(title = "TARİH ARALIĞI", weight = 1.8f) { c ->
                                            Column {
                                                Text(c.startDate, style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary))
                                                Text("→ ${c.endDate}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                            }
                                        },
                                        TourOSColumn<HotelContract>(title = "DURUM", weight = 1f) { c ->
                                            TourOSStatusBadge(
                                                text = if (c.isActive) "● Aktif" else "○ Pasif",
                                                backgroundColor = if (c.isActive) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer,
                                                textColor = if (c.isActive) TourOSColors.Success else TourOSColors.Error
                                            )
                                        },
                                        TourOSColumn<HotelContract>(title = "GECELİK FİYAT", weight = 1.2f) { c ->
                                            Text(
                                                text = "${c.pricePerNight.toInt()} ${c.currency}",
                                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                            )
                                        },
                                        TourOSColumn<HotelContract>(title = "PANSİYON / KONTENJAN", weight = 1.5f) { c ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TourOSStatusBadge(
                                                    text = c.mealPlan,
                                                    backgroundColor = TourOSColors.PrimaryContainer,
                                                    textColor = TourOSColors.Primary
                                                )
                                                TourOSStatusBadge(
                                                    text = "${c.allotment} Oda",
                                                    backgroundColor = TourOSColors.Secondary.copy(alpha = 0.15f),
                                                    textColor = TourOSColors.Secondary
                                                )
                                            }
                                        }
                                    )

                                    TourOSDataTable(
                                        items = filteredContracts,
                                        columns = columns,
                                        isCompact = isCompact,
                                        modifier = Modifier.fillMaxSize(),
                                        onItemClick = { viewModel.openEditContractForm(it) },
                                        compactCardContent = { contract ->
                                            val roomName = state.roomTypes.find { it.id == contract.roomTypeId }?.name ?: "Tüm Oda Tipleri"
                                            ContractCompactCard(
                                                contract = contract,
                                                roomTypeName = roomName,
                                                onEditClick = { viewModel.openEditContractForm(contract) },
                                                onDeleteClick = { viewModel.deleteContract(contract.id) }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Analiz Sekmesi
                        ContractAnalyticsSection(contracts = state.contracts)
                    }
                }
            }
        }
    }
}

// ─── Compact Kart Bileşeni ───────────────────────────────────────────────────

@Composable
private fun ContractCompactCard(
    contract: HotelContract,
    roomTypeName: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        // Başlık + Durum Rozeti
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(contract.seasonName, style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                Text("🛏️ $roomTypeName", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
            }
            TourOSStatusBadge(
                text = if (contract.isActive) "● Aktif" else "○ Pasif",
                backgroundColor = if (contract.isActive) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer,
                textColor = if (contract.isActive) TourOSColors.Success else TourOSColors.Error
            )
        }

        HorizontalDivider(color = TourOSColors.Divider)

        // Tarih Aralığı + Fiyat Satırı
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("📅 Kontrat Dönemi", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("${contract.startDate} → ${contract.endDate}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("💰 Gecelik", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text("${contract.pricePerNight.toInt()} ${contract.currency}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
            }
        }

        // Pansiyon + Kontenjan + Release Badge Satırı
        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
            TourOSStatusBadge(
                text = contract.mealPlan,
                backgroundColor = TourOSColors.PrimaryContainer,
                textColor = TourOSColors.Primary
            )
            TourOSStatusBadge(
                text = "${contract.allotment} Oda",
                backgroundColor = TourOSColors.Secondary.copy(alpha = 0.15f),
                textColor = TourOSColors.Secondary
            )
            TourOSStatusBadge(
                text = "${contract.releaseDays}G Release",
                backgroundColor = TourOSColors.WarningContainer,
                textColor = TourOSColors.Warning
            )
        }

        if (!contract.notes.isNullOrBlank()) {
            Text(
                text = "📝 ${contract.notes}",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )
        }

        // Aksiyon Butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TourOSButton(
                text = "Sil",
                onClick = onDeleteClick,
                variant = TourOSButtonVariant.DESTRUCTIVE
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.small))
            TourOSButton(
                text = "Düzenle",
                onClick = onEditClick,
                variant = TourOSButtonVariant.SECONDARY
            )
        }
    }
}

// ─── Kontrat Formu ────────────────────────────────────────────────────────────

@Composable
private fun ContractFormCard(
    formState: HotelContractFormState,
    roomTypes: List<RoomType>,
    onSeasonNameChange: (String) -> Unit,
    onRoomTypeChange: (String?) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onAllotmentChange: (String) -> Unit,
    onReleaseDaysChange: (String) -> Unit,
    onMealPlanChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.4f),
        borderColor = TourOSColors.Primary.copy(alpha = 0.2f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            // Form Başlığı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (formState.isEditing) "✏️ Kontrat Düzenle" else "➕ Yeni Otel Kontratı",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )
                TourOSButton(text = "✕ Kapat", onClick = onCancel, variant = TourOSButtonVariant.TERTIARY)
            }

            TourOSTextField(
                value = formState.seasonName,
                onValueChange = onSeasonNameChange,
                label = "Sezon Adı",
                placeholder = "Örn: Yaz 2026 Sezonu",
                modifier = Modifier.fillMaxWidth()
            )

            // Oda Tipi Seçimi
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                Text("Oda Tipi:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
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
            }

            // Tarih Aralığı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = formState.startDate,
                    onValueChange = onStartDateChange,
                    label = "Başlangıç (YYYY-AA-GG)",
                    placeholder = "2026-06-01",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.endDate,
                    onValueChange = onEndDateChange,
                    label = "Bitiş (YYYY-AA-GG)",
                    placeholder = "2026-09-30",
                    modifier = Modifier.weight(1f)
                )
            }

            // Fiyat / Kontenjan / Release
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = formState.pricePerNight,
                    onValueChange = onPriceChange,
                    label = "Gecelik Fiyat (₺)",
                    placeholder = "3500",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.allotment,
                    onValueChange = onAllotmentChange,
                    label = "Kontenjan (Allotment)",
                    placeholder = "20",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = formState.releaseDays,
                    onValueChange = onReleaseDaysChange,
                    label = "Release (Gün)",
                    placeholder = "7",
                    modifier = Modifier.weight(1f)
                )
            }

            // Pansiyon Tipi
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                Text("Pansiyon Tipi:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    listOf("BB" to "Oda Kahvaltı", "HB" to "Yarım Pansiyon", "FB" to "Tam Pansiyon", "AI" to "Her Şey Dahil", "RO" to "Sadece Oda").forEach { (code, label) ->
                        FilterChip(
                            selected = formState.mealPlan == code,
                            onClick = { onMealPlanChange(code) },
                            label = { Text("$code", style = TourOSTypography.Caption) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                selectedLabelColor = TourOSColors.Primary
                            )
                        )
                    }
                }
            }

            TourOSTextField(
                value = formState.notes,
                onValueChange = onNotesChange,
                label = "Notlar & Şartlar",
                placeholder = "Özel koşullar, genel şartlar...",
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = formState.isActive,
                    onCheckedChange = onIsActiveChange,
                    colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                )
                Text("Aktif Kontrat Olarak İşaretle", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
            }

            TourOSButton(
                text = "💾 Kontratı Kaydet",
                onClick = onSave,
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Analiz Sekmesi ───────────────────────────────────────────────────────────

@Composable
private fun ContractAnalyticsSection(contracts: List<HotelContract>) {
    val activeCount = contracts.count { it.isActive }
    val pastCount = contracts.count { !it.isActive }
    val avgPrice = if (contracts.isNotEmpty()) contracts.map { it.pricePerNight }.average().toInt() else 0
    val totalAllotment = contracts.sumOf { it.allotment }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text("📊 Kontrat Özet İstatistikleri", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                    HorizontalDivider(color = TourOSColors.Divider)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        // Toplam
                        TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                Text("Toplam Kontrat", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text("${contracts.size}", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                            }
                        }
                        // Aktif
                        TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.SuccessContainer.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                Text("Aktif Kontrat", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text("$activeCount", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Success))
                            }
                        }
                        // Pasif
                        TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.ErrorContainer.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                Text("Süresi Dolan", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text("$pastCount", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Error))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        TourOSCard(modifier = Modifier.weight(1f), contentPadding = TourOSSpacing.medium) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                Text("Ort. Gecelik Fiyat", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text("$avgPrice ₺", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                            }
                        }
                        TourOSCard(modifier = Modifier.weight(1f), contentPadding = TourOSSpacing.medium) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                Text("Toplam Kontenjan", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                Text("$totalAllotment Oda", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Secondary))
                            }
                        }
                    }
                }
            }
        }
    }
}

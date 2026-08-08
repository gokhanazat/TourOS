package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.SeasonPricingMatrixUiState
import com.mgacreative.touros.ui.viewmodel.SeasonPricingMatrixViewModel
import com.mgacreative.touros.ui.viewmodel.SeasonRateFormState

/**
 * 2.3.4 Sezon Fiyat Matrisi — TourOS 0.3
 *
 * Expanded: Tam düzenlenebilir veri tablosu (TourOSDataTable stili)
 * Compact : Kart listesi + inline form genişlemesi
 */
@Composable
fun SeasonPricingMatrixScreen(
    viewModel: SeasonPricingMatrixViewModel,
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
                title = "Sezon Fiyat Matrisi",
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
            is SeasonPricingMatrixUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }

            is SeasonPricingMatrixUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hata: ${state.message}",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                    )
                }
            }

            is SeasonPricingMatrixUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── Özet KPI ──────────────────────────────────────────
                        item {
                            SeasonKpiRow(
                                total = state.seasonRates.size,
                                active = state.seasonRates.count { it.isActive },
                                passive = state.seasonRates.count { !it.isActive }
                            )
                        }

                        // ── Tablo Başlığı ──────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 Tarih Aralığı Bazlı Fiyat Tablosu",
                                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                                )
                                Text(
                                    text = "${state.seasonRates.size} sezon",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                            }
                        }

                        // ── Expanded: Tam Tablo / Compact: Kart Listesi ────────
                        if (isExpanded && state.seasonRates.isNotEmpty()) {
                            item {
                                SeasonPricingTable(
                                    rates = state.seasonRates,
                                    roomTypes = state.roomTypes,
                                    onEditClick = { viewModel.openEditForm(it) },
                                    onDeleteClick = { viewModel.deleteSeasonRate(it.id) }
                                )
                            }
                        } else if (state.seasonRates.isEmpty() && !formState.isFormOpen) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                    ) {
                                        Text(
                                            "📭",
                                            style = TourOSTypography.TitleLarge
                                        )
                                        Text(
                                            "Tanımlı sezon fiyatı bulunamadı.",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(state.seasonRates) { _, rate ->
                                val roomTypeName = state.roomTypes.find { it.id == rate.roomTypeId }?.name ?: "Tüm Odalar"
                                SeasonRateCard(
                                    rate = rate,
                                    roomTypeName = roomTypeName,
                                    onEditClick = { viewModel.openEditForm(rate) },
                                    onDeleteClick = { viewModel.deleteSeasonRate(rate.id) }
                                )
                            }
                        }

                        // ── Form (AnimatedVisibility) ──────────────────────────
                        item {
                            AnimatedVisibility(
                                visible = formState.isFormOpen,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                SeasonRateForm(
                                    formState = formState,
                                    roomTypes = state.roomTypes,
                                    onSeasonNameChange = { viewModel.updateSeasonName(it) },
                                    onRoomTypeChange = { viewModel.updateRoomTypeId(it) },
                                    onStartDateChange = { viewModel.updateStartDate(it) },
                                    onEndDateChange = { viewModel.updateEndDate(it) },
                                    onSinglePriceChange = { viewModel.updateSinglePrice(it) },
                                    onDoublePriceChange = { viewModel.updateDoublePrice(it) },
                                    onTriplePriceChange = { viewModel.updateTriplePrice(it) },
                                    onExtraBedPriceChange = { viewModel.updateExtraBedPrice(it) },
                                    onChildPriceChange = { viewModel.updateChildPrice(it) },
                                    onCurrencyChange = { viewModel.updateCurrency(it) },
                                    onMealPlanChange = { viewModel.updateMealPlan(it) },
                                    onMinStayDaysChange = { viewModel.updateMinStayDays(it) },
                                    onIsActiveChange = { viewModel.updateIsActive(it) },
                                    onSave = { viewModel.saveSeasonRate() },
                                    onCancel = { viewModel.closeForm() }
                                )
                            }
                        }

                        // ── + Sezon Ekle Butonu ────────────────────────────────
                        item {
                            if (!formState.isFormOpen) {
                                TourOSButton(
                                    text = "+ Sezon Ekle",
                                    onClick = { viewModel.openNewForm() },
                                    variant = TourOSButtonVariant.SECONDARY,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── KPI Satırı ──────────────────────────────────────────────────────────────

@Composable
private fun SeasonKpiRow(total: Int, active: Int, passive: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        SeasonKpiCard("Toplam Sezon", total.toString(), TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
        SeasonKpiCard("Aktif", active.toString(), TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
        SeasonKpiCard("Pasif", passive.toString(), TourOSColors.Surface, TourOSColors.TextSecondary, Modifier.weight(1f))
    }
}

@Composable
private fun SeasonKpiCard(label: String, value: String, bgColor: Color, textColor: Color, modifier: Modifier) {
    TourOSCard(modifier = modifier, backgroundColor = bgColor, contentPadding = TourOSSpacing.medium) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = TourOSTypography.TitleLarge.copy(color = textColor))
            Text(label, style = TourOSTypography.Caption.copy(color = textColor.copy(alpha = 0.8f)))
        }
    }
}

// ─── Expanded: Düzenlenebilir Tablo ──────────────────────────────────────────

private val tableColumns = listOf(
    "Sezon Adı", "Tarih Aralığı", "Oda Tipi", "Pansiyon",
    "Single", "Double", "Triple", "Ek Yatak", "Çocuk", "Min.G", "Durum", "İşlem"
)
private val colWeights = listOf(1.4f, 1.6f, 1.2f, 0.7f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.6f, 0.7f, 1.0f)

@Composable
private fun SeasonPricingTable(
    rates: List<HotelSeasonRate>,
    roomTypes: List<RoomType>,
    onEditClick: (HotelSeasonRate) -> Unit,
    onDeleteClick: (HotelSeasonRate) -> Unit
) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        Column {
            // Tablo Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TourOSColors.Primary)
                    .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
                    .horizontalScroll(rememberScrollState())
            ) {
                tableColumns.forEachIndexed { i, col ->
                    Text(
                        text = col,
                        style = TourOSTypography.Label.copy(color = TourOSColors.OnPrimary),
                        modifier = Modifier.weight(colWeights[i]),
                        textAlign = if (i >= 4) TextAlign.Center else TextAlign.Start
                    )
                }
            }

            // Tablo Satırları
            rates.forEachIndexed { index, rate ->
                val bg = if (index % 2 == 0) TourOSColors.Background else TourOSColors.Surface
                val roomName = roomTypes.find { it.id == rate.roomTypeId }?.name ?: "Tümü"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sezon Adı
                    Text(
                        rate.seasonName,
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary),
                        modifier = Modifier.weight(colWeights[0])
                    )
                    // Tarih Aralığı
                    Column(modifier = Modifier.weight(colWeights[1])) {
                        Text(rate.startDate, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                        Text("→ ${rate.endDate}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                    }
                    // Oda Tipi
                    Text(
                        roomName,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.weight(colWeights[2])
                    )
                    // Pansiyon
                    Text(
                        rate.mealPlan,
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.weight(colWeights[3]),
                        textAlign = TextAlign.Center
                    )
                    // Fiyatlar
                    listOf(rate.singlePrice, rate.doublePrice, rate.triplePrice, rate.extraBedPrice, rate.childPrice)
                        .forEachIndexed { fi, price ->
                            Text(
                                "${price.toInt()} ${rate.currency}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                modifier = Modifier.weight(colWeights[4 + fi]),
                                textAlign = TextAlign.Center
                            )
                        }
                    // Min Gece
                    Text(
                        "${rate.minStayDays}G",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                        modifier = Modifier.weight(colWeights[9]),
                        textAlign = TextAlign.Center
                    )
                    // Durum
                    Box(modifier = Modifier.weight(colWeights[10])) {
                        TourOSStatusBadge(
                            text = if (rate.isActive) "Aktif" else "Pasif",
                            backgroundColor = if (rate.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                            textColor = if (rate.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                        )
                    }
                    // İşlem
                    Row(
                        modifier = Modifier.weight(colWeights[11]),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TourOSButton(
                            text = "Düzenle",
                            onClick = { onEditClick(rate) },
                            variant = TourOSButtonVariant.TERTIARY
                        )
                        TourOSButton(
                            text = "Sil",
                            onClick = { onDeleteClick(rate) },
                            variant = TourOSButtonVariant.DESTRUCTIVE
                        )
                    }
                }

                if (index < rates.size - 1) {
                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ─── Compact: Kart ───────────────────────────────────────────────────────────

@Composable
private fun SeasonRateCard(
    rate: HotelSeasonRate,
    roomTypeName: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rate.seasonName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                    Text(
                        "📅 ${rate.startDate}  →  ${rate.endDate}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                }
                TourOSStatusBadge(
                    text = if (rate.isActive) "Aktif" else "Pasif",
                    backgroundColor = if (rate.isActive) TourOSColors.SuccessContainer else TourOSColors.Surface,
                    textColor = if (rate.isActive) TourOSColors.Success else TourOSColors.TextSecondary
                )
            }

            // Meta bilgiler
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                SeasonBadge("🛏️ $roomTypeName")
                SeasonBadge("🍽️ ${rate.mealPlan}")
                SeasonBadge("⏳ ${rate.minStayDays} Gece")
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Fiyat çizelgesi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.4f))
                    .padding(TourOSSpacing.medium),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SeasonPriceCell("Single", rate.singlePrice, rate.currency)
                SeasonPriceCell("Double", rate.doublePrice, rate.currency)
                SeasonPriceCell("Triple", rate.triplePrice, rate.currency)
                SeasonPriceCell("Ek Yatak", rate.extraBedPrice, rate.currency)
                SeasonPriceCell("Çocuk", rate.childPrice, rate.currency)
            }

            // Aksiyonlar
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
                    variant = TourOSButtonVariant.TERTIARY
                )
            }
        }
    }
}

@Composable
private fun SeasonBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(TourOSColors.Surface)
            .border(0.5.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .padding(horizontal = TourOSSpacing.small, vertical = 3.dp)
    ) {
        Text(text, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
    }
}

@Composable
private fun SeasonPriceCell(label: String, price: Double, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
        Text(
            "${price.toInt()} $currency",
            style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
        )
    }
}

// ─── Sezon Form ──────────────────────────────────────────────────────────────

@Composable
private fun SeasonRateForm(
    formState: SeasonRateFormState,
    roomTypes: List<RoomType>,
    onSeasonNameChange: (String) -> Unit,
    onRoomTypeChange: (String?) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onSinglePriceChange: (String) -> Unit,
    onDoublePriceChange: (String) -> Unit,
    onTriplePriceChange: (String) -> Unit,
    onExtraBedPriceChange: (String) -> Unit,
    onChildPriceChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onMealPlanChange: (String) -> Unit,
    onMinStayDaysChange: (String) -> Unit,
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
            // Form Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (formState.isEditing) "✏️ Sezon Düzenle" else "➕ Yeni Sezon Ekle",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
                IconButton(onClick = onCancel) {
                    Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextSecondary))
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Sezon Adı
            TourOSTextField(
                value = formState.seasonName,
                onValueChange = onSeasonNameChange,
                label = "Sezon Adı",
                placeholder = "Örn: Yüksek Sezon, Bayram Dönemi",
                modifier = Modifier.fillMaxWidth()
            )

            // Tarih Aralığı
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

            // Oda Tipi Seçimi
            Text("Oda Tipi:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                FilterChip(
                    selected = formState.roomTypeId == null,
                    onClick = { onRoomTypeChange(null) },
                    label = { Text("Tümü", style = TourOSTypography.Caption) },
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

            // Pansiyon Tipi
            Text("Pansiyon Tipi:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                listOf("BB", "HB", "FB", "AI", "RO").forEach { code ->
                    FilterChip(
                        selected = formState.mealPlan == code,
                        onClick = { onMealPlanChange(code) },
                        label = { Text(code, style = TourOSTypography.Caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TourOSColors.PrimaryContainer,
                            selectedLabelColor = TourOSColors.Primary
                        )
                    )
                }
            }

            // Fiyat Matrisi
            Text("💰 Fiyat Matrisi (Gecelik):", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                TourOSTextField(value = formState.singlePrice, onValueChange = onSinglePriceChange, label = "Single", modifier = Modifier.weight(1f))
                TourOSTextField(value = formState.doublePrice, onValueChange = onDoublePriceChange, label = "Double", modifier = Modifier.weight(1f))
                TourOSTextField(value = formState.triplePrice, onValueChange = onTriplePriceChange, label = "Triple", modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                TourOSTextField(value = formState.extraBedPrice, onValueChange = onExtraBedPriceChange, label = "Ek Yatak", modifier = Modifier.weight(1f))
                TourOSTextField(value = formState.childPrice, onValueChange = onChildPriceChange, label = "Çocuk", modifier = Modifier.weight(1f))
                TourOSTextField(value = formState.minStayDays, onValueChange = onMinStayDaysChange, label = "Min Gece", modifier = Modifier.weight(1f))
            }

            // Aktif switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = formState.isActive,
                    onCheckedChange = onIsActiveChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = TourOSColors.Primary, checkedTrackColor = TourOSColors.PrimaryContainer)
                )
                Spacer(modifier = Modifier.width(TourOSSpacing.small))
                Text("Sezonu Aktif Yap", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary))
            }

            HorizontalDivider(color = TourOSColors.Divider)

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
                TourOSButton(
                    text = "💾 Kaydet",
                    onClick = onSave,
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    enabled = formState.seasonName.isNotBlank() && formState.startDate.isNotBlank() && formState.endDate.isNotBlank()
                )
            }
        }
    }
}

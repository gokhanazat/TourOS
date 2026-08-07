package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.ui.viewmodel.SeasonPricingMatrixUiState
import com.mgacreative.touros.ui.viewmodel.SeasonPricingMatrixViewModel
import com.mgacreative.touros.ui.viewmodel.SeasonRateFormState

/**
 * 2.3.4 Tarih Aralığı Bazlı Sezon Fiyat Matrisi Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonPricingMatrixScreen(
    viewModel: SeasonPricingMatrixViewModel,
    hotelId: String = "1",
    hotelName: String = "Grand Cave Suites",
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(hotelId) {
        viewModel.initForHotel(hotelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🗓️ Sezon Fiyat Matrisi", fontWeight = FontWeight.Bold)
                        Text(hotelName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
                    Text("+ Sezon Fiyatı Tanımla", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
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
                is SeasonPricingMatrixUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SeasonPricingMatrixUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is SeasonPricingMatrixUiState.Success -> {
                    if (formState.isFormOpen) {
                        // Sezon Fiyat Tanımlama / Düzenleme Formu
                        SeasonRateFormCard(
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
                    } else {
                        // Sezon Fiyat Matrisi Listesi & Çizelgesi
                        Text(
                            text = "🗓️ Tarih Aralığı Bazlı Sezon Fiyat Listesi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (state.seasonRates.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Tanımlı sezon fiyatı bulunamadı. Ekle butonuna basarak yeni fiyat matrisi ekleyebilirsiniz.")
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(state.seasonRates) { rate ->
                                    val roomTypeName = state.roomTypes.find { it.id == rate.roomTypeId }?.name ?: "Tüm Oda Tipleri"
                                    SeasonRateMatrixCard(
                                        rate = rate,
                                        roomTypeName = roomTypeName,
                                        onEditClick = { viewModel.openEditForm(rate) },
                                        onDeleteClick = { viewModel.deleteSeasonRate(rate.id) }
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
fun SeasonRateMatrixCard(
    rate: HotelSeasonRate,
    roomTypeName: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(rate.seasonName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("📅 ${rate.startDate}  ➜  ${rate.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (rate.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (rate.isActive) "Aktif" else "Pasif",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rate.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text("🛏️ Oda Tipi: $roomTypeName | 🍽️ Pansiyon: ${rate.mealPlan} | ⏳ Min. Konaklama: ${rate.minStayDays} Gece", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Fiyat Matrisi Çizelgesi (Single, Double, Triple, Extra Bed, Child)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PriceItemColumn("Single (1 P)", rate.singlePrice, rate.currency)
                PriceItemColumn("Double (2 P)", rate.doublePrice, rate.currency)
                PriceItemColumn("Triple (3 P)", rate.triplePrice, rate.currency)
                PriceItemColumn("Ek Yatak", rate.extraBedPrice, rate.currency)
                PriceItemColumn("Çocuk", rate.childPrice, rate.currency)
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
fun PriceItemColumn(title: String, price: Double, currency: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${price.toInt()} $currency", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SeasonRateFormCard(
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (formState.isEditing) "✏️ Sezon Fiyatı Düzenle" else "➕ Yeni Sezon Fiyat Tanımla",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCancel) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            OutlinedTextField(
                value = formState.seasonName,
                onValueChange = onSeasonNameChange,
                label = { Text("Sezon Adı (Örn: Yüksek Sezon, Bayram Dönemi)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Oda Tipi Filtre Seçimi
            Text("Oda Tipi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = formState.roomTypeId == null,
                    onClick = { onRoomTypeChange(null) },
                    label = { Text("Tüm Odalar", fontSize = 11.sp) }
                )
                roomTypes.forEach { room ->
                    FilterChip(
                        selected = formState.roomTypeId == room.id,
                        onClick = { onRoomTypeChange(room.id) },
                        label = { Text(room.name, fontSize = 11.sp) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.startDate,
                    onValueChange = onStartDateChange,
                    label = { Text("Başlangıç (YYYY-AA-GG)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.endDate,
                    onValueChange = onEndDateChange,
                    label = { Text("Bitiş (YYYY-AA-GG)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Fiyat Matrisi Girişleri
            Text("💰 Fiyat Matrisi (Gecelik - TRY):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formState.singlePrice,
                    onValueChange = onSinglePriceChange,
                    label = { Text("Single (1 PAX)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.doublePrice,
                    onValueChange = onDoublePriceChange,
                    label = { Text("Double (2 PAX)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.triplePrice,
                    onValueChange = onTriplePriceChange,
                    label = { Text("Triple (3 PAX)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formState.extraBedPrice,
                    onValueChange = onExtraBedPriceChange,
                    label = { Text("Ek Yatak Fiyatı") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.childPrice,
                    onValueChange = onChildPriceChange,
                    label = { Text("Çocuk Fiyatı") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.minStayDays,
                    onValueChange = onMinStayDaysChange,
                    label = { Text("Min Gece") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Pansiyon Tipi
            Text("Pansiyon Tipi:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("BB", "HB", "FB", "AI", "RO").forEach { code ->
                    FilterChip(
                        selected = formState.mealPlan == code,
                        onClick = { onMealPlanChange(code) },
                        label = { Text(code, fontSize = 10.sp) }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = formState.isActive, onCheckedChange = onIsActiveChange)
                Text("Sezon Fiyatını Aktif Yap", fontSize = 12.sp)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.seasonName.isNotBlank() && formState.startDate.isNotBlank() && formState.endDate.isNotBlank()
            ) {
                Text("💾 Sezon Fiyatını Kaydet")
            }
        }
    }
}

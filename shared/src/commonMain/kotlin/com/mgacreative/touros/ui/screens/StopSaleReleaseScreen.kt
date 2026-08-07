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
import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.ui.viewmodel.StopSaleFormState
import com.mgacreative.touros.ui.viewmodel.StopSaleReleaseUiState
import com.mgacreative.touros.ui.viewmodel.StopSaleReleaseViewModel

/**
 * 2.3.5 Stop Sale & Release Yönetim Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopSaleReleaseScreen(
    viewModel: StopSaleReleaseViewModel,
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
                        Text("⛔ Stop Sale & 🔓 Release", fontWeight = FontWeight.Bold)
                        Text(hotelName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
                is StopSaleReleaseUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StopSaleReleaseUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is StopSaleReleaseUiState.Success -> {
                    // Özet İstatistik Kartı
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⛔ Aktif Stop Sale", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.SemiBold)
                                Text("${state.activeStopSaleCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔓 Aktif Release", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                                Text("${state.activeReleaseCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // İşlem Butonları (Stop Sale Uygula / Release Et)
                    if (!formState.isFormOpen) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.openNewForm("STOP_SALE") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⛔ Satış Durdur (Stop Sale)", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.openNewForm("RELEASE") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🔓 Release Et", fontSize = 12.sp)
                            }
                        }
                    }

                    // Form Açıksa Formu Göster
                    if (formState.isFormOpen) {
                        StopSaleFormCard(
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
                    } else {
                        // Liste Gösterimi
                        Text("📋 Geçmiş ve Aktif Satış Durdurma / Release Kayıtları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (state.stopSales.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Kayıtlı Stop Sale veya Release işlemi bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(state.stopSales) { item ->
                                    val roomTypeName = state.roomTypes.find { it.id == item.roomTypeId }?.name ?: "Tüm Oda Tipleri"
                                    StopSaleItemCard(
                                        item = item,
                                        roomTypeName = roomTypeName,
                                        onToggleStatus = { viewModel.toggleStatus(item.id, item.isActive) },
                                        onDelete = { viewModel.deleteItem(item.id) }
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
fun StopSaleItemCard(
    item: HotelStopSale,
    roomTypeName: String,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isStopSale = item.actionType == "STOP_SALE"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isStopSale) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (isStopSale) "⛔ STOP SALE" else "🔓 RELEASE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStopSale) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(roomTypeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Switch(checked = item.isActive, onCheckedChange = { onToggleStatus() })
            }

            Text(
                text = "📅 Tarih Aralığı: ${item.startDate}  ➜  ${item.endDate}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!item.reason.isNullOrBlank()) {
                Text(
                    text = "📝 Gerekçe / Etki: ${item.reason}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isStopSale) "⚠️ Otomatik Etki: Seçilen tarihlerdeki Tur Kalkışlarında ve Rezervasyon Sihirbazında bu otel satışı durduruldu."
                    else "ℹ️ Otomatik Etki: Serbest bırakılan kontenjan genel stoka devredildi.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) {
                    Text("Sil", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StopSaleFormCard(
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (formState.actionType == "STOP_SALE") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (formState.actionType == "STOP_SALE") "⛔ Satış Durdurma (Stop Sale) Tanımla" else "🔓 Kontenjan Serbest Bırak (Release)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCancel) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            // İşlem Tipi Seçimi (Stop Sale / Release)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = formState.actionType == "STOP_SALE",
                    onClick = { onActionTypeChange("STOP_SALE") },
                    label = { Text("⛔ Stop Sale", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = formState.actionType == "RELEASE",
                    onClick = { onActionTypeChange("RELEASE") },
                    label = { Text("🔓 Release", fontSize = 11.sp) }
                )
            }

            // Oda Tipi Seçimi
            Text("Oda Tipi Kapsamı:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = formState.roomTypeId == null,
                    onClick = { onRoomTypeChange(null) },
                    label = { Text("Tüm Oda Tipleri", fontSize = 11.sp) }
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

            OutlinedTextField(
                value = formState.reason,
                onValueChange = onReasonChange,
                label = { Text("İşlem Gerekçesi / Not (Örn: Otel Dolu, Acente İadesi)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = formState.isActive, onCheckedChange = onIsActiveChange)
                Text("İşlemi Anında Aktifleştir", fontSize = 12.sp)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (formState.actionType == "STOP_SALE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                enabled = formState.startDate.isNotBlank() && formState.endDate.isNotBlank()
            ) {
                Text(if (formState.actionType == "STOP_SALE") "⛔ Satışı Durdur ve Turlara Uygula" else "🔓 Release İşlemini Kaydet")
            }
        }
    }
}

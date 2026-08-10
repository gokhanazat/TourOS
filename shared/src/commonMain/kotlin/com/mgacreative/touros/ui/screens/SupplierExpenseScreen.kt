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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.SupplierTransaction
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.SupplierExpenseUiState
import com.mgacreative.touros.ui.viewmodel.SupplierExpenseViewModel

/**
 * 3.1.3 Tedarikçi Cari & Otomatik Gider Akışı Ekranı — TourOS Canlı Veri Sürümü
 */
@Composable
fun SupplierExpenseScreen(
    viewModel: SupplierExpenseViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var isAddingExpense by remember { mutableStateOf(false) }
    var supplierName by remember { mutableStateOf("") }
    var supplierType by remember { mutableStateOf("hotel") } // hotel, vehicle, guide, other
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Tedarikçi Cari & Gider Girişi",
                subtitle = "Otel, araç yakıt ve rehber gider akışları ve cari takibi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    TourOSButton(
                        text = if (isAddingExpense) "📋 Liste" else "➕ Yeni Gider Gir",
                        onClick = { isAddingExpense = !isAddingExpense },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            when (val state = uiState) {
                is SupplierExpenseUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                }
                is SupplierExpenseUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                    }
                }
                is SupplierExpenseUiState.Success -> {
                    if (state.notificationMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.SuccessContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                state.notificationMessage!!,
                                style = TourOSTypography.Label.copy(color = TourOSColors.Success),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isAddingExpense) {
                        // ── MANUEL GİDER GİRİŞ FORMU ───────────────────────────────────
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.PrimaryContainer,
                            contentPadding = TourOSSpacing.large
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                                Text(
                                    "➕ Yeni Gider / Tedarikçi Borcu Ekle",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                    fontWeight = FontWeight.Bold
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Gider Kategori / Tedarikçi Türü:", style = TourOSTypography.Caption, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                        FilterChip(
                                            selected = supplierType == "hotel",
                                            onClick = { supplierType = "hotel" },
                                            label = { Text("🏨 Oteller", style = TourOSTypography.Caption) }
                                        )
                                        FilterChip(
                                            selected = supplierType == "vehicle",
                                            onClick = { supplierType = "vehicle" },
                                            label = { Text("🚌 Araç / Yakıt", style = TourOSTypography.Caption) }
                                        )
                                        FilterChip(
                                            selected = supplierType == "guide",
                                            onClick = { supplierType = "guide" },
                                            label = { Text("🚩 Rehber", style = TourOSTypography.Caption) }
                                        )
                                        FilterChip(
                                            selected = supplierType == "other",
                                            onClick = { supplierType = "other" },
                                            label = { Text("🏢 Diğer Gider", style = TourOSTypography.Caption) }
                                        )
                                    }
                                }

                                TourOSTextField(
                                    value = supplierName,
                                    onValueChange = { supplierName = it },
                                    label = "Tedarikçi / Alacaklı Adı",
                                    placeholder = "Örn: Hilton Hotel veya Petrol Ofisi A.Ş.",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                                ) {
                                    TourOSTextField(
                                        value = amountStr,
                                        onValueChange = { amountStr = it },
                                        label = "Gider Tutarı (₺)",
                                        placeholder = "Örn: 4500",
                                        modifier = Modifier.weight(1f)
                                    )
                                    TourOSTextField(
                                        value = description,
                                        onValueChange = { description = it },
                                        label = "Açıklama / Belge No",
                                        placeholder = "Örn: Travego Otobüs 4 Günlük Yakıt",
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }

                                TourOSButton(
                                    text = if (state.isCreatingExpense) "Kaydediliyor..." else "💾 Gider Kaydını Veritabanına İşle",
                                    onClick = {
                                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                                        if (supplierName.isNotBlank() && amt > 0) {
                                            viewModel.createSupplierExpense(
                                                supplierName = supplierName,
                                                supplierType = supplierType,
                                                amount = amt,
                                                categoryName = description,
                                                notes = description
                                            )
                                            supplierName = ""
                                            amountStr = ""
                                            description = ""
                                            isAddingExpense = false
                                        }
                                    },
                                    variant = TourOSButtonVariant.PRIMARY,
                                    enabled = supplierName.isNotBlank() && amountStr.toDoubleOrNull() != null && !state.isCreatingExpense,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // 1. KPI Özet Borç Kartları
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            SummaryCard("🏨 Otel Borçları", "${formatMoney(state.totalHotelDebt)} TRY", TourOSColors.PrimaryContainer, TourOSColors.Primary, Modifier.weight(1f))
                            SummaryCard("🚌 Araç Borçları", "${formatMoney(state.totalVehicleDebt)} TRY", TourOSColors.SecondaryContainer, TourOSColors.Secondary, Modifier.weight(1f))
                            SummaryCard("🚩 Rehber Borçları", "${formatMoney(state.totalGuideDebt)} TRY", TourOSColors.SuccessContainer, TourOSColors.Success, Modifier.weight(1f))
                        }

                        // 2. Kategori Filtre Çipleri
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            FilterChip(
                                selected = state.selectedCategoryFilter == null,
                                onClick = { viewModel.setCategoryFilter(null) },
                                label = { Text("Tüm Tedarikçiler", style = TourOSTypography.Caption) }
                            )
                            FilterChip(
                                selected = state.selectedCategoryFilter == "hotel",
                                onClick = { viewModel.setCategoryFilter("hotel") },
                                label = { Text("🏨 Oteller", style = TourOSTypography.Caption) }
                            )
                            FilterChip(
                                selected = state.selectedCategoryFilter == "vehicle",
                                onClick = { viewModel.setCategoryFilter("vehicle") },
                                label = { Text("🚌 Araçlar", style = TourOSTypography.Caption) }
                            )
                            FilterChip(
                                selected = state.selectedCategoryFilter == "guide",
                                onClick = { viewModel.setCategoryFilter("guide") },
                                label = { Text("🚩 Rehberler", style = TourOSTypography.Caption) }
                            )
                        }

                        // 3. Tedarikçi Cari Defteri & Gider Kartları Listesi
                        Text("📖 Tedarikçi Cari Defteri & Kayıtlı Giderler (${state.transactions.size}):", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)

                        if (state.transactions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Henüz veritabanında kaydedilmiş bir gider bulunmamaktadır.", style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small), modifier = Modifier.fillMaxWidth().weight(1f)) {
                                items(state.transactions) { item ->
                                    SupplierLedgerCard(
                                        item = item,
                                        onSettleClick = { viewModel.settleTransaction(item) }
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
private fun SummaryCard(title: String, value: String, bg: Color, textColor: Color, modifier: Modifier) {
    TourOSCard(modifier = modifier, backgroundColor = bg, contentPadding = TourOSSpacing.medium) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = TourOSTypography.Caption.copy(color = textColor.copy(alpha = 0.8f)), fontWeight = FontWeight.Bold)
            Text(value, style = TourOSTypography.TitleMedium.copy(color = textColor), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SupplierLedgerCard(
    item: SupplierTransaction,
    onSettleClick: () -> Unit
) {
    val categoryIcon = when (item.supplierType) {
        "hotel" -> "🏨 Otel Gideri"
        "vehicle" -> "🚌 Araç/Yakıt Gideri"
        "guide" -> "🚩 Rehber Gideri"
        else -> "🏢 Tedarikçi Gideri"
    }

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                ) {
                    Text(categoryIcon, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(if (item.isSettled) TourOSColors.SuccessContainer else TourOSColors.SecondaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.isSettled) "🟢 Ödendi & Giderleşti" else "🔴 Bekleyen Borç",
                        style = TourOSTypography.Caption.copy(color = if (item.isSettled) TourOSColors.Success else TourOSColors.Secondary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(item.supplierName, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
            Text("📝 ${item.description}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))

            HorizontalDivider(color = TourOSColors.Divider)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Cari Gider Tutarı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("${formatMoney(item.amount)} ${item.currency}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                }

                TourOSButton(
                    text = if (item.isSettled) "✅ Gider Kaydı İşlendi" else "⚡ Ödeme Yap & İşle",
                    onClick = onSettleClick,
                    variant = if (item.isSettled) TourOSButtonVariant.SECONDARY else TourOSButtonVariant.PRIMARY,
                    enabled = !item.isSettled
                )
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

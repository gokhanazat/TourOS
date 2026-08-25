package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OperatorBookingRowItem
import com.mgacreative.touros.ui.viewmodel.OperatorPaymentUiState
import com.mgacreative.touros.ui.viewmodel.OperatorPaymentViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OperatorPaymentManagementScreen(
    viewModel: OperatorPaymentViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedRowForPayment by remember { mutableStateOf<OperatorBookingRowItem?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("BANK_TRANSFER") }
    var receiptNumberText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("Tur Operatörü Rezervasyon Ödemeleri"),
                subtitle = AppLanguageManager.translate("Operatör bazlı TO PNR kayıtları, borç takibi ve rezervasyon ödeme işlemleri"),
                onNavigateBack = onNavigateBack,
                actions = {
                    TourOSButton(
                        text = "🔄 ${AppLanguageManager.translate("Yenile")}",
                        onClick = { viewModel.loadData() },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(TourOSColors.Background)
        ) {
            when (val state = uiState) {
                is OperatorPaymentUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                }
                is OperatorPaymentUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TourOSEmptyState(
                            title = AppLanguageManager.translate("Veri Yükleme Hatası"),
                            description = state.message,
                            actionButtonText = AppLanguageManager.translate("Tekrar Dene"),
                            onActionClick = { viewModel.loadData() }
                        )
                    }
                }
                is OperatorPaymentUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(TourOSSpacing.medium),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // 1. BİLDİRİM BANNER'I
                        if (state.notificationMessage != null) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = state.notificationMessage,
                                            style = TourOSTypography.BodyMedium.copy(color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                                        )
                                        IconButton(
                                            onClick = { viewModel.clearNotification() },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Text("✕", fontSize = 12.sp, color = Color(0xFF065F46))
                                        }
                                    }
                                }
                            }
                        }

                        // 2. ÖZET KPI KARTLARI
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                OPSummaryCard(
                                    title = AppLanguageManager.translate("Toplam TO Maliyeti"),
                                    value = "${state.totalCostSum.toInt()} EUR",
                                    icon = "🏢",
                                    containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                                    contentColor = TourOSColors.Primary,
                                    modifier = Modifier.weight(1f)
                                )
                                OPSummaryCard(
                                    title = AppLanguageManager.translate("Yapılan TO Ödemesi"),
                                    value = "${state.totalPaidSum.toInt()} EUR",
                                    icon = "💳",
                                    containerColor = Color(0xFFECFDF5),
                                    contentColor = Color(0xFF059669),
                                    modifier = Modifier.weight(1f)
                                )
                                OPSummaryCard(
                                    title = AppLanguageManager.translate("Kalan Net TO Borcu"),
                                    value = "${state.totalBalanceSum.toInt()} EUR",
                                    icon = "⏳",
                                    containerColor = Color(0xFFFEF2F2),
                                    contentColor = Color(0xFFDC2626),
                                    modifier = Modifier.weight(1f)
                                )
                                OPSummaryCard(
                                    title = AppLanguageManager.translate("Eksik PNR Sayısı"),
                                    value = "${state.missingPnrCount} Adet",
                                    icon = "⚠️",
                                    containerColor = Color(0xFFFFFBEB),
                                    contentColor = Color(0xFFD97706),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 3. FİLTRELEME & ARAMA ÇUBUĞU
                        item {
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.Surface,
                                contentPadding = TourOSSpacing.small
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Arama Kutusu
                                    Box(modifier = Modifier.weight(1.5f)) {
                                        TourOSTextField(
                                            value = state.searchQuery,
                                            onValueChange = { viewModel.updateFilters(query = it) },
                                            placeholder = AppLanguageManager.translate("Misafir, PNR veya Rezervasyon No ara..."),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // Operatör Seçimi
                                    var showOpDropdown by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        TourOSTextField(
                                            value = "🏢 ${state.selectedOperator}",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = AppLanguageManager.translate("Tur Operatörü"),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Box(modifier = Modifier.matchParentSize().clickable { showOpDropdown = true })
                                        DropdownMenu(
                                            expanded = showOpDropdown,
                                            onDismissRequest = { showOpDropdown = false },
                                            modifier = Modifier.width(220.dp).background(TourOSColors.Surface)
                                        ) {
                                            state.availableOperators.forEach { op ->
                                                DropdownMenuItem(
                                                    text = { Text(op, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                    onClick = {
                                                        viewModel.updateFilters(operator = op)
                                                        showOpDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Durum Filtresi (Tümü, Borçlu, Eksik PNR, Ödendi)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf("Tümü", "Borçlu", "Eksik PNR", "Ödendi").forEach { statusKey ->
                                            val isSel = state.selectedStatusFilter == statusKey
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) TourOSColors.Primary else TourOSColors.Background,
                                                border = BorderStroke(1.dp, if (isSel) TourOSColors.Primary else TourOSColors.Border),
                                                modifier = Modifier.clickable { viewModel.updateFilters(statusFilter = statusKey) }
                                            ) {
                                                Text(
                                                    text = AppLanguageManager.translate(statusKey),
                                                    style = TourOSTypography.Caption.copy(
                                                        color = if (isSel) Color.White else TourOSColors.TextPrimary,
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 11.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. REZERVASYON LİSTESİ TABLOSU
                        if (state.filteredRows.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TourOSEmptyState(
                                        title = AppLanguageManager.translate("Eşleşen Kayıt Bulunamadı"),
                                        description = AppLanguageManager.translate("Seçili operatör ve filtre kriterlerine uygun rezervasyon bulunmamaktadır.")
                                    )
                                }
                            }
                        } else {
                            items(state.filteredRows, key = { it.bookingId }) { row ->
                                OperatorBookingPaymentRowCard(
                                    row = row,
                                    onSavePnr = { pnr -> viewModel.saveOperatorPnr(row.bookingId, pnr) },
                                    onOpenPaymentDialog = {
                                        selectedRowForPayment = row
                                        paymentAmountText = row.remainingBalance.toInt().toString()
                                        receiptNumberText = ""
                                        notesText = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 5. ÖDEME YAPMA MODALI
            if (selectedRowForPayment != null) {
                val row = selectedRowForPayment!!
                AlertDialog(
                    onDismissRequest = { selectedRowForPayment = null },
                    containerColor = TourOSColors.Surface,
                    shape = RoundedCornerShape(12.dp),
                    title = {
                        Text(
                            text = "💳 ${AppLanguageManager.translate("Tur Operatörüne Ödeme Girişi")}",
                            style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Özet Bilgi
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "🏢 ${row.operatorName}  ·  Rez: ${row.bookingCode}",
                                        style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                    Text(
                                        text = "✈️ TO PNR: ${row.operatorPnrCode.ifBlank { "Eksik" }}  ·  Turist: ${row.customerName}",
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = "Toplam Maliyet: ${row.operatorCost.toInt()} ${row.currency}  ·  Kalan Borç: ${row.remainingBalance.toInt()} ${row.currency}",
                                        style = TourOSTypography.Caption.copy(color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }

                            // Ödeme Tutarı
                            TourOSTextField(
                                value = paymentAmountText,
                                onValueChange = { paymentAmountText = it },
                                label = AppLanguageManager.translate("Ödeme Tutarı (${row.currency})"),
                                placeholder = row.remainingBalance.toInt().toString(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Ödeme Yöntemi
                            var showMethodDropdown by remember { mutableStateOf(false) }
                            val methodLabels = mapOf(
                                "BANK_TRANSFER" to "🏦 Banka Havalesi / EFT",
                                "CREDIT_CARD" to "💳 Kredi Kartı",
                                "CURRENT_ACCOUNT" to "📑 Cari Mahsup",
                                "CASH" to "💵 Nakit Ödeme"
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                TourOSTextField(
                                    value = methodLabels[paymentMethod] ?: paymentMethod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = AppLanguageManager.translate("Ödeme Yöntemi"),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { showMethodDropdown = true })
                                DropdownMenu(
                                    expanded = showMethodDropdown,
                                    onDismissRequest = { showMethodDropdown = false },
                                    modifier = Modifier.width(260.dp).background(TourOSColors.Surface)
                                ) {
                                    methodLabels.forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                            onClick = {
                                                paymentMethod = key
                                                showMethodDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Dekont No / Belge No
                            TourOSTextField(
                                value = receiptNumberText,
                                onValueChange = { receiptNumberText = it },
                                label = AppLanguageManager.translate("Dekont / Makbuz No"),
                                placeholder = "Örn: DKB-849201",
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Açıklama
                            TourOSTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                label = AppLanguageManager.translate("Not / Açıklama"),
                                placeholder = "Örn: Coral Travel Garanti Bankası EFT",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TourOSButton(
                            text = "✓ ${AppLanguageManager.translate("Ödemeyi Kaydet")}",
                            onClick = {
                                val amountVal = paymentAmountText.toDoubleOrNull() ?: 0.0
                                viewModel.recordPayment(
                                    bookingId = row.bookingId,
                                    operatorName = row.operatorName,
                                    pnrCode = row.operatorPnrCode,
                                    amount = amountVal,
                                    currency = row.currency,
                                    paymentMethod = paymentMethod,
                                    receiptNumber = receiptNumberText,
                                    notes = notesText
                                )
                                selectedRowForPayment = null
                            },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    },
                    dismissButton = {
                        TourOSButton(
                            text = AppLanguageManager.translate("İptal"),
                            onClick = { selectedRowForPayment = null },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun OperatorBookingPaymentRowCard(
    row: OperatorBookingRowItem,
    onSavePnr: (String) -> Unit,
    onOpenPaymentDialog: () -> Unit
) {
    var pnrInput by remember(row.operatorPnrCode) { mutableStateOf(row.operatorPnrCode) }
    var isEditingPnr by remember(row.hasMissingPnr) { mutableStateOf(row.hasMissingPnr) }

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Rezervasyon & Operatör Bilgisi
            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🏢 ${row.operatorName}",
                        style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextPrimary, fontSize = 13.sp)
                    )
                    Text(
                        text = "#${row.bookingCode}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp)
                    )
                }
                Text(
                    text = "🏨 ${row.hotelOrTourName}  ·  👤 ${row.customerName}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp),
                    maxLines = 1
                )
                Text(
                    text = "📅 ${row.bookingDate}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextDisabled, fontSize = 10.sp)
                )
            }

            // 2. TO PNR Kodu Girişi ve Rozet
            Box(modifier = Modifier.weight(1.1f).padding(horizontal = 8.dp)) {
                if (isEditingPnr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = pnrInput,
                            onValueChange = { pnrInput = it },
                            singleLine = true,
                            textStyle = TourOSTypography.Caption.copy(fontSize = 12.sp, color = TourOSColors.Primary, fontWeight = FontWeight.Bold),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(30.dp)
                                        .background(TourOSColors.Background, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (row.hasMissingPnr) Color(0xFFEF4444) else TourOSColors.Border, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (pnrInput.isEmpty()) Text("TO PNR No", style = TourOSTypography.Caption.copy(fontSize = 10.sp, color = TourOSColors.TextDisabled))
                                    inner()
                                }
                            }
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TourOSColors.Primary,
                            modifier = Modifier.clickable {
                                onSavePnr(pnrInput)
                                isEditingPnr = false
                            }
                        ) {
                            Text(
                                text = "💾",
                                style = TourOSTypography.Caption.copy(color = Color.White, fontSize = 11.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "✓ PNR: ${row.operatorPnrCode}",
                                style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(onClick = { isEditingPnr = true }, modifier = Modifier.size(20.dp)) {
                            Text("✏️", fontSize = 10.sp)
                        }
                    }
                }
            }

            // 3. Maliyet ve Ödeme Özeti
            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Maliyet: ${row.operatorCost.toInt()} ${row.currency}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                )
                Text(
                    text = "Ödenen: ${row.totalPaid.toInt()} ${row.currency}",
                    style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontSize = 11.sp)
                )
                val balColor = if (row.isFullyPaid) Color(0xFF059669) else Color(0xFFDC2626)
                Text(
                    text = "Kalan: ${row.remainingBalance.toInt()} ${row.currency}",
                    style = TourOSTypography.Caption.copy(color = balColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                )
            }

            // 4. Ödeme Butonu
            Box(modifier = Modifier.padding(start = 12.dp)) {
                if (row.isFullyPaid) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "✓ Ödendi",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TourOSColors.Primary,
                        modifier = Modifier.clickable { onOpenPaymentDialog() }
                    ) {
                        Text(
                            text = "💳 ${AppLanguageManager.translate("Öde")}",
                            style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OPSummaryCard(
    title: String,
    value: String,
    icon: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = icon, fontSize = 22.sp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp)
                )
                Text(
                    text = value,
                    style = TourOSTypography.TitleMedium.copy(color = contentColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                )
            }
        }
    }
}


package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.OperatorLedgerItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.OperatorCurrentAccountReportViewModel
import com.mgacreative.touros.ui.viewmodel.OperatorCurrentAccountUiState
import org.koin.compose.viewmodel.koinViewModel

/**
 * 🏢 Tur Operatörü Cari Hesap & PNR Ekstre Raporu Ekranı.
 * TO PNR - Müşteri Adı - Paket Tur Kodu - Tur Satışı - TO Ödeme - Bakiye
 */
@Composable
fun OperatorCurrentAccountReportScreen(
    viewModel: OperatorCurrentAccountReportViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val msg = (uiState as? OperatorCurrentAccountUiState.Success)?.notificationMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TourOSColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.medium)
        ) {
            when (val state = uiState) {
                is OperatorCurrentAccountUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                    }
                }

                is OperatorCurrentAccountUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = TourOSColors.Error, style = TourOSTypography.BodyMedium)
                    }
                }

                is OperatorCurrentAccountUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── 1. Başlık & Aksiyon Butonları (Paylaş / CSV & Yazdır / PDF & Geri Dön) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Geri Dön",
                                        tint = TourOSColors.Primary
                                    )
                                }
                                Column {
                                    Text(
                                        text = "🏢 ${AppLanguageManager.translate("TO Cari Hesap & PNR Ekstresi")}",
                                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = AppLanguageManager.translate("Tur operatörleri bazında PNR satışları, yapılan ödemeler ve kalan cari bakiye dökümü"),
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                Button(
                                    onClick = { viewModel.exportToCsv() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("📄 ${AppLanguageManager.translate("Paylaş (CSV)")}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.triggerPrint() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A56), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🖨️ ${AppLanguageManager.translate("Yazdır / PDF")}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // ── 2. Filtreleme Çubuğu (Tarih Aralığı & Tur Operatörü Seçici) ──
                        TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.medium) {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                Text(
                                    text = "⚡ ${AppLanguageManager.translate("Filtreleme & Arama Seçenekleri")}",
                                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tarih Aralığı Filtresi
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("📅 ${AppLanguageManager.translate("Tarih Aralığı:")}", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("Tüm Zamanlar", "Bu Ay", "Bu Yıl").forEach { filterLabel ->
                                                val isSelected = state.selectedDateFilter == filterLabel
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.setFilter(state.selectedOperator, filterLabel) },
                                                    label = { Text(AppLanguageManager.translate(filterLabel), fontSize = 11.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                                        selectedLabelColor = TourOSColors.Primary
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Tur Operatörü Açılır Kutusu (Dropdown)
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("💼 ${AppLanguageManager.translate("Tur Operatörü Seçin:")}", style = TourOSTypography.Caption, color = TourOSColors.TextSecondary)
                                        var showOpDropdown by remember { mutableStateOf(false) }

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            TourOSTextField(
                                                value = "💼 ${if (state.selectedOperator == "Tümü") AppLanguageManager.translate("Tümü") else state.selectedOperator} ▼",
                                                onValueChange = { },
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Box(modifier = Modifier.matchParentSize().clickable { showOpDropdown = !showOpDropdown })

                                            DropdownMenu(
                                                expanded = showOpDropdown,
                                                onDismissRequest = { showOpDropdown = false },
                                                modifier = Modifier.width(260.dp).background(TourOSColors.Surface)
                                            ) {
                                                state.availableOperators.forEach { opName ->
                                                    DropdownMenuItem(
                                                        text = { Text("💼 ${if (opName == "Tümü") AppLanguageManager.translate("Tümü") else opName}", style = TourOSTypography.BodyMedium.copy(fontSize = 12.sp)) },
                                                        onClick = {
                                                            viewModel.setFilter(opName, state.selectedDateFilter)
                                                            showOpDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── 3. Üst Özet Metrik Kutuları (Toplam Borç - Toplam Ödeme - Kalan Bakiye) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            // Toplam Borç (Tur Maliyeti)
                            SummaryCard(
                                title = AppLanguageManager.translate("Toplam Borç (Tur Maliyeti)"),
                                value = "${formatCurrency(state.totalCost)} ₺",
                                subtitle = "${AppLanguageManager.translate("Paket Satış")}: ${formatCurrency(state.totalSales)} ₺",
                                icon = "💳",
                                containerColor = TourOSColors.PrimaryContainer.copy(alpha = 0.3f),
                                contentColor = TourOSColors.Primary,
                                modifier = Modifier.weight(1f)
                            )

                            // Toplam Ödeme (TO Ödenen)
                            SummaryCard(
                                title = AppLanguageManager.translate("Toplam Ödeme (TO Ödenen)"),
                                value = "${formatCurrency(state.totalPaid)} ₺",
                                subtitle = AppLanguageManager.translate("Acentenin TO'ya Ödediği"),
                                icon = "🟢",
                                containerColor = Color(0xFFECFDF5),
                                contentColor = Color(0xFF059669),
                                modifier = Modifier.weight(1f)
                            )

                            // Net Bakiye (Kalan Borç)
                            SummaryCard(
                                title = AppLanguageManager.translate("Net Bakiye (Kalan TO Borcu)"),
                                value = "${formatCurrency(state.totalBalance)} ₺",
                                subtitle = AppLanguageManager.translate("Kalan Ödenecek Tutar"),
                                icon = "⚖️",
                                containerColor = Color(0xFFFEF3C7),
                                contentColor = Color(0xFFD97706),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ── 4. Döküm Tablosu (TO PNR | Müşteri Adı | Paket Tur Kodu | TO Acente Adı | Tur Satış Fiyatı | Acenta % | Tur Satış (Maliyeti) | TO Ödemesi | Bakiye) ──
                        TourOSCard(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = 0.dp) {
                            val horizontalScrollState = rememberScrollState()
                            Box(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
                                Column(modifier = Modifier.width(1280.dp).fillMaxHeight()) {
                                    // Tablo Başlık Satırı
                                    Row(
                                        modifier = Modifier
                                            .width(1280.dp)
                                            .background(TourOSColors.Primary)
                                            .padding(horizontal = TourOSSpacing.medium, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(AppLanguageManager.translate("TO PNR"), modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("Müşteri Adı"), modifier = Modifier.weight(1.3f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("Paket Tur Kodu"), modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("TO Acente Adı"), modifier = Modifier.weight(1.3f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("Tur Satış Fiyatı"), modifier = Modifier.weight(1.1f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("Acenta %"), modifier = Modifier.weight(0.8f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("Tur Satışı (Maliyet)"), modifier = Modifier.weight(1.2f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("TO Ödemesi"), modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                        Text(AppLanguageManager.translate("Bakiye"), modifier = Modifier.weight(1.1f), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold)
                                    }

                                    if (state.filteredItems.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(AppLanguageManager.translate("Filtrelere uygun Tur Operatörü PNR kaydı bulunamadı."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
                                        }
                                    } else {
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(state.filteredItems) { item ->
                                                OperatorLedgerRowItem(item = item)
                                                HorizontalDivider(color = TourOSColors.Border.copy(alpha = 0.5f))
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
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.SemiBold))
                Text(icon, fontSize = 16.sp)
            }
            Text(value, style = TourOSTypography.TitleLarge.copy(color = contentColor, fontWeight = FontWeight.Bold))
            if (subtitle != null) {
                Text(subtitle, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 11.sp))
            }
        }
    }
}

@Composable
private fun OperatorLedgerRowItem(item: OperatorLedgerItem) {
    Row(
        modifier = Modifier
            .width(1280.dp)
            .padding(horizontal = TourOSSpacing.medium, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. TO PNR Kodu
        Box(modifier = Modifier.weight(1.0f)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (item.operatorPnrCode != "-") Color(0xFFECFDF5) else Color(0xFFF1F5F9)
            ) {
                Text(
                    text = item.operatorPnrCode,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    style = TourOSTypography.Label.copy(
                        color = if (item.operatorPnrCode != "-") Color(0xFF059669) else TourOSColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // 2. Müşteri Adı
        Text(
            text = item.customerName,
            modifier = Modifier.weight(1.3f),
            style = TourOSTypography.Label.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        )

        // 3. Paket Tur Kodu
        Text(
            text = item.bookingCode,
            modifier = Modifier.weight(1.0f),
            style = TourOSTypography.Label.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )

        // 4. TO Acente Adı
        Text(
            text = item.operatorName,
            modifier = Modifier.weight(1.3f),
            style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        )

        // 5. Tur Satış Fiyatı (Paket Satış Tutarı)
        Text(
            text = "${formatCurrency(item.totalSales)} ₺",
            modifier = Modifier.weight(1.1f),
            style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )

        // 6. Acenta % (Komisyon Oranı)
        Surface(
            modifier = Modifier.weight(0.8f),
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFEFF6FF)
        ) {
            Text(
                text = "%${formatCurrency(item.commissionRate)}",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                style = TourOSTypography.Label.copy(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
        }

        // 7. Tur Satışı (Maliyet)
        Text(
            text = "${formatCurrency(item.tourCost)} ₺",
            modifier = Modifier.weight(1.2f),
            style = TourOSTypography.Label.copy(color = Color(0xFF0F5A56), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )

        // 8. TO Ödemesi
        Text(
            text = "${formatCurrency(item.totalPaid)} ₺",
            modifier = Modifier.weight(1.0f),
            style = TourOSTypography.Label.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )

        // 9. Bakiye
        val isZeroBalance = item.balance <= 0.0
        Text(
            text = if (isZeroBalance) "0 ₺ (${AppLanguageManager.translate("Kapandı")})" else "${formatCurrency(item.balance)} ₺",
            modifier = Modifier.weight(1.1f),
            style = TourOSTypography.Label.copy(
                color = if (isZeroBalance) Color(0xFF64748B) else Color(0xFFD97706),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}

private fun formatCurrency(value: Double): String {
    val longVal = value.toLong()
    return if (value % 1.0 == 0.0) {
        longVal.toString()
    } else {
        val whole = (value.toInt()).toString()
        val decimal = ((value - value.toInt()) * 100).toInt()
        "$whole.${if (decimal < 10) "0$decimal" else decimal}"
    }
}

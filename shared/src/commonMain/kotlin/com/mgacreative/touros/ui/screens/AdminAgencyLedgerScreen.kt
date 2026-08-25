package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

/**
 * SaaS Admin - Acente Cari, Otomatik Borçlandırma ve Kilit Yönetimi Masası.
 */
@Composable
fun AdminAgencyLedgerScreen(
    viewModel: AdminAgencyLedgerViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val agencies by viewModel.agencies.collectAsState()
    val selectedAgency by viewModel.selectedAgency.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isAutoLockEnabled by viewModel.isAutoLockEnabled.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showOnlyDebtors by remember { mutableStateOf(false) }

    var showPaymentModal by remember { mutableStateOf(false) }
    var showDebitModal by remember { mutableStateOf(false) }

    // Dialog state'leri
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("BANK_TRANSFER") }
    var paymentRefNo by remember { mutableStateOf("") }
    var paymentDesc by remember { mutableStateOf("") }

    var debitAmountText by remember { mutableStateOf("") }
    var debitCategory by remember { mutableStateOf("MONTHLY_SUBSCRIPTION") }
    var debitRefNo by remember { mutableStateOf("") }
    var debitDesc by remember { mutableStateOf("") }

    val filteredAgencies = remember(agencies, searchQuery, showOnlyDebtors) {
        agencies.filter { agency ->
            val matchQuery = searchQuery.isBlank() || 
                agency.name.contains(searchQuery, ignoreCase = true) ||
                (agency.operator_code ?: "").contains(searchQuery, ignoreCase = true)
            val matchDebtor = !showOnlyDebtors || agency.current_balance > 0
            matchQuery && matchDebtor
        }
    }

    val totalDebt = remember(agencies) { agencies.sumOf { it.current_balance.coerceAtLeast(0.0) } }
    val debtorCount = remember(agencies) { agencies.count { it.current_balance > 0 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
            .padding(TourOSSpacing.large),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
    ) {
        // ── ÜST BAŞLIK & GLOBAL KİLİT KONTROL ANAHTARI ─────────────────────────
        TourOSCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(TourOSSpacing.large), verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Acente Cari & Otomatik Borçlandırma Masası",
                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        )
                        Text(
                            text = "Acente aylık sabit abonelikleri, cari ekstreleri, tahsilat girişi ve otomatik kilit yönetimi.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                        TourOSButton(
                            text = "Aylık Borçlandırmayı Çalıştır",
                            onClick = { viewModel.triggerMonthlyBilling() },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                        TourOSButton(
                            text = "Yenile",
                            onClick = { viewModel.loadData() },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                    }
                }

                Divider(color = TourOSColors.Border)

                // GLOBAL AÇ/KAPA KİLİT ANAHTARI
                Surface(
                    color = if (isAutoLockEnabled) TourOSColors.SecondaryContainer.copy(alpha = 0.4f) else TourOSColors.SuccessContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                    border = BorderStroke(1.dp, if (isAutoLockEnabled) TourOSColors.Secondary.copy(alpha = 0.5f) else TourOSColors.Success.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAutoLockEnabled) "Otomatik Borç Kilit Modu: AKTİF (Canlıda)" else "Otomatik Borç Kilit Modu: PASİF (İzleme / Test Modu)",
                                style = TourOSTypography.TitleMedium.copy(
                                    color = if (isAutoLockEnabled) TourOSColors.Secondary else TourOSColors.Success,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isAutoLockEnabled) 
                                    "Borcu olan (bakiye > 0) tüm acentelerin B2B arama ve sorgu erişimi otomatik durdurulur."
                                else 
                                    "Acenteler borçlandırılır ve ekstreler işlenir; ancak hiçbir acente kilitlenmez (Güvenli test modu).",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                            )
                        }

                        Switch(
                            checked = isAutoLockEnabled,
                            onCheckedChange = { viewModel.toggleAutoLock(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TourOSColors.Secondary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = TourOSColors.Success
                            )
                        )
                    }
                }
            }
        }

        // BİLDİRİM BANNER'I
        if (!notificationMessage.isNullOrBlank()) {
            Surface(
                color = TourOSColors.PrimaryContainer,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(notificationMessage ?: "", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold))
                    Text("✕", modifier = Modifier.clickable { viewModel.clearNotification() }.padding(4.dp), color = TourOSColors.Primary)
                }
            }
        }

        // ── KPI METRİK KARTLARI ────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.SurfaceVariant.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                Column {
                    Text("Toplam Alacak (Borç)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("${totalDebt.toInt()} ₺", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Secondary))
                }
            }
            TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.SurfaceVariant.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                Column {
                    Text("Borçlu Acente Sayısı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("$debtorCount / ${agencies.size}", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.SurfaceVariant.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                Column {
                    Text("Aylık Ortalama Abonelik", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("2.500 ₺", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            TourOSCard(modifier = Modifier.weight(1f), backgroundColor = TourOSColors.SurfaceVariant.copy(alpha = 0.5f), contentPadding = TourOSSpacing.medium) {
                Column {
                    Text("Kilit Kuralı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(if (isAutoLockEnabled) "AKTİF KİLİT" else "PASİF (AÇIK)", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = if (isAutoLockEnabled) TourOSColors.Secondary else TourOSColors.Success))
                }
            }
        }

        // ── İKİ SÜTUNLU MASTER-DETAIL DÜZENİ ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // SOL SÜTUN (%42): ACENTE LİSTESİ & BAKİYE
            TourOSCard(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
                Column(
                    modifier = Modifier.padding(TourOSSpacing.medium).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Acenteler (${filteredAgencies.size})", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showOnlyDebtors, onCheckedChange = { showOnlyDebtors = it })
                            Text("Sadece Borçlular", style = TourOSTypography.Caption)
                        }
                    }

                    TourOSTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Acente adı veya kodu ara...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAgencies) { agency ->
                            val isSelected = (selectedAgency?.id == agency.id)
                            val hasDebt = agency.current_balance > 0

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) TourOSColors.Primary else TourOSColors.Border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectAgency(agency) },
                                color = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.25f) else TourOSColors.Surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(agency.name, style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(agency.operator_code ?: "KODSUZ", style = TourOSTypography.Caption.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                                            Text("• Abonelik: ${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(agency.monthly_subscription_fee, decimals = false)} ₺", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (hasDebt) "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(agency.current_balance, decimals = false)} ₺ Borç" else "0 ₺ Borçsuz",
                                            style = TourOSTypography.BodyMedium.copy(
                                                color = if (hasDebt) TourOSColors.Secondary else TourOSColors.Success,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        TourOSStatusBadge(
                                            text = if (!hasDebt) "Aktif" else if (isAutoLockEnabled) "Kilitli" else "Borçlu",
                                            backgroundColor = if (!hasDebt) TourOSColors.SuccessContainer else if (isAutoLockEnabled) TourOSColors.SecondaryContainer else TourOSColors.PrimaryContainer,
                                            textColor = if (!hasDebt) TourOSColors.Success else if (isAutoLockEnabled) TourOSColors.Secondary else TourOSColors.Primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SAĞ SÜTUN (%58): SEÇİLİ ACENTENİN CARİ EKSTRESİ
            TourOSCard(modifier = Modifier.weight(0.58f).fillMaxHeight()) {
                val current = selectedAgency
                if (current == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Cari hareketlerini görmek için sol listeden bir acente seçiniz.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(TourOSSpacing.large).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // Acente Başlığı & Eylem Butonları
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(current.name, style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                                Text("Acente Kodu: ${current.operator_code ?: "-"} | Aylık Sabit Ücret: ${current.monthly_subscription_fee.toInt()} ₺", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TourOSButton(
                                    text = "Tahsilat Ekle",
                                    onClick = { 
                                        paymentAmountText = if (current.current_balance > 0) current.current_balance.toInt().toString() else "2500"
                                        paymentRefNo = "DKN-${(1000..9999).random()}"
                                        paymentDesc = "${current.name} Havale/EFT Tahsilatı"
                                        showPaymentModal = true 
                                    },
                                    variant = TourOSButtonVariant.PRIMARY
                                )
                                TourOSButton(
                                    text = "Borç Ekle",
                                    onClick = {
                                        debitAmountText = current.monthly_subscription_fee.toInt().toString()
                                        debitRefNo = "FTR-${(1000..9999).random()}"
                                        debitDesc = "Ekstra Sorgu / Hizmet Bedeli"
                                        showDebitModal = true
                                    },
                                    variant = TourOSButtonVariant.SECONDARY
                                )
                            }
                        }

                        // Güncel Bakiye Kutusu
                        Surface(
                            color = if (current.current_balance > 0) TourOSColors.SecondaryContainer.copy(alpha = 0.35f) else TourOSColors.SuccessContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Güncel Net Bakiye:",
                                    style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(current.current_balance, decimals = false)} ₺ ${if (current.current_balance > 0) "(BORÇLU)" else "(ÖDENDİ)"}",
                                    style = TourOSTypography.TitleLarge.copy(
                                        color = if (current.current_balance > 0) TourOSColors.Secondary else TourOSColors.Success,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = TourOSColors.Border)

                        Text("Cari Hareketler & Hesap Ekstresi", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))

                        // Ekstre Tablosu Başlığı
                        Surface(color = TourOSColors.SurfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth()) {
                                Text("Tarih", modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
                                Text("İşlem & Açıklama", modifier = Modifier.weight(2.0f), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
                                Text("Borç (+)", modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                                Text("Tahsilat (-)", modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                                Text("Bakiye", modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                            }
                        }

                        // Ekstre Satırları
                        if (transactions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Kayıtlı cari hareket bulunmamaktadır.", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                itemsIndexed(transactions) { idx, tx ->
                                    val isDebit = tx.transaction_type == "DEBIT"
                                    Surface(
                                        color = if (idx % 2 == 0) TourOSColors.SurfaceVariant.copy(alpha = 0.25f) else TourOSColors.Surface,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(tx.transaction_date?.take(10) ?: "2026-08-01", modifier = Modifier.weight(1.0f), style = TourOSTypography.Caption)
                                            Column(modifier = Modifier.weight(2.0f)) {
                                                Text(tx.description, style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("Ref: ${tx.reference_no ?: "-"} • ${tx.created_by ?: "Admin"}", style = TourOSTypography.Caption.copy(fontSize = 10.sp, color = TourOSColors.TextSecondary))
                                            }
                                            Text(if (isDebit) "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(tx.amount, decimals = false)} ₺" else "-", modifier = Modifier.weight(1.0f), style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Secondary, fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                                            Text(if (!isDebit) "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(tx.amount, decimals = false)} ₺" else "-", modifier = Modifier.weight(1.0f), style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                                            Text("${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(tx.balance_after, decimals = false)} ₺", modifier = Modifier.weight(1.0f), style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
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

    // ── TAHSİLAT GİRİŞİ MODALI ───────────────────────────────────────────────
    if (showPaymentModal && selectedAgency != null) {
        val agency = selectedAgency!!
        AlertDialog(
            onDismissRequest = { showPaymentModal = false },
            title = { Text("Tahsilat Girişi (${agency.name})", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text("Bu işlem acentenin borcunu düşürecek ve borç sıfırlandığında sistemi otomatik açacaktır.", style = TourOSTypography.Caption)
                    TourOSTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = "Tahsil Edilen Tutar (₺)"
                    )
                    TourOSTextField(
                        value = paymentRefNo,
                        onValueChange = { paymentRefNo = it },
                        label = "Dekont / İşlem No"
                    )
                    TourOSTextField(
                        value = paymentDesc,
                        onValueChange = { paymentDesc = it },
                        label = "Açıklama"
                    )
                }
            },
            confirmButton = {
                TourOSButton(
                    text = "Tahsilatı İşle & Borcu Düş",
                    onClick = {
                        val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.recordPayment(
                                companyId = agency.id,
                                amount = amount,
                                paymentMethod = paymentMethod,
                                referenceNo = paymentRefNo,
                                description = paymentDesc
                            )
                            showPaymentModal = false
                        }
                    }
                )
            },
            dismissButton = {
                TourOSButton(text = "İptal", onClick = { showPaymentModal = false }, variant = TourOSButtonVariant.SECONDARY)
            }
        )
    }

    // ── MANUEL BORÇ / FATURA MODALI ──────────────────────────────────────────
    if (showDebitModal && selectedAgency != null) {
        val agency = selectedAgency!!
        AlertDialog(
            onDismissRequest = { showDebitModal = false },
            title = { Text("Borç / Fatura Ekle (${agency.name})", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text("Acentenin cari hesabına borç tahakkuk ettirilir.", style = TourOSTypography.Caption)
                    TourOSTextField(
                        value = debitAmountText,
                        onValueChange = { debitAmountText = it },
                        label = "Borç Tutarı (₺)"
                    )
                    TourOSTextField(
                        value = debitRefNo,
                        onValueChange = { debitRefNo = it },
                        label = "Fatura / Referans No"
                    )
                    TourOSTextField(
                        value = debitDesc,
                        onValueChange = { debitDesc = it },
                        label = "Açıklama"
                    )
                }
            },
            confirmButton = {
                TourOSButton(
                    text = "Borcu Kaydet",
                    onClick = {
                        val amount = debitAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.recordDebit(
                                companyId = agency.id,
                                amount = amount,
                                category = debitCategory,
                                referenceNo = debitRefNo,
                                description = debitDesc
                            )
                            showDebitModal = false
                        }
                    }
                )
            },
            dismissButton = {
                TourOSButton(text = "İptal", onClick = { showDebitModal = false }, variant = TourOSButtonVariant.SECONDARY)
            }
        )
    }
}

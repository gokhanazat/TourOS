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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.AccountTransactionDetail
import com.mgacreative.touros.domain.model.CurrentAccountItem
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.CurrentAccountUiState
import com.mgacreative.touros.ui.viewmodel.CurrentAccountViewModel

/**
 * Cari Hesaplar & Mutabakat Ekranı — TourOS Canlı Veri & Benzersiz Cari Kodu Destekli Sürüm
 *
 * Müşteri, Acente ve Tedarikçilerin cari hesap borç/alacak takibi, ekstre dökümü.
 * Benzersiz Cari Kodu (Örn: CAR-2026-001) ve TC/VKN No ile isim çakışmaları tamamen önlenmiştir.
 */
@Composable
fun CurrentAccountScreen(
    viewModel: CurrentAccountViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    var selectedAccountForDetail by remember { mutableStateOf<CurrentAccountItem?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTransactionType by remember { mutableStateOf("Tümü") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Hesaplar & Mutabakat"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri, acente ve tedarikçi bakiye dökümleri ve ekstreler"),
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is CurrentAccountUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }

            is CurrentAccountUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Hata: ${state.message}",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                    )
                }
            }

            is CurrentAccountUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── 1. Üst Bakiye Özeti Şeridi (3 Kompakt KPI Kartı) ──
                        TopBalanceSummarySection(
                            totalCustomerReceivables = state.totalCustomerReceivables,
                            totalSupplierPayables = state.totalSupplierPayables,
                            netBalance = state.netBalance
                        )

                        // ── 2. Kompakt Arama & Filtre Araç Çubuğu (Tek Satır) ──
                        SearchAndFilterBar(
                            searchQuery = state.searchQuery,
                            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            selectedEntityType = state.selectedEntityType,
                            onEntityTypeSelected = { viewModel.setFilter(it) },
                            totalCount = state.accounts.size
                        )

                        // ── 3. Cari Hesap ERP Veri Tablosu ──
                        if (state.accounts.isEmpty()) {
                            TourOSCard(
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                contentPadding = TourOSSpacing.large
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Filtreye uygun veya kayıtlı cari hesap bulunamadı."),
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            if (isExpanded) {
                                // 🖥️ Desktop / Web ERP Veri Tablosu
                                CurrentAccountsDataTable(
                                    accounts = state.accounts,
                                    onStatementClick = { viewModel.selectAccountForStatement(it) }
                                )
                            } else {
                                // 📱 Mobil Kompakt Satır Listesi
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.accounts) { account ->
                                        CurrentAccountMobileCard(
                                            account = account,
                                            onStatementClick = { viewModel.selectAccountForStatement(account) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 4. Cari Hareket Dökümü Ekstre Modalı ──────────────────
                    if (state.selectedAccountForStatement != null) {
                        AccountStatementModal(
                            account = state.selectedAccountForStatement!!,
                            details = state.statementDetails,
                            isExpanded = isExpanded,
                            onDismiss = { viewModel.selectAccountForStatement(null) }
                        )
                    }
                }
            }
        }
    }
}

// ─── 1. Üst Bakiye Özeti (3 Kompakt Yan Yana KPI Kartı) ─────────────────────────

@Composable
private fun TopBalanceSummarySection(
    totalCustomerReceivables: Double,
    totalSupplierPayables: Double,
    netBalance: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // 1. Müşteri Alacakları
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0FDF4), // Hafif Yeşil
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "📈 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri Alacakları"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "₺ ${formatMoney(totalCustomerReceivables)}",
                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    )
                }
            }
        }

        // 2. Tedarikçi Borçları
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFF1F2), // Hafif Kırmızı
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "📉 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tedarikçi Borçları"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF9F1239), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "₺ ${formatMoney(totalSupplierPayables)}",
                        style = TourOSTypography.TitleMedium.copy(color = Color(0xFFBE123C), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    )
                }
            }
        }

        // 3. Net Genel Bakiye
        val isNetPositive = netBalance >= 0
        Surface(
            modifier = Modifier.weight(1.15f),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0A2540), // Kurumsal Lacivert Vurgu
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "⚖️ " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("NET GENEL BAKİYE"),
                        style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "₺ ${formatMoney(netBalance)}",
                        style = TourOSTypography.TitleMedium.copy(
                            color = if (isNetPositive) Color(0xFF34D399) else Color(0xFFF87171),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E3A5F)
                ) {
                    Text(
                        if (isNetPositive) "Alacaklı" else "Borçlu",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = TourOSTypography.Caption.copy(
                            color = if (isNetPositive) Color(0xFF34D399) else Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// ─── 2. Kompakt Tek Satır Arama & Filtre Çubuğu ───────────────────────────────

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedEntityType: String?,
    onEntityTypeSelected: (String?) -> Unit,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hızlı Arama Kutusu
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Adı, Cari Kodu (CAR-...), TO PNR veya VKN/TC ile ara..."), fontSize = 12.sp, color = Color(0xFF94A3B8)) },
            leadingIcon = { Text("🔍", fontSize = 14.sp) },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Text("✕", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                }
            } else null,
            singleLine = true,
            textStyle = TourOSTypography.BodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = TourOSColors.Divider,
                focusedBorderColor = TourOSColors.Primary
            )
        )

        // Filtre Çipleri
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                null to com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tümü"),
                "customer" to "👤 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteriler"),
                "agency" to "🏢 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acenteler"),
                "supplier" to "🏨 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tedarikçiler")
            ).forEach { (typeKey, typeLabel) ->
                val isSelected = selectedEntityType == typeKey
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) TourOSColors.Primary else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) TourOSColors.Primary else TourOSColors.Divider),
                    modifier = Modifier.clickable { onEntityTypeSelected(typeKey) }
                ) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = TourOSTypography.Caption.copy(
                            color = if (isSelected) Color.White else TourOSColors.TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Sayı Rozeti
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Text(
                    text = "Toplam: $totalCount",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                )
            }
        }
    }
}

// ─── 3. Desktop / Web Yüksek Yoğunluklu ERP Veri Tablosu ──────────────────────

@Composable
private fun CurrentAccountsDataTable(
    accounts: List<CurrentAccountItem>,
    onStatementClick: (CurrentAccountItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Divider),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── TABLO BAŞLIK SATIRI ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Kodu & Türü"), modifier = Modifier.weight(1.3f), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Ünvanı / İletişim"), modifier = Modifier.weight(2.0f), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text("VKN / TC", modifier = Modifier.weight(1.1f), style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Borç"), modifier = Modifier.weight(1.1f), textAlign = TextAlign.End, style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Toplam Alacak"), modifier = Modifier.weight(1.1f), textAlign = TextAlign.End, style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Net Bakiye"), modifier = Modifier.weight(1.3f), textAlign = TextAlign.End, style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Son İşlem"), modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center, style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İşlem"), modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center, style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
            }

            // ── TABLO VERİ SATIRLARI ──
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts) { account ->
                    val isDebit = account.balance > 0
                    val typeTitle = when (account.entityType) {
                        "customer" -> "👤 Müşteri"
                        "agency" -> "🏢 Acente"
                        "supplier" -> "🏨 Tedarikçi"
                        else -> "📌 Cari"
                    }
                    val codeText = account.accountCode.ifBlank { "CAR-${account.entityId.take(6).uppercase()}" }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatementClick(account) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Cari Kodu & Türü
                        Row(
                            modifier = Modifier.weight(1.3f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = codeText,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                            }
                            Text(typeTitle, style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp))
                        }

                        // 2. Cari Ünvanı / İletişim
                        Column(modifier = Modifier.weight(2.0f)) {
                            Text(
                                text = account.entityName,
                                style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!account.phone.isNullOrBlank() || !account.email.isNullOrBlank()) {
                                Text(
                                    text = "${account.phone ?: ""} ${if (!account.phone.isNullOrBlank() && !account.email.isNullOrBlank()) "·" else ""} ${account.email ?: ""}",
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 10.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 3. VKN / TC
                        Text(
                            text = (account.taxNo ?: "").ifBlank { "—" },
                            modifier = Modifier.weight(1.1f),
                            style = TourOSTypography.Caption.copy(color = Color(0xFF475569), fontSize = 11.sp)
                        )

                        // 4. Toplam Borç
                        Text(
                            text = "₺ ${formatMoney(account.totalDebit)}",
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.End,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontSize = 11.sp)
                        )

                        // 5. Toplam Alacak
                        Text(
                            text = "₺ ${formatMoney(account.totalCredit)}",
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.End,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                        )

                        // 6. Net Bakiye
                        Text(
                            text = (if (isDebit) "+ " else "") + "₺ ${formatMoney(account.balance)} ${account.currency}",
                            modifier = Modifier.weight(1.3f),
                            textAlign = TextAlign.End,
                            style = TourOSTypography.Caption.copy(
                                color = if (isDebit) Color(0xFF16A34A) else if (account.balance < 0) Color(0xFFDC2626) else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )

                        // 7. Son İşlem
                        Text(
                            text = account.lastTransactionDate.ifBlank { "—" },
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.Center,
                            style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp)
                        )

                        // 8. İşlem (Ekstre Butonu)
                        Box(
                            modifier = Modifier.weight(0.9f),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0284C7),
                                modifier = Modifier.clickable { onStatementClick(account) }
                            ) {
                                Text(
                                    text = "📄 Ekstre",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }
            }
        }
    }
}

// ─── 4. Mobil Kompakt Mini Kart ───────────────────────────────────────────────

@Composable
private fun CurrentAccountMobileCard(
    account: CurrentAccountItem,
    onStatementClick: () -> Unit
) {
    val isDebit = account.balance > 0
    val codeText = account.accountCode.ifBlank { "CAR-${account.entityId.take(6).uppercase()}" }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onStatementClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Divider)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(codeText, style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    Text(account.entityName, style = TourOSTypography.Caption.copy(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp), maxLines = 1)
                }
                Text("Borç/Alacak: ₺ ${formatMoney(account.totalDebit)} / ₺ ${formatMoney(account.totalCredit)}", style = TourOSTypography.Caption.copy(color = Color(0xFF64748B), fontSize = 10.sp))
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "₺ ${formatMoney(account.balance)} ${account.currency}",
                    style = TourOSTypography.Caption.copy(color = if (isDebit) Color(0xFF16A34A) else Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0284C7),
                    modifier = Modifier.clickable { onStatementClick() }
                ) {
                    Text("📄 Ekstre", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = TourOSTypography.Caption.copy(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

// ─── Hareket Dökümü Ekstre Modalı ─────────────────────────────────────────────

@Composable
private fun AccountStatementModal(
    account: CurrentAccountItem,
    details: List<AccountTransactionDetail>,
    isExpanded: Boolean,
    onDismiss: () -> Unit
) {
    val codeText = account.accountCode.ifBlank { "CAR-${account.entityId.take(6).uppercase()}" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Hareket Dökümü & Ekstre")}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${account.entityName} [$codeText]",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Güncel Bakiye:"), style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
                        Text(
                            "₺ ${formatMoney(account.balance)} ${account.currency}",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Primary)
                                .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
                        ) {
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tarih"), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Açıklama / Ref"), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Borç (TL)"), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Alacak (TL)"), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Bakiye (TL)"), style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                        }

                        if (details.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hareket detayı bulunamadı."), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(details) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.date, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(0.9f))
                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate(item.description), style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
                                            if (!item.operatorPnrCode.isNullOrBlank()) {
                                                Text("✈️ TO PNR: ${item.operatorPnrCode}", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold))
                                            }
                                        }
                                        Text(formatMoney(item.debit), style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(1.0f))
                                        Text(formatMoney(item.credit), style = TourOSTypography.Caption.copy(color = TourOSColors.Success), modifier = Modifier.weight(1.0f))
                                        Text(formatMoney(item.balance), style = TourOSTypography.Label.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                                    }
                                    HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TourOSButton(
                text = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kapat"),
                onClick = onDismiss,
                variant = TourOSButtonVariant.PRIMARY
            )
        }
    )
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

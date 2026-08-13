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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
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
                        // ── 1. Üst Bakiye Özeti Kartı ─────────────────────────
                        TopBalanceSummarySection(
                            totalCustomerReceivables = state.totalCustomerReceivables,
                            totalSupplierPayables = state.totalSupplierPayables,
                            netBalance = state.netBalance
                        )

                        // ── 2. Arama & Filtre Çubuğu ─────────────────────────
                        SearchAndFilterBar(
                            searchQuery = state.searchQuery,
                            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            selectedEntityType = state.selectedEntityType,
                            onEntityTypeSelected = { viewModel.setFilter(it) }
                        )

                        // ── 3. Cari Hesap Listesi ──────────────────────────────
                        Text(
                            "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Hesap Listesi")} (${state.accounts.size})",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )

                        if (state.accounts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Filtreye uygun veya veritabanında kaydedilmiş cari hesap bulunamadı."),
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                items(state.accounts) { account ->
                                    CurrentAccountCard(
                                        account = account,
                                        onStatementClick = { viewModel.selectAccountForStatement(account) }
                                    )
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

// ─── Üst Bakiye Özeti Kartı ───────────────────────────────────────────────────

@Composable
private fun TopBalanceSummarySection(
    totalCustomerReceivables: Double,
    totalSupplierPayables: Double,
    netBalance: Double
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            Text(
                com.mgacreative.touros.ui.localization.AppLanguageManager.translate("NET GENEL BAKİYE"),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "₺ ${formatMoney(netBalance)}",
                style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📈 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri Alacakları")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
                    Text(
                        "₺ ${formatMoney(totalCustomerReceivables)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📉 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tedarikçi Borçları")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), fontWeight = FontWeight.Bold)
                    Text(
                        "₺ ${formatMoney(totalSupplierPayables)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─── Arama & Filtre Çubuğu ────────────────────────────────────────────────────

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedEntityType: String?,
    onEntityTypeSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
        TourOSTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Arama"),
            placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari Adı, Cari Kodu (CAR-...), TO PNR (PEGAS-...) veya Vergi/TC No ile ara..."),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            FilterChip(
                selected = selectedEntityType == null,
                onClick = { onEntityTypeSelected(null) },
                label = { Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tüm Cariler"), style = TourOSTypography.Caption) }
            )
            FilterChip(
                selected = selectedEntityType == "customer",
                onClick = { onEntityTypeSelected("customer") },
                label = { Text("👤 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteriler")}", style = TourOSTypography.Caption) }
            )
            FilterChip(
                selected = selectedEntityType == "agency",
                onClick = { onEntityTypeSelected("agency") },
                label = { Text("🏢 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acenteler")}", style = TourOSTypography.Caption) }
            )
            FilterChip(
                selected = selectedEntityType == "supplier",
                onClick = { onEntityTypeSelected("supplier") },
                label = { Text("🏨 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tedarikçiler")}", style = TourOSTypography.Caption) }
            )
        }
    }
}

// ─── Cari Hesap Kartı (Cari Kodu & Vergi No Rozet Destekli) ───────────────────

@Composable
private fun CurrentAccountCard(
    account: CurrentAccountItem,
    onStatementClick: () -> Unit
) {
    val typeTitle = when (account.entityType) {
        "customer" -> com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri")
        "agency" -> com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Acente")
        "supplier" -> com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tedarikçi")
        else -> com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Cari")
    }

    val typeIcon = when (account.entityType) {
        "customer" -> "👤"
        "agency" -> "🏢"
        "supplier" -> "🏨"
        else -> "📌"
    }

    val codeText = account.accountCode.ifBlank { "CAR-${account.entityId.take(6).uppercase()}" }

    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.PrimaryContainer)
                            .padding(horizontal = TourOSSpacing.small, vertical = 2.dp)
                    ) {
                        Text(
                            "$typeIcon $typeTitle • $codeText",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!account.taxNo.isNullOrBlank()) {
                        Text(
                            "VKN/TC: ${account.taxNo}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                Text(
                    "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Son İşlem")}: ${account.lastTransactionDate.ifBlank { "—" }}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            Text(
                account.entityName,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                fontWeight = FontWeight.Bold
            )

            if (!account.phone.isNullOrBlank() || !account.email.isNullOrBlank()) {
                Text(
                    "📞 ${account.phone ?: "—"}  ·  ✉️ ${account.email ?: "—"}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Borç Toplamı / Alacak Toplamı"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(account.totalDebit)} / ₺ ${formatMoney(account.totalCredit)}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Net Bakiye"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(account.balance)} ${account.currency}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            TourOSButton(
                text = "📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hareket Dökümü & Ekstreyi Aç")}",
                onClick = onStatementClick,
                variant = TourOSButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
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

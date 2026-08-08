package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

private data class AccountTypeFilter(val key: String?, val label: String, val icon: String)

private val accountTypeFilters = listOf(
    AccountTypeFilter(null, "Tüm Cariler", "📋"),
    AccountTypeFilter("customer", "Müşteriler", "👤"),
    AccountTypeFilter("agency", "Acenteler", "🏢"),
    AccountTypeFilter("supplier", "Tedarikçiler", "🚚")
)

/**
 * Cari Hesaplar & Ekstre Dökümü — TourOS 0.3
 *
 * Üstte Bakiye Özeti (Büyük rakam, Primary renkte)
 * Altta Hareket Dökümü Tablosu (Borç ve Alacak sütunları başlıkla net şekilde ayrılmış).
 */
@Composable
fun CurrentAccountScreen(
    viewModel: CurrentAccountViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Cari Hesaplar & Ekstre",
                subtitle = "Müşteri, acente ve tedarikçi bakiye dökümleri",
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
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is CurrentAccountUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is CurrentAccountUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // ── 1. Üstte Bakiye Özeti (Büyük Rakam, Primary Renkte) ────
                        item {
                            TopBalanceSummarySection(
                                totalCustomerReceivables = state.totalCustomerReceivables,
                                totalSupplierPayables = state.totalSupplierPayables,
                                netBalance = state.netBalance
                            )
                        }

                        // ── 2. Arama ve Filtreler ──────────────────────────────
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                TourOSTextField(
                                    value = state.searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                                    label = "Cari Arama",
                                    placeholder = "Cari unvan veya isim ara...",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    items(accountTypeFilters) { filter ->
                                        FilterChip(
                                            selected = state.selectedEntityType == filter.key,
                                            onClick = { viewModel.setFilter(filter.key) },
                                            label = {
                                                Text("${filter.icon} ${filter.label}", style = TourOSTypography.Caption)
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TourOSColors.PrimaryContainer,
                                                selectedLabelColor = TourOSColors.Primary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── 3. Cari Hesap Listesi ──────────────────────────────
                        item {
                            Text(
                                "📋 Cari Hesap Listesi (${state.accounts.size})",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )
                        }

                        if (state.accounts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Filtreye uygun cari hesap bulunamadı.",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(state.accounts) { account ->
                                CurrentAccountCard(
                                    account = account,
                                    onStatementClick = { viewModel.selectAccountForStatement(account) }
                                )
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
                "NET GENEL BAKİYE",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            // BÜYÜK RAKAM (PRIMARY RENKTE)
            Text(
                text = "₺ ${formatMoney(netBalance)}",
                style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            // 2'li Alt Detay (Müşteri Alacakları vs Tedarikçi Borçları)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📈 Müşteri Alacakları", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(totalCustomerReceivables)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📉 Tedarikçi Borçları", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(totalSupplierPayables)}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    )
                }
            }
        }
    }
}

// ─── Cari Hesap Özeti Kartı ───────────────────────────────────────────────────

@Composable
private fun CurrentAccountCard(
    account: CurrentAccountItem,
    onStatementClick: () -> Unit
) {
    val (typeIcon, typeTitle) = when (account.entityType) {
        "customer" -> "👤" to "Müşteri"
        "agency"   -> "🏢" to "Acente"
        "supplier" -> "🚚" to "Tedarikçi"
        else       -> "📌" to "Cari"
    }

    TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = TourOSSpacing.large) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer)
                        .padding(horizontal = TourOSSpacing.small, vertical = 3.dp)
                ) {
                    Text(
                        "$typeIcon $typeTitle",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                    )
                }

                Text(
                    "Son İşlem: ${account.lastTransactionDate}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            Text(
                account.entityName,
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
            )

            if (!account.phone.isNullOrBlank() || !account.email.isNullOrBlank()) {
                Text(
                    "📞 ${account.phone ?: "—"}  ·  ✉️ ${account.email ?: "—"}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Sütun Başlıklarıyla Net Bakiye Özeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Borç Toplamı / Alacak Toplamı", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(account.totalDebit)} / ₺ ${formatMoney(account.totalCredit)}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Bakiye", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        "₺ ${formatMoney(account.balance)} ${account.currency}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                }
            }

            TourOSButton(
                text = "📋 Hareket Dökümü & Ekstreyi Aç",
                onClick = onStatementClick,
                variant = TourOSButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Hareket Dökümü Ekstre Modalı (Borç / Alacak Sütunları Başlıkla Ayrılmış) ──

@Composable
private fun AccountStatementModal(
    account: CurrentAccountItem,
    details: List<AccountTransactionDetail>,
    isExpanded: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "📋 Cari Hareket Dökümü & Ekstre",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                )
                Text(
                    account.entityName,
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bakiye Özeti
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
                        Text("Güncel Bakiye:", style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary))
                        Text(
                            "₺ ${formatMoney(account.balance)} ${account.currency}",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }
                }

                // HAREKET DÖKÜMÜ TABLOSU (BORÇ VE ALACAK SÜTUNLARI NET BAŞLIKLARLA AYRILMIŞ)
                TourOSCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                    Column {
                        // Tablo Başlık Şeridi (Borç (TL) ve Alacak (TL) Net Ayrılmış)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TourOSColors.Primary)
                                .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small)
                        ) {
                            Text("Tarih", style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), modifier = Modifier.weight(0.9f))
                            Text("Açıklama / Ref", style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), modifier = Modifier.weight(1.5f))
                            Text("Borç (TL)", style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), modifier = Modifier.weight(1.0f))
                            Text("Alacak (TL)", style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), modifier = Modifier.weight(1.0f))
                            Text("Bakiye (TL)", style = TourOSTypography.Caption.copy(color = TourOSColors.OnPrimary), modifier = Modifier.weight(1.0f))
                        }

                        // Satırlar
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(details) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TourOSColors.Surface)
                                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.small),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tarih
                                    Text(item.date, style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.weight(0.9f))

                                    // Açıklama / Ref
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(item.description, style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                                        if (!item.referenceNo.isNullOrBlank()) {
                                            Text("Ref: ${item.referenceNo}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                        }
                                    }

                                    // BORÇ (TL) SÜTUNU
                                    Text(
                                        text = if (item.debit > 0) "₺ ${formatMoney(item.debit)}" else "—",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                        modifier = Modifier.weight(1.0f)
                                    )

                                    // ALACAK (TL) SÜTUNU
                                    Text(
                                        text = if (item.credit > 0) "₺ ${formatMoney(item.credit)}" else "—",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                        modifier = Modifier.weight(1.0f)
                                    )

                                    // BAKİYE (TL) SÜTUNU
                                    Text(
                                        text = "₺ ${formatMoney(item.balance)}",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                                        modifier = Modifier.weight(1.0f)
                                    )
                                }
                                HorizontalDivider(color = TourOSColors.Divider, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TourOSButton(
                text = "Kapat",
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

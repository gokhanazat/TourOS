package com.mgacreative.touros.ui.screens

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
import com.mgacreative.touros.domain.model.AccountTransactionDetail
import com.mgacreative.touros.domain.model.CurrentAccountItem
import com.mgacreative.touros.ui.viewmodel.CurrentAccountUiState
import com.mgacreative.touros.ui.viewmodel.CurrentAccountViewModel

/**
 * 3.1.4 Cari Hesap & Hareket Dökümü Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentAccountScreen(
    viewModel: CurrentAccountViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📑 Cari Hesaplar & Ekstre Dökümü", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (val state = uiState) {
                is CurrentAccountUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CurrentAccountUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is CurrentAccountUiState.Success -> {
                    // 1. Finansal Bakiye Kartları
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📈 Müşteri Alacakları", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.totalCustomerReceivables} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📉 Tedarikçi Borçları", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.totalSupplierPayables} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚖️ Net Bakiye", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.netBalance} TRY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (state.netBalance >= 0) Color(0xFF15803D) else MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // 2. Arama Çubuğu & Filtreler
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("🔍 Cari Unvan / Adı Ara...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = state.selectedEntityType == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("Tüm Cariler", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedEntityType == "customer",
                            onClick = { viewModel.setFilter("customer") },
                            label = { Text("👤 Müşteriler", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedEntityType == "agency",
                            onClick = { viewModel.setFilter("agency") },
                            label = { Text("🏢 Acenteler", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = state.selectedEntityType == "supplier",
                            onClick = { viewModel.setFilter("supplier") },
                            label = { Text("🚚 Tedarikçiler", fontSize = 11.sp) }
                        )
                    }

                    // 3. Cari Hesap Listesi
                    Text("📋 Cari Hesap Listesi (${state.accounts.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.accounts) { account ->
                            CurrentAccountCard(
                                account = account,
                                onStatementClick = { viewModel.selectAccountForStatement(account) }
                            )
                        }
                    }

                    // 4. Cari Ekstre Hareket Dökümü Modalı (Dialog)
                    if (state.selectedAccountForStatement != null) {
                        AccountStatementModal(
                            account = state.selectedAccountForStatement!!,
                            details = state.statementDetails,
                            onDismiss = { viewModel.selectAccountForStatement(null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentAccountCard(
    account: CurrentAccountItem,
    onStatementClick: () -> Unit
) {
    val (typeIcon, typeTitle) = when (account.entityType) {
        "customer" -> "👤" to "Müşteri"
        "agency" -> "🏢" to "Acente"
        "supplier" -> "🚚" to "Tedarikçi"
        else -> "📌" to "Cari"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("$typeIcon $typeTitle", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Text("Son İşlem: ${account.lastTransactionDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(account.entityName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (!account.phone.isNull_or_empty() || !account.email.isNull_or_empty()) {
                Text("📞 ${account.phone ?: "-"} | ✉️ ${account.email ?: "-"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Borç / Alacak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${account.totalDebit} / ${account.totalCredit} ${account.currency}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Bakiye", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${account.balance} ${account.currency}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (account.balance >= 0) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = onStatementClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text("📋 Ekstre & Hareket Dökümünü İncele", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun AccountStatementModal(
    account: CurrentAccountItem,
    details: List<AccountTransactionDetail>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("📋 Cari Hareket Ekstresi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(account.entityName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Bakiye:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${account.balance} ${account.currency}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (account.balance >= 0) Color(0xFF15803D) else MaterialTheme.colorScheme.error)
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    items(details) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Ref: ${item.referenceNo ?: "-"}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(item.description, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Borç: ${item.debit} TRY", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                    Text("Alacak: ${item.credit} TRY", fontSize = 10.sp, color = Color(0xFF15803D))
                                    Text("Bakiye: ${item.balance} TRY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

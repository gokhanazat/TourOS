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
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.ui.viewmodel.InvoiceManagementUiState
import com.mgacreative.touros.ui.viewmodel.InvoiceManagementViewModel

/**
 * 3.1.5 Fatura Oluşturma ve PDF Export Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceManagementScreen(
    viewModel: InvoiceManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧾 Fatura Yönetimi & PDF Export", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("➕ Yeni Fatura", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                is InvoiceManagementUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is InvoiceManagementUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hata: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is InvoiceManagementUiState.Success -> {
                    if (state.notificationMessage != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                            Text(state.notificationMessage, color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }

                    // Fatura Listesi
                    Text("🧾 Düzenlenen Satış Faturaları (${state.invoices.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.invoices) { invoice ->
                            InvoiceCard(
                                invoice = invoice,
                                onExportPdfClick = { viewModel.exportInvoicePdf(invoice) }
                            )
                        }
                    }

                    // Yeni Fatura Oluşturma Dialog
                    if (showCreateDialog) {
                        CreateInvoiceDialog(
                            onDismiss = { showCreateDialog = false },
                            onCreate = { no, name, taxNo, amount, notes ->
                                viewModel.createNewInvoice(no, name, taxNo, amount, notes)
                                showCreateDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    onExportPdfClick: () -> Unit
) {
    val (statusText, statusBg, statusFg) = when (invoice.status) {
        "issued" -> Triple("ISSUED (Kesildi)", Color(0xFFDBEAFE), Color(0xFF1E40AF))
        "paid" -> Triple("PAID (Ödendi)", Color(0xFFDCFCE7), Color(0xFF15803D))
        else -> Triple("DRAFT (Taslak)", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🧾 Fatura No: ${invoice.invoiceNo}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                    Text(statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusFg, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Text("👤 Müşteri: ${invoice.customerName} | Vergi/TC: ${invoice.customerTaxNo ?: "-"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Matrah (KDV Haric)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${invoice.subtotal} ${invoice.currency}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("KDV (%${invoice.taxRate})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${invoice.taxAmount} ${invoice.currency}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
                Column {
                    Text("Toplam Tutar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${invoice.totalAmount} ${invoice.currency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Button(
                onClick = onExportPdfClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📄 PDF Dışa Aktar & Belgelere Kaydet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onCreate: (invoiceNo: String, customerName: String, customerTaxNo: String?, totalAmount: Double, notes: String?) -> Unit
) {
    var invoiceNo by remember { mutableStateOf("INV-202608-003") }
    var customerName by remember { mutableStateOf("") }
    var customerTaxNo by remember { mutableStateOf("") }
    var totalAmountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ Yeni Fatura Oluştur", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = invoiceNo, onValueChange = { invoiceNo = it }, label = { Text("Fatura Seri/No") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Müşteri Adı / Unvanı") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerTaxNo, onValueChange = { customerTaxNo = it }, label = { Text("Vergi No / T.C.") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = totalAmountStr, onValueChange = { totalAmountStr = it }, label = { Text("Toplam Tutar (TRY)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Açıklama / Notlar") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = totalAmountStr.toDoubleOrNull() ?: 0.0
                    onCreate(invoiceNo, customerName, customerTaxNo.ifBlank { null }, amount, notes.ifBlank { null })
                },
                enabled = customerName.isNotBlank() && totalAmountStr.toDoubleOrNull() != null
            ) {
                Text("Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

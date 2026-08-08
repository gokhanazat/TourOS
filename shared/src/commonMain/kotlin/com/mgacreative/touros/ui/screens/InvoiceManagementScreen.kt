package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.InvoiceManagementUiState
import com.mgacreative.touros.ui.viewmodel.InvoiceManagementViewModel

/**
 * Fatura Oluşturma & Gerçek Zamanlı PDF Önizleme — TourOS 0.3
 *
 * Sol: Düzenlenebilir Fatura Formu
 * Sağ: Gerçek Zamanlı PDF Önizlemesi (A4 Mockup)
 * Expanded (≥768dp): Yan yana | Compact (<768dp): Sekmeli (Form / PDF)
 */
@Composable
fun InvoiceManagementScreen(
    viewModel: InvoiceManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Düzenlenebilir Fatura Formu State'leri (Gerçek zamanlı PDF için)
    var invoiceNo by remember { mutableStateOf("INV-202608-003") }
    var customerName by remember { mutableStateOf("Ahmet Yılmaz") }
    var customerTaxNo by remember { mutableStateOf("12345678901") }
    var serviceDescription by remember { mutableStateOf("Kapadokya VIP Tur & Konaklama Paketi") }
    var subtotalStr by remember { mutableStateOf("15000") }
    var taxRateStr by remember { mutableStateOf("20") }
    var notes by remember { mutableStateOf("İşbu fatura 7 gün içerisinde ödenmelidir.") }

    val subtotal = subtotalStr.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateStr.toDoubleOrNull() ?: 20.0
    val taxAmount = subtotal * (taxRate / 100.0)
    val totalAmount = subtotal + taxAmount

    var compactTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Fatura Oluşturma & PDF",
                subtitle = "Fatura düzenleme ve gerçek zamanlı PDF önizleme",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is InvoiceManagementUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is InvoiceManagementUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is InvoiceManagementUiState.Success -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val isExpanded = maxWidth >= 768.dp

                    if (isExpanded) {
                        // ── MASAÜSTÜ / TABLET: SOLDA FORM, SAĞDA PDF ÖNİZLEME (YAN YANA) ─
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(TourOSSpacing.large),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            // SOL: Düzenlenebilir Fatura Formu Panel
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                Text(
                                    "✏️ Fatura Formu & Bilgiler",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )

                                InvoiceFormPanel(
                                    invoiceNo = invoiceNo, onInvoiceNoChange = { invoiceNo = it },
                                    customerName = customerName, onCustomerNameChange = { customerName = it },
                                    customerTaxNo = customerTaxNo, onCustomerTaxNoChange = { customerTaxNo = it },
                                    serviceDescription = serviceDescription, onServiceDescriptionChange = { serviceDescription = it },
                                    subtotalStr = subtotalStr, onSubtotalChange = { subtotalStr = it },
                                    taxRateStr = taxRateStr, onTaxRateChange = { taxRateStr = it },
                                    notes = notes, onNotesChange = { notes = it },
                                    onSaveInvoice = {
                                        viewModel.createNewInvoice(invoiceNo, customerName, customerTaxNo.ifBlank { null }, totalAmount, notes.ifBlank { null })
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            VerticalDivider(color = TourOSColors.Divider, thickness = 1.dp)

                            // SAĞ: Gerçek Zamanlı PDF Önizleme Panel (A4 Formatı)
                            Column(
                                modifier = Modifier.weight(1.1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                            ) {
                                Text(
                                    "📄 Gerçek Zamanlı PDF Önizlemesi",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                                )

                                RealTimePdfPreviewPanel(
                                    invoiceNo = invoiceNo,
                                    customerName = customerName,
                                    customerTaxNo = customerTaxNo,
                                    serviceDescription = serviceDescription,
                                    subtotal = subtotal,
                                    taxRate = taxRate,
                                    taxAmount = taxAmount,
                                    totalAmount = totalAmount,
                                    notes = notes,
                                    onExportPdf = {
                                        state.invoices.firstOrNull()?.let { viewModel.exportInvoicePdf(it) }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // ── MOBİL: SEKMELİ (FORM DÜZENLE / PDF ÖNİZLEME) ─────────────
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(TourOSSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            PrimaryTabRow(
                                selectedTabIndex = compactTabIndex,
                                containerColor = TourOSColors.Background,
                                contentColor = TourOSColors.Primary
                            ) {
                                Tab(
                                    selected = compactTabIndex == 0,
                                    onClick = { compactTabIndex = 0 },
                                    text = { Text("✏️ Fatura Formu", style = TourOSTypography.Label) }
                                )
                                Tab(
                                    selected = compactTabIndex == 1,
                                    onClick = { compactTabIndex = 1 },
                                    text = { Text("📄 PDF Önizleme", style = TourOSTypography.Label) }
                                )
                            }

                            when (compactTabIndex) {
                                0 -> InvoiceFormPanel(
                                    invoiceNo = invoiceNo, onInvoiceNoChange = { invoiceNo = it },
                                    customerName = customerName, onCustomerNameChange = { customerName = it },
                                    customerTaxNo = customerTaxNo, onCustomerTaxNoChange = { customerTaxNo = it },
                                    serviceDescription = serviceDescription, onServiceDescriptionChange = { serviceDescription = it },
                                    subtotalStr = subtotalStr, onSubtotalChange = { subtotalStr = it },
                                    taxRateStr = taxRateStr, onTaxRateChange = { taxRateStr = it },
                                    notes = notes, onNotesChange = { notes = it },
                                    onSaveInvoice = {
                                        viewModel.createNewInvoice(invoiceNo, customerName, customerTaxNo.ifBlank { null }, totalAmount, notes.ifBlank { null })
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                1 -> RealTimePdfPreviewPanel(
                                    invoiceNo = invoiceNo,
                                    customerName = customerName,
                                    customerTaxNo = customerTaxNo,
                                    serviceDescription = serviceDescription,
                                    subtotal = subtotal,
                                    taxRate = taxRate,
                                    taxAmount = taxAmount,
                                    totalAmount = totalAmount,
                                    notes = notes,
                                    onExportPdf = {
                                        state.invoices.firstOrNull()?.let { viewModel.exportInvoicePdf(it) }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Sol Panel: Düzenlenebilir Fatura Formu ──────────────────────────────────

@Composable
private fun InvoiceFormPanel(
    invoiceNo: String, onInvoiceNoChange: (String) -> Unit,
    customerName: String, onCustomerNameChange: (String) -> Unit,
    customerTaxNo: String, onCustomerTaxNoChange: (String) -> Unit,
    serviceDescription: String, onServiceDescriptionChange: (String) -> Unit,
    subtotalStr: String, onSubtotalChange: (String) -> Unit,
    taxRateStr: String, onTaxRateChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    onSaveInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    TourOSCard(
        modifier = modifier,
        backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.4f),
        contentPadding = TourOSSpacing.large
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                TourOSTextField(
                    value = invoiceNo,
                    onValueChange = onInvoiceNoChange,
                    label = "Fatura Seri / No",
                    placeholder = "INV-202608-003",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TourOSTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = "Müşteri Adı / Unvanı",
                    placeholder = "Ahmet Yılmaz",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TourOSTextField(
                    value = customerTaxNo,
                    onValueChange = onCustomerTaxNoChange,
                    label = "Vergi No / T.C. Kimlik",
                    placeholder = "12345678901",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TourOSTextField(
                    value = serviceDescription,
                    onValueChange = onServiceDescriptionChange,
                    label = "Hizmet / Kalem Açıklaması",
                    placeholder = "Kapadokya Tur Hizmeti...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    TourOSTextField(
                        value = subtotalStr,
                        onValueChange = onSubtotalChange,
                        label = "Matrah (KDV Hariç ₺)",
                        placeholder = "15000",
                        modifier = Modifier.weight(1f)
                    )
                    TourOSTextField(
                        value = taxRateStr,
                        onValueChange = onTaxRateChange,
                        label = "KDV Oranı (%)",
                        placeholder = "20",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TourOSTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = "Fatura Notları & Banka Bilgileri",
                    placeholder = "7 gün içinde ödenmelidir...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                HorizontalDivider(color = TourOSColors.Divider)
            }

            item {
                TourOSButton(
                    text = "💾 Faturayı Kaydet ve Kes",
                    onClick = onSaveInvoice,
                    variant = TourOSButtonVariant.PRIMARY,
                    enabled = customerName.isNotBlank() && subtotalStr.toDoubleOrNull() != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Sağ Panel: Gerçek Zamanlı PDF Önizlemesi (A4 Görünümü) ───────────────────

@Composable
private fun RealTimePdfPreviewPanel(
    invoiceNo: String,
    customerName: String,
    customerTaxNo: String,
    serviceDescription: String,
    subtotal: Double,
    taxRate: Double,
    taxAmount: Double,
    totalAmount: Double,
    notes: String,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    TourOSCard(
        modifier = modifier,
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.large
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // A4 PDF Belge Mockup İçeriği
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(Color.White)
                    .border(1.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .padding(TourOSSpacing.large)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    // PDF Başlığı & Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "TourOS Seyahat Acentası A.Ş.",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                "Turizm & Seyahat Hizmetleri",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "E-FATURA",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                "No: ${invoiceNo.ifBlank { "INV-0000" }}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                "Tarih: 07.08.2026",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }

                    HorizontalDivider(color = TourOSColors.Primary, thickness = 2.dp)

                    // Alıcı / Müşteri Bilgileri
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.PrimaryContainer.copy(alpha = 0.3f))
                            .padding(TourOSSpacing.medium)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("SAYIN (ALICI):", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            Text(
                                customerName.ifBlank { "[Müşteri Adı Girilmedi]" },
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                "Vergi / TC No: ${customerTaxNo.ifBlank { "—" }}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }

                    // Kalem Tablosu
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TourOSColors.PrimaryContainer)
                                    .padding(horizontal = TourOSSpacing.small, vertical = 4.dp)
                            ) {
                                Text("Hizmet Açıklaması", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), modifier = Modifier.weight(2f))
                                Text("Matrah (₺)", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = TourOSSpacing.small, vertical = 6.dp)
                            ) {
                                Text(
                                    serviceDescription.ifBlank { "Turizm Hizmet Bedeli" },
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    "₺ ${formatMoney(subtotal)}",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    // Toplam Alt Tablosu
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Ara Toplam: ₺ ${formatMoney(subtotal)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text("KDV (%${taxRate.toInt()}): ₺ ${formatMoney(taxAmount)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.width(160.dp))
                        Text(
                            "GENEL TOPLAM: ₺ ${formatMoney(totalAmount)}",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }

                    // Fatura Notları
                    if (notes.isNotBlank()) {
                        Text(
                            "Not: $notes",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            }

            Spacer(Modifier.height(TourOSSpacing.medium))

            // Dışa Aktar & PDF İndir Butonu
            TourOSButton(
                text = "📄 PDF Dışa Aktar & İndir",
                onClick = onExportPdf,
                variant = TourOSButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

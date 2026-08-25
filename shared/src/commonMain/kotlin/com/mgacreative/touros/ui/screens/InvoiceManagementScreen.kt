package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.InvoiceManagementUiState
import com.mgacreative.touros.ui.viewmodel.InvoiceManagementViewModel

/**
 * Fatura Yönetimi & E-Fatura — TourOS Canlı Veri Tabanlı & İptal/Sil Destekli Sürüm
 *
 * Sekmeler:
 * 1. ✏️ Yeni Fatura Oluştur (Gelir & Gider Faturası Türü Seçimli)
 * 2. 📄 Gerçek Zamanlı PDF Önizleme
 * 3. 📋 Kayıtlı Faturalar Listesi (İptal Etme & Silme Destekli)
 */
@Composable
fun InvoiceManagementScreen(
    viewModel: InvoiceManagementViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by com.mgacreative.touros.ui.localization.AppLanguageManager.currentLanguage.collectAsState()

    var selectedMainTab by remember { mutableStateOf(0) }

    // Düzenlenebilir Fatura Formu State'leri
    var invoiceType by remember { mutableStateOf("sale") } // "sale" (Gelir) veya "purchase" (Gider)
    var invoiceNo by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerTaxNo by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var subtotalStr by remember { mutableStateOf("") }
    var taxRateStr by remember { mutableStateOf("20") }
    var notes by remember { mutableStateOf("") }

    val subtotal = subtotalStr.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateStr.toDoubleOrNull() ?: 20.0
    val taxAmount = subtotal * (taxRate / 100.0)
    val totalAmount = subtotal + taxAmount

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Yönetimi & e-Fatura"),
                subtitle = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("GİB Entegrasyonu, e-Arşiv/e-Fatura Kesme ve İptal Yönetimi"),
                onNavigateBack = onNavigateBack
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(TourOSSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // Bildirim Mesajı
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

                    // ── ANA SEKMELER (HEM MASAÜSTÜ HEM MOBİLDE GÖRÜNÜR) ─────────────
                    PrimaryTabRow(
                        selectedTabIndex = selectedMainTab,
                        containerColor = TourOSColors.Background,
                        contentColor = TourOSColors.Primary
                    ) {
                        Tab(
                            selected = selectedMainTab == 0,
                            onClick = { selectedMainTab = 0 },
                            text = { Text("✏️ ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Oluştur")}", style = TourOSTypography.Label, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedMainTab == 1,
                            onClick = { selectedMainTab = 1 },
                            text = { Text("📄 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PDF Önizleme")}", style = TourOSTypography.Label, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedMainTab == 2,
                            onClick = { selectedMainTab = 2 },
                            text = { Text("📋 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kayıtlı Faturalar")} (${state.invoices.size})", style = TourOSTypography.Label, fontWeight = FontWeight.Bold) }
                        )
                    }

                    when (selectedMainTab) {
                        0 -> InvoiceFormPanel(
                            invoiceType = invoiceType, onInvoiceTypeChange = { invoiceType = it },
                            invoiceNo = invoiceNo, onInvoiceNoChange = { invoiceNo = it },
                            customerName = customerName, onCustomerNameChange = { customerName = it },
                            customerTaxNo = customerTaxNo, onCustomerTaxNoChange = { customerTaxNo = it },
                            serviceDescription = serviceDescription, onServiceDescriptionChange = { serviceDescription = it },
                            subtotalStr = subtotalStr, onSubtotalChange = { subtotalStr = it },
                            taxRateStr = taxRateStr, onTaxRateChange = { taxRateStr = it },
                            notes = notes, onNotesChange = { notes = it },
                            onSaveInvoice = {
                                viewModel.createNewInvoice(
                                    invoiceNo = invoiceNo.ifBlank { "INV-${(1000..9999).random()}" },
                                    invoiceType = invoiceType,
                                    customerName = customerName,
                                    customerTaxNo = customerTaxNo.ifBlank { null },
                                    totalAmount = totalAmount,
                                    notes = notes.ifBlank { null }
                                )
                            },
                            onNavigateToPdf = { selectedMainTab = 1 },
                            isSaving = state.isCreatingInvoice,
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> RealTimePdfPreviewPanel(
                            invoiceType = invoiceType,
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
                        2 -> SavedInvoicesList(
                            invoices = state.invoices,
                            onExportPdf = { viewModel.exportInvoicePdf(it) },
                            onCancelInvoice = { viewModel.cancelInvoice(it) },
                            onDeleteInvoice = { viewModel.deleteInvoice(it) }
                        )
                    }
                }
            }
        }
    }
}

// ─── 1. Sekme: Fatura Düzenleme Formu ──────────────────────────────────────────

@Composable
private fun InvoiceFormPanel(
    invoiceType: String, onInvoiceTypeChange: (String) -> Unit,
    invoiceNo: String, onInvoiceNoChange: (String) -> Unit,
    customerName: String, onCustomerNameChange: (String) -> Unit,
    customerTaxNo: String, onCustomerTaxNoChange: (String) -> Unit,
    serviceDescription: String, onServiceDescriptionChange: (String) -> Unit,
    subtotalStr: String, onSubtotalChange: (String) -> Unit,
    taxRateStr: String, onTaxRateChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    onSaveInvoice: () -> Unit,
    onNavigateToPdf: () -> Unit = {},
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    val subtotal = subtotalStr.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateStr.toDoubleOrNull() ?: 20.0
    val taxAmount = subtotal * (taxRate / 100.0)
    val totalAmount = subtotal + taxAmount

    BoxWithConstraints(modifier = modifier) {
        val isWide = maxWidth >= 860.dp

        if (isWide) {
            // 🖥️ Masaüstü / Web: 2 Sütunlu Finansal Izgara (Sol: Form | Sağ: Canlı Hesap Kartı)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
            ) {
                // SOL SÜTUN (Form Alanları)
                Surface(
                    modifier = Modifier.weight(1.4f).fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Divider),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(TourOSSpacing.large),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        // 1. Satır: Fatura Türü & Seri No
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Türü:"),
                                    style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold, color = TourOSColors.TextSecondary)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = invoiceType == "sale",
                                        onClick = { onInvoiceTypeChange("sale") },
                                        label = { Text("📈 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Satış (Gelir)"), style = TourOSTypography.Caption, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TourOSColors.SuccessContainer,
                                            selectedLabelColor = TourOSColors.Success
                                        )
                                    )
                                    FilterChip(
                                        selected = invoiceType == "purchase",
                                        onClick = { onInvoiceTypeChange("purchase") },
                                        label = { Text("📉 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Alış (Gider)"), style = TourOSTypography.Caption, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TourOSColors.PrimaryContainer,
                                            selectedLabelColor = TourOSColors.Primary
                                        )
                                    )
                                }
                            }

                            TourOSTextField(
                                value = invoiceNo,
                                onValueChange = onInvoiceNoChange,
                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Seri / No"),
                                placeholder = "Örn: INV-202608-001",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 2. Satır: Müşteri/Tedarikçi Adı & VKN/TC No
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = customerName,
                                onValueChange = onCustomerNameChange,
                                label = if (invoiceType == "purchase") com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tedarikçi / Satıcı Ünvanı") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Müşteri / Cari Adı"),
                                placeholder = if (invoiceType == "purchase") "Travego Turizm A.Ş." else "Ahmet Yılmaz",
                                modifier = Modifier.weight(1.3f)
                            )
                            TourOSTextField(
                                value = customerTaxNo,
                                onValueChange = onCustomerTaxNoChange,
                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Vergi / TC Kimlik No"),
                                placeholder = "12345678901",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 3. Kalem / Hizmet Açıklaması
                        TourOSTextField(
                            value = serviceDescription,
                            onValueChange = onServiceDescriptionChange,
                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hizmet / Kalem Açıklaması"),
                            placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: Kapadokya VIP Tur Paket Hizmeti Bedeli"),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 4. Satır: Matrah & KDV Oranı
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = subtotalStr,
                                onValueChange = onSubtotalChange,
                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Matrah (KDV Hariç ₺)"),
                                placeholder = "15000",
                                modifier = Modifier.weight(1.2f)
                            )
                            TourOSTextField(
                                value = taxRateStr,
                                onValueChange = onTaxRateChange,
                                label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("KDV Oranı (%)"),
                                placeholder = "20",
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        // 5. Notlar & Banka Bilgisi
                        TourOSTextField(
                            value = notes,
                            onValueChange = onNotesChange,
                            label = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Notları & Banka / IBAN"),
                            placeholder = com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Örn: 7 gün içinde ödenmelidir... TR00 0000 0000..."),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // SAĞ SÜTUN (Canlı Fatura Özeti & Aksiyon Paneli)
                Surface(
                    modifier = Modifier.weight(0.9f).fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A), // Modern Koyu Finansal Kart
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(TourOSSpacing.large),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                            // Başlık & GİB Rozeti
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🧾 " + com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Fatura Özeti"),
                                    style = TourOSTypography.TitleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        "🟢 GİB e-Arşiv",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = TourOSTypography.Caption.copy(color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155))

                            // Matrah
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Matrah (KDV Hariç):"), style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 12.sp))
                                Text("₺ ${formatMoney(subtotal)}", style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                            }

                            // KDV Tutarı
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hesaplanan KDV (%$taxRateStr):"), style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontSize = 12.sp))
                                Text("₺ ${formatMoney(taxAmount)}", style = TourOSTypography.BodyMedium.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold))
                            }

                            HorizontalDivider(color = Color(0xFF334155))

                            // Ödenecek Genel Toplam
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    com.mgacreative.touros.ui.localization.AppLanguageManager.translate("GENEL TOPLAM (Ödenecek)"),
                                    style = TourOSTypography.Caption.copy(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                                Text(
                                    "₺ ${formatMoney(totalAmount)}",
                                    style = TourOSTypography.DisplaySmall.copy(color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                                )
                            }
                        }

                        // Butonlar
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TourOSButton(
                                text = if (isSaving) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Kaydediliyor...") else "💾 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Faturayı Kaydet")}",
                                onClick = onSaveInvoice,
                                variant = TourOSButtonVariant.PRIMARY,
                                enabled = customerName.isNotBlank() && subtotalStr.toDoubleOrNull() != null && !isSaving,
                                modifier = Modifier.fillMaxWidth()
                            )

                            TourOSButton(
                                text = "📄 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PDF Önizleme")}",
                                onClick = onNavigateToPdf,
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            // 📱 Mobil Görünüm (Dikey Akış)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Divider)
            ) {
                Column(
                    modifier = Modifier.padding(TourOSSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    TourOSTextField(value = invoiceNo, onValueChange = onInvoiceNoChange, label = "Fatura Seri / No", placeholder = "INV-202608-001", modifier = Modifier.fillMaxWidth())
                    TourOSTextField(value = customerName, onValueChange = onCustomerNameChange, label = "Müşteri / Cari Adı", placeholder = "Ahmet Yılmaz", modifier = Modifier.fillMaxWidth())
                    TourOSTextField(value = customerTaxNo, onValueChange = onCustomerTaxNoChange, label = "Vergi / TC No", placeholder = "12345678901", modifier = Modifier.fillMaxWidth())
                    TourOSTextField(value = serviceDescription, onValueChange = onServiceDescriptionChange, label = "Hizmet Açıklaması", placeholder = "Tur Hizmet Bedeli", modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TourOSTextField(value = subtotalStr, onValueChange = onSubtotalChange, label = "Matrah (₺)", placeholder = "15000", modifier = Modifier.weight(1f))
                        TourOSTextField(value = taxRateStr, onValueChange = onTaxRateChange, label = "KDV (%)", placeholder = "20", modifier = Modifier.weight(1f))
                    }
                    Text("Genel Toplam: ₺ ${formatMoney(totalAmount)}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold))
                    TourOSButton(
                        text = if (isSaving) "Kaydediliyor..." else "💾 Faturayı Kaydet",
                        onClick = onSaveInvoice,
                        variant = TourOSButtonVariant.PRIMARY,
                        enabled = customerName.isNotBlank() && subtotalStr.toDoubleOrNull() != null && !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── 2. Sekme: Canlı PDF Önizleme ──────────────────────────────────────────────

@Composable
private fun RealTimePdfPreviewPanel(
    invoiceType: String,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "TourOS Seyahat Acentası A.Ş.",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Turizm & Seyahat Hizmetleri",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (invoiceType == "purchase") com.mgacreative.touros.ui.localization.AppLanguageManager.translate("GİDER FATURASI") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("SATIŞ E-FATURASI"),
                                style = TourOSTypography.TitleLarge.copy(color = if (invoiceType == "purchase") TourOSColors.Primary else TourOSColors.Success),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "No: ${invoiceNo.ifBlank { "INV-DRAFT" }}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = TourOSColors.Primary, thickness = 2.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.PrimaryContainer.copy(alpha = 0.3f))
                            .padding(TourOSSpacing.medium)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (invoiceType == "purchase") com.mgacreative.touros.ui.localization.AppLanguageManager.translate("SATICI (TEDARİKÇİ):") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("ALICI (MÜŞTERİ):"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                customerName.ifBlank { "[Unvan Girilmedi]" },
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Vergi / TC No")}: ${customerTaxNo.ifBlank { "—" }}",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }

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
                                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Hizmet / Açıklama"), style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Matrah (₺)"), style = TourOSTypography.Caption.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = TourOSSpacing.small, vertical = 6.dp)
                            ) {
                                Text(
                                    serviceDescription.ifBlank { com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Turizm Hizmet Bedeli") },
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

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Ara Toplam")}: ₺ ${formatMoney(subtotal)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("KDV")} (%${taxRate.toInt()}): ₺ ${formatMoney(taxAmount)}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.width(160.dp))
                        Text(
                            "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("GENEL TOPLAM")}: ₺ ${formatMoney(totalAmount)}",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (notes.isNotBlank()) {
                        Text(
                            "${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Not")}: $notes",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            }

            Spacer(Modifier.height(TourOSSpacing.medium))

            TourOSButton(
                text = "📄 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("PDF Dışa Aktar & İndir")}",
                onClick = onExportPdf,
                variant = TourOSButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── 3. Sekme: Kayıtlı Faturalar Listesi (İptal & Sil Destekli) ───────────────

@Composable
private fun SavedInvoicesList(
    invoices: List<Invoice>,
    onExportPdf: (Invoice) -> Unit,
    onCancelInvoice: (String) -> Unit,
    onDeleteInvoice: (String) -> Unit
) {
    var typeFilter by remember { mutableStateOf("ALL") } // ALL, SALE, PURCHASE

    val filteredInvoices = invoices.filter { inv ->
        when (typeFilter) {
            "SALE" -> inv.invoiceType == "sale"
            "PURCHASE" -> inv.invoiceType == "purchase"
            else -> true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
    ) {
        // Filtre Butonları
        LazyRow(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            item {
                FilterChip(
                    selected = typeFilter == "ALL",
                    onClick = { typeFilter = "ALL" },
                    label = { Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Tüm Faturalar")} (${invoices.size})", style = TourOSTypography.Caption) }
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "SALE",
                    onClick = { typeFilter = "SALE" },
                    label = { Text("📈 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Satış / Gelir")} (${invoices.count { it.invoiceType == "sale" }})", style = TourOSTypography.Caption) }
                )
            }
            item {
                FilterChip(
                    selected = typeFilter == "PURCHASE",
                    onClick = { typeFilter = "PURCHASE" },
                    label = { Text("📉 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Alış / Gider")} (${invoices.count { it.invoiceType == "purchase" }})", style = TourOSTypography.Caption) }
                )
            }
        }

        if (filteredInvoices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(TourOSSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Henüz kaydedilmiş bir fatura bulunmamaktadır."), style = TourOSTypography.BodyMedium, color = TourOSColors.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                items(filteredInvoices) { inv ->
                    val isCanceled = inv.status == "canceled"
                    val isPurchase = inv.invoiceType == "purchase"

                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isCanceled) TourOSColors.Surface.copy(alpha = 0.6f) else TourOSColors.Surface,
                        contentPadding = TourOSSpacing.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                                    Text(inv.invoiceNo, style = TourOSTypography.Label.copy(color = TourOSColors.Primary), fontWeight = FontWeight.Bold)
                                    Text(
                                        if (isPurchase) com.mgacreative.touros.ui.localization.AppLanguageManager.translate("GİDER") else com.mgacreative.touros.ui.localization.AppLanguageManager.translate("GELİR"),
                                        style = TourOSTypography.Caption.copy(color = if (isPurchase) TourOSColors.Primary else TourOSColors.Success),
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isCanceled) {
                                        Text("[${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İPTAL EDİLDİ")}]", style = TourOSTypography.Caption.copy(color = TourOSColors.Error), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(inv.customerName, style = TourOSTypography.BodyMedium, fontWeight = FontWeight.Bold)
                                Text("${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("Genel Toplam")}: ₺ ${formatMoney(inv.totalAmount)}", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                                TourOSButton(
                                    text = "📄 PDF",
                                    onClick = { onExportPdf(inv) },
                                    variant = TourOSButtonVariant.SECONDARY
                                )

                                if (!isCanceled && inv.id.isNotBlank()) {
                                    TourOSButton(
                                        text = "🚫 ${com.mgacreative.touros.ui.localization.AppLanguageManager.translate("İptal")}",
                                        onClick = { onCancelInvoice(inv.id) },
                                        variant = TourOSButtonVariant.SECONDARY
                                    )
                                }

                                if (inv.id.isNotBlank()) {
                                    IconButton(onClick = { onDeleteInvoice(inv.id) }) {
                                        Text("🗑️", style = TourOSTypography.TitleMedium)
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

private fun formatMoney(amount: Double): String {
    return com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(amount)
}

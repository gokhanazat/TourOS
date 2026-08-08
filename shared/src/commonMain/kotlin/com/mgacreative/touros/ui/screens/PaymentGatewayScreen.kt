package com.mgacreative.touros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.PaymentGatewayUiState
import com.mgacreative.touros.ui.viewmodel.PaymentGatewayViewModel

private enum class PaymentMethodType(val id: String, val title: String, val icon: String, val subtitle: String) {
    CASH("cash", "Nakit Tahsilat", "💵", "Elden Nakit Alma"),
    CARD("card", "Kredi / Banka Kartı", "💳", "POS & Online POS"),
    TRANSFER("transfer", "Havale / EFT", "🏦", "Banka Hesabına"),
    LINK("link", "Ödeme Linki", "🔗", "SMS & WhatsApp Linki")
}

/**
 * Ödeme Alma Ekranı — TourOS 0.3
 *
 * Üstte Büyük ve Net Tutar Alanı.
 * Büyük, İkonlu Segmented Ödeme Yöntemi Kartları (Nakit / Kart / Havale / Link).
 * Seçilen Yönteme Göre Dinamik Ödeme Paneli.
 */
@Composable
fun PaymentGatewayScreen(
    viewModel: PaymentGatewayViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedMethod by remember { mutableStateOf(PaymentMethodType.CARD) }
    var amountStr by remember { mutableStateOf("7500") }

    // Kredi Kartı Formu State'leri
    var cardHolder by remember { mutableStateOf("Ahmet Yılmaz") }
    var cardNumber by remember { mutableStateOf("5890 0400 1234 5678") }
    var expireMonth by remember { mutableStateOf("12") }
    var expireYear by remember { mutableStateOf("28") }
    var cvc by remember { mutableStateOf("321") }

    // Havale / Nakit / Link State'leri
    var referenceNote by remember { mutableStateOf("BK-2026-8910 Rezervasyon Ödemesi") }
    var customerPhoneEmail by remember { mutableStateOf("+90 555 123 4567") }

    val amount = amountStr.toDoubleOrNull() ?: 0.0

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "Ödeme Alma & Tahsilat",
                subtitle = "Çoklu ödeme yöntemi ve tutar yönetimi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PaymentGatewayUiState.Processing -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        CircularProgressIndicator(color = TourOSColors.Primary)
                        Text(
                            "💳 Ödeme Sağlayıcı İle İşlem Yürütülüyor...",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                        )
                    }
                }
            }
            is PaymentGatewayUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hata: ${state.message}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error))
                }
            }
            is PaymentGatewayUiState.Idle, is PaymentGatewayUiState.Success -> {
                val successState = state as? PaymentGatewayUiState.Success
                val activeProvider = successState?.activeProvider ?: "iyzico"
                val notificationMessage = successState?.notificationMessage
                val lastPaymentResponse = successState?.lastPaymentResponse
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // ── 1. BÜYÜK VE NET TUTAR ALANI ─────────────────────────────
                    item {
                        BigAmountHeaderCard(
                            amountStr = amountStr,
                            onAmountChange = { amountStr = it },
                            amountDouble = amount
                        )
                    }

                    // Bildirim mesajı
                    if (notificationMessage != null) {
                        item {
                            val isSuccess = lastPaymentResponse?.isSuccess == true
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                    .background(if (isSuccess) TourOSColors.SuccessContainer else TourOSColors.ErrorContainer)
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Text(
                                    notificationMessage,
                                    style = TourOSTypography.Label.copy(
                                        color = if (isSuccess) TourOSColors.Success else TourOSColors.Error
                                    )
                                )
                            }
                        }
                    }

                    // ── 2. BÜYÜK İKONLU SEGMENTED ÖDEME YÖNTEMİ KARTLARI ───────
                    item {
                        Text(
                            "💳 Ödeme Yöntemi Seçin",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                        ) {
                            PaymentMethodType.entries.forEach { method ->
                                SegmentedMethodCard(
                                    method = method,
                                    isSelected = selectedMethod == method,
                                    onClick = { selectedMethod = method },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // ── 3. DİNAMİK YÖNTEM PANELİ ──────────────────────────────
                    item {
                        when (selectedMethod) {
                            PaymentMethodType.CARD -> {
                                CardPaymentPanel(
                                    activeProvider = activeProvider,
                                    onProviderChange = { viewModel.setProvider(it) },
                                    cardHolder = cardHolder, onCardHolderChange = { cardHolder = it },
                                    cardNumber = cardNumber, onCardNumberChange = { cardNumber = it },
                                    expireMonth = expireMonth, onExpireMonthChange = { expireMonth = it },
                                    expireYear = expireYear, onExpireYearChange = { expireYear = it },
                                    cvc = cvc, onCvcChange = { cvc = it },
                                    amount = amount,
                                    onSubmitPayment = {
                                        viewModel.executePayment(cardNumber, cardHolder, expireMonth, expireYear, cvc, amount)
                                    }
                                )
                            }
                            PaymentMethodType.CASH -> {
                                CashPaymentPanel(
                                    amount = amount,
                                    note = referenceNote,
                                    onNoteChange = { referenceNote = it },
                                    onSubmitPayment = {
                                        viewModel.executePayment("CASH-POS", "Nakit Tahsilat", "00", "00", "000", amount)
                                    }
                                )
                            }
                            PaymentMethodType.TRANSFER -> {
                                TransferPaymentPanel(
                                    amount = amount,
                                    referenceNote = referenceNote,
                                    onReferenceChange = { referenceNote = it },
                                    onSubmitPayment = {
                                        viewModel.executePayment("BANK-EFT", "Havale/EFT", "00", "00", "000", amount)
                                    }
                                )
                            }
                            PaymentMethodType.LINK -> {
                                LinkPaymentPanel(
                                    amount = amount,
                                    contactInfo = customerPhoneEmail,
                                    onContactChange = { customerPhoneEmail = it },
                                    onSubmitPayment = {
                                        viewModel.executePayment("LINK-PAY", "Ödeme Linki", "00", "00", "000", amount)
                                    }
                                )
                            }
                        }
                    }

                    // ── 4. SON İŞLEM DEKONT ÖZETİ ──────────────────────────────
                    if (lastPaymentResponse != null) {
                        item {
                            val resp = lastPaymentResponse

                            TourOSCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = TourOSColors.PrimaryContainer,
                                contentPadding = TourOSSpacing.medium
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "🧾 Son İşlem Dekont Özeti",
                                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                                    )
                                    Text(
                                        "Sağlayıcı: ${resp.providerName.uppercase()}  ·  Durum: ${resp.status}  ·  İşlem ID: ${resp.transactionId ?: "—"}",
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Büyük ve Net Tutar Alanı Kartı ──────────────────────────────────────────

@Composable
private fun BigAmountHeaderCard(
    amountStr: String,
    onAmountChange: (String) -> Unit,
    amountDouble: Double
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
                "TAHSİL EDİLECEK TUTAR",
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )

            // BÜYÜK VE NET TUTAR GÖSTERGESİ
            Text(
                text = "₺ ${formatMoney(amountDouble)}",
                style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
            )

            HorizontalDivider(color = TourOSColors.Divider)

            // Tutar Düzenleme Girişi
            TourOSTextField(
                value = amountStr,
                onValueChange = onAmountChange,
                label = "Tutar Değiştir (TRY)",
                placeholder = "7500",
                modifier = Modifier.width(260.dp)
            )
        }
    }
}

// ─── Büyük İkonlu Segmented Ödeme Yöntemi Kartı ─────────────────────────────

@Composable
private fun SegmentedMethodCard(
    method: PaymentMethodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border
    val bgColor = if (isSelected) TourOSColors.PrimaryContainer else TourOSColors.Surface

    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            )
            .clickable { onClick() }
            .padding(TourOSSpacing.small),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                method.icon,
                style = TourOSTypography.DisplaySmall
            )
            Text(
                method.title,
                style = TourOSTypography.Label.copy(
                    color = if (isSelected) TourOSColors.Primary else TourOSColors.TextPrimary
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                method.subtitle,
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ─── Panel: Kredi Kartı Ödeme Paneli ──────────────────────────────────────────

@Composable
private fun CardPaymentPanel(
    activeProvider: String,
    onProviderChange: (String) -> Unit,
    cardHolder: String, onCardHolderChange: (String) -> Unit,
    cardNumber: String, onCardNumberChange: (String) -> Unit,
    expireMonth: String, onExpireMonthChange: (String) -> Unit,
    expireYear: String, onExpireYearChange: (String) -> Unit,
    cvc: String, onCvcChange: (String) -> Unit,
    amount: Double,
    onSubmitPayment: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.4f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "💳 POS Altyapı Seçimi",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)) {
                    FilterChip(
                        selected = activeProvider == "iyzico",
                        onClick = { onProviderChange("iyzico") },
                        label = { Text("🇹🇷 İyzico", style = TourOSTypography.Caption) }
                    )
                    FilterChip(
                        selected = activeProvider == "stripe",
                        onClick = { onProviderChange("stripe") },
                        label = { Text("🌐 Stripe", style = TourOSTypography.Caption) }
                    )
                    FilterChip(
                        selected = activeProvider == "mock",
                        onClick = { onProviderChange("mock") },
                        label = { Text("🧪 Test POS", style = TourOSTypography.Caption) }
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider)

            TourOSTextField(
                value = cardHolder,
                onValueChange = onCardHolderChange,
                label = "Kart Üzerindeki İsim",
                placeholder = "Ahmet Yılmaz",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSTextField(
                value = cardNumber,
                onValueChange = onCardNumberChange,
                label = "Kart Numarası",
                placeholder = "5890 0400 1234 5678",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = expireMonth,
                    onValueChange = onExpireMonthChange,
                    label = "Ay",
                    placeholder = "12",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = expireYear,
                    onValueChange = onExpireYearChange,
                    label = "Yıl",
                    placeholder = "28",
                    modifier = Modifier.weight(1f)
                )
                TourOSTextField(
                    value = cvc,
                    onValueChange = onCvcChange,
                    label = "CVC",
                    placeholder = "321",
                    modifier = Modifier.weight(1f)
                )
            }

            TourOSButton(
                text = "💳 ₺ ${formatMoney(amount)} Kart ile Öde",
                onClick = onSubmitPayment,
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Panel: Nakit Tahsilat Paneli ─────────────────────────────────────────────

@Composable
private fun CashPaymentPanel(
    amount: Double,
    note: String,
    onNoteChange: (String) -> Unit,
    onSubmitPayment: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SuccessContainer.copy(alpha = 0.4f),
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Text(
                "💵 Nakit Tahsilat İşlemi",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success)
            )

            Text(
                "Müşteriden elden ₺ ${formatMoney(amount)} tutarında nakit tahsil edilmiştir.",
                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
            )

            TourOSTextField(
                value = note,
                onValueChange = onNoteChange,
                label = "Tahsilat Açıklaması / Makbuz No",
                placeholder = "Makbuz #10928...",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSButton(
                text = "💵 ₺ ${formatMoney(amount)} Nakit Tahsil Et",
                onClick = onSubmitPayment,
                variant = TourOSButtonVariant.SUCCESS,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Panel: Havale / EFT Paneli ───────────────────────────────────────────────

@Composable
private fun TransferPaymentPanel(
    amount: Double,
    referenceNote: String,
    onReferenceChange: (String) -> Unit,
    onSubmitPayment: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.PrimaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Text(
                "🏦 Banka Havale / EFT İle Ödeme",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.Surface)
                    .padding(TourOSSpacing.medium)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Alıcı: TourOS Seyahat Acentası A.Ş.", style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary))
                    Text("IBAN: TR92 0006 2000 0000 1234 5678 90", style = TourOSTypography.Caption.copy(color = TourOSColors.Primary))
                    Text("Banka: Garanti BBVA — Kurumsal Şube", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                }
            }

            TourOSTextField(
                value = referenceNote,
                onValueChange = onReferenceChange,
                label = "Havale Dekont / Açıklama Notu",
                placeholder = "Açıklama veya dekont no...",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSButton(
                text = "🏦 Havale / EFT Bildirimini Onayla (₺ ${formatMoney(amount)})",
                onClick = onSubmitPayment,
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Panel: Ödeme Linki Paneli ────────────────────────────────────────────────

@Composable
private fun LinkPaymentPanel(
    amount: Double,
    contactInfo: String,
    onContactChange: (String) -> Unit,
    onSubmitPayment: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.SecondaryContainer,
        contentPadding = TourOSSpacing.large
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            Text(
                "🔗 SMS & WhatsApp İle Ödeme Linki Gönder",
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary)
            )

            Text(
                "Müşteriye ₺ ${formatMoney(amount)} tutarında güvenli İyzico/Stripe ödeme linki gönderilir.",
                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
            )

            TourOSTextField(
                value = contactInfo,
                onValueChange = onContactChange,
                label = "Müşteri Telefon / E-Posta",
                placeholder = "+90 555 123 4567 veya ahmet@touros.com",
                modifier = Modifier.fillMaxWidth()
            )

            TourOSButton(
                text = "📲 SMS / WhatsApp İle Link Gönder (₺ ${formatMoney(amount)})",
                onClick = onSubmitPayment,
                variant = TourOSButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSLoadingIndicator
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.BookingWizardStep
import com.mgacreative.touros.ui.viewmodel.CreateBookingWizardUiState
import com.mgacreative.touros.ui.viewmodel.CreateBookingWizardViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine uygun Rezervasyon Sihirbazı - Adım 2.
 * - Yolcular & PAX +/- Sayaç Bileşenleri.
 * - Transfer & Ek Hizmetler Checkbox / Radio listesi.
 * - İndirim / Komisyon Hakediş Özeti.
 * - Altta Sabit (Sticky) Özet Çubuğu & "✓ Rezervasyonu Onayla" Primary Butonu.
 */
@Composable
fun CreateBookingStep2Screen(
    onNavigateBack: () -> Unit = {},
    onBookingCreatedSuccess: (String) -> Unit = {},
    viewModel: CreateBookingWizardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isBookingCreated) {
        if (uiState.isBookingCreated && uiState.createdBookingId != null) {
            onBookingCreatedSuccess(uiState.createdBookingId!!)
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "Rezervasyon Tamamlama",
                subtitle = "Adım 2 / 2: Yolcular, Transfer & Ekstra Hizmetler ve Onay",
                navigationIcon = {
                    TourOSButton(
                        text = "← Adım 1'e Dön",
                        onClick = { viewModel.goToPreviousStep() },
                        variant = TourOSButtonVariant.TERTIARY
                    )
                }
            )
        },
        bottomBar = {
            StickySummaryBottomBar(
                uiState = uiState,
                onPreviousClick = { viewModel.goToPreviousStep() },
                onNextClick = {
                    if (uiState.currentStep == BookingWizardStep.CONFIRMATION) {
                        viewModel.submitBooking()
                    } else {
                        viewModel.goToNextStep()
                    }
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(TourOSSpacing.large)
        ) {
            if (uiState.isLoading) {
                TourOSLoadingIndicator(message = "Rezervasyon kaydediliyor ve onaylanıyor...")
            } else {
                when (uiState.currentStep) {
                    BookingWizardStep.PASSENGERS -> PassengersStepContent(uiState = uiState, viewModel = viewModel)
                    BookingWizardStep.EXTRAS_TRANSFER -> ExtrasTransferStepContent(uiState = uiState, viewModel = viewModel)
                    BookingWizardStep.DISCOUNT_COMMISSION -> DiscountCommissionStepContent(uiState = uiState, viewModel = viewModel)
                    BookingWizardStep.CONFIRMATION -> ConfirmationStepContent(uiState = uiState, viewModel = viewModel)
                    else -> PassengersStepContent(uiState = uiState, viewModel = viewModel)
                }
            }
        }

        if (uiState.error != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = {
                    Text(
                        text = "❌ Rezervasyon Kayıt Hatası",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Error),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = uiState.error.orEmpty(),
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                    )
                },
                confirmButton = {
                    TourOSButton(
                        text = "Tamam",
                        onClick = { viewModel.clearError() },
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            )
        }
    }
}

@Composable
private fun PassengersStepContent(
    uiState: CreateBookingWizardUiState,
    viewModel: CreateBookingWizardViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
    ) {
        // Kişi Dağılımı (PAX Counter Steppers)
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "Kişi Dağılımı (PAX Sayacı)", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Text(text = "Yetişkin, çocuk ve bebek sayılarını +/- sayaçlar ile ayarlayın.", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                PaxStepperRow(
                    label = "Yetişkin Sayısı (12+ Yaş)",
                    count = uiState.adultCount,
                    onCountChanged = { viewModel.updatePaxCounts(it, uiState.childCount, uiState.infantCount) }
                )
                HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.padding(vertical = TourOSSpacing.small))

                PaxStepperRow(
                    label = "Çocuk Sayısı (2-11 Yaş)",
                    count = uiState.childCount,
                    onCountChanged = { viewModel.updatePaxCounts(uiState.adultCount, it, uiState.infantCount) }
                )
                HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.padding(vertical = TourOSSpacing.small))

                PaxStepperRow(
                    label = "Bebek Sayısı (0-2 Yaş)",
                    count = uiState.infantCount,
                    onCountChanged = { viewModel.updatePaxCounts(uiState.adultCount, uiState.childCount, it) }
                )
            }
        }

        // Ana Yolcu / İletişim Bilgileri Formu
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "Ana Yolcu & İletişim Bilgileri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                TourOSTextField(
                    value = uiState.leadPassengerName,
                    onValueChange = { viewModel.updateLeadPassenger(it.uppercase(), uiState.leadPassengerEmail, uiState.leadPassengerPhone, uiState.leadPassengerTcNo) },
                    label = "Ad Soyad *",
                    placeholder = "ÖRN: AHMET YILMAZ",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TourOSTextField(
                        value = uiState.leadPassengerPhone,
                        onValueChange = { viewModel.updateLeadPassenger(uiState.leadPassengerName, uiState.leadPassengerEmail, it, uiState.leadPassengerTcNo) },
                        label = "Telefon Numarası *",
                        placeholder = "+90 532 000 00 00",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                    TourOSTextField(
                        value = uiState.leadPassengerEmail,
                        onValueChange = { viewModel.updateLeadPassenger(uiState.leadPassengerName, it, uiState.leadPassengerPhone, uiState.leadPassengerTcNo) },
                        label = "E-posta Adresi",
                        placeholder = "ahmet@example.com",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                TourOSTextField(
                    value = uiState.leadPassengerTcNo,
                    onValueChange = { viewModel.updateLeadPassenger(uiState.leadPassengerName, uiState.leadPassengerEmail, uiState.leadPassengerPhone, it.uppercase()) },
                    label = "TC Kimlik / Pasaport No",
                    placeholder = "11 Haneli TC Kimlik veya Pasaport Numarası",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PaxStepperRow(
    label: String,
    count: Int,
    onCountChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextPrimary))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TourOSButton(
                text = "-",
                onClick = { onCountChanged(count - 1) },
                variant = TourOSButtonVariant.TERTIARY
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            Text(text = "$count", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSButton(
                text = "+",
                onClick = { onCountChanged(count + 1) },
                variant = TourOSButtonVariant.TERTIARY
            )
        }
    }
}

@Composable
private fun ExtrasTransferStepContent(
    uiState: CreateBookingWizardUiState,
    viewModel: CreateBookingWizardViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
    ) {
        // Transfer Seçenekleri (Radio Group)
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "Transfer Hizmeti Seçimi", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                val transfers = listOf(
                    "Yok" to 0.0,
                    "Havalimanı Çift Yön Standart Transfer" to 750.0,
                    "VIP Karşılama ve Özel Araç Transferi" to 1500.0
                )

                transfers.forEach { (name, price) ->
                    val isSelected = uiState.selectedTransfer == name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = TourOSSpacing.xSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectTransfer(name, price) },
                            colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary)
                        )
                        Text(text = name, style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(1f))
                        Text(
                            text = if (price > 0) "+$price TRY" else "Ücretsiz",
                            style = TourOSTypography.TitleMedium.copy(color = if (price > 0) TourOSColors.Primary else TourOSColors.Success)
                        )
                    }
                }
            }
        }

        // Ekstra Hizmetler (Checkbox List)
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "Ekstra Hizmet ve Paket Seçimleri", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                val extras = listOf(
                    "Özel Türkçe Rehberlik Hizmeti" to 500.0,
                    "Kapsamlı Seyahat İptal & Sağlık Sigortası" to 350.0,
                    "Ekstra 23kg Bagaj Hakkı" to 400.0,
                    "Akşam Yemeği Dahil Özel Paket" to 800.0
                )

                extras.forEach { (extraName, price) ->
                    val isChecked = uiState.selectedExtras.contains(extraName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = TourOSSpacing.xSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleExtraService(extraName, price) },
                            colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                        )
                        Text(text = extraName, style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextPrimary), modifier = Modifier.weight(1f))
                        Text(text = "+$price TRY", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscountCommissionStepContent(
    uiState: CreateBookingWizardUiState,
    viewModel: CreateBookingWizardViewModel
) {
    var codeInput by remember { mutableStateOf(uiState.couponCode) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
    ) {
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "İndirim Kodu / Promosyon Kuponu", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TourOSTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        placeholder = "Örn: PROMO10, VIP1000",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                    TourOSButton(
                        text = "Uygula",
                        onClick = { viewModel.applyCoupon(codeInput) },
                        variant = TourOSButtonVariant.SECONDARY
                    )
                }

                if (uiState.discountAmount > 0) {
                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                    TourOSStatusBadge(
                        text = "🎉 İndirim Uygulandı: -${uiState.discountAmount} TRY",
                        backgroundColor = TourOSColors.SuccessContainer,
                        textColor = TourOSColors.Success
                    )
                }
            }
        }

        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "Acente Komisyon Önizlemesi", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Acente Komisyon Oranı:", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary))
                    Text(text = "%${uiState.agencyCommissionRate}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                }
                Spacer(modifier = Modifier.height(TourOSSpacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Hesaplanan Komisyon Hak Hakedişi:", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary))
                    Text(text = "${uiState.agencyCommissionAmount} TRY", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                }
            }
        }
    }
}

@Composable
private fun ConfirmationStepContent(
    uiState: CreateBookingWizardUiState,
    viewModel: CreateBookingWizardViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
    ) {
        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "📋 Rezervasyon Özet Bilgileri", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Text(text = "Tur: ${uiState.selectedTour?.title ?: '-'}", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextPrimary))
                Text(text = "Kalkış Tarihi: ${uiState.selectedDeparture?.departureDate ?: '-'}", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                Text(text = "Otel & Oda: ${uiState.selectedHotel?.name ?: '-'} (${uiState.selectedRoomType?.name ?: '-'})", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                Text(text = "Kişiler: ${uiState.adultCount} Yetişkin, ${uiState.childCount} Çocuk, ${uiState.infantCount} Bebek", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                Text(text = "Ana Yolcu: ${uiState.leadPassengerName} (${uiState.leadPassengerPhone})", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
            }
        }

        item {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Text(text = "💰 Fiyat Detaylandırılması", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                SummaryRow(label = "Tur Taban Fiyatı", value = "${uiState.baseTourPrice} TRY")
                SummaryRow(label = "Otel Konaklama Toplamı", value = "${uiState.roomTotalPrice} TRY")
                SummaryRow(label = "Transfer Hizmeti", value = "${uiState.transferPrice} TRY")
                SummaryRow(label = "Ekstra Hizmetler", value = "${uiState.extrasTotalPrice} TRY")
                SummaryRow(label = "Kupon İndirimi", value = "-${uiState.discountAmount} TRY")
                HorizontalDivider(color = TourOSColors.Divider, modifier = Modifier.padding(vertical = TourOSSpacing.small))
                SummaryRow(label = "GENEL TOPLAM TUTAR", value = "${uiState.finalTotalPrice} TRY", isBold = true)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TourOSSpacing.xSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                    else TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
        )
        Text(
            text = value,
            style = if (isBold) TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                    else TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
        )
    }
}

@Composable
private fun StickySummaryBottomBar(
    uiState: CreateBookingWizardUiState,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Border,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TourOSButton(
                text = "‹ Geri",
                onClick = onPreviousClick,
                variant = TourOSButtonVariant.TERTIARY
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Genel Toplam Tutar", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Text(
                    text = "${uiState.finalTotalPrice} TRY",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
            }

            TourOSButton(
                text = if (uiState.currentStep == BookingWizardStep.CONFIRMATION) "✓ Rezervasyonu Onayla" else "İlerle ›",
                onClick = onNextClick,
                variant = TourOSButtonVariant.PRIMARY,
                enabled = uiState.canProceedNext,
                isLoading = uiState.isLoading
            )
        }
    }
}

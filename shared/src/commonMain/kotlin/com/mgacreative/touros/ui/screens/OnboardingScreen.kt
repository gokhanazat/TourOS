package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSDropdown
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

/**
 * TourOS 0.3 Tasarım Sistemine uygun Onboarding (Adım Adım Kurulum Akışı).
 * Adımlar:
 * 1. Firma Bilgisi
 * 2. Ayarlar
 * 3. Tamamlandı
 */
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) } // 1, 2, 3

    // Step 1 State: Firma Bilgileri
    var companyName by remember { mutableStateOf("") }
    var legalTitle by remember { mutableStateOf("") }
    var taxOffice by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Step 2 State: Sistem Ayarları
    val currencies = listOf("TRY (₺)", "USD ($)", "EUR (€)", "GBP (£)")
    var selectedCurrency by remember { mutableStateOf("TRY (₺)") }
    var defaultCommission by remember { mutableStateOf("10") }
    val timezones = listOf("Europe/Istanbul (UTC+3)", "Europe/London (UTC+0)", "America/New_York (UTC-5)")
    var selectedTimezone by remember { mutableStateOf("Europe/Istanbul (UTC+3)") }
    val languages = listOf("Türkçe", "English")
    var selectedLanguage by remember { mutableStateOf("Türkçe") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TourOSColors.Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(TourOSSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TourOSCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.xxLarge
            ) {
                // Stepper Header
                OnboardingStepIndicator(currentStep = currentStep)

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                when (currentStep) {
                    1 -> {
                        // ADIM 1: Firma Bilgisi
                        Text(
                            text = "Firma ve Acente Bilgileri",
                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                        )
                        Text(
                            text = "TourOS paneliniz için temel şirket profilinizi oluşturun.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                        TourOSTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = "Firma / Acente Adı *",
                            placeholder = "Örn: Anatolia Travel Operasyon",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        TourOSTextField(
                            value = legalTitle,
                            onValueChange = { legalTitle = it },
                            label = "Ticari Unvan",
                            placeholder = "Örn: Anatolia Turizm Hizmetleri A.Ş.",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            TourOSTextField(
                                value = taxOffice,
                                onValueChange = { taxOffice = it },
                                label = "Vergi Dairesi",
                                placeholder = "Karaköy V.D.",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                            TourOSTextField(
                                value = taxNumber,
                                onValueChange = { taxNumber = it },
                                label = "Vergi / T.C. No",
                                placeholder = "1234567890",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            TourOSTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Telefon Numarası",
                                placeholder = "+90 532 000 0000",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                            TourOSTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Kurumsal E-posta",
                                placeholder = "info@anatolia.com",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        TourOSTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Merkez Adres",
                            placeholder = "İstiklal Cad. No:100 Beyoğlu / İstanbul",
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.weight(1f))
                            TourOSButton(
                                text = "İleri: Ayarlar →",
                                onClick = { currentStep = 2 },
                                variant = TourOSButtonVariant.PRIMARY,
                                enabled = companyName.isNotBlank()
                            )
                        }
                    }

                    2 -> {
                        // ADIM 2: Ayarlar
                        Text(
                            text = "Operasyonel Sistem Ayarları",
                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                        )
                        Text(
                            text = "Rezervasyon, komisyon ve para birimi varsayılanlarınızı belirleyin.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.large))

                        TourOSDropdown(
                            items = currencies,
                            selectedItem = selectedCurrency,
                            onItemSelected = { selectedCurrency = it },
                            itemLabel = { it },
                            label = "Varsayılan Para Birimi",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        TourOSTextField(
                            value = defaultCommission,
                            onValueChange = { defaultCommission = it },
                            label = "Varsayılan Acente Komisyon Oranı (%)",
                            placeholder = "10",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        TourOSDropdown(
                            items = timezones,
                            selectedItem = selectedTimezone,
                            onItemSelected = { selectedTimezone = it },
                            itemLabel = { it },
                            label = "Zaman Dilimi (Timezone)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                        TourOSDropdown(
                            items = languages,
                            selectedItem = selectedLanguage,
                            onItemSelected = { selectedLanguage = it },
                            itemLabel = { it },
                            label = "Panel Dili",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TourOSButton(
                                text = "← Geri",
                                onClick = { currentStep = 1 },
                                variant = TourOSButtonVariant.TERTIARY
                            )
                            TourOSButton(
                                text = "Kurulumu Tamamla ✓",
                                onClick = { currentStep = 3 },
                                variant = TourOSButtonVariant.PRIMARY
                            )
                        }
                    }

                    3 -> {
                        // ADIM 3: Tamamlandı
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(TourOSColors.SuccessContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Success)
                                )
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            Text(
                                text = "Tebrikler! TourOS Kurulumu Tamamlandı",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                            )

                            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                            Text(
                                text = "Sisteminiz $companyName firması için başarıyla yapılandırıldı.",
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                            )

                            Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                            // Özet Kartı
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                    .background(TourOSColors.Surface)
                                    .border(TourOSSpacing.borderWidth, TourOSColors.Border)
                                    .padding(TourOSSpacing.large)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                                    SummaryRow("Firma Adı", companyName.ifBlank { "Anatolia Travel" })
                                    HorizontalDivider(color = TourOSColors.Divider)
                                    SummaryRow("Para Birimi", selectedCurrency)
                                    HorizontalDivider(color = TourOSColors.Divider)
                                    SummaryRow("Komisyon Oranı", "%$defaultCommission")
                                    HorizontalDivider(color = TourOSColors.Divider)
                                    SummaryRow("Zaman Dilimi", selectedTimezone)
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.xxLarge))

                            TourOSButton(
                                text = "Dashboard'a Git →",
                                onClick = onOnboardingComplete,
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepIndicator(currentStep: Int) {
    val steps = listOf("1. Firma Bilgisi", "2. Ayarlar", "3. Tamamlandı")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isActive = currentStep >= stepNumber
            val isCurrent = currentStep == stepNumber

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isActive) TourOSColors.Primary else TourOSColors.Surface)
                        .border(
                            width = TourOSSpacing.borderWidth,
                            color = if (isActive) TourOSColors.Primary else TourOSColors.Border,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentStep > stepNumber) "✓" else stepNumber.toString(),
                        style = TourOSTypography.Caption.copy(
                            color = if (isActive) TourOSColors.OnPrimary else TourOSColors.TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(TourOSSpacing.xSmall))

                Text(
                    text = title,
                    style = if (isCurrent) TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    else TourOSTypography.Caption.copy(color = if (isActive) TourOSColors.TextPrimary else TourOSColors.TextDisabled)
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = TourOSSpacing.small)
                        .background(if (currentStep > stepNumber) TourOSColors.Primary else TourOSColors.Border)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
        )
        Text(
            text = value,
            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
        )
    }
}

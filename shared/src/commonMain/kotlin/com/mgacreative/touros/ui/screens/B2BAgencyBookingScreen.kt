package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mgacreative.touros.ui.viewmodel.B2BAgencyBookingViewModel

/**
 * B2B Rezervasyon Oluşturma — TourOS 0.3
 *
 * Ana rezervasyon sihirbazıyla birebir aynı bileşenler (5 Adımlı Wizard İlerleme Çubuğu).
 * Sadece Üst Bar'da acente adı/logosu ve B2B cari hesap durumu gösterilir.
 */
@Composable
fun B2BAgencyBookingScreen(
    viewModel: B2BAgencyBookingViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(1) } // 1: Tur, 2: Pax, 3: Yolcu, 4: B2B Fiyat, 5: Onay
    var customerName by remember { mutableStateOf("Johann Schmidt") }
    var customerPhone by remember { mutableStateOf("+49 151 998877") }
    var customerEmail by remember { mutableStateOf("johann@germanytravel.de") }
    var notes by remember { mutableStateOf("Pencere kenarı otobüs koltuğu ricası.") }

    val stepTitles = listOf("1. Tur", "2. Pax", "3. Yolcu", "4. B2B Fiyat", "5. Onay")

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            // SADECE ÜST BAR'DA ACENTE ADI & LOGOSU GÖSTERİLİR (Strict Rule)
            TourOSTopBar(
                title = "B2B Rezervasyon Sihirbazı",
                subtitle = "🏢 Global Travel Agency A.Ş. (ACN-GLB) — Cari Limit: ₺ 120,000",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                },
                actions = {
                    TourOSStatusBadge(
                        text = "B2B CARİ LİMİT",
                        backgroundColor = TourOSColors.SuccessContainer,
                        textColor = TourOSColors.Success,
                        modifier = Modifier.padding(end = TourOSSpacing.small)
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // Bildirim Mesajı
            if (state.notificationMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }
            }

            // ── 1. ANA REZERVASYON SİHİRBAZI 5 ADIMLI İLERLEME ÇUBUĞU ──────────
            item {
                WizardStepIndicatorBar(
                    currentStep = currentStep,
                    steps = stepTitles,
                    onStepClick = { step -> currentStep = step }
                )
            }

            // ── 2. SEÇİLİ TUR VE OPERASYON KARTI ──────────────────────────────
            item {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.PrimaryContainer,
                    contentPadding = TourOSSpacing.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "🏔️ Tur: ${state.selectedDepartureTitle}",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                "Kalkış Tarihi: 15.08.2026  ·  Konfirmasyon: Anında Onay",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }

                        TourOSStatusBadge(
                            text = "%${state.commissionPercentage} Komisyon",
                            backgroundColor = TourOSColors.SecondaryContainer,
                            textColor = TourOSColors.Secondary
                        )
                    }
                }
            }

            // ── 3. MÜŞTERİ VE YOLCU BİLGİLERİ FORMU ───────────────────────────
            item {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Surface,
                    contentPadding = TourOSSpacing.large
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                        Text(
                            "📝 Yolcu & Müşteri İletişim Detayları",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                        )

                        TourOSTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = "Müşteri Adı Soyadı",
                            placeholder = "Johann Schmidt",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            TourOSTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = "Telefon No",
                                placeholder = "+49 151 998877",
                                modifier = Modifier.weight(1f)
                            )
                            TourOSTextField(
                                value = customerEmail,
                                onValueChange = { customerEmail = it },
                                label = "E-Posta",
                                placeholder = "johann@germanytravel.de",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Yolcu Sayısı (Pax Counter)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Yolcu Sayısı (Pax):",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.updatePaxCount(state.paxCount - 1) },
                                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                                ) {
                                    Text("-", style = TourOSTypography.TitleLarge)
                                }
                                Text(
                                    "${state.paxCount} Kişi",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                )
                                OutlinedButton(
                                    onClick = { viewModel.updatePaxCount(state.paxCount + 1) },
                                    shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                                ) {
                                    Text("+", style = TourOSTypography.TitleLarge)
                                }
                            }
                        }

                        TourOSTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = "Acente Özel Notları",
                            placeholder = "Pencere kenarı ricası...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── 4. B2B CARİ NET FİYAT HESAPLAMA KARTI ───────────────────────────
            item {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.SecondaryContainer.copy(alpha = 0.4f),
                    contentPadding = TourOSSpacing.large
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        Text(
                            "💰 B2B Cari Fiyat Hesaplama Özeti",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Brüt Liste Fiyatı (${state.paxCount} Pax):",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                            Text(
                                "₺ ${formatMoney(state.totalPrice)}",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Acente Komisyon İndirimi (%${state.commissionPercentage}):",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Success)
                            )
                            Text(
                                "-₺ ${formatMoney(state.commissionAmount)}",
                                style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                            )
                        }

                        HorizontalDivider(color = TourOSColors.Divider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Acentenin Ödeyeceği Net Cari Tutar:",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary)
                            )
                            Text(
                                "₺ ${formatMoney(state.netPayable)}",
                                style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                            )
                        }
                    }
                }
            }

            // ── 5. ONAY VE REZERVASYON KAYIT BUTONU ────────────────────────────
            item {
                TourOSButton(
                    text = "🚀 Rezervasyonu B2B Cari Limit İle Onayla (₺ ${formatMoney(state.netPayable)})",
                    onClick = {
                        if (customerName.isNotBlank()) {
                            viewModel.submitB2BBooking(customerName, customerPhone, customerEmail, notes)
                        }
                    },
                    enabled = !state.isLoading && customerName.isNotBlank(),
                    variant = TourOSButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Wizard Step Indicator Bar ────────────────────────────────────────────────

@Composable
private fun WizardStepIndicatorBar(
    currentStep: Int,
    steps: List<String>,
    onStepClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.xSmall)
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep

            val bg = when {
                isCompleted -> TourOSColors.SuccessContainer
                isActive -> TourOSColors.PrimaryContainer
                else -> TourOSColors.Background
            }
            val textColor = when {
                isCompleted -> TourOSColors.Success
                isActive -> TourOSColors.Primary
                else -> TourOSColors.TextSecondary
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(bg)
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) TourOSColors.Primary else TourOSColors.Border,
                        shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
                    )
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title,
                    style = TourOSTypography.Caption.copy(color = textColor),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}

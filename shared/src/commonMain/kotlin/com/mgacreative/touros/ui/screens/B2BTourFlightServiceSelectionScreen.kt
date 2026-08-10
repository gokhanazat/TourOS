package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel
import com.mgacreative.touros.ui.viewmodel.ExtraService
import com.mgacreative.touros.ui.viewmodel.FlightOption
import org.koin.compose.viewmodel.koinViewModel

/**
 * Görsel 3, 4, 9 & 10 Esintili Alternatif Uçuş & Ekstra Hizmetler (Sigorta/Transfer) Seçim Ekrani
 */
@Composable
fun B2BTourFlightServiceSelectionScreen(
    viewModel: B2BTourSearchViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onProceedToPassengerCheckout: () -> Unit = {}
) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val availableFlightOptions by viewModel.availableFlightOptions.collectAsState()
    val selectedFlightOption by viewModel.selectedFlightOption.collectAsState()
    val extraServices by viewModel.extraServices.collectAsState()

    val product = selectedProduct ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(AppLanguageManager.translate("Seçili tur bulunamadı."), style = TourOSTypography.BodyMedium)
        }
        return
    }

    val basePrice = remember(product.price) { product.price * 1.125 }
    val flightDelta = selectedFlightOption?.priceDeltaRub ?: 0.0
    val extrasTotalEur = remember(extraServices) {
        extraServices.filter { it.isSelected }.sumOf { it.unitPriceEur * it.paxCount }
    }
    val extrasTotalRub = remember(extrasTotalEur) { extrasTotalEur * 100.0 }
    val grandTotalRub = basePrice + flightDelta + extrasTotalRub

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("2. Adım: Uçuş & Hizmet Seçimi"),
                subtitle = "${product.hotelName} — ${product.region}",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
        ) {
            // ── SİHİRBAZ ÜST ADIM ÇUBUĞU (WIZARD STEP BAR - GÖRSEL 3 & 9) ───────────
            WizardStepHeaderBar(currentStep = 2)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // ── SEÇİLİ KONAKLAMA ÖZET KARTI (GÖRSEL 3 & 9) ────────────────────
                item {
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.PrimaryContainer.copy(alpha = 0.2f),
                        contentPadding = TourOSSpacing.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "🏨 ${product.hotelName} ⭐".repeat(product.hotelCategory.coerceAtMost(5)),
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🛏️ ${product.roomType.ifBlank { "FAMILY ROOM" }}  ·  🍽️ ${product.mealType.ifBlank { "Ultra All Inclusive" }}",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                                Text(
                                    text = "📅 21.08.26 - 28.08.26 (7 ${AppLanguageManager.translate("Gece")})  ·  👥 2 ADL + 2 CHD",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = AppLanguageManager.translate("Konaklama Fiyatı"),
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                                Text(
                                    text = "${basePrice.toInt()} ${product.currency}",
                                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ── UÇUŞ SEÇİM BÖLÜMÜ ("Найдено 8 рейсов" - GÖRSEL 9 & 10) ────────
                item {
                    Text(
                        text = "✈️ ${AppLanguageManager.translate("Uçuş Alternatifleri Seçimi")} (${availableFlightOptions.size} ${AppLanguageManager.translate("Uçuş Çifti Bulundu")})",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                }

                items(availableFlightOptions, key = { it.id }) { option ->
                    FlightOptionCardItem(
                        option = option,
                        isSelected = selectedFlightOption?.id == option.id,
                        onSelect = { viewModel.selectedFlightOption.value = option }
                    )
                }

                // ── EKSTRA HİZMETLER VE SİGORTALAR ("Основные и дополнительные услуги" - GÖRSEL 3 & 4) ───
                item {
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                    Text(
                        text = "🛡️ ${AppLanguageManager.translate("Zorunlu ve İsteğe Bağlı Ekstra Hizmetler")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                }

                items(extraServices, key = { it.id }) { srv ->
                    ExtraServiceCardItem(
                        service = srv,
                        onToggle = { viewModel.toggleExtraService(srv.id) }
                    )
                }
            }

            // ── GÖRSEL 3 ALT FİYAT KIRILIM VE İLERLEME ÇUBUĞU (FOOTER) ──────────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppLanguageManager.translate("Zorunlu Hizmetler"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                            Text(
                                text = "68 666 RUB",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = AppLanguageManager.translate("İsteğe Bağlı Ekstralar"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                            Text(
                                text = "${extrasTotalRub.toInt()} RUB",
                                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = AppLanguageManager.translate("TOPLAM SATIŞ TUTARI"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${grandTotalRub.toInt()} ${product.currency}",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TourOSButton(
                        text = "➡️ ${AppLanguageManager.translate("Devam Et (Yolcu Bilgileri)")}",
                        onClick = onProceedToPassengerCheckout,
                        variant = TourOSButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

// ─── ADIM ADIM İLERLEME ÇUBUĞU BİLEŞENİ (WIZARD STEP BAR) ──────────────────────

@Composable
fun WizardStepHeaderBar(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
    ) {
        listOf("1. OTEL", "2. UÇUŞ", "3. HİZMETLER", "4. TURİSTLER").forEachIndexed { idx, label ->
            val stepNo = idx + 1
            val isCompleted = stepNo < currentStep
            val isCurrent = stepNo == currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isCompleted -> TourOSColors.SuccessContainer
                            isCurrent -> TourOSColors.PrimaryContainer
                            else -> TourOSColors.Surface
                        }
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (isCompleted) "✓ " else ""}$label",
                    style = TourOSTypography.Caption.copy(
                        color = when {
                            isCompleted -> TourOSColors.Success
                            isCurrent -> TourOSColors.Primary
                            else -> TourOSColors.TextSecondary
                        }
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── UÇUŞ SEÇENEĞİ KART BİLEŞENİ (GÖRSEL 9 & 10) ───────────────────────────────

@Composable
private fun FlightOptionCardItem(
    option: FlightOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    TourOSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        backgroundColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.15f) else TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = TourOSColors.Primary)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "🛫 GİDİŞ: ${option.outboundAirline} (${option.outboundFlightNumber})  ·  ${option.outboundDeparturePort} ➔ ${option.outboundArrivalPort} (${option.outboundDuration})",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        text = "🛬 DÖNÜŞ: ${option.inboundAirline} (${option.inboundFlightNumber})  ·  ${option.inboundDeparturePort} ➔ ${option.inboundArrivalPort} (${option.inboundDuration})",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "🧳 El Bagajı: ${option.handBaggageKg}kg  ·  Kayıtlı Bagaj: ${option.baggageKg}kg",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (option.priceDeltaRub > 0) {
                    Text(
                        text = "+${option.priceDeltaRub.toInt()} RUB",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Warning),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Uçuş Farkı",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TourOSColors.SuccessContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Fark Yok (Pakete Dahil)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── EKSTRA HİZMET KART BİLEŞENİ (GÖRSEL 3 & 4) ────────────────────────────────

@Composable
private fun ExtraServiceCardItem(
    service: ExtraService,
    onToggle: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Switch(
                    checked = service.isSelected,
                    onCheckedChange = { if (!service.isMandatory) onToggle() },
                    enabled = !service.isMandatory,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TourOSColors.Surface,
                        checkedTrackColor = TourOSColors.Success
                    )
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = service.name,
                            style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        if (service.isMandatory) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TourOSColors.PrimaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Zorunlu",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Kişi Başı: ${service.unitPriceEur} EUR  ·  Toplam (${service.paxCount} Yolcu): ${(service.unitPriceEur * service.paxCount)} EUR",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            Text(
                text = "${(service.unitPriceEur * service.paxCount * 100).toInt()} RUB",
                style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

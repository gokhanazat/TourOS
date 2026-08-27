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
    productId: String = "",
    viewModel: B2BTourSearchViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onProceedToPassengerCheckout: () -> Unit = {}
) {
    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            viewModel.selectProductById(productId)
        }
    }

    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val availableFlightOptions by viewModel.availableFlightOptions.collectAsState()
    val selectedFlightOption by viewModel.selectedFlightOption.collectAsState()
    val extraServices by viewModel.extraServices.collectAsState()
    val adults by viewModel.adults.collectAsState()
    val childrenAges by viewModel.childrenAges.collectAsState()

    val product = selectedProduct ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(color = TourOSColors.Primary)
                Text(
                    text = AppLanguageManager.translate("Tur ve uçuş detayları hazırlanıyor..."),
                    style = TourOSTypography.BodyMedium.copy(color = Color(0xFF64748B))
                )
            }
        }
        return
    }

    val isFlightOnly = product.productType.equals("FLIGHT", ignoreCase = true) || product.flightNumber.isNotBlank() || product.tourName.contains("Uçuş", ignoreCase = true)
    val dynamicMultiplier = remember(adults, childrenAges, isFlightOnly) {
        B2BTourSearchViewModel.calculateMultiplier(adults, childrenAges, isFlightOnly)
    }
    val basePrice = remember(product.price, dynamicMultiplier) { product.price * dynamicMultiplier }
    val flightDelta = selectedFlightOption?.priceDeltaRub ?: 0.0

    val currencyRateToProduct = remember(product.currency) {
        when (product.currency.uppercase()) {
            "RUB" -> 100.0
            "TRY", "TL" -> 38.0
            "USD" -> 1.08
            else -> 1.0 // EUR
        }
    }
    val mandatoryExtrasInProductCurrency = remember(extraServices, currencyRateToProduct) {
        extraServices.filter { it.isSelected && it.isMandatory }.sumOf { (it.unitPriceEur * currencyRateToProduct) * it.paxCount }
    }
    val optionalExtrasInProductCurrency = remember(extraServices, currencyRateToProduct) {
        extraServices.filter { it.isSelected && !it.isMandatory }.sumOf { (it.unitPriceEur * currencyRateToProduct) * it.paxCount }
    }
    val extrasTotalInProductCurrency = mandatoryExtrasInProductCurrency + optionalExtrasInProductCurrency
    val grandTotal = basePrice + flightDelta + extrasTotalInProductCurrency

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("2. Adım: Uçuş & Hizmet Seçimi"),
                subtitle = "${product.hotelName} — ${product.region}  ·  🏢 ${product.safeOperatorName.ifBlank { "Coral Travel" }}",
                onNavigateBack = onNavigateBack
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
                    val starsStr = "⭐".repeat(product.safeHotelCategory.coerceIn(1, 5))
                    val displayHotelName = product.safeHotelName.ifBlank { product.safeTourName.ifBlank { "Holiday Inn Istanbul Airport" } }
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
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🏨 $displayHotelName $starsStr",
                                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    // Tur Operatörü Rozeti
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = TourOSColors.Primary.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TourOSColors.Primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🏢", fontSize = 11.sp)
                                            Text(
                                                text = "${AppLanguageManager.translate("Operatör")}: ${product.safeOperatorName.ifBlank { "Coral Travel" }}",
                                                style = TourOSTypography.Caption.copy(
                                                    color = TourOSColors.Primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "🛏️ ${product.roomType.ifBlank { "FAMILY ROOM" }}  ·  🍽️ ${product.mealType.ifBlank { "Ultra All Inclusive" }}",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                                val paxDesc = "$adults ADL" + (if (childrenAges.isNotEmpty()) " + ${childrenAges.size} CHD (${childrenAges.joinToString(",") { "${it}y" }})" else "")
                                Text(
                                    text = "📅 ${product.departureDate?.ifBlank { "21.08.2026" } ?: "21.08.2026"} (${product.nights} ${AppLanguageManager.translate("Gece")})  ·  👥 $paxDesc",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
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

                // ── EKSTRA HİZMETLER VE SİGORTALAR (COMPACT BİLEŞİK KART - GÖRSEL 3 & 4) ───
                item {
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                    Text(
                        text = "🛡️ ${AppLanguageManager.translate("Zorunlu ve İsteğe Bağlı Ekstra Hizmetler")}",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.Surface,
                        contentPadding = 8.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            extraServices.forEachIndexed { idx, srv ->
                                if (idx > 0) {
                                    HorizontalDivider(
                                        color = TourOSColors.Divider.copy(alpha = 0.3f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                CompactExtraServiceRow(
                                    service = srv,
                                    currency = product.currency,
                                    onToggle = { viewModel.toggleExtraService(srv.id) }
                                )
                            }
                        }
                    }
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
                                text = "${mandatoryExtrasInProductCurrency.toInt()} ${product.currency}",
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
                                text = "${optionalExtrasInProductCurrency.toInt()} ${product.currency}",
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
                                text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(grandTotal, decimals = false)} ${product.currency}",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
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
fun WizardStepHeaderBar(
    currentStep: Int,
    onStepClick: ((Int) -> Unit)? = null
) {
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
                    .clickable(enabled = onStepClick != null) {
                        onStepClick?.invoke(stepNo)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (isCompleted) "✓ " else ""}${AppLanguageManager.translate(label)}",
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
                        text = "+${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(option.priceDeltaRub, decimals = false)} RUB",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Warning),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppLanguageManager.translate("Uçuş Farkı"),
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
                            text = AppLanguageManager.translate("Fark Yok (Pakete Dahil)"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Success),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── GÖRSEL 10: UÇUŞ DETAYLARI VE ZORUNLU EK ÜCRETLER (MANDATORY SURCHARGES BREAKDOWN) ──
        if (isSelected) {
            Spacer(modifier = Modifier.height(TourOSSpacing.small))
            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(TourOSSpacing.small))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                    .padding(TourOSSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "⚡ ${AppLanguageManager.translate("Zorunlu Uçuş Farkları ve Ek Ücret Dökümü (Mandatory Surcharges)")}",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "• ${AppLanguageManager.translate("Dönem Uçuş Farkı (TURKISH AIRLINES)")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(text = "+34.333 RUB", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "• ${AppLanguageManager.translate("Sabah Gidiş Uçuş Ek Ücreti (02:05 VKO)")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(text = "+14.137 RUB", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "• ${AppLanguageManager.translate("Akşam Dönüş Uçuş Ek Ücreti (18:40 AYT)")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(text = "+18.176 RUB", style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "• ${AppLanguageManager.translate("Grup Havalimanı Transferi")}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(text = AppLanguageManager.translate("Dahil (Включен)"), style = TourOSTypography.Caption.copy(color = TourOSColors.Success), fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.3f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = AppLanguageManager.translate("Toplam Zorunlu Ek Ücretler:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary), fontWeight = FontWeight.Bold)
                    Text(text = "66.646 RUB", style = TourOSTypography.Label.copy(color = TourOSColors.Warning), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── COMPACT EKSTRA HİZMET SATIR BİLEŞENİ (GÖRSEL 3 & 4) ─────────────────────────

@Composable
private fun CompactExtraServiceRow(
    service: ExtraService,
    currency: String = "EUR",
    onToggle: () -> Unit
) {
    val conversionRate = when (currency.uppercase()) {
        "RUB" -> 100.0
        "TRY", "TL" -> 38.0
        "USD" -> 1.08
        else -> 1.0
    }
    val unitPriceInCurrency = service.unitPriceEur * conversionRate
    val totalPriceInCurrency = unitPriceInCurrency * service.paxCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = !service.isMandatory) { onToggle() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Switch(
                checked = service.isSelected,
                onCheckedChange = { if (!service.isMandatory) onToggle() },
                enabled = !service.isMandatory,
                modifier = Modifier.height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TourOSColors.Surface,
                    checkedTrackColor = TourOSColors.Success
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = service.name,
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary, fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (service.isMandatory) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TourOSColors.PrimaryContainer)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Zorunlu",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "${AppLanguageManager.translate("Kişi Başı")}: ${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(unitPriceInCurrency, decimals = false)} $currency  ·  ${AppLanguageManager.translate("Toplam")} (${service.paxCount} ${AppLanguageManager.translate("Yolcu")}): ${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(totalPriceInCurrency, decimals = false)} $currency",
                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 10.sp)
                )
            }
        }

        Text(
            text = "${com.mgacreative.touros.domain.util.KmpCurrencyFormatter.formatAmount(totalPriceInCurrency, decimals = false)} $currency",
            style = TourOSTypography.Label.copy(color = TourOSColors.Primary, fontSize = 13.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

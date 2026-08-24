package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2BTourSearchViewModel
import com.mgacreative.touros.ui.viewmodel.PassengerInfo
import org.koin.compose.viewmodel.koinViewModel

/**
 * Görsel 5 & 6 Esintili Adım Adım Yolcu (Turist) Bilgileri Formu ve Rezervasyon Onay Ekrani
 */
@Composable
fun B2BPassengerCheckoutWizardScreen(
    viewModel: B2BTourSearchViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onBookingSuccess: () -> Unit = {}
) {
    val passengers by viewModel.passengers.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()

    var showSuccessModal by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("4. Adım: Turist (Yolcu) Bilgileri"),
                subtitle = AppLanguageManager.translate("Pasaport ve iletişim bilgilerini eksiksiz giriniz"),
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
            // ── SİHİRBAZ ADIM ÇUBUĞU (ADIM 4: TURİSTLER) ───────────────────────────
            WizardStepHeaderBar(currentStep = 4)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                itemsIndexed(passengers) { idx, pax ->
                    PassengerFormCardItem(
                        passenger = pax,
                        paxIndex = idx + 1,
                        onUpdatePassenger = { updated ->
                            viewModel.passengers.value = viewModel.passengers.value.mapIndexed { i, old ->
                                if (i == idx) updated else old
                            }
                        }
                    )
                }
            }

            // ── ALT AKSİYON VE REZERVASYON TAMAMLAMA ÇUBUĞU ────────────────────────
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
                    Column {
                        Text(
                            text = AppLanguageManager.translate("Rezervasyon Durumu"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                        Text(
                            text = "⚡ ${AppLanguageManager.translate("Anında Onaylı Operatör Kaydı")}",
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        TourOSButton(
                            text = AppLanguageManager.translate("Taslak Olarak Kaydet"),
                            onClick = { showSuccessModal = true },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                        TourOSButton(
                            text = "🚀 ${AppLanguageManager.translate("Rezervasyonu Tamamla & Onayla")}",
                            onClick = { showSuccessModal = true },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            }
        }
    }

    // REZERVASYON BAŞARILI MODALI
    if (showSuccessModal) {
        AlertDialog(
            onDismissRequest = { showSuccessModal = false },
            title = {
                Text(
                    text = "🎉 ${AppLanguageManager.translate("Rezervasyon Başarıyla Oluşturuldu!")}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    Text(
                        text = "${AppLanguageManager.translate("PNR / Rezervasyon Kodu")}: B2B-PNR-${(100000..999999).random()}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppLanguageManager.translate("Turist bilgileri ve bilet konfirmasyonu operatör sistemine başarıyla aktarıldı."),
                        style = TourOSTypography.BodyMedium
                    )
                }
            },
            confirmButton = {
                TourOSButton(
                    text = AppLanguageManager.translate("Tamam & Kapat"),
                    onClick = {
                        showSuccessModal = false
                        onBookingSuccess()
                    },
                    variant = TourOSButtonVariant.PRIMARY
                )
            }
        )
    }
}

// ─── GÖRSEL 5 & 6 ESİNTİLİ YOLCU FORM KART BİLEŞENİ ───────────────────────────

@Composable
private fun PassengerFormCardItem(
    passenger: PassengerInfo,
    paxIndex: Int,
    onUpdatePassenger: (PassengerInfo) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            // BAŞLIK VE CİNSİYET SEÇİMİ (GÖRSEL 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${AppLanguageManager.translate("Turist")} $paxIndex ${if (passenger.isPayer) "(${AppLanguageManager.translate("Sipariş Veren Müşteri")})" else ""}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                    fontWeight = FontWeight.Bold
                )

                // CİNSİYET TOGGLE BUTONLARI (GÖRSEL 5 & 6)
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    FilterChip(
                        selected = passenger.gender == "MALE",
                        onClick = { onUpdatePassenger(passenger.copy(gender = "MALE")) },
                        label = { Text(AppLanguageManager.translate("Bay (Мужской)"), style = TourOSTypography.Caption) }
                    )
                    FilterChip(
                        selected = passenger.gender == "FEMALE",
                        onClick = { onUpdatePassenger(passenger.copy(gender = "FEMALE")) },
                        label = { Text(AppLanguageManager.translate("Bayan (Женский)"), style = TourOSTypography.Caption) }
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))

            // FORM SATIRI 1: AD, SOYAD, DOĞUM TARİHİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.firstName,
                        onValueChange = { onUpdatePassenger(passenger.copy(firstName = it)) },
                        label = AppLanguageManager.translate("Adı (Имя)"),
                        placeholder = "AHMET",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.lastName,
                        onValueChange = { onUpdatePassenger(passenger.copy(lastName = it)) },
                        label = AppLanguageManager.translate("Soyadı (Фамилия)"),
                        placeholder = "YILMAZ",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.birthDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(birthDate = it)) },
                        label = AppLanguageManager.translate("Doğum Tarihi (GG.AA.YYYY)"),
                        placeholder = "12.05.1985",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // FORM SATIRI 2: UYRUK, PASAPORT NO, GEÇERLİLİK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.citizenship,
                        onValueChange = { onUpdatePassenger(passenger.copy(citizenship = it)) },
                        label = AppLanguageManager.translate("Uyruk (Гражданство)"),
                        placeholder = "Türkiye",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.passportNumber,
                        onValueChange = { onUpdatePassenger(passenger.copy(passportNumber = it)) },
                        label = AppLanguageManager.translate("Pasaport No (Номер)"),
                        placeholder = "84920492",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.documentExpiryDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentExpiryDate = it)) },
                        label = AppLanguageManager.translate("Son Geçerlilik (Срок действия)"),
                        placeholder = "12.05.2030",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // FORM SATIRI 3: İLETİŞİM BİLGİLERİ (Sadece 1. Turist için)
            if (passenger.isPayer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = passenger.phone,
                            onValueChange = { onUpdatePassenger(passenger.copy(phone = it)) },
                            label = AppLanguageManager.translate("Telefon No"),
                            placeholder = "+90 532 100 2030",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(modifier = Modifier.weight(1.5f)) {
                        TourOSTextField(
                            value = passenger.email,
                            onValueChange = { onUpdatePassenger(passenger.copy(email = it)) },
                            label = AppLanguageManager.translate("E-posta Adresi"),
                            placeholder = "ahmet@gmail.com",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // ── GÖRSEL 6: ÇOCUK/BEBEK YOLCU ÖZEL ALANLARI ("Ответственный за ребенка") ──
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.5f))
                        .padding(TourOSSpacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👨‍👦 ${AppLanguageManager.translate("Çocuktan Sorumlu Yetişkin (Ответственный за ребенка)")}:",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Turist 1 (Yetişkin / Lead)",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = passenger.isInfantSeatRequested,
                            onCheckedChange = { isChecked ->
                                onUpdatePassenger(passenger.copy(isInfantSeatRequested = isChecked))
                            },
                            colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                        )
                        Text(
                            text = AppLanguageManager.translate("İnfant İçin Uçakta Ayrı Koltuk"),
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

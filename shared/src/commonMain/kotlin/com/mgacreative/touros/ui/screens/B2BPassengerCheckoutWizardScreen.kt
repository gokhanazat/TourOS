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
import androidx.compose.ui.unit.sp
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
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val passengers by viewModel.passengers.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()

    var showSuccessModal by remember { mutableStateOf(false) }
    var createdPnrCode by remember { mutableStateOf("") }
    val isSaving by viewModel.isSavingBooking.collectAsState()

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
                            onClick = {
                                viewModel.confirmBookingAndSaveToSupabase { pnr ->
                                    createdPnrCode = pnr
                                    showSuccessModal = true
                                }
                            },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                        TourOSButton(
                            text = if (isSaving) "⏳ ${AppLanguageManager.translate("Kaydediliyor...")}" else "🚀 ${AppLanguageManager.translate("Rezervasyonu Tamamla & Onayla")}",
                            onClick = {
                                if (!isSaving) {
                                    viewModel.confirmBookingAndSaveToSupabase { pnr ->
                                        createdPnrCode = pnr
                                        showSuccessModal = true
                                    }
                                }
                            },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            }
        }
    }

    val bookingErrorMessage by viewModel.bookingErrorMessage.collectAsState()

    // REZERVASYON HATA MODALI
    if (bookingErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.bookingErrorMessage.value = null },
            title = {
                Text(
                    text = "❌ Rezervasyon Kaydedilemedi",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Error),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = bookingErrorMessage.orEmpty(),
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                )
            },
            confirmButton = {
                TourOSButton(
                    text = "Kapat",
                    onClick = { viewModel.bookingErrorMessage.value = null },
                    variant = TourOSButtonVariant.PRIMARY
                )
            }
        )
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
                        text = "${AppLanguageManager.translate("PNR / Rezervasyon Kodu")}: ${createdPnrCode.ifBlank { "B2B-PNR-100001" }}",
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
            // BAŞLIK, SİPARİŞ VEREN VE FOTOĞRAF / OCR BUTONLARI (TO FORMATI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "👤 Туристы $paxIndex",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = passenger.isPayer,
                            onCheckedChange = { onUpdatePassenger(passenger.copy(isPayer = it)) },
                            colors = CheckboxDefaults.colors(checkedColor = TourOSColors.Primary)
                        )
                        Text(
                            text = "Является заказчиком",
                            style = TourOSTypography.Caption.copy(fontWeight = FontWeight.SemiBold, color = TourOSColors.TextPrimary)
                        )
                    }
                }

                // GALERİDEN SEÇ / FOTOĞRAF ÇEK BUTONLARI & CİNSİYET
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { /* OCR / Galeri */ },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("📁 ВЫБРАТЬ ИЗ ГАЛЕРЕИ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { /* Kamera */ },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("📷 СДЕЛАТЬ ФОТО", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    // CİNSİYET SEÇİMİ
                    FilterChip(
                        selected = passenger.gender == "MALE",
                        onClick = { onUpdatePassenger(passenger.copy(gender = "MALE")) },
                        label = { Text("Мужской", style = TourOSTypography.Caption.copy(fontWeight = if (passenger.gender == "MALE") FontWeight.Bold else FontWeight.Normal)) }
                    )
                    FilterChip(
                        selected = passenger.gender == "FEMALE",
                        onClick = { onUpdatePassenger(passenger.copy(gender = "FEMALE")) },
                        label = { Text("Женский", style = TourOSTypography.Caption.copy(fontWeight = if (passenger.gender == "FEMALE") FontWeight.Bold else FontWeight.Normal)) }
                    )
                }
            }

            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))

            // ─── SATIR 1: ИМЯ, ФАМИЛИЯ, ДАТА РОЖДЕНИЯ ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1.2f)) {
                    TourOSTextField(
                        value = passenger.firstName,
                        onValueChange = { onUpdatePassenger(passenger.copy(firstName = it.uppercase())) },
                        label = "ИМЯ (Adı)",
                        placeholder = "IVAN",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1.2f)) {
                    TourOSTextField(
                        value = passenger.lastName,
                        onValueChange = { onUpdatePassenger(passenger.copy(lastName = it.uppercase())) },
                        label = "ФАМИЛИЯ (Soyadı)",
                        placeholder = "IVANOV",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.birthDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(birthDate = com.mgacreative.touros.utils.DateUtils.formatDateInput(it))) },
                        label = "ДАТА РОЖДЕНИЯ (Doğum Tarihi)",
                        placeholder = "ДД.ММ.ГГГГ",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ─── SATIR 2: ГРАЖДАНСТВО, ДОКУМЕНТ, СЕРИЯ, НОМЕР, ДАТА ВЫДАЧИ ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.citizenship,
                        onValueChange = { onUpdatePassenger(passenger.copy(citizenship = it.uppercase())) },
                        label = "ГРАЖДАНСТВО (Vatandaşlık)",
                        placeholder = "Россия",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.documentType,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentType = it)) },
                        label = "ДОКУМЕНТ (Belge)",
                        placeholder = "Загранпаспорт",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(0.7f)) {
                    TourOSTextField(
                        value = passenger.passportSeries,
                        onValueChange = { onUpdatePassenger(passenger.copy(passportSeries = it.uppercase())) },
                        label = "СЕРИЯ (Seri)",
                        placeholder = "51",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.passportNumber,
                        onValueChange = { onUpdatePassenger(passenger.copy(passportNumber = it.uppercase())) },
                        label = "НОМЕР (No)",
                        placeholder = "1234567",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1.1f)) {
                    TourOSTextField(
                        value = passenger.documentIssueDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentIssueDate = com.mgacreative.touros.utils.DateUtils.formatDateInput(it))) },
                        label = "ДАТА ВЫДАЧИ (Veriliş)",
                        placeholder = "ДД.ММ.ГГГГ",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ─── SATIR 3: СРОК ДЕЙСТВИЯ, КЕМ ВЫДАН, ТЕЛЕФОН, EMAIL ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Column(modifier = Modifier.weight(1.1f)) {
                    TourOSTextField(
                        value = passenger.documentExpiryDate,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentExpiryDate = com.mgacreative.touros.utils.DateUtils.formatDateInput(it))) },
                        label = "СРОК ДЕЙСТВИЯ (Bitiş)",
                        placeholder = "ДД.ММ.ГГГГ",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Минимальный срок: +6 месяцев",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 9.sp)
                    )
                }
                Box(modifier = Modifier.weight(1.2f)) {
                    TourOSTextField(
                        value = passenger.documentIssuedBy,
                        onValueChange = { onUpdatePassenger(passenger.copy(documentIssuedBy = it)) },
                        label = "КЕМ ВЫДАН (Veren Makam)",
                        placeholder = "МВД 77001",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1.1f)) {
                    TourOSTextField(
                        value = passenger.phone,
                        onValueChange = { onUpdatePassenger(passenger.copy(phone = it)) },
                        label = "НОМЕР ТЕЛЕФОНА (Telefon)",
                        placeholder = "+7 999 123 45 67",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(1.2f)) {
                    TourOSTextField(
                        value = passenger.email,
                        onValueChange = { onUpdatePassenger(passenger.copy(email = it)) },
                        label = "ЭЛ. ПОЧТА СЧЕТА (E-posta)",
                        placeholder = "tourist@mail.ru",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ─── SATIR 4: СТРАНА РОЖДЕНИЯ, АДРЕС ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(
                        value = passenger.birthCountry,
                        onValueChange = { onUpdatePassenger(passenger.copy(birthCountry = it)) },
                        label = "СТРАНА РОЖДЕНИЯ (Doğum Ülkesi)",
                        placeholder = "Россия",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(modifier = Modifier.weight(2f)) {
                    TourOSTextField(
                        value = passenger.address,
                        onValueChange = { onUpdatePassenger(passenger.copy(address = it)) },
                        label = "АДРЕС (İkamet Adresi)",
                        placeholder = "г. Москва, ул. Ленина, д. 10, кв. 5",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

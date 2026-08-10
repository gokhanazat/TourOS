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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mgacreative.touros.utils.rememberFilePickerLauncher
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSStatusBadge
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.HotelFormViewModel
import com.mgacreative.touros.ui.viewmodel.HotelPeriodItem
import com.mgacreative.touros.ui.viewmodel.PeriodRoomItem
import com.mgacreative.touros.ui.viewmodel.STANDARD_ROOM_TYPES

/**
 * 2.3.1 TourOS 0.3 Otel Kayıt / Düzenleme Formu Screen.
 * - Kapak Görseli Yükleme/Seçme + Canlı Görsel Önizlemesi.
 * - 2 Tarih Arası Periyot Yapısı (Birden Fazla Periyot Ekleme).
 * - Periyot Altında Standard Oda Tipleri (Kontenjan, Maliyet ₺, Satış ₺).
 */
@Composable
fun HotelFormScreen(
    viewModel: HotelFormViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val coverPickerLauncher = rememberFilePickerLauncher(
        mimeType = "image/*"
    ) { fileName, _ ->
        viewModel.updateCoverImageUrl(fileName)
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = if (uiState.hotelId.isNullOrBlank()) "🏨 Yeni Otel Kaydı" else "🏨 Otel Bilgilerini Düzenle",
                subtitle = "Konaklama tesisi detayları, görsel seçimi, periyotlar ve oda fiyatlandırması",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        },
        containerColor = TourOSColors.Surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TourOSSpacing.large)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // ── 1. TEMEL OTEL BİLGİLERİ ───────────────────────────────────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        "🏨 Temel Otel Bilgileri",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )

                    TourOSTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = "Otel Adı *",
                        placeholder = "Örn: Grand Cave Resort & Spa",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        TourOSTextField(
                            value = uiState.city,
                            onValueChange = { viewModel.updateCity(it) },
                            label = "Şehir *",
                            placeholder = "Örn: Nevşehir / Ürgüp",
                            modifier = Modifier.weight(1f)
                        )

                        TourOSTextField(
                            value = uiState.country,
                            onValueChange = { viewModel.updateCountry(it) },
                            label = "Ülke",
                            placeholder = "Türkiye",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Yıldız Derecesi Seçimi (1-5 Yıldız)
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        Text(
                            "Otel Yıldız Derecesi: ${uiState.starRating} Yıldız ⭐",
                            style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            (1..5).forEach { rating ->
                                val isSelected = uiState.starRating == rating
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateStarRating(rating) },
                                    label = { Text("$rating ⭐", style = TourOSTypography.BodyMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                        selectedLabelColor = TourOSColors.Primary
                                    )
                                )
                            }
                        }
                    }

                    // Pansiyon Tipi Seçimi
                    Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                        Text(
                            "Pansiyon Tipi (Konaklama Konsepti):",
                            style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            listOf("RO" to "Sadece Oda", "BB" to "Oda Kahvaltı", "HB" to "Yarım Pansiyon", "FB" to "Tam Pansiyon", "AI" to "Her Şey Dahil").forEach { (code, label) ->
                                val isSelected = uiState.boardType == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateBoardType(code) },
                                    label = { Text(label, style = TourOSTypography.BodyMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TourOSColors.PrimaryContainer,
                                        selectedLabelColor = TourOSColors.Primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. KAPAK FOTOĞRAFI & TANITIM AÇIKLAMASI ──────────────────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        "🖼️ Kapak Fotoğrafı & Görsel Seçimi",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TourOSTextField(
                            value = uiState.coverImageUrl,
                            onValueChange = { viewModel.updateCoverImageUrl(it) },
                            label = "Görsel URL veya Dosya Yolu",
                            placeholder = "https://images.unsplash.com/... veya C:/dosya.jpg",
                            modifier = Modifier.weight(1f)
                        )

                        TourOSButton(
                            text = "📁 Görsel Seç",
                            onClick = { coverPickerLauncher() },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                    }

                    // Canlı Önizleme Kutusu
                    if (uiState.coverImageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                .background(TourOSColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = uiState.coverImageUrl,
                                contentDescription = "Kapak Görseli Önizleme",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    TourOSTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = "Otel Tanıtım Açıklaması",
                        placeholder = "Otel imkanları, konumu, vadi manzarası ve detaylar...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── 3. OTEL PERİYOTLARI VE ODA TİPLERİ FİYATLANDIRMASI ───────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "📅 Otel Periyotları & Oda Fiyatlandırması",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            Text(
                                "2 Tarih arası periyotlar tanımlayarak oda bazlı kontenjan, maliyet ve satış fiyatı girin",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }

                        TourOSButton(
                            text = "+ Yeni Periyot Ekle",
                            onClick = { viewModel.addPeriod() },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }

                    uiState.periods.forEachIndexed { pIndex, period ->
                        PeriodItemCard(
                            period = period,
                            periodIndex = pIndex + 1,
                            onUpdateHeader = { name, start, end -> viewModel.updatePeriodHeader(period.id, name, start, end) },
                            onRemovePeriod = { viewModel.removePeriod(period.id) },
                            onAddRoom = { viewModel.addRoomToPeriod(period.id) },
                            onRemoveRoom = { roomId -> viewModel.removeRoomFromPeriod(period.id, roomId) },
                            onUpdateRoom = { roomId, name, allotment, cost, sale ->
                                viewModel.updatePeriodRoom(period.id, roomId, name, allotment, cost, sale)
                            }
                        )
                    }
                }
            }

            // ── 4. İLETİŞİM VE ADRES BİLGİLERİ ────────────────────────────────
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.large
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Text(
                        "📞 İletişim & Adres Bilgileri",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )

                    TourOSTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        label = "Açık Adres",
                        placeholder = "Göreme Mah. Müze Cad. No:12 Nevşehir",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        TourOSTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updatePhone(it) },
                            label = "Telefon",
                            placeholder = "+90 384 271 2000",
                            modifier = Modifier.weight(1f)
                        )

                        TourOSTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = "E-posta",
                            placeholder = "rezervasyon@otel.com",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TourOSTextField(
                        value = uiState.website,
                        onValueChange = { viewModel.updateWebsite(it) },
                        label = "Web Sitesi",
                        placeholder = "https://www.otel.com",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── KAYIT BAŞARI VE HATA BİLDİRİMLERİ ─────────────────────────────
            if (uiState.isSavedSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.SuccessContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Text(
                        "🎉 Otel ve periyot oda fiyatlandırması başarıyla kaydedildi!",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                    )
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.ErrorContainer)
                        .padding(TourOSSpacing.medium)
                ) {
                    Text(
                        "⚠️ Hata: ${uiState.errorMessage}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Error)
                    )
                }
            }

            // ── KAYDET BUTONU ──────────────────────────────────────────────────
            TourOSButton(
                text = "💾 Otel Kaydını Tamamla",
                onClick = { viewModel.saveHotel() },
                variant = TourOSButtonVariant.PRIMARY,
                enabled = !uiState.isLoading && uiState.name.isNotBlank() && uiState.city.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}

// ─── PERİYOT VE KONTENJAN ODA TİPLERİ BİLEŞENİ ─────────────────────────────

@Composable
private fun PeriodItemCard(
    period: HotelPeriodItem,
    periodIndex: Int,
    onUpdateHeader: (name: String, start: String, end: String) -> Unit,
    onRemovePeriod: () -> Unit,
    onAddRoom: () -> Unit,
    onRemoveRoom: (roomId: String) -> Unit,
    onUpdateRoom: (roomId: String, roomTypeName: String, allotment: Int, costPrice: Double, salePrice: Double) -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        borderColor = TourOSColors.Primary.copy(alpha = 0.4f),
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
            // PERİYOT BAŞLIĞI VEYA TARİH ALANLARI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📆 Periyot #$periodIndex: ${period.periodName}",
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                )

                IconButton(onClick = onRemovePeriod) {
                    Text("🗑️", style = TourOSTypography.TitleMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                TourOSTextField(
                    value = period.periodName,
                    onValueChange = { onUpdateHeader(it, period.startDate, period.endDate) },
                    label = "Periyot Adı",
                    placeholder = "Örn: Yaz Sezonu",
                    modifier = Modifier.weight(1.2f)
                )

                TourOSTextField(
                    value = period.startDate,
                    onValueChange = { onUpdateHeader(period.periodName, it, period.endDate) },
                    label = "Başlangıç Tarihi",
                    placeholder = "2026-06-01",
                    modifier = Modifier.weight(1f)
                )

                TourOSTextField(
                    value = period.endDate,
                    onValueChange = { onUpdateHeader(period.periodName, period.startDate, it) },
                    label = "Bitiş Tarihi",
                    placeholder = "2026-09-30",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // ODA TİPLERİ TABLO / LİSTESİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🛏️ Oda Tipleri, Kontenjan, Maliyet & Satış",
                    style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
                )

                TourOSButton(
                    text = "+ Oda Tipi Ekle",
                    onClick = onAddRoom,
                    variant = TourOSButtonVariant.SECONDARY
                )
            }

            period.rooms.forEach { room ->
                PeriodRoomRow(
                    room = room,
                    onRemove = { onRemoveRoom(room.id) },
                    onUpdate = { name, allotment, cost, sale ->
                        onUpdateRoom(room.id, name, allotment, cost, sale)
                    }
                )
            }
        }
    }
}

@Composable
private fun PeriodRoomRow(
    room: PeriodRoomItem,
    onRemove: () -> Unit,
    onUpdate: (roomTypeName: String, allotment: Int, costPrice: Double, salePrice: Double) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
            .background(TourOSColors.Background)
            .padding(TourOSSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Oda Tipi Seçim Dropdown
        Box(modifier = Modifier.weight(2f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .border(1.dp, TourOSColors.Border, RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .clickable { isDropdownExpanded = true }
                    .padding(horizontal = TourOSSpacing.medium),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    room.roomTypeName,
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                )
            }

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                STANDARD_ROOM_TYPES.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type, style = TourOSTypography.BodyMedium) },
                        onClick = {
                            onUpdate(type, room.allotment, room.costPrice, room.salePrice)
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Kontenjan
        TourOSTextField(
            value = if (room.allotment == 0) "" else room.allotment.toString(),
            onValueChange = { val alt = it.toIntOrNull() ?: 0; onUpdate(room.roomTypeName, alt, room.costPrice, room.salePrice) },
            label = "Kontenjan",
            placeholder = "10",
            modifier = Modifier.weight(1f)
        )

        // Maliyet (₺)
        TourOSTextField(
            value = if (room.costPrice == 0.0) "" else if (room.costPrice % 1.0 == 0.0) room.costPrice.toLong().toString() else room.costPrice.toString(),
            onValueChange = { val cost = it.toDoubleOrNull() ?: 0.0; onUpdate(room.roomTypeName, room.allotment, cost, room.salePrice) },
            label = "Maliyet (₺)",
            placeholder = "1500",
            modifier = Modifier.weight(1.2f)
        )

        // Satış (₺)
        TourOSTextField(
            value = if (room.salePrice == 0.0) "" else if (room.salePrice % 1.0 == 0.0) room.salePrice.toLong().toString() else room.salePrice.toString(),
            onValueChange = { val sale = it.toDoubleOrNull() ?: 0.0; onUpdate(room.roomTypeName, room.allotment, room.costPrice, sale) },
            label = "Satış (₺)",
            placeholder = "2500",
            modifier = Modifier.weight(1.2f)
        )

        IconButton(onClick = onRemove) {
            Text("✕", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Error))
        }
    }
}

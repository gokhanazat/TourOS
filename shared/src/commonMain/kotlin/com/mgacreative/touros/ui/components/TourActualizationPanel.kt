package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.data.tourvisor.TourActualizationDto
import com.mgacreative.touros.data.tourvisor.TourActualizationState
import com.mgacreative.touros.data.tourvisor.TourActualizationStore
import com.mgacreative.touros.data.tourvisor.TvFlightLeg
import com.mgacreative.touros.data.tourvisor.TvFlightSet
import com.mgacreative.touros.data.tourvisor.tourvisorTourIdOf
import com.mgacreative.touros.domain.util.KmpCurrencyFormatter
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import org.koin.compose.koinInject

/**
 * Rezervasyon adımı: Tourvisor'dan canlı tur detayı.
 *  - Gerçek gidiş-dönüş uçuşları (actdetail.php)
 *  - Tura dahil olanlar / dahil olmayanlar (tourinfo.contents + flags)
 *  - Zorunlu ek ödemeler (addpayments) ve vize ücreti (visacharge) — kişi başı × yolcu sayısı
 *  - Otel / uçuş yer durumu, tarifeli uçuş uyarısı, operatör sistemine bağlantı
 *
 * Bilgi alınamazsa rezervasyon durmaz; panel "TO'dan onay alın" uyarısı gösterir.
 * Search modülüne ait değildir; sadece seçili ürünü okur.
 */
@Composable
fun TourActualizationPanel(
    product: UnifiedProductEntity,
    paxCount: Int,
    modifier: Modifier = Modifier
) {
    if (tourvisorTourIdOf(product.id) == null) return // Tourvisor ürünü değil (yerel tur/otel)

    val store: TourActualizationStore = koinInject()
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    LaunchedEffect(product.id) { store.load(product.id) }
    val state by remember(product.id) { store.stateFor(product.id) }.collectAsState()

    // currentLanguage okunarak dil değişince panel yeniden çizilir
    val lang = currentLanguage.code

    TourOSCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Background,
        borderColor = TourOSColors.Primary.copy(alpha = 0.35f),
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Text(
                text = tx(lang, "Tur detayı — operatörden canlı bilgi", "Детали тура — онлайн у туроператора"),
                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )

            if (!product.isCharter) {
                InfoLine(
                    text = tx(lang, "Tarifeli uçuş: tura ek ödeme çıkabilir.", "Регулярный рейс: возможны доплаты к туру."),
                    color = TourOSColors.Warning,
                    background = TourOSColors.WarningContainer
                )
            }

            when (val s = state) {
                is TourActualizationState.NotApplicable -> Unit
                is TourActualizationState.Loading -> LoadingRow(lang)
                is TourActualizationState.Unavailable -> UnavailableBlock(lang, s, onRetry = { store.load(product.id, force = true) })
                is TourActualizationState.Ready -> ReadyBlock(lang, s.data, product, paxCount)
            }

            Text(
                text = tx(
                    lang,
                    "Rezervasyonu tur operatörünün sisteminde talep olarak girin. Bu bilgiler bilgilendirme amaçlıdır; kesin fiyat ve uçuş operatör onayıyla belirlenir.",
                    "Бронирование оформляется заявкой в системе туроператора. Данные носят информационный характер; окончательные цена и рейсы — после подтверждения туроператора."
                ),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )
        }
    }
}

/**
 * Ekranların toplam tutara ekleyebileceği zorunlu ek ödemeler (ek ödemeler + vize), RUB.
 * Ürün para birimi RUB değilse 0 döner (kur dönüşümü uydurulmaz; panel tutarı ayrıca gösterir).
 */
@Composable
fun rememberTourvisorMandatoryExtras(product: UnifiedProductEntity?, paxCount: Int): Double {
    if (product == null || tourvisorTourIdOf(product.id) == null) return 0.0
    val store: TourActualizationStore = koinInject()
    val state by remember(product.id) { store.stateFor(product.id) }.collectAsState()
    val data = (state as? TourActualizationState.Ready)?.data ?: return 0.0
    if (!product.currency.equals("RUB", ignoreCase = true)) return 0.0
    return data.mandatoryExtrasTotal(paxCount)
}

// ─── Bölümler ─────────────────────────────────────────────────────────────────

@Composable
private fun LoadingRow(lang: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = TourOSColors.Primary)
        Text(
            text = tx(lang, "Operatörden güncel bilgi alınıyor…", "Получаем актуальные данные у туроператора…"),
            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
        )
    }
}

@Composable
private fun UnavailableBlock(lang: String, s: TourActualizationState.Unavailable, onRetry: () -> Unit) {
    val reasonText = when (s.reason) {
        "disabled" -> tx(lang, "Canlı tur detayı şu an kapalı.", "Онлайн-детали тура сейчас отключены.")
        "quota_exceeded" -> tx(lang, "Günlük canlı sorgu sınırı doldu.", "Дневной лимит онлайн-запросов исчерпан.")
        "timeout" -> tx(lang, "Operatör zamanında yanıt vermedi.", "Туроператор не ответил вовремя.")
        else -> if (s.message?.contains("obsolete", ignoreCase = true) == true) {
            tx(lang, "Bu tur operatörde artık geçerli değil (fiyat veya yer durumu değişmiş olabilir).", "Тур больше неактуален у туроператора (цена или наличие мест могли измениться).")
        } else {
            tx(lang, "Bu tur için canlı bilgi alınamadı.", "Не удалось получить актуальные данные по туру.")
        }
    }
    InfoLine(
        text = "$reasonText ${tx(lang, "Uçuş saatleri, ek ödemeler ve fiyat için TO'dan onay alın.", "Рейсы, доплаты и цену уточните у туроператора.")}",
        color = TourOSColors.Warning,
        background = TourOSColors.WarningContainer
    )
    if (s.reason != "disabled" && s.reason != "quota_exceeded") {
        TourOSButton(
            text = tx(lang, "Tekrar dene", "Повторить"),
            onClick = onRetry,
            variant = TourOSButtonVariant.SECONDARY
        )
    }
}

@Composable
private fun ReadyBlock(lang: String, d: TourActualizationDto, product: UnifiedProductEntity, paxCount: Int) {
    val uriHandler = LocalUriHandler.current
    val pax = paxCount.coerceAtLeast(1)

    // Güncel fiyat kontrolü (Tourvisor fiyatı RUB)
    val livePrice = d.price
    if (livePrice != null && livePrice > 0 && product.currency.equals("RUB", ignoreCase = true)) {
        val diff = livePrice - product.price
        val placement = listOfNotNull(
            d.adults?.let { "$it ${tx(lang, "yetişkin", "взр.")}" },
            d.child?.takeIf { it > 0 }?.let { "$it ${tx(lang, "çocuk", "реб.")}" }
        ).joinToString(" + ")
        val base = "${tx(lang, "Operatördeki güncel fiyat", "Актуальная цена у туроператора")}: ${money(livePrice)} RUB" +
            (if (placement.isNotBlank()) " ($placement)" else "")
        if (kotlin.math.abs(diff) >= 1.0) {
            InfoLine(
                text = "$base — ${tx(lang, "listedeki fiyat", "цена в списке")}: ${money(product.price)} RUB",
                color = if (diff > 0) TourOSColors.Warning else TourOSColors.Success,
                background = if (diff > 0) TourOSColors.WarningContainer else TourOSColors.SuccessContainer
            )
        } else {
            InfoLine(text = "$base ✓", color = TourOSColors.Success, background = TourOSColors.SuccessContainer)
        }
    }

    // Yer durumu
    val statusLines = buildList {
        when (d.hotelStatus) {
            1 -> add(tx(lang, "Otel: talep üzerine", "Отель: под запрос") to TourOSColors.Warning)
            2 -> add(tx(lang, "Otel: anında onay", "Отель: моментальное подтверждение") to TourOSColors.Success)
        }
        if (d.onRequest == true || d.flightStatus == 1) {
            add(tx(lang, "Uçuş: yerler talep üzerine", "Перелёт: места под запрос") to TourOSColors.Warning)
        } else if (d.flightStatus == 2) {
            add(tx(lang, "Uçuş: az yer kaldı", "Перелёт: мало мест") to TourOSColors.Warning)
        }
        d.nightFlight?.takeIf { it > 0 }?.let {
            add("${tx(lang, "Gece uçuşu, yolda geçen gece", "Ночной перелёт, ночей в пути")}: $it" to TourOSColors.TextSecondary)
        }
    }
    if (statusLines.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            statusLines.forEach { (text, color) -> Chip(text, color) }
        }
    }

    // Uçuşlar
    SectionTitle(tx(lang, "Uçuşlar", "Перелёт"))
    // Varsayılan set en üstte, alternatifler fiyata göre artan; ilk 5 gösterilir, kalanı butonla açılır
    val allSets = d.safeFlights.sortedWith(compareByDescending<TvFlightSet> { it.isDefault }.thenBy { it.price ?: Double.MAX_VALUE })
    var showAllSets by remember(d.tourId) { mutableStateOf(false) }
    val sets = if (showAllSets) allSets else allSets.take(VISIBLE_FLIGHT_SETS)
    when {
        d.safeFlags.noflight -> Text(
            text = tx(lang, "Bu turda uçuş yok (sadece otel).", "Перелёт не входит в тур (только отель)."),
            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
        )
        sets.isEmpty() -> InfoLine(
            text = tx(lang, "Uçuş bilgisi operatörden gelmedi — TO'dan onay alın.", "Данные о перелёте не получены — уточните у туроператора."),
            color = TourOSColors.Warning,
            background = TourOSColors.WarningContainer
        )
        else -> {
            val defaultPrice = sets.firstOrNull { it.isDefault }?.price ?: sets.first().price
            sets.forEachIndexed { idx, set ->
                FlightSetBlock(lang, set, isFirst = idx == 0, defaultPrice = defaultPrice)
            }
            if (allSets.size > VISIBLE_FLIGHT_SETS) {
                TourOSButton(
                    text = if (showAllSets) tx(lang, "Daha az göster", "Свернуть") else "${tx(lang, "Tüm seçenekleri göster", "Показать все варианты")} (${allSets.size})",
                    onClick = { showAllSets = !showAllSets },
                    variant = TourOSButtonVariant.SECONDARY
                )
            }
        }
    }

    // Tura dahil olanlar / dahil olmayanlar
    SectionTitle(tx(lang, "Tura dahil olanlar", "Что входит в тур"))
    val flags = d.safeFlags
    val included = if (d.safeContents.isNotEmpty()) d.safeContents else buildList {
        if (!flags.noflight) add("Авиаперелет")
        add("Проживание в отеле")
        if (!flags.nomeal) add("Питание")
        if (!flags.nomedinsurance) add("Медицинская страховка")
        if (!flags.notransfer) add("Трансфер")
    }
    Text(
        text = included.joinToString(" · ") +
            if (d.safeContents.isEmpty()) "  (${tx(lang, "operatör listesi yok, standart paket içeriği", "стандартный состав, оператор не передал список")})" else "",
        style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
    )
    val excluded = buildList {
        if (flags.nomedinsurance) add(tx(lang, "Medikal sigorta tura DAHİL DEĞİL", "Медицинская страховка НЕ входит в тур"))
        if (flags.notransfer) add(tx(lang, "Transfer tura DAHİL DEĞİL", "Трансфер НЕ входит в тур"))
        if (flags.nomeal) add(tx(lang, "Yemek tura DAHİL DEĞİL", "Питание НЕ входит в тур"))
    }
    excluded.forEach { InfoLine(it, TourOSColors.Error, TourOSColors.ErrorContainer) }

    // Zorunlu ek ödemeler + vize
    val payments = d.safeAddPayments.filter { (it.amount ?: 0.0) > 0.0 }
    val visa = d.visaCharge ?: 0.0
    if (payments.isNotEmpty() || visa > 0.0) {
        SectionTitle(tx(lang, "Zorunlu ek ödemeler (fiyata dahil değil)", "Обязательные доплаты (не входят в цену)"))
        payments.forEach { p ->
            val unit = p.amount ?: 0.0
            AmountRow("${p.name}: ${money(unit)} × $pax", unit * pax)
        }
        if (visa > 0.0) {
            AmountRow("${tx(lang, "Vize", "Виза")}: ${money(visa)} × $pax", visa * pax)
        }
        HorizontalDivider(color = TourOSColors.Divider)
        AmountRow(tx(lang, "Toplam zorunlu ek ödeme", "Итого обязательных доплат"), d.mandatoryExtrasTotal(pax), bold = true)
        if (!product.currency.equals("RUB", ignoreCase = true)) {
            Text(
                text = tx(lang, "Tutarlar RUB'dur, ürün fiyatı farklı para biriminde olduğu için toplama eklenmedi.", "Суммы в рублях; не добавлены к итогу, так как цена тура в другой валюте."),
                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
            )
        }
    }

    // Operatör sistemine bağlantı
    val link = d.operatorLink?.takeIf { it.startsWith("http", ignoreCase = true) }
    if (link != null) {
        TourOSButton(
            text = tx(lang, "Turu operatör sisteminde aç", "Открыть тур в системе туроператора"),
            onClick = { runCatching { uriHandler.openUri(link) } },
            variant = TourOSButtonVariant.SECONDARY
        )
    }
}

@Composable
private fun FlightSetBlock(lang: String, set: TvFlightSet, isFirst: Boolean, defaultPrice: Double?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFirst) TourOSColors.PrimaryContainer.copy(alpha = 0.5f) else TourOSColors.Surface)
            .padding(TourOSSpacing.small),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = if (set.isDefault) tx(lang, "Varsayılan uçuş", "Перелёт по умолчанию") else tx(lang, "Alternatif uçuş", "Альтернативный перелёт"),
                style = TourOSTypography.Caption.copy(color = TourOSColors.Primary),
                fontWeight = FontWeight.Bold
            )
            val p = set.price
            if (!set.isDefault && p != null && defaultPrice != null && kotlin.math.abs(p - defaultPrice) >= 1.0) {
                val delta = p - defaultPrice
                Text(
                    text = "${if (delta > 0) "+" else "−"}${money(kotlin.math.abs(delta))} ${set.currency ?: "RUB"}",
                    style = TourOSTypography.Caption.copy(color = if (delta > 0) TourOSColors.Warning else TourOSColors.Success),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        set.forward.forEach { LegLine(lang, tx(lang, "Gidiş", "Туда"), set.dateForward, it) }
        set.backward.forEach { LegLine(lang, tx(lang, "Dönüş", "Обратно"), set.dateBackward, it) }
    }
}

@Composable
private fun LegLine(lang: String, direction: String, setDate: String?, leg: TvFlightLeg) {
    val date = leg.departureDate?.takeIf { it.isNotBlank() } ?: setDate?.takeIf { it.isNotBlank() }
    val datePart = date?.let { "$it · " } ?: ""
    val airline = listOfNotNull(leg.number?.takeIf { it.isNotBlank() }, leg.airlineName?.takeIf { it.isNotBlank() }).joinToString(" ")
    val route = "${leg.departurePort.orEmpty()} → ${leg.arrivalPort.orEmpty()}"
    val times = if (leg.isPlaceholder || leg.departureTime.isNullOrBlank()) {
        tx(lang, "uçuş saati ve numarası operatör tarafından bildirilecek", "рейс и время уточняются у туроператора")
    } else {
        "${leg.departureTime} → ${leg.arrivalTime.orEmpty()}"
    }
    val extras = buildList {
        leg.flightClass?.takeIf { it.isNotBlank() }?.let { add("${tx(lang, "sınıf", "класс")} $it") }
        when {
            leg.baggageKg != null && leg.baggageKg > 0 -> add("${tx(lang, "bagaj", "багаж")} ${leg.baggageKg} кг")
            leg.baggageIncluded == false -> add(tx(lang, "bagaj dahil değil", "багаж не включён"))
        }
        leg.plane?.takeIf { it.isNotBlank() }?.let { add(it) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = "$direction: $datePart${if (airline.isNotBlank()) "$airline · " else ""}$route · $times" +
                (if (extras.isNotEmpty()) " · ${extras.joinToString(" · ")}" else ""),
            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary)
        )
        if (leg.noPlaces) {
            Chip(tx(lang, "Bu uçuşta yer yok", "На этот рейс нет мест"), TourOSColors.Error)
        } else if (leg.onDemand) {
            Chip(tx(lang, "Yerler talep üzerine", "Места под запрос"), TourOSColors.Warning)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun AmountRow(label: String, amountRub: Double, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${money(amountRub)} RUB",
            style = TourOSTypography.Caption.copy(color = TourOSColors.TextPrimary),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoLine(text: String, color: Color, background: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, style = TourOSTypography.Caption.copy(color = color), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, style = TourOSTypography.Caption.copy(color = color), fontWeight = FontWeight.Bold)
    }
}

// ─── Yardımcılar ──────────────────────────────────────────────────────────────

private const val VISIBLE_FLIGHT_SETS = 5

private fun money(v: Double): String = KmpCurrencyFormatter.formatAmount(v, decimals = false)

/**
 * Proje kuralı: sadece yönetici tarafı Türkçedir; acente ve web tarafında sabit metinler HER ZAMAN Rusça.
 * Bu panel acente/web rezervasyon adımında göründüğü için Rusça metin döner.
 * Türkçe metin sadece kod okuyan için açıklama olarak tutulur.
 */
@Suppress("UNUSED_PARAMETER")
private fun tx(lang: String, tr: String, ru: String): String = ru

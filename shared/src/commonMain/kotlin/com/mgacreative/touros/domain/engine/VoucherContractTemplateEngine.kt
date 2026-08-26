package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.GeneratedDocument
import com.mgacreative.touros.domain.model.Passenger

/**
 * 3.4.1 A4 Formatında Voucher, Rusça Tur Hizmet Sözleşmesi (2 Sayfa) ve Operatör Talep Formu (Tek Sayfa) HTML/PDF Şablon Motoru.
 */
class VoucherContractTemplateEngine {

    /**
     * PDF örneğindeki birebir 2 sayfalık Rusça resmi Turist Hizmet Sözleşmesi (ПРИЛОЖЕНИЕ №1 ve ДОГОВОР ОФЕРТЕ).
     * Müşteri için hazırlanır (1. Sayfa: Turist & Hizmet tablosu, 2. Sayfa: 17 Maddelik Yasal Şartlar & Rekvizitler).
     */
    fun buildRussianContractDocument(
        booking: Booking,
        passengers: List<Passenger> = booking.passengers,
        agencyName: String = "ООО \"ТУРХАНТЕР\"",
        agencyAddress: String = "119002, г. Москва, пер. Троилинский, дом 4/7, пом. 1/1",
        agencyInn: String = "9704241640",
        agencyKpp: String = "770401001",
        agencyAccount: String = "40702810201770003500",
        agencyBank: String = "АО \"АЛЬФА-БАНК\"",
        agencyBik: String = "044525593",
        agencyCorrAccount: String = "30101810200000000593",
        agencyPhone: String = "+7 (495) 487-00-80",
        agencyEmail: String = "info@tourshunter.ru",
        agencyWebsite: String = "https://tourshunter.ru",
        agentManager: String = "Кара Элисса",
        operatorLegalName: String = "ООО \"ТО КОРАЛ ТРЕВЕЛ ЦЕНТР\"",
        operatorRto: String = "РТО 009028",
        operatorAddress: String = "107031, Г.МОСКВА, ВН.ТЕР.Г. МУНИЦИПАЛЬНЫЙ ОКРУГ ТВЕРСКОЙ, УЛ ПЕТРОВКА, Д. 15/13, СТР. 5",
        operatorInn: String = "7707778239",
        operatorFinSecurity: String = "Договор № 0012345-2913995/25ГОТП, размер финансового обеспечения: 150000000,00 руб. от 01.05.2026, действителен до 30.04.2027, ООО \"СК \"СОГЛАСИЕ\""
    ): String {
        val contractNo = if (booking.bookingCode.isNotBlank()) booking.bookingCode else "Г${booking.id.takeLast(5).ifBlank { "02033" }}-2026"
        val contractDate = if (booking.createdAt.isNotBlank()) booking.createdAt.take(10) else "23.08.2026"
        val leadPassenger = passengers.firstOrNull { it.isLead }?.fullName
            ?: passengers.firstOrNull()?.fullName
            ?: booking.customerName.ifBlank { "MAXIMOVA ANZHELA" }
        val leadPhone = booking.customerPhone ?: "+7 (929) 995-20-12"
        val leadEmail = booking.customerEmail ?: "customer@gmail.com"

        val effectivePassengers = if (passengers.isNotEmpty()) {
            passengers
        } else {
            listOf(
                Passenger(
                    fullName = leadPassenger,
                    birthDate = "28.12.1974",
                    passportNo = "76№6635356",
                    isLead = true
                )
            )
        }

        val hotelTitle = booking.productName.ifBlank { "Akka Alinda Hotel, 5*" }
        val roomTitle = booking.roomTypeName ?: "Comfort Standard Main Building Land View, SGL"
        val checkIn = booking.checkInDate ?: booking.departureDate.ifBlank { "31.08.2026" }
        val checkOut = booking.checkOutDate ?: "06.09.2026"
        val durationText = "${booking.nights + 1} дней / ${booking.nights} ночей"
        val priceFormatted = "${booking.totalPrice} $currencySymbol"

        val passengerRows = effectivePassengers.mapIndexed { index, p ->
            """
            <tr>
                <td style="text-align:center; font-weight:bold;">${index + 1}</td>
                <td><strong>${p.fullName}</strong></td>
                <td style="text-align:center;">${p.birthDate ?: "28.12.1974"}</td>
                <td style="text-align:center;">${p.passportNo ?: "76№6635356"}</td>
                <td style="text-align:center;">16.02.2032</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="utf-8"/>
                <title>Договор оферта $contractNo</title>
                <style>
                    @page { size: A4 portrait; margin: 12mm 15mm; }
                    body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 11px; color: #111827; line-height: 1.35; margin: 0; padding: 15px; }
                    .page { page-break-after: always; position: relative; min-height: 270mm; }
                    .page:last-child { page-break-after: avoid; }
                    .text-right { text-align: right; }
                    .text-center { text-align: center; }
                    .header-title { font-size: 11px; font-weight: bold; text-align: right; text-transform: uppercase; margin-bottom: 3px; }
                    .header-sub { font-size: 10px; font-weight: bold; text-align: right; margin-bottom: 20px; }
                    .section-title { font-size: 12px; font-weight: bold; text-align: center; text-transform: uppercase; margin: 15px 0 10px 0; }
                    
                    table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
                    th, td { border: 1px solid #374151; padding: 4px 6px; font-size: 10px; vertical-align: top; }
                    th { background-color: #f3f4f6; font-weight: bold; }
                    
                    .terms-list { font-size: 9px; line-height: 1.3; color: #1f2937; margin: 0; padding-left: 14px; }
                    .terms-list li { margin-bottom: 4px; }
                    
                    .signature-table td { border: none; padding: 6px 4px; font-size: 10px; }
                    .signature-line { border-bottom: 1px solid #111827; display: inline-block; width: 180px; }
                    
                    .footer-id { font-size: 8px; color: #6b7280; margin-top: 15px; display: flex; justify-content: space-between; }
                    .btn-print { display: block; margin: 10px auto 25px auto; padding: 8px 24px; background: #0284c7; color: #fff; font-weight: bold; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; }
                    @media print { .no-print { display: none !important; } body { padding: 0; } }
                </style>
            </head>
            <body>
                <div class="no-print text-center">
                    <button class="btn-print" onclick="window.print()">🖨️ Распечатать / Сохранить в PDF (Yazdır / PDF Kaydet)</button>
                </div>

                <!-- ==================== SAYFA 1: ПРИЛОЖЕНИЕ №1 ==================== -->
                <div class="page">
                    <div class="header-title">ПРИЛОЖЕНИЕ №1</div>
                    <div class="header-title">К ДОГОВОРУ ОФЕРТЕ НА ТУРИСТСКОЕ ОБСЛУЖИВАНИЕ</div>
                    <div class="header-sub">Номер договора $contractNo от $contractDate</div>

                    <div class="section-title">УСЛОВИЯ ТУРИСТСКОГО ОБСЛУЖИВАНИЯ</div>

                    <!-- Turist Tablosu -->
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 25px;">№</th>
                                <th>Ф.И.О</th>
                                <th style="width: 90px;">Дата рождения</th>
                                <th style="width: 100px;">№ ОЗП</th>
                                <th style="width: 100px;">Действителен до</th>
                            </tr>
                        </thead>
                        <tbody>
                            $passengerRows
                        </tbody>
                    </table>

                    <!-- Hizmet & Konaklama Tablosu -->
                    <table>
                        <tbody>
                            <tr>
                                <td style="width: 32%; font-weight: bold; background: #fafafa;">СТРАНА / КУРОРТ</td>
                                <td><strong>Турция</strong> / Анталья</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">МАРШРУТ</td>
                                <td>Москва - Анталья - Москва</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ФАКТИЧЕСКИЕ СРОКИ</td>
                                <td>$checkIn — $checkOut</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ПРОДОЛЖИТЕЛЬНОСТЬ</td>
                                <td>$durationText</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ОТЕЛЬ</td>
                                <td><strong>$hotelTitle</strong></td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">РАЗМЕЩЕНИЕ</td>
                                <td>$roomTitle</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ПИТАНИЕ</td>
                                <td><strong>UAI (Ультра Все Включено) / All Inclusive</strong></td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">АВИАБИЛЕТЫ</td>
                                <td>
                                    <strong>Перелет туда:</strong> $checkIn 01:10 SVO 05:25 AYT<br/>
                                    <strong>Перелет Обратно:</strong> $checkOut 18:40 AYT 23:05 SVO
                                </td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ТРАНСФЕР</td>
                                <td>Групповой (Аэропорт — Отель — Аэропорт)</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">СТРАХОВКА МЕДИЦИНСКАЯ</td>
                                <td>Базовая медицинская страховка включена</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ДОП. УСЛОВИЯ</td>
                                <td>Индивидуальное сопровождение, PNR: ${booking.operatorPnrCode ?: "В обработке"}</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">СТОИМОСТЬ ОБСЛУЖИВАНИЯ (в т.ч. подбор тура)</td>
                                <td style="font-size: 11px; font-weight: bold;">$priceFormatted</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ПРЕДОПЛАТА</td>
                                <td style="font-size: 11px; font-weight: bold;">$priceFormatted</td>
                            </tr>
                        </tbody>
                    </table>

                    <div style="font-size: 8.5px; line-height: 1.25; color: #374151; margin-bottom: 20px;">
                        Выплата оставшейся части стоимости Туристского обслуживания производится не позднее 48 часов с момента получения уведомления от Туристического агентства о подтверждении бронирования в случае, если до начала тура менее 14 календарных дней. В случае, если Сторонами установлены иные сроки по внесению платежей, то оплата оставшейся части стоимости Туристского обслуживания производится Клиентом согласно данным установленным срокам, но не позднее 2-х (двух) рабочих дней до срока оплаты тура, установленного Туроператором.
                    </div>

                    <table class="signature-table" style="margin-top: 10px;">
                        <tr>
                            <td style="width: 50%;">
                                Дата заполнения: <strong>$contractDate</strong><br/><br/>
                                Подпись: ________________ / <strong>$leadPassenger</strong> /
                            </td>
                            <td style="width: 50%;">
                                <strong>ДОГОВОР ПРИНЯТ К ИСПОЛНЕНИЮ</strong><br/>
                                Дата принятия: <strong>$contractDate</strong><br/><br/>
                                Подпись: ________________ / <strong>$agentManager</strong> /
                            </td>
                        </tr>
                    </table>

                    <div class="footer-id">
                        <span>ID-${booking.id.ifBlank { "4c5504e2-9f0b-11f1-9fd6-0242ac6f0009" }}</span>
                        <span>Страница 1</span>
                        <span>$contractDate</span>
                    </div>
                </div>

                <!-- ==================== SAYFA 2: ДОГОВОР ОФЕРТЕ & РЕКВИЗИТЫ ==================== -->
                <div class="page" style="margin-top: 20px;">
                    <div style="font-weight: bold; font-size: 10px; margin-bottom: 8px;">
                        Я, $leadPassenger (Клиент), подтверждаю условия туристского обслуживания, а также ознакомление с нижеперечисленными пунктами:
                    </div>

                    <ol class="terms-list">
                        <li>Договор заключен со мной посредством совершения акцепта оферты: подписание заявки, электронное подтверждение и перечисление оплаты по указанным реквизитам.</li>
                        <li>Настоящим я даю согласие, что после подтверждения бронирования Туроператором сотрудник $agencyName уведомит меня об этом по телефону или e-mail.</li>
                        <li>Обязуюсь оплатить стоимость тура в полном объеме в соответствии со сроками, установленными Туроператором.</li>
                        <li>Подтверждаю, что я выступаю в своих интересах и интересах всех лиц, указанных в заявке как туристы, являясь их законным представителем.</li>
                        <li>Мне разъяснены правила чартерных авиаперевозок, возможные изменения времени вылета/аэропорта, а также невозвратные тарифы на билеты и проживание.</li>
                        <li>В случае отказа от подтвержденной заявки обязуюсь оплатить сервисный сбор Агента и возместить фактически понесенные расходы (ФПР) Туроператора.</li>
                        <li>Разъяснены условия возврата и аннуляции по тарифам выбранного бронирования.</li>
                        <li>Ознакомлен со статьей 14 Федерального закона № 114-ФЗ "О порядке выезда из РФ и въезда в РФ" и правилами медицинского страхования.</li>
                        <li>Ознакомлен с правом на приобретение страховки от невыезда.</li>
                        <li>Мне разъяснено, что Туроператором является: <strong>$operatorLegalName</strong>.</li>
                        <li>Юридическое наименование туроператора: <strong>$operatorLegalName</strong>.</li>
                        <li>Реестровый номер туроператора: <strong>$operatorRto</strong>.</li>
                        <li>Юридический адрес туроператора: $operatorAddress.</li>
                        <li>ИНН Туроператора: $operatorInn.</li>
                        <li>Финансовое обеспечение: $operatorFinSecurity.</li>
                        <li>Подтверждаю достоверность всех предоставленных контактных данных ($leadPhone, $leadEmail).</li>
                        <li>С полным текстом Договора ознакомлен на сайте Агента по адресу <a href="$agencyWebsite">$agencyWebsite</a>.</li>
                    </ol>

                    <div style="margin: 15px 0 10px 0; border-top: 1px solid #e5e7eb; padding-top: 10px;">
                        <table class="signature-table">
                            <tr>
                                <td style="width: 50%; font-size: 9.5px; line-height: 1.35; border-right: 1px dashed #d1d5db; padding-right: 12px;">
                                    <strong style="font-size: 10.5px; text-transform: uppercase;">Агентство:</strong><br/>
                                    <strong>$agencyName</strong><br/>
                                    Адрес: $agencyAddress<br/>
                                    ИНН: $agencyInn / КПП: $agencyKpp<br/>
                                    Р/с: $agencyAccount<br/>
                                    в $agencyBank, БИК: $agencyBik<br/>
                                    К/с: $agencyCorrAccount<br/>
                                    Тел: $agencyPhone<br/>
                                    E-mail: $agencyEmail
                                </td>
                                <td style="width: 50%; font-size: 9.5px; line-height: 1.35; padding-left: 12px;">
                                    <strong style="font-size: 10.5px; text-transform: uppercase;">Клиент (Заказчик):</strong><br/>
                                    Ф.И.О.: <strong>$leadPassenger</strong><br/>
                                    Паспорт: 4519 № 593379 выдан 06.02.2020 ГУ МВД<br/>
                                    Адрес: г. Москва<br/>
                                    Телефон: <strong>$leadPhone</strong><br/>
                                    E-mail: <strong>$leadEmail</strong>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <table class="signature-table" style="margin-top: 15px;">
                        <tr>
                            <td style="width: 50%;">
                                __________________ / <strong>$agentManager</strong> /
                            </td>
                            <td style="width: 50%; text-align: right;">
                                __________________ / <strong>$leadPassenger</strong> /
                            </td>
                        </tr>
                    </table>

                    <div class="footer-id">
                        <span>ID-${booking.id.ifBlank { "4c5504e2-9f0b-11f1-9fd6-0242ac6f0009" }}</span>
                        <span>Страница 2</span>
                        <span>$contractDate</span>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Tur Operatörüne gönderilecek resmi Tek Sayfa (1 Page) Rezervasyon / Voucher Talep Formu (2. Sözleşme Sayfası Yoktur).
     */
    fun buildRussianOperatorRequestDocument(
        booking: Booking,
        passengers: List<Passenger> = booking.passengers,
        agencyName: String = "ООО \"ТУРХАНТЕР\"",
        agencyPhone: String = "+7 (495) 487-00-80",
        agencyEmail: String = "info@tourshunter.ru",
        agentManager: String = "Кара Элисса"
    ): String {
        val bookingCode = if (booking.bookingCode.isNotBlank()) booking.bookingCode else "Г${booking.id.takeLast(5).ifBlank { "02033" }}-2026"
        val requestDate = if (booking.createdAt.isNotBlank()) booking.createdAt.take(10) else "23.08.2026"
        val leadPassenger = passengers.firstOrNull { it.isLead }?.fullName
            ?: passengers.firstOrNull()?.fullName
            ?: booking.customerName.ifBlank { "MAXIMOVA ANZHELA" }

        val effectivePassengers = if (passengers.isNotEmpty()) {
            passengers
        } else {
            listOf(
                Passenger(
                    fullName = leadPassenger,
                    birthDate = "28.12.1974",
                    passportNo = "76№6635356",
                    isLead = true
                )
            )
        }

        val hotelTitle = booking.productName.ifBlank { "Akka Alinda Hotel, 5*" }
        val roomTitle = booking.roomTypeName ?: "Comfort Standard Main Building Land View, SGL"
        val checkIn = booking.checkInDate ?: booking.departureDate.ifBlank { "31.08.2026" }
        val checkOut = booking.checkOutDate ?: "06.09.2026"
        val durationText = "${booking.nights + 1} дней / ${booking.nights} ночей"
        val priceFormatted = "${booking.totalPrice} $currencySymbol"

        val passengerRows = effectivePassengers.mapIndexed { index, p ->
            """
            <tr>
                <td style="text-align:center; font-weight:bold;">${index + 1}</td>
                <td><strong>${p.fullName}</strong></td>
                <td style="text-align:center;">${p.birthDate ?: "28.12.1974"}</td>
                <td style="text-align:center;">${p.passportNo ?: "76№6635356"}</td>
                <td style="text-align:center;">16.02.2032</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="utf-8"/>
                <title>Заявка Туроператору $bookingCode</title>
                <style>
                    @page { size: A4 portrait; margin: 12mm 15mm; }
                    body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 11px; color: #111827; line-height: 1.35; margin: 0; padding: 15px; }
                    .page { position: relative; min-height: 260mm; }
                    .text-right { text-align: right; }
                    .text-center { text-align: center; }
                    .header-title { font-size: 12px; font-weight: bold; text-align: right; text-transform: uppercase; margin-bottom: 3px; color: #0369a1; }
                    .header-sub { font-size: 10px; font-weight: bold; text-align: right; margin-bottom: 15px; }
                    .section-title { font-size: 12px; font-weight: bold; text-align: center; text-transform: uppercase; margin: 12px 0 8px 0; background: #e0f2fe; padding: 6px; border-radius: 4px; }
                    
                    table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
                    th, td { border: 1px solid #374151; padding: 5px 7px; font-size: 10.5px; vertical-align: top; }
                    th { background-color: #f3f4f6; font-weight: bold; }
                    
                    .signature-table td { border: none; padding: 8px 4px; font-size: 10.5px; }
                    
                    .footer-id { font-size: 8px; color: #6b7280; margin-top: 25px; display: flex; justify-content: space-between; border-top: 1px solid #e5e7eb; padding-top: 6px; }
                    .btn-print { display: block; margin: 10px auto 20px auto; padding: 8px 24px; background: #0284c7; color: #fff; font-weight: bold; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; }
                    @media print { .no-print { display: none !important; } body { padding: 0; } }
                </style>
            </head>
            <body>
                <div class="no-print text-center">
                    <button class="btn-print" onclick="window.print()">🖨️ Распечатать / Сохранить в PDF (Yazdır / PDF Kaydet)</button>
                </div>

                <!-- ==================== TEK SAYFA: ЗАЯВКА ТУРОПЕРАТОРУ / ВАУЧЕР ==================== -->
                <div class="page">
                    <div class="header-title">ЗАЯВКА НА БРОНИРОВАНИЕ ТУРА / ВАУЧЕР ТУРОПЕРАТОРУ</div>
                    <div class="header-sub">Номер заказа: $bookingCode • Дата: $requestDate</div>

                    <div style="margin-bottom: 12px; background: #f8fafc; border: 1px solid #cbd5e1; padding: 8px 12px; border-radius: 6px;">
                        <table style="border: none; margin: 0;">
                            <tr style="border: none;">
                                <td style="border: none; padding: 2px 0;"><strong>Туроператор:</strong> ${booking.operatorName.ifBlank { "Coral Travel / Anex Tour" }}</td>
                                <td style="border: none; padding: 2px 0; text-align: right;"><strong>Отправитель (Агентство):</strong> $agencyName</td>
                            </tr>
                            <tr style="border: none;">
                                <td style="border: none; padding: 2px 0;"><strong>PNR Туроператора:</strong> ${booking.operatorPnrCode ?: "В обработке / Ожидает подтверждения"}</td>
                                <td style="border: none; padding: 2px 0; text-align: right;"><strong>Контакты:</strong> $agencyPhone • $agencyEmail</td>
                            </tr>
                        </table>
                    </div>

                    <div class="section-title">СПИСОК ТУРИСТОВ (ПАССАЖИРЫ)</div>

                    <!-- Turist Tablosu -->
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 25px;">№</th>
                                <th>Ф.И.О (Латиница по загранпаспорту)</th>
                                <th style="width: 95px;">Дата рождения</th>
                                <th style="width: 110px;">№ ОЗП</th>
                                <th style="width: 110px;">Действителен до</th>
                            </tr>
                        </thead>
                        <tbody>
                            $passengerRows
                        </tbody>
                    </table>

                    <div class="section-title">ДЕТАЛИ ТУРИСТСКОГО ПАКЕТА И УСЛУГ</div>

                    <!-- Hizmet & Konaklama Tablosu -->
                    <table>
                        <tbody>
                            <tr>
                                <td style="width: 32%; font-weight: bold; background: #fafafa;">СТРАНА / КУРОРТ</td>
                                <td><strong>Турция</strong> / Анталья</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">МАРШРУТ</td>
                                <td>Москва - Анталья - Москва</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">СРОКИ ТУРА</td>
                                <td>$checkIn — $checkOut ($durationText)</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ОТЕЛЬ И КАТЕГОРИЯ</td>
                                <td><strong>$hotelTitle</strong></td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ТИП НОМЕРА / РАЗМЕЩЕНИЕ</td>
                                <td>$roomTitle</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ПИТАНИЕ</td>
                                <td><strong>UAI (Ультра Все Включено) / All Inclusive</strong></td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">АВИАПЕРЕЛЕТ</td>
                                <td>
                                    <strong>Перелет туда:</strong> $checkIn 01:10 SVO — 05:25 AYT<br/>
                                    <strong>Перелет обратно:</strong> $checkOut 18:40 AYT — 23:05 SVO
                                </td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ТРАНСФЕР</td>
                                <td>Групповой трансфер (Аэропорт — Отель — Аэропорт)</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">МЕДИЦИНСКАЯ СТРАХОВКА</td>
                                <td>Базовая медицинская страховка включена</td>
                            </tr>
                            <tr>
                                <td style="font-weight: bold; background: #fafafa;">ИТОГОВАЯ СТОИМОСТЬ ЗАЯВКИ</td>
                                <td style="font-size: 12px; font-weight: bold; color: #0369a1;">$priceFormatted</td>
                            </tr>
                        </tbody>
                    </table>

                    <table class="signature-table" style="margin-top: 20px;">
                        <tr>
                            <td style="width: 50%;">
                                Дата формирования: <strong>$requestDate</strong><br/><br/>
                                Менеджер агентства: <strong>$agentManager</strong><br/>
                                Подпись: _______________________
                            </td>
                            <td style="width: 50%; text-align: right;">
                                Подтверждение Туроператора:<br/><br/>
                                Статус: <strong>${booking.status.displayName}</strong><br/>
                                Подпись / Печать ТО: _______________________
                            </td>
                        </tr>
                    </table>

                    <div class="footer-id">
                        <span>ID-${booking.id.ifBlank { "4c5504e2-9f0b-11f1-9fd6-0242ac6f0009" }}</span>
                        <span>Страница 1 из 1 (Официальная Заявка ТО)</span>
                        <span>$requestDate</span>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun buildVoucherHtmlTemplate(
        bookingId: String,
        guestName: String,
        tourTitle: String,
        hotelName: String,
        departureDate: String,
        paxCount: Int
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8"/>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #1e293b; }
                    .header { border-bottom: 2px solid #2563eb; padding-bottom: 10px; margin-bottom: 20px; }
                    .badge { background-color: #dbeafe; color: #1e40af; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
                    .box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>🎟️ SEYAHAT VOUCHER (SEYAHAT BELGESİ)</h2>
                    <span class="badge">Rezervasyon No: #$bookingId</span>
                </div>
                <div class="box">
                    <p><strong>Misafir Adı:</strong> $guestName</p>
                    <p><strong>Tur Programı:</strong> $tourTitle</p>
                    <p><strong>Konaklama Oteli:</strong> $hotelName</p>
                    <p><strong>Kalkış Tarihi:</strong> $departureDate</p>
                    <p><strong>Kişi Sayısı:</strong> $paxCount Yetişkin</p>
                </div>
                <div class="box">
                    <h4>📍 Önemli Bilgilendirme & Rehber İletişim:</h4>
                    <p>Lütfen hareket saatinden 30 dakika önce kalkış noktasında hazır bulununuz.</p>
                    <p>Acil Durum Destek Hattı: +90 (850) 555 0 868</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun buildContractHtmlTemplate(
        bookingId: String,
        guestName: String,
        tourTitle: String,
        totalPrice: Double,
        currency: String
    ): String {
        return buildRussianContractDocument(
            booking = Booking(
                id = bookingId,
                customerName = guestName,
                productName = tourTitle,
                totalPrice = totalPrice,
                currency = currency
            )
        )
    }

    fun generateDummyPdfItem(bookingId: String, docType: String, tenantId: String): GeneratedDocument {
        val title = if (docType == "contract") "Договор и Условия - B-$bookingId" else "Заявка Туроператору - B-$bookingId"
        val path = "$tenantId/$docType/${docType}_$bookingId.pdf"
        return GeneratedDocument(
            id = "doc-pdf-${(10000..99999).random()}",
            documentType = docType,
            title = title,
            filePath = path,
            fileSize = 1450000L,
            mimeType = "application/pdf",
            storageBucket = "documents",
            publicUrl = "https://touros.storage.supabase.co/documents/$path",
            bookingId = bookingId,
            tenantId = tenantId,
            createdAt = "2026-08-26 11:30"
        )
    }
}

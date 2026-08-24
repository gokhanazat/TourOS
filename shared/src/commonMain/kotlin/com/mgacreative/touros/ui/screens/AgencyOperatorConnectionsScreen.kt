package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.data.database.entity.AgencyOperatorConnectionEntity
import com.mgacreative.touros.data.database.entity.OperatorSeasonRate
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AgencyOperatorConnectionsUiState
import com.mgacreative.touros.ui.viewmodel.AgencyOperatorConnectionsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tur Operatörleri Ekranı (Acente Tarafı) — Vektörel (ImageVector) İkon Mimarisi
 *
 * Web, Desktop, Android ve iOS platformlarında piksel düzeyinde 1:1 aynı görsel bütünlük
 * sağlayan %100 Compose Material Vektörel İkon yapısı.
 */
@Composable
fun AgencyOperatorConnectionsScreen(
    viewModel: AgencyOperatorConnectionsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isFormViewVisible by remember { mutableStateOf(false) }
    var selectedOperatorForEdit by remember { mutableStateOf<AgencyOperatorConnectionEntity?>(null) }
    var operatorToDelete by remember { mutableStateOf<AgencyOperatorConnectionEntity?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = if (isFormViewVisible) {
                    if (selectedOperatorForEdit == null) AppLanguageManager.translate("Yeni Tur Operatörü Tanımlama") else AppLanguageManager.translate("Tur Operatörü Düzenleme")
                } else {
                    AppLanguageManager.translate("Bağlı Tur Operatörleri")
                },
                subtitle = if (isFormViewVisible) {
                    AppLanguageManager.translate("Operatör marka, fiyatlama, API ve muhasebe parametrelerini yönetin")
                } else {
                    AppLanguageManager.translate("Acente Pazaryeri multi-operatör bağlantı ve komisyon yönetimi")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isFormViewVisible) {
                                isFormViewVisible = false
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TourOSColors.Primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isFormViewVisible) {
                // ── TAM SAYFA VEYA DİKEY FORMLU SAYFA GÖRÜNÜMÜ ────────────────────
                SinglePageOperatorFormView(
                    initialOperator = selectedOperatorForEdit,
                    onBackToList = { isFormViewVisible = false },
                    onSave = { entity ->
                        viewModel.saveConnection(
                            connection = entity,
                            onSuccess = { isFormViewVisible = false },
                            onError = {}
                        )
                    }
                )
            } else {
                // ── OPERATÖR PORTFÖYÜ LİSTE GÖRÜNÜMÜ ─────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(TourOSSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    // ÜST BAŞLIK & OPERATÖR EKLE BUTONU
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppLanguageManager.translate("Operatör Portföyü & Bağlantılar"),
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppLanguageManager.translate("Sisteminizde tanımlı ve B2B/API entegrasyonlu tur operatörleriniz"),
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                            )
                        }

                        TourOSButton(
                            text = "+ ${AppLanguageManager.translate("Tur Operatörü Ekle")}",
                            onClick = {
                                selectedOperatorForEdit = null
                                isFormViewVisible = true
                            },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }

                    // ARAMA & FİLTRELEME ÇUBUĞU
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = AppLanguageManager.translate("Operatör Arama"),
                                placeholder = AppLanguageManager.translate("Operatör adı, kodu veya entegrasyon türü ile ara..."),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            FilterChip(
                                selected = selectedStatusFilter == null,
                                onClick = { selectedStatusFilter = null },
                                label = { Text(AppLanguageManager.translate("Tümü"), style = TourOSTypography.Caption) }
                            )
                            FilterChip(
                                selected = selectedStatusFilter == "ACTIVE",
                                onClick = { selectedStatusFilter = "ACTIVE" },
                                label = { Text(AppLanguageManager.translate("Aktif"), style = TourOSTypography.Caption) }
                            )
                            FilterChip(
                                selected = selectedStatusFilter == "PAUSED",
                                onClick = { selectedStatusFilter = "PAUSED" },
                                label = { Text(AppLanguageManager.translate("Pasif"), style = TourOSTypography.Caption) }
                            )
                        }
                    }

                    HorizontalDivider(color = TourOSColors.Divider)

                    // EKRAN DURUMU (YÜKLENİYOR / HATA / LİSTE)
                    when (val state = uiState) {
                        is AgencyOperatorConnectionsUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TourOSColors.Primary)
                            }
                        }

                        is AgencyOperatorConnectionsUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = state.message, color = TourOSColors.Error, style = TourOSTypography.BodyMedium)
                            }
                        }

                        is AgencyOperatorConnectionsUiState.Success -> {
                            val filteredList = remember(state.connections, searchQuery, selectedStatusFilter) {
                                val q = searchQuery.trim().lowercase()
                                state.connections.filter { item ->
                                    val matchesSearch = q.isBlank() ||
                                            item.operatorName.lowercase().contains(q) ||
                                            item.operatorCompanyId.lowercase().contains(q) ||
                                            item.operatorType.lowercase().contains(q) ||
                                            item.integrationType.lowercase().contains(q)

                                    val matchesStatus = selectedStatusFilter == null || item.status == selectedStatusFilter
                                    matchesSearch && matchesStatus
                                }
                            }

                            if (filteredList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = AppLanguageManager.translate("Henüz tanımlanmış bir tur operatörü bulunamadı. Yukarıdaki 'Tur Operatörü Ekle' butonundan ekleyebilirsiniz."),
                                        style = TourOSTypography.BodyMedium,
                                        color = TourOSColors.TextSecondary
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 340.dp),
                                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredList, key = { it.id }) { item ->
                                        OperatorConnectionCardItem(
                                            connection = item,
                                            onEditClick = {
                                                selectedOperatorForEdit = item
                                                isFormViewVisible = true
                                            },
                                            onDeleteClick = {
                                                operatorToDelete = item
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── SİLME ONAY DİALOGU ──────────────────────────────────────────────────
    operatorToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { operatorToDelete = null },
            title = { Text(AppLanguageManager.translate("Operatör Bağlantısını Sil")) },
            text = { Text("${target.operatorName.ifBlank { target.operatorCompanyId }} ${AppLanguageManager.translate("isimli operatör bağlantısını silmek istediğinize emin misiniz?")}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConnection(
                            id = target.id,
                            onSuccess = { operatorToDelete = null },
                            onError = { operatorToDelete = null }
                        )
                    }
                ) {
                    Text(AppLanguageManager.translate("Sil"), color = TourOSColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { operatorToDelete = null }) {
                    Text(AppLanguageManager.translate("İptal"))
                }
            }
        )
    }
}

// ─── OPERATÖR KART BİLEŞENİ (VEKTÖREL İKONLU) ──────────────────────────────────

@Composable
private fun OperatorConnectionCardItem(
    connection: AgencyOperatorConnectionEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Operatör Avatar / Vektörel İkon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TourOSColors.PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (connection.operatorName.ifBlank { connection.operatorCompanyId }).take(2).uppercase(),
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = connection.operatorName.ifBlank { "Operatör (${connection.operatorCompanyId.take(8)})" },
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${AppLanguageManager.translate("Kod")}: ${connection.operatorCompanyId.ifBlank { connection.id.take(8) }}  ·  ${connection.operatorType}",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }

                TourOSStatusBadge(
                    text = AppLanguageManager.translate(connection.status),
                    backgroundColor = if (connection.status == "ACTIVE") TourOSColors.SuccessContainer else TourOSColors.Error.copy(alpha = 0.15f),
                    textColor = if (connection.status == "ACTIVE") TourOSColors.Success else TourOSColors.Error
                )
            }

            HorizontalDivider(color = TourOSColors.Divider)

            // Detay Bilgileri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(AppLanguageManager.translate("Satış Kâr Marjı"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        text = if (connection.priceAdjustmentType == "percentage") "%${connection.priceAdjustmentValue} ${AppLanguageManager.translate("Kar Marjı")}" else "+${connection.priceAdjustmentValue} ${connection.currency} ${AppLanguageManager.translate("Sabit Ekleme")}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(AppLanguageManager.translate("Hak Edilen Komisyon"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text(
                        text = "%${connection.commissionRate} ${AppLanguageManager.translate("Komisyon")}",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (connection.apiEndpoint.isNotBlank() || connection.contactPhone.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TourOSColors.Primary
                    )
                    Text(
                        text = "${AppLanguageManager.translate("Entegrasyon")}: ${connection.integrationType}  ·  ${connection.contactPhone.ifBlank { "—" }}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // İşlem Butonları (Vektörel İkonlu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = TourOSColors.Error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppLanguageManager.translate("Sil"), style = TourOSTypography.Caption.copy(color = TourOSColors.Error))
                }

                Spacer(modifier = Modifier.width(TourOSSpacing.small))

                TourOSButton(
                    text = AppLanguageManager.translate("Düzenle & Detay"),
                    onClick = onEditClick,
                    variant = TourOSButtonVariant.SECONDARY
                )
            }
        }
    }
}

// ─── TAM SAYFA VEKTÖREL İKONLU OPERATÖR FORMU (FULL PAGE FORM VIEW) ───────────

@Composable
private fun SinglePageOperatorFormView(
    initialOperator: AgencyOperatorConnectionEntity?,
    onBackToList: () -> Unit,
    onSave: (AgencyOperatorConnectionEntity) -> Unit
) {
    // Form State'leri
    var operatorName by remember(initialOperator) { mutableStateOf(initialOperator?.operatorName ?: "") }
    var operatorCompanyId by remember(initialOperator) { mutableStateOf(initialOperator?.operatorCompanyId ?: "") }
    var operatorType by remember(initialOperator) { mutableStateOf(initialOperator?.operatorType ?: "GLOBAL") }
    var operatorLogo by remember(initialOperator) { mutableStateOf(initialOperator?.operatorLogo ?: "") }

    var priceAdjustmentType by remember(initialOperator) { mutableStateOf(initialOperator?.priceAdjustmentType ?: "percentage") }
    var priceAdjustmentValueStr by remember(initialOperator) { mutableStateOf(initialOperator?.priceAdjustmentValue?.toString() ?: "10.0") }
    var commissionRateStr by remember(initialOperator) { mutableStateOf(initialOperator?.commissionRate?.toString() ?: "8.0") }
    var currency by remember(initialOperator) { mutableStateOf(initialOperator?.currency ?: "TRY") }

    var integrationType by remember(initialOperator) { mutableStateOf(initialOperator?.integrationType ?: "API") }
    var apiEndpoint by remember(initialOperator) { mutableStateOf(initialOperator?.apiEndpoint ?: "") }
    var apiKey by remember(initialOperator) { mutableStateOf(initialOperator?.apiKey ?: "") }

    var taxOffice by remember(initialOperator) { mutableStateOf(initialOperator?.taxOffice ?: "") }
    var taxNumber by remember(initialOperator) { mutableStateOf(initialOperator?.taxNumber ?: "") }
    var iban by remember(initialOperator) { mutableStateOf(initialOperator?.iban ?: "") }
    var bankName by remember(initialOperator) { mutableStateOf(initialOperator?.bankName ?: "") }

    var contactName by remember(initialOperator) { mutableStateOf(initialOperator?.contactName ?: "") }
    var contactPhone by remember(initialOperator) { mutableStateOf(initialOperator?.contactPhone ?: "") }
    var contactEmail by remember(initialOperator) { mutableStateOf(initialOperator?.contactEmail ?: "") }
    var status by remember(initialOperator) { mutableStateOf(initialOperator?.status ?: "ACTIVE") }
    var operatorSeasons by remember(initialOperator) { mutableStateOf<List<OperatorSeasonRate>>(initialOperator?.seasons ?: emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(TourOSSpacing.large),
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
    ) {
        // SAYFA ÜST AKSİYON BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                TourOSButton(
                    text = AppLanguageManager.translate("Operatör Listesine Dön"),
                    onClick = onBackToList,
                    variant = TourOSButtonVariant.SECONDARY
                )
                Text(
                    text = if (initialOperator == null) AppLanguageManager.translate("Yeni Tur Operatörü Tanımlama") else AppLanguageManager.translate("Tur Operatörü Düzenleme"),
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                TextButton(onClick = onBackToList) {
                    Text(AppLanguageManager.translate("İptal"), color = TourOSColors.TextSecondary)
                }
                TourOSButton(
                    text = AppLanguageManager.translate("Değişiklikleri Kaydet"),
                    onClick = {
                        val valAdj = priceAdjustmentValueStr.toDoubleOrNull() ?: 0.0
                        val valComm = commissionRateStr.toDoubleOrNull() ?: 8.0
                        val finalEntity = AgencyOperatorConnectionEntity(
                            id = initialOperator?.id ?: "",
                            agencyId = initialOperator?.agencyId ?: "agency-01",
                            operatorCompanyId = operatorCompanyId.ifBlank { "OP-${operatorName.take(4).uppercase()}" },
                            operatorName = operatorName.ifBlank { "Yeni Operatör" },
                            operatorLogo = operatorLogo,
                            operatorType = operatorType,
                            integrationType = integrationType,
                            apiEndpoint = apiEndpoint,
                            apiKey = apiKey,
                            priceAdjustmentType = priceAdjustmentType,
                            priceAdjustmentValue = valAdj,
                            commissionRate = valComm,
                            currency = currency,
                            taxOffice = taxOffice,
                            taxNumber = taxNumber,
                            iban = iban,
                            bankName = bankName,
                            contactName = contactName,
                            contactPhone = contactPhone,
                            contactEmail = contactEmail,
                            status = status,
                            seasons = operatorSeasons
                        )
                        onSave(finalEntity)
                    },
                    variant = TourOSButtonVariant.PRIMARY
                )
            }
        }

        HorizontalDivider(color = TourOSColors.Divider)

        // ── BLOK 1: GENEL BİLGİLER ─────────────────────────────────────────
        FormSectionCard(
            title = AppLanguageManager.translate("Genel Bilgiler"),
            icon = Icons.Default.Language
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = operatorName,
                            onValueChange = { operatorName = it },
                            label = AppLanguageManager.translate("Operatör Marka Adı *"),
                            placeholder = AppLanguageManager.translate("Örn: Coral Travel, Pegas Touristik, ETS Tur")
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = operatorCompanyId,
                            onValueChange = { operatorCompanyId = it },
                            label = AppLanguageManager.translate("Operatör Firma Kodu / ID *"),
                            placeholder = AppLanguageManager.translate("Örn: OP-CORAL-01")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(AppLanguageManager.translate("Operatör Türü:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            listOf("GLOBAL" to "Global Operatör", "DOMESTIC" to "İç Pazar", "DMC" to "Yerel DMC").forEach { (code, label) ->
                                FilterChip(
                                    selected = operatorType == code,
                                    onClick = { operatorType = code },
                                    label = { Text(AppLanguageManager.translate(label), style = TourOSTypography.Caption) }
                                )
                            }
                        }
                    }

                    Column {
                        Text(AppLanguageManager.translate("Bağlantı Durumu:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                            listOf("ACTIVE" to "Aktif", "PAUSED" to "Pasif").forEach { (code, label) ->
                                FilterChip(
                                    selected = status == code,
                                    onClick = { status = code },
                                    label = { Text(AppLanguageManager.translate(label), style = TourOSTypography.Caption) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── BLOK 2: FİYATLANDIRMA & KOMİSYON ──────────────────────────────
        FormSectionCard(
            title = AppLanguageManager.translate("Fiyatlandırma & Komisyon Parametreleri"),
            icon = Icons.Default.AttachMoney
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(AppLanguageManager.translate("Kar Marjı Tipi:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary), modifier = Modifier.padding(end = 8.dp))
                    RadioButton(selected = priceAdjustmentType == "percentage", onClick = { priceAdjustmentType = "percentage" })
                    Text(AppLanguageManager.translate("Yüzde (% Kar Eklemeli)"), style = TourOSTypography.BodyMedium, modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = priceAdjustmentType == "fixed", onClick = { priceAdjustmentType = "fixed" })
                    Text(AppLanguageManager.translate("Sabit Tutar Ekleme"), style = TourOSTypography.BodyMedium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = priceAdjustmentValueStr,
                            onValueChange = { priceAdjustmentValueStr = it },
                            label = if (priceAdjustmentType == "percentage") AppLanguageManager.translate("Fiyat Kar Marjı (%)") else AppLanguageManager.translate("Sabit Kar Tutarı"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(
                            value = commissionRateStr,
                            onValueChange = { commissionRateStr = it },
                            label = AppLanguageManager.translate("Hak Edilen Komisyon (%)"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Text(AppLanguageManager.translate("Çalışma Para Birimi:"), style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                Row(horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                    listOf("TRY", "EUR", "USD", "RUB", "GBP").forEach { curr ->
                        FilterChip(
                            selected = currency == curr,
                            onClick = { currency = curr },
                            label = { Text(curr, style = TourOSTypography.Caption, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TourOSColors.Divider)

                Text(
                    text = AppLanguageManager.translate("📅 Operatör Sezon Periyotları & Özel Komisyon / Pax Oranları"),
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                )

                if (operatorSeasons.isEmpty()) {
                    Text(
                        text = AppLanguageManager.translate("Henüz tanımlı özel sezon periyodu yok. Varsayılan genel komisyon oranı geçerlidir."),
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                operatorSeasons.forEachIndexed { sIdx, seasonItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TourOSColors.SurfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Periyot ${sIdx + 1}: ${seasonItem.name.ifBlank { "Sezon" }}",
                                style = TourOSTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                            )
                            IconButton(
                                onClick = {
                                    val mutable = operatorSeasons.toMutableList()
                                    mutable.removeAt(sIdx)
                                    operatorSeasons = mutable
                                }
                            ) {
                                Text("🗑️", fontSize = 14.sp)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                TourOSTextField(
                                    value = seasonItem.name,
                                    onValueChange = { newName ->
                                        val mutable = operatorSeasons.toMutableList()
                                        mutable[sIdx] = mutable[sIdx].copy(name = newName)
                                        operatorSeasons = mutable
                                    },
                                    label = AppLanguageManager.translate("Sezon Adı (örn: Yüksek Sezon)")
                                )
                            }
                            Box(modifier = Modifier.weight(0.9f)) {
                                TourOSTextField(
                                    value = if (seasonItem.commissionRate > 0) seasonItem.commissionRate.toString() else "",
                                    onValueChange = { newCommStr ->
                                        val mutable = operatorSeasons.toMutableList()
                                        val valComm = newCommStr.toDoubleOrNull() ?: 0.0
                                        mutable[sIdx] = mutable[sIdx].copy(commissionRate = valComm)
                                        operatorSeasons = mutable
                                    },
                                    label = AppLanguageManager.translate("Komisyon Oranı (%)"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            Box(modifier = Modifier.weight(0.9f)) {
                                TourOSTextField(
                                    value = if (seasonItem.paxFee > 0) seasonItem.paxFee.toString() else "",
                                    onValueChange = { newPaxStr ->
                                        val mutable = operatorSeasons.toMutableList()
                                        val valPax = newPaxStr.toDoubleOrNull() ?: 0.0
                                        mutable[sIdx] = mutable[sIdx].copy(paxFee = valPax)
                                        operatorSeasons = mutable
                                    },
                                    label = AppLanguageManager.translate("Pax Başı Tutar"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                TourOSTextField(
                                    value = seasonItem.startDate,
                                    onValueChange = { newStart ->
                                        val mutable = operatorSeasons.toMutableList()
                                        mutable[sIdx] = mutable[sIdx].copy(startDate = newStart)
                                        operatorSeasons = mutable
                                    },
                                    label = AppLanguageManager.translate("Başlangıç Tarihi (YYYY-MM-DD)"),
                                    placeholder = "2026-06-01"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                TourOSTextField(
                                    value = seasonItem.endDate,
                                    onValueChange = { newEnd ->
                                        val mutable = operatorSeasons.toMutableList()
                                        mutable[sIdx] = mutable[sIdx].copy(endDate = newEnd)
                                        operatorSeasons = mutable
                                    },
                                    label = AppLanguageManager.translate("Bitiş Tarihi (YYYY-MM-DD)"),
                                    placeholder = "2026-09-30"
                                )
                            }
                        }
                    }
                }

                TourOSButton(
                    text = "➕ Yeni Sezon / Periyot Ekle",
                    onClick = {
                        val mutable = operatorSeasons.toMutableList()
                        mutable.add(
                            OperatorSeasonRate(
                                id = "s_${(1000..9999).random()}",
                                name = "Yeni Sezon",
                                startDate = "2026-06-01",
                                endDate = "2026-09-30",
                                commissionRate = 12.0,
                                paxFee = 0.0
                            )
                        )
                        operatorSeasons = mutable
                    },
                    variant = TourOSButtonVariant.SECONDARY
                )
            }
        }



        // ── BLOK 4: MUHASEBE & BANKA ────────────────────────────────────────
        FormSectionCard(
            title = AppLanguageManager.translate("Fatura & Banka Hesap Detayları"),
            icon = Icons.Default.AccountBalance
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(value = taxOffice, onValueChange = { taxOffice = it }, label = AppLanguageManager.translate("Vergi Dairesi"))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(value = taxNumber, onValueChange = { taxNumber = it }, label = AppLanguageManager.translate("Vergi Numarası"))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                    Box(modifier = Modifier.weight(1f)) {
                        TourOSTextField(value = bankName, onValueChange = { bankName = it }, label = AppLanguageManager.translate("Banka Adı & Şube"), placeholder = "Örn: İş Bankası Levent Şubesi")
                    }
                    Box(modifier = Modifier.weight(1.2f)) {
                        TourOSTextField(value = iban, onValueChange = { iban = it }, label = AppLanguageManager.translate("IBAN Numarası"), placeholder = "TR00 0000 0000 0000 0000 0000 00")
                    }
                }
            }
        }

        // ── BLOK 5: TEMSİLCİ & İLETİŞİM ─────────────────────────────────────
        FormSectionCard(
            title = AppLanguageManager.translate("Operatör Yetkili & İletişim Sorumlusu"),
            icon = Icons.Default.Phone
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)) {
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(value = contactName, onValueChange = { contactName = it }, label = AppLanguageManager.translate("Temsilci Adı Soyadı"), placeholder = "Örn: Ahmet Yılmaz")
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = AppLanguageManager.translate("Telefon / GSM"), placeholder = "+90 532 000 0000")
                }
                Box(modifier = Modifier.weight(1f)) {
                    TourOSTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = AppLanguageManager.translate("Kurumsal E-Posta"), placeholder = "b2b@operatorder.com")
                }
            }
        }

        HorizontalDivider(color = TourOSColors.Divider)

        // SAYFA ALT KAYDET & İPTAL BUTONLARI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackToList) {
                Text(AppLanguageManager.translate("İptal"), color = TourOSColors.TextSecondary)
            }
            Spacer(modifier = Modifier.width(TourOSSpacing.medium))
            TourOSButton(
                text = AppLanguageManager.translate("Değişiklikleri Kaydet"),
                onClick = {
                    val valAdj = priceAdjustmentValueStr.toDoubleOrNull() ?: 0.0
                    val valComm = commissionRateStr.toDoubleOrNull() ?: 8.0
                    val finalEntity = AgencyOperatorConnectionEntity(
                        id = initialOperator?.id ?: "",
                        agencyId = initialOperator?.agencyId ?: "agency-01",
                        operatorCompanyId = operatorCompanyId.ifBlank { "OP-${operatorName.take(4).uppercase()}" },
                        operatorName = operatorName.ifBlank { "Yeni Operatör" },
                        operatorLogo = operatorLogo,
                        operatorType = operatorType,
                        integrationType = integrationType,
                        apiEndpoint = apiEndpoint,
                        apiKey = apiKey,
                        priceAdjustmentType = priceAdjustmentType,
                        priceAdjustmentValue = valAdj,
                        commissionRate = valComm,
                        currency = currency,
                        taxOffice = taxOffice,
                        taxNumber = taxNumber,
                        iban = iban,
                        bankName = bankName,
                        contactName = contactName,
                        contactPhone = contactPhone,
                        contactEmail = contactEmail,
                        status = status
                    )
                    onSave(finalEntity)
                },
                variant = TourOSButtonVariant.PRIMARY
            )
        }
    }
}

// ─── VEKTÖREL İKONLU FORM BLOK KARTI BİLEŞENİ ───────────────────────────────

@Composable
private fun FormSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    TourOSCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = TourOSColors.Surface,
        contentPadding = TourOSSpacing.medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TourOSColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary),
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = TourOSColors.Divider.copy(alpha = 0.5f))
            content()
        }
    }
}

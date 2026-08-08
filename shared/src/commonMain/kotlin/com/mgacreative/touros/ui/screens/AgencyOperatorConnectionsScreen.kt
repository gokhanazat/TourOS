package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.data.database.entity.AgencyOperatorConnectionEntity
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AgencyOperatorConnectionsUiState
import com.mgacreative.touros.ui.viewmodel.AgencyOperatorConnectionsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Prompt 4.6.4 Tur Operatörü Kartı Ekranı (Acente Tarafı).
 * Connected operators list and new operator connection form.
 * Form includes: Operator Code/ID, Price adjustment type (percentage/fixed), Price adjustment value, Commission rate.
 * NO Email notification field! All notifications are in-app.
 */
@Composable
fun AgencyOperatorConnectionsScreen(
    viewModel: AgencyOperatorConnectionsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddModal by remember { mutableStateOf(false) }

    var operatorIdInput by remember { mutableStateOf("") }
    var priceAdjType by remember { mutableStateOf("percentage") } // "percentage" veya "fixed"
    var priceAdjValueInput by remember { mutableStateOf("0.0") }
    var commissionRateInput by remember { mutableStateOf("10.0") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
            .padding(TourOSSpacing.large)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bağlı Tur Operatörleri",
                    style = TourOSTypography.DisplaySmall,
                    color = TourOSColors.TextPrimary
                )
                Text(
                    text = "Acente Pazaryeri multi-operatör bağlantı ve komisyon yönetimi",
                    style = TourOSTypography.BodyMedium,
                    color = TourOSColors.TextSecondary
                )
            }
            TourOSButton(
                text = "+ Operatör Bağlantısı Ekle",
                onClick = { showAddModal = !showAddModal }
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        if (showAddModal) {
            TourOSCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = TourOSSpacing.large),
                backgroundColor = TourOSColors.Surface,
                borderColor = TourOSColors.Primary
            ) {
                Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
                    Text(
                        text = "Yeni Tur Operatörü Bağlantısı",
                        style = TourOSTypography.TitleLarge,
                        color = TourOSColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.small))

                    TourOSTextField(
                        value = operatorIdInput,
                        onValueChange = { operatorIdInput = it },
                        label = "Operatör Firma ID / Kodu"
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                    Text(
                        text = "Fiyat Ayarlama Tipi",
                        style = TourOSTypography.BodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = priceAdjType == "percentage",
                            onClick = { priceAdjType = "percentage" }
                        )
                        Text(text = "Yüzde (%)", modifier = Modifier.padding(end = 16.dp))
                        RadioButton(
                            selected = priceAdjType == "fixed",
                            onClick = { priceAdjType = "fixed" }
                        )
                        Text(text = "Sabit Tutar (₺/€/$)")
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.small))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = priceAdjValueInput,
                                onValueChange = { priceAdjValueInput = it },
                                label = if (priceAdjType == "percentage") "Fiyat Kar Marjı (%)" else "Sabit Kar Eklemeli (Tutar)"
                            )
                        }
                        Spacer(modifier = Modifier.width(TourOSSpacing.medium))
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = commissionRateInput,
                                onValueChange = { commissionRateInput = it },
                                label = "Hak Edilen Komisyon Oranı (%)"
                            )
                        }
                    }

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(TourOSSpacing.small))
                        Text(text = err, color = TourOSColors.Error, style = TourOSTypography.Label)
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TourOSButton(
                            text = "İptal",
                            onClick = { showAddModal = false },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        TourOSButton(
                            text = "Bağlantıyı Kaydet",
                            onClick = {
                                val valAdj = priceAdjValueInput.toDoubleOrNull() ?: 0.0
                                val valComm = commissionRateInput.toDoubleOrNull() ?: 10.0
                                if (operatorIdInput.isBlank()) {
                                    errorMessage = "Lütfen Operatör Firma ID/Kodu girin."
                                    return@TourOSButton
                                }
                                viewModel.createConnection(
                                    operatorCompanyId = operatorIdInput,
                                    priceAdjustmentType = priceAdjType,
                                    priceAdjustmentValue = valAdj,
                                    commissionRate = valComm,
                                    onSuccess = {
                                        showAddModal = false
                                        operatorIdInput = ""
                                        errorMessage = null
                                    },
                                    onError = { err -> errorMessage = err }
                                )
                            }
                        )
                    }
                }
            }
        }

        when (val state = uiState) {
            is AgencyOperatorConnectionsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TourOSColors.Primary)
                }
            }
            is AgencyOperatorConnectionsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = TourOSColors.Error)
                }
            }
            is AgencyOperatorConnectionsUiState.Success -> {
                if (state.connections.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Henüz bağlı bir tur operatörü yok. Yukarıdaki butondan ekleyebilirsiniz.",
                            color = TourOSColors.TextSecondary
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                    ) {
                        items(state.connections) { item ->
                            OperatorConnectionCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorConnectionCard(connection: AgencyOperatorConnectionEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(TourOSSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operatör: ${connection.operatorCompanyId.take(8)}...",
                    style = TourOSTypography.TitleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TourOSColors.Primary
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (connection.status == "ACTIVE") TourOSColors.Success.copy(alpha = 0.15f)
                            else TourOSColors.Error.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = connection.status,
                        color = if (connection.status == "ACTIVE") TourOSColors.Success else TourOSColors.Error,
                        style = TourOSTypography.Label
                    )
                }
            }

            Spacer(modifier = Modifier.height(TourOSSpacing.small))
            Text(
                text = "Fiyat Ayarlaması: ${if (connection.priceAdjustmentType == "percentage") "%${connection.priceAdjustmentValue} Kar Marjı" else "+${connection.priceAdjustmentValue} TL Sabit Ekleme"}",
                style = TourOSTypography.BodyMedium
            )
            Text(
                text = "Hak Edilen Komisyon: %${connection.commissionRate}",
                style = TourOSTypography.BodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TourOSColors.Secondary
            )
        }
    }
}

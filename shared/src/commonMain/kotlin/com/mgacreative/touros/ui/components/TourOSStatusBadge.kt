package com.mgacreative.touros.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

@Composable
fun TourOSStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TourOSColors.PrimaryContainer,
    textColor: Color = TourOSColors.Primary
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall)
            )
            .padding(horizontal = TourOSSpacing.small, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TourOSTypography.Caption.copy(
                color = textColor,
                fontSize = TourOSTypography.Caption.fontSize
            )
        )
    }
}

/**
 * Rezervasyon Durumlarına Özel Rozet Bileşeni.
 */
@Composable
fun TourOSBookingStatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, txtColor, label) = when (status) {
        BookingStatus.BEKLIYOR -> Triple(TourOSColors.WarningContainer, TourOSColors.Warning, status.displayName)
        BookingStatus.OPSIYON -> Triple(TourOSColors.InfoContainer, TourOSColors.Info, status.displayName)
        BookingStatus.ONAYLANDI -> Triple(TourOSColors.SuccessContainer, TourOSColors.Success, status.displayName)
        BookingStatus.IPTAL -> Triple(TourOSColors.ErrorContainer, TourOSColors.Error, status.displayName)
        BookingStatus.TAMAMLANDI -> Triple(TourOSColors.Surface, TourOSColors.TextSecondary, status.displayName)
    }

    TourOSStatusBadge(
        text = label,
        modifier = modifier,
        backgroundColor = bgColor,
        textColor = txtColor
    )
}

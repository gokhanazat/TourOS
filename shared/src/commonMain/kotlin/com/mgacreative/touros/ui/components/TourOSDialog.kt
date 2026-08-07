package com.mgacreative.touros.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

@Composable
fun TourOSDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    confirmButtonText: String = "Tamam",
    onConfirmClick: (() -> Unit)? = null,
    dismissButtonText: String? = "İptal",
    onDismissClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge),
            color = TourOSColors.Background,
            border = BorderStroke(TourOSSpacing.borderWidth, TourOSColors.Border)
        ) {
            Column(modifier = Modifier.padding(TourOSSpacing.xLarge)) {
                Text(
                    text = title,
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.TextPrimary)
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.large))

                content()

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    if (!dismissButtonText.isNullOrBlank() && onDismissClick != null) {
                        TourOSButton(
                            text = dismissButtonText,
                            onClick = onDismissClick,
                            variant = TourOSButtonVariant.TERTIARY
                        )
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                    }

                    if (onConfirmClick != null) {
                        TourOSButton(
                            text = confirmButtonText,
                            onClick = onConfirmClick,
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            }
        }
    }
}

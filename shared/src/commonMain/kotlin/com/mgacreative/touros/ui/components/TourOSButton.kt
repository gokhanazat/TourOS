package com.mgacreative.touros.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

enum class TourOSButtonVariant {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    DESTRUCTIVE
}

@Composable
fun TourOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TourOSButtonVariant = TourOSButtonVariant.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(TourOSSpacing.cornerRadius)

    when (variant) {
        TourOSButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TourOSColors.Primary,
                    contentColor = TourOSColors.OnPrimary,
                    disabledContainerColor = TourOSColors.TextDisabled,
                    disabledContentColor = TourOSColors.OnPrimary
                ),
                contentPadding = PaddingValues(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small)
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, contentColor = TourOSColors.OnPrimary)
            }
        }
        TourOSButtonVariant.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TourOSColors.Secondary,
                    contentColor = TourOSColors.OnSecondary,
                    disabledContainerColor = TourOSColors.TextDisabled,
                    disabledContentColor = TourOSColors.OnSecondary
                ),
                contentPadding = PaddingValues(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small)
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, contentColor = TourOSColors.OnSecondary)
            }
        }
        TourOSButtonVariant.TERTIARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled && !isLoading,
                shape = shape,
                border = BorderStroke(TourOSSpacing.borderWidth, if (enabled) TourOSColors.Border else TourOSColors.TextDisabled),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TourOSColors.TextPrimary,
                    disabledContentColor = TourOSColors.TextDisabled
                ),
                contentPadding = PaddingValues(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small)
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, contentColor = if (enabled) TourOSColors.TextPrimary else TourOSColors.TextDisabled)
            }
        }
        TourOSButtonVariant.DESTRUCTIVE -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(44.dp),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TourOSColors.Error,
                    contentColor = TourOSColors.OnError,
                    disabledContainerColor = TourOSColors.TextDisabled,
                    disabledContentColor = TourOSColors.OnError
                ),
                contentPadding = PaddingValues(horizontal = TourOSSpacing.large, vertical = TourOSSpacing.small)
            ) {
                ButtonContent(text = text, isLoading = isLoading, icon = icon, contentColor = TourOSColors.OnError)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    icon: (@Composable () -> Unit)?,
    contentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(TourOSSpacing.small))
        } else if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(TourOSSpacing.small))
        }
        Text(
            text = text,
            style = TourOSTypography.TitleMedium.copy(color = contentColor)
        )
    }
}

package com.mgacreative.touros.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography

@Composable
fun TourOSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = TourOSTypography.Label.copy(color = TourOSColors.TextPrimary)
            )
            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary),
            placeholder = if (!placeholder.isNullOrBlank()) {
                { Text(text = placeholder, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextDisabled)) }
            } else null,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(TourOSSpacing.cornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TourOSColors.Background,
                unfocusedContainerColor = TourOSColors.Background,
                disabledContainerColor = TourOSColors.Surface,
                errorContainerColor = TourOSColors.ErrorContainer,
                focusedBorderColor = TourOSColors.Primary,
                unfocusedBorderColor = TourOSColors.Border,
                disabledBorderColor = TourOSColors.Border,
                errorBorderColor = TourOSColors.Error
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
            Text(
                text = errorMessage,
                style = TourOSTypography.Caption.copy(color = TourOSColors.Error)
            )
        }
    }
}

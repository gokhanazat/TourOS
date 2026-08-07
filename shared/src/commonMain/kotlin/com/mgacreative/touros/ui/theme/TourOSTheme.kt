package com.mgacreative.touros.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorScheme = lightColorScheme(
    primary = TourOSColors.Primary,
    onPrimary = TourOSColors.OnPrimary,
    primaryContainer = TourOSColors.PrimaryContainer,
    onPrimaryContainer = TourOSColors.OnPrimaryContainer,
    secondary = TourOSColors.Secondary,
    onSecondary = TourOSColors.OnSecondary,
    secondaryContainer = TourOSColors.SecondaryContainer,
    onSecondaryContainer = TourOSColors.OnSecondaryContainer,
    background = TourOSColors.Background,
    onBackground = TourOSColors.TextPrimary,
    surface = TourOSColors.Surface,
    onSurface = TourOSColors.TextPrimary,
    surfaceVariant = TourOSColors.Surface,
    onSurfaceVariant = TourOSColors.TextSecondary,
    outline = TourOSColors.Border,
    outlineVariant = TourOSColors.Divider,
    error = TourOSColors.Error,
    onError = TourOSColors.OnError,
    errorContainer = TourOSColors.ErrorContainer,
    onErrorContainer = TourOSColors.Error
)

val LocalTourOSColors = staticCompositionLocalOf { TourOSColors }
val LocalTourOSTypography = staticCompositionLocalOf { TourOSTypography }
val LocalTourOSSpacing = staticCompositionLocalOf { TourOSSpacing }

/**
 * TourOS Tema Sağlayıcı (SADECE Light Tema)
 * Tüm platformlarda birebir aynı kurumsal görünümü sunar.
 */
@Composable
fun TourOSTheme(
    content: @Composable () -> Unit
) {
    val shapes = Shapes(
        small = RoundedCornerShape(TourOSSpacing.cornerRadiusSmall),
        medium = RoundedCornerShape(TourOSSpacing.cornerRadius),
        large = RoundedCornerShape(TourOSSpacing.cornerRadiusLarge)
    )

    CompositionLocalProvider(
        LocalTourOSColors provides TourOSColors,
        LocalTourOSTypography provides TourOSTypography,
        LocalTourOSSpacing provides TourOSSpacing
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = TourOSTypography.materialTypography,
            shapes = shapes,
            content = content
        )
    }
}

object TourOSTheme {
    val colors: TourOSColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTourOSColors.current

    val typography: TourOSTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTourOSTypography.current

    val spacing: TourOSSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalTourOSSpacing.current
}

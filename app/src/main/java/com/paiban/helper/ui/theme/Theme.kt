package com.paiban.helper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.paiban.helper.data.preferences.ThemeMode

internal fun fallbackLightColorScheme() = lightColorScheme(
    primary = PaibanPrimary,
    onPrimary = PaibanOnPrimary,
    primaryContainer = PaibanPrimaryContainer,
    onPrimaryContainer = PaibanOnPrimaryContainer,
    secondary = PaibanSecondary,
    onSecondary = PaibanOnSecondary,
    secondaryContainer = PaibanSecondaryContainer,
    onSecondaryContainer = PaibanOnSecondaryContainer,
    tertiary = PaibanTertiary,
    onTertiary = PaibanOnTertiary,
    tertiaryContainer = PaibanTertiaryContainer,
    onTertiaryContainer = PaibanOnTertiaryContainer,
    background = PaibanBackgroundLight,
    onBackground = PaibanOnBackgroundLight,
    surface = PaibanSurfaceLight,
    onSurface = PaibanOnSurfaceLight,
    surfaceVariant = PaibanSurfaceVariantLight,
    onSurfaceVariant = PaibanOnSurfaceVariantLight,
    surfaceContainer = PaibanSurfaceContainerLight,
    surfaceContainerHigh = PaibanSurfaceContainerHighLight,
    surfaceContainerHighest = PaibanSurfaceContainerHighestLight,
    outline = PaibanOutlineLight,
    outlineVariant = PaibanOutlineVariantLight,
    error = PaibanError,
    onError = PaibanOnError,
    errorContainer = PaibanErrorContainer,
    onErrorContainer = PaibanOnErrorContainer,
)

internal fun fallbackDarkColorScheme() = darkColorScheme(
    primary = PaibanPrimaryContainer,
    onPrimary = PaibanOnPrimaryContainer,
    primaryContainer = PaibanPrimary,
    onPrimaryContainer = PaibanOnPrimary,
    secondary = PaibanSecondary,
    onSecondary = PaibanOnSecondary,
    secondaryContainer = PaibanSecondaryContainerDark,
    onSecondaryContainer = PaibanOnSecondaryContainerDark,
    tertiary = PaibanTertiary,
    onTertiary = PaibanOnTertiary,
    tertiaryContainer = PaibanTertiaryContainerDark,
    onTertiaryContainer = PaibanOnTertiaryContainerDark,
    background = PaibanBackgroundDark,
    onBackground = PaibanOnBackgroundDark,
    surface = PaibanSurfaceDark,
    onSurface = PaibanOnSurfaceDark,
    surfaceVariant = PaibanSurfaceVariantDark,
    onSurfaceVariant = PaibanOnSurfaceVariantDark,
    surfaceContainer = PaibanSurfaceContainerDark,
    surfaceContainerHigh = PaibanSurfaceContainerHighDark,
    surfaceContainerHighest = PaibanSurfaceContainerHighestDark,
    outline = PaibanOutlineDark,
    outlineVariant = PaibanOutlineVariantDark,
    error = PaibanError,
    onError = PaibanOnError,
    errorContainer = PaibanErrorContainerDark,
    onErrorContainer = PaibanOnErrorContainer,
)

internal fun dynamicAccentAwareColorScheme(
    darkTheme: Boolean,
    dynamicColorEnabled: Boolean,
    dynamicPrimary: androidx.compose.ui.graphics.Color,
    dynamicSecondary: androidx.compose.ui.graphics.Color,
    dynamicTertiary: androidx.compose.ui.graphics.Color,
): ColorScheme {
    val base = if (darkTheme) fallbackDarkColorScheme() else fallbackLightColorScheme()
    if (!dynamicColorEnabled) {
        return base
    }
    return base.copy(
        primary = dynamicPrimary,
        secondary = dynamicSecondary,
        tertiary = dynamicTertiary,
        primaryContainer = dynamicPrimary,
        secondaryContainer = dynamicSecondary,
        tertiaryContainer = dynamicTertiary,
    )
}

@Composable
fun PaibanTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val context = LocalContext.current
    val systemScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        null
    }
    val colorScheme = dynamicAccentAwareColorScheme(
        darkTheme = darkTheme,
        dynamicColorEnabled = dynamicColor && systemScheme != null,
        dynamicPrimary = systemScheme?.primary ?: if (darkTheme) PaibanPrimaryContainer else PaibanPrimary,
        dynamicSecondary = systemScheme?.secondary ?: PaibanSecondary,
        dynamicTertiary = systemScheme?.tertiary ?: PaibanTertiary,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaibanTypography,
        content = content,
    )
}

package com.paiban.helper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.paiban.helper.data.preferences.ThemeMode

// ============================================================
// 排版助手品牌色（来自于 APK 原版）
// ============================================================

@Immutable
data class PaibanBrandColors(
    val gradientPrimary: Brush = Brush.linearGradient(
        colors = listOf(ApkGradientStart, ApkGradientEnd),
    ),
    val gradientPrimaryHorizontal: Brush = Brush.horizontalGradient(
        colors = listOf(ApkGradientStart, ApkGradientEnd),
    ),
    val formatButtonBg: Color = ApkFormatButtonBg,
    val formatButtonBgSelected: Color = ApkFormatButtonBgSelected,
    val formatButtonText: Color = ApkFormatButtonText,
    val formatButtonTextSelected: Color = ApkFormatButtonTextSelected,
    val editorBg: Color = ApkEditorBg,
    val editorBorder: Color = ApkEditorBorder,
    val editorText: Color = ApkEditorText,
    val editorHint: Color = ApkEditorHint,
    val cardBg: Color = ApkCardBg,
    val cardBorder: Color = ApkCardBorder,
    val bottomNavBg: Color = ApkBottomNavBg,
    val bottomNavSelected: Color = ApkBottomNavSelected,
    val bottomNavUnselected: Color = ApkBottomNavUnselected,
    // Dark variants
    val formatButtonBgDark: Color = Color(0xFF334155),
    val formatButtonBgSelectedDark: Color = Color(0xFF1E3A5F),
    val formatButtonTextDark: Color = Color(0xFFE2E8F0),
    val formatButtonTextSelectedDark: Color = Color(0xFF93C5FD),
    val editorBgDark: Color = Color(0xFF1E293B),
    val editorBorderDark: Color = Color(0xFF334155),
    val editorTextDark: Color = Color(0xFFF1F5F9),
    val editorHintDark: Color = Color(0xFF64748B),
    val bottomNavBgDark: Color = Color(0xFF1E293B),
)

val LightPaibanBrandColors = PaibanBrandColors()
val DarkPaibanBrandColors = PaibanBrandColors(
    gradientPrimary = Brush.linearGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF818CF8)),
    ),
    gradientPrimaryHorizontal = Brush.horizontalGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF818CF8)),
    ),
    formatButtonBg = Color(0xFF334155),
    formatButtonBgSelected = Color(0xFF1E3A5F),
    formatButtonText = Color(0xFFE2E8F0),
    formatButtonTextSelected = Color(0xFF93C5FD),
    editorBg = Color(0xFF1E293B),
    editorBorder = Color(0xFF334155),
    editorText = Color(0xFFF1F5F9),
    editorHint = Color(0xFF64748B),
)

val LocalPaibanBrandColors = staticCompositionLocalOf { LightPaibanBrandColors }

internal fun fallbackLightColorScheme() = lightColorScheme(
    primary = ApkPrimary,
    onPrimary = ApkOnPrimary,
    primaryContainer = ApkPrimaryContainer,
    onPrimaryContainer = ApkOnPrimaryContainer,
    secondary = ApkSecondary,
    onSecondary = ApkOnSecondary,
    secondaryContainer = ApkSecondaryContainer,
    onSecondaryContainer = ApkOnSecondaryContainer,
    tertiary = ApkTertiary,
    onTertiary = ApkOnTertiary,
    tertiaryContainer = ApkTertiaryContainer,
    onTertiaryContainer = ApkOnTertiaryContainer,
    background = ApkBackgroundLight,
    onBackground = ApkOnBackgroundLight,
    surface = ApkSurfaceLight,
    onSurface = ApkOnSurfaceLight,
    surfaceVariant = ApkSurfaceVariantLight,
    onSurfaceVariant = ApkOnSurfaceVariantLight,
    surfaceContainer = ApkSurfaceContainerLight,
    surfaceContainerHigh = ApkSurfaceContainerHighLight,
    surfaceContainerHighest = ApkSurfaceContainerHighestLight,
    outline = ApkOutlineLight,
    outlineVariant = ApkOutlineVariantLight,
    error = ApkError,
    onError = ApkOnError,
    errorContainer = ApkErrorContainer,
    onErrorContainer = ApkOnErrorContainer,
)

internal fun fallbackDarkColorScheme() = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF1E3A5F),
    primaryContainer = Color(0xFF1D4ED8),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFA5B4FC),
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF4F46E5),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Color(0xFFFCD34D),
    onTertiary = Color(0xFF78350F),
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = ApkBackgroundDark,
    onBackground = ApkOnBackgroundDark,
    surface = ApkSurfaceDark,
    onSurface = ApkOnSurfaceDark,
    surfaceVariant = ApkSurfaceVariantDark,
    onSurfaceVariant = ApkOnSurfaceVariantDark,
    surfaceContainer = ApkSurfaceContainerDark,
    surfaceContainerHigh = ApkSurfaceContainerHighDark,
    surfaceContainerHighest = ApkSurfaceContainerHighestDark,
    outline = ApkOutlineDark,
    outlineVariant = ApkOutlineVariantDark,
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
)

// ============================================================
// 排版
// ============================================================

val PaibanTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
)

// ============================================================
// 主题
// ============================================================

@Composable
fun PaibanTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val context = LocalContext.current
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme: ColorScheme = if (useDynamic) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) fallbackDarkColorScheme() else fallbackLightColorScheme()
    }

    val brandColors = if (darkTheme) DarkPaibanBrandColors else LightPaibanBrandColors

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = PaibanTypography,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalPaibanBrandColors provides brandColors,
        ) {
            content()
        }
    }
}

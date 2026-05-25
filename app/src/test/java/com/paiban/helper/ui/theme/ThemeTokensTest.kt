package com.paiban.helper.ui.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ThemeTokensTest {
    @Test
    fun fallbackLightColorSchemeExposesExtendedMaterialTokens() {
        val scheme = fallbackLightColorScheme()

        assertEquals(PaibanPrimary, scheme.primary)
        assertEquals(PaibanSecondaryContainer, scheme.secondaryContainer)
        assertEquals(PaibanTertiary, scheme.tertiary)
        assertEquals(PaibanSurfaceVariantLight, scheme.surfaceVariant)
        assertEquals(PaibanSurfaceContainerLight, scheme.surfaceContainer)
        assertEquals(PaibanSurfaceContainerHighLight, scheme.surfaceContainerHigh)
        assertEquals(PaibanSurfaceContainerHighestLight, scheme.surfaceContainerHighest)
        assertEquals(PaibanOutlineLight, scheme.outline)
        assertEquals(PaibanOutlineVariantLight, scheme.outlineVariant)
        assertEquals(PaibanError, scheme.error)
        assertEquals(PaibanOnErrorContainer, scheme.onErrorContainer)
    }

    @Test
    fun fallbackDarkColorSchemeExposesExtendedMaterialTokens() {
        val scheme = fallbackDarkColorScheme()

        assertEquals(PaibanBackgroundDark, scheme.background)
        assertEquals(PaibanSurfaceDark, scheme.surface)
        assertEquals(PaibanOnBackgroundDark, scheme.onBackground)
        assertEquals(PaibanSurfaceVariantDark, scheme.surfaceVariant)
        assertEquals(PaibanSurfaceContainerDark, scheme.surfaceContainer)
        assertEquals(PaibanSurfaceContainerHighDark, scheme.surfaceContainerHigh)
        assertEquals(PaibanSurfaceContainerHighestDark, scheme.surfaceContainerHighest)
        assertEquals(PaibanOutlineDark, scheme.outline)
    }

    @Test
    fun paibanTypographyDefinesReadableHierarchy() {
        val typography = PaibanTypography

        assertEquals(32, typography.headlineMedium.fontSize.value.toInt())
        assertEquals(FontWeight.SemiBold, typography.headlineMedium.fontWeight)
        assertEquals(22, typography.titleLarge.fontSize.value.toInt())
        assertEquals(FontWeight.Medium, typography.titleMedium.fontWeight)
        assertEquals(16, typography.bodyLarge.fontSize.value.toInt())
        assertEquals(16, typography.bodyMedium.fontSize.value.toInt())
        assertEquals(12, typography.labelSmall.fontSize.value.toInt())
        assertNotEquals(typography.bodyMedium, typography.labelSmall)
    }
}

package com.paiban.helper.ui.common

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun pageContentPadding(): PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

@Composable
fun appHorizontalAndTopInsets(): WindowInsets {
    return WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
}

@Composable
fun previewFloatingActionInsets(): WindowInsets {
    return WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
}

fun View.captureSystemBarInsets(
    onInsetsChanged: (statusBars: Insets, navigationBars: Insets) -> Unit,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        onInsetsChanged(statusBars, navigationBars)
        insets
    }
    requestApplyInsets()
}

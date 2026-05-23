package com.paiban.helper.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paiban.helper.navigation.AppDestination

@Composable
fun AppScaffold(
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content,
    )
}

internal fun isDestinationSelected(
    currentRoute: String?,
    destination: AppDestination,
): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == destination.route) return true

    val nextCharIndex = destination.route.length
    return currentRoute.startsWith(destination.route) &&
        currentRoute.getOrNull(nextCharIndex)?.let { it == '/' || it == '?' } == true
}

package com.paiban.helper.ui.common

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun AccessibleSnackbarHost(
    hostState: SnackbarHostState,
) {
    SnackbarHost(hostState = hostState)
}

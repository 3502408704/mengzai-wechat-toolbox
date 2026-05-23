package com.paiban.helper.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class PageHeaderModel(
    val title: String,
    val subtitle: String? = null,
)

data class AppPageContentPadding(
    val scaffoldPadding: PaddingValues,
    val contentPadding: PaddingValues = pageContentPadding(),
) {
    fun asPaddingValues(): PaddingValues = CombinedPaddingValues(
        outer = scaffoldPadding,
        inner = contentPadding,
    )

    fun contentPaddingOnly(): PaddingValues = contentPadding
}

private data class CombinedPaddingValues(
    val outer: PaddingValues,
    val inner: PaddingValues,
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
        outer.calculateLeftPadding(layoutDirection) + inner.calculateLeftPadding(layoutDirection)

    override fun calculateTopPadding() = outer.calculateTopPadding() + inner.calculateTopPadding()

    override fun calculateRightPadding(layoutDirection: LayoutDirection) =
        outer.calculateRightPadding(layoutDirection) + inner.calculateRightPadding(layoutDirection)

    override fun calculateBottomPadding() = outer.calculateBottomPadding() + inner.calculateBottomPadding()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPage(
    header: PageHeaderModel,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contextStrip: (@Composable () -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    content: @Composable (AppPageContentPadding) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.semantics(mergeDescendants = true) {},
                    ) {
                        Text(
                            text = header.title,
                            modifier = Modifier.clearAndSetSemantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                        )
                        header.subtitle?.let { subtitle ->
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    navigationIcon?.invoke()
                },
                actions = actions,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        bottomBar = {
            bottomAction?.invoke()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            contextStrip?.invoke()
            content(
                AppPageContentPadding(
                    scaffoldPadding = innerPadding,
                    contentPadding = pageContentPadding(),
                )
            )
        }
    }
}

package com.paiban.helper.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.paiban.helper.ui.ai.AiAssistantScreen
import com.paiban.helper.ui.editor.EditorRoute
import com.paiban.helper.ui.history.HistoryRoute
import com.paiban.helper.ui.preview.PreviewRoute
import com.paiban.helper.ui.preview.PreviewRouteSource
import com.paiban.helper.ui.settings.SettingsRoute
import com.paiban.helper.ui.settings.HelpScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = AppDestination.Editor.route,
) {
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val showBottomBar = currentDestination
                ?.hierarchy
                ?.any { dest ->
                    AppDestination.topLevelDestinations().any { it.route == dest.route }
                } == true
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    AppDestination.topLevelDestinations().forEach { destination ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == destination.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon(),
                                contentDescription = destination.label,
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                ),
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                }
        }
    }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(AppDestination.Editor.route) {
                EditorRoute(
                    onNavigatePreview = { navController.navigate(AppDestination.previewEditorRoute()) },
                    onNavigateAi = { navController.navigate(AppDestination.AiAssistant.route) },
                )
            }
            composable(AppDestination.History.route) {
                HistoryRoute(
                    onNavigatePreview = { route -> navController.navigate(route) },
                    onNavigateEditor = {
                        navController.navigate(AppDestination.Editor.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            // Templates now use inline ModalBottomSheet in EditorScreen
            composable(AppDestination.AiAssistant.route) {
                AiAssistantScreen(
                    onDismiss = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Settings.route) {
                SettingsRoute(
                    onNavigateHelp = { navController.navigate(AppDestination.Help.route) },
                )
            }
            composable(AppDestination.Help.route) {
                HelpScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.PreviewEditor.route) {
                PreviewRoute(
                    source = PreviewRouteSource.Editor,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(
                route = AppDestination.PreviewHistory.route,
                arguments = listOf(
                    navArgument("historyId") { type = NavType.LongType },
                ),
            ) { entry ->
                PreviewRoute(
                    source = PreviewRouteSource.History(
                        historyId = entry.arguments?.getLong("historyId") ?: -1L,
                    ),
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun AppDestination.icon() = when (this) {
    AppDestination.Editor -> Icons.Outlined.Edit
    AppDestination.History -> Icons.Outlined.History
    AppDestination.Settings -> Icons.Outlined.Settings
    AppDestination.Templates -> Icons.Outlined.Edit
    AppDestination.AiAssistant -> Icons.Outlined.Edit
    AppDestination.PreviewEditor -> Icons.Outlined.Edit
    AppDestination.PreviewHistory -> Icons.Outlined.History
    AppDestination.Help -> Icons.Outlined.Settings
}



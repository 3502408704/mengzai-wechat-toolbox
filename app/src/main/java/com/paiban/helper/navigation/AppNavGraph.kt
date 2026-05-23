package com.paiban.helper.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.paiban.helper.ui.editor.TemplateSelectionRoute
import com.paiban.helper.ui.history.HistoryRoute
import com.paiban.helper.ui.preview.PreviewRoute
import com.paiban.helper.ui.preview.PreviewRouteSource
import com.paiban.helper.ui.settings.SettingsRoute

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
            NavigationBar {
                AppDestination.topLevelDestinations().forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == destination.route } == true,
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
                        label = { androidx.compose.material3.Text(destination.label) },
                    )
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
                    onNavigateTemplates = { navController.navigate(AppDestination.Templates.route) },
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
            composable(AppDestination.Templates.route) {
                TemplateSelectionRoute(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.AiAssistant.route) {
                AiAssistantScreen()
            }
            composable(AppDestination.Settings.route) {
                SettingsRoute()
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
    AppDestination.Editor -> Icons.Outlined.MenuBook
    AppDestination.History -> Icons.Outlined.History
    AppDestination.AiAssistant -> Icons.Outlined.Palette
    AppDestination.Settings -> Icons.Outlined.Settings
    AppDestination.Templates -> Icons.Outlined.MenuBook
    AppDestination.PreviewEditor -> Icons.Outlined.MenuBook
    AppDestination.PreviewHistory -> Icons.Outlined.History
}

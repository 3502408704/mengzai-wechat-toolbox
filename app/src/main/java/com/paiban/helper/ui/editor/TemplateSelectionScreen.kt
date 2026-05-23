package com.paiban.helper.ui.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.heading
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.domain.template.TemplateCategory
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel

@Composable
fun TemplateSelectionRoute(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    TemplateSelectionScreen(
        options = state.availableTemplates,
        selectedTemplateId = state.pendingTemplateId,
        onSelectTemplate = viewModel::selectTemplate,
        onConfirmSelection = {
            viewModel.confirmTemplateSelection()
            onNavigateBack()
        },
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun TemplateSelectionScreen(
    options: List<TemplateOption>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit,
    onConfirmSelection: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val groupedOptions = TemplateCategory.defaults.mapNotNull { category ->
        val categoryOptions = options.filter { it.categoryId == category.id }
        category.takeIf { categoryOptions.isNotEmpty() }?.let { it to categoryOptions }
    }

    AppPage(
        header = PageHeaderModel(
            title = "选择模板",
            subtitle = "用于当前草稿",
        ),
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "返回",
                )
            }
        },
        bottomAction = {
            Surface(shadowElevation = 2.dp) {
                Button(
                    onClick = onConfirmSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                ) {
                    Text("确定使用")
                }
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = contentPadding.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(groupedOptions, key = { it.first.id }) { (category, categoryOptions) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    categoryOptions.forEach { option ->
                        TemplateSelectionRow(
                            option = option,
                            selected = option.id == selectedTemplateId,
                            onClick = { onSelectTemplate(option.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateSelectionRow(
    option: TemplateOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) {},
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(option.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

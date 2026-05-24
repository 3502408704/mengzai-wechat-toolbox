package com.paiban.helper.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.domain.template.TemplateCategory

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
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
@androidx.compose.material3.ExperimentalMaterial3Api
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "选择模板",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            "用于当前草稿",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = onConfirmSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        ,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "确定使用模板",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(groupedOptions, key = { it.first.id }) { (category, categoryOptions) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier
                            .semantics { heading() }
                            .padding(bottom = 4.dp),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 2.dp else 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    option.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    ),
                )
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

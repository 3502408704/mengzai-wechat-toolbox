package com.paiban.helper.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel

internal fun aiAssistantSubtitle(): String = "实验室功能，即将开放"

internal fun aiAssistantCardContentDescription(): String = "AI 辅助，即将开放"

@Composable
fun AiAssistantScreen() {
    AppPage(
        header = PageHeaderModel(
            title = "AI 辅助",
            subtitle = aiAssistantSubtitle(),
        ),
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding.asPaddingValues()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = aiAssistantCardContentDescription()
                        stateDescription = "实验室功能，后续版本开放，不可用"
                    }
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("实验室功能，后续版本开放", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "后续版本将提供原生 AI 辅助编辑体验，并支持通过配置 API 开箱即用。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

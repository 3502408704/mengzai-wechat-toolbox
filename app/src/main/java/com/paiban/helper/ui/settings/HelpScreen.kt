package com.paiban.helper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
) {
    AppPage(
        header = PageHeaderModel(title = "\u5e2e\u52a9"),
        navigationIcon = {
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.semantics { contentDescription = "\u8fd4\u56de" },
            ) {
                Text("\u2190 \u8fd4\u56de")
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding.asPaddingValues())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── 快速入门 ──
            SectionHeader("\u5feb\u901f\u5165\u95e8")

            HelpCard(
                title = "\u7f16\u8f91\u5185\u5bb9",
                body = "\u5728\u7f16\u8f91\u9875\u8f93\u5165 Markdown \u6216 HTML \u5185\u5bb9\u3002" +
                        "\u652f\u6301\u52a0\u7c97\u3001\u659c\u4f53\u3001\u6807\u9898\u3001\u5f15\u7528\u3001\u5217\u8868\u3001\u4ee3\u7801\u5757\u7b49\u5e38\u7528\u683c\u5f0f\u3002" +
                        "\u70b9\u51fb\u201c\u5e38\u7528\u683c\u5f0f\u201d\u6309\u94ae\u6253\u5f00\u683c\u5f0f\u5de5\u5177\u7bb1\u3002",
            )

            HelpCard(
                title = "\u9884\u89c8\u6548\u679c",
                body = "\u7f16\u8f91\u5b8c\u6210\u540e\u70b9\u51fb\u201c\u9884\u89c8\u6210\u54c1\u201d\u6309\u94ae\uff0c" +
                        "\u67e5\u770b\u6392\u7248\u6e32\u67d3\u6548\u679c\u3002\u53ef\u4ee5\u7f29\u653e\u3001\u590d\u5236\u3001\u5206\u4eab\u6216\u5bfc\u51fa\u4e3a HTML \u6587\u4ef6\u3002",
            )

            HelpCard(
                title = "\u590d\u5236\u4e0e\u53d1\u5e03",
                body = "\u5728\u9884\u89c8\u9875\u70b9\u51fb\u201c\u590d\u5236\u201d\uff0c\u5185\u5bb9\u5c06\u4fdd\u5b58\u5230\u526a\u8d34\u677f\u3002" +
                        "\u76f4\u63a5\u7c98\u8d34\u5230\u516c\u4f17\u53f7\u7f16\u8f91\u5668\u5373\u53ef\u53d1\u5e03\u3002",
            )

            // ── AI 辅助 ──
            SectionHeader("AI \u8f85\u52a9")

            HelpCard(
                title = "\u4f7f\u7528 AI \u7f16\u8f91",
                body = "\u70b9\u51fb\u7f16\u8f91\u9875\u7684\u201cAI \u7f16\u8f91\u201d\u6309\u94ae\uff0c" +
                        "\u53ef\u4ee5\u8bf7 AI \u5e2e\u52a9\u6da6\u8272\u3001\u6269\u5199\u3001\u7f29\u5199\u3001\u62df\u6807\u9898\u6216\u64b0\u5199\u6458\u8981\u3002" +
                        "\u652f\u6301\u591a\u6b21\u5bf9\u8bdd\uff0c\u5386\u53f2\u8bb0\u5f55\u81ea\u52a8\u4fdd\u5b58\u3002",
            )

            HelpCard(
                title = "\u914d\u7f6e AI \u6a21\u578b",
                body = "\u524d\u5f80\u201c\u8bbe\u7f6e\u201d\u201cAI \u914d\u7f6e\u201d\uff0c" +
                        "\u53ef\u4ee5\u6dfb\u52a0\u591a\u4e2a AI \u670d\u52a1\u5668\u5730\u5740\u548c API Key\u3002" +
                        "\u5efa\u8bae\u4f7f\u7528\u652f\u6301\u6d41\u5f0f\u54cd\u5e94\u7684\u6a21\u578b\u4ee5\u83b7\u5f97\u66f4\u597d\u4f53\u9a8c\u3002",
            )

            // ── 历史记录 ──
            SectionHeader("\u5386\u53f2\u8bb0\u5f55")

            HelpCard(
                title = "\u81ea\u52a8\u4fdd\u5b58",
                body = "\u6bcf\u6b21\u9884\u89c8\u540e\uff0c\u5f53\u524d\u5185\u5bb9\u4f1a\u81ea\u52a8\u4fdd\u5b58\u5230\u5386\u53f2\u8bb0\u5f55\u3002" +
                        "\u53ef\u4ee5\u5728\u201c\u5386\u53f2\u201d\u6807\u7b7e\u9875\u67e5\u770b\u6240\u6709\u4fdd\u5b58\u7684\u5feb\u7167\u3002" +
                        "\u652f\u6301\u518d\u6b21\u9884\u89c8\u3001\u7f16\u8f91\u6216\u5220\u9664\u3002",
            )

            // ── 无障碍使用 ──
            SectionHeader("\u65e0\u969c\u788d\u4f7f\u7528")

            HelpCard(
                title = "TalkBack \u5c4f\u5e55\u9605\u8bfb\u5668",
                body = "\u672c\u5de5\u5177\u5b8c\u5168\u652f\u6301 TalkBack \u65e0\u969c\u788d\u6d4f\u89c8\u3002" +
                        "\u53cc\u6307\u5de6\u53f3\u6ed1\u52a8\u6d4f\u89c8\u5143\u7d20\uff0c\u53cc\u6307\u5355\u51fb\u9009\u4e2d\u3002" +
                        "\u6240\u6709\u6309\u94ae\u548c\u64cd\u4f5c\u5747\u6709\u8bed\u97f3\u63d0\u793a\u3002",
            )

            HelpCard(
                title = "\u952e\u76d8\u5bfc\u822a",
                body = "\u8fde\u63a5\u5916\u63a5\u952e\u76d8\u540e\uff0c\u53ef\u4ee5\u7528 Tab \u952e\u5faa\u73af\u6d4f\u89c8\u5143\u7d20\uff0c" +
                        "Enter \u952e\u6267\u884c\u64cd\u4f5c\u3002\u6240\u6709\u529f\u80fd\u5747\u53ef\u901a\u8fc7\u952e\u76d8\u5b8c\u6210\u3002",
            )

            HelpCard(
                title = "\u5bf9\u6bd4\u5ea6\u4e0e\u89c6\u89c9",
                body = "\u5de5\u5177\u652f\u6301\u6df1\u8272\u6a21\u5f0f\u548c\u52a8\u6001\u53d6\u8272\u3002" +
                        "\u6587\u672c\u5bf9\u6bd4\u5ea6\u7b26\u5408 WCAG AA \u6807\u51c6\uff0c\u786e\u4fdd\u4f4e\u89c6\u529b\u7528\u6237\u4e5f\u80fd\u6e05\u6670\u9605\u8bfb\u3002" +
                        "\u53ef\u4ee5\u5728\u201c\u8bbe\u7f6e\u201d\u4e2d\u8c03\u6574\u4e3b\u9898\u3002",
            )

            // ── 常见问题 ──
            SectionHeader("\u5e38\u89c1\u95ee\u9898")

            HelpCard(
                title = "\u5982\u4f55\u5bfc\u5165\u6587\u4ef6\uff1f",
                body = "\u5728\u7f16\u8f91\u9875\u9876\u90e8\u83dc\u5355\u4e2d\u9009\u62e9\u201c\u5bfc\u5165\u6587\u4ef6\u201d\uff0c" +
                        "\u652f\u6301\u5bfc\u5165 .txt \u548c .md \u6587\u4ef6\u3002\u4e5f\u652f\u6301\u4ece\u526a\u8d34\u677f\u76f4\u63a5\u7c98\u8d34\u3002",
            )

            HelpCard(
                title = "\u5982\u4f55\u64a4\u9500\u64cd\u4f5c\uff1f",
                body = "\u7f16\u8f91\u9875\u9876\u90e8\u5de5\u5177\u680f\u63d0\u4f9b\u201c\u64a4\u9500\u201d\u548c\u201c\u91cd\u505a\u201d\u6309\u94ae\u3002" +
                        "\u652f\u6301\u591a\u6b65\u64a4\u9500\u3002",
            )

            HelpCard(
                title = "\u5982\u4f55\u5220\u9664\u6240\u6709\u5386\u53f2\u8bb0\u5f55\uff1f",
                body = "\u5728\u201c\u5386\u53f2\u201d\u9875\u70b9\u51fb\u9876\u90e8\u7684\u201c\u6e05\u7a7a\u201d\u6309\u94ae\u5373\u53ef\u5220\u9664\u6240\u6709\u5386\u53f2\u8bb0\u5f55\u3002" +
                        "\u6b64\u64cd\u4f5c\u4e0d\u53ef\u64a4\u9500\uff0c\u8bf7\u786e\u8ba4\u540e\u518d\u6267\u884c\u3002",
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "\u7248\u672c 0.81  \u00b7  \u68a6\u5b50\u516c\u4f17\u53f7\u6392\u7248\u5de5\u5177",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "\u7248\u672c\u4fe1\u606f" }
                    .padding(bottom = 32.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun HelpCard(
    title: String,
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

# 预览页顶栏精简 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将预览页顶栏从 9 个控件精简到 3 个（返回/⋮/缩放滑块），刷新改为下拉手势，分享/复制/导出折叠入更多菜单。

**Architecture:** 重写 `PreviewTopBar` composable，将原有分散按钮替换为 DropdownMenu + Slider；`PreviewScreen` 中 WebView 包裹 `pullToRefresh`；`PreviewViewModel.updateZoomPercent` 接受 Float 并吸附到 5 的倍数。

**Tech Stack:** Jetpack Compose, Material3 (Slider, DropdownMenu, pullToRefresh), Kotlin

---

### Task 1: ViewModel 适配 —— updateZoomPercent 改为接收 Float

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`

- [ ] **Step 1: 修改 updateZoomPercent 签名和实现**

将 `updateZoomPercent(rawValue: Int)` 改为 `updateZoomPercent(rawValue: Float)`，吸附到最近的 5 的倍数：

```kotlin
fun updateZoomPercent(rawValue: Float) {
    val snappedValue = (rawValue / 5f).roundToInt() * 5
        .coerceIn(85, 150)
    if (snappedValue != _uiState.value.zoomPercent) {
        _uiState.value = _uiState.value.copy(
            zoomPercent = snappedValue,
            transientMessage = "已缩放到 ${snappedValue}%",
        )
    }
}
```

需要新增 import：`import kotlin.math.roundToInt`

- [ ] **Step 2: 修改 resetZoom 的 transientMessage**

按 Concise Labels 规则，`"已恢复原始比例"` → `"已重置"`：

```kotlin
fun resetZoom() {
    if (_uiState.value.zoomPercent != 100) {
        _uiState.value = _uiState.value.copy(
            zoomPercent = 100,
            transientMessage = "已重置",
        )
    }
}
```

- [ ] **Step 3: 编译验证**

```powershell
./gradlew :app:compileDebugKotlin 2>&1 | Select-Object -Last 20
```

预期：BUILD SUCCESSFUL

---

### Task 2: PreviewTopBar 重写 —— 精简为返回 + 标题 + ⋮ + 滑块

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`

- [ ] **Step 1: 更新 imports**

新增：
```kotlin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
```

移除不再需要的：
```kotlin
import androidx.compose.material.icons.outlined.Refresh       // 删除
import androidx.compose.material.icons.outlined.ZoomIn         // 删除
import androidx.compose.material.icons.outlined.ZoomOut        // 删除
import androidx.compose.material3.TextButton                   // 删除（确认其他地方不用）
```

- [ ] **Step 2: 修改 PreviewRoute 的 onZoomChange 类型**

`onZoomChange: (Int) -> Unit` → `onZoomChange: (Float) -> Unit`

```kotlin
onZoomChange = { viewModel.updateZoomPercent(it) },
```

移除 `onZoomIn`/`onZoomOut` 专用的 lambda，slider 直接传 Float。

- [ ] **Step 3: 修改 PreviewScreen 签名**

移除 `onRefresh`、`onShare`、`onExport`、`onResetZoom`、`onCopy` 参数，改为：

```kotlin
@Composable
fun PreviewScreen(
    source: PreviewRouteSource,
    state: PreviewUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onResetZoom: () -> Unit,
    onZoomChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState,
)
```

签名不改（保持传给 PreviewTopBar），但 TopBar 内部不再直接使用。

- [ ] **Step 4: 重写 PreviewTopBar**

```kotlin
@Composable
private fun PreviewTopBar(
    source: PreviewRouteSource,
    zoomPercent: Int,
    onNavigateBack: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onResetZoom: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 第一行：返回 + 标题 + ⋮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.semantics { contentDescription = "返回" },
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = "预览",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = previewSourceSubtitle(source),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.semantics { contentDescription = "更多选项" },
                    ) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("复制") },
                            onClick = {
                                menuExpanded = false
                                onCopy()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("导出") },
                            onClick = {
                                menuExpanded = false
                                onExport()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.FileDownload, contentDescription = null)
                            },
                        )
                    }
                }
            }

            // 第二行：缩放滑块
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = zoomPercent.toFloat(),
                    onValueChange = onZoomChange,
                    valueRange = 85f..150f,
                    steps = 12, // (150-85)/5 - 1 = 12
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "缩放比例"
                            stateDescription = "${zoomPercent}%"
                        },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = lerpZoomTrackColor(zoomPercent),
                    ),
                )
                Text(
                    text = previewZoomLabel(zoomPercent),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .semantics {
                            contentDescription = "缩放"
                            stateDescription = "${zoomPercent}%"
                        }
                        .clickable(
                            interactionSource = null,
                            indication = null,
                        ) { onResetZoom() },
                )
            }
        }
    }
}

/** 缩放轨道颜色渐变：缩小端冷色 → 100% 主题色 → 放大端暖色 */
@Composable
private fun lerpZoomTrackColor(zoomPercent: Int): androidx.compose.ui.graphics.Color {
    val fraction = ((zoomPercent - 85).coerceIn(0, 65) / 65f)
    val cold = androidx.compose.ui.graphics.Color(0xFF4A90D9)
    val hot = androidx.compose.ui.graphics.Color(0xFFE5734A)
    return if (zoomPercent <= 100) {
        androidx.compose.ui.graphics.Color(
            red = androidx.compose.ui.graphics.lerp(cold.red, MaterialTheme.colorScheme.primary.red, fraction / 0.2308f),
            green = androidx.compose.ui.graphics.lerp(cold.green, MaterialTheme.colorScheme.primary.green, fraction / 0.2308f),
            blue = androidx.compose.ui.graphics.lerp(cold.blue, MaterialTheme.colorScheme.primary.blue, fraction / 0.2308f),
        )
    } else {
        androidx.compose.ui.graphics.Color(
            red = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primary.red, hot.red, (fraction - 0.2308f) / 0.7692f),
            green = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primary.green, hot.green, (fraction - 0.2308f) / 0.7692f),
            blue = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primary.blue, hot.blue, (fraction - 0.2308f) / 0.7692f),
        )
    }
}
```

移除 `previewZoomSteps()` 函数（不再需要）。

- [ ] **Step 5: 编译验证**

```powershell
./gradlew :app:compileDebugKotlin 2>&1 | Select-Object -Last 20
```

预期：BUILD SUCCESSFUL

---

### Task 3: WebView 包裹 pullToRefresh

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`

- [ ] **Step 1: 用 PullToRefreshBox 包裹 WebView**

在 `PreviewScreen` 中找到 WebView 的 `AndroidView`，用 `PullToRefreshBox` 包裹：

```kotlin
// === WebView 预览区（下拉刷新）===
PullToRefreshBox(
    isRefreshing = false,
    onRefresh = {
        onRefresh()
        // PullToRefreshBox 内部管理 refreshing 状态，
        // 刷新完成后 LaunchedEffect 观察 transientMessage 关闭指示器
    },
    modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "排版预览"
                stateDescription = previewRegionStateDescription(state.zoomPercent)
                liveRegion = LiveRegionMode.Polite
            },
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.setSupportZoom(true)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                    }
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                state.htmlDocument,
                "text/html",
                "utf-8",
                null,
            )
            webView.post {
                webView.setInitialScale(state.zoomPercent)
            }
        },
    )
}
```

注意：`PullToRefreshBox` 需要 `androidx.compose.material3:material3` 版本 ≥ 1.3.0。如果项目 Material3 版本不足，改用 `pullRefresh` modifier + `PullRefreshIndicator`。

先检查版本：

```powershell
Select-String -Path "app/build.gradle.kts" -Pattern "material3"
```

如果版本 < 1.3.0，使用备选方案：

```kotlin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
// 或备选：
import androidx.compose.material.pullrefresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.rememberPullRefreshState
```

- [ ] **Step 2: 编译验证**

```powershell
./gradlew :app:compileDebugKotlin 2>&1 | Select-Object -Last 20
```

预期：BUILD SUCCESSFUL

---

### Task 4: 清理 & 无障碍验证

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`

- [ ] **Step 1: 移除未使用的 imports**

确认以下已移除：
- `Refresh`, `ZoomIn`, `ZoomOut` 
- `TextButton`（如果 `PreviewScreen.kt` 内不再使用）
- `previewZoomSteps()` 函数

- [ ] **Step 2: 检查 PreviewTopBar 传递的参数**

`PreviewScreen` 体内调用 `PreviewTopBar(...)` 更新为精简后的参数列表：

```kotlin
PreviewTopBar(
    source = source,
    zoomPercent = state.zoomPercent,
    onNavigateBack = onNavigateBack,
    onZoomChange = onZoomChange,
    onResetZoom = onResetZoom,
    onCopy = onCopy,
    onShare = onShare,
    onExport = onExport,
)
```

- [ ] **Step 3: 无障碍逐控件检查**

确认每个控件标签：

| 控件 | contentDescription | 是否有 Text 子元素 | 风险 |
|------|-------------------|-------------------|------|
| 返回 IconButton | `"返回"` | 仅 Icon(null) | ✅ |
| ⋮ IconButton | `"更多选项"` | 仅 Icon(null) | ✅ |
| DropdownMenuItem 复制 | 无 contentDescription | `Text("复制")` | ✅ |
| DropdownMenuItem 分享 | 无 contentDescription | `Text("分享")` | ✅ |
| DropdownMenuItem 导出 | 无 contentDescription | `Text("导出")` | ✅ |
| Slider | `"缩放比例"` | 无 Text | ✅ |
| 百分比 Text | `"缩放"` | `Text("120%")` | ⚠️ 可能覆盖 |

百分比 Text 的问题：`contentDescription = "缩放"` 会覆盖 `Text("120%")` 的播报，但加上 `stateDescription` 后播报 "缩放 120%"。可考虑改为直接在 Text 上不设 contentDescription，让文本自然播报 "120%"，同时在语义上标记为可点击。

如果决定保持现状不调整百分比标签，此步通过。

- [ ] **Step 4: 最终编译验证**

```powershell
./gradlew :app:compileDebugKotlin 2>&1 | Select-Object -Last 20
```

预期：BUILD SUCCESSFUL

---

### Task 5: 功能验证

- [ ] **Step 1: 验证 ⋮ 菜单**

确认点击 ⋮ 弹出菜单，复制/分享/导出正常触发，菜单点击后关闭。

- [ ] **Step 2: 验证滑块**

确认拖动滑块 WebView 缩放同步变化，步进 5%。

- [ ] **Step 3: 验证双击百分比重置**

确认点击百分比标签 resetZoom 触发。

- [ ] **Step 4: 验证下拉刷新**

确认下拉 WebView 触发刷新，完成后播报"已刷新"。

- [ ] **Step 5: 提交**

```powershell
git add app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt
git add app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt
git commit -m "refactor: 精简预览顶栏 — 更多菜单 + 缩放滑块 + 下拉刷新"
```

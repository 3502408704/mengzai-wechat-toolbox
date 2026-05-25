# 预览页顶栏精简设计

**日期**: 2026-05-24
**状态**: 待实施

## 目标

将预览页顶栏从 9 个控件精简到 3 个，提升预览区域的沉浸感，让用户注意力集中在排版效果上。

## 当前问题

预览顶栏两行共 9 个可交互控件：

| 位置 | 控件 |
|------|------|
| 第一行 | 返回、标题/副标题、刷新、分享、导出 |
| 第二行 | 缩小、百分比、放大、重置、复制 |

控件过多导致：
- 视觉杂乱，挤压预览空间
- 高频操作（复制）和低频操作（导出）混在一起
- 缩放控制占用一整行，交互效率低

## 改造后布局

`
第一行: [←返回] [预览 / 来自当前草稿]              [⋮]
第二行: [━━━━━●━━━━━ 120%]
`

### 控件变更

| 操作 | 原位置 | 新位置 | 说明 |
|------|--------|--------|------|
| 返回 | 第一行左 | 第一行左 | 不变 |
| 标题/副标题 | 第一行中 | 第一行中 | 不变 |
| 刷新 | 第一行按钮 | **移除，改用下拉刷新** | 手势更自然 |
| 分享 | 第一行按钮 | ⋮ 菜单 | |
| 导出 | 第一行按钮 | ⋮ 菜单 | |
| 缩小/放大/百分比/重置 | 第二行全部 | **一根 Slider** | 一步到位 |
| 复制 | 第二行按钮 | ⋮ 菜单 | |

### 更多菜单（⋮）

点击右上角 ⋮ IconButton，弹出 DropdownMenu：

`
┌───────────┐
│  📋 复制   │
│  📤 分享   │
│  📥 导出   │
└───────────┘
`

- 每个菜单项为 DropdownMenuItem，包含 Icon + Text
- 顺序：复制（最高频）→ 分享 → 导出

### 缩放滑块

- 控件：Material3 Slider，水平方向
- 范围：85f .. 150f
- 步进：5（吸附到最近的 5 的倍数）
- 滑块右侧显示当前百分比文本 120%
- 重置：双击百分比文本 → 回到 100%（带 nimateFloatAsState 弹性动画）
- 视觉效果：track 颜色随缩放值渐变（放大端暖色、缩小端冷色）

### 下拉刷新

- 使用 Compose Material3 pullToRefresh 包裹 WebView
- 下拉触发 → 调用 iewModel.refresh()
- 刷新完成后 announcer 播报 "已刷新"

## 无障碍要求

遵循 accessibility-baseline 技能中的规则：

- ⋮ IconButton：contentDescription = "更多选项"
- Slider：contentDescription = "缩放比例"，stateDescription 动态更新为当前百分比
- 菜单项：各自有 Text 标签，**不添加冗余 contentDescription**（避免双重播报）
- 百分比文本：contentDescription = "缩放"，stateDescription = "120%"
- 双击重置通过 semantics { onClick { ... } } 暴露给无障碍服务
- 下拉刷新完成：通过 ememberAccessibilityAnnouncer 播报 "已刷新"
- 菜单打开/关闭：不自动播报（DropdownMenu 的展开已是明确的用户手势）

## 涉及文件

| 文件 | 变更 |
|------|------|
| PreviewScreen.kt | 重写 PreviewTopBar，新增 Slider、DropdownMenu、pullToRefresh |
| PreviewViewModel.kt | updateZoomPercent 签名可能需调整以接收 Float |

## 不变内容

- PreviewRoute 的参数传递
- PreviewViewModel 的核心逻辑（refresh、notifyCopied、notifyShared、exportHtml 等）
- 空状态展示（EmptyPreviewState）
- Snackbar 机制
- 剪贴板 / 分享工具函数

## 验收标准

1. 预览顶栏仅显示返回按钮、标题、⋮ 按钮、缩放滑块
2. ⋮ 菜单包含复制、分享、导出三项，点击正常触发原操作
3. 滑块拖动缩放步进 5%，松手后在 WebView 生效
4. 双击百分比标签重置到 100%
5. 下拉 WebView 触发刷新，完成后播报"已刷新"
6. TalkBack 下无双重播报，所有控件有正确的语义标签

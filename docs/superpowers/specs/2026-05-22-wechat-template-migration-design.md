# 微信公众号富文本复制与模板体系迁移设计

- 日期：2026-05-22
- 目标项目：排版助手 Android 客户端
- 参考实现：`mspringjade/wechat-formatter`
- 设计目标：在保留现有 Android 原生界面、导航与本地数据结构的前提下，迁移参考项目中最关键的两项能力：面向微信公众号编辑器的内联富文本输出，以及可切换的文章模板体系。

## 1. 范围

本设计只覆盖以下内容：

- 把当前过于简化的 Markdown 渲染链路升级为可用于公众号发布的文章渲染链路
- 新增模板体系，并接入当前编辑页与预览页
- 让复制到系统剪贴板的 HTML 成为“可粘贴到微信公众号编辑器并尽量保留样式”的最终内联 HTML
- 让预览页使用与复制结果同源的渲染结果，减少“预览好看但复制失真”的偏差

本设计不包含：

- 嵌入参考仓库的 Web 工作台
- 替换现有 Compose 原生 UI 为 WebView 主界面
- 接入参考仓库中的 AI 排版能力
- 重做历史、设置、导入导出、底部导航的信息架构

## 2. 背景问题

当前项目已经具备“编辑 -> 预览 -> 复制”的基础主链路，但用户反馈表明它离“公众号发布工具”还有明显差距，集中体现在以下问题：

- CSS 支持极少，很多输入样式在清洗或渲染阶段丢失
- Markdown 解析能力过于简化，标题、引用、代码块、表格、图片组合等复杂结构无法稳定生成预期 HTML
- 预览页使用的文档样式壳较薄，和公众号编辑器实际表现差距较大
- 复制功能虽然使用了 HTML 剪贴板接口，但复制内容本身不是公众号友好的内联 HTML，导致粘贴时样式损失严重

参考仓库之所以能完成“复制后仍保留样式”的核心原因，不在于浏览器复制 API，而在于它在复制前就已经把各个区块的视觉样式转换成了 DOM 元素上的 `style=""` 内联属性，并针对公众号编辑器的兼容性使用了更稳妥的块级结构。

## 3. 迁移原则

### 3.1 保留现有 Android 原生体验

- 继续使用当前 Compose 编辑页、预览页、历史页、设置页结构
- 不将应用改造成“Web 工具壳”
- 模板选择能力作为现有界面的增强，而不是另起一套 UI

### 3.2 复制结果优先于浏览器式渲染

渲染引擎不再以“普通网页展示”作为第一目标，而以“微信公众号编辑器粘贴兼容性”作为第一目标。预览页只是一种对最终发布 HTML 的展示容器，因此预览结果必须尽量与复制结果同源。

### 3.3 模板配置驱动

模板体系应当像参考仓库一样由结构化配置驱动，而不是将样式逻辑硬编码在多个 Compose 组件或字符串拼接分支中。这样后续继续补模板、调主题色、扩展分类才有可维护性。

### 3.4 安全清洗继续保留，但不能破坏排版

当前安全清洗策略过于保守，已经伤害了正常排版。新方案必须继续过滤脚本与危险属性，但要允许安全的内联样式、常见布局属性以及公众号所需的兼容标记。

## 4. 目标能力

### 4.1 内联富文本复制

复制到剪贴板时输出：

- HTML 富文本片段：用于粘贴到微信公众号编辑器
- 纯文本兜底：用于目标应用不识别 HTML 时仍保住内容

复制的 HTML 片段需要满足：

- 样式主要以 `style=""` 形式直接挂在元素上
- 表格单元格等必要场景补充传统属性，如 `bgcolor`
- 标题、引用、列表、代码块、图片、链接、分隔线、表格均能单独套模板样式
- 避免过度依赖 `<style>` 标签、复杂选择器、Flex 和公众号兼容性较差的布局方式

### 4.2 模板体系

应用需要引入一套可选择的模板系统，支持：

- 模板分类
- 模板列表与当前模板选择
- 默认模板
- 每个模板拥有自己的主题色、容器样式、标题样式、引用样式、列表样式、代码块样式、表格样式等

首版模板交付策略：

- 先迁移模板基础架构与模板选择能力
- 模板数据结构按参考仓库可扩展形式设计
- 首批固定迁移 12 套模板，至少覆盖极简风、商务风、文艺风、科技风四个分类，每个分类至少 3 套，保证功能闭环后再扩展到更多模板

### 4.3 同源预览

预览页要展示的是“最终发布 HTML”的完整文档包装版本。也就是说：

- 复制使用文章 HTML 片段
- 预览使用包裹该片段后的完整 HTML 文档
- 导出 HTML 使用完整文档版本

这样可以避免当前“预览看到的是一种 HTML，复制出去的是另一种 HTML”的结构性偏差。

## 5. 架构设计

### 5.1 渲染管线重构

当前渲染链路：

- `MarkdownConverter`：少量正则转 HTML
- `PreviewDocumentBuilder`：拼接基础 HTML 壳
- `HtmlSanitizer`：安全清洗

新渲染链路重构为：

1. `ArticleTemplateRepository`
   - 加载模板定义
   - 提供模板查询、分组、默认模板

2. `MarkdownRenderEngine`
   - 负责把 Markdown 或混合内容转换为结构稳定的 HTML
   - 至少要覆盖标题、段落、强调、引用、列表、代码块、图片、链接、分隔线、表格、内嵌 HTML

3. `InlineArticleRenderer`
   - 负责把解析后的内容按模板映射为公众号友好的内联 HTML
   - 对每一种块级元素单独注入样式和兼容结构

4. `HtmlSanitizer`
   - 作为最后一步安全清洗
   - 允许白名单内标签、属性和安全样式

5. `PreviewDocumentBuilder`
   - 输出两份结果：
   - `publishHtml`：可复制的文章 HTML 片段
   - `htmlDocument`：用于 WebView 预览和导出的完整文档

### 5.2 数据模型

新增核心模型：

- `ArticleTemplate`
  - `id`
  - `name`
  - `description`
  - `category`
  - `themeColor`
  - `backgroundColor`
  - `containerStyle`
  - `h1Style`
  - `h2Style`
  - `h3Style`
  - `pStyle`
  - `blockquoteStyle`
  - `blockquoteInnerBefore`
  - `blockquoteInnerAfter`
  - `listStyle`
  - `listItemStyle`
  - `listIconHtml`
  - `strongStyle`
  - `emStyle`
  - `codeContainerStyle`
  - `codeHeaderStyle`
  - `codeBlockStyle`
  - `imgStyle`
  - `hrStyle`
  - `linkStyle`
  - `tableStyle`
  - `thStyle`
  - `tdStyle`
  - `delStyle`
  - 其它后续扩展字段

- `TemplateCategory`
  - `id`
  - `name`

- `RenderArticleResult`
  - `publishHtml`
  - `previewDocument`
  - `plainText`
  - `contentType`
  - `templateId`

### 5.3 模板存储位置

模板定义放在应用 `assets` 中，以结构化 JSON 存储。原因如下：

- 避免把大批量模板硬编码进 Kotlin 源码
- 方便后续持续补模板或调整已有模板
- 便于做模板批量生成与导入

仓库层负责把 JSON 反序列化为 Kotlin 模型。

## 6. 页面接入设计

### 6.1 编辑页

保留现有编辑器与工具栏，新增“模板与样式”卡片，放在编辑控制区附近，包含：

- 当前模板名称
- 当前模板分类
- 模板选择入口
- 主题色预览
- 恢复默认模板动作

设计原则：

- 编辑页仍然以“写内容”为主
- 模板选择是增强操作，不应抢占主编辑空间
- 模板切换后不直接修改原始内容，只影响预览和复制输出

### 6.2 预览页

保留当前预览页结构，但增强以下内容：

- WebView 加载由新渲染管线生成的完整预览文档
- 复制按钮明确服务于公众号发布
- 说明文案固定增加“复制结果已针对公众号编辑器做兼容优化”

### 6.3 历史与草稿

当前工作草稿需要记住当前模板 ID。原因如下：

- 同一篇文章恢复时应回到其原有模板
- 模板不应只是全局设置，否则切换文章会造成预览错乱

历史快照也建议附带模板 ID，以便恢复历史时保留当时样式选择。

## 7. 复制与导出设计

### 7.1 剪贴板复制

复制时：

- HTML 使用 `publishHtml`
- 纯文本使用 `plainText`

保留当前 `ClipData.newHtmlText()` 路线，但复制内容换成真正的内联文章片段，而非完整预览文档或简化渲染结果。

### 7.2 预览文档

`htmlDocument` 用于 WebView 和导出。文档骨架包含：

- `DOCTYPE`
- `html/head/body`
- `meta charset`
- `meta viewport`
- 最少量的阅读级兜底样式

但主体视觉不依赖文档级 CSS，而依赖内联 HTML 中各元素自带的 `style=""`。

### 7.3 HTML 导出

导出 HTML 时输出完整文档，不输出仅片段内容。这样导出文件可以直接用浏览器打开检查，同时尽量保留与预览一致的视觉表现。

## 8. 安全与兼容策略

### 8.1 白名单原则

清洗阶段保留：

- 文章常见结构标签
- 安全的内联样式属性
- 图片、链接、表格所需常见属性

继续过滤：

- `script`
- `iframe`
- `object`
- `embed`
- 表单类标签
- 任意 `on*` 事件属性
- `javascript:` 等危险协议

### 8.2 样式属性控制

允许的内联样式以排版相关属性为主，包括但不限于：

- `color`
- `background-color`
- `font-size`
- `font-weight`
- `font-style`
- `line-height`
- `letter-spacing`
- `text-align`
- `text-decoration`
- `margin`
- `padding`
- `border`
- `border-radius`
- `display`
- `width`
- `max-width`
- `box-sizing`
- `word-break`
- `word-wrap`

避免放开：

- 行为性样式
- 可触发脚本或浏览器特殊能力的样式
- 与发布场景明显无关的高风险属性

### 8.3 公众号兼容优先

需要显式采用公众号更稳的结构策略：

- 列表优先使用嵌套 `section` 和图标块，而不是完全依赖浏览器原生 `ul/li`
- 多图并排使用 `inline-block`，避免依赖 Flex
- 表格单元格补 `bgcolor`
- 标题、引用等块级结构用更稳的嵌套容器包裹

## 9. 代码改动边界

重点变更文件：

- `app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt`
- `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
- `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- `app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt`
- `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/editor/EditorViewModel.kt`
- `app/src/main/java/com/paiban/helper/data/db/DraftEntity.kt`
- `app/src/main/java/com/paiban/helper/data/db/HistoryEntity.kt`
- `app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt`

新增模块：

- `domain/template/`
- `domain/render/template/`
- `assets/templates/`

## 10. 测试策略

### 10.1 单元测试

需要重点覆盖：

- 模板加载
- 模板选择与默认模板逻辑
- Markdown 到内联 HTML 的核心渲染结果
- 标题、引用、列表、代码块、表格、图片、多图场景
- 安全清洗不会吞掉允许样式
- 复制结果使用 `publishHtml` 而不是 `htmlDocument`

### 10.2 集成测试

至少覆盖以下主链路：

- 编辑内容 -> 选择模板 -> 生成预览
- 切换模板后预览内容变化
- 复制后输出 HTML 与纯文本同时存在
- 恢复历史后模板 ID 随内容恢复

### 10.3 手工验收

重点手工验证：

- 预览样式与模板选择一致
- 标题、引用、列表、代码块、表格在预览中完整展示
- 复制到微信公众号编辑器后样式尽量保留
- 多图场景不溢出
- 表格在窄屏下不出现明显错位

## 11. 分阶段交付建议

### Phase A：打通基础结构

- 引入模板模型与仓库
- 为草稿与历史增加模板 ID
- 让编辑页和预览页都感知当前模板

### Phase B：替换渲染引擎

- 重写 Markdown 渲染与模板映射
- 输出 `publishHtml` 与 `htmlDocument`
- 调整预览与复制逻辑

### Phase C：补兼容与模板扩容

- 优化公众号兼容结构
- 增加更多模板
- 补测试与回归验证

## 12. 风险与取舍

### 12.1 风险

- Android 侧若找不到合适的成熟 Markdown 解析方案，首版实现复杂度会高于当前正则方案
- 公众号编辑器并非标准浏览器，不同标签组合仍可能存在个别兼容差异
- 模板数量较大时，完全手工迁移的工作量不小

### 12.2 取舍

- 首版优先保证“内联复制可用”和“模板体系闭环”，不追求一次性补齐参考仓库全部细节
- 首批模板数量可以先少后多，但模板架构必须一次搭好
- 保留原生 UI，避免因追求代码复用而牺牲 Android 体验

## 13. 结论

本次迁移的本质不是“把一个 Web 项目搬进 Android”，而是“把参考项目中最有价值的发布能力移植到现有 Android 架构里”。具体来说：

- 保留现有 Compose 原生界面与工作流
- 把模板体系引入当前编辑与预览链路
- 用面向微信公众号兼容性的内联 HTML 替换当前简化渲染结果
- 让预览、复制与导出三者建立同源关系

这样可以在不推翻现有项目结构的情况下，补上用户最在意的“模板开箱即用”和“复制后样式不丢”两项核心能力。

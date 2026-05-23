# Global Edge-To-Edge And Accessibility Refresh Design

## 1. Context

`梦崽公众号工具箱` is already implemented as a native Android app with Jetpack Compose, Material 3, Hilt, Room, and a screen set that covers onboarding, editing, template selection, preview, history, AI placeholder, and settings.

The current UI works functionally, but it has three structural problems:

- edge-to-edge behavior is not configured globally, so spacing around the status bar, navigation bar, and cutout area is inconsistent
- page scaffolding consumes padding in multiple layers, which creates layouts that can feel either too close to or too far from the system bars
- accessibility semantics and action hierarchy are incomplete, especially for icon actions, focus order, preview interaction, and post-action announcements

The user also needs the preview experience to be more immersive and easier to inspect visually, including a full-screen preview layout and an accessible zoom model that does not rely only on gestures.

## 2. Goal

Refactor the app so that every screen follows one shared layout and accessibility rule set, while giving the onboarding, editor, and preview screens a more intentional and user-approved experience.

Success means:

- all pages use one consistent edge-to-edge and inset strategy
- all pages keep primary content within a consistent 12dp to 16dp horizontal rhythm
- the onboarding screen no longer sits too close to the status bar
- the preview screen becomes full-screen and content-first
- icon labels are concise and unambiguous
- zoom is available through an accessible slider
- low-frequency preview actions move into the overflow menu
- TalkBack users get predictable focus order, safe cutout avoidance, and explicit spoken feedback after important actions

## 3. Non-Goals

This refresh does not:

- change the app's core information architecture
- replace Compose with XML layouts
- redesign the rendering engine or HTML export pipeline
- add a new AI feature beyond improving the current placeholder page
- introduce a separate preview editing mode

## 4. Reference Standards

The implementation should be guided by these official references:

- Android edge-to-edge and window inset guidance
- Android cutout handling guidance
- Material Design 3 component and layout guidance
- Android accessibility principles and Compose accessibility guidance
- WCAG 2.2 target-size guidance as a mobile accessibility baseline

Reference links used for this design:

- https://developer.android.com/develop/ui/views/layout/edge-to-edge
- https://developer.android.com/develop/ui/compose/system/setup-e2e
- https://developer.android.com/develop/ui/compose/system/cutouts
- https://developer.android.com/develop/ui/compose/system/material-insets
- https://developer.android.com/guide/topics/ui/accessibility/principles
- https://developer.android.com/guide/topics/ui/accessibility/composables
- https://developer.android.com/develop/ui/compose/accessibility/traversal
- https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html
- https://m3.material.io/components/floating-action-button/overview

## 5. Core Design Decisions

### 5.1 Compose Is The Primary UI Path

The app already uses Compose across its navigation graph, so this refresh should stay Compose-native. Kotlin View code is still required for the activity-level edge-to-edge setup and for an explicit `WindowInsets` example, but screen refactoring should remain in Compose.

### 5.2 Global Rules Must Be Enforced In Shared Scaffolding

This work should not be done as isolated page patches. The main source of inconsistent spacing is the interaction between:

- `MainActivity`
- `AppScaffold`
- `AppPage`
- page-level use of `Scaffold`, `padding`, and navigation bar handling

The shared shell must own the global safe-area strategy so individual pages are not forced to guess when insets have already been consumed.

### 5.3 Emphasis Should Be Selective

Only the onboarding, editor, and preview screens should receive more visible structural redesign. History, settings, templates, and AI assistant should adopt the same spacing and accessibility rules, but keep a restrained visual update.

### 5.4 Concise Labels Win Over Verbose Labels

The user explicitly prefers concise action labels. Icon and control semantics should use short functional names where they remain unambiguous.

Examples:

- `撤销`
- `恢复`
- `返回`
- `刷新`
- `更多操作`
- `导出`
- `分享`

Longer descriptions such as `撤销上一步编辑` should not be used unless a concise term becomes ambiguous in context.

## 6. Global Layout And Insets Strategy

### 6.1 Activity Configuration

`MainActivity` must enable edge-to-edge by calling:

- `WindowCompat.setDecorFitsSystemWindows(window, false)`

`AndroidManifest.xml` must set:

- `android:windowLayoutInDisplayCutoutMode="shortEdges"` on `MainActivity`

This makes the app draw edge-to-edge while allowing content backgrounds to extend behind system bars and around cutouts.

### 6.2 Shared Safe-Area Strategy

The app should use one rule consistently:

- backgrounds may extend edge-to-edge
- interactive controls and readable text must stay inside safe content bounds

Implementation direction:

- `AppScaffold` owns app-level structural insets, especially where bottom navigation is present
- `AppPage` owns page-level top bar and content spacing behavior
- pages consume content padding from shared scaffolding instead of stacking custom fixed padding against system bars

### 6.3 Margin And Spacing Rules

Global layout rhythm:

- standard outer horizontal margin: `16dp`
- allowed tighter edge case: not less than `12dp`
- common vertical gaps: `12dp` and `16dp`

This rule exists to prevent both failure modes seen in the current UI:

- controls visually drift too far from the screen edge because multiple safe-area paddings stack together
- controls sit too close to the status bar or cutout because only static top padding is used

### 6.4 Cutout And Focus Safety

Controls near the top edge must use system-bar-aware padding so TalkBack focus rectangles are fully visible and never hidden by a notch or cutout.

The design standard is:

- top-edge controls shift below the real status-bar or cutout inset
- no top margin should be hardcoded as a fake status-bar height

## 7. Shared Accessibility Model

### 7.1 Labels

Rules:

- icon-only actions must have a semantic label
- labels should be concise and functional
- labels should not include redundant nouns such as `按钮` or `图片`

### 7.2 Focus Order

Focus order must mirror user workflow rather than visual coincidence.

For editing-oriented flows, the required sequence is:

- header
- main editing or reading region
- supporting controls
- preview or export actions

Compose traversal groups and traversal indexes should be used where natural tree order is not enough.

### 7.3 Touch Targets

All tappable controls should meet or exceed `48dp x 48dp`.

This applies to:

- icon buttons
- chips
- slider thumb and surrounding interaction area
- menu entries
- bottom actions

### 7.4 Spoken Feedback

Important actions must trigger a spoken accessibility announcement in addition to visible UI feedback.

Examples:

- copy success
- draft saved
- import success
- zoom value changed

The implementation may use a shared helper that wraps `View.announceForAccessibility(...)` through the current Compose root view.

## 8. Screen-Level Design

## 8.1 Onboarding Screen

### Purpose

Welcome the user and explain the main flow without crowding the status bar.

### Structure

- top introduction block
- short capability cards
- bottom primary action

### Inset Rule

The top content block must begin after:

- status bar bottom inset plus `24dp`

This specifically fixes the current issue where onboarding content begins too close to the system bar.

### Visual Direction

The onboarding page should feel clean and calm, with clear title hierarchy and a single obvious way forward.

## 8.2 Editor Screen

### Purpose

Make the editor feel like the main workspace instead of a stack of configuration cards.

### Focus Order

The editor page should read in this order:

- title and page header
- editor text field
- formatting toolbar
- template section
- editor preferences
- primary preview action

### Layout Direction

The text field becomes the visual core of the screen. Helper controls remain nearby, but they should not displace the editor from the center of the workflow.

### Semantics

Concise labels should be used for icon actions:

- `撤销`
- `恢复`
- `更多操作`

The editable text node must remain the true accessibility owner of the writing area. Decorative containers should not override the editing semantics.

### Feedback

Actions such as importing clipboard content, importing a file, clearing the draft, or saving state should surface both visual feedback and spoken feedback.

## 8.3 Template Selection Screen

### Purpose

Stay consistent with the new page shell and bottom safe-area rules without becoming a major redesign target.

### Layout Direction

- keep grouped template categories
- unify top and bottom spacing with the shared scaffold rules
- bottom confirm action follows the same bottom inset strategy as the rest of the app

## 8.4 Preview Screen

### Purpose

The preview must act as a full-screen reading and inspection space.

### Primary Structural Decision

The preview is not a normal card-based page. It should become a content-first full-screen experience with a lightweight control chrome.

### Layout

- immersive full-screen page
- compact top overlay bar anchored to the safe top inset
- full-width preview region
- internal reading padding that simulates WeChat article spacing
- floating capsule primary action near the bottom safe inset

### Reading Surface

The preview content area should:

- expand to the full available width
- avoid redundant outer padding wrappers
- apply internal horizontal padding of `15dp`

This internal padding exists to mimic the real reading environment rather than adding generic card spacing.

### Primary Action

`复制富文本` remains the main preview action and should appear as a floating capsule-style action near the bottom of the screen.

Its vertical position must be based on:

- navigation bar inset plus `24dp`

### Top Bar Actions

Only high-frequency actions should stay visible in the preview chrome.

Visible actions:

- `返回`
- zoom slider block
- `更多操作`

Low-frequency actions move into the overflow menu:

- `分享`
- `导出`
- `恢复原始比例`

If `刷新` remains necessary for the current architecture, it should be evaluated as either visible or overflow based on final space constraints, but it should not displace the zoom control.

### Zoom Accessibility

Zoom must not rely only on pinch gestures.

Approved interaction model:

- a visible slider in the top control area
- discrete zoom steps rather than free continuous movement
- a visible text value such as `100%`
- spoken feedback after changes, such as `已缩放到 125%`

Recommended discrete values:

- `85%`
- `100%`
- `115%`
- `130%`
- `150%`
- `175%`

The slider is the primary accessible zoom control. A `恢复原始比例` action should be available in the overflow menu.

### Preview Accessibility Semantics

The preview region should expose a semantic role description that identifies it as:

- `公众号排版效果预览`

It should also communicate that it is:

- read-only
- scrollable
- zoomable

### Focus Order

Preview focus should move in this order:

- top control bar
- preview reading region
- floating copy action

## 8.5 History Screen

### Purpose

Adopt the new layout rhythm and preserve the current interaction model without over-redesigning the page.

### Rules

- list rows remain the primary interaction unit
- custom accessibility actions remain available for `预览` `编辑` `删除`
- spacing and card rhythm align with the shared shell

## 8.6 Settings Screen

### Purpose

Behave like a calm preference page, but inherit the same edge-to-edge and touch-target rules.

### Rules

- rows remain merged semantic objects
- switches and radio options do not create duplicate focus targets
- section spacing aligns with the global rhythm

## 8.7 AI Assistant Screen

### Purpose

Remain a simple placeholder page while inheriting the same page shell, spacing, and semantics conventions.

## 9. Kotlin WindowInsets Example Requirement

In addition to Compose-based inset handling, the implementation must include a Kotlin example that demonstrates dynamic top and bottom safe-area handling with `WindowInsets`.

That example should show:

- `WindowCompat.setDecorFitsSystemWindows(window, false)`
- `ViewCompat.setOnApplyWindowInsetsListener(...)`
- reading `statusBars()` and `navigationBars()`
- applying dynamic padding or margin updates instead of hardcoded heights

This example should be included in a real app file or utility so it can serve as a reference for future hybrid View-based work.

## 10. Files Expected To Change

At minimum, the implementation will likely modify:

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/paiban/helper/MainActivity.kt`
- `app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt`
- `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`
- `app/src/main/java/com/paiban/helper/ui/common/AppPage.kt`
- `app/src/main/java/com/paiban/helper/ui/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`
- `app/src/main/java/com/paiban/helper/ui/editor/TemplateSelectionScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`

A shared utility file may also be added for accessibility announcements or inset helpers.

## 11. Verification Strategy

Before considering implementation complete, verification should cover:

- compile success
- manual page review for onboarding, editor, preview, history, settings, templates, and AI placeholder
- confirmation that top content no longer sits too close to the status bar
- confirmation that preview is full-screen and the floating primary action does not obscure content
- confirmation that all icon-only actions have concise labels
- confirmation that touch targets meet the `48dp` rule
- confirmation that key actions announce success
- confirmation that the preview slider announces zoom changes and exposes the current value accessibly

## 12. Scope Summary

This redesign is a global structural refresh, not a single-screen patch. The implementation should first unify the app shell and inset strategy, then apply the approved screen-level changes with special attention to the onboarding, editor, and full-screen preview experience.

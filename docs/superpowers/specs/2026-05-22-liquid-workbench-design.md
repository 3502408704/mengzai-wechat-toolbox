# Liquid Workbench Design For 排版助手

## 1. Context

`排版助手` already has the core native Android building blocks in place:

- editor input for HTML / Markdown / mixed content
- template selection flow
- history browsing and restore flow
- settings screen for theme and developer options

The current UI is functional but still reads as a conventional Material card stack. It does not yet express the product as a high-focus mobile creation tool, and it does not satisfy the requested visual direction:

- Android 17 style oversized shape language
- iOS-like liquid black restraint and frosted surfaces
- stronger typographic contrast and layout scale
- explicit accessibility through touch target size, contrast, and semantic ownership

This redesign keeps the existing Compose-native architecture and feature scope, but redefines the app as a three-state mobile workbench with a unified visual system called `Liquid Workbench`.

## 2. Goal

Redesign the mobile UI for `排版助手` so it:

- merges Material You structure with iOS-like liquid black polish
- feels minimal and restrained while still visually bold
- keeps editing as the primary workflow
- promotes history as the dominant management surface
- preserves template selection and settings without visual clutter
- meets strong accessibility expectations for contrast, semantics, and touch size

## 3. Non-Goals

This redesign does not:

- replace the native Android app with a web UI
- change the underlying render pipeline for HTML / Markdown / preview output
- add new product features beyond editor, templates, history, and settings
- introduce onboarding flows, coach marks, or tutorial overlays
- require a full information architecture rewrite outside the approved shell changes

## 4. Product Model

The app should no longer feel like four unrelated pages. It should feel like one mobile workbench with three explicit operating states:

- `创作`
- `管理`
- `设置`

These states map to user intent rather than raw feature count:

- `创作` is for writing and preparing output
- `管理` is for revisiting, restoring, and reusing previous work
- `设置` is for system-level preferences

Templates remain an important capability, but they should become a supporting surface inside creation and management rather than a top-level destination competing with the main flow.

## 5. Information Architecture

## 5.1 Top-Level Shell

The app shell should be rebuilt around a fixed chrome structure:

- frosted top bar containing the app title area and a segmented state switcher
- central content region that swaps by state
- frosted bottom dock containing one full-width pill primary action

The top segmented switcher becomes the primary navigation and contains:

- `创作`
- `管理`
- `设置`

This replaces a more conventional bottom-tab mental model with a more editorial, tool-like workbench model.

## 5.2 State Responsibilities

### 创作

Default landing state.

Primary responsibility:

- input content
- refine formatting
- choose template when needed
- move toward preview

### 管理

Secondary state with strong visual identity.

Primary responsibility:

- browse saved history
- reopen or preview previous work
- switch template context when needed

History is the first visual focus. Templates are secondary support.

### 设置

Quiet utility state.

Primary responsibility:

- theme mode
- dynamic color behavior if retained
- developer and product information settings

## 6. Core Design Principles

## 6.1 One Workbench, Three Moods

The user should always feel they are inside the same product surface, not jumping between disconnected screens. The shell remains stable while content changes personality:

- `创作` feels focused and immersive
- `管理` feels archival and panoramic
- `设置` feels calm and system-like

## 6.2 Big Shapes, Low Noise

The interface should avoid small corners, hard edges, and dense control clusters. Visual confidence comes from:

- oversized radii
- broad cards
- large text hierarchy
- clear empty space

not from decorative ornament.

## 6.3 Contrast Is Aesthetic And Accessible

The requested high-contrast look is not just a branding choice. It must also guarantee strong readability in both light and dark themes, especially for:

- metadata text
- switch and radio states
- icon-only actions
- selected segmented control state

## 6.4 Frosted Chrome, Solid Content

Blur should be used intentionally, not everywhere. The frosted glass effect belongs to app chrome:

- top state switcher bar
- bottom action dock

Primary content cards remain solid surfaces so the UI stays legible and grounded.

## 7. Layout Specification

## 7.1 Global Spacing

Primary content should use an outward, almost edge-to-edge mobile layout:

- horizontal safe margin: `16dp` to `20dp`
- section spacing: `16dp` to `24dp`
- card internal padding: `16dp` to `20dp`

Cards should visually stretch across the screen rather than sit as small centered panels.

## 7.2 Shape System

Use a small, explicit shape scale:

- `32dp` radius for major cards
- `28dp` radius for secondary panels and chrome containers
- `999dp` radius for pill buttons, segmented controls, chips, and compact actions

No standard small-radius Material card treatment should remain in the primary UI.

## 7.3 Top Chrome

The top bar is a frosted, semi-transparent container pinned to the safe area. It should:

- expose the current state clearly
- keep the segmented control large enough for confident tapping
- allow underlying content to subtly show through during scroll

The top area may also include a state-specific subtitle, but it should remain concise.

## 7.4 Bottom Chrome

The bottom dock is a frosted base pinned above the navigation area. It holds one dominant pill CTA:

- full width within safe margins
- height: `56dp` to `60dp`
- no secondary buttons placed beside it

The label may change by state, but in `创作` the primary label remains `预览成品`.

## 8. State-Level Screen Design

## 8.1 创作 State

### Purpose

This is the primary working surface for drafting and formatting content.

### Layout

The `创作` state should be vertically structured as:

1. lightweight contextual intro
2. dominant editor card
3. horizontal markdown tool strip
4. compact template summary card
5. compact editing preferences card

### Editor Card

The editor card is the hero surface and should take the most vertical mass on screen. Inside it:

- title row for current draft context
- optional type hint such as HTML / Markdown / mixed
- large editable field region
- line numbers only when enabled, never visually overpowering content

The field surface itself should feel inset and protected, using a softer inner surface rather than a bordered text box.

### Markdown Toolbar

The markdown actions should become a horizontally scrollable set of thick pill controls rather than a cramped tool row. Each item must preserve:

- minimum hit area `48x48dp`
- visible label or understandable icon semantics
- clear selected or pressed feedback

### Template Summary

Template selection remains available but visually restrained. The summary card should show only:

- current template name
- category
- one clear action to switch template

It should feel like context, not a competing task.

### Editing Preferences

Line numbers, font scale, and similar preferences stay available but sit lower in the scroll. They should not interrupt the editor-first reading order.

## 8.2 管理 State

### Purpose

This state is the operational archive of the app. It is where the user returns to revisit work, reopen drafts, and scan previous output.

### Layout

The `管理` state should be vertically structured as:

1. short state description
2. dominant history section
3. secondary template strip or panel

### History As The Main Focus

History must be the first and strongest visual block. The first screenful should reveal multiple large cards immediately, with enough breathing room to make scanning feel deliberate rather than cramped.

Each history card should present:

- title
- time
- format
- optional short summary
- optional status or favorite badge

Inline action clutter should stay minimal. The card itself remains the main touch target, with long press or overflow handling secondary actions.

### Template As Support

The template panel should sit below or adjacent to history in a clearly secondary position. It can be horizontal and visually lighter, acting as a quick way to re-enter a style direction without stealing focus from history.

### Management Tone

Compared with `创作`, this state should feel more panoramic and gallery-like:

- larger vertical spacing
- more obvious card rhythm
- stronger hierarchy between title and metadata

## 8.3 设置 State

### Purpose

This state is intentionally quieter and should feel closest to a system preferences surface.

### Layout

Each setting group is a large rounded solid card with its section title outside the card. The screen should favor:

- clear grouping
- short descriptions
- full-row interaction

The settings state should include at minimum:

- appearance
- editor preferences
- developer options
- about

### Controls

Material interaction logic remains:

- radio controls for theme mode
- switches for binary preferences

But their visual treatment should be more explicit:

- thicker switch track and thumb feel
- stronger selected color contrast
- clearer off-state visibility in dark mode

## 9. Visual System

## 9.1 Dark Theme: Liquid Black

Dark mode must anchor the product identity.

Required palette direction:

- app background: `#000000`
- primary card surface: `#1C1C1E`
- text: near-white with strong contrast
- primary accent: high-saturation electric blue or similar neon emphasis
- destructive accent: bright orange-red when needed

Dark mode should distinguish layers with subtle surface steps, not borders.

## 9.2 Light Theme

Light mode is intentionally quieter.

Required palette direction:

- app background: `#F2F2F7`
- card surface: `#FFFFFF`
- primary text: deep black
- supporting text: cool dark gray

Visual drama comes from scale and spacing more than color.

## 9.3 Accent Policy

Use one primary emphasis color at a time for the UI system. It should drive:

- segmented control selection
- primary CTA
- active switch or radio state
- focused highlights where needed

Avoid introducing many colorful accent families across the same screen.

## 9.4 Elevation And Separation

In light mode:

- use soft, restrained shadow to separate cards from background

In dark mode:

- prefer tonal separation between surfaces
- avoid glow-heavy or border-heavy treatments

## 9.5 Typography

Typography should create the requested “big and bold” rhythm while remaining readable:

- page titles: `32sp`
- card titles: `22sp`
- body text: `16sp`
- metadata text: smaller but still high contrast

The editor input region may retain a monospaced typeface, but surrounding interface typography should use a more expressive and modern sans-serif hierarchy than plain defaults.

## 10. Motion And State Change

Motion should reinforce liquidity without becoming ornamental.

## 10.1 State Switching

When switching between `创作`, `管理`, and `设置`, the content region should animate with:

- horizontal movement
- slight opacity change
- slight scale stabilization

Recommended duration:

- `220ms`

The top and bottom frosted chrome should remain stable and not visually jump.

## 10.2 Card Entrance

Large cards may use subtle staggered rise or fade on first appearance, especially in `管理`, but this should remain restrained and should not slow down repeated navigation.

## 10.3 CTA Behavior

The bottom CTA may use a soft active-state pulse or press compression, but it should not continuously animate in a distracting way.

## 11. Accessibility Specification

## 11.1 Touch Targets

Every interactive control must provide at least `48x48dp` of tappable area, including:

- segmented state options
- icon buttons
- markdown tool actions
- template entry points
- history row actions
- settings rows

Visual size may exceed this minimum, but never fall below it.

## 11.2 Contrast

All text and control states must preserve strong contrast in both themes, especially:

- metadata on history cards
- disabled states
- off-state switches and radios
- icon-only controls in frosted bars

“Elegant low contrast” must not override usability.

## 11.3 Semantic Ownership

Each settings row should be one logical accessibility target.

Each history card should be one logical primary target with exposed custom actions for:

- `预览`
- `编辑`
- `删除`

The top segmented control must announce both label and selected state.

## 11.4 Editor Semantics

The editable field remains the true editing node. Decorative containers must not replace or obscure the input semantics. Helper context such as current template or import actions should remain separately discoverable.

## 11.5 Dynamic Labels

The bottom CTA label must remain semantically aligned with the current state. If the visible label changes, the accessibility announcement must change with it.

## 12. Current Codebase Mapping

This redesign should be implemented by reshaping the existing Compose surfaces rather than inventing a parallel UI stack.

Current relevant screens already exist:

- `EditorScreen`
- `HistoryScreen`
- `SettingsScreen`

This means implementation can proceed by:

- introducing a new top-level shell and state switcher
- reworking the existing screen compositions into state sections
- redefining theme tokens and shared card/button/chrome primitives

Templates should be folded into the new workbench hierarchy rather than preserved as an equally weighted standalone destination.

## 13. Implementation Boundaries

The redesign should cover:

- top-level workbench shell
- frosted top and bottom chrome
- oversized shape and spacing system
- revised light and dark theme tokens
- editor, history, and settings visual restructuring
- updated CTA hierarchy
- accessibility semantics and contrast validation

The redesign should not require:

- changes to content rendering business logic
- changes to template data format
- unrelated repository refactors
- new product modules

## 14. Testing Expectations

Implementation should be validated through:

- theme token regression checks
- layout model or presentation tests where screen state rules change
- accessibility semantics tests for settings, history, and the segmented state switcher
- manual dark and light mode contrast review
- manual touch target verification for all compact controls

## 15. Recommended Execution Order

1. Introduce the workbench shell and top-level state model
2. Define liquid theme tokens, shapes, and frosted chrome primitives
3. Refactor `创作` state around the new editor-first layout
4. Refactor `管理` state around history-first layout and secondary template support
5. Refactor `设置` state into grouped, high-contrast preference cards
6. Sweep semantics, touch targets, and contrast behavior

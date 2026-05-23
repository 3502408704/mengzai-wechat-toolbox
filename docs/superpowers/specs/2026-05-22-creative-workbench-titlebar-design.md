# Creative Workbench Titlebar Design

## 1. Context

The app already covers the main native Android flows for writing, template selection, preview, history, AI placeholder, and settings. It also already introduced some screen-level `TopAppBar` usage, especially on settings, editor, preview, and template selection.

However, the page identity is still inconsistent:

- `Settings` has a clear page header and feels structurally separated.
- `History` still presents its page identity inside the content area instead of through a page-level title bar.
- `Editor`, `Preview`, and `Template Selection` have top bars, but they do not yet form a unified page-separation system.
- Different screens still feel like adjacent feature panels instead of one coherent creative workspace.

The user explicitly clarified that the "title bar layering" goal is not about adding more navigation controls. The goal is to use page title bars to separate pages clearly, similar to settings, so that page identity itself becomes part of the accessibility experience.

The user also selected a `content creation` visual direction rather than a purely neutral productivity style.

## 2. Goal

Create a coherent visual and structural system for the whole app that:

- gives every page a clear and consistent title bar identity
- uses title bars as page separators across all pages, not only settings
- improves accessibility by making the current page easy to recognize visually and via screen readers
- strengthens the app's atmosphere as a focused writing and publishing workbench
- keeps the current native Android Compose architecture intact

## 3. User-Confirmed Constraints

The design must follow these confirmed decisions:

- visual direction: `content creation`
- title bars are not an extra navigation system
- title bars should separate pages in the same spirit as the current settings screen
- this pattern should cover all pages, including main pages and secondary pages
- the title bar pattern itself should improve accessibility by making page identity obvious

## 4. Non-Goals

This design does not:

- replace the current navigation architecture
- introduce a new global navigation paradigm
- redesign the application into a desktop multi-pane layout
- add onboarding, walkthrough overlays, or tutorials
- add heavy brand illustration systems
- introduce new business features unrelated to screen structure and visual hierarchy

## 5. Design Principles

### 5.1 Page Identity Comes Before Page Content

Each screen should first announce what page the user is in, and only then present the page's working content. This is true both visually and semantically.

### 5.2 Title Bars Are Structural, Not Decorative

The top title area is a page boundary. It should not be treated as cosmetic chrome. It defines entry context, return relationship, and current state.

### 5.3 Content-Creation Atmosphere Without Noise

The app should feel like a writing and publishing workbench:

- calmer than a playful branded app
- warmer and more intentional than a default settings-like app
- more editorial and canvas-oriented than a generic utility

### 5.4 Accessibility Through Predictable Hierarchy

Users, including TalkBack users, should encounter pages in a stable order:

1. page title
2. page status or context
3. primary content region
4. primary action

This order is more important than any decorative treatment.

## 6. Evaluated Approaches

### 6.1 Approach A: Unified Creative Workbench

Use a shared structure across all screens:

- title bar
- optional context strip
- main work/content region
- bottom primary action when appropriate

Benefits:

- strongest consistency
- easiest to understand across screen changes
- easiest to align with accessibility expectations
- supports visual upgrade without introducing navigation complexity

Trade-off:

- requires systematic refactor across all screens rather than isolated tweaks

### 6.2 Approach B: Hero-Header Page Separation

Use larger, more dramatic page headers with strong page-introduction visuals.

Benefits:

- most visually expressive
- strongest page identity at first glance

Trade-off:

- consumes vertical space
- may reduce efficiency on small screens
- risks turning important structure into spectacle

### 6.3 Approach C: Minimal Title Bars With Rich Content Panels

Keep title bars lightweight and rely on content areas for differentiation.

Benefits:

- very modern and minimal
- preserves maximum content area

Trade-off:

- weaker page recognition
- does not fully satisfy the user's accessibility goal for title-bar-led page separation

## 7. Recommended Direction

Use **Approach A: Unified Creative Workbench**.

This direction best matches the confirmed goals:

- all pages gain a consistent page-separation system
- title bars become a reliable accessibility affordance
- the product feels more like one coherent creative environment
- the result stays practical for Android Compose implementation

## 8. Information Architecture And Shared Page Skeleton

All pages should align to a shared four-layer structure.

### 8.1 Layer One: Page Title Bar

This layer defines the page itself.

Responsibilities:

- state the page name
- expose back navigation where the page is a child route
- expose only high-frequency or structurally important actions
- optionally show compact secondary context

### 8.2 Layer Two: Page Context Strip

This layer appears only when the screen needs it.

Responsibilities:

- explain current source, state, or mode
- announce relationships such as current draft, template, or read-only state
- support accessibility by reducing ambiguity before the user reaches the main content

This must not be used for filler copy.

### 8.3 Layer Three: Core Work Region

This is the actual workspace:

- editor canvas
- preview result
- history list
- template list
- settings groups
- AI placeholder content

The work region should feel like the primary focus after the title bar has established orientation.

### 8.4 Layer Four: Primary Action Region

Use a bottom CTA only when a screen genuinely has one dominant action.

Examples:

- editor: go to preview
- preview: copy rich text
- template selection: confirm template

Screens such as history and settings should usually avoid bottom CTAs.

## 9. Visual Language

### 9.1 Title Bar Tone

The title bar should feel like a light editorial chapter header rather than a dense utility toolbar.

Characteristics:

- generous top spacing
- strong page title
- restrained secondary text
- low-noise action layout
- soft container color using theme-derived tonal surfaces

The visual separation should be obvious but not heavy.

### 9.2 Content-Creation Workbench Atmosphere

The app should emphasize the feeling of shaping content:

- editor feels like a writing surface
- preview feels like a finished result surface
- history feels like a shelf of saved drafts
- templates feel like a curated style selection space

This should come from layout rhythm, surfaces, and hierarchy rather than gimmicks.

### 9.3 Reduced Card Overuse

The current UI often relies on separate cards to create structure. The refresh should reduce that dependence.

Structure should come more from:

- title bars
- section labels
- spacing
- surface tiers
- grouped containers

Cards remain useful when a bounded group truly needs a shared container.

## 10. Screen-Level Design

## 10.1 Editor

### Title Bar

- primary title: `编辑`
- secondary context: current draft name or `未命名草稿`
- actions: undo, redo, overflow

### Context Strip

Show compact context such as:

- selected template
- lightweight editor status
- editing preferences or helper mode summary

### Work Region

The main editor becomes the strongest visual surface on the page. Supporting controls such as template summary, markdown shortcuts, and editor preferences should read as assistant tools around the writing surface, not as competing top-level cards.

### Primary Action

- bottom CTA: `预览成品`

## 10.2 Preview

### Title Bar

- primary title: `预览`
- secondary context: source identity such as current draft or history entry
- actions: refresh, export, share

### Context Strip

Use a compact read-only statement, for example communicating that the result is not editable here.

### Work Region

The preview surface should dominate the page. Explanatory copy should be reduced and moved into the context layer where possible.

### Primary Action

- bottom CTA: `复制公众号富文本`

## 10.3 History

### Title Bar

- primary title: `历史`
- no duplicate page title inside the list content

### Context Strip

Optional compact helper information can explain interaction model if needed, but it should remain concise and accessible.

### Work Region

The saved entries list should begin directly as the main content. The page identity should no longer be embedded as a content header.

### Primary Action

No bottom CTA by default.

## 10.4 Template Selection

### Title Bar

- primary title: `选择模板`
- back navigation shown
- secondary context: currently selected template or current draft relationship when useful

### Context Strip

Can briefly explain that the choice applies to the current draft.

### Work Region

Grouped template choices remain the main body, but the overall page should feel like a step in the writing workflow rather than a detached picker.

### Primary Action

- bottom CTA: `确定使用`

## 10.5 Settings

### Title Bar

- primary title: `设置`

### Context Strip

Usually unnecessary unless future settings sections need active mode summaries.

### Work Region

Keep settings semantics and structure, but visually align spacing, title treatment, and surface rhythm with the rest of the app so the page no longer feels like it belongs to a separate design language.

## 10.6 AI Placeholder

### Title Bar

- primary title: `AI 辅助`

### Context Strip

Use compact state text such as `实验室功能，即将开放`.

### Work Region

The placeholder page should still feel like a proper page, not merely an unavailable tab target.

## 11. Accessibility Specification

### 11.1 Page Entry Hierarchy

Every screen should expose:

- a page-level heading in the title bar
- optional concise context immediately after the heading
- the primary content region after that

This ordering should be stable across all pages.

### 11.2 Heading Ownership

The title bar owns the page heading. Content regions should not repeat the same page title as another heading when that repetition adds no meaning.

Example:

- history should not announce `历史` in the title bar and then again as the first content heading

### 11.3 Secondary Context Rules

Secondary context should only convey useful meaning:

- current draft name
- read-only state
- current template
- experimental feature status
- source relationship

It should not become decorative subtitle clutter.

### 11.4 Primary Action Stability

Where a page has a dominant bottom action, its purpose and position should remain stable so assistive technology users can predict it.

### 11.5 Child Routes

Pages with back navigation should use the title bar to clarify both:

- the current page identity
- the fact that the user is in a child step of a larger workflow

## 12. Component And Implementation Boundaries

The implementation should likely introduce or refactor shared page-shell primitives rather than restyling every screen independently.

Likely shared building blocks:

- page title bar component
- optional page context strip component
- shared page spacing tokens
- shared bottom CTA container pattern

This should happen inside the current Compose architecture and existing navigation graph.

## 13. Data And State Implications

The design mostly affects presentation, but some contextual details may need better screen inputs, such as:

- current draft title surfaced clearly in editor
- source label surfaced clearly in preview
- selected template summary surfaced clearly in editor and template selection
- experimental state surfaced clearly in AI placeholder

These are UI-model concerns, not major domain-model changes.

## 14. Error Handling And Empty States

Page structure should stay intact even when content is empty or unavailable.

Examples:

- empty history still shows the history title bar first, then the empty-state content
- preview errors or empty states still remain under the preview title bar and read-only context
- AI unavailable state still appears as a proper page under a clear title bar

The page boundary should never disappear just because the content region is empty.

## 15. Testing Requirements

Testing should verify both structure and semantics.

Required coverage:

- page title appears in the title bar across editor, preview, history, template selection, settings, and AI pages
- history no longer duplicates its page title inside content unnecessarily
- title bar headings remain the primary semantic page headings
- contextual subtitles or strips appear only where expected
- bottom primary actions remain stable on pages that require them
- empty states still preserve the page title bar structure

Where practical, Compose UI tests should verify heading presence and stable page entry hierarchy.

## 16. Recommended Execution Order

1. Build shared page-shell primitives for title bar, context strip, and bottom CTA framing
2. Refactor history first, because it currently lacks page-level title-bar separation
3. Refactor editor, preview, and template selection onto the shared shell
4. Align settings and AI placeholder to the same title and spacing language
5. Run an accessibility sweep to remove duplicated page headings and confirm screen entry order

## 17. Expected Outcome

After this refresh:

- every page feels clearly separate and immediately identifiable
- accessibility improves because page identity is announced consistently
- the product feels less like a collection of forms and more like a coherent content creation workbench
- the user experience becomes closer to `enter page -> understand page -> perform task`

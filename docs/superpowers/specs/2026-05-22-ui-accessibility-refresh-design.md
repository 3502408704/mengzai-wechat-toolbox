# UI And Accessibility Refresh Design

## 1. Context

Current Compose screens already provide the core editing, preview, history, template, and clipboard flows, but user feedback shows two clear problems:

- The visual system still feels like a stack of generic cards and buttons rather than a modern Android Material You app.
- Accessibility semantics and focus behavior are incorrect in several places, especially history, settings, and editor controls.

This design refresh keeps the existing native Android architecture and Compose implementation, but upgrades the presentation, interaction hierarchy, and accessibility model so the app feels consistent with recent Android Material Design guidance and is easier to use with TalkBack.

## 2. Goal

Refresh the editor, preview, history, settings, and bottom navigation UI so they:

- fully align with a modern Material You / Dynamic Color presentation
- use clearer button hierarchy and page structure
- follow Google Android accessibility guidance for semantics, focus, and actions
- support the user-approved history interaction model:
  - tap history item to open preview
  - long press to open actions menu
  - expose `Preview`, `Edit`, and `Delete` through accessibility actions

## 3. Non-Goals

This refresh does not:

- replace the app with a web UI
- redesign the overall product information architecture beyond current tabs
- add a dedicated history detail page
- introduce complex onboarding or tutorial overlays
- change the existing template rendering engine or publish HTML pipeline

## 4. Core Design Principles

### 4.1 Native Android First

All changes stay inside Jetpack Compose and preserve the app's native Android feel. The target visual direction is:

- Google Material 3 page structure and component language
- Apple-like restraint in density and hierarchy
- low-noise layouts with fewer competing buttons

### 4.2 Dynamic Color Should Drive Atmosphere

Dynamic Color is already wired at a basic level. This refresh expands it into a fuller token system so wallpaper-derived color affects:

- primary and secondary emphasis
- selected navigation state
- filled and tonal button containers
- surface container tiers
- dividers, outlines, and low-emphasis regions

Fallback palettes remain for Android versions below Android 12.

### 4.3 One Primary Action Per Screen

Each page should have one obvious main task:

- editor: write and prepare content
- preview: inspect final result and copy publish-ready HTML
- history: browse saved entries
- settings: adjust preferences

Secondary actions move into app bars, helper rows, or overflow menus.

### 4.4 Accessibility Is Structural, Not Decorative

Accessibility cannot be added by sprinkling `contentDescription` onto everything. It must come from correct focus grouping, correct control ownership, and correct action exposure.

## 5. Visual System Refresh

## 5.1 Theme Tokens

The theme layer should expand beyond the current minimal `primary/background/surface` setup and define a fuller Material 3 token set, including:

- `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`
- `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`
- `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`
- `background`, `onBackground`
- `surface`, `onSurface`
- `surfaceVariant`, `onSurfaceVariant`
- `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest`
- `outline`, `outlineVariant`
- `error`, `onError`, `errorContainer`, `onErrorContainer`

These tokens should be used intentionally rather than leaving most pages on default flat cards.

## 5.2 Typography

Typography should stop relying on bare `Typography()` defaults and define a consistent hierarchy for:

- page titles
- section titles
- list item titles
- body text
- supporting metadata
- small status labels

The goal is not heavy branding, but more stable visual rhythm and clearer priority.

## 5.3 Shared Layout Rhythm

All primary pages should use a common skeleton:

- top app bar
- consistent horizontal padding
- clear section spacing
- restrained use of cards
- one bottom CTA only when the screen truly has a single dominant next step

This removes the current "title block plus several unrelated cards" feel.

## 6. Screen-Level Design

## 6.1 Editor Screen

### Purpose

The editor remains the primary workspace for input and revision.

### New Structure

- top app bar:
  - import
  - overflow menu for lower-frequency actions
- helper section near top:
  - template selection
  - markdown shortcuts
  - editor controls such as line numbers and font scale
- central workspace:
  - editor surface with stronger visual identity
  - fixed line-number column relationship
  - compose-native text input remains the true editing node
- bottom primary CTA:
  - `预览成品`

### Button Hierarchy

- main action: preview
- helper actions: undo, redo, import, new draft
- lower-priority actions should move out of the main horizontal button row

### Visual Direction

The editor should feel like a focused writing surface, not a card full of small controls.

## 6.2 Preview Screen

### Purpose

The preview screen should feel like a result viewer, not a settings form.

### New Structure

- top app bar:
  - refresh
  - export
  - share or overflow actions
- content-first main region:
  - preview result dominates the page
  - empty, loading, and failure states remain centered around the preview surface
- bottom fixed primary CTA:
  - `复制公众号富文本`

### Visual Direction

The current explanatory card should be reduced. The content itself should dominate.

## 6.3 History Screen

### Purpose

History becomes a browsing list, not a grid of restore buttons.

### New Structure

- top app bar
- lightweight list of history entries
- each entry presents:
  - display title
  - format label
  - natural-language timestamp
  - favorite state
  - optional one-line summary for visual scanning

### Interaction Model

- tap item: open preview for that history entry
- long press item: open action menu
- menu actions:
  - `预览`
  - `编辑`
  - `删除`
- if favorite remains user-facing, it should be a secondary menu action rather than a competing inline button

### Naming Rules

History entry titles follow this order:

1. imported file name when the record originates from file import
2. first level-one heading when the record comes from pasted or edited content
3. `未命名历史记录` as fallback

The title itself should not append format or time.

## 6.4 Settings Screen

### Purpose

Settings should look and behave like a system preference screen.

### New Structure

Sections:

- `外观`
  - theme mode
  - dynamic color
- `编辑器`
  - line numbers default
  - font-related preferences as applicable
- `实验室`
  - AI assistant placeholder
- `开发者`
  - developer mode
- `关于`
  - future reserved informational items

### Visual Direction

Use grouped list rows instead of a stack of large cards for each individual setting.

## 6.5 Bottom Navigation

### Purpose

Bottom navigation should look like a real navigation system, not a placeholder strip.

### New Structure

- replace `Text("•")` icons with real Material icons
- use Material 3 selected-state treatment
- give disabled AI entry a calm but explicit placeholder treatment

### Disabled AI Tab

The AI tab remains present but must clearly communicate:

- visually: upcoming or unavailable
- semantically: `即将推出，不可用`

## 7. Accessibility Specification

## 7.1 Global Rules

- Do not replace visible button, switch, or radio labels with redundant `contentDescription`.
- Use `contentDescription` mainly for icon-only controls or missing non-visual meaning.
- One logical object should usually produce one primary focus target.
- Supporting text, state, and control semantics should be merged into one understandable unit when they represent one setting or one list item.
- Avoid exposing both a clickable row and a separately clickable child control when they mean the same thing.

## 7.2 History Accessibility Model

Each history item is a single primary focus node.

Primary spoken content should include:

- title
- format
- favorite state
- natural-language time
- optional short summary only if it helps

Primary spoken content should not include tutorial text like:

- "double tap to preview"
- "long press for more"

Those instructions are intentionally excluded from the main label because they are redundant and noisy.

### Accessibility Actions

History items should expose custom accessibility actions:

- `预览`
- `编辑`
- `删除`

These actions mirror the visual action menu and give TalkBack users a direct equivalent to long press.

### Visual Long Press

Long press remains the visual gesture for opening the actions menu, but accessibility support should not rely on the user discovering or hearing long-press tutorial text in the label.

## 7.3 Settings Accessibility Model

Each setting row should be one focusable object.

### Radio Rows

For theme selection:

- keep one clickable semantic owner per option
- do not let both the outer row and the inner radio button act as separate equivalent actions

### Switch Rows

For dynamic color and developer mode:

- the row owns the semantic meaning
- the switch is the visual state indicator and interaction affordance
- title, description, and current state should feel like one object to TalkBack

## 7.4 Editor Accessibility Model

- the actual editable text field remains the real editing node
- outer decoration containers should not steal the primary editing description
- template selector, line-number toggle, and font controls should be grouped by logical row

## 7.5 Preview Accessibility Model

The preview area should be described as a result-reading region:

- readable
- scrollable
- not editable

## 7.6 Navigation Accessibility Model

Bottom navigation items should expose:

- correct label
- selected state
- disabled state where applicable

The AI item must be understandable as unavailable, not merely dimmed.

## 8. Data And Content Implications

This UI refresh requires small but meaningful content-model support around history presentation:

- history origin awareness when deciding file-name-derived title vs heading-derived title
- helper logic to extract the first level-one heading from raw content
- natural-language date formatting suitable for visual display and screen readers

This does not require changing the current rendering pipeline, but it does require better presentation helpers and possibly small repository or UI-model extensions.

## 9. Implementation Boundaries

This refresh should cover:

- theme token expansion
- typography hierarchy
- shared page scaffolding refinements
- editor layout cleanup
- preview layout cleanup
- history interaction redesign
- settings list redesign
- bottom navigation modernization
- semantic cleanup and accessibility action support

This refresh should not introduce:

- a brand-new navigation architecture
- a separate history details screen
- web-based UI
- large unrelated business logic refactors

## 10. Testing Requirements

Testing should cover both UI logic and accessibility behavior:

- history title derivation from file name and first H1
- history item action routing for preview, edit, and delete
- settings row focus ownership behavior
- semantics-focused Compose UI tests where practical
- regression checks for preview copy flow and editor behavior after layout changes

## 11. Recommended Execution Order

1. Expand theme and page scaffolding tokens
2. Refactor bottom navigation and shared top-level page structure
3. Refactor history screen interaction model and title derivation
4. Refactor settings focus ownership
5. Refactor editor and preview hierarchy
6. Sweep incorrect semantics and add accessibility actions


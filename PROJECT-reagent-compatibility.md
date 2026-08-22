# Reagent Test Suite Compatibility & Branch Stabilization

## Objective

Methodically achieve 100% test passing across the test suite under both:
1. **Eucalypt** (`pnpm test | grep -v squint` via Vitest / Happy-DOM / Squint)
2. **Real Reagent** (`pnpm test:reagent-all` via Scittle / Happy-DOM)

Reagent behavior is the ground truth.

> **Note for AI assistants**: Always append new information, status updates, and findings to this document. Do NOT remove or overwrite historical context or old log entries unless explicitly instructed by the user.

---

## Current Immediate Strategy

### Phase 1: Main Branch Diagnosis & Stabilization (Completed)
Before resuming Reagent runner work, we established a clean baseline on `main` and verified local health.

- [x] **Investigate CI vs Local Discrepancies (Ground Truth)**:
  - Discovered local `main` was 4 commits behind `origin/main`.
  - An untracked `raf_test.test.cljs` was lingering locally, causing Vitest empty suite errors.
- [x] **Fix Real Breakages on `main`**:
  - Removed untracked `raf_test.test.cljs`.
  - Reset `main` to `origin/main` (`0.0.16` / `squint-cljs@0.14.207`).
- [x] **Verify `main` is 100% Green**:
  - Confirmed `pnpm test` passes locally with 42/42 test files passing (112 total tests, 0 failures).

### Phase 2: Merge & Re-baseline `reagent-runner` Branch (Completed)
- [x] Checkout `reagent-runner` branch.
- [x] Merge clean `main` into `reagent-runner`.
- [x] Re-run full test audit:
  - [x] `pnpm test` (54/54 test files passing, 185 tests total).
  - [x] `pnpm test:reagent-all` (Installed missing `react`, `react-dom`, `scittle` devDeps; 23 files passed completely, 27 had failures, 4 timed out).

### Phase 3: Reagent Runner Test Alignment
- [ ] Fix timeout/hanging tests under Reagent runner (`reentrant_render`). (Note: `multiple_select`, `radio_buttons`, and `reentrant_render` fixed!).
- [ ] Category 1: Update synchronous DOM assertions to async `(th/wait-for-render)` for Reagent compatibility.
- [ ] Category 2: Standardize event dispatching using `th/fire-event`.
- [ ] Category 3: Fix EDN / string representation differences under Scittle/ClojureScript.
- [ ] Category 4: Address deep reconciliation and structural test failures.
- [ ] Ensure 100% test pass rate in both Eucalypt and Reagent runner modes.

---

## Failure Categories & Tracking List

*Note: Only check items off this list after confirming the test passes in both Eucalypt (`pnpm test`) and Reagent runner (`pnpm test:reagent`).*

### Category 1: Async Rendering & Event Simulation
*Cause*: Reagent batches DOM updates asynchronously via `requestAnimationFrame` / React event loop. Additionally, direct `.dispatchEvent` on Happy-DOM nodes can bypass React synthetic event delegation.
*Fix Strategy*: Always try wrapping assertions in `(th/wait-for-render)` promise chains first. If standard DOM events fail under Reagent, use `th/fire-event`.

- [x] `src/test/src/prop_change_rerender.test.cljs` — Toggles atom via button click.
- [x] `src/test/src/boolean_attributes.test.cljs` — Toggles checkboxes/disabled attributes via button clicks.
- [x] `src/test/src/empty_fragment.test.cljs` — `reset!` on atom to hide fragment child.
- [x] `src/test/src/fragment_clickable.test.cljs` — Clicks on section element to convert slots to coins.
- [x] `src/test/src/click_swap.test.cljs` — Button click that updates a `nil` atom.
- [x] `src/test/src/fragment_switching.test.cljs` — `reset!` on atom to switch page component.
- [x] `src/test/src/multiple_instances.test.cljs` — Counter button clicks on independent components.
- [x] `src/test/src/list_rerender.test.cljs` — Clicks to select/remove list items.
- [x] `src/test/src/text_input.test.cljs` — Typing into `<input type="text">`.
- [x] `src/test/src/textarea.test.cljs` — Typing into `<textarea>`.
- [x] `src/test/src/numeric_input.test.cljs` — Input events on `<input type="number">`.
- [x] `src/test/src/range_slider.test.cljs` — Input events on `<input type="range">`.
- [x] `src/test/src/select_attribute.test.cljs` — Changing option selection.
- [x] `src/test/src/enter_to_submit.test.cljs` — Input event followed by Enter `keydown` event.
- [x] `src/test/src/list_demo.test.cljs` — Multi-step additions/deletions in a loop (needs promise chaining across steps).
- [x] `src/test/src/timer.test.cljs` — Uses `sleep` promises, but clicking toggle button needs `wait-for-render` before checking hidden state.
- [x] `src/test/src/various_events.test.cljs` — Dispatches events like blur/dblclick; test with `wait-for-render` first, then `fire-event` if needed.
- [x] `src/test/src/todomvc.test.cljs` — Multi-step assertions across editing, filtering, toggling, and clearing todos require async promise chains.

### Category 3: EDN / String Representation Differences
*Cause*: Printing values using `pr-str` or map serialisation differs between Squint and ClojureScript/Scittle.
*Fix*: Standardise output formatting or assertion parsing across runtimes.

- [x] `src/test/src/shared_state_multiple_roots.test.cljs`
- [x] `src/test/src/nested_ratoms_race.test.cljs`

### Category 4: Deep Reconciliation & Structural Test Differences
*Cause*: Tests expecting exact React container lifecycle/cleanup, raw DOM manipulation, string style props, or complex keyed node reordering.

- [x] `src/test/src/camel_case_events.test.cljs` — Uses `(aget el "on...")` DOM property checks and unsupported `:onFocusIn`/`:onFocusOut` props. Potential fix: Refactor DOM property checks to behavioral event tests.
- [x] `src/test/src/event_handler_registration.test.cljs` — Uses `(aget el "onclick")` property checks. Potential fix: Replace DOM property checks with behavioral event testing (`th/fire-event` + `wait-for-render`).
- [x] `src/test/src/reentrant_render.test.cljs` (moved remaining failing test to behavioural differences list)
- [x] `src/test/src/nested_fors.test.cljs`
- [x] `src/test/src/style_attribute.test.cljs`
- [ ] `src/test/src/ref_cleanup.test.cljs`
- [ ] `src/test/src/render_diff_fundamentals.test.cljs`
- [ ] `src/test/src/keyed_list_reordering.test.cljs`
- [ ] `src/test/src/uncontrolled_and_focus.test.cljs`
- [ ] `src/test/src/component_reconciliation.test.cljs`

---

## Tests Failing Due to Behavioral Differences Between React/Reagent and Eucalypt

This section documents verified behavioral differences between React/Reagent 1.0 (the canonical ground truth) and Eucalypt as an actionable checklist. These will be revisited to decide resolution (most likely updating Eucalypt to match Reagent behavior):

- [ ] **Root Hierarchy Change / Element vs Fragment Unmounting (`reentrant_render.test.cljs` test 5)**:
  - **Test**: `should keep child state when switching fragment and non-fragment roots`
  - **Scenario**: A component switches its top-level return value between a Fragment `[:<> [:p ...] [child-counter] [switch]]` and a DOM element `[:div [:p ...] [child-counter] [switch]]`.
  - **React/Reagent Behavior**: React treats the difference in parent DOM container / node type at the root as a tree unmount, destroying all child component state and remounting `child-counter` with initial state (`0`).
  - **Eucalypt Behavior**: Eucalypt's keyed component cache preserves the child component's internal state atom even across parent node hierarchy restructuring, keeping the count (`1`).
  - **Resolution needed**: Align Eucalypt's tree unmounting/reconciliation with React when root container node types change, then align the test assertion accordingly.

- [ ] **DOM Attribute Emission for `:key` in Props Map**:
  - **Scenario**: In Hiccup, `:key` can be specified either via metadata `(with-meta [:span ...] {:key "foo"})` or in the attributes/props map `[:span {:key "foo"} ...]`.
  - **React/Reagent Behavior**: `:key` (along with `:ref`) is a reserved reconciler prop and is never emitted to the DOM as an HTML attribute (`key="foo"`).
  - **Eucalypt Behavior**: Due to custom attribute pass-through, `[:span {:key "foo"} ...]` currently sets `key="foo"` on the DOM node.
  - **Resolution needed**: Exclude `:key` (and verify `:ref`) from being rendered into DOM element attributes during reconciliation in `src/eucalypt.cljs`, then run full test suites across both Eucalypt and Reagent runner to ensure no regressions.

## Log & Observations

- **2026-08-14**:
  - Updated `src/test/src/event_handler_registration.test.cljs` to use standard `:on-mouse-down`, `th/fire-event`, and `(th/wait-for-render)` promise chains. Extracted Eucalypt-specific custom event/attribute assertions into `src/test/src/eucalypt_extensions.test.cljs`. Verified 100% pass rate in both Eucalypt and Reagent runner. Marked `event_handler_registration.test.cljs` as complete.
  - Updated `src/test/src/camel_case_events.test.cljs` to test `:onFocus` and `:onBlur` instead of unsupported `:onFocusIn` and `:onFocusOut`, and removed unused `focusin`/`focusout` mappings in `helpers.cljs`. Verified 100% pass rate in both Eucalypt and Reagent runner. Marked `camel_case_events.test.cljs` as complete.
  - Updated `src/test/src/todomvc.test.cljs` to use `th/fire-event` and `(th/wait-for-render)` promise chains across all multi-step interactive tests (add, toggle, delete, edit, escape, blur, clear, complete-all, filter). Verified 100% pass rate (10/10 tests) in both Eucalypt and Reagent runner. Marked `todomvc.test.cljs` as complete.
  - Documented potential solution for `event_handler_registration.test.cljs` and `camel_case_events.test.cljs`: replace `(aget el "on<event>")` DOM property assertions with behavioral event firing (`th/fire-event` + `wait-for-render` + state assertions) since React/Reagent uses event delegation instead of direct DOM node event properties.
  - Fixed `helpers/fire-event` to map `"doubleclick"`/`"dblclick"` to standard `"dblclick"` DOM events under Eucalypt.
  - Updated `src/test/src/various_events.test.cljs` to use `th/fire-event` and `(th/wait-for-render)` promise chains. Verified 100% pass rate in both Eucalypt and Reagent runner. Marked `various_events.test.cljs` as complete.
- **2026-08-13**:
  - Updated `helpers/fire-event` to dispatch both `"change"` and `"input"` events for text inputs (`<input>` and `<textarea>`) under Eucalypt, aligning event dispatch with Reagent/React synthetic event handling.
  - Updated `src/test/src/text_input.test.cljs` to use `th/fire-event` and `(th/wait-for-render)` promise chains. Verified 100% pass in both Eucalypt and Reagent runner. Marked `text_input.test.cljs` as complete.
  - Updated `src/test/src/timer.test.cljs` to use `(th/wait-for-render)` promise chains after toggling timer button, and toggled interval off at test end for clean process exit. Verified 100% pass in both Eucalypt and Reagent runner. Marked `timer.test.cljs` as complete.
- **2026-08-12**:
  - Performed initial test audit of 55 test files across Eucalypt and Reagent runner. Created `PROJECT-reagent-compatibility.md` to track progress and state across LLM sessions.
  - Switched to `main` branch to inspect base health.
  - Reset `main` to `origin/main` and removed untracked `raf_test.test.cljs`.
  - Confirmed `pnpm test` on `main` is 100% green (42 passed, 112 tests, 0 failures).
  - Checked out `reagent-runner` branch and merged `main` into it.
  - Resolved `package.json` merge conflict by keeping updated `squint-cljs@0.14.207` from `main` and regenerated `pnpm-lock.yaml`.
  - Updated `click_swap.test.cljs` and `nested_ratoms_race.test.cljs` components to use `pr-str` instead of `js/JSON.stringify` to align with EDN test assertions.
  - Confirmed `pnpm test` on `reagent-runner` branch is 100% green (54/54 test files, 185 total tests passing).
  - Re-installed missing devDependencies (`react@17.0.2`, `react-dom@17.0.2`, `scittle@0.8.32`) to restore Reagent runner test environment.
  - Completed `pnpm test:reagent-all` audit on `reagent-runner` branch:
    - **23 files passed 100%** under real Reagent.
    - **27 files had assertion failures** (mostly due to synchronous DOM checks requiring `(th/wait-for-render)` for Reagent's asynchronous rendering).
    - **4 files timed out / errored**: `multiple_select`, `radio_buttons`, `reentrant_render`, `todomvc`.
  - Added `beforeEach` hook support to `src/test/reagent-runner/vitest-shim.cljs`.
  - Updated string formatting in `src/test/src/multiple_select.test.cljs` using `.join` on `clj->js` vector for consistent string representation across ClojureScript and Squint.
  - Diagnosed `multiple_select.test.cljs` error under Reagent runner: React 17 synthetic event system attempts to set `eventPhase` on raw Happy-DOM `js/Event`, throwing `TypeError: Cannot set property eventPhase of #<Event> which has only a getter`.
  - Added `eventPhase` prototype patch in `src/test/reagent-runner/runreagent.cljs` to allow React 17 synthetic event dispatching on Happy-DOM events.
  - Added `th/fire-event` helper in `src/test/src/helpers.cljs` that uses `ReactTestUtils.Simulate` when available (under Reagent runner / React 17) and standard `dispatchEvent` under Eucalypt.
  - Updated `multiple_select.test.cljs` to use `(th/fire-event select-el "change")` and `(th/wait-for-render)`.
  - Confirmed `multiple_select.test.cljs` now passes 100% in both Eucalypt (`pnpm test`) and Reagent runner (`pnpm test:reagent`).
  - Updated `select.test.cljs` to use `(th/fire-event select-el "change")` and `(th/wait-for-render)`. Confirmed 100% pass in both Eucalypt and Reagent runner.
  - Updated `radio_buttons.test.cljs` to use native `(.click radio-a)` and `(th/wait-for-render)` promise chains. Confirmed 100% pass rate in both Eucalypt (`pnpm test`) and Reagent runner (`pnpm test:reagent`).
  - Updated `helpers/fire-event` to default `"click"` events to native `(.click el)` in all environments because `ReactTestUtils.Simulate.click` does not toggle native `checked` DOM state or fire React `onChange` handlers on radio/checkbox inputs.
  - Updated `src/test/src/prop_change_rerender.test.cljs` with `(th/wait-for-render)` promise chains after button clicks. Confirmed 100% pass rate in both Eucalypt and Reagent runner.

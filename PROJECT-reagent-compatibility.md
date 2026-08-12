# Reagent Test Suite Compatibility & Branch Stabilization

## Objective

Methodically achieve 100% test passing across the test suite under both:
1. **Eucalypt** (`pnpm test` via Vitest / Happy-DOM / Squint)
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
- [ ] Fix timeout/hanging tests under Reagent runner (`multiple_select`, `radio_buttons`, `reentrant_render`, `todomvc`).
- [ ] Update synchronous DOM assertions to async `(th/wait-for-render)` for Reagent compatibility.
- [ ] Ensure 100% test pass rate in both Eucalypt and Reagent runner modes.

---

## Log & Observations

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

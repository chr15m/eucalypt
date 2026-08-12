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
- [ ] Re-run full test audit (`pnpm test` and `pnpm test:reagent-all`).

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

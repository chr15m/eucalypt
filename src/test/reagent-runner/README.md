# Reagent Runner

A test harness for running Reagent ClojureScript files in a simulated browser environment to verify canonical Reagent behavior.

## Purpose

This tool runs `.cljs` files containing Reagent components using Scittle in a happy-dom simulated browser environment. The output serves as the canonical reference for how Reagent components should behave, which we can compare against Eucalypt + Squint implementations.

## How It Works

1. `runreagent.cljs` - An nbb script that:
   - Sets up a happy-dom browser environment
   - Loads React and ReactDOM
   - Evaluates the target ClojureScript file using Scittle
   - Renders the resulting DOM to console

2. `reagent-update-script.cljs` - An example Reagent component demonstrating the pattern

## Usage

```bash
npx nbb src/test/reagent-runner/runreagent.cljs <path-to-cljs-file>
```

Example:

```bash
npx nbb src/test/reagent-runner/runreagent.cljs src/test/reagent-runner/reagent-update-script.cljs
```

This will output the rendered DOM after component interactions (e.g., button clicks).

## Writing Test Scripts

Your `.cljs` file should:
- Use `reagent.core` and `reagent.dom` namespaces
- Define components using Reagent's hiccup syntax
- Render to `(.getElementById js/document "app")`
- Perform any interactions (clicks, etc.) to test state updates

The runner will execute the script and display the final DOM state.

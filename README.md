# Eucalypt

Eucalypt is a frontend library for [Squint ClojureScript](https://github.com/squint-cljs/squint).
It replaces [Reagent & React](https://reagent-project.github.io/) with a compatible-ish subset of the Reagent API.
It supports form-1 and form-2 Reagent components.

The goal is to build very small frontend artifacts (<10k) using "Reagent" and "ClojureScript" (if you squint hard enough).
It's suitable for small pieces of one-off frontend code that do something simple, not large production web apps.
The examples from the Reagent homepage have been ported and work with Eucalypt.
Eucalypt is itself very small and fits in a single cljs file.

<!-- end-about -->

- **[Try the demo](https://chr15m.github.io/eucalypt/)** - a single HTML file artifact gzipped to ~15k.
- [See the demo source code for examples](https://github.com/chr15m/eucalypt/tree/main/src/demo/).
- [Check out the tests](https://github.com/chr15m/eucalypt/tree/main/src/test/src/)
- ![Test badge](https://github.com/chr15m/eucalypt/actions/workflows/ci.yml/badge.svg)

[Use](#use) | [Bugs](#bugs) | [Dev](#dev) | [Tests](#tests) | [Build](#build) | [Use of AI](#use-of-ai) | [Mr Clean](#mr-clean-original-readme)

## Use

Install with npm:

```
npm i eucalypt
```

Use it in your Squint cljs script like this:

```
(ns my-app
  (:require
    [eucalypt :as r]))

(defonce state (r/atom {}))

(defn component:main [state]
  [:<>
    [:p "Hello world!"]
    [:pre (pr-str @state)]])

(r/render
  [component:main state]
  (js/document.getElementById "app"))
```

## Bugs

If you find an example form-1 or form-2 component that works in Reagent but doesn't work with Eucalypt, please create a failing test case in the `tests/src` folder, and raise a PR.

## Dev

```
pnpm i
npm run dev
```

## Tests

```
npm run test
```

## Build

```
npm run build
```

## Use of AI

Eucalypt is a fork of [Mr Clean](https://bitbucket.org/sonwh98/mr-clean/) that has been slop-coded with AI into compiling with Squint.

Slop-coded means this library was built by 1. creating failing test cases 2. using an LLM to fix the code until all tests pass.
If you're uncomfortable with this method of development, which involves less human oversight, then you may not want to use this library.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.

What's with the name? Eucalyptus oil (sometimes used in cleaning) looks like a reagent if you squint hard enough.

---

## Mr Clean (original README)

Mr Clean is a Reagent compatible ClojureScript library without dependency on React.js. At the heart of React.js
is a very simple idea GUI = function(data). Reagent takes this idea even further hiccup = function(data).  The
advantage of using Mr Clean over Reagent is that the generated javascript of a hello-world app is half
the size of that of reagent.

There is no diffing of virtual DOM. The state of components is flushed to the DOM when data changes. 

### What works?

Ractive atoms and cursors work as expected which are the important parts making components reactive to data changes. 
form 1, form 2 components are fully supported.

### What doesn't work?

None of the reagent macros are implemented. r/reaction is not a macro but a function that returns an atom holding the result of evaluating a function.

form 3 components are not fully supported because not all lifecycle methods are implemented.


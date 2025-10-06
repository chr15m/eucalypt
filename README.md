# Eucalypt

<p align="center" id="logo"><img src="docs/eucalypt.svg" alt="Eucalypt leaf logo" width="128"/></p>

Eucalypt is a frontend library for [Squint ClojureScript](https://github.com/squint-cljs/squint).
It replaces [Reagent & React](https://reagent-project.github.io/) with a compatible-ish subset of the Reagent API.
It supports form-1 and form-2 Reagent components.

The goal is to build very small frontend artifacts (~10k) using "Reagent" and "ClojureScript" (if you squint hard enough).
It's suitable for small pieces of one-off frontend code that do something simple, not large production web apps.
The examples from the Reagent homepage have been ported and work with Eucalypt.
Eucalypt is itself very small and fits in a single cljs file.

<!-- end-about -->

- **[Try the demo](https://chr15m.github.io/eucalypt/)** - a single HTML file artifact gzipped to ~16k.
- [See the demo source code for examples](https://github.com/chr15m/eucalypt/tree/main/src/demo/).
- [![Latest test Results](https://raw.githubusercontent.com/chr15m/eucalypt/build/test-badge.svg)](https://github.com/chr15m/eucalypt/blob/build/test-results.md) [Tests source code](https://github.com/chr15m/eucalypt/tree/main/src/test/src/)
- Note: Eucalypt was [slop-coded with AI and low human oversight](#use-of-ai) so please be careful.

*What's with the name?* Eucalyptus oil, which is sometimes used in cleaning, looks like a reagent if you squint hard enough.

[Use](#use) | [Bugs](#bugs) | [Gotchas](#gotchas) | [Dev](#dev) | [Tests](#tests) | [Build](#build) | [Use of AI](#use-of-ai) | [Mr Clean](#mr-clean-original-readme)

## Use

Install with npm:

```shell
npm i eucalypt
```

Use it in your Squint cljs script like this:

```clojure
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

There's also a "create" script you can use to bootstrap a Squint, Vite, and Eucalypt project:

```shell
npm create eucalypt myapp
cd myapp
npm install
npm run watch
npm run build # <- builds a dist/index.html
```

#### Use in JavaScript

Thanks to @borkdude for [this example](https://codepen.io/borkdude/pen/OPMNvwa).

```javascript
import { render, atom } from "https://esm.sh/eucalypt";
import { deref, swap_BANG_, update_in } from "https://esm.sh/squint-cljs/core.js";

const swap = swap_BANG_;

const state = atom({counter: 0});

function myComponent () {
 return ["<>",
          ["div", "Hello world"],
          ["div", "Counter: ", deref(state).counter],
          ["button", {"on-click": () => {
            console.log('clock')
            swap(state, update_in, ["counter"], (x) => x + 1);           
            }
          }, "inc"]
        ];
}

render([myComponent], document.getElementById("app"))
```

## Bugs

If you find an example form-1 or form-2 component that works in Reagent but doesn't work with Eucalypt, please create a failing test case in the `tests/src` folder, and raise a PR.

## Gotchas

Eucalypt gotchas:

- Not currently reentrant. State is shared globally.
- The only well-tested API is `r/atom` and `r/render`.
- Lots and lots of other weirdness and edge cases for sure.
- Only the `:component-will-unmount` lifecycle hook is currently wired up. Other Reagent lifecycle keys are not yet implemented yet (but `:ref` is).
- [It's slop code](#use-of-ai).

Some things to watch out for when using Squint, which I ran into building this:

- Keywords are just strings. No `keyword?`, `name` etc.
- No `binding`.
- No `sorted-hashmap`.

## Dev

```shell
pnpm i
npm run dev
```

## Tests

```shell
npm run test
```

## Build

```shell
npm run build
```

## Use of AI

Eucalypt is a fork of [Mr Clean](https://bitbucket.org/sonwh98/mr-clean/) that has been slop-coded with AI into compiling with Squint.

Slop-coded means this library was built by:

1. Creating failing test cases.
2. Using various LLMs to fix the code until all tests pass.

If you're uncomfortable with this method of development, which involves less human oversight, then you may want to be careful when using this library.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.

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


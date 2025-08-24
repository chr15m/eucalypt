# Eucalypt [WIP, slop, not for prod]

[Reagent](https://reagent-project.github.io/) compatible-ish [Squint](https://github.com/squint-cljs/squint)-ClojureScript library without React.

Goal: create ClojureScript frontend UIs with a Reagent-like API that compile down to sub-10k JS artifacts using [squint-cljs](https://github.com/squint-cljs/squint).

Eucalypt is a fork of [Mr Clean](https://bitbucket.org/sonwh98/mr-clean/) that has been slop-coded into working with Squint.

### Mr Clean (original README)

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


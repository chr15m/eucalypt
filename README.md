### Mr Clean

Mr Clean is a Reagent compatible ClojureScript library without dependency on React.js. At the heart of React.js
is a very simple idea GUI = function(data). Reagent takes this idea even further hiccup = function(data)

There is no diffing of virtual DOM. The state of components is flushed to the DOM when data changes.

### Install

Add the following dependency to your `project.clj` file:

    [stigmergy/mr-clean "0.1.0-SNAPSHOT"]

### What works?

Ractive atoms and cursors work as expected. 

### What doesn't work?

r/reaction is not a macro but a function that returns an atom holding the result of evaluating of a function.

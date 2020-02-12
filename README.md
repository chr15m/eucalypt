### Mr Clean

Mr Clean is a Reagent compatible ClojureScript library without dependency on React.js. At the heart of React.js
is a very simple idea GUI = function(data). Reagent takes this idea even further hiccup = function(data).  The
advantage of using Mr Clean over Reagent is that the generated javascript of a hello-world app is half
the size of that of reagent.

There is no diffing of virtual DOM. The state of components is flushed to the DOM when data changes. 

### Install

Add the following dependency to your `project.clj` file:

    [stigmergy/mr-clean "0.1.0-SNAPSHOT"]

### What works?

Ractive atoms and cursors work as expected which are the important parts making components reactive to data changes. 
form 1, form 2 components are fully supported.

### What doesn't work?

None of the reagent macros are implemented. r/reaction is not a macro but a function that returns an atom holding the result of evaluating a function.

form 3 components are not fully supported because not all lifecycle methods are implemented.


### Examples
```Clojure
(require '[stigmergy.mr-clean :as r])

(defn timer-component []
  (let [seconds-elapsed (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-elapsed inc) 1000)
      [:div
       "Seconds Elapsed: " @seconds-elapsed])))

(r/render [timer-component] (js/document.getElementById "app"))
```

See comments in [dev.cljs](https://bitbucket.org/sonwh98/mr-clean/src/master/src/cljs/stigmergy/dev.cljs) for more examples.

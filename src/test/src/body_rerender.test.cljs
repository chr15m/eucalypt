(ns body-rerender.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Re-rendering into document.body"
  (fn []
    (it "should correctly update the DOM when rendering over an existing component"
      (fn []
        (letfn [(component-a [] [:div "Component A"])
                (component-b [] [:p "Component B"])]
          ;; First render, should work
          (r/render [component-a] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>Component A</div>")

          ;; Second render, which triggers the bug
          (r/render [component-b] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<p>Component B</p>"))))))

(ns empty-fragment.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(def show-p (r/atom true))

(defn fragment-component []
  [:<>
   [:h1 "Fragment Test"]
   (when @show-p
     [:p "I am here."])])

(describe "Fragment Root Component"
  (fn []
    (it "should re-render correctly when a child is removed"
      (fn []
        (reset! show-p true)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [fragment-component] container)

          (th/assert-equal (.-innerHTML container) "<h1>Fragment Test</h1><p>I am here.</p>")

          (reset! show-p false)

          (th/assert-equal (.-innerHTML container) "<h1>Fragment Test</h1>"))))))

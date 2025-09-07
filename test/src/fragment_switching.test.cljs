(ns fragment-switching.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(def app-state (r/ratom {:page :simple}))

(defn simple-page []
  [:p "Simple Page"])

(defn fragments-page []
  [:<>
   [:div {:id "fragment-div"} "This is from the fragment"]
   (for [i (range 3)]
     ^{:key i} [:section "Section " i])])

(defn main-component []
  [:div
   (case (:page @app-state)
     :simple [simple-page]
     :fragments [fragments-page])])

(describe "Fragment Switching Bug"
  (fn []
    (it "should remove fragment content when switching away"
      (fn []
        (reset! app-state {:page :simple})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [main-component] container)

          ;; Should be on simple page
          (th/assert-equal (.-innerHTML container) "<div><p>Simple Page</p></div>")

          ;; Switch to fragments page
          (reset! app-state {:page :fragments})

          ;; Check fragments are rendered
          (th/assert-equal (.-length (.querySelectorAll container "section")) 3)
          (th/assert-equal (.-textContent (.querySelector container "#fragment-div")) "This is from the fragment")

          ;; Switch back to simple page
          (reset! app-state {:page :simple})

          ;; Check that fragments are gone. This is expected to fail.
          (th/assert-equal (.-innerHTML container) "<div><p>Simple Page</p></div>")
          (th/assert-equal (.-length (.querySelectorAll container "section")) 0)))))) 

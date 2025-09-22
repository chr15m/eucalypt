(ns various-renderings.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(defn render-and-get-html [hiccup]
  (let [container (.createElement js/document "div")]
    (.appendChild js/document.body container)
    (r/render hiccup container)
    (.-innerHTML container)))

(describe "Various value rendering"
  (fn []
    (it "should not render nil, true, or false"
      (fn []
        (th/assert-equal
          (render-and-get-html [:div "a" nil "b" true "c" false "d"])
          "<div>abcd</div>")))

    (it "should render numbers as text"
      (fn []
        (th/assert-equal
          (render-and-get-html [:div 1 2 3])
          "<div>123</div>")))

    (it "should merge adjacent strings"
      (fn []
        (th/assert-equal
          (render-and-get-html [:div "a" "b" "c"])
          "<div>abc</div>")))

    (it "should merge adjacent strings and numbers"
      (fn []
        (th/assert-equal
          (render-and-get-html [:div "a" 1 "b" 2])
          "<div>a1b2</div>")))

    (it "should handle empty strings"
      (fn []
        (th/assert-equal
          (render-and-get-html [:div "a" "" "b"])
          "<div>ab</div>")))

    (it "should handle a complex mix of values"
      (fn []
        (th/assert-equal
          (render-and-get-html [:div {} "a" nil "b" true 1 false "c" 2 "d"])
          "<div>ab1c2d</div>")))))

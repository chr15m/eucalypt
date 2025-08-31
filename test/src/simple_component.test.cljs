(ns simple-component.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(defn simple-component []
  [:div
   [:p {:id "p1"} "I am a component!"]
   [:p {:class "someclass"}
    "I have " [:strong "bold"]
    [:span {:class "text-red"} " and red "] "text."]])

(describe "Simple Component"
  (fn []
    (it "should render correctly"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [simple-component] container)

          (let [p1 (.querySelector container "#p1")]
            (th/assert-not-nil p1)
            (th/assert-equal (.-textContent p1) "I am a component!"))

          (let [strong (.querySelector container "strong")]
            (th/assert-not-nil strong)
            (th/assert-equal (.-textContent strong) "bold"))

          (th/assert-equal (.-innerHTML container) "<div><p id=\"p1\">I am a component!</p><p class=\"someclass\">I have <strong>bold</strong><span class=\"text-red\"> and red </span>text.</p></div>"))))))

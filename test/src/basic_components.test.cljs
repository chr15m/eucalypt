(ns basic-components.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

;;; Simple Component
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

;;; Simple Parent
(defn simple-parent []
  [:div
   [:h1 "I am the parent"]
   [simple-component]])

(describe "Simple Parent"
  (fn []
    (it "should render with a child component"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [simple-parent] container)
          (th/assert-equal (.-innerHTML container) "<div><h1>I am the parent</h1><div><p id=\"p1\">I am a component!</p><p class=\"someclass\">I have <strong>bold</strong><span class=\"text-red\"> and red </span>text.</p></div></div>"))))))

;;; Hello Component
(defn hello-component [username]
  [:p "Hello, " username "!"])

(describe "Hello Component"
  (fn []
    (it "should say hello"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [hello-component "World"] container)
          (th/assert-equal (.-innerHTML container) "<p>Hello, World!</p>"))))))

;;; Lister
(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li item])])

(describe "Lister"
  (fn []
    (it "should render a list"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [lister ["one" "two" "three"]] container)
          (th/assert-equal (.-innerHTML container) "<ul><li>one</li><li>two</li><li>three</li></ul>"))))))

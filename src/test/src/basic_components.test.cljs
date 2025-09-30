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

;;; Class attribute handling
(describe "Class attribute handling"
  (fn []
    (it "should handle a vector of strings"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class ["class1" "class2"]}] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2\"></div>"))))

    (it "should handle a vector of keywords"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class [:class1 :class2]}] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2\"></div>"))))

    (it "should handle a vector of mixed strings and keywords"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class ["class1" :class2]}] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2\"></div>"))))

    (it "should handle a vector with nil values"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class ["class1" nil "class2"]}] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2\"></div>"))))

    (it "should handle an empty vector"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class []}] container)
          (th/assert-equal (.-innerHTML container) "<div></div>"))))

    (it "should handle a vector with only nil values"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class [nil nil]}] container)
          (th/assert-equal (.-innerHTML container) "<div></div>"))))))

;;; Hiccup tag decorators
(describe "Hiccup tag decorators"
  (fn []
    (it "should handle a single class decorator"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div.my-class] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"my-class\"></div>"))))

    (it "should handle multiple class decorators"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div.class1.class2] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2\"></div>"))))

    (it "should handle an id decorator"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div#my-id] container)
          (th/assert-equal (.-innerHTML container) "<div id=\"my-id\"></div>"))))

    (it "should handle both id and class decorators"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div#my-id.my-class] container)
          (th/assert-equal (.-innerHTML container) "<div id=\"my-id\" class=\"my-class\"></div>"))))

    (it "should merge class decorator with :class string attribute"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div.class1 {:class "class2"}] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2\"></div>"))))

    (it "should merge class decorator with :class vector attribute"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div.class1 {:class ["class2" "class3"]}] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"class1 class2 class3\"></div>"))))

    (it "should give :id attribute precedence over id decorator"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div#id-from-tag {:id "id-from-attr"}] container)
          (th/assert-equal (.-innerHTML container) "<div id=\"id-from-attr\"></div>"))))

    (it "should handle complex precedence"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div#id-from-tag.class-from-tag {:id "id-from-attr" :class "class-from-attr"}] container)
          (th/assert-equal (.-innerHTML container) "<div id=\"id-from-attr\" class=\"class-from-tag class-from-attr\"></div>"))))

    (it "should default to div for class-only decorator"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:.my-class] container)
          (th/assert-equal (.-innerHTML container) "<div class=\"my-class\"></div>"))))))

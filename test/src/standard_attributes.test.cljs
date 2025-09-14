(ns standard-attributes.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def attribute-state (r/ratom {:show-class? false
                               :show-title? false
                               :show-data-attr? false}))

(defn attribute-test-component []
  [:div
   [:button {:id "toggle-class"
             :on-click #(swap! attribute-state update :show-class? not)}
    "Toggle Class"]
   [:button {:id "toggle-title"
             :on-click #(swap! attribute-state update :show-title? not)}
    "Toggle Title"]
   [:button {:id "toggle-data-attr"
             :on-click #(swap! attribute-state update :show-data-attr? not)}
    "Toggle Data Attr"]

   ;; Test element with conditional class attribute
   [:div {:id "test-class"
          :class (when (:show-class? @attribute-state) "active")}
    "Class Test"]

   ;; Test element with conditional title attribute
   [:div {:id "test-title"
          :title (when (:show-title? @attribute-state) "This is a tooltip")}
    "Title Test"]

   ;; Test element with conditional data attribute
   [:div {:id "test-data"
          :data-test (when (:show-data-attr? @attribute-state) "test-value")}
    "Data Attr Test"]])

(describe "Standard Attribute Handling"
  (fn []
    (it "should handle class attribute with nil/null values correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [attribute-test-component] container)

          (let [test-element (.querySelector container "#test-class")
                toggle-btn (.querySelector container "#toggle-class")]

            ;; Initially, class should not be set (when returns nil)
            (th/assert-equal (.hasAttribute test-element "class") false)
            (th/assert-equal (.-className test-element) "")

            ;; After clicking, class should be "active"
            (.click toggle-btn)
            (th/assert-equal (.hasAttribute test-element "class") true)
            (th/assert-equal (.-className test-element) "active")

            ;; After clicking again, class should be removed
            (.click toggle-btn)
            (th/assert-equal (.hasAttribute test-element "class") false)
            (th/assert-equal (.-className test-element) "")))))

    (it "should handle title attribute with nil/null values correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [attribute-test-component] container)

          (let [test-element (.querySelector container "#test-title")
                toggle-btn (.querySelector container "#toggle-title")]

            ;; Initially, title should not be set
            (th/assert-equal (.hasAttribute test-element "title") false)

            ;; After clicking, title should be set
            (.click toggle-btn)
            (th/assert-equal (.hasAttribute test-element "title") true)
            (th/assert-equal (.getAttribute test-element "title") "This is a tooltip")

            ;; After clicking again, title should be removed
            (.click toggle-btn)
            (th/assert-equal (.hasAttribute test-element "title") false)))))

    (it "should handle data attributes with nil/null values correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [attribute-test-component] container)

          (let [test-element (.querySelector container "#test-data")
                toggle-btn (.querySelector container "#toggle-data-attr")]

            ;; Initially, data-test should not be set
            (th/assert-equal (.hasAttribute test-element "data-test") false)

            ;; After clicking, data-test should be set
            (.click toggle-btn)
            (th/assert-equal (.hasAttribute test-element "data-test") true)
            (th/assert-equal (.getAttribute test-element "data-test") "test-value")

            ;; After clicking again, data-test should be removed
            (.click toggle-btn)
            (th/assert-equal (.hasAttribute test-element "data-test") false)))))))

(ns attribute-handling.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def attribute-state (r/ratom {:show-class? false
                               :show-title? false
                               :show-data-attr? false
                               :disabled? false
                               :checked? false
                               :selected-value nil
                               :style-color nil}))

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
   [:button {:id "toggle-disabled"
             :on-click #(swap! attribute-state update :disabled? not)}
    "Toggle Disabled"]
   [:button {:id "toggle-checked"
             :on-click #(swap! attribute-state update :checked? not)}
    "Toggle Checked"]
   [:button {:id "set-selected"
             :on-click #(swap! attribute-state assoc :selected-value "option2")}
    "Set Selected"]
   [:button {:id "clear-selected"
             :on-click #(swap! attribute-state assoc :selected-value nil)}
    "Clear Selected"]
   [:button {:id "set-style"
             :on-click #(swap! attribute-state assoc :style-color "red")}
    "Set Style"]
   [:button {:id "clear-style"
             :on-click #(swap! attribute-state assoc :style-color nil)}
    "Clear Style"]

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
    "Data Attr Test"]

   ;; Test input with conditional disabled
   [:input {:id "test-disabled"
            :type "text"
            :disabled (when (:disabled? @attribute-state) true)}]

   ;; Test checkbox with conditional checked
   [:input {:id "test-checked"
            :type "checkbox"
            :checked (when (:checked? @attribute-state) true)}]

   ;; Test select with conditional selected
   [:select {:id "test-select"}
    [:option {:value "option1"} "Option 1"]
    [:option {:value "option2"
              :selected (when (= (:selected-value @attribute-state) "option2") true)}
     "Option 2"]
    [:option {:value "option3"} "Option 3"]]

   ;; Test element with conditional style
   [:div {:id "test-style"
          :style {:color (when (:style-color @attribute-state) (:style-color @attribute-state))}}
    "Style Test"]])

(describe "Attribute Handling"
  (fn []
    (it "should handle class attribute with nil/null values correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false
                                 :disabled? false
                                 :checked? false
                                 :selected-value nil
                                 :style-color nil})
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
                                 :show-data-attr? false
                                 :disabled? false
                                 :checked? false
                                 :selected-value nil
                                 :style-color nil})
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
                                 :show-data-attr? false
                                 :disabled? false
                                 :checked? false
                                 :selected-value nil
                                 :style-color nil})
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
            (th/assert-equal (.hasAttribute test-element "data-test") false)))))

    (it "should handle boolean attributes (disabled, checked) correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false
                                 :disabled? false
                                 :checked? false
                                 :selected-value nil
                                 :style-color nil})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [attribute-test-component] container)

          (let [disabled-input (.querySelector container "#test-disabled")
                checked-input (.querySelector container "#test-checked")
                toggle-disabled-btn (.querySelector container "#toggle-disabled")
                toggle-checked-btn (.querySelector container "#toggle-checked")]

            ;; Initially, inputs should not be disabled/checked
            (th/assert-equal (.-disabled disabled-input) false)
            (th/assert-equal (.-checked checked-input) false)

            ;; After clicking, inputs should be disabled/checked
            (.click toggle-disabled-btn)
            (.click toggle-checked-btn)
            (th/assert-equal (.-disabled disabled-input) true)
            (th/assert-equal (.-checked checked-input) true)

            ;; After clicking again, inputs should not be disabled/checked
            (.click toggle-disabled-btn)
            (.click toggle-checked-btn)
            (th/assert-equal (.-disabled disabled-input) false)
            (th/assert-equal (.-checked checked-input) false)))))

    (it "should handle selected attribute correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false
                                 :disabled? false
                                 :checked? false
                                 :selected-value nil
                                 :style-color nil})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [attribute-test-component] container)

          (let [select-element (.querySelector container "#test-select")
                option2 (.querySelector select-element "option[value='option2']")
                set-btn (.querySelector container "#set-selected")
                clear-btn (.querySelector container "#clear-selected")]

            ;; Initially, option2 should not be selected
            (th/assert-equal (.-selected option2) false)

            ;; After clicking set, option2 should be selected
            (.click set-btn)
            (th/assert-equal (.-selected option2) true)

            ;; After clicking clear, option2 should not be selected
            (.click clear-btn)
            (th/assert-equal (.-selected option2) false)))))

    (it "should handle style attributes with nil/null values correctly"
      (fn []
        (reset! attribute-state {:show-class? false
                                 :show-title? false
                                 :show-data-attr? false
                                 :disabled? false
                                 :checked? false
                                 :selected-value nil
                                 :style-color nil})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [attribute-test-component] container)

          (let [test-element (.querySelector container "#test-style")
                set-btn (.querySelector container "#set-style")
                clear-btn (.querySelector container "#clear-style")]

            ;; Initially, color style should not be set
            (th/assert-equal (.. test-element -style -color) "")

            ;; After clicking set, color should be red
            (.click set-btn)
            (th/assert-equal (.. test-element -style -color) "red")

            ;; After clicking clear, color should be empty again
            (.click clear-btn)
            (th/assert-equal (.. test-element -style -color) "")))))))

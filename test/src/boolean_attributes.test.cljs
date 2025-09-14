(ns boolean-attributes.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def attribute-state (r/ratom {:disabled? false
                               :checked? false}))

(defn attribute-test-component []
  [:div
   [:button {:id "toggle-disabled"
             :on-click #(swap! attribute-state update :disabled? not)}
    "Toggle Disabled"]
   [:button {:id "toggle-checked"
             :on-click #(swap! attribute-state update :checked? not)}
    "Toggle Checked"]

   ;; Test input with conditional disabled
   [:input {:id "test-disabled"
            :type "text"
            :disabled (when (:disabled? @attribute-state) true)}]

   ;; Test checkbox with conditional checked
   [:input {:id "test-checked"
            :type "checkbox"
            :checked (when (:checked? @attribute-state) true)}]])

(describe "Boolean Attribute Handling"
  (fn []
    (it "should handle boolean attributes (disabled, checked) correctly"
      (fn []
        (reset! attribute-state {:disabled? false
                                 :checked? false})
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
            (th/assert-equal (.-checked checked-input) false)))))))

(ns select-attribute.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def attribute-state (r/ratom {:selected-value nil}))

(defn attribute-test-component []
  [:div
   [:button {:id "set-selected"
             :on-click #(swap! attribute-state assoc :selected-value "option2")}
    "Set Selected"]
   [:button {:id "clear-selected"
             :on-click #(swap! attribute-state assoc :selected-value nil)}
    "Clear Selected"]

   ;; Test select with conditional selected
   [:select {:id "test-select"}
    [:option {:value "option1"} "Option 1"]
    [:option {:value "option2"
              :selected (when (= (:selected-value @attribute-state) "option2") true)}
     "Option 2"]
    [:option {:value "option3"} "Option 3"]]])

(describe "Selected Attribute Handling"
  (fn []
    (it "should handle selected attribute correctly"
      (fn []
        (reset! attribute-state {:selected-value nil})
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
            (th/assert-equal (.-selected option2) false)))))))

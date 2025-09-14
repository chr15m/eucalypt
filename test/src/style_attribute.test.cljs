(ns style-attribute.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def attribute-state (r/ratom {:style-color nil}))

(defn attribute-test-component []
  [:div
   [:button {:id "set-style"
             :on-click #(swap! attribute-state assoc :style-color "red")}
    "Set Style"]
   [:button {:id "clear-style"
             :on-click #(swap! attribute-state assoc :style-color nil)}
    "Clear Style"]

   ;; Test element with conditional style
   [:div {:id "test-style"
          :style {:color (when (:style-color @attribute-state) (:style-color @attribute-state))}}
    "Style Test"]])

(describe "Style Attribute Handling"
  (fn []
    (it "should handle style attributes with nil/null values correctly"
      (fn []
        (reset! attribute-state {:style-color nil})
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

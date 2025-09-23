(ns numeric-input.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def numeric-state (r/atom {:value 10}))

(defn numeric-input-page []
  [:div
   [:h2 "Numeric Input Test"]
   [:input {:type "number"
            :id "numeric-input"
            :value (:value @numeric-state)
            :on-input (fn [e]
                        (swap! numeric-state assoc :value (js/parseInt (.. e -target -value) 10)))}]
   [:p {:id "output"} "Value: " (:value @numeric-state)]
   [:button {:id "set-to-42" :on-click #(reset! numeric-state {:value 42})} "Set to 42"]])

(describe "Numeric Input Component"
  (fn []
    (it "should update on input and be controllable"
      (fn []
        (reset! numeric-state {:value 10})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [numeric-input-page] container)

          (let [input-el (.querySelector container "#numeric-input")
                output (.querySelector container "#output")
                button (.querySelector container "#set-to-42")]

            (th/assert-equal (.-value input-el) "10")
            (th/assert-equal (.-textContent output) "Value: 10")

            ;; Simulate user changing value
            (set! (.-value input-el) "25")
            (.dispatchEvent input-el (new js/Event "input" #js {:bubbles true}))

            (th/assert-equal (.-value input-el) "25")
            (th/assert-equal (.-textContent output) "Value: 25")
            (th/assert-equal (:value @numeric-state) 25)

            ;; Control component by clicking button
            (.click button)
            (th/assert-equal (.-value input-el) "42")
            (th/assert-equal (.-textContent output) "Value: 42")
            (th/assert-equal (:value @numeric-state) 42)))))))

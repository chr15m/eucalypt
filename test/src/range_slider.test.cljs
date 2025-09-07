(ns range-slider.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def slider-state (r/ratom {:value 50}))

(defn range-slider-page []
  [:div
   [:h2 "Range Slider Test"]
   [:input {:type "range"
            :id "range-slider"
            :min "0"
            :max "100"
            :value (:value @slider-state)
            :on-input (fn [e]
                        (swap! slider-state assoc :value (js/parseInt (.. e -target -value) 10)))}]
   [:p {:id "output"} "Value: " (:value @slider-state)]
   [:button {:id "set-to-75" :on-click #(swap! slider-state assoc :value 75)} "Set to 75"]])

(describe "Range Slider Component"
  (fn []
    (it "should update on input and be controllable"
      (fn []
        (reset! slider-state {:value 50})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [range-slider-page] container)

          (let [slider-el (.querySelector container "#range-slider")
                output (.querySelector container "#output")
                button (.querySelector container "#set-to-75")]

            (th/assert-equal (.-value slider-el) "50")
            (th/assert-equal (.-textContent output) "Value: 50")

            ;; Simulate user changing slider
            (set! (.-value slider-el) "25")
            (.dispatchEvent slider-el (new js/Event "input" #js {:bubbles true}))

            (th/assert-equal (.-value slider-el) "25")
            (th/assert-equal (.-textContent output) "Value: 25")
            (th/assert-equal (:value @slider-state) 25)

            ;; Control component by clicking button
            (.click button)
            (th/assert-equal (.-value slider-el) "75")
            (th/assert-equal (.-textContent output) "Value: 75")
            (th/assert-equal (:value @slider-state) 75))))))) 

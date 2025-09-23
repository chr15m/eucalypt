(ns checkbox.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def checkbox-state (r/atom {:checked? true}))

(defn checkbox-page []
  [:div
   [:h2 "Checkbox Test"]
   [:label
    [:input {:type "checkbox"
             :id "checkbox-input"
             :checked (:checked? @checkbox-state)
             :on-change (fn [e]
                          (swap! checkbox-state assoc :checked? (.. e -target -checked)))}]
    " Is checked?"]
   [:p {:id "output"} "Checked: " (str (:checked? @checkbox-state))]
   [:button {:id "toggle-checked" :on-click #(swap! checkbox-state update :checked? not)} "Toggle"]])

(describe "Checkbox Component"
  (fn []
    (it "should update on change and be controllable"
      (fn []
        (reset! checkbox-state {:checked? true})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [checkbox-page] container)

          (let [checkbox-el (.querySelector container "#checkbox-input")
                output (.querySelector container "#output")
                button (.querySelector container "#toggle-checked")]

            (th/assert-equal (.-checked checkbox-el) true)
            (th/assert-equal (.-textContent output) "Checked: true")

            ;; Simulate user unchecking
            (.click checkbox-el)

            (th/assert-equal (.-checked checkbox-el) false)
            (th/assert-equal (.-textContent output) "Checked: false")
            (th/assert-equal (:checked? @checkbox-state) false)

            ;; Control component by clicking button
            (.click button)
            (th/assert-equal (.-checked checkbox-el) true)
            (th/assert-equal (.-textContent output) "Checked: true")
            (th/assert-equal (:checked? @checkbox-state) true))))))) 

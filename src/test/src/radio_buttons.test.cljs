(ns radio-buttons.test
  (:require ["vitest" :refer [describe it afterEach beforeEach]]
            [eucalypt :as r]
            [helpers :as th]))

(beforeEach
 (fn []
   #_ (.setItem js/localStorage "debug" "eucalypt:*")))

(afterEach
  (fn []
    #_ (.removeItem js/localStorage "debug")
    (set! (.-innerHTML js/document.body) "")))

(def radio-state (r/atom {:selected "b"}))

(defn radio-button-page []
  [:div
   [:h2 "Radio Button Test"]
   [:fieldset
    [:legend "Choose an option"]
    [:div
     [:input {:type "radio" :id "radio-a" :name "option" :value "a"
              :checked (= "a" (:selected @radio-state))
              :on-change #(swap! radio-state assoc :selected "a")}]
     [:label {:for "radio-a"} "Option A"]]
    [:div
     [:input {:type "radio" :id "radio-b" :name "option" :value "b"
              :checked (= "b" (:selected @radio-state))
              :on-change #(swap! radio-state assoc :selected "b")}]
     [:label {:for "radio-b"} "Option B"]]
    [:div
     [:input {:type "radio" :id "radio-c" :name "option" :value "c"
              :checked (= "c" (:selected @radio-state))
              :on-change #(swap! radio-state assoc :selected "c")}]
     [:label {:for "radio-c"} "Option C"]]]
   [:p {:id "output"} "Selected: " (:selected @radio-state)]
   [:button {:id "select-c" :on-click #(reset! radio-state {:selected "c"})} "Select C"]])

(describe "Radio Button Component"
  (fn []
    (it "should update on change and be controllable"
      (fn []
        (reset! radio-state {:selected "b"})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [radio-button-page] container)

          (let [radio-a (.querySelector container "#radio-a")
                radio-b (.querySelector container "#radio-b")
                output (.querySelector container "#output")
                button (.querySelector container "#select-c")]

            (th/assert-equal (.-checked radio-a) false)
            (th/assert-equal (.-checked radio-b) true)
            (th/assert-equal (.-textContent output) "Selected: b")

            ;; Simulate user clicking radio A
            (.click radio-a)

            (th/assert-equal (.-checked radio-a) true)
            (th/assert-equal (.-checked radio-b) false)
            (th/assert-equal (.-textContent output) "Selected: a")
            (th/assert-equal (:selected @radio-state) "a")

            ;; Control component by clicking button
            (.click button)
            (let [radio-c (.querySelector container "#radio-c")]
              (th/assert-equal (.-checked radio-a) false)
              (th/assert-equal (.-checked radio-c) true)
              (th/assert-equal (.-textContent output) "Selected: c")
              (th/assert-equal (:selected @radio-state) "c"))))))))

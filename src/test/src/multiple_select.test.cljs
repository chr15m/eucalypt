(ns multiple-select.test
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

(def select-state (r/atom {:selected ["b"]}))

(defn handle-change [e]
  (let [opts (.. e -target -selectedOptions)
        selected-values (->> opts
                             (js/Array.from)
                             (map #(.-value %))
                             vec)]
    (swap! select-state assoc :selected selected-values)))

(defn multiple-select-page []
  [:div
   [:h2 "Multiple Select Test"]
   [:select {:id "select-input"
             :multiple true
             :value (clj->js (:selected @select-state))
             :on-change handle-change}
    [:option {:value "a"} "Option A"]
    [:option {:value "b"} "Option B"]
    [:option {:value "c"} "Option C"]]
   [:p {:id "output"} "You selected: " (.join (clj->js (:selected @select-state)) ",")]
   [:button {:id "change-selection" :on-click #(reset! select-state {:selected ["a" "c"]})} "Set to A & C"]])

(describe "Multiple Select Component"
          (fn []
            (it "should update on change and be controllable"
                (fn []
                  (reset! select-state {:selected ["b"]})
                  (let [container (.createElement js/document "div")]
                    (.appendChild js/document.body container)
                    (r/render [multiple-select-page] container)

                    (let [select-el (.querySelector container "#select-input")
                          output (.querySelector container "#output")
                          button (.querySelector container "#change-selection")
                          option-a (-> select-el (.querySelector "[value=a]"))
                          option-b (-> select-el (.querySelector "[value=b]"))
                          option-c (-> select-el (.querySelector "[value=c]"))]

                      (th/assert-equal (.-selected option-a) false)
                      (th/assert-equal (.-selected option-b) true)
                      (th/assert-equal (.-selected option-c) false)
                      (th/assert-equal (.-textContent output) "You selected: b")

                      ;; Update selected options
                      (set! (.-selected option-a) true)
                      (set! (.-selected option-b) false)
                      (set! (.-selected option-c) true)

                      (th/fire-event select-el "change")

                      (-> (th/wait-for-render)
                          (.then (fn []
                                   (th/assert-equal (.-selected option-a) true)
                                   (th/assert-equal (.-selected option-b) false)
                                   (th/assert-equal (.-selected option-c) true)
                                   (th/assert-equal (.-textContent output) "You selected: a,c")
                                   (th/assert-equal (= (:selected @select-state) ["a" "c"]) true)

                                   ;; Control component by clicking button
                                   (.click button)
                                   (th/wait-for-render)))
                          (.then (fn []
                                   (th/assert-equal (.-selected option-a) true)
                                   (th/assert-equal (.-selected option-b) false)
                                   (th/assert-equal (.-selected option-c) true)
                                   (th/assert-equal (.-textContent output) "You selected: a,c")
                                   (th/assert-equal (= (:selected @select-state) ["a" "c"]) true))))))))))

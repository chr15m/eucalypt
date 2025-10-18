(ns select.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def select-state (r/atom {:selected "b"}))

(defn select-page []
  [:div
   [:h2 "Select Test"]
   [:select {:id "select-input"
             :value (:selected @select-state)
             :on-change (fn [e]
                          (swap! select-state assoc :selected (.. e -target -value)))}
    [:option {:value "a"} "Option A"]
    [:option {:value "b"} "Option B"]
    [:option {:value "c"} "Option C"]]
   [:p {:id "output"} "You selected: " (:selected @select-state)]
   [:button {:id "change-to-a" :on-click #(swap! select-state assoc :selected "a")} "Set to A"]])

(describe "Select Component"
  (fn []
    (it "should set value from :value attribute"
      (fn []
        (let [container (.createElement js/document "div")
              component [:select {:value "b"}
                         [:option {:value "a"} "A"]
                         [:option {:value "b"} "B"]
                         [:option {:value "c"} "C"]]]
          (.appendChild js/document.body container)
          (r/render component container)
          (let [select-el (.querySelector container "select")]
            (th/assert-not-nil select-el)
            (th/assert-equal (.-value select-el) "b")))))

    (it "should honor :selected attribute when no :value is provided"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render
            [:select
             [:option {:value "a"} "A"]
             [:option {:value "b" :selected true} "B"]
             [:option {:value "c"} "C"]]
            container)
          (let [select-el (.querySelector container "select")
                options (.querySelectorAll select-el "option")
                option-a (aget options 0)
                option-b (aget options 1)
                option-c (aget options 2)]
            (th/assert-equal (.-value select-el) "b")
            (th/assert-equal (.-selected option-a) false)
            (th/assert-equal (.-selected option-b) true)
            (th/assert-equal (.-selected option-c) false)))))

    (it "should update on change and be controllable"
      (fn []
        (reset! select-state {:selected "b"})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [select-page] container)

          (let [select-el (.querySelector container "#select-input")
                output (.querySelector container "#output")
                button (.querySelector container "#change-to-a")]

            (th/assert-equal (.-value select-el) "b")
            (th/assert-equal (.-textContent output) "You selected: b")

            ;; Simulate user changing selection
            (set! (.-value select-el) "c")
            (.dispatchEvent select-el (new js/Event "change" #js {:bubbles true}))

            (th/assert-equal (.-value select-el) "c")
            (th/assert-equal (.-textContent output) "You selected: c")
            (th/assert-equal (:selected @select-state) "c")

            ;; Control component by clicking button
            (.click button)
            (th/assert-equal (.-value select-el) "a")
            (th/assert-equal (.-textContent output) "You selected: a")
            (th/assert-equal (:selected @select-state) "a")))))))

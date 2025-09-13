(ns multiple-instances.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(defn counter-component [_id]
  (let [counter (r/ratom 0)]
    (fn [id]
      [:div
       [:p "Counter " id ": " @counter]
       [:button {:id (str "btn-" id)
                 :on-click #(swap! counter inc)} "Increment"]])))

(defn main-page []
  [:div
   [counter-component "A"]
   [counter-component "B"]])

(describe "Multiple component instances"
  (fn []
    (it "should have independent state"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [main-page] container)

          (let [btn-a (.querySelector container "#btn-A")
                all-ps (.querySelectorAll container "p")
                p-a (aget all-ps 0)
                p-b (aget all-ps 1)]

            (th/assert-equal (.-textContent p-a) "Counter A: 0")
            (th/assert-equal (.-textContent p-b) "Counter B: 0")

            (.click btn-a)

            (th/assert-equal (.-textContent p-a) "Counter A: 1")
            ;; This assertion will fail because both components share state
            (th/assert-equal (.-textContent p-b) "Counter B: 0")))))))

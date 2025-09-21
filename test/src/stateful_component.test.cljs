(ns stateful-component.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def app-state (r/atom {:counter 0}))

(defn counter-component []
  [:div
   [:p "The current count is: " (:counter @app-state)]
   [:button {:id "increment"
             :on-click (fn [_] (swap! app-state update :counter inc))}
    "Increment counter"]])

(describe "Stateful Component"
  (fn []
    (it "should update when ratom changes"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [counter-component] container)

          (th/assert-equal (.-textContent (.querySelector container "p")) "The current count is: 0")

          (let [button (.querySelector container "#increment")]
            (.click button))

          (th/assert-equal (.-textContent (.querySelector container "p")) "The current count is: 1"))))))

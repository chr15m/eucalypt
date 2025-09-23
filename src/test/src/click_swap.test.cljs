(ns click-swap.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def state (r/atom {:items [1 nil 3]}))

(defn handle-click []
  (swap! state assoc-in [:items 1] 2))

(defn test-component []
  [:div
   [:button {:on-click handle-click} "Click Me"]
   [:div
    (for [item (:items @state)]
      (when item
        [:p (str "Item " item)]))]])

(describe "Click Swap bug"
  (fn []
    (it "should not crash when swapping item"
      (fn []
        (reset! state {:items [1 nil 3]})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [test-component] container)

          (let [button (.querySelector container "button")]
            (.click button)
            ;; The test passes if it doesn't crash.
            (let [ps (.querySelectorAll container "p")]
              (th/assert-equal (.-length ps) 3)
              (th/assert-equal (.-textContent (aget ps 1)) "Item 2"))))))))

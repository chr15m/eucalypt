(ns click-swap.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def state (r/atom nil))

(defn test-component []
  [:div
   [:button {:on-click #(swap! state update :counter (fnil inc 0))} "inc"]
   [:p (pr-str @state)]])

(describe "Click Swap with nil atom"
  (fn []
    (it "should update nil atom correctly"
      (fn []
        (reset! state nil)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [test-component] container)

          (let [button (.querySelector container "button")]
            (.click button)
            (th/assert-equal @state {:counter 1})
            (let [p (.querySelector container "p")]
              (th/assert-equal (.-textContent p) "{\"counter\":1}"))))))))

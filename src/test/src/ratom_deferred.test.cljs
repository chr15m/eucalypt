(ns ratom-deferred.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def fail-tick-count (r/atom 0))
(def app-state (r/atom {:value 0}))

(defn fail-tick [atm]
  (swap! fail-tick-count inc)
  (swap! atm update :fail-tick (fnil inc 0)))

(defn fail-case []
  (let [interval-id (r/atom nil)
        _ (fail-tick interval-id)]
    (fn []
      [:div
       [:p "Mode A"]])))

(defn app []
  [:div
   [:span "Value: " (:value @app-state)]
   [fail-case]])

(describe "Deferred ratom initialization"
  (fn []
    (it "should only initialize local ratom once when parent re-renders"
      (fn []
        (reset! fail-tick-count 0)
        (reset! app-state {:value 0})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [app] container)

          (th/assert-equal @fail-tick-count 1
                           "Initial mount should initialize local ratom once")

          (swap! app-state update :value inc)

          (th/assert-equal @fail-tick-count 1
                           "Parent re-render should not re-run local ratom
setup"))))))

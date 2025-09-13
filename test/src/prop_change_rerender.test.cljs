(ns prop-change-rerender.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

;; A simple child component that displays a message based on its props
(defn child-component [{:keys [done?]}]
  [:p {:class (when done? "completed")}
   (if done? "Task is done" "Task is not done")])

;; A parent component that controls the state
(def app-state (r/ratom {:done? false}))

(defn parent-component []
  [:div
   [child-component @app-state]
   [:button {:id "toggle-done"
             :on-click #(swap! app-state update :done? not)}
    "Toggle Done"]])

(describe "Component re-render on prop change"
  (fn []
    (it "should update child component when props change"
      (fn []
        (reset! app-state {:done? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [parent-component] container)

          (let [p (.querySelector container "p")
                button (.querySelector container "#toggle-done")]

            ;; Initial state
            (th/assert-equal (.-textContent p) "Task is not done")
            (th/assert-equal (.contains (.-classList p) "completed") false)

            ;; Click button to change state
            (.click button)

            ;; Assert DOM has updated
            (th/assert-equal (.-textContent p) "Task is done")
            (th/assert-equal (.contains (.-classList p) "completed") true)

            ;; Click again to toggle back
            (.click button)

            (th/assert-equal (.-textContent p) "Task is not done")
            (th/assert-equal (.contains (.-classList p) "completed") false))))))) 

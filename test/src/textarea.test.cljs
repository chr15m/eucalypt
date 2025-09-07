(ns textarea.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def textarea-state (r/ratom {:text "Initial text"}))

(defn textarea-page []
  [:div
   [:h2 "Textarea Test"]
   [:textarea {:id "textarea-input"
               :value (:text @textarea-state)
               :on-input (fn [e]
                           (swap! textarea-state assoc :text (.. e -target -value)))}]
   [:p {:id "output"} "Content: " (:text @textarea-state)]])

(describe "Textarea Component"
  (fn []
    (it "should update text as user types"
      (fn []
        (reset! textarea-state {:text "Initial text"})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [textarea-page] container)

          (let [textarea (.querySelector container "#textarea-input")
                output (.querySelector container "#output")]

            (th/assert-equal (.-value textarea) "Initial text")
            (th/assert-equal (.-textContent output) "Content: Initial text")

            ;; Simulate user typing
            (set! (.-value textarea) "New text")
            (.dispatchEvent textarea (new js/Event "input" #js {:bubbles true}))
            (th/assert-equal (.-textContent output) "Content: New text")
            (th/assert-equal (:text @textarea-state) "New text")))))))

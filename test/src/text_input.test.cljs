(ns text-input.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def text-state (r/ratom {:text ""}))

(defn text-input-page []
  [:div
   [:h2 "Text Input Test"]
   [:input {:type "text"
            :id "text-input"
            :value (:text @text-state)
            :on-change (fn [e]
                         (swap! text-state assoc :text (.. e -target -value)))}]
   [:p {:id "output"} "You typed: " (:text @text-state)]])

(describe "Text Input Component"
  (fn []
    (it "should update text as user types"
      (fn []
        (reset! text-state {:text ""})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [text-input-page] container)

          (let [input (.querySelector container "#text-input")
                output (.querySelector container "#output")]

            (th/assert-equal (.-textContent output) "You typed: ")

            ;; Type a few characters
            (set! (.-value input) "abc")
            (.dispatchEvent input (new js/Event "input" #js {:bubbles true}))
            (th/assert-equal (.-textContent output) "You typed: abc")

            ;; Type a few more
            (set! (.-value input) "abc def")
            (.dispatchEvent input (new js/Event "input" #js {:bubbles true}))
            (th/assert-equal (.-textContent output) "You typed: abc def")))))))

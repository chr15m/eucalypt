(ns text-input.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def text-state (r/atom {:text ""}))

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

            (th/fire-event input "change" {:target {:value "abc"}})
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (.-textContent output) "You typed: abc")

                         (th/fire-event input "change" {:target {:value "abc def"}})
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-textContent output) "You typed: abc def"))))))))))

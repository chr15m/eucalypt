(ns enter-to-submit.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]
            [clojure.string :as str]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def submitted-text (r/atom nil))

(defn enter-submit-input [{:keys [on-save]}]
  (js/console.log "enter-submit-input creating new state")
  (let [val (r/atom "")]
    (fn []
      (letfn [(save [_e]
                (js/console.log "save called. @val is:" @val)
                (let [v (-> @val str str/trim)]
                  (when-not (empty? v)
                    (js/console.log "on-save will be called with:" v)
                    (on-save v))
                  (reset! val "")))]
        [:input {:type "text"
                 :id "submit-input"
                 :value @val
                 :on-change (fn [e]
                              (let [new-v (.. e -target -value)]
                                (js/console.log "on-change called. new value:" new-v)
                                (reset! val new-v)))
                 :on-key-down (fn [e]
                                (js/console.log "on-key-down called. key code:" (.-code e))
                                (when (= (.-code e) "Enter")
                                  (save e)))}]))))

(defn test-page []
  (js/console.log "test-page rendering")
  [:div
   [enter-submit-input {:on-save (fn [v]
                                   (js/console.log "submitted-text will be reset to:" v)
                                   (reset! submitted-text v))}]
   [:p {:id "output"} "Submitted: " (str @submitted-text)]])

(describe "Enter to submit"
  (fn []
    (it "should submit text on Enter key"
      (fn []
        (reset! submitted-text nil)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (js/console.log "--- before render ---")
          (r/render [test-page] container)
          (js/console.log "--- after render ---")

          (let [input (.querySelector container "#submit-input")
                output (.querySelector container "#output")]

            (th/assert-equal (.-textContent output) "Submitted: ")

            ;; Simulate typing and pressing enter
            (set! (.-value input) "hello world")
            (js/console.log "input value set to:" (.-value input))
            (js/console.log "--- before dispatching input event ---")
            (.dispatchEvent input (new js/Event "input" #js {:bubbles true}))
            (js/console.log "--- after dispatching input event ---")
            (js/console.log "--- before dispatching keydown event ---")
            (.dispatchEvent input (new js/KeyboardEvent "keydown" #js {:code "Enter", :bubbles true}))
            (js/console.log "--- after dispatching keydown event ---")

            (js/console.log "output textContent after events:" (.-textContent output))
            (th/assert-equal (.-textContent output) "Submitted: hello world")
            (th/assert-equal @submitted-text "hello world")

            ;; Input should be cleared after submit
            (th/assert-equal (.-value input) "")))))))

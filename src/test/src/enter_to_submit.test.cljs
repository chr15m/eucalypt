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
  (th/log "enter-submit-input creating new state")
  (let [val (r/atom "")]
    (fn []
      (letfn [(save [_e]
                (th/log "save called. @val is:" @val)
                (let [v (-> @val str str/trim)]
                  (when-not (empty? v)
                    (th/log "on-save will be called with:" v)
                    (on-save v))
                  (reset! val "")))]
        [:input {:type "text"
                 :id "submit-input"
                 :value @val
                 :on-change (fn [e]
                              (let [new-v (.. e -target -value)]
                                (th/log "on-change called. new value:" new-v)
                                (reset! val new-v)))
                 :on-key-down (fn [e]
                                (th/log "on-key-down called. key code:" (.-code e))
                                (when (= (.-code e) "Enter")
                                  (save e)))}]))))

(defn test-page []
  (th/log "test-page rendering")
  [:div
   [enter-submit-input {:on-save (fn [v]
                                   (th/log "submitted-text will be reset to:" v)
                                   (reset! submitted-text v))}]
   [:p {:id "output"} "Submitted: " (str @submitted-text)]])

(describe "Enter to submit"
  (fn []
    (it "should submit text on Enter key"
      (fn []
        (reset! submitted-text nil)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (th/log "--- before render ---")
          (r/render [test-page] container)
          (th/log "--- after render ---")

          (let [input (.querySelector container "#submit-input")
                output (.querySelector container "#output")]

            (th/assert-equal (.-textContent output) "Submitted: ")

            ;; Simulate typing and pressing enter
            (set! (.-value input) "hello world")
            (th/log "input value set to:" (.-value input))
            (th/log "--- before dispatching input event ---")
            (.dispatchEvent input (new js/Event "input" #js {:bubbles true}))
            (th/log "--- after dispatching input event ---")
            (th/log "--- before dispatching keydown event ---")
            (.dispatchEvent input (new js/KeyboardEvent "keydown" #js {:code "Enter", :bubbles true}))
            (th/log "--- after dispatching keydown event ---")

            (th/log "output textContent after events:" (.-textContent output))
            (th/assert-equal (.-textContent output) "Submitted: hello world")
            (th/assert-equal @submitted-text "hello world")

            ;; Input should be cleared after submit
            (th/assert-equal (.-value input) "")))))))

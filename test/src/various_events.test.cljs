(ns various-events.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

;;; on-blur test
(def blur-state (r/ratom {:blurred? false}))

(defn blur-component []
  [:input {:type "text"
           :id "blur-input"
           :on-blur #(swap! blur-state assoc :blurred? true)}])

(describe "on-blur event"
  (fn []
    (it "should trigger when element loses focus"
      (fn []
        (reset! blur-state {:blurred? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [blur-component] container)
          (let [input (.querySelector container "#blur-input")]
            (.focus input)
            (.blur input)
            (th/assert-equal (:blurred? @blur-state) true)))))))

;;; on-double-click test
(def dbl-click-state (r/ratom {:clicked? false}))

(defn dbl-click-component []
  [:button {:id "dbl-click-btn"
            :on-double-click #(swap! dbl-click-state assoc :clicked? true)}
   "Double Click Me"])

(describe "on-double-click event"
  (fn []
    (it "should trigger on double click"
      (fn []
        (reset! dbl-click-state {:clicked? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [dbl-click-component] container)
          (let [button (.querySelector container "#dbl-click-btn")]
            (.dispatchEvent button (new js/Event "dblclick" #js {:bubbles true}))
            (th/assert-equal (:clicked? @dbl-click-state) true)))))))

;;; on-key-down test
(def key-down-state (r/ratom {:key-pressed nil}))

(defn key-down-component []
  [:input {:type "text"
           :id "key-down-input"
           :on-key-down (fn [e]
                          (when (= (.-code e) "Enter")
                            (swap! key-down-state assoc :key-pressed "Enter")))}])

(describe "on-key-down event"
  (fn []
    (it "should trigger on key press"
      (fn []
        (reset! key-down-state {:key-pressed nil})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [key-down-component] container)
          (let [input (.querySelector container "#key-down-input")]
            (.dispatchEvent input (new js/KeyboardEvent "keydown" #js {:code "Enter" :bubbles true}))
            (th/assert-equal (:key-pressed @key-down-state) "Enter")))))))

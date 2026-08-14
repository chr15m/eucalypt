(ns camel-case-events.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

;;; onClick and onMouseDown test
(def click-state (r/atom {:clicked? false :mouse-down? false}))

(defn click-component []
  [:button {:id "click-btn"
            :onClick #(swap! click-state assoc :clicked? true)
            :onMouseDown #(swap! click-state assoc :mouse-down? true)}
   "Click Me"])

(describe "Camel case events"
  (fn []
    (it "should support onClick and onMouseDown"
      (fn []
        (reset! click-state {:clicked? false :mouse-down? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [click-component] container)
          (let [button (.querySelector container "#click-btn")]
            (th/assert-equal (:mouse-down? @click-state) false)
            (th/fire-event button "mousedown")
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (:mouse-down? @click-state) true)
                         (th/assert-equal (:clicked? @click-state) false)
                         (th/fire-event button "click")
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (:clicked? @click-state) true))))))))))

;;; onFocusIn/onFocusOut test
(def focus-state (r/atom {:focus-in? false :focus-out? false}))

(defn focus-component []
  [:input {:type "text"
           :id "focus-input"
           :onFocusIn #(swap! focus-state assoc :focus-in? true)
           :onFocusOut #(swap! focus-state assoc :focus-out? true)}])

(describe "Camel case focus events"
  (fn []
    (it "should support onFocusIn and onFocusOut"
      (fn []
        (reset! focus-state {:focus-in? false :focus-out? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [focus-component] container)
          (let [input (.querySelector container "#focus-input")]
            (th/assert-equal (:focus-in? @focus-state) false)
            (th/fire-event input "focusin")
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (:focus-in? @focus-state) true)
                         (th/assert-equal (:focus-out? @focus-state) false)
                         (th/fire-event input "focusout")
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (:focus-out? @focus-state) true))))))))))

;;; onMouseOver test
(def mouse-over-state (r/atom {:count 0}))

(defn mouse-over-component []
  [:div {:id "mouse-over-div"
         :onMouseOver #(swap! mouse-over-state update :count inc)}
   "Hover over me"])

(describe "Camel case mouse events"
  (fn []
    (it "should support onMouseOver"
      (fn []
        (reset! mouse-over-state {:count 0})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [mouse-over-component] container)
          (let [div (.querySelector container "#mouse-over-div")]
            (th/assert-equal (:count @mouse-over-state) 0)
            (th/fire-event div "mouseover")
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (:count @mouse-over-state) 1)
                         (th/fire-event div "mouseover")
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (:count @mouse-over-state) 2))))))))))

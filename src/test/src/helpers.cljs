(ns helpers
  (:refer-clojure :exclude [rand])
  (:require ["vitest" :refer [expect]]))

(defn log [& args]
  ;; DEPRECATED: use js/console.log instead
  (try
    (let [debug-env (.. js/process -env -DEBUG)]
      (when (and (string? debug-env)
                 (.includes debug-env "eucalypt-tests"))
        (.apply (.-log js/console) js/console args)))
    ;; ignore if process is not available or something else goes wrong
    (catch :default _)))

(defn assert-equal [actual expected]
  (-> (expect actual) (.toEqual expected)))

(defn assert-not-nil [actual]
  (-> (expect actual) .-not (.toBeNull)))

(defn rand [] ; shim for missing rand in squint
  (js/Math.random))

(defn fire-event
  "Fires a DOM event on an element. Uses ReactTestUtils.Simulate if available (under Reagent runner),
   otherwise dispatches a standard DOM event."
  [el event-type & [opts]]
  (if (and (= event-type "click") (fn? (.-click el)))
    (.click el)
    (let [simulate-name (case event-type
                          "keydown" "keyDown"
                          "keyup" "keyUp"
                          "keypress" "keyPress"
                          "doubleclick" "doubleClick"
                          "dblclick" "doubleClick"
                          "mousedown" "mouseDown"
                          "mouseup" "mouseUp"
                          "mouseover" "mouseOver"
                          "mouseout" "mouseOut"
                          "mousemove" "mouseMove"
                          "mouseenter" "mouseEnter"
                          "mouseleave" "mouseLeave"
                          "animationend" "animationEnd"
                          event-type)
          simulate-fn (and (exists? js/ReactTestUtils)
                           (aget js/ReactTestUtils "Simulate")
                           (aget (.-Simulate js/ReactTestUtils) simulate-name))]
      (if simulate-fn
        (simulate-fn el (clj->js (or opts #js {})))
        (let [tag (and (.-tagName el) (.toUpperCase (.-tagName el)))
              is-text-input? (contains? #{"INPUT" "TEXTAREA"} tag)
              evt-opts (clj->js (merge {:bubbles true} opts))
              dom-event-type (if (or (= event-type "doubleclick") (= event-type "dblclick"))
                               "dblclick"
                               event-type)
              evt (if (contains? #{"keydown" "keyup" "keypress"} dom-event-type)
                    (new js/KeyboardEvent dom-event-type evt-opts)
                    (new js/Event dom-event-type evt-opts))]
          (when-let [v (get-in opts [:target :value])]
            (set! (.-value el) v))
          (.dispatchEvent el evt)
          (when (and (= event-type "change") is-text-input?)
            (.dispatchEvent el (new js/Event "input" evt-opts))))))))

(defn wait-for-render
  "Returns a promise that resolves after the next requestAnimationFrame.
   Use this in tests after triggering events to wait for re-renders.
   Reagent batches updates and schedules re-renders asynchronously via rAF.
   Eucalypt re-renders synchronously but this helper ensures compatibility
   with both libraries in the test environment."
  []
  (js/Promise.
    (fn [resolve]
      (js/requestAnimationFrame resolve))))

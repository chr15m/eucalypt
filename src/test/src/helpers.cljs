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
  (if (and (exists? js/ReactTestUtils)
           (aget js/ReactTestUtils "Simulate")
           (aget (.-Simulate js/ReactTestUtils) event-type))
    ((aget (.-Simulate js/ReactTestUtils) event-type) el)
    (.dispatchEvent el (new js/Event event-type (clj->js (or opts {:bubbles true}))))))

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

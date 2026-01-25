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

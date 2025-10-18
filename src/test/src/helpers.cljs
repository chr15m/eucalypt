(ns helpers
  (:require ["vitest" :refer [expect]]))

(defn log [& args]
  (try
    (let [debug-env (.. js/process -env -DEBUG)]
      (when (and (string? debug-env)
                 (.includes debug-env "eucalypt-tests"))
        (.apply (.-log js/console) js/console args)))
    (catch :default _
      ;; ignore if process is not available or something else goes wrong
      )))

(defn assert-equal [actual expected]
  (-> (expect actual) (.toEqual expected)))

(defn assert-not-nil [actual]
  (-> (expect actual) (.not.toBeNull)))

(defn rand []
  (js/Math.random))

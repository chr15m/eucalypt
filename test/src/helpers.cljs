(ns helpers
  (:require ["vitest" :refer [expect]]))

(defn assert-equal [actual expected]
  (-> (expect actual) (.toEqual expected)))

(defn assert-not-nil [actual]
  (-> (expect actual) (.not.toBeNull)))

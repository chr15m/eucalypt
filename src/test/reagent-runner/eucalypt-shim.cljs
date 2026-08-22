(ns eucalypt
  (:require [reagent.core]
            [reagent.dom]
            [reagent.ratom]))

;; Map eucalypt API to reagent API by aliasing the functions
(defn atom [& args]
  (apply reagent.core/atom args))

(defn render [& args]
  (apply reagent.dom/render args))

(defn cursor [& args]
  (apply reagent.core/cursor args))

(defn reaction [& args]
  (apply reagent.ratom/make-reaction args))

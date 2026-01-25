(ns eucalypt
  (:require [reagent.core]
            [reagent.dom]))

;; Map eucalypt API to reagent API by aliasing the functions
(defn atom [& args]
  (apply reagent.core/atom args))

(defn render [& args]
  (apply reagent.dom/render args))

(defn cursor [& args]
  (apply reagent.core/cursor args))

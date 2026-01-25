(ns updater
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(def state (r/atom nil))

(defn app []
  [:div "Hello world!"]
  [:button {:on-click #(swap! state update :val inc)} "Counter: " (:val @state)])

(rdom/render [app] (.getElementById js/document "app"))

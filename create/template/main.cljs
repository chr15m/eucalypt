(ns main
  (:require
    [eucalypt :as r]))

(defonce state (r/atom {:v 0}))

(defn component:main [state]
  [:<>
    [:p "Hello from Eucalypt!"]
    [:button {:on-click #(swap! state update :v inc)} "inc"]
    [:pre (pr-str @state)]])

(r/render
  [component:main state]
  (js/document.getElementById "app"))


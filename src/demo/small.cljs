(ns small
  (:require
    [eucalypt :as r]))

(defonce state (r/atom {:v 0}))

(defn component:main [state]
  [:<>
    [:p "Hello from Eucalypt!"]
    [:button {:on-click #(swap! state update :v inc)} "inc"]
    [:pre (pr-str @state)]
    [:p "Check the network tab for artifact size."]
    [:style "body { font-family: arial,helvetica; width: 350px; margin: auto; }"]])

(r/render
  [component:main state]
  (js/document.getElementById "app"))


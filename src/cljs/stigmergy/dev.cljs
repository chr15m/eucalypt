(ns stigmergy.dev
  (:require [stigmergy.mr-clean :as r]))

(enable-console-print!)
(r/init)

(def app-state (r/atom {:name "Sonny"
                        :age 10}))
(def age (r/cursor app-state [:age]))


(comment
  (defn your-age [age height]
    ;;(prn "your-age " @age height)
    (prn "your age=" @age)
    (let [hiccup (if (even? @age)
                   [:h1 {:style {:color :red}} "even age="@age " height=" height]
                   [:h1 {:style {:color :blue}} "odd age="@age ])]
      hiccup))

  (defn hello [state greeting]
    (let [counter (r/atom 0)
          value (r/atom "1")
          age (r/cursor app-state [:age])
          double-age (r/reaction (fn []
                                   (let [d (* 2 @age)]
                                     (prn "double-age=" d)
                                     d)))]
      (fn [state greeting]
        ;; (prn "value=" @value)
        ;; (prn "counter=" @counter)
        [:div
         [:h1 "I'm " (:name @state) " age="@age]
         [:input {:id "foo" :on-input #(reset! value (.. % -target -value))
                  :value @value}]
         [:button {:on-click #(do
                                (swap! counter inc))} @counter]
         [your-age age @counter]
         nil
         [your-age double-age (inc @counter)]])))

  (r/render [hello app-state "wassup2"] (js/document.getElementById "app"))
  (swap! age inc) 
  
  (swap! app-state assoc :name "vlad3")
  (swap! app-state assoc :age 13)

  (count  @(.-watchers app-state))
  (count  @(.-watchers age))
  ((first @(.-watchers age)))
  
  @(.-watchers app-state)
  @(.-watchers age)

  (def age (cursor app-state [:age]))
  (def hello2 (r/create-class {:component-will-mount (fn [renderable]
                                                       (prn "hello2 component-will-mount=" )
                                                       )
                               :component-did-mount (fn [renderable]
                                                      (prn "hello2 component-did-mount ")
                                                      )
                               :component-will-unmount (fn [renderable]
                                                         (prn "hello2 component-will-unmount ")
                                                         )
                               :component-will-receive-props (fn [renderable]
                                                               ;;(prn "component-will-receive-props " renderable)
                                                               )
                               :reagent-render (fn [app-state]
                                                 (prn "hello2 render")
                                                 (let [age (r/cursor app-state [:age])
                                                       height 300]
                                                   [:div
                                                    [:h1 "i'm a reagent component. I am " (:name @app-state)]
                                                    [your-age age height]
                                                    ]))}))
  (r/render [hello2 app-state]
            (js/document.getElementById "app"))

  (swap! age inc)

  (defn my-name [app-state]
    (prn "my-name " app-state)
    [:h1 "my name is " (:name @app-state)])
  
  (defn hello3 [app-state]
    ;;(prn "hello3 " app-state)
    [:div
     ;;[:h1 "hello " (:name @app-state)]
     [my-name app-state]
     ]
    )
  
  (r/render [hello3 app-state]
            (js/document.getElementById "app"))

  (swap! app-state assoc :name "foo6")
  
  (setq projectile-project-search-path '("~/workspace/stigmergy-webtop/src"
                                         ))

  (defn remove-at-index [a i]
    (let [length (count a)]
      (cond
        (= 0 i) (vec (rest a))
        (= i (dec length)) (subvec a 0 i)
        :else (vec (concat (subvec a 0 i)
                           (subvec a (inc i) length))))))

  (defn window [app-state]
    [:div {:style {:border-style :solid
                   :position :absolute
                   :left (:x @app-state)
                   :top (:y @app-state)}}
     [:div {:style {:background-color :yellow}
            :draggable true
            :on-drag (fn [evt]
                       (swap! app-state (fn [app-state]
                                          (let [x (.. evt -clientX)
                                                y (.. evt -clientY)]
                                            (assoc app-state :x x :y y)))))}
      "Title"]
     [:div {:style {:width 500
                    :height 200
                    :background-color :blue}} "body"]])
  
  (def app-state (atom {:x 0 :y 0}))
  (swap! app-state assoc :x 300 :y 50)
  (render [window app-state] (js/document.getElementById "app"))

  (require '[clojure.walk :as w])
  (def h [:div
          [:h1 "I'm " "Sonny" " age=" 10]
          [:input {:id "foo", :value "1"}]
          [:button 0]
          nil
          [:h1 {:style {:color :red}} "even age=" 10 " height=" 0]
          [:h1 {:style {:color :red}} "even age=" 20 " height=" 1]])

  (def h [:div [:h1 "I'm " "Sonny" " age=" 10] [:input {:id "foo", :on-input nil, :value "1"}] [:button {} 0] [:h1 {:style {:color :red}} "even age=" 10 " height=" 0] nil [:h1 {:style {:color :red}} "even age=" 20 " height=" 1]])
  (def j (w/postwalk (fn [x]
                       (if (nil? x)
                         ""
                         x)
                       )
                     h))
  
  )

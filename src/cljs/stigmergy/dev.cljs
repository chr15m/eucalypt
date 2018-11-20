(ns stigmergy.dev
  (:require [stigmergy.mr-clean :as r]))

(enable-console-print!)
(r/init)

(comment
  (def app-state (r/atom {:name "Sonny"
                          :age 10}))
  
  (def age (r/cursor app-state [:age]))
  
  (defn your-age [age height]
    (prn "your-age " @age height)
    (let [hiccup (if (even? @age)
                   [:h1 {:style {:color :red}} "even age="@age " height=" height]
                   [:h1 {:style {:color :blue}} "odd age="@age ])]
      hiccup))

  (comment
    (r/render [your-age age 200] (js/document.getElementById "app"))
    (render [hello app-state "wassup2"] (js/document.getElementById "app"))
    (swap! age inc)
    (swap! app-state assoc :name "Sonny To")
    (.-watchers app-state)
    (.-watchers age)
    (satisfies? IAtom age)
    (satisfies? IAtom app-state)
    (= @(.-watchers app-state) @(.-watchers age))
    
    (ik/hiccup->str [:h1 "foo"])
    )
  
  (defn hello [state greeting]
    (let [counter (r/atom 0)
          value (r/atom "1")
          age (r/cursor app-state [:age])
          double-age (r/reaction (fn [] (* 2 @age)))]
      (prn "staet0=" @state) 
      (prn "value0=" @value)
      (prn "counter0=" @counter)
      (fn [state greeting]
        (prn "value=" @value)
        (prn "counter=" @counter)
        [:div
         [:h1 "I'm " (:name @state) " age="@age]
         [:input {:id "foo" :on-input #(reset! value (.. % -target -value))
                  :value @value}]
         [:button {:on-click #(do
                                (swap! counter inc))} @counter]
         [your-age age @counter]
         [your-age double-age (inc @counter)]])))

  (r/render [hello app-state "wassup2"] (js/document.getElementById "app"))

  (swap! age inc) 
  (swap! age2 inc)

  (def age (cursor app-state [:age]))
  (render [hello app-state "wassup2"] (js/document.getElementById "app"))

  (swap! app-state assoc :name "vlad")
  (swap! app-state assoc :age 13)

  (count  @(.-watchers app-state))
  (count  @(.-watchers age))
  ((first @(.-watchers age)))
  
  @(.-watchers app-state)
  @(.-watchers age)

  (def age (cursor app-state [:age]))
  (def hello2 (create-class {:component-will-mount (fn [renderable]
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
                                               (let [age (cursor app-state [:age])
                                                     height 300]
                                                 [:div
                                                  [:h1 "i'm a reagent component. I am " (:name @app-state)]
                                                  [your-age age height]
                                                  ]))}))
  (render [hello2 app-state]
          (js/document.getElementById "app"))

  
  (swap! age inc)

  (render [:h1 "wassup " @age] (js/document.getElementById "app"))
  
  
  (def foo (normalize-component [:h1 "hello " @age])) 

  (setq projectile-project-search-path '("~/workspace/stigmergy-webtop/src"
                                         ))

  (go (>! render-queue 2))

  (let [f (fn [a] (inc a))
        g (fn [a]  (+ a 2))
        j (comp f g)
        r (j 1)
        ]
    r
    )
  (def s (atom 2))
  @s
  (def r (reaction (fn [] (+ 2 @s))))
  @r
  (reset! s 6)
  (reset! r 2)
  (swap! r inc)

  (def age (cursor app-state [:age]))
  (def age2 (reaction (fn [] (* 2 @age))))
  (render [your-age age2 200] (js/document.getElementById "app"))

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
  )

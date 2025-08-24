(ns main
  (:require [eucalypt :as r]))

(js/console.log "main.cljs loading...")

(def app-state
  (r/ratom
   {:page :home
    :counter 0
    :timer (js/Date.)
    :text-input ""}))

(defn nav-link [page-kw text]
  [:a {:href "#"
       :style {:margin-right "10px"}
       :on-click (fn [e]
                   (.preventDefault e)
                   (swap! app-state assoc :page page-kw))}
   text])

(defn home-page []
  [:div
   [:h1 "Home Page"]
   [:p "Welcome to the mr-clean demo app!"]
   [:p "The current count is: " (:counter @app-state)]
   [:button {:on-click (fn [_] (swap! app-state update :counter inc))}
    "Increment counter"]])

(defn text-input-page []
  [:div
   [:h1 "Text Input Page"]
   [:p "The current text is: " (:text-input @app-state)]
   [:input {:type "text"
            :value (:text-input @app-state)
            :on-input (fn [e]
                        (swap! app-state assoc :text-input (aget (aget e "target") "value")))}]
   [:p "This is some text after the input to test rendering."]])

(defn skateboarding-icon []
  [:svg {:stroke "currentColor", :fill "none", :stroke-linejoin "round", :width "24", :viewBox "0 0 24 24", :xmlns "http://www.w3.org/2000/svg", :stroke-linecap "round", :stroke-width "2", :class "icon icon-tabler icons-tabler-outline icon-tabler-skateboarding", :height "24"}
   [:path {:stroke "none", :d "M0 0h24v24H0z", :fill "none"}]
   [:path {:d "M16 4a1 1 0 1 0 2 0a1 1 0 0 0 -2 0"}]
   [:path {:d "M5.5 15h3.5l.75 -1.5"}]
   [:path {:d "M14 19v-5l-2.5 -3l2.5 -4"}]
   [:path {:d "M8 8l3 -1h4l1 3h3"}]
   [:path {:d "M17.5 21a.5 .5 0 1 0 0 -1a.5 .5 0 0 0 0 1z", :fill "currentColor"}]
   [:path {:d "M3 18c0 .552 .895 1 2 1h14c1.105 0 2 -.448 2 -1"}]
   [:path {:d "M6.5 21a.5 .5 0 1 0 0 -1a.5 .5 0 0 0 0 1z", :fill "currentColor"}]])

(defn svg-page []
  [:div
   [:h1 "SVG Test Page"]
   [skateboarding-icon]])

(defn calc-bmi [{:keys [height weight bmi] :as data}]
  (let [h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(def bmi-data (r/ratom (calc-bmi {:height 180 :weight 80})))

(defn slider [the-atom calc-fn param value min max step invalidates]
  [:input {:type "range" :value value :min min :max max :step step
           :style {:width "100%"}
           :on-input (fn [e]
                       (js/console.log "slider on-input fired" e)
                       (let [new-value (js/parseFloat (aget (aget e "target") "value"))]
                         (swap! the-atom
                                (fn [data]
                                  (-> data
                                    (assoc param new-value)
                                    (dissoc invalidates)
                                    calc-fn)))))}])

(defn bmi-page []
  (let [{:keys [weight height bmi]} @bmi-data
        [color diagnose] (cond
                          (< bmi 18.5) ["orange" "underweight"]
                          (< bmi 25) ["inherit" "normal"]
                          (< bmi 30) ["orange" "overweight"]
                          :else ["red" "obese"])]
    [:div
     [:h3 "BMI calculator"]
     [:div
      "Height: " (int height) "cm"
      [slider bmi-data calc-bmi :height height 100 220 1 :bmi]]
     [:div
      "Weight: " (int weight) "kg"
      [slider bmi-data calc-bmi :weight weight 30 150 1 :bmi]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider bmi-data calc-bmi :bmi bmi 10 50 1 :weight]]]))

(defn calc-ohms [{:keys [voltage current resistance] :as data}]
  (if (nil? voltage)
    (assoc data :voltage (* current resistance))
    (assoc data :current (/ voltage resistance))))

(def ohms-data (r/ratom {:voltage 12 :current 0.5 :resistance 24}))

(defn ohms-law-page []
  (let [{:keys [voltage current resistance]} @ohms-data]
    [:div
     [:h3 "Ohm's Law Calculator"]
     [:div
      "Voltage: " (.toFixed voltage 2) "V"
      [slider ohms-data calc-ohms :voltage voltage 0 30 0.1 :current]]
     [:div
      "Current: " (.toFixed current 2) "A"
      [slider ohms-data calc-ohms :current current 0 3 0.01 :voltage]]
     [:div
      "Resistance: " (.toFixed resistance 2) "Ω"
      [slider ohms-data calc-ohms :resistance resistance 0 100 1 :voltage]]]))

(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:p {:class "someclass"}
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(defn simple-parent []
  [:div
   [:p "I include simple-component."]
   [simple-component]])

(defn hello-component [name]
  [:p "Hello, " name "!"])

(defn say-hello []
  [hello-component "world"])

(defn lister [items]
  [:ul
   (for [item items]
     ^{:key item} [:li "Item " item])])

(defn lister-user []
  [:div
   "Here is a list:"
   [lister (range 3)]])

(defn basic-tests-page []
  [:div
   [:h1 "Basic Tests"]
   [:h2 "simple-component"]
   [simple-component]
   [:hr]
   [:h2 "simple-parent"]
   [simple-parent]
   [:hr]
   [:h2 "say-hello (hello-component)"]
   [say-hello]
   [:hr]
   [:h2 "lister-user (lister)"]
   [lister-user]])

(defn ref-test-page []
  (js/console.log "ref-test-page render fn called")
  [:div {:ref (fn [el]
                (js/console.log "ref-test-page :ref fn called with el:" el))}
   [:h1 "Ref Test Page"]
   [:p {:ref (fn [el]
               (aset el "style" "background-color" "pink"))}
    "Pink background added in ref."]
   [:p "Check the console to see the :ref function getting called."]])

(def timer-page
  (let [interval-id (atom nil)
        ref-fn (fn [el]
                 (js/console.log "timer-page :ref fn called with el:" el "and interval-id:" @interval-id)
                 (if el
                   (when (nil? @interval-id)
                     (js/console.log "Timer :ref mount, starting timer.")
                     (reset! interval-id
                             (js/setInterval
                               #(swap! app-state assoc :timer (js/Date.))
                               1000)))
                   (when @interval-id
                     (js/console.log "Timer :ref unmount, clearing timer.")
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil))))]
    (js/console.log "timer-page component defined, interval-id atom created:" interval-id)
    (fn []
      (js/console.log "timer-page render fn called, interval-id is:" @interval-id)
      [:div {:ref ref-fn}
       [:h1 "Timer Page"]
       [:p "The current time is: " (-> (:timer @app-state) .getSeconds str)]])))

(defn app []
  (let [page (:page @app-state)]
    [:div
     [:nav
      [nav-link :home "Home"]
      [nav-link :basic-tests "Basic Tests"]
      [nav-link :text-input "Text Input"]
      [nav-link :timer "Timer"]
      [nav-link :ref-test "Ref Test"]
      [nav-link :svg "SVG"]
      #_ [nav-link :bmi "BMI Calc"]
      #_ [nav-link :ohms "Ohm's Law"]]
     [:hr {:style {:margin "1rem 0"}}]
     (case page
       :home [home-page]
       :basic-tests [basic-tests-page]
       :text-input [text-input-page]
       :timer [timer-page]
       :ref-test [ref-test-page]
       :svg [svg-page]
       :bmi [bmi-page]
       :ohms [ohms-law-page]
       [:div "Page not found"])]))

(js/console.log "main.cljs: about to call render!")
(r/render [app]
          (.getElementById js/document "app"))
(js/console.log "main.cljs: render! finished")

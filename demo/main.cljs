(ns main
  (:require [eucalypt :as r]
            [clojure.string :as str]))

(js/console.log "main.cljs loading...")

(def app-state
  (r/ratom
   {:page :home
    :counter 0
    :timer 0
    :coins 99
    :showfrag false
    :text-input ""
    :page-size "loading..."}))

(def clock-time (r/ratom (js/Date.)))
(def time-color (r/ratom "#f34"))
(js/setInterval #(reset! clock-time (js/Date.)) 1000)

(defn get-page-size []
  (-> (js/fetch (.-href js/location))
      (.then (fn [response]
               (let [headers (.-headers response)
                     is-gzipped (= (.get headers "content-encoding") "gzip")
                     content-length (.get headers "content-length")]
                 (if (and is-gzipped content-length)
                   (let [size-kb (/ (js/parseInt content-length) 1024)]
                     (swap! app-state assoc :page-size (str (.toFixed size-kb 1) "kb gzipped")))
                   (-> (.text response)
                       (.then (fn [text]
                                (let [size-kb (/ (.-length text) 1024)]
                                  (swap! app-state assoc :page-size (str (.toFixed size-kb 1) "kb"))))))))))
      (.catch (fn [error]
                (js/console.error "Error fetching page size:" error)
                (swap! app-state assoc :page-size "Error")))))

(defn nav-link [page-kw text]
  [:a {:href "#"
       :class "nav-link"
       :on-click (fn [e]
                   (.preventDefault e)
                   (swap! app-state assoc :page page-kw))}
   text])

(defn home-page []
  [:div
   [:h1 "Home Page"]
   [:p "Welcome to the eucalypt demo app."]
   [:p "The ClojureScript code for these examples is compiled into this single index.html
       file."]
   [:p "The size of this index.html file is: " [:strong (:page-size @app-state)]]
   [:p "The compiler is "
    [:a {:href "https://github.com/squint-cljs/squint"} "squint-cljs"]
    " and the build tool is "
    [:a {:href "https://vitejs.dev"} "vite"] "."]
   [:hr]
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
                        (swap! app-state assoc
                               :text-input (aget (aget e "target") "value")))}]
   [:p "This is some text after the input to test rendering."]])

(defn skateboarding-icon []
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :width "24"
         :height "24"
         :viewBox "0 0 24 24"
         :stroke "currentColor"
         :fill "none"
         :stroke-linejoin "round"
         :stroke-linecap "round"
         :stroke-width "2"
         :class "icon"}
   [:path {:stroke "none" :d "M0 0h24v24H0z" :fill "none"}]
   [:path {:d "M16 4a1 1 0 1 0 2 0a1 1 0 0 0 -2 0"}]
   [:path {:d "M5.5 15h3.5l.75 -1.5"}]
   [:path {:d "M14 19v-5l-2.5 -3l2.5 -4"}]
   [:path {:d "M8 8l3 -1h4l1 3h3"}]
   [:path {:d "M17.5 21a.5 .5 0 1 0 0 -1a.5 .5 0 0 0 0 1z" :fill "currentColor"}]
   [:path {:d "M3 18c0 .552 .895 1 2 1h14c1.105 0 2 -.448 2 -1"}]
   [:path {:d "M6.5 21a.5 .5 0 1 0 0 -1a.5 .5 0 0 0 0 1z" :fill "currentColor"}]])

(defn svg-page []
  [:div
   [:h1 "SVG Test Page"]
   [skateboarding-icon]])

(defn slider [the-atom calc-fn param value min max step invalidates]
  [:input {:type "range" :value value :min min :max max :step step
           :class "slider"
           :on-input (fn [e]
                       (js/console.log "slider on-input fired" e)
                       (let [new-value (js/parseFloat
                                         (aget (aget e "target") "value"))]
                         (swap! the-atom
                                (fn [data]
                                  (-> data
                                    (assoc param new-value)
                                    (dissoc invalidates)
                                    calc-fn)))))}])

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
    [:span {:class "text-red"} " and red "] "text."]])

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
               (when el
                 (aset el "style" "background-color" "pink")))}
    "Original text that gets replaced."]
   [:p "Check the console to see the :ref function getting called."]])

(def timer-page
  (let [interval-id (atom nil)
        ref-fn (fn [el]
                 (js/console.log
                   "timer-page :ref fn called with el:" el
                   "and interval-id:" @interval-id)
                 (if el
                   (when (nil? @interval-id)
                     (js/console.log "Timer :ref mount, starting timer.")
                     (reset! interval-id
                             (js/setInterval
                               #(swap! app-state update :timer inc)
                               1000)))
                   (when @interval-id
                     (js/console.log "Timer :ref unmount, clearing timer.")
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil))))]
    (js/console.log "timer-page component defined, interval-id atom created:"
                    interval-id)
    (fn []
      (js/console.log "timer-page render fn called, interval-id is:" @interval-id)
      [:div {:ref ref-fn}
       [:h1 "Timer Page"]
       [:p "The timer increments every second only while on this tab."]
       [:p "The current timer is: " (:timer @app-state)]])))

(def list-data (r/ratom []))

(defn list-demo-page []
  [:div
   [:h1 "List Demo"]
   [:button {:on-click (fn [_]
                         (swap! list-data conj (js/Math.random)))}
    "+ Add"]
   [:ul
    (for [item @list-data]
      ^{:key item}
      [:li
       item
       " "
       [:button {:on-click (fn [_]
                             (swap! list-data
                                    (fn [items]
                                      (vec (remove #(= % item) items)))))}
        "x"]])]])

;;; TodoMVC implementation

(defonce todos (r/ratom {}))
(defonce todomvc-counter (r/ratom 0))

(defn add-todo [text]
  (let [id (swap! todomvc-counter inc)]
    (swap! todos assoc id {:id id :title text :done false})))

(defn toggle [id] (swap! todos update-in [id :done] not))
(defn save [id title] (swap! todos assoc-in [id :title] title))
(defn delete-todo [id] (swap! todos dissoc id))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v] (swap! todos mmap map #(assoc-in % [1 :done] v)))
(defn clear-done [] (swap! todos mmap remove #(get-in % [1 :done])))

(defn todo-input [{:keys [title on-save on-stop] :as _props}]
  (let [val (r/ratom (or title ""))]
    (fn [props]
      (letfn [(stop [_e]
                (reset! val "")
                (when on-stop (on-stop)))
              (save [e]
                (let [v (-> @val str str/trim)]
                  (when-not (empty? v)
                    (on-save v)))
                (stop e))]
        [:input (merge
                  {:type "text"
                   :value @val
                   :on-blur save
                   :on-change (fn [e] (reset! val (.. e -target -value)))
                   :on-key-down (fn [e]
                                  (case (.-code e)
                                    "Enter" (save e)
                                    "Escape" (stop e)
                                    nil))}
                  (select-keys props [:id :class :placeholder :ref]))]))))

(defn todo-edit [props]
  [todo-input (assoc props :ref (fn [el] (when el (.focus el))))])

(defn todo-stats [{:keys [filt active done]}]
  (let [props-for (fn [name]
                    {:href "#"
                     :class (when (= name @filt) "selected")
                     :on-click (fn [e]
                                 (.preventDefault e)
                                 (reset! filt name))})]
    [:div
     [:span {:id "todo-count"}
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul {:id "filters"}
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? done)
       [:button {:id "clear-completed" :on-click clear-done}
        "Clear completed " done])]))

(defn todo-item []
  (let [editing (r/ratom false)]
    (fn [{:keys [id done title]}]
      [:li
       {:class (->> [(when done "completed")
                     (when @editing "editing")]
                    (remove nil?)
                    (str/join " "))}
       [:div {:class "view"}
        [:input {:class "toggle"
                 :type "checkbox"
                 :checked done
                 :on-change #(toggle id)}]
        [:label {:on-double-click #(reset! editing true)}
         title]
        [:button {:class "destroy" :on-click #(delete-todo id)}]]
       (when @editing
         [todo-edit {:class "edit"
                     :title title
                     :on-save #(save id %)
                     :on-stop #(reset! editing false)}])])))

(defn todomvc-page []
  (let [filt (r/ratom :all)]
    (fn []
      (let [items (->> (vals @todos) (sort-by :id))
            done (->> items (filter :done) count)
            active (- (count items) done)]
        [:div
         [:section {:id "todoapp"}
          [:header {:id "header"}
           [:h1 "todos"]
           [todo-input {:id "new-todo"
                        :placeholder "What needs to be done?"
                        :on-save add-todo}]]
          (when (seq items)
            [:div
             [:section {:id "main"}
              [:input {:id "toggle-all" :type "checkbox" :checked (zero? active)
                       :on-change #(complete-all (pos? active))}]
              [:label {:for "toggle-all"} "Mark all as complete"]
              [:ul {:id "todo-list"}
               (for [todo (filter (case @filt
                                    :active (complement :done)
                                    :done :done
                                    :all identity) items)]
                 ^{:key (:id todo)} [todo-item todo])]]
             [:footer {:id "footer"}
              [todo-stats {:active active :done done :filt filt}]]])]
         [:footer {:id "info"}
          [:p "Double-click to edit a todo"]]]))))

(defn init-todos []
  (reset! todos {})
  (reset! todomvc-counter 0)
  (add-todo "one")
  (add-todo "two")
  (add-todo "three")
  (swap! todos assoc-in [2 :done] true))

(defn greeting [message]
  [:h1 message])

(defn clock []
  (let [time-str (-> @clock-time .toTimeString (str/split " ") first)]
    [:div {:style {:color @time-color
                   :font-size "2em"
                   :font-family "monospace"}}
     time-str]))

(defn color-input []
  [:div
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-input (fn [e]
                        (reset! time-color (.. e -target -value)))}]])

(defn clock-page []
  [:div
   [greeting "Hello world, it is now"]
   [clock]
   [color-input]])

(defn coins []
  [:div {:class "hud"} "🪙 " 99])

(defn fragments []
  [:<>
   [coins]
   (if (:showfrag @app-state)
     [:div "Hello"]
     (for [_ [1 2 3]]
       [:section
        [:div
         (let [slots-vec [1 2 3 4 5]
               slots-per-row 5
               rows (partition-all slots-per-row slots-vec)]
           (map-indexed
             (fn [row-idx row]
               [:div {:class "slot-row" :key row-idx}
                (let [row-vec (vec row)]
                  (map
                    (fn [slot]
                      (if slot
                        [:span
                         "🪙"]
                        [:span
                         "⚪"]))
                    row-vec))])
             rows))]]))
   [:button
    {:on-click #(swap! app-state update :showfrag not)}
    (if (:showfrag @app-state) "Show" "Hide")]])

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
      [nav-link :list-demo "List Demo"]
      [nav-link :todomvc "TodoMVC"]
      [nav-link :fragments "Fragments"]
      [nav-link :clock "Clock Demo"]
      [nav-link :ohms "Ohm's Law"]]
     [:hr {:class "separator"}]
     (case page
       :home [home-page]
       :basic-tests [basic-tests-page]
       :text-input [text-input-page]
       :timer [timer-page]
       :ref-test [ref-test-page]
       :svg [svg-page]
       :list-demo [list-demo-page]
       :todomvc [todomvc-page]
       :ohms [ohms-law-page]
       :fragments [fragments]
       :clock [clock-page]
       [:div "Page not found"])
     [:p [:a {:href "https://github.com/chr15m/eucalypt"} "Source code"]]]))

(js/console.log "main.cljs: about to call render!")
(r/render [app]
          (.getElementById js/document "app"))
(get-page-size)
(init-todos)
(js/console.log "main.cljs: render! finished")

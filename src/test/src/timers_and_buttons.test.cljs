(ns timers-and-buttons.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(defonce app-state (r/atom {:page :tab-0}))

(defn nav-link [page-kw text]
  [:a {:href "#"
       :class (when (= (:page @app-state) page-kw) "active")
       :on-click (fn [e]
                   (.preventDefault e)
                   ;(js/console.log "nav-link clicked" (str page-kw))
                   (swap! app-state assoc :page page-kw))}
   text])

(defonce tab-1-state (r/atom {:count 0}))

(def tab-1-component
  (let [interval-id (atom nil)
        ref-fn (fn [el]
                 ;(js/console.log "tab-1 ref" (if el (.toString el) "nil"))
                 (if el
                   (when (nil? @interval-id)
                     ;(js/console.log "tab-1 mount: starting timer")
                     (reset! interval-id
                             (js/setInterval
                               #(swap! tab-1-state update :count inc)
                               1000)))
                   (when @interval-id
                     ;(js/console.log "tab-1 unmount: clearing timer")
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil))))]
    (fn []
      (let [{:keys [count]} @tab-1-state]
        [:div {:ref ref-fn}
         [:h2 "Tab 1"]
         [:p "Interval tick: " count]
         [:button {:on-click #(reset! tab-1-state {:count 0})}
          "Restart"]]))))

(defonce tab-2-state (r/atom {:tick 0 :last-key nil}))

(def tab-2-component
  (let [interval-id (atom nil)
        keydown-fn (fn [e]
                     (swap! tab-2-state assoc :last-key (.-key e)))
        ref-fn (fn [el]
                 ;(js/console.log "tab-2 ref" (if el (.toString el) "nil"))
                 (if el
                   (when (nil? @interval-id)
                     ;(js/console.log "tab-2 mount: starting timer")
                     (reset! interval-id
                             (js/setInterval
                               #(swap! tab-2-state update :tick inc)
                               500))
                     (.addEventListener js/window "keydown" keydown-fn))
                   (when @interval-id
                     ;(js/console.log "tab-2 unmount: clearing timer")
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil)
                     (.removeEventListener js/window "keydown" keydown-fn))))]
    (fn []
      (let [{:keys [tick last-key]} @tab-2-state]
        [:div {:ref ref-fn}
         [:h2 "Tab 2"]
         [:p "Interval tick: " tick]
         [:p "Last key pressed: " (or last-key "none")]
         [:button {:on-click #(reset! tab-2-state {:tick 0 :last-key nil})}
          "Restart"]]))))

(defn tab-0-component []
  [:p "Hello"])

(defn app []
  (let [page (:page @app-state)]
    [:div
     [:nav
      (for [[kw label] [[:tab-0 "Tab 0"] [:tab-1 "Tab 1"] [:tab-2 "Tab 2"]]]
        ^{:key kw}
        [nav-link kw label])]
     [:hr]
     (case page
       :tab-0 [tab-0-component]
       :tab-1 [tab-1-component]
       :tab-2 [tab-2-component]
       [:div "Unknown tab"])]))

(defn sleep [ms]
  (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

(defn restart-buttons [container]
  (.querySelectorAll container "button"))

(defn restart-button-count [container]
  (.-length (restart-buttons container)))

(defn assert-single-restart! [container]
  (let [count (restart-button-count container)]
    ;(js/console.log "TEST assert restart count" count)
    (th/assert-equal count 1)
    (let [btn (aget (restart-buttons container) 0)]
      ;(js/console.log "TEST assert restart text" (when btn (.-textContent btn)))
      (th/assert-not-nil btn)
      (th/assert-equal (.-textContent btn) "Restart"))))

(defn find-nav-link [container label]
  (let [links (.querySelectorAll container "nav a")
        len (.-length links)]
    (loop [idx 0]
      (when (< idx len)
        (let [el (.item links idx)]
          (if (= (.-textContent el) label)
            el
            (recur (inc idx))))))))

(defn click-nav! [container label]
  (when-let [link (find-nav-link container label)]
    (.click link)))

(describe "Timers and Restart buttons"
  (fn []
    (it "should keep exactly one Restart button while switching tabs"
      (fn []
        ;(js/console.log "TEST begin timers_and_buttons")
        (reset! app-state {:page :tab-0})
        (reset! tab-1-state {:count 0})
        (reset! tab-2-state {:tick 0 :last-key nil})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [app] container)
          (-> (sleep 10)
              (.then (fn []
                       ;(js/console.log "TEST step -> Tab 1")
                       (click-nav! container "Tab 1")
                       (sleep 10)))
              (.then (fn []
                       ;(js/console.log "TEST verify Tab 1")
                       (assert-single-restart! container)
                       ;(js/console.log "TEST step -> Tab 2")
                       (click-nav! container "Tab 2")
                       (sleep 10)))
              (.then (fn []
                       ;(js/console.log "TEST verify Tab 2")
                       (assert-single-restart! container)
                       ;(js/console.log "TEST step -> Tab 0")
                       (click-nav! container "Tab 0")
                       (sleep 10)))
              (.then (fn []
                       ;(js/console.log "TEST step -> Tab 2 (again)")
                       (click-nav! container "Tab 2")
                       (sleep 10)))
              (.then (fn []
                       ;(js/console.log "TEST verify Tab 2 after return")
                       (assert-single-restart! container)
                       ;(js/console.log "TEST step -> Tab 1 final")
                       (click-nav! container "Tab 1")
                       (sleep 10)))
              (.then (fn []
                       ;(js/console.log "TEST final verify Tab 1")
                       (assert-single-restart! container)))))))))

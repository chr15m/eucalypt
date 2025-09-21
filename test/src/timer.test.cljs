(ns timer.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(defn sleep [ms]
  (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

(def timer-state (r/atom {:seconds 0}))

(def timer-component
  (let [timer-id (atom nil)
        ref-fn (fn [el]
                 (if el
                   (do
                     (js/console.log "Setting up timer")
                     (reset! timer-id (js/setInterval #(swap! timer-state update :seconds inc) 100)))
                   (do
                     (js/console.log "Tearing down timer")
                     (js/clearInterval @timer-id))))]
    (fn []
      [:div
       [:p "Seconds: " (:seconds @timer-state)]
       [:div {:ref ref-fn}]])))

(def show-timer (r/atom true))

(defn main-component []
  [:div
   (if @show-timer
     [timer-component]
     [:p "Timer is hidden"])
   [:button {:id "toggle"
             :on-click #(swap! show-timer not)} "Toggle Timer"]])

(describe "Timer Component"
          (fn []
            (it "should start and stop timer correctly"
                (fn []
                  (reset! timer-state {:seconds 0})
                  (reset! show-timer true)
                  (let [container (.createElement js/document "div")]
                    (.appendChild js/document.body container)
                    (r/render [main-component] container)

                    (th/assert-equal (.-textContent (.querySelector container "p")) "Seconds: 0")

                    (-> (sleep 250)
                        (.then (fn []
                                 (let [p (.querySelector container "p")]
                                   (th/assert-equal
                                     (.-textContent p)
                                     "Seconds: 2"))))
                        (.then (fn []
                                 ;; Hide timer
                                 (.click (.querySelector container "#toggle"))))
                        (.then (fn []
                                 (th/assert-equal
                                   (.-textContent (.querySelector container "p"))
                                   "Timer is hidden")
                                 (sleep 150)))
                        (.then (fn []
                                 ;; The timer was stopped, so the count should still be 2
                                 (th/assert-equal
                                   (:seconds @timer-state)
                                   2)
                                 ;; Show timer again
                                 (.click (.querySelector container "#toggle"))))
                        (.then (fn []
                                 (th/assert-equal
                                   (.-textContent (.querySelector container "p"))
                                   "Seconds: 2")
                                 (sleep 250)))
                        (.then (fn []
                                 (th/assert-equal
                                   (.-textContent (.querySelector container "p"))
                                   "Seconds: 4")))))))))

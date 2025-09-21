(ns fragment-clickable.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(def app-state (r/atom {:slots (vec (repeat 15 nil))}))

(defn handle-click []
  (swap! app-state
         (fn [current-state]
           (let [slots (:slots current-state)]
             (if (some nil? slots)
               (let [slot-index (.indexOf slots nil)]
                 (assoc-in current-state [:slots slot-index] "🪙"))
               current-state)))))

(defn clickable-fragments-page []
  [:section {:id "clickable-section"
             :style {:cursor "pointer"}
             :on-click handle-click}
   [:div {:class "slots"}
    (let [slots-vec (:slots @app-state)
          slots-per-row 5
          rows (partition-all slots-per-row slots-vec)]
      (map-indexed
       (fn [row-idx row]
         [:div {:class "slot-row" :key row-idx}
          (let [row-vec (vec row)
                n (count row-vec)
                middle (/ (dec n) 2.0)]
            (map-indexed
             (fn [col-idx slot]
               (let [dist (js/Math.abs (- col-idx middle))
                     y-offset (* dist dist 0.2)
                     style {:transform (str "translateY(" y-offset "em)")}]
                 (if slot
                   [:span
                    {:class "emoji filled coin" :style style :key col-idx}
                    slot]
                   [:span
                    {:class "emoji slot" :style style :key col-idx}
                    "⚪"])))
             row-vec))])
       rows))]])

(describe "Clickable Fragment"
  (fn []
    (it "should convert slots to coins on click"
      (fn []
        (reset! app-state {:slots (vec (repeat 15 nil))})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [clickable-fragments-page] container)

          (let [section (.querySelector container "#clickable-section")
                get-coins #(.querySelectorAll container ".coin")
                get-slots #(.querySelectorAll container ".slot")]

            ;; Initial state
            (th/assert-equal (.-length (get-coins)) 0)
            (th/assert-equal (.-length (get-slots)) 15)

            ;; Click once
            (.click section)
            (th/assert-equal (.-length (get-coins)) 1)
            (th/assert-equal (.-length (get-slots)) 14)

            ;; Click five more times
            (dotimes [_ 5] (.click section))
            (th/assert-equal (.-length (get-coins)) 6)
            (th/assert-equal (.-length (get-slots)) 9)

            ;; Click until all are coins
            (dotimes [_ 9] (.click section))
            (th/assert-equal (.-length (get-coins)) 15)
            (th/assert-equal (.-length (get-slots)) 0)

            ;; Click again (should have no effect)
            (.click section)
            (th/assert-equal (.-length (get-coins)) 15)
            (th/assert-equal (.-length (get-slots)) 0)))))))

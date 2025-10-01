(ns games
  (:require
    [eucalypt :as r]
    ["es-toolkit" :refer [isEqual]]))

(def app-state (r/atom {:page :home}))

;;; --- Tic Tac Toe ---
(defn ttt-new-game []
  {:board (vec (repeat 9 nil))
   :x-turn? true})

(defonce ttt-state (r/atom (ttt-new-game)))

(defn ttt-winner [board]
  (let [lines [[0 1 2] [3 4 5] [6 7 8]
               [0 3 6] [1 4 7] [2 5 8]
               [0 4 8] [2 4 6]]]
    (some (fn [[a b c]]
            (when (and (get board a)
                       (= (get board a) (get board b) (get board c)))
              (get board a)))
          lines)))

(defn ttt-square [i]
  (let [{:keys [board x-turn?]} @ttt-state
        mark (get board i)]
    [:div {:style {:width "60px" :height "60px"
                   :border "1px solid #000"
                   :display "flex" :align-items "center"
                   :justify-content "center"
                   :font-size "24px"
                   :cursor "pointer"}
           :on-click #(when (and (nil? mark) (not (ttt-winner board)))
                        (swap! ttt-state update :board assoc i (if x-turn? "X" "O"))
                        (swap! ttt-state update :x-turn? not))}
     mark]))

(defn ttt-board-view []
  [:div {:style {:display "grid"
                 :grid-template-columns "repeat(3, 60px)"
                 :grid-template-rows "repeat(3, 60px)"
                 :gap "2px"}}
   (for [i (range 9)]
     ^{:key i} [ttt-square i])])

(defn tic-tac-toe-game []
  (let [{:keys [board x-turn?]} @ttt-state
        w (ttt-winner board)]
    [:div {:style {:font-family "sans-serif"}}
     [ttt-board-view]
     (cond
       w [:p (str "Winner: " w)]
       (every? some? board) [:p "Draw!"]
       :else [:p (str "Next turn: " (if x-turn? "X" "O"))])
     [:button {:on-click #(reset! ttt-state (ttt-new-game))}
      "Restart"]]))


;;; --- Snake ---
(def snake-size 20)    ;; grid size (20x20)
(def snake-cell 10)    ;; each cell = 10px

(defonce snake-state
  (r/atom {:snake [[5 10] [4 10] [3 10]] ; initial snake body
           :dir [1 0]                    ; moving right
           :food [15 10]                 ; initial food
           :alive? true}))

(defn snake-random-food []
  [(rand-int snake-size) (rand-int snake-size)])

(defn move-snake []
  (swap! snake-state
         (fn [{:keys [snake dir food alive?] :as st}]
           (if (not alive?)
             st
             (let [head (vec [(+ (nth (first snake) 0) (nth dir 0))
                              (+ (nth (first snake) 1) (nth dir 1))])
                   ate? (isEqual head food)
                   new-snake (vec (cons head (if ate? snake (butlast snake))))
                   x (nth head 0)
                   y (nth head 1)
                   hit-wall? (or (< x 0) (< y 0)
                                 (>= x snake-size) (>= y snake-size))
                   hit-self? (some #(isEqual head %) (rest new-snake))]
               (cond
                 hit-wall? (assoc st :alive? false)
                 hit-self? (assoc st :alive? false)
                 ate?      (assoc st :snake new-snake :food (snake-random-food))
                 :else     (assoc st :snake new-snake)))))))

(defn handle-keydown [e]
  (let [key (.-key e)]
    (swap! snake-state update :dir
           (fn [dir]
             (case key
               "ArrowUp"    (if (isEqual dir [0 1]) dir [0 -1])
               "ArrowDown"  (if (isEqual dir [0 -1]) dir [0 1])
               "ArrowLeft"  (if (isEqual dir [1 0]) dir [-1 0])
               "ArrowRight" (if (isEqual dir [-1 0]) dir [1 0])
               dir)))))

(defn snake-cell-rect [[x y] color]
  [:rect {:x (* x snake-cell) :y (* y snake-cell)
          :width snake-cell :height snake-cell
          :fill color}])

(def snake-game-board
  (let [interval-id (r/atom nil)
        ref-fn (fn [el]
                 (if el
                   (when (nil? @interval-id)
                     (reset! interval-id (js/setInterval move-snake 200))
                     (.addEventListener js/window "keydown" handle-keydown))
                   (when @interval-id
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil)
                     (.removeEventListener js/window "keydown" handle-keydown))))]
    (fn []
      (let [{:keys [snake food alive?]} @snake-state]
        [:div {:ref ref-fn}
         [:svg {:width (* snake-size snake-cell) :height (* snake-size snake-cell)
                :style {:border "1px solid black"
                        :background "#eef"}}
          ;; snake
          (for [part snake]
            ^{:key (str part)} [snake-cell-rect part "green"])
          ;; food
          [snake-cell-rect food "red"]
          ;; game over overlay
          (when (not alive?)
            [:text {:x 50 :y 100 :font-size 20 :fill "black"} "Game Over!"])]
         [:p
          [:button {:on-click #(reset! snake-state {:snake [[5 10] [4 10] [3 10]]
                                                    :dir [1 0]
                                                    :food [15 10]
                                                    :alive? true})} "Restart"]]]))))

;;; --- Main App ---
(defn nav-link [page-kw text]
  [:a {:href "#"
       :class "nav-link"
       :on-click (fn [e]
                   (.preventDefault e)
                   (swap! app-state assoc :page page-kw))}
   text])

(defn home-page []
  [:div
   [:h1 "Games"]
   [:p "Some games implemented with Eucalypt."]])

(defn app []
  (let [page (:page @app-state)]
    [:div
     [:nav
      [nav-link :home "Home"]
      [nav-link :tic-tac-toe "Tic Tac Toe"]
      [nav-link :snake "Snake"]]
     [:hr]
     (case page
       :home [home-page]
       :tic-tac-toe [tic-tac-toe-game]
       :snake [snake-game-board]
       [:div "Page not found"])]))

(r/render [app] (.getElementById js/document "app"))

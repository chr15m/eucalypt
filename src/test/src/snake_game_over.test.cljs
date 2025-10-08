(ns snake-game-over.test
  (:require ["vitest" :refer [describe it]]
            [eucalypt :as r]
            [helpers :as th]))

(defn sleep [ms]
  (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

(def size 20)
(def cell 10)

(defn random-food []
  [(rand-int size) (rand-int size)])

(defn new-state []
  {:snake [[5 10] [4 10] [3 10]]
   :dir [1 0]
   :food [15 10]
   :alive? true})

(defonce snake-state (r/atom (new-state)))

(defn reset-snake-state []
  (reset! snake-state (new-state)))

(defn move-snake []
  (swap! snake-state
         (fn [{:keys [snake dir food alive?] :as st}]
           (if (not alive?)
             st
             (let [[hx hy] (first snake)
                   [dx dy] dir
                   new-head [(+ hx dx) (+ hy dy)]
                   ate? (= new-head food)
                   new-snake (vec (cons new-head (if ate? snake (butlast snake))))
                   [nx ny] new-head
                   hit-wall? (or (< nx 0) (>= nx size) (< ny 0) (>= ny size))
                   hit-self? (some #(= new-head %) (rest new-snake))]
               (cond
                 hit-wall? (assoc st :alive? false)
                 hit-self? (assoc st :alive? false)
                 ate? (assoc st :snake new-snake :food (random-food))
                 :else (assoc st :snake new-snake)))))))

(defonce snake-interval (js/setInterval move-snake 20))

(defonce keydown-listener
  (.addEventListener js/window "keydown"
                     (fn [e]
                       (let [key (.-key e)]
                         (swap! snake-state update :dir
                                (fn [dir]
                                  (case key
                                    "ArrowUp" (if (= dir [0 1]) dir [0 -1])
                                    "ArrowDown" (if (= dir [0 -1]) dir [0 1])
                                    "ArrowLeft" (if (= dir [1 0]) dir [-1 0])
                                    "ArrowRight" (if (= dir [-1 0]) dir [1 0])
                                    dir)))))))

(defn cell-rect [[x y] color]
  [:rect {:x (* x cell)
          :y (* y cell)
          :width cell
          :height cell
          :fill color}])

(defn game-board []
  (let [{:keys [snake food alive?]} @snake-state]
    [:svg {:width (* size cell)
           :height (* size cell)
           :style {:border "1px solid black"
                   :background "#eef"}}
     (for [segment snake]
       ^{:key (str segment)}
       [cell-rect segment "green"])
     [cell-rect food "red"]
     (when-not alive?
       [:text {:x 50 :y 100 :font-size 20 :fill "black"} "Game Over!"])]))

(defonce container
  (let [el (.createElement js/document "div")]
    (.appendChild js/document.body el)
    el))

(describe "Snake demo"
  (fn []
    (it "should show Game Over when the snake hits the wall"
      (fn []
        (reset-snake-state)
        (let [initial-child-count (atom nil)]
          (r/render [game-board] container)
          (-> (sleep 1)
              (.then (fn []
                       (let [svg (.querySelector container "svg")]
                         (th/assert-not-nil svg)
                         (let [child-count (.-length (.-childNodes svg))]
                           (reset! initial-child-count child-count))
                         (th/assert-equal (:alive? @snake-state) true)
                         (th/assert-equal (.-textContent svg) ""))))
              (.then (fn [] (sleep 1)))
              (.then (fn []
                       (let [svg (.querySelector container "svg")]
                         (th/assert-not-nil svg)
                         (th/assert-equal (:alive? @snake-state) true)
                         (th/assert-equal (.-textContent svg) ""))))
              (.then (fn [] (sleep 350)))
              (.then (fn []
                       (let [svg (.querySelector container "svg")]
                         (th/assert-not-nil svg)
                         (th/assert-equal (:alive? @snake-state) false)
                         (let [text-el (.querySelector svg "text")
                               final-child-count (.-length (.-childNodes svg))]
                           (th/assert-not-nil text-el)
                           (when (some? @initial-child-count)
                             (th/assert-equal (> final-child-count @initial-child-count) true))
                           (th/assert-equal (.-localName text-el) "text")
                           (th/assert-equal (.-namespaceURI text-el) "http://www.w3.org/2000/svg")
                           (th/assert-equal (.-ownerSVGElement text-el) svg)
                           (th/assert-equal (.getAttribute text-el "fill") "black")
                           (th/assert-equal (.-textContent text-el) "Game Over!")
                           (let [check-visibility (aget text-el "checkVisibility")]
                             (when check-visibility
                               (th/assert-equal (.call check-visibility text-el) true))))
                         (th/assert-equal (.-textContent svg) "Game Over!")))))))))) 

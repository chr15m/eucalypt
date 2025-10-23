(ns keyed-list-reordering.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]
            [clojure.string :as str]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

;;; --- Simple list reordering ---

(def list-data (r/atom []))

(defn list-component []
  [:ul
   (for [item @list-data]
     ^{:key item}
     [:li item])])

(defn move [v from to]
  (let [item (nth v from)
        temp (vec (concat (subvec v 0 from) (subvec v (inc from))))]
    (vec (concat (subvec temp 0 to) [item] (subvec temp to)))))

(describe "Keyed list reordering"
  (fn []
    (it "should move keyed children"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [list-component] container)

          ;; Move to beginning
          (reset! list-data ["b" "c" "a"])
          (th/assert-equal (.-textContent container) "bca")
          (reset! list-data (move @list-data 2 0))
          (th/assert-equal (.-textContent container) "abc")

          ;; Swap
          (reset! list-data ["a" "b"])
          (th/assert-equal (.-textContent container) "ab")
          (reset! list-data ["b" "a"])
          (th/assert-equal (.-textContent container) "ba")

          ;; Move to end
          (reset! list-data ["a" "b" "c" "d"])
          (th/assert-equal (.-textContent container) "abcd")
          (reset! list-data (move @list-data 0 3))
          (th/assert-equal (.-textContent container) "bcda"))))

    (it "should reverse keyed children"
      (fn []
        (let [values (vec (map str (range 10)))
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (reset! list-data values)
          (r/render [list-component] container)
          (th/assert-equal (.-textContent container) (str/join "" values))

          (reset! list-data (vec (reverse @list-data)))
          (th/assert-equal (.-textContent container) (str/join "" (reverse values))))))

    (it "should handle full reorders (sorting)"
      (fn []
        (let [initial-data ["Apple" "Grape" "Cherry" "Orange" "Banana"]
              container (.createElement js/document "div")]
          (reset! list-data initial-data)
          (.appendChild js/document.body container)
          (r/render [list-component] container)
          (th/assert-equal (.-textContent container) "AppleGrapeCherryOrangeBanana")

          ;; Sort ascending
          (reset! list-data (vec (sort @list-data)))
          (th/assert-equal (.-textContent container) "AppleBananaCherryGrapeOrange")

          ;; Sort descending
          (reset! list-data (vec (sort-by identity (comp - (partial compare)) @list-data)))
          (th/assert-equal (.-textContent container) "OrangeGrapeCherryBananaApple"))))

    (it "should handle shuffled child ordering"
      (fn []
        (let [a ["0" "1" "2" "3" "4" "5" "6"]
              b ["1" "3" "5" "2" "6" "4" "0"]
              c ["11" "3" "1" "4" "6" "2" "5" "0" "9" "10"]
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (reset! list-data a)
          (r/render [list-component] container)
          (th/assert-equal (.-textContent container) (str/join "" a))

          (reset! list-data b)
          (th/assert-equal (.-textContent container) (str/join "" b))

          (reset! list-data c)
          (th/assert-equal (.-textContent container) (str/join "" c))

          (reset! list-data a)
          (th/assert-equal (.-textContent container) (str/join "" a)))))))

;;; --- Stateful list reordering ---

(defn stateful-item [id]
  (let [counter (r/atom 0)]
    (fn [id]
      [:li {:id (str "item-" id)}
       "Item " id ", count: " @counter
       [:button {:on-click #(swap! counter inc)} "Inc"]])))

(def reorder-state (r/atom ["A" "B"]))

(defn reorderable-list []
  [:div
   (for [id @reorder-state]
     (with-meta [stateful-item id] {:key id}))])

(describe "Keyed stateful list reordering"
  (fn []
    (it "should preserve state when moving keyed children components"
      (fn []
        (reset! reorder-state ["A" "B"])
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [reorderable-list] container)

          (let [get-item-a #(.querySelector container "#item-A")
                get-item-b #(.querySelector container "#item-B")]

            ;; Initial state
            (th/assert-equal (.-textContent (get-item-a)) "Item A, count: 0Inc")
            (th/assert-equal (.-textContent (get-item-b)) "Item B, count: 0Inc")

            ;; Click A's button
            (.click (.querySelector (get-item-a) "button"))
            (th/assert-equal (.-textContent (get-item-a)) "Item A, count: 1Inc")
            (th/assert-equal (.-textContent (get-item-b)) "Item B, count: 0Inc")

            ;; Reorder
            (reset! reorder-state ["B" "A"])

            ;; Check state is preserved
            (th/assert-equal (.-textContent (get-item-a)) "Item A, count: 1Inc")
            (th/assert-equal (.-textContent (get-item-b)) "Item B, count: 0Inc")

            ;; Check order in DOM
            (let [items (.querySelectorAll container "li")]
              (th/assert-equal (.-id (aget items 0)) "item-B")
              (th/assert-equal (.-id (aget items 1)) "item-A"))))))))

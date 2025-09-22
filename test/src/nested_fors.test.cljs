(ns nested-fors.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def slots
  {"🐈‍⬛" [nil]
   "🥔" [nil nil nil]
   "🥕" [nil nil nil nil nil]
   "🍅" [nil nil nil nil nil nil nil]
   "🐇" [nil nil nil nil nil nil nil nil nil]
   "🌳" [nil nil nil nil nil nil nil nil nil nil nil nil]})

(defn component-row [row-vec middle]
  (map-indexed
    (fn [col-idx slot]
      (let [dist (js/Math.abs (- col-idx middle))
            y-offset (* dist dist 0.2)
            style {:transform (str "translateY(" y-offset "em)")}]
        (if slot
          [:span
           {:class "emoji filled" :style style :key col-idx}
           "🪙"]
          [:span
           {:class "emoji" :style style :key col-idx}
           "⚪"])))
    row-vec))

(defn emoji-test []
  (for [emoji (keys slots)]
    ^{:key emoji}
    [:section {:class "slide big"}
     [:div {:class "slots"}
      (let [slots-vec (get slots emoji)
            slots-per-row 5
            rows (partition-all slots-per-row slots-vec)]
        (map-indexed
          (fn [row-idx row]
            [:div {:class "slot-row" :key row-idx}
             (let [row-vec (vec row)
                   n (count row-vec)
                   middle (/ (dec n) 2.0)]
               [component-row row-vec middle])])
          rows))]
     [:span {:class "emoji"} emoji]]))

(describe
  "For loops"
  (fn []
    (it "should render a row of spans"
        (fn []
          (let [container (.createElement js/document "div")]
            (.appendChild js/document.body container)
            (r/render [component-row [nil nil nil nil] 1.5] container)
            (th/log "Container HTML:" (.-innerHTML container))
            (let [spans (.querySelectorAll container "span")]
              (th/assert-equal (.-length spans) 4)))))
    (it "should render another row of spans"
        (fn []
          (let [container (.createElement js/document "div")]
            (.appendChild js/document.body container)
            (r/render [component-row [nil nil nil nil nil] 2.0] container)
            (th/log "Container HTML:" (.-innerHTML container))
            (let [spans (.querySelectorAll container "span")]
              (th/assert-equal (.-length spans) 5)))))
    (it "should render nested fors correctly"
        (fn []
          (let [container (.createElement js/document "div")]
            (.appendChild js/document.body container)
            (r/render [emoji-test] container)
            (let [sections (.querySelectorAll container "section")]
              (th/assert-equal (.-length sections) 6))
            (let [emojis (.querySelectorAll container ".emoji")]
              ;; 37 slots + 6 section emojis
              (th/assert-equal (.-length emojis) 43)))))))


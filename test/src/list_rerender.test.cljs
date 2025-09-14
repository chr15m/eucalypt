(ns list-rerender.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def list-data (r/ratom [{:id 1 :text "A"}
                         {:id 2 :text "B"}
                         {:id 3 :text "C"}]))

;; A stateful component with its own local state
(defn list-item []
  (let [selected? (r/ratom false)]
    (fn [{:keys [id text]}]
      [:li {:id (str "item-" id)
            :class (when @selected? "selected")}
       "Item: " text " "
       [:button {:on-click #(swap! selected? not)} "Select"]])))

(defn main-component []
  [:div
   [:ul
    (for [item @list-data]
      ^{:key (:id item)} [list-item item])]
   [:button {:id "remove-B"
             :on-click #(swap! list-data (fn [items] (vec (remove (fn [i] (= (:id i) 2)) items))))}
    "Remove B"]])

(describe "List re-rendering"
  (fn []
    (it "should maintain independent state for items in a for-loop"
      (fn []
        (reset! list-data [{:id 1 :text "A"} {:id 2 :text "B"} {:id 3 :text "C"}])
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [main-component] container)

          (let [item1 (.querySelector container "#item-1")
                item2 (.querySelector container "#item-2")
                item3 (.querySelector container "#item-3")
                select-btn1 (.querySelector item1 "button")]

            ;; Initial state: no items selected
            (th/assert-equal (.contains (.-classList item1) "selected") false)
            (th/assert-equal (.contains (.-classList item2) "selected") false)
            (th/assert-equal (.contains (.-classList item3) "selected") false)

            ;; Click to select the first item
            (.click select-btn1)

            ;; Assert only the first item is selected
            (th/assert-equal (.contains (.-classList item1) "selected") true)
            (th/assert-equal (.contains (.-classList item2) "selected") false)
            (th/assert-equal (.contains (.-classList item3) "selected") false))))) 

    (it "should correctly re-render when an item is removed"
      (fn []
        (reset! list-data [{:id 1 :text "A"} {:id 2 :text "B"} {:id 3 :text "C"}])
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [main-component] container)

          (let [item1 (.querySelector container "#item-1")
                select-btn1 (.querySelector item1 "button")
                remove-btn (.querySelector container "#remove-B")]

            ;; Select the first item
            (.click select-btn1)
            (th/assert-equal (.contains (.-classList item1) "selected") true)

            ;; Remove the second item
            (.click remove-btn)

            ;; Assert list is shorter and item 1 is still selected
            (th/assert-equal (.-length (.querySelectorAll container "li")) 2)
            (let [item1-after-remove (.querySelector container "#item-1")]
              (th/assert-not-nil item1-after-remove)
              (th/assert-equal (.contains (.-classList item1-after-remove) "selected") true))
            (th/assert-equal (.querySelector container "#item-2") nil)))))))

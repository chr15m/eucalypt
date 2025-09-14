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
  (js/console.log "list-item outer function called")
  (let [selected? (r/ratom false)]
    (js/console.log "list-item created new ratom:" selected?)
    (fn [{:keys [id text]}]
      (js/console.log "list-item inner function called for id:" id "selected?:" @selected?)
      [:li {:id (str "item-" id)
            :class (when @selected? "selected")}
       "Item: " text " "
       [:button {:on-click #(do
                              (js/console.log "Select button clicked for id:" id "current selected?:" @selected?)
                              (swap! selected? not)
                              (js/console.log "After toggle, selected?:" @selected?))} "Select"]])))

(defn main-component []
  [:div
   [:ul
    (for [item @list-data]
      (do
        (js/console.log "main-component: rendering item with id:" (:id item) "and key:" (:id item))
        (with-meta [list-item item] {:key (:id item)})))]
   [:button {:id "remove-B"
             :on-click #(do
                          (js/console.log "Remove B clicked, list before:" @list-data)
                          (swap! list-data (fn [items] (vec (remove (fn [i] (= (:id i) 2)) items))))
                          (js/console.log "Remove B clicked, list after:" @list-data))}
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

            ;; Debug: log what we found
            (js/console.log "Test: found item1?" (some? item1))
            (js/console.log "Test: found item2?" (some? item2))
            (js/console.log "Test: found item3?" (some? item3))

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
            (js/console.log "Test: clicking select button for item 1")
            (.click select-btn1)
            (th/assert-equal (.contains (.-classList item1) "selected") true)

            ;; Remove the second item
            (js/console.log "Test: clicking remove button")
            (.click remove-btn)

            ;; Assert list is shorter and item 1 is still selected
            (th/assert-equal (.-length (.querySelectorAll container "li")) 2)
            (let [item1-after-remove (.querySelector container "#item-1")]
              (js/console.log "Test: item1 after remove exists?" (some? item1-after-remove))
              (js/console.log "Test: item1 after remove selected?" (when item1-after-remove (.contains (.-classList item1-after-remove) "selected")))
              (th/assert-not-nil item1-after-remove)
              (th/assert-equal (.contains (.-classList item1-after-remove) "selected") true))
            (th/assert-equal (.querySelector container "#item-2") nil)))))))

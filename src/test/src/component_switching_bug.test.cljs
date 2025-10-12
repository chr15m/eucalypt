(ns component-switching-bug.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(defn sleep [ms]
  (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

;; --- Test components and state ---

(def page-state (r/atom :fragments))
(def clock-ratom (r/atom 0))
(def list-component-state (r/atom {:show? true}))

;; This interval is intentionally not cleared to replicate the bug
(defonce _ (js/setInterval #(swap! clock-ratom inc) 50))

(defn fragments-header []
  [:h1 "Fragments Page"])

(defn fragments-component []
  [:<>
   [fragments-header]
   (when (:show? @list-component-state)
     [:div {:id "fragments-root"}
      (for [_ [1 2 3]]
        [:section
         [:div
          (let [slots-vec [1 2 3 4 5]
                rows (partition-all 5 slots-vec)]
            (map-indexed
             (fn [row-idx row]
               ^{:key row-idx}
               [:div {:class "slot-row"}
                (let [row-vec (vec row)]
                  (map (fn [slot]
                         (if slot
                           [:span "🪙"]
                           [:span "⚪"]))
                       row-vec))])
             rows))]])])
   [:button {:on-click #(swap! list-component-state update :show? not)} "Toggle"]])

(defn clock-display []
  (let [val @clock-ratom]
    [:p "Time: " val]))

(defn clock-component []
  [:div {:id "clock-root"}
   [:h1 "Clock Page"]
   [clock-display]])

(defn main-app []
  [:div
   (case @page-state
     :fragments [fragments-component]
     :clock [clock-component])])

;; --- Test ---

(describe "Component switching bug"
  (fn []
    (it "should not duplicate nodes when switching back from a component with a global ratom"
      (fn []
        (reset! page-state :fragments)
        (reset! clock-ratom 0)
        (reset! list-component-state {:show? true})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (th/log "--- Test start ---")
          (r/render [main-app] container)

          ;; 1. Initial render is fragments component
          (th/log "--- Step 1: Initial render ---")
          (th/log "HTML:" (.-innerHTML container))
          (th/assert-equal (.-length (.querySelectorAll container "h1")) 1)
          (th/assert-equal (.-length (.querySelectorAll container "section")) 3)
          (th/assert-equal (.-length (.querySelectorAll container "span")) 15)

          ;; 2. Switch to clock component
          (th/log "--- Step 2: Switching to clock ---")
          (reset! page-state :clock)

          (-> (sleep 10)
              (.then (fn []
                       (th/log "--- After switching to clock ---")
                       (th/log "HTML:" (.-innerHTML container))
                       (th/assert-not-nil (.querySelector container "#clock-root"))
                       (th/assert-equal (.querySelector container "#fragments-root") nil)

                       ;; 3. Switch back to fragments component
                       (th/log "--- Step 3: Switching back to fragments ---")
                       (reset! page-state :fragments)))
              (.then (fn [] (sleep 10)))
              (.then (fn []
                       (th/log "--- After switching back to fragments ---")
                       (th/log "HTML:" (.-innerHTML container))
                       (th/assert-not-nil (.querySelector container "#fragments-root"))
                       (th/assert-equal (.querySelector container "#clock-root") nil)
                       (th/assert-equal (.-length (.querySelectorAll container "h1")) 1)
                       (th/assert-equal (.-length (.querySelectorAll container "section")) 3)
                       (th/assert-equal (.-length (.querySelectorAll container "span")) 15)

                       ;; 4. Wait for the clock-ratom to update and trigger the rogue watcher
                       (th/log "--- Step 4: Waiting for rogue watcher ---")))
              (.then (fn [] (sleep 100)))
              (.then (fn []
                       (th/log "--- Step 5: Checking for duplication ---")
                       (th/log "HTML:" (.-innerHTML container))
                       ;; 5. Assert that nodes are not duplicated
                       (th/assert-equal (.-length (.querySelectorAll container "h1")) 1
                                        "H1 should not be duplicated after switching")
                       (th/assert-equal (.-length (.querySelectorAll container "section")) 3
                                        "Sections should not be duplicated after switching")
                       (th/assert-equal (.-length (.querySelectorAll container "span")) 15
                                        "Spans should not be duplicated after switching"))))))))) 

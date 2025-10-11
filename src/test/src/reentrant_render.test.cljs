(ns reentrant-render.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(defn child-counter [label]
  (let [clicks (r/atom 0)]
    (fn []
      [:div {:class (str "child child-" label)}
       [:span {:class (str "count-" label)} @clicks]
       [:button {:class (str "inc-" label)
                 :on-click #(swap! clicks inc)}
        (str "Increment " label)]])))

(defn parent-with-toggle [label]
  (let [toggled? (r/atom false)]
    (fn []
      [:section {:class (str "parent parent-" label)}
       [:h2 (str label ": " (if @toggled? "on" "off"))]
       [child-counter label]
       [:button {:class (str "toggle-" label)
                 :on-click #(swap! toggled? not)}
        (str "Toggle " label)]])))

(describe "Re-entrant rendering"
  (fn []
    (it "should keep child local state when another root renders"
      (fn []
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [parent-with-toggle "Alpha"] container-a)

          (let [count-a #(.-textContent (.querySelector container-a ".count-Alpha"))
                inc-a (.querySelector container-a ".inc-Alpha")
                toggle-a (.querySelector container-a ".toggle-Alpha")]

            ;; Initial state
            (th/assert-equal (count-a) "0")

            ;; Child increments should update its own state
            (.click inc-a)
            (th/assert-equal (count-a) "1")

            ;; Parent re-render should not reset the child
            (.click toggle-a)
            (th/assert-equal (count-a) "1")

            ;; Render a second root instance elsewhere
            (r/render [parent-with-toggle "Beta"] container-b)

            ;; Trigger a parent re-render in the first root again.
            ;; Expectation: child state should remain 1.
            (.click toggle-a)
            (th/assert-equal (count-a) "1")))))))

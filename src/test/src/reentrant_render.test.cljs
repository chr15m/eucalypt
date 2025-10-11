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
    (it "should keep child local state when multiple roots render interleaved"
      (fn []
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")
              container-c (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)
          (.appendChild js/document.body container-c)

          (letfn [(count-text [container label]
                    (.-textContent (.querySelector container (str ".count-" label))))
                  (inc-btn [container label]
                    (.querySelector container (str ".inc-" label)))
                  (toggle-btn [container label]
                    (.querySelector container (str ".toggle-" label)))]
            ;; Root A interactions
            (r/render [parent-with-toggle "Alpha"] container-a)
            (th/assert-equal (count-text container-a "Alpha") "0")
            (.click (inc-btn container-a "Alpha"))
            (th/assert-equal (count-text container-a "Alpha") "1")
            (.click (toggle-btn container-a "Alpha"))
            (th/assert-equal (count-text container-a "Alpha") "1")

            ;; Mount root B and ensure A retains its state
            (r/render [parent-with-toggle "Beta"] container-b)
            (th/assert-equal (count-text container-a "Alpha") "1")
            (th/assert-equal (count-text container-b "Beta") "0")

            ;; Interleave interactions between A and B
            (.click (inc-btn container-b "Beta"))
            (th/assert-equal (count-text container-b "Beta") "1")
            (.click (toggle-btn container-b "Beta"))
            (th/assert-equal (count-text container-a "Alpha") "1")

            (.click (inc-btn container-a "Alpha"))
            (th/assert-equal (count-text container-a "Alpha") "2")
            (.click (toggle-btn container-a "Alpha"))
            (th/assert-equal (count-text container-a "Alpha") "2")
            (th/assert-equal (count-text container-b "Beta") "1")

            ;; Mount root C after A and B have active local state
            (r/render [parent-with-toggle "Gamma"] container-c)
            (th/assert-equal (count-text container-a "Alpha") "2")
            (th/assert-equal (count-text container-b "Beta") "1")
            (th/assert-equal (count-text container-c "Gamma") "0")

            ;; Exercise C while ensuring A and B retain their state
            (.click (inc-btn container-c "Gamma"))
            (th/assert-equal (count-text container-c "Gamma") "1")
            (.click (toggle-btn container-c "Gamma"))
            (th/assert-equal (count-text container-a "Alpha") "2")
            (th/assert-equal (count-text container-b "Beta") "1")
            (th/assert-equal (count-text container-c "Gamma") "1")

            ;; Additional interleaving to ensure stability
            (.click (inc-btn container-b "Beta"))
            (th/assert-equal (count-text container-b "Beta") "2")
            (.click (toggle-btn container-b "Beta"))
            (th/assert-equal (count-text container-a "Alpha") "2")
            (th/assert-equal (count-text container-c "Gamma") "1")))))))

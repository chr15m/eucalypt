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

(def shared-flag (r/atom false))

(defn parent-with-shared-toggle [label]
  (let [local? (r/atom false)]
    (fn []
      [:section {:class (str "parent shared-toggle-" label)}
       [:h2 (str label ": " (if @local? "on" "off"))]
       [child-counter label]
       [:p {:class (str "shared-state-" label)}
        (str "Shared: " (if @shared-flag "on" "off"))]
       [:button {:class (str "toggle-" label)
                 :on-click #(do
                              (swap! local? not)
                              (swap! shared-flag not))}
        (str "Toggle " label)]])))

(defn parent-with-shared-observer [label]
  (let [local? (r/atom false)]
    (fn []
      [:section {:class (str "parent shared-observer-" label)}
       [:h2 (str label ": " (if @local? "on" "off"))]
       [child-counter label]
       [:p {:class (str "shared-state-" label)}
        (str "Shared: " (if @shared-flag "on" "off"))]
       [:button {:class (str "toggle-" label)
                 :on-click #(swap! local? not)}
        (str "Toggle " label)]])))

(def heavy-shared-state (r/atom {:value 0}))

(defn watch-heavy-parent [label]
  (let [local? (r/atom false)
        derived (r/reaction #(str "Derived: " (:value @heavy-shared-state)))]
    (fn []
      [:section {:class (str "parent heavy-" label)}
       [:h2 (str label ": " (if @local? "on" "off"))]
       [child-counter label]
       [:p {:class (str "derived-state-" label)} @derived]
       [:button {:class (str "toggle-" label)
                 :on-click #(swap! local? not)}
        (str "Toggle " label)]
       [:button {:class (str "bump-" label)
                 :on-click #(swap! heavy-shared-state update :value inc)}
        (str "Bump shared for " label)]])))

(defn fragment-parent [label]
  (let [use-fragment? (r/atom true)]
    (fn []
      (let [mode-label (if @use-fragment? "Fragment layout" "Element layout")
            counter (with-meta [child-counter label]
                      {:key (str label "-counter")})
            switch [:button {:class (str "switch-" label)
                             :on-click #(swap! use-fragment? not)}
                    (str "Switch layout for " label)]]
        (if @use-fragment?
          [:<>
           [:p {:class (str "mode-" label)} mode-label]
           counter
           switch]
          [:div {:class (str "div-container-" label)}
           [:p {:class (str "mode-" label)} mode-label]
           counter
           switch])))))

(defn- count-text [container label]
  (.-textContent (.querySelector container (str ".count-" label))))

(defn- inc-btn [container label]
  (.querySelector container (str ".inc-" label)))

(defn- toggle-btn [container label]
  (.querySelector container (str ".toggle-" label)))

(defn- shared-text [container label]
  (.-textContent (.querySelector container (str ".shared-state-" label))))

(defn- bump-btn [container label]
  (.querySelector container (str ".bump-" label)))

(defn- derived-text [container label]
  (.-textContent (.querySelector container (str ".derived-state-" label))))

(defn- switch-btn [container label]
  (.querySelector container (str ".switch-" label)))

(defn- mode-text [container label]
  (.-textContent (.querySelector container (str ".mode-" label))))

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

          (r/render [parent-with-toggle "Alpha"] container-a)
          (th/assert-equal (count-text container-a "Alpha") "0")
          (.click (inc-btn container-a "Alpha"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (.click (toggle-btn container-a "Alpha"))
          (th/assert-equal (count-text container-a "Alpha") "1")

          (r/render [parent-with-toggle "Beta"] container-b)
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "0")

          (.click (inc-btn container-b "Beta"))
          (th/assert-equal (count-text container-b "Beta") "1")
          (.click (toggle-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")

          (.click (inc-btn container-a "Alpha"))
          (th/assert-equal (count-text container-a "Alpha") "2")
          (.click (toggle-btn container-a "Alpha"))
          (th/assert-equal (count-text container-a "Alpha") "2")
          (th/assert-equal (count-text container-b "Beta") "1")

          (r/render [parent-with-toggle "Gamma"] container-c)
          (th/assert-equal (count-text container-a "Alpha") "2")
          (th/assert-equal (count-text container-b "Beta") "1")
          (th/assert-equal (count-text container-c "Gamma") "0")

          (.click (inc-btn container-c "Gamma"))
          (th/assert-equal (count-text container-c "Gamma") "1")
          (.click (toggle-btn container-c "Gamma"))
          (th/assert-equal (count-text container-a "Alpha") "2")
          (th/assert-equal (count-text container-b "Beta") "1")
          (th/assert-equal (count-text container-c "Gamma") "1")

          (.click (inc-btn container-b "Beta"))
          (th/assert-equal (count-text container-b "Beta") "2")
          (.click (toggle-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "2")
          (th/assert-equal (count-text container-c "Gamma") "1"))))

    (it "should preserve state when roots trigger cross renders via shared atoms"
      (fn []
        (reset! shared-flag false)
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [parent-with-shared-toggle "Alpha"] container-a)
          (r/render [parent-with-shared-observer "Beta"] container-b)

          (th/assert-equal (count-text container-a "Alpha") "0")
          (th/assert-equal (count-text container-b "Beta") "0")
          (th/assert-equal (shared-text container-a "Alpha") "Shared: off")
          (th/assert-equal (shared-text container-b "Beta") "Shared: off")

          (.click (inc-btn container-a "Alpha"))
          (.click (inc-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (.click (toggle-btn container-a "Alpha"))
          (th/assert-equal (shared-text container-a "Alpha") "Shared: on")
          (th/assert-equal (shared-text container-b "Beta") "Shared: on")
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (.click (toggle-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")
          (th/assert-equal (shared-text container-a "Alpha") "Shared: on")
          (th/assert-equal (shared-text container-b "Beta") "Shared: on"))))

    (it "should keep other roots intact when one root is unmounted and remounted"
      (fn []
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [parent-with-toggle "Alpha"] container-a)
          (r/render [parent-with-toggle "Beta"] container-b)

          (.click (inc-btn container-a "Alpha"))
          (.click (inc-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (r/render [:p {:class "placeholder"} "Placeholder"] container-b)
          (th/assert-equal (count-text container-a "Alpha") "1")

          (r/render [parent-with-toggle "Beta"] container-b)
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "0"))))

    (it "should handle heavy watcher traffic without disturbing local state"
      (fn []
        (reset! heavy-shared-state {:value 0})
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [watch-heavy-parent "Alpha"] container-a)
          (r/render [watch-heavy-parent "Beta"] container-b)

          (th/assert-equal (derived-text container-a "Alpha") "Derived: 0")
          (th/assert-equal (derived-text container-b "Beta") "Derived: 0")

          (.click (inc-btn container-a "Alpha"))
          (.click (inc-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (.click (bump-btn container-a "Alpha"))
          (th/assert-equal (derived-text container-a "Alpha") "Derived: 1")
          (th/assert-equal (derived-text container-b "Beta") "Derived: 1")
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (.click (toggle-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (.click (bump-btn container-b "Beta"))
          (th/assert-equal (derived-text container-a "Alpha") "Derived: 2")
          (th/assert-equal (derived-text container-b "Beta") "Derived: 2")
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1"))))

    (it "should keep child state when switching fragment and non-fragment roots"
      (fn []
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [fragment-parent "Alpha"] container-a)
          (r/render [parent-with-toggle "Beta"] container-b)

          (th/assert-equal (count-text container-a "Alpha") "0")
          (th/assert-equal (count-text container-b "Beta") "0")
          (th/assert-equal (mode-text container-a "Alpha") "Fragment layout")

          (.click (inc-btn container-a "Alpha"))
          (.click (inc-btn container-b "Beta"))
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (.click (switch-btn container-a "Alpha"))
          (th/assert-equal (mode-text container-a "Alpha") "Element layout")
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1")

          (.click (switch-btn container-a "Alpha"))
          (th/assert-equal (mode-text container-a "Alpha") "Fragment layout")
          (th/assert-equal (count-text container-a "Alpha") "1")
          (th/assert-equal (count-text container-b "Beta") "1"))))))

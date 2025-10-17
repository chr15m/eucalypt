(ns games-fail-case-race.test
  (:require ["vitest" :refer [describe it afterEach]]
            [clojure.string :as str]
            [eucalypt :as r]
            [helpers :as th]))

(def app-state (r/atom {:page :home}))

(afterEach
  (fn []
    (reset! app-state {:page :home})
    (set! (.-innerHTML js/document.body) "")))

(defn sleep [ms]
  (js/Promise. (fn [resolve] (js/setTimeout resolve ms))))

(defn texts-by-selector [container selector]
  (let [nodes (.querySelectorAll container selector)
        len (.-length nodes)]
    (loop [i 0 acc []]
      (if (< i len)
        (recur (inc i) (conj acc (.-textContent (aget nodes i))))
        acc))))

(defn mode-texts [container]
  (->> (texts-by-selector container "p")
       (filter #(str/starts-with? % "Mode "))
       vec))

(defn button-with-text [container text]
  (let [buttons (.querySelectorAll container "button")
        len (.-length buttons)]
    (loop [i 0]
      (when (< i len)
        (let [btn (aget buttons i)]
          (if (= text (.-textContent btn))
            btn
            (recur (inc i))))))))

(defn click-nav! [container idx]
  (let [links (.querySelectorAll container ".nav-link")
        link (aget links idx)]
    (th/assert-not-nil link)
    (.click link)))

(defn log-dom! [label container]
  (let [html (.-innerHTML container)]
    (js/console.log (str label " DOM:") html)
    (th/log label html)))

(defn single-state [container]
  (let [pre-values (texts-by-selector container "pre.app-state")]
    (th/assert-equal (count pre-values) 1)
    (js/JSON.parse (first pre-values))))

;; --- Components under test (mirrors demo/games.cljs) ---

(defn nav-link [page-kw text]
  [:a {:href "#"
       :class "nav-link"
       :on-click (fn [e]
                   (.preventDefault e)
                   (swap! app-state assoc :page page-kw))}
   text])

(defn home-page []
  [:div
   [:h1 "Test"]
   [:p "Failing tests."]])

(defn fail-tick []
  (swap! app-state update :fail-tick (fnil inc 0)))

(defn fail-case-b []
  (let [interval-id (r/atom nil)
        ref-fn (fn [el]
                 (js/console.log "ref" (str el))
                 (if el
                   (when (nil? @interval-id)
                     (reset! interval-id (js/setInterval fail-tick 200)))
                   (when @interval-id
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil))))]
    (fn []
      [:div {:ref ref-fn}
       [:pre {:class "timer-state"} (pr-str (:fail-tick @app-state))]
       [:p "Mode B"]
       [:p
        [:button {:on-click #(swap! app-state dissoc :fail-case)}
         "B button"]]])))

(defn fail-case-a []
  (let [interval-id (r/atom nil)
        ref-fn (fn [el]
                 (js/console.log "ref" (str el))
                 (if el
                   (when (nil? @interval-id)
                     (reset! interval-id (js/setInterval fail-tick 200)))
                   (when @interval-id
                     (js/clearInterval @interval-id)
                     (reset! interval-id nil))))]
    (fn []
      [:div {:ref ref-fn}
       [:pre {:class "timer-state"} (pr-str (:fail-tick @app-state))]
       [:p "Mode A"]
       [:p
        [:button {:on-click #(swap! app-state assoc :fail-case true)}
         "A button"]]
       [:p "Wat A"]])))

(defn fail-case []
  (if (:fail-case @app-state)
    [fail-case-b]
    [fail-case-a]))

(defn fail-case-pre []
  [:p "Wat"])

(defn app []
  (let [page (:page @app-state)]
    [:div
     [:nav
      [nav-link :home "Home"]
      [nav-link :fail-case-pre "Fail case pre"]
      [nav-link :fail-case "Fail case"]]
     [:hr]
     [:pre {:class "app-state"} (pr-str @app-state)]
     (case page
       :home [home-page]
       :fail-case-pre [fail-case-pre]
       :fail-case [fail-case]
       [:div "Page not found"])]))

(describe "Fail case race conditions"
  ;; In Reagent these interactions do not duplicate or drop nodes, but with the
  ;; Eucalypt ratom we observe duplicate/missing DOM in the browser.
  (fn []
    (it "should not duplicate or drop nodes when switching tabs and toggling modes"
      (fn []
        (reset! app-state {:page :home})
        (js/Promise.
          (fn [resolve reject]
            (let [container (.createElement js/document "div")]
              (try
                (.appendChild js/document.body container)
                (r/render [app] container)
                (let [flow (-> (sleep 20)
                               (.then
                                 (fn []
                                   (log-dom! "Step 1: Initial render" container)
                                   (th/assert-equal (.-textContent (.querySelector container "h1"))
                                                    "Test")
                                   (th/assert-equal (.-textContent (.querySelector container "p"))
                                                    "Failing tests.")
                                   (let [state (single-state container)]
                                     (th/assert-equal (aget state "page") "home"))
                                   nil))
                               (.then
                                 (fn []
                                   (th/log "Step 2: Navigate to fail case (Mode A)")
                                   (click-nav! container 2)
                                   (sleep 40)))
                               (.then
                                 (fn []
                                   (log-dom! "Step 3: After switching to Mode A" container)
                                   (let [modes (mode-texts container)
                                         a-btn (button-with-text container "A button")
                                         p-texts (texts-by-selector container "p")
                                         state (single-state container)]
                                     (th/assert-equal (count modes) 1)
                                     (th/assert-equal (first modes) "Mode A")
                                     (th/assert-not-nil a-btn)
                                     (th/assert-equal (.-textContent a-btn) "A button")
                                     (th/assert-equal 1 (count (filter #(= % "Wat A") p-texts)))
                                     (th/assert-equal (aget state "page") "fail-case"))
                                   nil))
                               (.then
                                 (fn []
                                   (th/log "Step 4: Switch to Mode B via button")
                                   (.click (button-with-text container "A button"))
                                   (sleep 80)))
                               (.then
                                 (fn []
                                   (log-dom! "Step 5: After switching to Mode B" container)
                                   (let [modes (mode-texts container)
                                         b-btn (button-with-text container "B button")
                                         state (single-state container)]
                                     (th/assert-equal (count modes) 1)
                                     (th/assert-equal (first modes) "Mode B")
                                     (th/assert-not-nil b-btn)
                                     (th/assert-equal (.-textContent b-btn) "B button")
                                     (th/assert-equal (aget state "page") "fail-case")
                                     (th/assert-equal (aget state "fail-case") true))
                                   (sleep 250)))
                               (.then
                                 (fn []
                                   (th/log "Step 6: Navigate to fail case pre")
                                   (click-nav! container 1)
                                   (sleep 40)))
                               (.then
                                 (fn []
                                   (log-dom! "Step 7: On fail case pre page" container)
                                   (th/assert-equal (texts-by-selector container "p") ["Wat"])
                                   (th/assert-equal (count (mode-texts container)) 0)
                                   nil))
                               (.then
                                 (fn []
                                   (th/log "Step 8: Navigate back to fail case (should remain Mode B)")
                                   (click-nav! container 2)
                                   (sleep 80)))
                               (.then
                                 (fn []
                                   (log-dom! "Step 9: Back on fail case (expected Mode B)" container)
                                   (let [modes (mode-texts container)
                                         b-btn (button-with-text container "B button")
                                         state (single-state container)]
                                     (th/assert-equal (count modes) 1)
                                     (th/assert-equal (first modes) "Mode B")
                                     (th/assert-not-nil b-btn)
                                     (th/assert-equal (.-textContent b-btn) "B button")
                                     (th/assert-equal 1 (count (texts-by-selector container "button")))
                                     (th/assert-equal (aget state "page") "fail-case")
                                     (th/assert-equal (aget state "fail-case") true))
                                   nil))
                               (.then
                                 (fn []
                                   (th/log "Step 10: Toggle back to Mode A")
                                   (.click (button-with-text container "B button"))
                                   (sleep 80)))
                               (.then
                                 (fn []
                                   (log-dom! "Step 11: After toggling back to Mode A" container)
                                   (let [modes (mode-texts container)
                                         a-btn (button-with-text container "A button")
                                         p-texts (texts-by-selector container "p")
                                         state (single-state container)]
                                     (th/assert-equal (count modes) 1)
                                     (th/assert-equal (first modes) "Mode A")
                                     (th/assert-not-nil a-btn)
                                     (th/assert-equal (.-textContent a-btn) "A button")
                                     (th/assert-equal 1 (count (filter #(= % "Wat A") p-texts)))
                                     (th/assert-equal (aget state "page") "fail-case")
                                     (th/assert-equal (aget state "fail-case") js/undefined))
                                   nil))
                               (.then
                                 (fn []
                                   (th/log "Step 12: Navigate home to ensure unmount clears interval")
                                   (click-nav! container 0)
                                   (sleep 40)))
                               (.then
                                 (fn []
                                   (log-dom! "Step 13: Final home view" container)
                                   (th/assert-equal (.-textContent (.querySelector container "h1"))
                                                    "Test")
                                   (th/assert-equal (.-textContent (.querySelector container "p"))
                                                    "Failing tests.")
                                   (let [state (single-state container)]
                                     (th/assert-equal (aget state "page") "home"))
                                   (reset! app-state {:page :home})
                                   (resolve nil)
                                   nil)))]
                  (.catch flow (fn [err] (reject err))))
                (catch :default err
                  (reject err))))))))))

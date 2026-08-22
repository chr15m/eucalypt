;;; Eucalypt Extensions Test Suite
;;;
;;; This file contains tests for backwards-compatible extensions in Eucalypt
;;; that go beyond the standard React/Reagent 1.0 specification, such as:
;;; - Direct custom DOM event listeners (e.g. :on-other-click, Web Components)
;;; - Setting arbitrary non-standard HTML attributes directly on elements
;;; - Direct DOM property inspection and access
;;; - Components returning raw sequences without a wrapping fragment
;;; - Passing style as a raw CSS string instead of a map
;;; - Ref cleanup functions (React 19 style) returned from ref callbacks

(ns eucalypt-extensions.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(def ref-events (r/atom []))
(def show-ref? (r/atom true))

(defn tracking-ref [el]
  (swap! ref-events conj (if el
                           {:kind :mount
                            :tag (.-tagName el)}
                           {:kind :nil}))
  (when el
    (fn []
      (swap! ref-events conj {:kind :cleanup}))))

(defn ref-cleanup-component []
  [:div
   (when @show-ref?
     [:span {:id "ref-target"
             :ref tracking-ref}
      "Hello ref"])])

(defn- child-counter [label]
  (let [clicks (r/atom 0)]
    (fn []
      [:div {:class (str "child child-" label)}
       [:span {:class (str "count-" label)} @clicks]
       [:button {:class (str "inc-" label)
                 :on-click #(swap! clicks inc)}
        (str "Increment " label)]])))

(defn- parent-with-toggle [label]
  (let [toggled? (r/atom false)]
    (fn []
      [:section {:class (str "parent parent-" label)}
       [:h2 (str label ": " (if @toggled? "on" "off"))]
       [child-counter label]
       [:button {:class (str "toggle-" label)
                 :on-click #(swap! toggled? not)}
        (str "Toggle " label)]])))

(defn- fragment-parent [label]
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

(describe "Eucalypt Extensions"
  (fn []
    (it "should support custom DOM event handlers and non-standard attributes"
      (fn []
        (let [other-click-fired (r/atom false)
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:id "test-div"
                           :click "some-value"
                           :on-click false
                           :on-another-click nil
                           :on-other-click #(reset! other-click-fired true)}]
                    container)
          (let [div (.querySelector container "#test-div")]
            ;; In Eucalypt, non-standard attributes are set on the DOM element.
            (th/assert-equal (.getAttribute div "click") "some-value")
            ;; :on-click with false is not attached
            (th/assert-equal (.-onclick div) nil)
            (th/assert-equal (.-onanotherclick div) nil)
            (th/assert-not-nil (.-onotherclick div))

            ;; Dispatch the custom event and check handler was fired
            (.dispatchEvent div (new js/Event "otherclick" #js {:bubbles true}))
            (th/assert-equal @other-click-fired true)))))

    (it "should render component functions that return raw lazy sequences directly"
      (fn []
        (letfn [(raw-seq-comp []
                  (map (fn [item] [:span {:key item} item]) ["A" "B" "C"]))]
          (let [container (.createElement js/document "div")]
            (.appendChild js/document.body container)
            (r/render [raw-seq-comp] container)
            (let [spans (.querySelectorAll container "span")]
              (th/assert-equal (.-length spans) 3)
              (th/assert-equal (.-textContent container) "ABC"))))))

    (it "should apply style as String"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:style "top: 5px; position: relative;"}] container)
          (th/assert-equal (.. container -firstChild -style -cssText) "top: 5px; position: relative;"))))

    (it "should run ref cleanup function instead of calling ref with nil on unmount"
      (fn []
        (reset! ref-events [])
        (reset! show-ref? true)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [ref-cleanup-component] container)

          (th/assert-equal (mapv #(get % :kind) @ref-events) [:mount])

          (reset! show-ref? false)
          (th/assert-equal (mapv #(get % :kind) @ref-events) [:mount :cleanup])
          (th/assert-equal (nil? (some #(= (get % :kind) :nil) @ref-events)) true)

          (reset! show-ref? true)
          (th/assert-equal (mapv #(get % :kind) @ref-events) [:mount :cleanup :mount])

          (reset! show-ref? false)
          (th/assert-equal (mapv #(get % :kind) @ref-events)
                           [:mount :cleanup :mount :cleanup])
          (th/assert-equal (nil? (some #(= (get % :kind) :nil) @ref-events)) true))))

    (it "should reorder unkeyed child pairs preserving DOM node identity"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div [:a "a"] [:b "b"]] container)

          (let [a-el (-> container .-firstChild .-firstChild)
                b-el (-> container .-firstChild .-lastChild)]
            (th/assert-equal (.-nodeName a-el) "A")
            (th/assert-equal (.-nodeName b-el) "B")

            (r/render [:div [:b "b"] [:a "a"]] container)
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (identical? (-> container .-firstChild .-firstChild) b-el) true)
                         (th/assert-equal (identical? (-> container .-firstChild .-lastChild) a-el) true))))))))

    (it "should maintain focus on unkeyed inputs when unkeyed siblings are moved or toggled"
      (fn []
        (let [focus-app (fn [{:keys [show-first? show-last?]}]
                          [:div
                           (when show-first? [:p "first"])
                           [:input {:id "focusable"}]
                           (when show-last? [:p "last"])])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [focus-app {:show-first? true :show-last? true}] container)
          (let [input (.querySelector container "#focusable")]
            (set! (.-value input) "a word")
            (.focus input)
            (.setSelectionRange input 2 5)
            (th/assert-equal js/document.activeElement input)

            ;; Move from middle to beginning by removing unkeyed first sibling
            (r/render [focus-app {:show-first? false :show-last? true}] container)
            (th/assert-equal js/document.activeElement input "move from middle to beginning")
            (th/assert-equal (.-selectionStart input) 2)
            (th/assert-equal (.-selectionEnd input) 5)))))

    (it "should keep child state when switching fragment and non-fragment roots"
      (fn []
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [fragment-parent "Alpha"] container-a)
          (r/render [parent-with-toggle "Beta"] container-b)

          (th/assert-equal (.-textContent (.querySelector container-a ".count-Alpha")) "0")
          (th/assert-equal (.-textContent (.querySelector container-b ".count-Beta")) "0")
          (th/assert-equal (.-textContent (.querySelector container-a ".mode-Alpha")) "Fragment layout")

          (.click (.querySelector container-a ".inc-Alpha"))
          (.click (.querySelector container-b ".inc-Beta"))
          (-> (th/wait-for-render)
              (.then (fn []
                       (th/assert-equal (.-textContent (.querySelector container-a ".count-Alpha")) "1")
                       (th/assert-equal (.-textContent (.querySelector container-b ".count-Beta")) "1")
                       (.click (.querySelector container-a ".switch-Alpha"))
                       (th/wait-for-render)))
              (.then (fn []
                       (th/assert-equal (.-textContent (.querySelector container-a ".mode-Alpha")) "Element layout")
                       (th/assert-equal (.-textContent (.querySelector container-a ".count-Alpha")) "1")
                       (th/assert-equal (.-textContent (.querySelector container-b ".count-Beta")) "1")
                       (.click (.querySelector container-a ".switch-Alpha"))
                       (th/wait-for-render)))
              (.then (fn []
                       (th/assert-equal (.-textContent (.querySelector container-a ".mode-Alpha")) "Fragment layout")
                       (th/assert-equal (.-textContent (.querySelector container-a ".count-Alpha")) "1")
                       (th/assert-equal (.-textContent (.querySelector container-b ".count-Beta")) "1")))))))))

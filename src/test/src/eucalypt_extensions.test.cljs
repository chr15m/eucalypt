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
                         (th/assert-equal (identical? (-> container .-firstChild .-lastChild) a-el) true))))))))))

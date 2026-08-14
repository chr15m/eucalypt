;;; Eucalypt Extensions Test Suite
;;;
;;; This file contains tests for backwards-compatible extensions in Eucalypt
;;; that go beyond the standard React/Reagent 1.0 specification, such as:
;;; - Direct custom DOM event listeners (e.g. :on-other-click, Web Components)
;;; - Setting arbitrary non-standard HTML attributes directly on elements
;;; - Direct DOM property inspection and access

(ns eucalypt-extensions.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

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
            (th/assert-equal @other-click-fired true)))))))

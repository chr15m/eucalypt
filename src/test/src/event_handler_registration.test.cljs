(ns event-handler-registration.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Event handler registration"
  (fn []
    (it "should only register on* properties as handlers"
      (fn []
        (let [on-click-fired (r/atom false)
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:id "test-div"
                           ;; This is not an event handler, just an attribute
                           :click "some-value"
                           ;; This is an event handler
                           :on-click #(reset! on-click-fired true)}]
                    container)
          (let [div (.querySelector container "#test-div")]
            ;; In Eucalypt, unlike Preact, non-standard attributes are set.
            (th/assert-equal (.getAttribute div "click") "some-value")
            ;; :on-click should be a handler, not an attribute.
            (th/assert-equal (.hasAttribute div "on-click") false)
            (.click div)
            (th/assert-equal @on-click-fired true)))))

    (it "should only register functions as handlers"
      (fn []
        (let [other-click-fired (r/atom false)
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:id "test-div"
                           :on-click false
                           :on-another-click nil
                           :on-other-click #(reset! other-click-fired true)}]
                    container)
          (let [div (.querySelector container "#test-div")]
            ;; Non-function handlers should be ignored (set to nil)
            (th/assert-equal (.-onclick div) nil)
            (th/assert-equal (.-onanotherclick div) nil)
            (th/assert-not-nil (.-onotherclick div))

            ;; Clicking should not throw an error
            (.click div)

            ;; Dispatch the other event and check it was fired
            (.dispatchEvent div (new js/Event "otherclick" #js {:bubbles true}))
            (th/assert-equal @other-click-fired true)))))))

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
            (th/assert-equal @other-click-fired true)))))

    (it "should update event handlers"
      (fn []
        (let [click1-fired (r/atom false)
              click2-fired (r/atom false)
              handler-ratom (r/atom #(reset! click1-fired true))
              container (.createElement js/document "div")
              component (fn [] [:div {:id "test-div" :on-click @handler-ratom}])]
          (.appendChild js/document.body container)
          (r/render [component] container)

          ;; Initial render with handler 1
          (let [div (.querySelector container "#test-div")]
            (.click div)
            (th/assert-equal @click1-fired true)
            (th/assert-equal @click2-fired false))

          ;; Reset state and update handler
          (reset! click1-fired false)
          (reset! click2-fired false)
          (reset! handler-ratom #(reset! click2-fired true))

          ;; After re-render, click again
          (let [div (.querySelector container "#test-div")]
            (.click div)
            (th/assert-equal @click1-fired false)
            (th/assert-equal @click2-fired true)))))

    (it "should remove event handlers"
      (fn []
        (let [click-fired (r/atom false)
              mousedown-fired (r/atom false)
              show-mousedown (r/atom true)
              show-click (r/atom true)
              container (.createElement js/document "div")
              component (fn []
                          [:div {:id "test-div"
                                 :on-click (when @show-click #(reset! click-fired true))
                                 :on-mousedown (when @show-mousedown #(reset! mousedown-fired true))}])]
          (.appendChild js/document.body container)
          (r/render [component] container)

          ;; Both handlers should work initially
          (let [div (.querySelector container "#test-div")]
            (.dispatchEvent div (new js/Event "mousedown" #js {:bubbles true}))
            (th/assert-equal @mousedown-fired true)
            (.click div)
            (th/assert-equal @click-fired true))

          ;; Reset state
          (reset! click-fired false)
          (reset! mousedown-fired false)

          ;; Remove mousedown handler
          (reset! show-mousedown false)

          (let [div (.querySelector container "#test-div")]
            (.dispatchEvent div (new js/Event "mousedown" #js {:bubbles true}))
            (th/assert-equal @mousedown-fired false "mousedown should be removed")
            (.click div)
            (th/assert-equal @click-fired true "click should still work"))

          ;; Reset state
          (reset! click-fired false)
          (reset! mousedown-fired false)

          ;; Remove click handler
          (reset! show-click false)

          (let [div (.querySelector container "#test-div")]
            (.dispatchEvent div (new js/Event "mousedown" #js {:bubbles true}))
            (th/assert-equal @mousedown-fired false "mousedown should still be removed")
            (.click div)
            (th/assert-equal @click-fired false "click should be removed")))))))

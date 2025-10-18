(ns uncontrolled-and-focus.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Uncontrolled Inputs"
  (fn []
    (it "should keep value of uncontrolled text inputs"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          ;; An uncontrolled input has its value prop set to nil (or undefined)
          (r/render [:input {:type "text" :id "test-input" :value nil}] container)
          (let [input (.querySelector container "#test-input")]
            ;; User types into the input
            (set! (.-value input) "foo")
            ;; Re-render the component, still uncontrolled
            (r/render [:input {:type "text" :id "test-input" :value nil}] container)
            ;; The user's value should be preserved
            (th/assert-equal (.-value input) "foo")))))

    (it "should keep value of uncontrolled checkboxes"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          ;; An uncontrolled checkbox has its checked prop set to nil
          (r/render [:input {:type "checkbox" :id "test-checkbox" :checked nil}] container)
          (let [checkbox (.querySelector container "#test-checkbox")]
            ;; User checks the box
            (set! (.-checked checkbox) true)
            ;; Re-render the component, still uncontrolled
            (r/render [:input {:type "checkbox" :id "test-checkbox" :checked nil}] container)
            ;; The user's change should be preserved
            (th/assert-equal (.-checked checkbox) true))))))) 

(describe "Focus and Selection Management"
  (fn []
    (it "should maintain focus when moving an input"
      (fn []
        (let [order (r/atom ["a" "b" "c"])
              component (fn []
                          [:div
                           (for [item @order]
                             (if (= item "b")
                               ^{:key "b"} [:input {:id "focusable"}]
                               ^{:key item} [:p item]))])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [component] container)

          (let [input (.querySelector container "#focusable")]
            (.focus input)
            (th/assert-equal js/document.activeElement input)

            ;; Reorder the elements, moving the input
            (reset! order ["b" "a" "c"])

            (th/assert-equal js/document.activeElement input "Focus should be maintained after reorder")))))

    (it "should keep text selection on re-render"
      (fn []
        (let [state (r/atom {:text "hello"})
              component (fn []
                          [:div
                           [:p "Some text"]
                           [:input {:id "selectable" :value (:text @state)}]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [component] container)

          (let [input (.querySelector container "#selectable")]
            (.focus input)
            (.setSelectionRange input 1 4) ;; select "ell"

            (th/assert-equal (.-selectionStart input) 1)
            (th/assert-equal (.-selectionEnd input) 4)

            ;; Trigger re-render by changing a parent's state
            (swap! state assoc :text "hello world")

            (th/assert-equal (.-selectionStart input) 1 "selectionStart should be preserved")
            (th/assert-equal (.-selectionEnd input) 4 "selectionEnd should be preserved")))))))

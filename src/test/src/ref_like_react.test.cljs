(ns ref-like-react.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Ref callbacks should behave like React"
  (fn []
    (it "should not cause infinite loop when ref callback modifies a reactive atom"
      (fn []
        (let [container (.createElement js/document "div")
              render-count (atom 0)
              ref-call-count (atom 0)
              state (r/atom {:mounted false})]
          (.appendChild js/document.body container)
          
          (defn test-component []
            (swap! render-count inc)
            [:div {:ref (fn [el]
                          (when el
                            (swap! ref-call-count inc)
                            (swap! state assoc :mounted true)))}
             "Render count: " @render-count])
          
          (r/render [test-component] container)
          
          ;; The component should render once
          ;; The ref should be called once
          ;; Even though the ref modifies a reactive atom that the component doesn't deref
          (th/assert-equal @render-count 1)
          (th/assert-equal @ref-call-count 1)
          (th/assert-equal (:mounted @state) true))))
    
    (it "should call ref with nil and then the element on every render for inline functions"
      (fn []
        (let [container (.createElement js/document "div")
              ref-calls (atom [])
              counter (r/atom 0)]
          (.appendChild js/document.body container)

          (defn test-component []
            [:div
             [:div {:ref (fn [el]
                           (swap! ref-calls conj (if el "mounted" "unmounted")))}
              "Counter: " @counter]
             [:button {:id "inc-btn"
                       :on-click #(swap! counter inc)}
              "Increment"]])

          (r/render [test-component] container)

          ;; Initial render - ref should be called once with element
          (th/assert-equal @ref-calls ["mounted"])

          ;; Trigger a re-render by clicking the button
          (.click (.querySelector container "#inc-btn"))

          ;; Ref should be called with nil, then with element
          (th/assert-equal @ref-calls ["mounted" "unmounted" "mounted"])

          ;; Click again
          (.click (.querySelector container "#inc-btn"))

          ;; And again
          (th/assert-equal @ref-calls ["mounted" "unmounted" "mounted" "unmounted" "mounted"]))))))

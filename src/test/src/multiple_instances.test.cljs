(ns multiple-instances.test
  (:require ["vitest" :refer [describe it afterEach beforeEach]]
            [eucalypt :as r]
            [helpers :as th]))

(beforeEach
 (fn []
   (r/clear-component-instances!)))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(defn counter-component [_id]
  (let [counter (r/atom 0)]
    (fn [id]
      [:div
       [:p "Counter " id ": " @counter]
       [:button {:id (str "btn-" id)
                 :on-click #(swap! counter inc)} "Increment"]])))

(defn main-page []
  [:div
   [counter-component "A"]
   [counter-component "B"]])

(defn keyed-page []
  [:div
   (with-meta [counter-component "A"] {:key "counter-A"})
   (with-meta [counter-component "B"] {:key "counter-B"})])

(describe "Multiple component instances"
  (fn []
    (it "should have independent state with positional keys"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [main-page] container)

          (let [btn-a (.querySelector container "#btn-A")
                all-ps (.querySelectorAll container "p")
                p-a (aget all-ps 0)
                p-b (aget all-ps 1)]

            (th/assert-equal (.-textContent p-a) "Counter A: 0")
            (th/assert-equal (.-textContent p-b) "Counter B: 0")

            (.click btn-a)

            (th/assert-equal (.-textContent p-a) "Counter A: 1")
            (th/assert-equal (.-textContent p-b) "Counter B: 0")))))

    (it "should have independent state with explicit keys"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [keyed-page] container)

          (let [btn-a (.querySelector container "#btn-A")
                btn-b (.querySelector container "#btn-B")
                all-ps (.querySelectorAll container "p")
                p-a (aget all-ps 0)
                p-b (aget all-ps 1)]

            (th/assert-equal (.-textContent p-a) "Counter A: 0")
            (th/assert-equal (.-textContent p-b) "Counter B: 0")

            ;; Click A twice
            (.click btn-a)
            (.click btn-a)

            ;; Click B once
            (.click btn-b)

            (th/assert-equal (.-textContent p-a) "Counter A: 2")
            (th/assert-equal (.-textContent p-b) "Counter B: 1")))))

    (it "should maintain state when components are reordered with keys"
      (fn []
        (let [order (r/atom ["A" "B"])
              reorderable-page (fn []
                                 [:div
                                  [:button {:id "reorder"
                                            :on-click #(swap! order reverse)}
                                   "Reorder"]
                                  (for [id @order]
                                    (with-meta [counter-component id] {:key (str "counter-" id)}))])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [reorderable-page] container)

          (let [btn-a (.querySelector container "#btn-A")
                btn-b (.querySelector container "#btn-B")
                reorder-btn (.querySelector container "#reorder")]

            ;; Set different values for A and B
            (.click btn-a)
            (.click btn-a)
            (.click btn-b)

            (let [all-ps-before (.querySelectorAll container "p")
                  p-a-before (aget all-ps-before 0)
                  p-b-before (aget all-ps-before 1)]
              (th/assert-equal (.-textContent p-a-before) "Counter A: 2")
              (th/assert-equal (.-textContent p-b-before) "Counter B: 1"))

            ;; Reorder the components
            (.click reorder-btn)

            ;; After reordering, the state should follow the components
            (let [all-ps-after (.querySelectorAll container "p")
                  first-p-after (aget all-ps-after 0)
                  second-p-after (aget all-ps-after 1)]
              ;; B should now be first (with its state of 1)
              (th/assert-equal (.-textContent first-p-after) "Counter B: 1")
              ;; A should now be second (with its state of 2)
              (th/assert-equal (.-textContent second-p-after) "Counter A: 2"))))))))

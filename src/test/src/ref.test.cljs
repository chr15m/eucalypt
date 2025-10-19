(ns ref.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(def ref-val (atom nil))

(defn ref-component []
  [:div {:id "ref-div"
         :ref #(reset! ref-val %)}
   "I have a ref."])

(def show-ref-component (r/atom true))

(defn main-component []
  [:div
   (if @show-ref-component
     [ref-component]
     [:p "Ref component is hidden"])
   [:button {:id "toggle"
             :on-click #(swap! show-ref-component not)} "Toggle"]])

(describe "Ref attribute"
  (fn []
    (it "should be called with element on mount and nil on unmount"
      (fn []
        (reset! ref-val nil)
        (reset! show-ref-component true)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [main-component] container)

          (th/assert-not-nil @ref-val)
          (th/assert-equal (.-tagName @ref-val) "DIV")
          (th/assert-equal (.-id @ref-val) "ref-div")

          ;; Toggle to unmount
          (.click (.querySelector container "#toggle"))

          (th/assert-equal @ref-val nil)))))

    (it "should not call stale refs"
      (fn []
        (let [ref1-calls (atom [])
              ref2-calls (atom [])
              ref1 (fn [el] (swap! ref1-calls conj el))
              ref2 (fn [el] (swap! ref2-calls conj el))
              show-ref1 (r/atom true)
              app (fn [] [:div {:ref (if @show-ref1 ref1 ref2)}])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (let [div-el (-> container .-firstChild)]
            (th/assert-equal @ref1-calls [div-el])
            (th/assert-equal @ref2-calls []))

          (reset! show-ref1 false)

          (let [div-el (-> container .-firstChild)]
            (th/assert-equal (count @ref1-calls) 2)
            (th/assert-equal (first @ref1-calls) div-el)
            (th/assert-equal (second @ref1-calls) nil)
            (th/assert-equal @ref2-calls [div-el])))))

    (it "should null and re-invoke refs when swapping component root element type"
      (fn []
        (let [calls (atom [])
              ref-fn (fn [el] (swap! calls conj el))
              show-div (r/atom true)
              app (fn [] (if @show-div
                           [:div {:ref ref-fn}]
                           [:span {:ref ref-fn}]))
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (th/assert-equal (count @calls) 1)
          (let [first-call (first @calls)]
            (th/assert-not-nil first-call)
            (th/assert-equal (.-nodeName first-call) "DIV"))

          (reset! show-div false)

          (th/assert-equal (count @calls) 3)
          (let [[div-mount div-unmount span-mount] @calls]
            (th/assert-equal (.-nodeName div-mount) "DIV")
            (th/assert-equal div-unmount nil)
            (th/assert-not-nil span-mount)
            (th/assert-equal (.-nodeName span-mount) "SPAN")))))

    (it "should have a consistent order"
      (fn []
        (let [events (atom [])
              ref-fn (fn [tag-name]
                       (fn [el]
                         (swap! events conj (if el tag-name (str "null-" tag-name)))))
              app (fn []
                    [:div {:ref (ref-fn "DIV")}
                     [:h1 {:ref (ref-fn "H1")} "hi"]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (r/render [app] container)

          ;; Observed Preact behavior from console logs:
          ;; 1. Mount: H1, DIV (bottom-up)
          ;; 2. Update -> Unmount: null-H1, null-DIV (bottom-up)
          ;; 3. Update -> Mount: H1, DIV (bottom-up)
          ;; Total: ["H1", "DIV", "null-H1", "null-DIV", "H1", "DIV"]
          (th/assert-equal @events ["H1" "DIV" "null-H1" "null-DIV" "H1" "DIV"])))))

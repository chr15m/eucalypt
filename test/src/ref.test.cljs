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

(def show-ref-component (r/ratom true))

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

          (th/assert-equal @ref-val nil))))))

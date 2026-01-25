(ns placeholders.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(defn bar []
  [:div "bar"])

(defn foo [{:keys [condition]}]
  (let [sibling (if condition [bar] js/undefined)]
    [:div
     [:div "Hello"]
     sibling]))

(defn create-nullable [name]
  (fn [{:keys [show?]}]
    (when show? name)))

(defn create-stateful-nullable [name toggle-ref]
  (let [show? (r/atom true)]
    (reset! toggle-ref #(swap! show? not))
    (fn []
      (when @show?
        [:div name]))))

(describe "null placeholders"
  (fn []
    (it "should treat undefined as a hole"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [foo {:condition true}] container)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>Hello</div><div>bar</div></div>")

          (r/render [foo {:condition false}] container)
          (th/assert-equal (.-innerHTML container) "<div><div>Hello</div></div>")

          ;; root + "Hello" div
          (th/assert-equal (.-length (.querySelectorAll container "div")) 2))))

    (it "should efficiently replace null placeholders in parent rerenders (#2350)"
      (fn []
        (let [nullable-1 (create-nullable "Nullable 1")
              nullable-2 (create-nullable "Nullable 2")
              show? (r/atom false)
              app (fn []
                    [:div
                     [:div (str @show?)]
                     [nullable-1 {:show? @show?}]
                     [:div "the middle"]
                     [nullable-2 {:show? @show?}]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>false</div><div>the middle</div></div>")

          (reset! show? true)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>true</div>Nullable 1<div>the middle</div>Nullable 2</div>")

          (reset! show? false)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>false</div><div>the middle</div></div>"))))

    (it "should efficiently replace self-updating null placeholders"
      (fn []
        (let [toggle-1 (atom nil)
              toggle-2 (atom nil)
              nullable-1 (create-stateful-nullable "Nullable" toggle-1)
              nullable-2 (create-stateful-nullable "Nullable2" toggle-2)
              app (fn []
                    [:div
                     [:div "1"]
                     [nullable-1]
                     [:div "3"]
                     [nullable-2]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>1</div><div>Nullable</div><div>3</div><div>Nullable2</div></div>")

          (@toggle-2)
          (@toggle-1)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>1</div><div>3</div></div>")

          (@toggle-2)
          (@toggle-1)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>1</div><div>Nullable</div><div>3</div><div>Nullable2</div></div>"))))

    (it "should only call unmount once when removing placeholders (#4104)"
      (fn []
        (let [ref-calls (atom [])
              ref-fn (fn [el]
                       (swap! ref-calls conj (if el :mount :nil)))
              show? (r/atom true)
              app (fn []
                    [:div
                     [:div "Test2"]
                     (when @show?
                       [:div {:ref ref-fn} "Test3"])
                     [:div "Iframe"]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (th/assert-equal (.-innerHTML container)
                           "<div><div>Test2</div><div>Test3</div><div>Iframe</div></div>")
          (th/assert-equal @ref-calls [:mount])

          (reset! ref-calls [])
          (reset! show? false)

          (th/assert-equal (.-innerHTML container)
                           "<div><div>Test2</div><div>Iframe</div></div>")
          (th/assert-equal @ref-calls [:nil])

          (reset! ref-calls [])
          (reset! show? false)
          (th/assert-equal @ref-calls []))))))

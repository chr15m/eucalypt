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
          (th/assert-equal (.-length (.querySelectorAll container "div")) 2))))))

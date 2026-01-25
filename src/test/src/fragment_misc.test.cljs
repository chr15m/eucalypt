(ns fragment-misc.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Misc Fragment tests"
  (fn []
    (it "should not crash with null as last child"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:<> [:span "world"] nil] container)
          (th/assert-equal (.-innerHTML container) "<span>world</span>")

          (r/render [:<> [:span "world"] [:p "Hello"]] container)
          (th/assert-equal (.-innerHTML container) "<span>world</span><p>Hello</p>")

          (r/render [:<> [:span "world"] nil] container)
          (th/assert-equal (.-innerHTML container) "<span>world</span>"))))

    (it "should respect keyed Fragments"
      (fn []
        (let [key-state (r/atom "foo")
              app (fn []
                    (with-meta [:<> "foo"] {:key @key-state}))
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [app] container)
          (th/assert-equal (.-innerHTML container) "foo")

          (reset! key-state "bar")
          (th/assert-equal (.-innerHTML container) "foo"))))))

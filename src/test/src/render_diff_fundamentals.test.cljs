(ns render-diff-fundamentals.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(describe "Render/diff fundamentals (from Preact)"
  (fn []
    (it "should allow node type change with content"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:span "Bad"] container)
          (r/render [:div "Good"] container)
          (th/assert-equal (.-innerHTML container) "<div>Good</div>"))))

    (it "should reorder child pairs"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div [:a "a"] [:b "b"]] container)

          (let [a-el (-> container .-firstChild .-firstChild)
                b-el (-> container .-firstChild .-lastChild)]
            (th/assert-equal (.-nodeName a-el) "A")
            (th/assert-equal (.-nodeName b-el) "B")

            (r/render [:div [:b "b"] [:a "a"]] container)

            ;; After re-render, the DOM nodes should be the same instances, just reordered.
            (th/assert-equal (identical? (-> container .-firstChild .-firstChild) b-el) true)
            (th/assert-equal (identical? (-> container .-firstChild .-lastChild) a-el) true)))))

    (it "should update in-place keyed DOM nodes"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:ul
                     ^{:key "a"} [:li "a"]
                     ^{:key "b"} [:li "b"]
                     ^{:key "c"} [:li "c"]]
                    container)
          (let [li-a (-> container .-firstChild (.querySelector "li:nth-child(1)"))
                li-b (-> container .-firstChild (.querySelector "li:nth-child(2)"))
                li-c (-> container .-firstChild (.querySelector "li:nth-child(3)"))]
            (th/assert-equal (.-textContent li-a) "a")
            (th/assert-equal (.-textContent li-b) "b")
            (th/assert-equal (.-textContent li-c) "c")

            (r/render [:ul
                       ^{:key "a"} [:li "x"]
                       ^{:key "b"} [:li "y"]
                       ^{:key "c"} [:li "z"]]
                      container)

            (let [li-a-after (-> container .-firstChild (.querySelector "li:nth-child(1)"))
                  li-b-after (-> container .-firstChild (.querySelector "li:nth-child(2)"))
                  li-c-after (-> container .-firstChild (.querySelector "li:nth-child(3)"))]
              (th/assert-equal (identical? li-a li-a-after) true)
              (th/assert-equal (identical? li-b li-b-after) true)
              (th/assert-equal (identical? li-c li-c-after) true)
              (th/assert-equal (.-textContent li-a-after) "x")
              (th/assert-equal (.-textContent li-b-after) "y")
              (th/assert-equal (.-textContent li-c-after) "z"))))))

    (it "should not lead to stale DOM nodes"
      (fn []
        (let [state (r/atom 0)
              child (fn [i] (if (< i 2) nil [:div "foo"]))
              app (fn [] [:div [child @state]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [app] container)
          (th/assert-equal (.-innerHTML container) "<div></div>")

          (reset! state 1)
          (th/assert-equal (.-innerHTML container) "<div></div>")

          (reset! state 2)
          (th/assert-equal (.-innerHTML container) "<div><div>foo</div></div>")

          (reset! state 3)
          (th/assert-equal (.-innerHTML container) "<div><div>foo</div></div>"))))

    (it "should remove attributes on re-render"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:class "red" :title "a title"} [:span "Hi"]] container)
          (th/assert-equal (-> container .-firstChild (.getAttribute "class")) "red")
          (th/assert-equal (-> container .-firstChild (.getAttribute "title")) "a title")

          (r/render [:div [:span "Bye"]] container)
          (th/assert-equal (-> container .-firstChild (.hasAttribute "class")) false)
          (th/assert-equal (-> container .-firstChild (.hasAttribute "title")) false)
          (th/assert-equal (.-textContent (-> container .-firstChild .-firstChild)) "Bye"))))

    (it "should reconcile children in right order"
      (fn []
        (let [list-state (r/atom ["A" "B" "C" "D" "E"])
              list-component (fn []
                               [:ul (for [item @list-state]
                                      ^{:key item} [:li item])])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          (r/render [list-component] container)
          (th/assert-equal (.-textContent (.querySelector container "ul")) "ABCDE")

          (reset! list-state ["B" "E" "C" "D"])
          (th/assert-equal (.-textContent (.querySelector container "ul")) "BECD"))))))

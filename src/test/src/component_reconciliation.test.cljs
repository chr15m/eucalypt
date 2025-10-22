(ns component-reconciliation.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Component reconciliation"
  (fn []
    (it "should not orphan children"
      (fn []
        (let [state-c (r/atom {:show? false})
              state-a (r/atom {:show? false})]
          (letfn [(component-b []
                    [:p "B"])
                  (component-c []
                    (if (:show? @state-c)
                      [:div "data"]
                      [:p "Loading"]))
                  (wrap-c []
                    [component-c])
                  (component-a []
                    (if (:show? @state-a)
                      [component-b]
                      [wrap-c]))]

            (r/render [component-a] js/document.body)
            (th/assert-equal (.-innerHTML js/document.body) "<p>Loading</p>")

            (swap! state-c assoc :show? true)
            (th/assert-equal (.-innerHTML js/document.body) "<div>data</div>")

            (swap! state-a assoc :show? true)
            (th/assert-equal (.-innerHTML js/document.body) "<p>B</p>")))))

    (it "should remove orphaned elements replaced by Components"
      (fn []
        (letfn [(comp-span []
                  [:span "span in a component"])]
          (r/render [comp-span] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<span>span in a component</span>")

          (r/render [:div "just a div"] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>just a div</div>")

          (r/render [comp-span] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<span>span in a component</span>"))))

    (it "should remove children when root changes to text node"
      (fn []
        (let [state (r/atom {:alt false})]
          (letfn [(comp []
                    (if (:alt @state)
                      "asdf"
                      [:div "test"]))]
            (r/render [comp] js/document.body)
            (th/assert-equal (.-innerHTML js/document.body) "<div>test</div>")

            (reset! state {:alt true})
            (th/assert-equal (.-innerHTML js/document.body) "asdf")

            (reset! state {:alt false})
            (th/assert-equal (.-innerHTML js/document.body) "<div>test</div>")

            (reset! state {:alt true})
            (th/assert-equal (.-innerHTML js/document.body) "asdf")))))

    (it "should maintain order when setting state (that inserts dom-elements)"
      (fn []
        (let [state (r/atom {:values ["abc"]})]
          (letfn [(entry [value]
                    [:div value])
                  (app []
                    [:div
                     (for [v (:values @state)]
                       ^{:key v} [entry v])
                     [:button "First Button"]
                     [:button "Second Button"]
                     [:button "Third Button"]])]

            (r/render [app] js/document.body)
            (th/assert-equal (-> js/document.body .-firstChild .-innerHTML)
                             "<div>abc</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

            (swap! state update :values conj "def")
            (th/assert-equal (-> js/document.body .-firstChild .-innerHTML)
                             "<div>abc</div><div>def</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

            (swap! state update :values conj "ghi")
            (th/assert-equal (-> js/document.body .-firstChild .-innerHTML)
                             "<div>abc</div><div>def</div><div>ghi</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

            (reset! state {:values ["abc"]})
            (th/assert-equal (-> js/document.body .-firstChild .-innerHTML)
                             "<div>abc</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")))))))

(describe "Children as props (via arguments)"
  (fn []
    (it "should handle various child types passed as props"
      (fn []
        (letfn [(wrapper [child]
                  [:div "prefix-" child "-suffix"])]
          ;; VNode
          (r/render [wrapper [:p "vnode"]] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>prefix-<p>vnode</p>-suffix</div>")

          ;; string
          (r/render [wrapper "string"] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>prefix-string-suffix</div>")

          ;; number
          (r/render [wrapper 123] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>prefix-123-suffix</div>")

          ;; nil
          (r/render [wrapper nil] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>prefix--suffix</div>")

          ;; boolean
          (r/render [wrapper true] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>prefix--suffix</div>")
          (r/render [wrapper false] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>prefix--suffix</div>"))))

    (it "should handle multiple children passed as props"
      (fn []
        (letfn [(wrapper [& children]
                  (into [:div "wrapper-"] children))]
          (r/render [wrapper [:p "one"] [:p "two"]] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>wrapper-<p>one</p><p>two</p></div>"))))

    (it "should handle children passed as a list"
      (fn []
        (letfn [(wrapper [children]
                  [:ul (for [c children] [:li c])])]
          (r/render [wrapper ["a" "b" "c"]] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<ul><li>a</li><li>b</li><li>c</li></ul>"))))

    (it "should ignore extra arguments if not used"
      (fn []
        (letfn [(explicit-child-component []
                  [:div "explicit"])]
          (r/render [explicit-child-component "ignored"] js/document.body)
          (th/assert-equal (.-innerHTML js/document.body) "<div>explicit</div>"))))))

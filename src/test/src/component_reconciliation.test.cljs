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
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
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

              (r/render [component-a] container)
              (th/assert-equal (.-innerHTML container) "<p>Loading</p>")

              (swap! state-c assoc :show? true)
              (th/assert-equal (.-innerHTML container) "<div>data</div>")

              (swap! state-a assoc :show? true)
              (th/assert-equal (.-innerHTML container) "<p>B</p>")))))))

    (it "should remove orphaned elements replaced by Components"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(comp-span []
                    [:span "span in a component"])]
            (r/render [comp-span] container)
            (th/assert-equal (.-innerHTML container) "<span>span in a component</span>")

            (r/render [:div "just a div"] container)
            (th/assert-equal (.-innerHTML container) "<div>just a div</div>")

            (r/render [comp-span] container)
            (th/assert-equal (.-innerHTML container) "<span>span in a component</span>")))))

    (it "should remove children when root changes to text node"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (let [state (r/atom {:alt false})]
            (letfn [(comp []
                      (if (:alt @state)
                        "asdf"
                        [:div "test"]))]
              (r/render [comp] container)
              (th/assert-equal (.-innerHTML container) "<div>test</div>")

              (reset! state {:alt true})
              (th/assert-equal (.-innerHTML container) "asdf")

              (reset! state {:alt false})
              (th/assert-equal (.-innerHTML container) "<div>test</div>")

              (reset! state {:alt true})
              (th/assert-equal (.-innerHTML container) "asdf"))))))

    (it "should maintain order when setting state (that inserts dom-elements)"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
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

              (r/render [app] container)
              (th/assert-equal (-> container .-firstChild .-innerHTML)
                               "<div>abc</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

              (swap! state update :values conj "def")
              (th/assert-equal (-> container .-firstChild .-innerHTML)
                               "<div>abc</div><div>def</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

              (swap! state update :values conj "ghi")
              (th/assert-equal (-> container .-firstChild .-innerHTML)
                               "<div>abc</div><div>def</div><div>ghi</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

              (reset! state {:values ["abc"]})
              (th/assert-equal (-> container .-firstChild .-innerHTML)
                               "<div>abc</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")))))))

(describe "Children as props (via arguments)"
  (fn []
    (it "should handle various child types passed as props"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(wrapper [child]
                    [:div "prefix-" child "-suffix"])]
            ;; VNode
            (r/render [wrapper [:p "vnode"]] container)
            (th/assert-equal (.-innerHTML container) "<div>prefix-<p>vnode</p>-suffix</div>")

            ;; string
            (r/render [wrapper "string"] container)
            (th/assert-equal (.-innerHTML container) "<div>prefix-string-suffix</div>")

            ;; number
            (r/render [wrapper 123] container)
            (th/assert-equal (.-innerHTML container) "<div>prefix-123-suffix</div>")

            ;; nil
            (r/render [wrapper nil] container)
            (th/assert-equal (.-innerHTML container) "<div>prefix--suffix</div>")

            ;; boolean
            (r/render [wrapper true] container)
            (th/assert-equal (.-innerHTML container) "<div>prefix--suffix</div>")
            (r/render [wrapper false] container)
            (th/assert-equal (.-innerHTML container) "<div>prefix--suffix</div>")))))

    (it "should handle multiple children passed as props"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(wrapper [& children]
                    (into [:div "wrapper-"] children))]
            (r/render [wrapper [:p "one"] [:p "two"]] container)
            (th/assert-equal (.-innerHTML container) "<div>wrapper-<p>one</p><p>two</p></div>")))))

    (it "should handle children passed as a list"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(wrapper [children]
                    [:ul (for [c children] [:li c])])]
            (r/render [wrapper ["a" "b" "c"]] container)
            (th/assert-equal (.-innerHTML container) "<ul><li>a</li><li>b</li><li>c</li></ul>")))))

    (it "should ignore extra arguments if not used"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(explicit-child-component []
                    [:div "explicit"])]
            (r/render [explicit-child-component "ignored"] container)
            (th/assert-equal (.-innerHTML container) "<div>explicit</div>")))))))

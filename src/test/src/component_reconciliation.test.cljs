(ns component-reconciliation.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(defn x-comp [children]
  (into [:<>] children))

(defn app-comp [{:keys [i]}]
  (if (== i 0)
    [:div
     (with-meta [x-comp ["1"]] {:key 1})
     (with-meta [x-comp ["2"]] {:key 2})]
    [:div
     (with-meta [x-comp ["2"]] {:key 2})
     (with-meta [x-comp ["1"]] {:key 1})]))

(describe "Component reconciliation"
  (fn []
    (it "should handle reordering components that return Fragments"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          ;; Initial render: 1 then 2
          (r/render [app-comp {:i 0}] container)
          (th/assert-equal (.-textContent container) "12")

          (let [first-node (-> container .-firstChild .-firstChild)]
            ;(js/console.log "Initial first node text:" (.-textContent first-node))
            ;(js/console.log "Initial first node outerHTML:" (.-outerHTML first-node))
            (th/assert-equal (.-textContent first-node) "1")

            ;; Re-render: 2 then 1
            ;(js/console.log "--- Re-rendering to swap order ---")
            ;(js/console.log "app-comp(1) hiccup:" (pr-str [app-comp {:i 1}]))

            ;; Check internal state before render
            #_ (let [root-info (get @r/roots container)
                     runtime (:runtime root-info)
                     mounted (:mounted-components @runtime)]
                 (js/console.log "Mounted components count:" (count mounted)))

            (r/render [app-comp {:i 1}] container)
            ; (js/console.log "HTML after swap:" (.-innerHTML container))
            (th/assert-equal (.-textContent container) "21")

            ;; Verify the node containing "1" is still the same instance but moved
            (let [new-second-node (-> container .-firstChild .-lastChild)]
              ;(js/console.log "New second node text:" (.-textContent new-second-node))
              ;(js/console.log "New second node outerHTML:" (.-outerHTML new-second-node))
              ;(js/console.log "Nodes are identical?:" (identical? first-node new-second-node))
              (th/assert-equal (identical? first-node new-second-node) true))))))

    (it "should not orphan children"
      (fn []
        (let [state-c (r/atom {:show? false})
              state-a (r/atom {:show? false})
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
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
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "<div>data</div>")
                         (swap! state-a assoc :show? true)
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "<p>B</p>"))))))))

    (it "should remove orphaned elements replaced by Components"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(comp-span []
                    [:span "span in a component"])]
            (r/render [comp-span] container)
            (th/assert-equal (.-innerHTML container) "<span>span in a component</span>")

            (r/render [:div "just a div"] container)
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "<div>just a div</div>")
                         (r/render [comp-span] container)
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "<span>span in a component</span>"))))))))

    (it "should remove children when root changes to text node"
      (fn []
        (let [state (r/atom {:alt false})
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(comp []
                    (if (:alt @state)
                      "asdf"
                      [:div "test"]))]
            (r/render [comp] container)
            (th/assert-equal (.-innerHTML container) "<div>test</div>")

            (reset! state {:alt true})
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "asdf")
                         (reset! state {:alt false})
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "<div>test</div>")
                         (reset! state {:alt true})
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-innerHTML container) "asdf"))))))))

    (it "should maintain order when setting state (that inserts dom-elements)"
      (fn []
        (let [state (r/atom {:values ["abc"]})
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (letfn [(entry [value]
                    [:div value])
                  (app []
                    [:div
                     (for [v (:values @state)]
                       (with-meta [entry v] {:key v}))
                     [:button "First Button"]
                     [:button "Second Button"]
                     [:button "Third Button"]])]

            (r/render [app] container)
            (th/assert-equal (-> container .-firstChild .-innerHTML)
                             "<div>abc</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")

            (swap! state update :values conj "def")
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (-> container .-firstChild .-innerHTML)
                                          "<div>abc</div><div>def</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")
                         (swap! state update :values conj "ghi")
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (-> container .-firstChild .-innerHTML)
                                          "<div>abc</div><div>def</div><div>ghi</div><button>First Button</button><button>Second Button</button><button>Third Button</button>")
                         (reset! state {:values ["abc"]})
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (-> container .-firstChild .-innerHTML)
                                          "<div>abc</div><button>First Button</button><button>Second Button</button><button>Third Button</button>"))))))))))

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
                    [:ul (for [c children]
                           (with-meta [:li c] {:key c}))])]
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

(describe "Component initialization"
  (fn []
    (it "should not crash when setting state in constructor"
        (fn []
          (let [state-in-constructor (atom nil)
                container (.createElement js/document "div")]
            (.appendChild js/document.body container)
            (letfn [(foo []
                      (let [local-state (r/atom {:preact "awesome"})]
                        (reset! state-in-constructor @local-state)
                        (fn []
                          [:div (js/JSON.stringify (clj->js @local-state))])))]
              (r/render [foo] container)
              (th/assert-equal @state-in-constructor {:preact "awesome"})))))

    (it "should initialize props but not state in Component constructor"
        (fn []
          (let [captured-initial-state (atom nil)
                container (.createElement js/document "div")]
            (.appendChild js/document.body container)
            (letfn [(foo [_props]
                      (let [local-state (r/atom nil)]
                        (reset! captured-initial-state @local-state)
                        (fn [props]
                          [:div (js/JSON.stringify (clj->js props))])))]
              (r/render [foo {:bar "baz"}] container)
              (th/assert-equal @captured-initial-state nil)
              (th/assert-equal (.-innerHTML container) "<div>{\"bar\":\"baz\"}</div>")))))))

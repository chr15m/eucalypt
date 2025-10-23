(ns hoc-and-nesting.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

;;; --- High-Order Components ---

(describe "High-Order Components"
  (fn []
    (it "should render wrapper HOCs"
      (fn []
        (let [text-prop "We'll throw some happy little limbs on this tree."
              paint-something (fn [props] [:div (:text props)])
              with-bob-ross (fn [child-component]
                              (fn [props]
                                [child-component (assoc props :text text-prop)]))
              paint (with-bob-ross paint-something)
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [paint] container)
          (th/assert-equal (.-innerHTML container) (str "<div>" text-prop "</div>")))))

    (it "should render nested functional components"
      (fn []
        (let [inner (fn [props] [:div {:foo (:foo props)} "inner"])
              outer (fn [props] [inner props])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [outer {:foo "bar"}] container)
          (th/assert-equal (.-innerHTML container) "<div foo=\"bar\">inner</div>"))))

    (it "should re-render nested components"
      (fn []
        (let [inner (fn [{:keys [i]}] [:div {:i i} "inner"])
              outer (let [state (r/atom {:i 1 :alt? false})]
                      (fn []
                        (if (:alt? @state)
                          [:div {:class "is-alt"}]
                          [:div
                           [inner @state]
                           [:button {:id "updater" :on-click #(swap! state update :i inc)}]
                           [:button {:id "switcher" :on-click #(swap! state update :alt? not)}]])))
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [outer] container)

          (th/assert-equal (-> container .-firstChild .-innerHTML)
                           "<div i=\"1\">inner</div><button id=\"updater\"></button><button id=\"switcher\"></button>")

          ;; Update and re-render
          (.click (.querySelector container "#updater"))
          (th/assert-equal (-> container .-firstChild .-innerHTML)
                           "<div i=\"2\">inner</div><button id=\"updater\"></button><button id=\"switcher\"></button>")

          ;; Switch to alt view
          (.click (.querySelector container "#switcher"))
          (th/assert-equal (.-innerHTML container) "<div class=\"is-alt\"></div>"))))

    (it "should unmount children of HOCs without unmounting parent"
      (fn []
        (let [unmount-counts (r/atom {})
              record-unmount (fn [id] (swap! unmount-counts update id (fnil inc 0)))
              inner-a (fn []
                        (let [ref-fn (fn [el] (when-not el (record-unmount :a)))]
                          (fn [] [:div {:ref ref-fn} "Inner A"])))
              inner-b (fn []
                        (let [ref-fn (fn [el] (when-not el (record-unmount :b)))]
                          (fn [] [:div {:ref ref-fn} "Inner B"])))
              outer (fn []
                      (let [child-type (r/atom :a)
                            ref-fn (fn [el] (when-not el (record-unmount :outer)))]
                        (fn []
                          [:div {:ref ref-fn}
                           (if (= @child-type :a) [inner-a] [inner-b])
                           [:button {:id "switcher" :on-click #(reset! child-type :b)} "Switch"]])))
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [outer] container)

          (th/assert-equal (.-innerHTML container) "<div><div>Inner A</div><button id=\"switcher\">Switch</button></div>")
          (th/assert-equal @unmount-counts {})

          (.click (.querySelector container "#switcher"))

          (th/assert-equal (.-innerHTML container) "<div><div>Inner B</div><button id=\"switcher\">Switch</button></div>")
          (th/assert-equal (:a @unmount-counts) 1)
          (th/assert-equal (not (:b @unmount-counts)) true)
          (th/assert-equal (not (:outer @unmount-counts)) true))))))

;;; --- Component Nesting ---

(describe "Component Nesting"
  (fn []
    (it "should resolve intermediary functional component"
      (fn []
        (let [unmount-counts (atom {})
              record-unmount (fn [id] (swap! unmount-counts update id (fnil inc 0)))
              inner (fn []
                      (let [ref-fn (fn [el] (when-not el (record-unmount :inner)))]
                        (fn [] [:div {:ref ref-fn} "inner"])))
              func-wrapper (fn [] [inner])
              root (fn [] [func-wrapper])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [root] container)
          (th/assert-equal (.-innerHTML container) "<div>inner</div>")

          (r/render [:p "unmounted"] container)
          (th/assert-equal (.-innerHTML container) "<p>unmounted</p>")
          (th/assert-equal (:inner @unmount-counts) 1))))

    (it "should handle deeply nested stateful components"
      (fn []
        (let [leaf-state (r/atom 0)
              leaf-component (fn []
                               [:span {:id "leaf"}
                                "Leaf: " @leaf-state
                                [:button {:on-click #(swap! leaf-state inc)} "Inc Leaf"]])
              node-component (fn [child]
                               [:div.node
                                "Node wrapper. "
                                child])
              root-state (r/atom 0)
              root-component (fn []
                               [:div
                                [:h1 "Root"]
                                [node-component [node-component [leaf-component]]]
                                [:p "Root state: " @root-state]
                                [:button {:id "root-inc" :on-click #(swap! root-state inc)} "Inc Root"]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [root-component] container)

          (th/assert-equal (.-textContent (.querySelector container "#leaf")) "Leaf: 0Inc Leaf")

          ;; Update leaf component state
          (.click (.querySelector container "#leaf button"))
          (th/assert-equal (.-textContent (.querySelector container "#leaf")) "Leaf: 1Inc Leaf")

          ;; Update root component state
          (.click (.querySelector container "#root-inc"))
          (th/assert-equal (.-textContent (.querySelector container "p")) "Root state: 1")

          ;; Check that leaf component state was preserved
          (th/assert-equal (.-textContent (.querySelector container "#leaf")) "Leaf: 1Inc Leaf"))))))

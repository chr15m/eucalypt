(ns uncontrolled-and-focus.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "Uncontrolled Inputs"
  (fn []
    (it "should keep value of uncontrolled text inputs"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          ;; An uncontrolled input has its value prop set to nil (or undefined)
          (r/render [:input {:type "text" :id "test-input" :value nil}] container)
          (let [input (.querySelector container "#test-input")]
            ;; User types into the input
            (set! (.-value input) "foo")
            ;; Re-render the component, still uncontrolled
            (r/render [:input {:type "text" :id "test-input" :value nil}] container)
            ;; The user's value should be preserved
            (th/assert-equal (.-value input) "foo")))))

    (it "should keep value of uncontrolled checkboxes"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          ;; An uncontrolled checkbox has its checked prop set to nil
          (r/render [:input {:type "checkbox" :id "test-checkbox" :checked nil}] container)
          (let [checkbox (.querySelector container "#test-checkbox")]
            ;; User checks the box
            (set! (.-checked checkbox) true)
            ;; Re-render the component, still uncontrolled
            (r/render [:input {:type "checkbox" :id "test-checkbox" :checked nil}] container)
            ;; The user's change should be preserved
            (th/assert-equal (.-checked checkbox) true))))))) 

(defn- focus-input [container]
  (let [input (.querySelector container "#focusable")]
    (set! (.-value input) "a word")
    (.focus input)
    (.setSelectionRange input 2 5)
    (th/assert-equal js/document.activeElement input)
    input))

(defn- validate-focus [input message]
  (th/assert-equal js/document.activeElement input message)
  (th/assert-equal (.-selectionStart input) 2)
  (th/assert-equal (.-selectionEnd input) 5))

(def dynamic-list-state (r/atom {:before [] :after []}))

(defn dynamic-list [{:keys [unkeyed? as]
                     :or {as :p}}]
  [:div
   (for [v (:before @dynamic-list-state)]
     (with-meta (if (= as :input) [:input {:type "text"}] [as v])
                (when-not unkeyed? {:key v})))
   [:input {:id "focusable"}]
   (for [v (:after @dynamic-list-state)]
     (with-meta (if (= as :input) [:input {:type "text"}] [as v])
                (when-not unkeyed? {:key v})))])

(defn conditional-list [{:keys [show-0? show-1? show-2? show-3?]}]
  [:div
   (when show-0? [:p 0])
   (when show-1? [:p 1])
   [:input {:id "focusable"}]
   (when show-2? [:p 2])
   (when show-3? [:p 3])])

(def fragment-focus-state (r/atom {:active? false}))

(defn fragment-focus-app []
  (let [input-ref (atom nil)]
    (fn []
      [:div
       [:h1 "Heading"]
       (if-not (:active? @fragment-focus-state)
         [:<>
          "foobar"
          [:<>
           "Hello World"
           [:h2 "yo"]]
          [:input {:type "text" :id "focusable" :ref #(reset! input-ref %)}]]
         [:<>
          [:<>
           "Hello World"
           [:h2 "yo"]]
          "foobar"
          [:input {:type "text" :id "focusable" :ref #(reset! input-ref %)}]])])))

(def selection-state (r/atom {:active? false}))

(defn selection-app []
  (let [input-ref (atom nil)]
    (fn []
      [:div
       [:h1 "Heading"]
       (if-not (:active? @selection-state)
         [:<>
          "foobar"
          [:<>
           "Hello World"
           [:h2 "yo"]]
          [:input {:type "text" :id "focusable" :value "foobar" :ref #(reset! input-ref %)}]]
         [:<>
          [:<>
           "Hello World"
           [:h2 "yo"]]
          "foobar"
          [:input {:type "text" :id "focusable" :value "foobar" :ref #(reset! input-ref %)}]])])))

(defn focus-app [{:keys [show-first? show-last?]}]
  [:div
   (when show-first? [:p "first"])
   [:input {:id "focusable"}]
   (when show-last? [:p "last"])])

(describe "Focus and Selection Management"
  (fn []
    (it "should maintain focus when moving an input"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)

          ;; 1. Initial render: middle
          (r/render [focus-app {:show-first? true :show-last? true}] container)

          ;; 2. Move to beginning
          (let [input (focus-input container)]
            (r/render [focus-app {:show-first? false :show-last? true}] container)
            (validate-focus input "move from middle to beginning"))

          ;; 3. Move to middle
          (let [input (focus-input container)]
            (r/render [focus-app {:show-first? true :show-last? true}] container)
            (validate-focus input "move from beginning to middle"))

          ;; 4. Move to end
          (let [input (focus-input container)]
            (r/render [focus-app {:show-first? true :show-last? false}] container)
            (validate-focus input "move from middle to end"))

          ;; 5. Move to middle again
          (let [input (focus-input container)]
            (r/render [focus-app {:show-first? true :show-last? true}] container)
            (validate-focus input "move from end to middle")))))

    (it "should keep text selection on re-render"
      (fn []
        (let [state (r/atom {:text "hello"})
              component (fn []
                          [:div
                           [:p "Some text"]
                           [:input {:id "selectable" :value (:text @state)}]])
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [component] container)

          (let [input (.querySelector container "#selectable")]
            (.focus input)
            (.setSelectionRange input 1 4) ;; select "ell"

            (th/assert-equal (.-selectionStart input) 1)
            (th/assert-equal (.-selectionEnd input) 4)

            ;; Trigger re-render by changing a parent's state
            (swap! state assoc :text "hello world")

            (th/assert-equal (.-selectionStart input) 1 "selectionStart should be preserved")
            (th/assert-equal (.-selectionEnd input) 4 "selectionEnd should be preserved")))))

    (it "should maintain focus when adding children around input"
      (fn []
        (reset! dynamic-list-state {:before [] :after []})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [dynamic-list] container)
          (let [input (focus-input container)
                prepend #(swap! dynamic-list-state update :before (fn [b] (cons (th/rand) b)))
                append #(swap! dynamic-list-state update :after (fn [a] (conj a (th/rand))))]

            (prepend)
            (validate-focus input "insert sibling before")

            (append)
            (validate-focus input "insert sibling after")

            (append)
            (validate-focus input "insert sibling after again")

            (prepend)
            (validate-focus input "insert sibling before again")))))

    (it "should maintain focus when adding children around input (unkeyed)"
      (fn []
        (reset! dynamic-list-state {:before [] :after []})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [dynamic-list {:unkeyed? true}] container)
          (let [input (focus-input container)
                prepend #(swap! dynamic-list-state update :before (fn [b] (cons (th/rand) b)))
                append #(swap! dynamic-list-state update :after (fn [a] (conj a (th/rand))))]

            (prepend)
            (validate-focus input "insert sibling before")

            (append)
            (validate-focus input "insert sibling after")

            (append)
            (validate-focus input "insert sibling after again")

            (prepend)
            (validate-focus input "insert sibling before again")))))

    (it "should maintain focus when conditional elements around input"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [conditional-list {:show-0? true :show-1? true :show-2? true :show-3? true}] container)
          (let [input (focus-input container)]
            (r/render [conditional-list {:show-1? true :show-2? true :show-3? true}] container)
            (validate-focus input "remove sibling before")

            (r/render [conditional-list {:show-1? true :show-2? true}] container)
            (validate-focus input "remove sibling after")

            (r/render [conditional-list {:show-1? true}] container)
            (validate-focus input "remove sibling after 2")

            (r/render [conditional-list {}] container)
            (validate-focus input "remove sibling before 2")))))

    (it "should maintain focus when removing elements around input"
      (fn []
        (reset! dynamic-list-state {:before [0 1] :after [2 3]})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [dynamic-list] container)
          (let [input (focus-input container)
                shift #(swap! dynamic-list-state update :before rest)
                pop #(swap! dynamic-list-state update :after butlast)]

            (shift)
            (validate-focus input "remove sibling before")

            (pop)
            (validate-focus input "remove sibling after")

            (pop)
            (validate-focus input "remove sibling after 2")

            (shift)
            (validate-focus input "remove sibling before 2")))))

    (it "should maintain focus when adding input next to the current input"
      (fn []
        (reset! dynamic-list-state {:before [] :after []})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [dynamic-list {:as :input}] container)
          (let [input (focus-input container)
                prepend #(swap! dynamic-list-state update :before (fn [b] (cons (th/rand) b)))
                append #(swap! dynamic-list-state update :after (fn [a] (conj a (th/rand))))]

            (prepend)
            (validate-focus input "add input before")

            (append)
            (validate-focus input "add input after")

            (prepend)
            (validate-focus input "add input before again")))))

    ;;; Eucalypt does not support hydration, so the Preact test 'should maintain focus when hydrating' is not applicable.

    (it "should keep focus in Fragments"
      (fn []
        (reset! fragment-focus-state {:active? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [fragment-focus-app] container)
          (let [input (.querySelector container "#focusable")]
            (.focus input)
            (swap! fragment-focus-state assoc :active? true)
            (th/assert-equal js/document.activeElement input "After rerender")))))

    (it "should keep text selection when reordering fragments"
      (fn []
        (reset! selection-state {:active? false})
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [selection-app] container)
          (let [input (.querySelector container "#focusable")]
            (.focus input)
            (.setSelectionRange input 2 5)
            (swap! selection-state assoc :active? true)

            (th/assert-equal js/document.activeElement input "Before rerender")
            (th/assert-equal (.-selectionStart input) 2)
            (th/assert-equal (.-selectionEnd input) 5)
            (th/assert-equal js/document.activeElement input "After rerender")))))))

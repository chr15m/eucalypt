(ns shared-state-multiple-roots.test
  (:require ["vitest" :refer [describe it]]
            [eucalypt :as r]
            [helpers :as th]))

(defonce state (r/atom {:counter 0
                        :other [:foo :bar]}))

(defn component:main [state]
  [:<>
   [:p "Hello world! The counter is: " (:counter @state)]
   [:pre (js/JSON.stringify @state)]
   [:button {:on-click #(swap! state update :counter (fnil inc 0))} "inc"]
   [:button {:on-click #(swap! state update :counter (fnil dec 0))} "dec"]])

(describe "Shared state across multiple roots"
  (fn []
    (it "keeps both roots in sync when rendering same component twice"
      (fn []
        (let [container-a (.createElement js/document "div")
              container-b (.createElement js/document "div")]
          (set! (.-id container-a) "app")
          (set! (.-id container-b) "app2")
          (.appendChild js/document.body container-a)
          (.appendChild js/document.body container-b)

          (r/render [component:main state] container-a)
          (r/render [component:main state] container-b)

          (let [counter-text (fn [container]
                               (.-textContent (.querySelector container "p")))
                json-text (fn [container]
                            (.-textContent (.querySelector container "pre")))
                button-at (fn [container idx]
                            (aget (.querySelectorAll container "button") idx))]

            ;; Initial render reflects shared state
            (th/assert-equal (counter-text container-a) "Hello world! The counter is: 0")
            (th/assert-equal (counter-text container-b) "Hello world! The counter is: 0")
            (th/assert-equal (json-text container-a) "{\"counter\":0,\"other\":[\"foo\",\"bar\"]}")
            (th/assert-equal (json-text container-b) "{\"counter\":0,\"other\":[\"foo\",\"bar\"]}")

            ;; Increment via first root updates both
            (.click (button-at container-a 0))
            (th/assert-equal (counter-text container-a) "Hello world! The counter is: 1")
            (th/assert-equal (counter-text container-b) "Hello world! The counter is: 1")
            (th/assert-equal (json-text container-a) "{\"counter\":1,\"other\":[\"foo\",\"bar\"]}")
            (th/assert-equal (json-text container-b) "{\"counter\":1,\"other\":[\"foo\",\"bar\"]}")

            ;; Decrement via second root updates both
            (.click (button-at container-b 1))
            (th/assert-equal (counter-text container-a) "Hello world! The counter is: 0")
            (th/assert-equal (counter-text container-b) "Hello world! The counter is: 0")
            (th/assert-equal (json-text container-a) "{\"counter\":0,\"other\":[\"foo\",\"bar\"]}")
            (th/assert-equal (json-text container-b) "{\"counter\":0,\"other\":[\"foo\",\"bar\"]}")))))))

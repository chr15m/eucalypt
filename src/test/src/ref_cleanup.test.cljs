(ns ref-cleanup.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def ref-events (r/atom []))
(def show-ref? (r/atom true))

(defn tracking-ref [el]
  (swap! ref-events conj (if el
                           {:kind :mount
                            :tag (.-tagName el)}
                           {:kind :nil}))
  (when el
    (fn []
      (swap! ref-events conj {:kind :cleanup}))))

(defn ref-cleanup-component []
  [:div
   (when @show-ref?
     [:span {:id "ref-target"
             :ref tracking-ref}
      "Hello ref"])])

(describe "Ref cleanup function support"
  (fn []
    (it "should run cleanup instead of calling ref with nil on unmount"
      (fn []
        (reset! ref-events [])
        (reset! show-ref? true)
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [ref-cleanup-component] container)

          (th/assert-equal (mapv #(get % :kind) @ref-events) [:mount])

          (reset! show-ref? false)
          (th/assert-equal (mapv #(get % :kind) @ref-events) [:mount :cleanup])
          (th/assert-equal (nil? (some #(= (get % :kind) :nil) @ref-events)) true)

          (reset! show-ref? true)
          (th/assert-equal (mapv #(get % :kind) @ref-events) [:mount :cleanup :mount])

          (reset! show-ref? false)
          (th/assert-equal (mapv #(get % :kind) @ref-events)
                           [:mount :cleanup :mount :cleanup])
          (th/assert-equal (nil? (some #(= (get % :kind) :nil) @ref-events)) true))))))

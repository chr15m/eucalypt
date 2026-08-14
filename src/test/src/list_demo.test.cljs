(ns list-demo.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(def list-data (r/atom []))

(defn list-demo-page []
  [:div
   [:h1 "List Demo"]
   [:button {:id "add-btn"
             :on-click (fn [_]
                         (swap! list-data conj (js/Math.random)))}
    "+ Add"]
   [:ul
    (for [item @list-data]
      (with-meta
       [:li
        item
        " "
        [:button {:class "delete-btn"
                  :on-click (fn [_]
                              (swap! list-data
                                     (fn [items]
                                       (vec (remove #(= % item) items)))))}
         "x"]]
       {:key item}))]])

(describe "List Demo"
  (fn []
    (it "should add and remove items from the list"
      (fn []
        (reset! list-data [])
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [list-demo-page] container)

          (let [add-btn (.querySelector container "#add-btn")
                get-items #(.querySelectorAll container "li")
                get-delete-btns #(.querySelectorAll container ".delete-btn")]

            
            ;; Add 5 items
            (dotimes [_ 5] (.click add-btn))
            (-> (th/wait-for-render)
                (.then (fn []
                         (th/assert-equal (.-length (get-items)) 5)

                         ;; Delete 3rd item
                         (.click (aget (get-delete-btns) 2))
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-length (get-items)) 4)

                         ;; Delete 3rd item again
                         (.click (aget (get-delete-btns) 2))
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-length (get-items)) 3)

                         ;; Add two more items
                         (.click add-btn)
                         (.click add-btn)
                         (th/wait-for-render)))
                (.then (fn []
                         (th/assert-equal (.-length (get-items)) 5)

                         ;; Delete all items
                         (let [delete-all (fn delete-all []
                                            (if (> (.-length (get-delete-btns)) 0)
                                              (do
                                                (.click (aget (get-delete-btns) 0))
                                                (.then (th/wait-for-render) delete-all))
                                              (js/Promise.resolve)))]
                           (delete-all))))
                (.then (fn []
                         (th/assert-equal (.-length (get-items)) 0))))))))))

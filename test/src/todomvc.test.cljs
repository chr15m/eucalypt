(ns todomvc.test
  (:require ["vitest" :refer [describe it afterEach beforeEach]]
            [eucalypt :as r]
            [helpers :as th]
            [clojure.string :as str]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

;;; TodoMVC implementation (isolated for testing, based on original reagent example)

(defonce todos (r/atom {}))
(defonce counter (r/atom 0))

(defn add-todo [text]
  (js/console.log "add-todo called with:" text)
  (js/console.log "todos count before:" (count @todos))
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :title text :done false}))
  (js/console.log "todos count after:" (count @todos)))

(defn toggle [id]
  (js/console.log "toggle called for id:" id)
  (js/console.log "todo" id "done state before:" (get-in @todos [id :done]))
  (swap! todos update-in [id :done] not)
  (js/console.log "todo" id "done state after:" (get-in @todos [id :done])))
(defn save [id title] (swap! todos assoc-in [id :title] title))
(defn delete-todo [id] (swap! todos dissoc id))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v] (swap! todos mmap map #(assoc-in % [1 :done] v)))
(defn clear-done [] (swap! todos mmap remove #(get-in % [1 :done])))

(defn todo-input [{:keys [title on-save on-stop] :as _props}]
  (let [val (r/atom (or title ""))]
    (fn [props]
      (letfn [(stop [_e]
                (reset! val "")
                (when on-stop (on-stop)))
              (save [e]
                (let [v (-> @val str str/trim)]
                  (when-not (empty? v)
                    (on-save v)))
                (stop e))]
        [:input (merge
                  {:type "text"
                   :value @val
                   :on-blur save
                   :on-change (fn [e] (reset! val (.. e -target -value)))
                   :on-key-down (fn [e]
                                  (case (.-code e)
                                    "Enter" (save e)
                                    "Escape" (stop e)
                                    nil))}
                  (select-keys props [:id :class :placeholder :ref]))]))))

(defn todo-edit [props]
  [todo-input (assoc props :ref (fn [el] (when el (.focus el))))])

(defn todo-stats [{:keys [filt active done]}]
  (let [props-for (fn [name]
                    {:href "#"
                     :class (when (= name @filt) "selected")
                     :on-click (fn [e]
                                 (.preventDefault e)
                                 (reset! filt name))})]
    [:div
     [:span {:id "todo-count"}
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul {:id "filters"}
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? done)
       [:button {:id "clear-completed" :on-click clear-done}
        "Clear completed " done])]))

(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [id done title]}]
      [:li
       {:class (->> [(when done "completed")
                     (when @editing "editing")]
                    (remove nil?)
                    (str/join " "))}
       [:div {:class "view"}
        [:input {:class "toggle"
                 :type "checkbox"
                 :checked done
                 :on-change #(toggle id)}]
        [:label {:on-double-click #(reset! editing true)}
         title]
        [:button {:class "destroy" :on-click #(delete-todo id)}]]
       (when @editing
         [todo-edit {:class "edit"
                     :title title
                     :on-save #(save id %)
                     :on-stop #(reset! editing false)}])])))

(defn todomvc-page []
  (let [filt (r/atom :all)]
    (fn []
      (let [items (->> (vals @todos) (sort-by :id))
            done (->> items (filter :done) count)
            active (- (count items) done)]
        [:div
         [:section {:id "todoapp"}
          [:header {:id "header"}
           [:h1 "todos"]
           [todo-input {:id "new-todo"
                        :placeholder "What needs to be done?"
                        :on-save add-todo}]]
          (when (seq items)
            [:div
             [:section {:id "main"}
              [:input {:id "toggle-all" :type "checkbox" :checked (zero? active)
                       :on-change #(complete-all (pos? active))}]
              [:label {:for "toggle-all"} "Mark all as complete"]
              [:ul {:id "todo-list"}
               (for [todo (filter (case @filt
                                    :active (complement :done)
                                    :done :done
                                    :all identity) items)]
                 ^{:key (:id todo)} [todo-item todo])]]
             [:footer {:id "footer"}
              [todo-stats {:active active :done done :filt filt}]]])]
         [:footer {:id "info"}
          [:p "Double-click to edit a todo"]]]))))

;;; Tests

(describe "TodoMVC Component"
  (fn []
    (beforeEach
     (fn []
       (reset! todos {})
       (reset! counter 0)
       (add-todo "one")
       (add-todo "two")
       (add-todo "three")
       (swap! todos assoc-in [2 :done] true)))

    (it "should render initial state correctly"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [todo-items (.querySelectorAll container "#todo-list li")]
            (th/assert-equal (.-length todo-items) 3)
            (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "2")

            (let [item1-label (.querySelector (aget todo-items 0) "label")
                  item2-li (aget todo-items 1)
                  item2-label (.querySelector item2-li "label")]
              (th/assert-equal (.-textContent item1-label) "one")
              (th/assert-equal (.-textContent item2-label) "two")
              (th/assert-equal (.contains (.-classList item2-li) "completed") true))

            ;; There should not be any visible edit inputs on initial render
            (th/assert-equal (.-length (.querySelectorAll container "li .edit")) 0)))))

    (it "should add a new todo"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [input (.querySelector container "#new-todo")]
            (set! (.-value input) "four")
            (.dispatchEvent input (new js/Event "input" #js {:bubbles true}))
            (.dispatchEvent input (new js/KeyboardEvent "keydown" #js {:code "Enter", :bubbles true}))

            (let [todo-items (.querySelectorAll container "#todo-list li")]
              (th/assert-equal (.-length todo-items) 4)
              (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "3")
              (th/assert-equal (.-textContent (.querySelector (aget todo-items 3) "label")) "four")))))) 

    (it "should toggle a todo"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [item1-checkbox (.querySelector (aget (.querySelectorAll container "#todo-list li") 0) ".toggle")]
            (.click item1-checkbox)
            (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "1")
            (th/assert-equal (.contains (.-classList (aget (.querySelectorAll container "#todo-list li") 0)) "completed") true))))) 

    (it "should delete a todo"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [delete-btn (.querySelector (aget (.querySelectorAll container "#todo-list li") 0) ".destroy")]
            (.click delete-btn)
            (let [todo-items (.querySelectorAll container "#todo-list li")]
              (th/assert-equal (.-length todo-items) 2)
              (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "1"))))))

    (it "should edit a todo"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [item1-li (aget (.querySelectorAll container "#todo-list li") 0)
                item1-label (.querySelector item1-li "label")]

            (.dispatchEvent item1-label (new js/Event "dblclick" #js {:bubbles true}))

            (let [edit-input (.querySelector item1-li ".edit")]
              (th/assert-not-nil edit-input)
              (th/assert-equal (.-value edit-input) "one")

              (set! (.-value edit-input) "one (edited)")
              (.dispatchEvent edit-input (new js/Event "input" #js {:bubbles true}))
              (.dispatchEvent edit-input (new js/KeyboardEvent "keydown" #js {:code "Enter", :bubbles true}))

              (th/assert-equal (.-length (.querySelectorAll container "li .edit")) 0)
              (th/assert-equal (.-textContent (.querySelector item1-li "label")) "one (edited)"))))))

    (it "should cancel editing on Escape"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [item1-li (aget (.querySelectorAll container "#todo-list li") 0)
                item1-label (.querySelector item1-li "label")]

            (.dispatchEvent item1-label (new js/Event "dblclick" #js {:bubbles true}))

            (let [edit-input (.querySelector item1-li ".edit")]
              (set! (.-value edit-input) "one (edited)")
              (.dispatchEvent edit-input (new js/Event "input" #js {:bubbles true}))
              (.dispatchEvent edit-input (new js/KeyboardEvent "keydown" #js {:code "Escape", :bubbles true}))

              (th/assert-equal (.-length (.querySelectorAll container "li .edit")) 0)
              (th/assert-equal (.-textContent (.querySelector item1-li "label")) "one"))))))

    (it "should save on blur"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [item1-li (aget (.querySelectorAll container "#todo-list li") 0)
                item1-label (.querySelector item1-li "label")]

            (.dispatchEvent item1-label (new js/Event "dblclick" #js {:bubbles true}))

            (let [edit-input (.querySelector item1-li ".edit")]
              (set! (.-value edit-input) "one (blurred)")
              (.dispatchEvent edit-input (new js/Event "input" #js {:bubbles true}))
              (.dispatchEvent edit-input (new js/Event "blur" #js {:bubbles true}))

              (th/assert-equal (.-length (.querySelectorAll container "li .edit")) 0)
              (th/assert-equal (.-textContent (.querySelector item1-li "label")) "one (blurred)"))))))

    (it "should clear completed todos"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [clear-btn (.querySelector container "#clear-completed")]
            (.click clear-btn)
            (let [todo-items (.querySelectorAll container "#todo-list li")]
              (th/assert-equal (.-length todo-items) 2)
              (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "2")))))

    (it "should complete all todos"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [toggle-all-checkbox (.querySelector container "#toggle-all")]
            (.click toggle-all-checkbox)
            (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "0")
            (th/assert-equal (.-length (.querySelectorAll container "#todo-list li.completed")) 3)

            (.click toggle-all-checkbox)
            (th/assert-equal (.-textContent (.querySelector container "#todo-count strong")) "3")
            (th/assert-equal (.-length (.querySelectorAll container "#todo-list li.completed")) 0)))))

    (it "should filter todos"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [todomvc-page] container)

          (let [filters-el (.querySelector container "#filters")
                all-filter (aget (.querySelectorAll filters-el "a") 0)
                active-filter (aget (.querySelectorAll filters-el "a") 1)
                completed-filter (aget (.querySelectorAll filters-el "a") 2)]

            (.click active-filter)
            (th/assert-equal (.-length (.querySelectorAll container "#todo-list li")) 2)

            (.click completed-filter)
            (th/assert-equal (.-length (.querySelectorAll container "#todo-list li")) 1)

            (.click all-filter)
            (th/assert-equal (.-length (.querySelectorAll container "#todo-list li")) 3)))))))) 

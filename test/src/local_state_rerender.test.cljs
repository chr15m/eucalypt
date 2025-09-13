(ns local-state-rerender.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

;; A component with local state that controls rendering
(defn editable-component []
  (let [editing? (r/ratom false)]
    (fn []
      [:div {:class (when @editing? "editing")}
       (if @editing?
         [:input {:type "text" :value "Editing..."}]
         [:p "Not editing"])
       [:button {:id "edit-btn"
                 :on-click #(reset! editing? true)}
        "Edit"]])))

(describe "Component re-render on local state change"
  (fn []
    (it "should update component when its local state changes"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [editable-component] container)

          (let [div (.querySelector container "div")
                button (.querySelector container "#edit-btn")]

            ;; Initial state
            (th/assert-not-nil (.querySelector container "p"))
            (th/assert-equal (.querySelector container "input") nil)
            (th/assert-equal (.contains (.-classList div) "editing") false)

            ;; Click button to change local state
            (.click button)

            ;; Assert DOM has updated
            (th/assert-equal (.querySelector container "p") nil)
            (th/assert-not-nil (.querySelector container "input"))
            (th/assert-equal (.contains (.-classList div) "editing") true))))))) 

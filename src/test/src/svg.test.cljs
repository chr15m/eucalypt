(ns svg.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(defn skateboarding-icon []
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :width "24" :height "24"
         :viewBox "0 0 24 24"
         :fill "none"
         :stroke "currentColor"
         :stroke-width "2"
         :stroke-linecap "round"
         :stroke-linejoin "round"}
   [:circle {:cx "5.5" :cy "18.5" :r "1.5"}]
   [:circle {:cx "18.5" :cy "18.5" :r "1.5"}]
   [:path {:d "M12 12H6l-3 6h18l-3-6h-6"}]
   [:path {:d "M6 12V7a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v5"}]])

(defn svg-page []
  [:div
   [:h2 "SVG Test"]
   [skateboarding-icon]])

(describe "SVG Component"
  (fn []
    (it "should render SVG correctly"
      (fn []
        (let [container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [svg-page] container)
          (let [svg (.querySelector container "svg")]
            (th/assert-not-nil svg)
            (th/assert-equal (.-namespaceURI svg) "http://www.w3.org/2000/svg")
            (th/assert-equal (.getAttribute svg "width") "24")
            (th/assert-equal (count (.querySelectorAll svg "circle")) 2)
            (th/assert-equal (count (.querySelectorAll svg "path")) 2)))))

    (it "should render SVG children with the correct namespace"
      (fn []
        (let [container (.createElement js/document "div")
              svg-ns "http://www.w3.org/2000/svg"]
          (.appendChild js/document.body container)
          (r/render
            [:svg {:id "outer" :xmlns svg-ns}
             [:g {:id "group"}
              [:circle {:id "inner-circle" :cx "0" :cy "0" :r "5"}]
              [:text {:id "inner-text"} "Label"]]]
            container)
          (let [group (.querySelector container "#group")
                circle (.querySelector container "#inner-circle")
                text (.querySelector container "#inner-text")]
            (th/assert-not-nil group)
            (th/assert-not-nil circle)
            (th/assert-not-nil text)
            (th/assert-equal (.-namespaceURI group) svg-ns)
            (th/assert-equal (.-namespaceURI circle) svg-ns)
            (th/assert-equal (.-namespaceURI text) svg-ns)))))

    (it "should inherit correct namespace URI from parent SVG elements"
      (fn []
        (let [container (.createElement js/document "div")
              svg-ns "http://www.w3.org/2000/svg"]
          (.appendChild js/document.body container)
          (r/render
            [:svg {:id "root-svg" :xmlns svg-ns}
             [:svg {:id "inner-svg"}
              [:g {:id "nested-group"}
               [:rect {:id "nested-rect" :width "10" :height "10"}]]]]
            container)
          (let [inner-svg (.querySelector container "#inner-svg")
                nested-group (.querySelector container "#nested-group")
                nested-rect (.querySelector container "#nested-rect")]
            (th/assert-not-nil inner-svg)
            (th/assert-not-nil nested-group)
            (th/assert-not-nil nested-rect)
            (th/assert-equal (.-namespaceURI inner-svg) svg-ns)
            (th/assert-equal (.-namespaceURI nested-group) svg-ns)
            (th/assert-equal (.-namespaceURI nested-rect) svg-ns)))))

    (it "should switch back to HTML namespace within foreignObject"
      (fn []
        (let [container (.createElement js/document "div")
              svg-ns "http://www.w3.org/2000/svg"
              html-ns "http://www.w3.org/1999/xhtml"]
          (.appendChild js/document.body container)
          (r/render
            [:svg {:id "foreign-root" :xmlns svg-ns}
             [:foreignObject {:width "100" :height "50"}
              [:div {:id "foreign-div"}
               [:p {:id "foreign-paragraph"} "Foreign content"]]]]
            container)
          (let [foreign-object (.querySelector container "foreignObject")
                foreign-div (.querySelector container "#foreign-div")
                foreign-paragraph (.querySelector container "#foreign-paragraph")]
            (th/assert-not-nil foreign-object)
            (th/assert-not-nil foreign-div)
            (th/assert-not-nil foreign-paragraph)
            (th/assert-equal (.-namespaceURI foreign-object) svg-ns)
            (th/assert-equal (.-namespaceURI foreign-div) html-ns)
            (th/assert-equal (.-namespaceURI foreign-paragraph) html-ns)))))

    (it "should transition from DOM to SVG and back"
      (fn []
        (let [container (.createElement js/document "div")
              svg-ns "http://www.w3.org/2000/svg"
              html-ns "http://www.w3.org/1999/xhtml"]
          (.appendChild js/document.body container)

          ;; Phase 1: render plain DOM
          (r/render [:div {:id "phase-one"} "HTML phase"] container)
          (let [phase-one (.querySelector container "#phase-one")]
            (th/assert-not-nil phase-one)
            (th/assert-equal (.-namespaceURI phase-one) html-ns)
            (th/assert-equal (.querySelector container "svg") nil))

          ;; Phase 2: render SVG
          (r/render
            [:svg {:id "phase-two" :xmlns svg-ns}
             [:circle {:id "phase-two-circle" :cx "5" :cy "5" :r "5"}]]
            container)
          (let [phase-two (.querySelector container "#phase-two")
                phase-two-circle (.querySelector container "#phase-two-circle")]
            (th/assert-not-nil phase-two)
            (th/assert-not-nil phase-two-circle)
            (th/assert-equal (.-namespaceURI phase-two) svg-ns)
            (th/assert-equal (.-namespaceURI phase-two-circle) svg-ns))

          ;; Phase 3: return to DOM
          (r/render [:div {:id "phase-three"} "Back to HTML"] container)
          (let [phase-three (.querySelector container "#phase-three")]
            (th/assert-not-nil phase-three)
            (th/assert-equal (.-namespaceURI phase-three) html-ns)
            (th/assert-equal (.querySelector container "svg") nil)))))))

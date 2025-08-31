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
            (th/assert-equal (count (.querySelectorAll svg "path")) 2)))))))

(ns dangerously-set-inner-html.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
 (fn []
   (set! (.-innerHTML js/document.body) "")))

(describe "dangerouslySetInnerHTML"
  (fn []
    (it "should set innerHTML"
      (fn []
        (let [html-string "<b>foo &amp; bar</b>"
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:dangerouslySetInnerHTML {:__html html-string}}] container)
          ;; happy-dom decodes entities but doesn't re-encode them on read
          ;; In a browser, this would be "<b>foo &amp; bar</b>"
          (th/assert-equal (.-innerHTML (.-firstChild container)) "<b>foo & bar</b>")
          (th/assert-equal (.-innerHTML container) "<div><b>foo & bar</b></div>"))))

    (it "should unset innerHTML when updated"
      (fn []
        (let [html-string "<b>foo &amp; bar</b>"
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:dangerouslySetInnerHTML {:__html html-string}}] container)
          ;; happy-dom decodes entities but doesn't re-encode them on read
          ;; In a browser, this would be "<div><b>foo &amp; bar</b></div>"
          (th/assert-equal (.-innerHTML container) "<div><b>foo & bar</b></div>")

          (r/render [:div [:p "text"]] container)
          (th/assert-equal (.-innerHTML container) "<div><p>text</p></div>"))))

    (it "should update innerHTML"
      (fn []
        (let [html-string-1 "<b>foo</b>"
              html-string-2 "<i>bar</i>"
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:dangerouslySetInnerHTML {:__html html-string-1}}] container)
          (th/assert-equal (.-innerHTML container) (str "<div>" html-string-1 "</div>"))

          (r/render [:div {:dangerouslySetInnerHTML {:__html html-string-2}}] container)
          (th/assert-equal (.-innerHTML container) (str "<div>" html-string-2 "</div>")))))

    (it "should not render children when dangerouslySetInnerHTML is present"
      (fn []
        (let [html-string "<b>foo</b>"
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [:div {:dangerouslySetInnerHTML {:__html html-string}} [:p "I should not be rendered"]] container)
          (th/assert-equal (.-innerHTML container) (str "<div>" html-string "</div>")))))

    (it "should not execute scripts on creation"
      (fn []
        (let [html-string "<img src=x onerror='window.vulnerable = true'>"
              expected-html "<div><img src=\"x\" onerror=\"window.vulnerable = true\"></div>"
              container (.createElement js/document "div")]
          (aset js/window "vulnerable" false)
          (.appendChild js/document.body container)
          (r/render [:div {:dangerouslySetInnerHTML {:__html html-string}}] container)
          ;; happy-dom normalizes attributes with quotes
          ;; In a browser, this would also have quotes.
          (th/assert-equal (.-innerHTML container) expected-html)
          (th/assert-equal (aget js/window "vulnerable") false))))

    (it "should apply proper mutation for VNodes with dangerouslySetInnerHTML attr"
      (fn []
        (let [state (r/atom {:html "<b><i>test</i></b>"})
              component (fn []
                          (if-let [html (:html @state)]
                            [:div {:dangerouslySetInnerHTML {:__html html}}]
                            [:div]))
              container (.createElement js/document "div")]
          (.appendChild js/document.body container)
          (r/render [component] container)
          (th/assert-equal (.-innerHTML container) "<div><b><i>test</i></b></div>")

          (reset! state {:html nil})
          (th/assert-equal (.-innerHTML container) "<div></div>")

          (reset! state {:html "<foo><bar>test</bar></foo>"})
          (th/assert-equal (.-innerHTML container) "<div><foo><bar>test</bar></foo></div>"))))))

(ns runreagent
  (:require
    ["fs" :as fs]
    ["happy-dom" :as hd]))

(let [script (fs/readFileSync "reagent-update-script.cljs" "utf8")
      react-dom (js/require "react-dom")
      react (js/require "react")
      window (hd/Window.)
      document (aget window "document")
      scittle (fs/readFileSync
                "node_modules/scittle/dist/scittle.js" "utf8")
      scittle-reagent (fs/readFileSync
                        "node_modules/scittle/dist/scittle.reagent.js" "utf8")]
  (aset js/globalThis "ReactDOM" react-dom)
  (aset js/globalThis "React" react)
  (aset js/globalThis "document" document)
  (aset js/globalThis "window" window)
  (let [app-div (doto (.createElement document "div")
                  (aset "id" "app"))]
    (-> document .-body (.appendChild app-div))
    (js/eval.call js/globalThis scittle)
    (js/eval.call js/globalThis scittle-reagent)
    (js/scittle.core.eval_string script)
    (-> document (.querySelector "button") (.click))
    (js/setTimeout
      (fn [] (js/console.log (-> document .-body (.toString)))) 
      0)))

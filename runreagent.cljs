(ns runreagent
  (:require
    ["fs" :as fs]
    ["process" :as process]
    ["happy-dom" :as hd]))

(defn eval-file [path]
  (let [file-content (fs/readFileSync path "utf8")]
    (js/eval.call js/globalThis file-content)))

(let [script-path (last (aget process "argv"))
      script (fs/readFileSync script-path "utf8")
      react-dom (js/require "react-dom")
      react (js/require "react")
      window (hd/Window.)
      document (aget window "document")]
  (aset js/globalThis "ReactDOM" react-dom)
  (aset js/globalThis "React" react)
  (aset js/globalThis "document" document)
  (aset js/globalThis "window" window)
  (let [app-div (doto (.createElement document "div")
                  (aset "id" "app"))]
    (-> document .-body (.appendChild app-div))
    (eval-file "node_modules/scittle/dist/scittle.js")
    (eval-file "node_modules/scittle/dist/scittle.reagent.js")
    (js/scittle.core.eval_string script)
    (-> document (.querySelector "button") (.click))
    (js/setTimeout
      (fn [] (js/console.log (-> document .-body (.toString)))) 
      0)))

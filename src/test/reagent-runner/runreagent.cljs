(ns runreagent
  (:require
    ["fs" :as fs]
    ["process" :as process]
    ["happy-dom" :as hd]))

(defn eval-file [path]
  (let [file-content (fs/readFileSync path "utf8")]
    (js/eval.call js/globalThis file-content)))

(defn read-file [path]
  (fs/readFileSync path "utf8"))

(let [script-path (last (aget process "argv"))
      test-script-raw (read-file script-path)
      ;; Replace JS module requires with ClojureScript namespace requires
      test-script (-> test-script-raw
                      (.replace #"\[\"vitest\" :refer \[(.*?)\]\]"
                                "[vitest :refer [$1]]"))
      react-dom (js/require "react-dom")
      react (js/require "react")
      window (hd/Window.)
      document (aget window "document")]
  ;; Set up global environment
  (aset js/globalThis "ReactDOM" react-dom)
  (aset js/globalThis "React" react)
  (aset js/globalThis "document" document)
  (aset js/globalThis "window" window)

  ;; Create #app div
  (let [app-div (doto (.createElement document "div")
                  (aset "id" "app"))]
    (-> document .-body (.appendChild app-div)))

  ;; Load Scittle
  (eval-file "node_modules/scittle/dist/scittle.js")
  (eval-file "node_modules/scittle/dist/scittle.reagent.js")

  ;; Load shims into Scittle environment
  (js/scittle.core.eval_string (read-file "src/test/reagent-runner/vitest-shim.cljs"))
  (js/scittle.core.eval_string (read-file "src/test/reagent-runner/eucalypt-shim.cljs"))
  (js/scittle.core.eval_string (read-file "src/test/reagent-runner/helpers-shim.cljs"))

  ;; Run the test file
  (js/scittle.core.eval_string test-script)

  ;; Print test summary
  (js/scittle.core.eval_string "(vitest/print-summary)"))

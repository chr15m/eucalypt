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
      test-utils (js/require "react-dom/test-utils")
      window (hd/Window.)
      document (aget window "document")]
  ;; Set up global environment - copy all window properties to globalThis
  (doseq [k (js/Object.keys window)]
    (when-not (aget js/globalThis k)
      (aset js/globalThis k (aget window k))))
  (aset js/globalThis "ReactDOM" react-dom)
  (aset js/globalThis "React" react)
  (aset js/globalThis "ReactTestUtils" test-utils)
  (aset js/globalThis "document" document)
  (aset js/globalThis "window" window)

  ;; Patch Happy DOM / Node Event.prototype.eventPhase getter-only quirk for React 17
  (doseq [event-cls [(aget window "Event") js/globalThis.Event]]
    (when (and event-cls (.-prototype event-cls))
      (js/Object.defineProperty
       (.-prototype event-cls)
       "eventPhase"
       #js {:get (js/eval "(function() { return this._eventPhase || 0; })")
            :set (js/eval "(function(v) { this._eventPhase = v; })")
            :configurable true
            :enumerable true})))

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
  (js/scittle.core.eval_string (-> (read-file "src/test/src/helpers.cljs")
                                    (.replace #"\[\"vitest\" :refer \[(.*?)\]\]"
                                              "[vitest :refer [$1]]")))

  ;; Run the test file
  (try
    (js/scittle.core.eval_string test-script)
    (catch :default e
      (js/console.error e)
      (js/process.exit 1)))

  ;; Await the sequential test promise chain, then print summary
  (js/scittle.core.eval_string
   "(.then vitest/test-promise
      (fn [] (vitest/print-summary))
      (fn [e] (js/console.error e) (js/process.exit 1)))"))

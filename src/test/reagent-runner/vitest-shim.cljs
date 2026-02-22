#_:clj-kondo/ignore (ns vitest)

;; Test state tracking
(def ^:dynamic *current-suite* nil)
(def ^:dynamic *after-each-hooks* [])
(def test-results (atom {:suites [] :total 0 :passed 0 :failed 0}))

(defn describe [suite-name test-fn]
  (js/console.log (str "\n" suite-name))
  (binding [*current-suite* suite-name
            *after-each-hooks* []]
    (test-fn)))

(defn it [test-name test-fn]
  (swap! test-results update :total inc)
  (try
    (test-fn)
    ;; Run afterEach hooks
    (doseq [hook *after-each-hooks*]
      (hook))
    (swap! test-results update :passed inc)
    (js/console.log (str "  ✓ " test-name))
    (catch js/Error e
      (swap! test-results update :failed inc)
      (js/console.log (str "  ✗ " test-name))
      (js/console.log (str "    Error: " (.-message e))))))

(defn afterEach [hook-fn]
  (set! *after-each-hooks* (conj *after-each-hooks* hook-fn)))

;; Expectation API
(defn expect [actual]
  (js-obj
    "not" #js {:toBeNull (fn []
                           (when (nil? actual)
                             (throw (js/Error. (str "Expected value not to be nil, but it was nil")))))
               :toBe (fn [expected]
                       (when (= actual expected)
                         (throw (js/Error. (str "Expected " (pr-str actual) " not to be " (pr-str expected))))))}
    "toEqual" (fn [expected]
                (when-not (= actual expected)
                  (throw (js/Error. (str "Expected " (pr-str actual) " to equal " (pr-str expected))))))
    "toBe" (fn [expected]
             (when-not (identical? actual expected)
               (throw (js/Error. (str "Expected " (pr-str actual) " to be " (pr-str expected))))))))

(defn print-summary []
  (let [results @test-results
        total (:total results)
        passed (:passed results)
        failed (:failed results)]
    (js/console.log "\n---")
    (js/console.log (str "Tests: " passed " passed, " failed " failed, " total " total"))))

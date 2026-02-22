#_:clj-kondo/ignore (ns vitest)

;; Test state tracking
(def after-each-hooks (atom []))
(def registered-tests (atom []))
(def test-results (atom {:total 0 :passed 0 :failed 0}))
(def test-promise nil)

(defn afterEach [hook-fn]
  (swap! after-each-hooks conj hook-fn))

(defn it [test-name test-fn]
  (swap! registered-tests conj {:test-name test-name :testfunc test-fn}))

(defn run-test [{:keys [test-name testfunc]}]
  (swap! test-results update :total inc)
  (let [hooks @after-each-hooks
        result (try (testfunc) (catch js/Error e e))]
    (-> (js/Promise.resolve result)
        (.then (fn [_]
                 (doseq [hook hooks] (hook))
                 (swap! test-results update :passed inc)
                 (js/console.log (str "  ✓ " test-name))))
        (.catch (fn [e]
                  (doseq [hook hooks] (hook))
                  (swap! test-results update :failed inc)
                  (js/console.log (str "  ✗ " test-name))
                  (js/console.log (str "    Error: " (.-message e))))))))

(defn describe [suite-name test-fn]
  (reset! registered-tests [])
  (test-fn)
  (let [tests @registered-tests
        chain (reduce (fn [chain test]
                        (.then chain (fn [] (run-test test))))
                      (-> (or test-promise (js/Promise.resolve nil))
                          (.then (fn [] (js/console.log (str "\n" suite-name)))))
                      tests)]
    (set! test-promise chain)))

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

(ns mr-clean
  (:require [clojure.string :as str]))

(defn- with-meta* [obj m]
  (aset obj "---meta" m)
  obj)

(defn- meta* [obj]
  (aget obj "---meta"))

(defn- walk [inner outer form]
  (js/console.log "walk called with form:" form)
  (let [m (meta* form)]
    (cond
      (string? form)
      (do (js/console.log "walk: string branch")
          (outer form))

      (list? form)
      (do (js/console.log "walk: list branch")
          (outer (let [res (apply list (map inner form))]
                   (if m (with-meta* res m) res))))

      (and (coll? form) (not (fn? form)))
      (do (js/console.log "walk: coll branch")
          (outer (let [res (into (empty form) (map inner form))]
                   (if m (with-meta* res m) res))))

      (seq? form)
      (do (js/console.log "walk: seq branch")
          (outer (let [res (doall (map inner form))]
                   (if m (with-meta* res m) res))))

      :else
      (do (js/console.log "walk: else branch")
          (outer form)))))

(defn- postwalk [f form]
  (walk (partial postwalk f) f form))

(defonce ^:dynamic *watcher* nil)
(defonce create-class identity)
(defonce dom-node identity)

(defonce mounted-components (atom {}))
(defonce container->mounted-component (atom {}))

;; *** hiccup-to-dom implementation ***

(declare hiccup->dom)

(defn- style-map->css-str [style-map]
  (apply str (map (fn [[k v]] (str k ":" v ";")) style-map)))

(defn- set-attributes! [element attrs]
  (doseq [[k v] attrs]
    (cond
      (str/starts-with? k "on-")
      (aset element (str/replace k #"-" "") v)

      (= :style k)
      (aset element "style" (style-map->css-str v))

      :else
      (.setAttribute element k v))))

(defn- create-element [hiccup]
  (let [[tag & content] hiccup
        attrs (if (map? (first content)) (first content) {})
        content (if (map? (first content)) (rest content) content)
        element (.createElement js/document tag)]
    (set-attributes! element attrs)
    (doseq [child content]
      (when-let [child-node (hiccup->dom child)]
        (.appendChild element child-node)))
    element))

(defn hiccup->dom [hiccup]
  (js/console.log "hiccup->dom called with:" hiccup)
  (let [result (cond
                 (or (string? hiccup) (number? hiccup)) (.createTextNode js/document (str hiccup))
                 (vector? hiccup) (let [[tag & content] hiccup]
                                    (cond
                                      (fn? tag) (hiccup->dom (apply tag content))
                                      (= :<> tag) (let [fragment (.createDocumentFragment js/document)]
                                                    (doseq [child (rest hiccup)]
                                                      (when-let [child-node (hiccup->dom child)]
                                                        (.appendChild fragment child-node)))
                                                    fragment)
                                      :else (create-element hiccup)))
                 (seq? hiccup) (let [fragment (.createDocumentFragment js/document)]
                                 (doseq [item hiccup]
                                   (when-let [child-node (hiccup->dom item)]
                                     (.appendChild fragment child-node)))
                                 fragment)
                 (fn? hiccup) (hiccup->dom (hiccup))
                 (map? hiccup) (hiccup->dom ((:reagent-render hiccup)))
                 (nil? hiccup) nil
                 :else (.createTextNode js/document (str hiccup)))]
    (js/console.log "hiccup->dom returning:" result)
    result))

;; Component support
(defn component? [x]
  (and (fn? x) (not (contains? (meta x) :component))))


;; *** mr clean implementation continues ***


;; (extend-type js/NodeList
;;   ISeqable
;;   (-seq [nodeList] (vec (array-seq nodeList))))

;; (extend-type js/NamedNodeMap
;;   ISeqable
;;   (-seq [named-node-map] (vec (array-seq named-node-map))))

(defn hiccup-eq? [hiccup-a hiccup-b]
  (let [a-str (.stringify js/JSON hiccup-a)
        b-str (.stringify js/JSON hiccup-b)
        result (= a-str b-str)]
    (js/console.log "hiccup-eq?:" result "a-str:" a-str "b-str:" b-str)
    (when (not result)
      (js/console.log "hiccup-eq? details: a:" hiccup-a "b:" hiccup-b))
    result))

(declare fully-render-hiccup)
(declare patch)

(defn- get-hiccup-children [hiccup]
  (let [content (rest hiccup)]
    (if (map? (first content))
      (rest content)
      content)))

(defn- fully-render-hiccup [hiccup]
  (js/console.log "fully-render-hiccup called with:" hiccup)
  (let [result (cond
                 (vector? hiccup)
                 (let [tag (first hiccup)]
                   (if (fn? tag)
                     (let [res (apply tag (rest hiccup))]
                       (if (map? res)
                         ;; This is a component that returned a map with :reagent-render
                         (fully-render-hiccup ((:reagent-render res)))
                         (fully-render-hiccup res)))
                     (let [attrs (when (map? (second hiccup)) (second hiccup))
                           children (if attrs (drop 2 hiccup) (rest hiccup))]
                       (if attrs
                         (into [(first hiccup) attrs] (map fully-render-hiccup children))
                         (into [(first hiccup)] (map fully-render-hiccup children))))))
                 (map? hiccup)
                 ;; This is a map component directly in the hiccup tree
                 (fully-render-hiccup ((:reagent-render hiccup)))
                 :else
                 hiccup)]
    (js/console.log "fully-render-hiccup returning:" result)
    result))

(defn- patch-children [hiccup-a-rendered hiccup-b-rendered dom-a]
  (js/console.log "--- patch-children start ---")
  (js/console.log "patch-children: hiccup-a-rendered" hiccup-a-rendered)
  (js/console.log "patch-children: hiccup-b-rendered" hiccup-b-rendered)
  (let [children-a (get-hiccup-children hiccup-a-rendered)
        children-b (get-hiccup-children hiccup-b-rendered)
        dom-nodes (atom (vec (aget dom-a "childNodes")))
        len-a (count children-a)
        len-b (count children-b)]
    (js/console.log "patch-children: children-a" children-a)
    (js/console.log "patch-children: children-b" children-b)
    (js/console.log "patch-children: dom-nodes" @dom-nodes)
    ;; Patch or replace existing nodes
    (doseq [i (range (min len-a len-b))]
      (let [child-a (nth children-a i)
            child-b (nth children-b i)
            dom-node (nth @dom-nodes i)
            new-dom-node (patch child-a child-b dom-node)]
        (js/console.log "patch-children: comparing child" i child-a child-b)
        (when (not= dom-node new-dom-node)
          (swap! dom-nodes assoc i new-dom-node))))
    ;; Add new nodes
    (when (> len-b len-a)
      (js/console.log "patch-children: adding new nodes")
      (doseq [i (range len-a len-b)]
        (.appendChild dom-a (hiccup->dom (nth children-b i)))))
    ;; Remove surplus nodes
    (when (> len-a len-b)
      (js/console.log "patch-children: removing surplus nodes")
      (doseq [i (range (dec len-a) (dec len-b) -1)]
        (.remove (nth @dom-nodes i))))))

(defn- get-attrs [hiccup]
  (let [s (second hiccup)]
    (if (map? s) s {})))

(defn- patch-attributes [hiccup-a-rendered hiccup-b-rendered dom-a]
  (let [a-attrs (get-attrs hiccup-a-rendered)
        b-attrs (get-attrs hiccup-b-rendered)]
    ;; Remove attributes from a that are not in b
    (doseq [[k _] a-attrs]
      (when-not (contains? b-attrs k)
        (if (str/starts-with? k "on-")
          (aset dom-a (str/replace k #"-" "") nil)
          (.removeAttribute dom-a k))))
    ;; Add/update attributes from b
    (doseq [[k v] b-attrs]
      (let [old-v (get a-attrs k)]
        (if (str/starts-with? k "on-")
          (aset dom-a (str/replace k #"-" "") v)
          (when (not= v old-v)
            (let [val-str (if (= k :style)
                            (style-map->css-str v)
                            v)]
              (.setAttribute dom-a k val-str))))))))

(defn- component->hiccup [normalized-component]
  (let [[config & params] normalized-component]
    (apply (:reagent-render config) params)))

(defn patch
  "transform dom-a to dom representation of hiccup-b.
  if hiccup-a and hiccup-b are not the same element type, then a new dom element is created from hiccup-b."
  [hiccup-a-rendered hiccup-b-rendered dom-a]
  (js/console.log "patch: hiccup-a-rendered" hiccup-a-rendered "hiccup-b-rendered" hiccup-b-rendered "dom-a" dom-a)
  (cond
    (hiccup-eq? hiccup-a-rendered hiccup-b-rendered)
    (do (js/console.log "patch: hiccup is equal, doing nothing")
        dom-a)

    (or (not (vector? hiccup-a-rendered)) (not (vector? hiccup-b-rendered)) (not= (first hiccup-a-rendered) (first hiccup-b-rendered)))
    (let [new-node (hiccup->dom hiccup-b-rendered)]
      (js/console.log "patch: replacing node" dom-a "with" new-node)
      (.replaceWith dom-a new-node)
      new-node)

    :else
    (do (js/console.log "patch: hiccup not equal, patching children and attributes")
        (patch-attributes hiccup-a-rendered hiccup-b-rendered dom-a)
        (patch-children hiccup-a-rendered hiccup-b-rendered dom-a)
        dom-a)))

(defn modify-dom [normalized-component]
  (js/console.log "modify-dom called for component:" normalized-component)
  (let [[{:keys [_reagent-render]} & _params] normalized-component
        {:keys [hiccup dom container]} (get @mounted-components normalized-component)
        new-hiccup-unrendered (component->hiccup normalized-component)
        new-hiccup-rendered (fully-render-hiccup new-hiccup-unrendered)
        new-dom (patch hiccup new-hiccup-rendered dom)]
    (js/console.log "modify-dom: new DOM" new-dom)
    (swap! mounted-components assoc normalized-component {:hiccup new-hiccup-rendered
                                                          :dom new-dom
                                                          :container container})
    (when (not= dom new-dom)
      (js/console.log "modify-dom: DOM changed, replacing in container")
      (aset container "innerHTML" "")
      (.appendChild container new-dom))))

(defn notify-watchers [watchers]
  (js/console.log "notify-watchers called with" (count @watchers) "watchers")
  (doseq [watcher @watchers]
    (js/console.log "calling watcher")
    (watcher)))

(defn- add-modify-dom-watcher-on-ratom-deref
  "This is where the magic of adding watchers to ratoms happen automatically.
  This is achieved by setting the dnymaic var *watcher* then evaluating reagent-render
  which causes the deref of the ratom to trigger adding the watcher to (.-watchers ratom)"
  [normalized-component]
  (let [old-watcher *watcher*]
    (try
      (set! *watcher* (with-meta* #(modify-dom normalized-component)
                        {:normalized-component normalized-component}))
      (let [reagent-render (-> normalized-component first :reagent-render)
            params (rest normalized-component)
            hiccup (apply reagent-render params)
            dom (hiccup->dom hiccup)]
        [hiccup dom])
      (finally
        (set! *watcher* old-watcher)))))

(defn ratom [initial-value]
  (let [a (atom initial-value)
        orig-deref (aget a "_deref")
        orig-reset_BANG_ (aget a "_reset_BANG_")]
    (aset a "watchers" (atom #{}))
    (aset a "cursors" (atom #{}))
    (aset a "_deref" (fn []
                      (when *watcher*
                        (js/console.log "ratom deref: watcher found, adding to set.")
                        (swap! (aget a "watchers") conj *watcher*))
                      (.call orig-deref a)))
    (aset a "_reset_BANG_" (fn [new-val]
                             (js/console.log "ratom _reset_BANG_ called with" new-val)
                             (let [res (.call orig-reset_BANG_ a new-val)]
                               (notify-watchers (aget a "watchers"))
                               (doseq [c @(aget a "cursors")]
                                 (notify-watchers (aget c "watchers")))
                               res)))
    a))

(defn cursor [the-ratom path]
  (let [cursors (aget the-ratom "cursors")
        found-cursor (some (fn [c] (when (= path (aget c "path")) c)) @cursors)]
    (if (nil? found-cursor)
      (let [watchers (atom #{})
            this-cursor (js-obj
                         "_deref" (fn []
                                   (when *watcher*
                                     (swap! watchers conj *watcher*))
                                   (let [old-watcher *watcher*]
                                     (try
                                       (set! *watcher* nil)
                                       (get-in @the-ratom path)
                                       (finally
                                         (set! *watcher* old-watcher)))))
                         "_swap" (fn [f & args]
                                  (swap! the-ratom
                                    (fn [current-state]
                                      (let [current-cursor-value (get-in current-state path)
                                            new-cursor-value (apply f current-cursor-value args)]
                                        (assoc-in current-state path new-cursor-value)))))
                         "watchers" watchers
                         "path" path)]
        (swap! cursors conj this-cursor)
        this-cursor)
      found-cursor)))

(defn reaction [f & params]
  (let [ra (ratom nil)
        watcher #(reset! ra (apply f params))]
    (let [old-watcher *watcher*]
      (try
        (set! *watcher* watcher)
        (watcher)
        (let [reaction-obj (js-obj
                            "_deref" (fn [] @ra)
                            "_swap" (fn [& _] (throw (js/Error. "Reactions are readonly"))))]
          (aset reaction-obj "watchers" (aget ra "watchers"))
          reaction-obj)
        (finally
          (set! *watcher* old-watcher))))))

(defn- rm-watchers [normalized-component]
  (let [params (rest normalized-component)]
    (doseq [p params
            :when (and (object? p) (aget p "watchers"))]
      (let [watchers (aget p "watchers")]
        (doseq [w @watchers
                :when (= (-> w meta* :normalized-component)
                         normalized-component)]
          (swap! watchers (fn [watchers]
                                         (set (remove #(= w %) watchers)))))))))

(defonce life-cycle-methods {:get-initial-state (fn [_this])
                             :component-will-receive-props identity
                             :should-component-update identity
                             :component-will-mount identity
                             :component-did-mount identity
                             :component-will-update identity
                             :component-did-update identity
                             :component-will-unmount rm-watchers})

(defn normalize-component [component]
  (when (sequential? component)
    (let [first-element (first component)
          params (rest component)]
      (cond (fn? first-element) (let [a-fn first-element
                                      func-or-hiccup (apply a-fn params)
                                      render-fn (if (fn? func-or-hiccup)
                                                  func-or-hiccup
                                                  a-fn)
                                      comp-with-lifecycle (merge life-cycle-methods
                                                                 {:reagent-render render-fn})]
                                  (into [comp-with-lifecycle] params))
            (keyword? first-element) (into [(assoc life-cycle-methods :reagent-render (fn [] component))]
                                           params)
            (map? first-element) (let [component-as-map first-element
                                       render-fn (:reagent-render component-as-map)
                                       comp-with-lifecycle (into {:reagent-render render-fn}
                                                                 (map (fn [[k func]]
                                                                        (let [func2 (get component-as-map k)
                                                                              func-func2 (if func2 (comp func2 func) func)]
                                                                          [k func-func2]))
                                                                      life-cycle-methods))]
                                   (into [comp-with-lifecycle] params))))))

(defn unmount-components [container]
  (when-let [mounted-component (get @container->mounted-component container)]
    (let [[{:keys [component-will-unmount]} & _params] mounted-component]
      (component-will-unmount mounted-component)
      (swap! container->mounted-component dissoc container)))
  (aset container "innerHTML" ""))

(defn do-render [normalized-component container]
  (unmount-components container)

  (let [[{:keys [_reagent-render component-will-mount component-did-mount]}
         & _params] normalized-component
        [hiccup dom] (add-modify-dom-watcher-on-ratom-deref normalized-component)
        hiccup-rendered (fully-render-hiccup hiccup)]
    (component-will-mount normalized-component)
    (.appendChild container dom)
    (component-did-mount normalized-component)
    (swap! mounted-components assoc normalized-component {:hiccup hiccup-rendered
                                                          :dom dom
                                                          :container container})
    (swap! container->mounted-component assoc container normalized-component)))


(defn render [component container]
  (let [normalized-component (normalize-component component)]
    (do-render normalized-component container)))

(def render-component render)

(defn init []
  (prn "mr-clean init"))

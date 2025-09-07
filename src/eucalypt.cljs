(ns eucalypt
  (:require [clojure.string :as str]
            ["es-toolkit" :refer [isEqual]]))

(defn- log [& args]
  (try
    (when (= (.getItem js/localStorage "debug") "eucalypt:*")
      (.apply (.-log js/console) js/console args))
    (catch :default _
      ;; ignore if localStorage is not available
      )))

(log "eucalypt.cljs loading...")

(defn- with-meta* [obj m]
  (aset obj "---meta" m)
  obj)

(defn- meta* [obj]
  (aget obj "---meta"))

#_ (defn- walk [inner outer form]
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

#_ (defn- postwalk [f form]
  (walk (partial postwalk f) f form))

(defonce ^:dynamic *watcher* nil)
(defonce ^:dynamic *xml-ns* "http://www.w3.org/1999/xhtml")
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
      (= :xmlns k)
      nil ;; The namespace is set by createElementNS

      (= :ref k)
      (do
        (aset element "---ref-fn" v)
        (v element))

      (str/starts-with? k "on-")
      (aset element (str/replace k #"-" "") v)

      (= :style k)
      (aset element "style" (style-map->css-str v))

      :else
      (.setAttributeNS element nil k v))))

(defn- create-element [hiccup]
  (let [[tag & content] hiccup
        attrs (if (map? (first content)) (first content) {})
        value (:value attrs)
        attrs-without-value (dissoc attrs :value)
        content (if (map? (first content)) (rest content) content)
        old-ns *xml-ns*
        new-ns (if (= :svg tag) "http://www.w3.org/2000/svg" old-ns)
        element (.createElementNS js/document new-ns tag)]
    (try
      (set! *xml-ns* new-ns)
      (set-attributes! element attrs-without-value)
      (doseq [child content]
        (when-let [child-node (hiccup->dom child)]
          (.appendChild element child-node)))
      (when (some? value)
        (aset element "value" value))
      element
      (finally
        (set! *xml-ns* old-ns)))))

(defn hiccup->dom [hiccup]
  (log "hiccup->dom called with:" hiccup)
  (let [result (cond
                 (or (string? hiccup) (number? hiccup)) (.createTextNode js/document (str hiccup))
                 (vector? hiccup) (let [[tag & content] hiccup]
                                    (cond
                                      (fn? tag) (hiccup->dom (apply tag content))
                                      (vector? tag) (let [fragment (.createDocumentFragment js/document)]
                                                      (doseq [item hiccup]
                                                        (when-let [child-node (hiccup->dom item)]
                                                          (.appendChild fragment child-node)))
                                                      fragment)
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
    (log "hiccup->dom returning:" result)
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
  (let [result (isEqual hiccup-a hiccup-b)]
    (log "hiccup-eq?:" result)
    (when (not result)
      (log "hiccup-eq? details: a:" hiccup-a "b:" hiccup-b))
    result))

(declare fully-render-hiccup)
(declare patch)

(defn- get-hiccup-children [hiccup]
  (let [content (rest hiccup)]
    (if (map? (first content))
      (rest content)
      content)))

(defn- fully-render-hiccup [hiccup]
  (log "fully-render-hiccup called with:" hiccup)
  (let [result (cond
                 (fn? hiccup)
                 (fully-render-hiccup (hiccup))

                 (vector? hiccup)
                 (let [tag (first hiccup)]
                   (if (fn? tag)
                     (let [call-site-attrs (when (map? (second hiccup)) (second hiccup))
                           params (if call-site-attrs (drop 2 hiccup) (rest hiccup))
                           res (apply tag params)
                           rendered-hiccup (fully-render-hiccup res)]
                       (if (and call-site-attrs (vector? rendered-hiccup))
                         (let [res-tag (first rendered-hiccup)
                               res-attrs (when (map? (second rendered-hiccup)) (second rendered-hiccup))
                               res-children (if res-attrs (drop 2 rendered-hiccup) (rest rendered-hiccup))]
                           (if res-attrs
                             (into [res-tag (merge res-attrs call-site-attrs)] res-children)
                             (into [res-tag call-site-attrs] res-children)))
                         rendered-hiccup))

                     (let [attrs (when (map? (second hiccup)) (second hiccup))
                           children (if attrs (drop 2 hiccup) (rest hiccup))
                           head (if attrs [(first hiccup) attrs] [(first hiccup)])]
                       (into head
                             (mapcat
                              (fn [child]
                                (let [processed (fully-render-hiccup child)]
                                  (if (and (seq? processed) (not (vector? processed)) (not (string? processed)))
                                    processed
                                    [processed])))
                              children)))))
                 (map? hiccup)
                 ;; This is a map component directly in the hiccup tree
                 (fully-render-hiccup ((:reagent-render hiccup)))
                 :else
                 hiccup)]
    (log "fully-render-hiccup returning:" result)
    result))

(defn- unmount-node-and-children [node]
  (when node
    (when-let [ref-fn (aget node "---ref-fn")]
      (log "unmount-node-and-children: calling ref-fn for node" node)
      (ref-fn nil)
      (aset node "---ref-fn" nil))
    (doseq [child (vec (aget node "childNodes"))]
      (unmount-node-and-children child))))

(defn- remove-node-and-unmount! [node]
  (when node
    (unmount-node-and-children node)
    (.remove node)))

(defn- patch-children [hiccup-a-rendered hiccup-b-rendered dom-a]
  (log "--- patch-children start ---")
  (log "patch-children: hiccup-a-rendered" hiccup-a-rendered)
  (log "patch-children: hiccup-b-rendered" hiccup-b-rendered)
  (let [children-a (vec (remove nil? (get-hiccup-children hiccup-a-rendered)))
        children-b (vec (remove nil? (get-hiccup-children hiccup-b-rendered)))
        dom-nodes (atom (vec (aget dom-a "childNodes")))
        len-a (count children-a)
        len-b (count children-b)]
    (log "patch-children: children-a" children-a)
    (log "patch-children: children-b" children-b)
    (log "patch-children: dom-nodes" @dom-nodes)
    ;; Patch or replace existing nodes
    (loop [i 0]
      (when (< i (min len-a len-b))
        (let [child-a (nth children-a i)
              child-b (nth children-b i)
              dom-node (nth @dom-nodes i)]
          (log "patch-children: about to patch child" i "child-a:" child-a "child-b:" child-b "dom-node:" dom-node)
          (let [new-dom-node (patch child-a child-b dom-node)]
            (log "patch-children: comparing child" i child-a child-b)
            (when (not= dom-node new-dom-node)
              (swap! dom-nodes assoc i new-dom-node))))
        (recur (inc i))))
    ;; Add new nodes
    (when (> len-b len-a)
      (log "patch-children: adding new nodes")
      (doseq [i (range len-a len-b)]
        (.appendChild dom-a (hiccup->dom (nth children-b i)))))
    ;; Remove surplus nodes
    (when (> len-a len-b)
      (log "patch-children: removing surplus nodes")
      (doseq [i (range (dec len-a) (dec len-b) -1)]
        (remove-node-and-unmount! (nth @dom-nodes i))))))

(defn- get-attrs [hiccup]
  (log "get-attrs called on hiccup:" hiccup)
  (let [s (second hiccup)]
    (if (map? s) s {})))

(defn- patch-attributes [hiccup-a-rendered hiccup-b-rendered dom-a]
  (log "patch-attributes called with hiccup-a:" hiccup-a-rendered "hiccup-b:" hiccup-b-rendered)
  (let [a-attrs (get-attrs hiccup-a-rendered)
        b-attrs (get-attrs hiccup-b-rendered)
        a-ref (get a-attrs :ref)
        b-ref (get b-attrs :ref)]
    (log "patch-attributes: a-attrs:" a-attrs "b-attrs:" b-attrs)
    (log "patch-attributes: a-ref:" a-ref "b-ref:" b-ref)

    ;; Handle :ref lifecycle
    (log "patch-attributes: about to compare refs. are they equal?" (= a-ref b-ref) "a-ref:" a-ref "b-ref:" b-ref)
    (when (not (= a-ref b-ref))
      (log "patch-attributes: refs are different, handling lifecycle")
      (when a-ref (a-ref nil))
      (when b-ref (b-ref dom-a))
      (aset dom-a "---ref-fn" b-ref))

    ;; Remove attributes from a that are not in b
    (doseq [[k _] a-attrs]
      (when (and (not (contains? b-attrs k)) (not= k :ref) (not= k :xmlns))
        (if (str/starts-with? k "on-")
          (aset dom-a (str/replace k #"-" "") nil)
          (.removeAttribute dom-a k))))
    ;; Add/update attributes from b
    (doseq [[k v] b-attrs]
      (when (and (not= k :ref) (not= k :xmlns))
        (let [old-v (get a-attrs k)]
          (if (str/starts-with? k "on-")
            (aset dom-a (str/replace k #"-" "") v)
            (when (not= v old-v)
              (if (= :value k)
                (aset dom-a "value" v)
                (let [val-str (if (= k :style)
                                  (style-map->css-str v)
                                  v)]
                  (.setAttributeNS dom-a nil k val-str))))))))))

(defn- component->hiccup [normalized-component]
  (let [[config & params] normalized-component]
    (apply (:reagent-render config) params)))

(defn patch
  "transform dom-a to dom representation of hiccup-b.
  if hiccup-a and hiccup-b are not the same element type, then a new dom element is created from hiccup-b."
  [hiccup-a-rendered hiccup-b-rendered dom-a]
  (log "patch: hiccup-a-rendered" hiccup-a-rendered "hiccup-b-rendered" hiccup-b-rendered "dom-a" dom-a)
  (let [are-equal (hiccup-eq? hiccup-a-rendered hiccup-b-rendered)]
    (log "patch: are-equal is" are-equal "(type" (js/typeof are-equal) ")")
    (cond
      are-equal
      (do (log "patch: hiccup is equal, doing nothing")
          dom-a)

      (or (not (vector? hiccup-a-rendered)) (not (vector? hiccup-b-rendered)) (not= (first hiccup-a-rendered) (first hiccup-b-rendered)))
      (let [new-node (hiccup->dom hiccup-b-rendered)]
        (log "patch: replacing node" dom-a "with" new-node)
        (unmount-node-and-children dom-a)
        (.replaceWith dom-a new-node)
        new-node)

      :else
      (do (log "patch: hiccup not equal, patching children and attributes")
          (patch-attributes hiccup-a-rendered hiccup-b-rendered dom-a)
          (patch-children hiccup-a-rendered hiccup-b-rendered dom-a)
          (let [a-attrs (get-attrs hiccup-a-rendered)
                b-attrs (get-attrs hiccup-b-rendered)
                b-value (:value b-attrs)]
            (when (and (contains? b-attrs :value) (not= (:value a-attrs) b-value))
              (if (and (= (.-tagName dom-a) "SELECT") (.-multiple dom-a))
                (let [value-set (set b-value)]
                  (doseq [opt (.-options dom-a)]
                    (aset opt "selected" (contains? value-set (.-value opt)))))
                (aset dom-a "value" b-value))))
          dom-a))))

(defn modify-dom [normalized-component]
  (log "modify-dom called for component:" normalized-component)
  (let [[{:keys [_reagent-render]} & _params] normalized-component
        mounted-info (get @mounted-components normalized-component)
        _ (log "modify-dom: mounted-info from cache:" mounted-info)
        {:keys [hiccup dom container]} mounted-info
        new-hiccup-unrendered (component->hiccup normalized-component)
        new-hiccup-rendered (fully-render-hiccup new-hiccup-unrendered)]
    (if (and (vector? hiccup) (= :<> (first hiccup)))
      (do
        (patch-children hiccup new-hiccup-rendered container)
        (swap! mounted-components assoc normalized-component {:hiccup new-hiccup-rendered
                                                              :dom dom
                                                              :container container}))
      (let [new-dom (patch hiccup new-hiccup-rendered dom)]
        (log "modify-dom: new DOM" new-dom)
        (swap! mounted-components assoc normalized-component {:hiccup new-hiccup-rendered
                                                              :dom new-dom
                                                              :container container})
        (when (not= dom new-dom)
          (log "modify-dom: DOM changed, replacing in container")
          (aset container "innerHTML" "")
          (.appendChild container new-dom))))))

(defn notify-watchers [watchers]
  (log "notify-watchers called with" (count @watchers) "watchers")
  (doseq [watcher @watchers]
    (log "calling watcher")
    (let [old-watcher *watcher*]
      (try
        (set! *watcher* watcher)
        (watcher)
        (finally
          (set! *watcher* old-watcher))))))

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
                        (log "ratom deref: watcher found, adding to set.")
                        (swap! (aget a "watchers") conj *watcher*))
                      (.call orig-deref a)))
    (aset a "_reset_BANG_" (fn [new-val]
                             (log "ratom _reset_BANG_ called with" new-val)
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
        watcher #(reset! ra (apply f params))
        old-watcher *watcher*]
    (try
      (set! *watcher* watcher)
      (watcher)
      (let [reaction-obj (js-obj
                           "_deref" (fn [] @ra)
                           "_swap" (fn [& _] (throw (js/Error. "Reactions are readonly"))))]
        (aset reaction-obj "watchers" (aget ra "watchers"))
        reaction-obj)
      (finally
        (set! *watcher* old-watcher)))))

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
                             :component-will-update identity
                             :component-did-update identity
                             :component-will-unmount rm-watchers})

(defn normalize-component [component]
  (log "normalize-component called with:" component)
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
  (doseq [child (vec (aget container "childNodes"))]
    (remove-node-and-unmount! child)))

(defn do-render [normalized-component container]
  (unmount-components container)

  (let [[{:keys [_reagent-render]}
         & _params] normalized-component
        [hiccup dom] (add-modify-dom-watcher-on-ratom-deref normalized-component)
        hiccup-rendered (fully-render-hiccup hiccup)]
    (.appendChild container dom)
    (swap! mounted-components assoc normalized-component {:hiccup hiccup-rendered
                                                          :dom dom
                                                          :container container})
    (swap! container->mounted-component assoc container normalized-component)))


(defn render [component container]
  (log "render called with component:" component "and container:" container)
  (let [normalized-component (normalize-component component)]
    (log "render: normalized-component is" normalized-component)
    (do-render normalized-component container)))

(def render-component render)

(defn init []
  (prn "eucalypt init"))

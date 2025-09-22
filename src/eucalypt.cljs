(ns eucalypt
  ;(:refer-clojure :exclude [atom])
  (:require
    ; [clojure.core :refer [atom] :rename {atom core-atom}]
    ["es-toolkit" :refer [isEqual]]))

(def ^:private core-atom atom)

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

(declare modify-dom)

(defonce ^:dynamic *watcher* nil)
(defonce ^:dynamic *xml-ns* "http://www.w3.org/1999/xhtml")
(defonce life-cycle-methods {:get-initial-state (fn [_this])
                             :component-will-receive-props identity
                             :should-component-update identity
                             :component-will-update identity
                             :component-did-update identity
                             :component-will-unmount rm-watchers})

(defonce mounted-components (core-atom {}))
(defonce container->mounted-component (core-atom {}))
(defonce component-instances (core-atom {}))
(defonce positional-key-counter (core-atom 0))
(defonce all-ratoms (core-atom {}))

(defn- with-watcher-bound [normalized-component f]
  (let [old-watcher *watcher*]
    (try
      (set! *watcher* (with-meta* #(modify-dom normalized-component)
                        {:normalized-component normalized-component}))
      (f)
      (finally
        (set! *watcher* old-watcher)))))

(defn- remove-watchers-for-component [component]
  (doseq [[_ratom-id ratom] @all-ratoms]
    (when-let [watchers (aget ratom "watchers")]
      (swap! watchers
             (fn [watcher-set]
               (set (remove #(= (-> % meta* :normalized-component)
                                component)
                            watcher-set)))))))

;; *** hiccup-to-dom implementation ***

(declare hiccup->dom)

(defn- style-map->css-str [style-map]
  (apply str (map (fn [[k v]] (str k ":" v ";")) style-map)))

(def ^:private event-name-map
  {:on-double-click "ondblclick"})

(defn- get-event-name [k tag-name]
  (if (and (= k :on-change)
           (#{"INPUT" "TEXTAREA"} tag-name))
    "oninput"
    (get event-name-map k (.replaceAll k "-" ""))))

(defn- set-attributes! [element attrs]
  (doseq [[k v] attrs]
    (cond
      (= :xmlns k)
      nil ;; The namespace is set by createElementNS

      (= :ref k)
      (when (some? v)
        (aset element "---ref-fn" v)
        (v element))

      (.startsWith k "on-")
      (when (some? v)
        (let [event-name (get-event-name k (.-tagName element))]
          (aset element event-name v)))

      (= :style k)
      (when (some? v)
        (aset element "style" (style-map->css-str v)))

      (= :class k)
      (if (or (nil? v) (= "" v))
        (.removeAttribute element "class")
        (.setAttribute element "class" v))

      (or (= :checked k) (= :selected k))
      (when (some? v)
        (aset element k v))

      :else
      (when (some? v)
        (.setAttributeNS element nil k v)))))

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
        (if (and (= (.-tagName element) "SELECT") (.-multiple element))
          (let [value-set (set value)]
            (doseq [opt (.-options element)]
              (aset opt "selected" (contains? value-set (.-value opt)))))
          (aset element "value" value)))
      element
      (finally
        (set! *xml-ns* old-ns)))))

(defn- get-or-create-fn-id [f]
  (if-let [id (aget f "_eucalypt_id")]
    id
    (let [new-id (str "fn_" (random-uuid))]
      (aset f "_eucalypt_id" new-id)
      new-id)))

#_ (defn- get-or-create-component-instance-id [component-vec]
  (if-let [id (aget component-vec "_eucalypt_instance_id")]
    id
    (let [new-id (str "instance_" (random-uuid))]
      (aset component-vec "_eucalypt_instance_id" new-id)
      new-id)))

(defn normalize-component [component]
  (log "normalize-component called with:" component)
  (log "normalize-component: component metadata:" (meta component))
  (when (sequential? component)
    (let [first-element (first component)
          params (rest component)]
      (cond (fn? first-element)
            (let [a-fn first-element
                  params-vec (vec params)
                  component-meta (meta component)
                  fn-id (get-or-create-fn-id a-fn)
                  ;; Form-2 components are stateful and need a unique key per instance.
                  ;; Form-1 components are stateless and can be shared, using just the fn-id as a key.
                  instance-key (cond
                                 (contains? component-meta :key)
                                 (str fn-id "_key_" (:key component-meta))
                                 :else
                                 (str fn-id "_pos_" (swap! positional-key-counter inc)))
                  shared-key fn-id
                  cached-instance (get @component-instances instance-key)
                  cached-shared (get @component-instances shared-key)]
              (cond
                cached-instance
                (do (log "normalize-component: using cached instance for key:" instance-key)
                    (into [(:instance cached-instance)] params-vec))

                cached-shared
                (do (log "normalize-component: using cached shared for key:" shared-key)
                    (into [(:instance cached-shared)] params-vec))

                :else
                (let [_ (log "normalize-component: cache miss for keys:" instance-key "and" shared-key)
                      func-or-hiccup (apply a-fn params-vec)]
                  (if (fn? func-or-hiccup)
                    ;; Form-2 component (stateful)
                    (let [closure func-or-hiccup
                          instance (merge life-cycle-methods {:reagent-render closure})
                          result (into [instance] params-vec)]
                      (log "normalize-component: Form-2, caching with key:" instance-key)
                      (swap! component-instances assoc instance-key {:type :form-2 :instance instance})
                      result)
                    ;; Form-1 component (stateless)
                    (let [instance (merge life-cycle-methods {:reagent-render a-fn})
                          result (into [instance] params-vec)]
                      (log "normalize-component: Form-1, caching with key:" shared-key)
                      (swap! component-instances assoc shared-key {:type :form-1 :instance instance})
                      result)))))

            (string? first-element)
            (into [(assoc life-cycle-methods :reagent-render (fn [] component))]
                  params)
            (map? first-element)
            (let [component-as-map first-element
                  render-fn (:reagent-render component-as-map)
                  comp-with-lifecycle (into {:reagent-render render-fn}
                                            (map (fn [[k func]]
                                                   (let [func2 (get component-as-map k)
                                                         func-func2 (if func2 (comp func2 func) func)]
                                                     [k func-func2]))
                                                 life-cycle-methods))]
              (into [comp-with-lifecycle] params))))))

(defn- component->hiccup [normalized-component]
  (let [[config & params] normalized-component
        reagent-render (:reagent-render config)]
    (log "component->hiccup: calling reagent-render with params:" params)
    (let [result (apply reagent-render params)]
      (log "component->hiccup: reagent-render returned:" result)
      result)))

(defn hiccup->dom [hiccup]
  (log "hiccup->dom called with:" hiccup)
  (let [result
        (cond
          (or (string? hiccup) (number? hiccup))
          (.createTextNode js/document (str hiccup))

          (vector? hiccup)
          (let [[tag & _content] hiccup]
            (cond
              (fn? tag) (hiccup->dom (component->hiccup (normalize-component hiccup)))
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

          (seq? hiccup)
          (let [fragment (.createDocumentFragment js/document)]
            (doseq [item hiccup]
              ;; Preserve metadata when processing sequences
              (let [item-with-meta (if (and (vector? item) (meta item))
                                     (with-meta item (meta item))
                                     item)]
                (when-let [child-node (hiccup->dom item-with-meta)]
                  (.appendChild fragment child-node))))
            fragment)

          (fn? hiccup)
          (hiccup->dom (hiccup))

          (map? hiccup) (hiccup->dom ((:reagent-render hiccup)))
          (nil? hiccup) nil

          :else
          (.createTextNode js/document (str hiccup)))]
    (log "hiccup->dom returning:" result)
    result))

(defn hiccup-eq? [hiccup-a hiccup-b]
  (let [result (isEqual hiccup-a hiccup-b)]
    (log "hiccup-eq?:" result)
    (when (not result)
      ;(js/console.log "hiccup-eq? returned false. a:" hiccup-a "b:" hiccup-b) 
      (log "hiccup-eq? details: a:" hiccup-a "b:" hiccup-b))
    result))

(defn- is-sequence-of-hiccup-elements? [x]
  (log "is-sequence-of-hiccup-elements? checking:" x)
  (let [result (and (sequential? x)
                    (not (string? x))
                    (not (empty? x))
                    (every? (fn [item]
                              (or (nil? item)
                                  (and (vector? item)
                                       (or (string? (first item))
                                           (fn? (first item))))))
                            x))]
    (log "is-sequence-of-hiccup-elements? result:" result)
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
  (let [result
        (cond
          (nil? hiccup)
          nil

          (fn? hiccup)
          (fully-render-hiccup (hiccup))

          (is-sequence-of-hiccup-elements? hiccup)
          (mapv fully-render-hiccup hiccup)

          (and (some? (aget hiccup js/Symbol.iterator))
               (not (vector? hiccup))
               (not (string? hiccup)))
          (mapv fully-render-hiccup hiccup)

          (vector? hiccup)
          (let [tag (first hiccup)]
            (if (fn? tag)
              (fully-render-hiccup (component->hiccup (normalize-component hiccup)))

              (let [attrs (when (map? (second hiccup)) (second hiccup))
                    children (if attrs (drop 2 hiccup) (rest hiccup))
                    head (if attrs [(first hiccup) attrs] [(first hiccup)])]
                (into head
                      (reduce (fn [acc child]
                                (let [processed (fully-render-hiccup child)]
                                  (log "fully-render-hiccup reduce: processing child, processed=" processed)
                                  (let [is-seq (is-sequence-of-hiccup-elements? processed)]
                                    (log "fully-render-hiccup reduce: is-sequence-of-hiccup-elements?=" is-seq)
                                    (cond
                                      ;; Unpack fragments
                                      (and (vector? processed) (= :<> (first processed)))
                                      (do
                                        (log "fully-render-hiccup reduce: unpacking fragment")
                                        (into acc (get-hiccup-children processed)))

                                      ;; Unpack sequences of hiccup elements (but not single hiccup vectors)
                                      is-seq
                                      (do
                                        (log "fully-render-hiccup reduce: unpacking sequence of hiccup elements")
                                        (into acc processed))

                                      ;; Append a single child (including single hiccup vectors)
                                      :else
                                      (do
                                        (log "fully-render-hiccup reduce: appending single child")
                                        (conj acc processed))))))
                              [] children)))))

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
  (log "--- patch-children start for dom:" (.toString dom-a))
  (let [children-a (vec (remove nil? (get-hiccup-children hiccup-a-rendered)))
        children-b (vec (remove nil? (get-hiccup-children hiccup-b-rendered)))
        dom-nodes (core-atom (vec (aget dom-a "childNodes")))
        len-a (count children-a)
        len-b (count children-b)
        len-dom (count @dom-nodes)]
    (log "patch-children: len-a:" len-a "len-b:" len-b "len-dom:" len-dom)
    (log "patch-children: children-a:" children-a)
    (log "patch-children: children-b:" children-b)
    ;; Patch or replace existing nodes
    (loop [i 0]
      (when (< i (min len-a len-b))
        (let [child-a (nth children-a i)
              child-b (nth children-b i)
              dom-node (nth @dom-nodes i)]
          (let [new-dom-node (patch child-a child-b dom-node)]
            (when (not= dom-node new-dom-node)
              (swap! dom-nodes assoc i new-dom-node))))
        (recur (inc i))))
    ;; Add new nodes
    (when (> len-b len-a)
      (doseq [i (range len-a len-b)]
        (.appendChild dom-a (hiccup->dom (nth children-b i)))))
    ;; Remove surplus nodes
    (when (> len-a len-b)
      (dotimes [_ (- len-a len-b)]
        (remove-node-and-unmount! (.-lastChild dom-a))))))

(defn- get-attrs [hiccup]
  (log "get-attrs called on hiccup:" hiccup)
  (let [s (second hiccup)]
    (if (map? s) s {})))

(defn- patch-attributes [hiccup-a-rendered hiccup-b-rendered dom-a]
  (log "patch-attributes called with hiccup-a:" hiccup-a-rendered "hiccup-b:" hiccup-b-rendered)
  (let [a-attrs (get-attrs hiccup-a-rendered)
        b-attrs (get-attrs hiccup-b-rendered)
        a-ref (get a-attrs :ref)
        b-ref (get b-attrs :ref)
        tag-name (.-tagName dom-a)]
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
        (if (.startsWith k "on-")
          (aset dom-a (get-event-name k tag-name) nil)
          (.removeAttribute dom-a k))))
    ;; Add/update attributes from b
    (doseq [[k v] b-attrs]
      (when (and (not= k :ref) (not= k :xmlns))
        (let [old-v (get a-attrs k)]
          (if (.startsWith k "on-")
            (aset dom-a (get-event-name k tag-name) v)
            (when (not= v old-v)
              (log "patch-attributes: updating attribute" k "from" old-v "to" v "on" dom-a)
              (cond
                (= :value k) nil ;; handled in patch
                (= :class k)
                (if (or (nil? v) (= "" v))
                  (.removeAttribute dom-a "class")
                  (.setAttribute dom-a "class" v))
                (or (= :checked k) (= :selected k))
                (aset dom-a k v)

                :else
                (if (nil? v)
                  (.removeAttribute dom-a k)
                  (let [val-str (if (= k :style)
                                    (style-map->css-str v)
                                    v)]
                    (.setAttributeNS dom-a nil k val-str)))))))))))

(defn- realize-deep [x]
  (cond
    ;; Squint's LazyIterable has a `.gen` property. Realize it to a vector.
    (and (seq? x) (aget x "gen"))
    (mapv realize-deep x)

    ;; For other sequential types (vectors, lists), recurse.
    (and (sequential? x) (not (string? x)))
    (into (empty x) (map realize-deep x))

    :else x))

(defn patch
  "transform dom-a to dom representation of hiccup-b.
  if hiccup-a and hiccup-b are not the same element type, then a new dom element is created from hiccup-b."
  [hiccup-a-rendered hiccup-b-rendered dom-a]
  (log "patch: hiccup-a-rendered" hiccup-a-rendered "hiccup-b-rendered" hiccup-b-rendered "dom-a" (.toString dom-a))
  (let [hiccup-a-realized (realize-deep hiccup-a-rendered)
        hiccup-b-realized (realize-deep hiccup-b-rendered)
        are-equal (hiccup-eq? hiccup-a-realized hiccup-b-realized)]
    (log "patch: are-equal is" are-equal "(type" (js/typeof are-equal) ")")
    (cond
      are-equal
      (do (log "patch: hiccup is equal, doing nothing")
          dom-a)

      (or (not (vector? hiccup-a-realized))
          (not (vector? hiccup-b-realized))
          (not= (first hiccup-a-realized)
                (first hiccup-b-realized)))
      (let [new-node (hiccup->dom hiccup-b-realized)]
        (log "patch: replacing node" dom-a "with" new-node)
        (unmount-node-and-children dom-a)
        (.replaceWith dom-a new-node)
        new-node)

      :else
      (do (log "patch: hiccup not equal, patching children and attributes")
          (patch-attributes hiccup-a-realized hiccup-b-realized dom-a)
          (patch-children hiccup-a-realized hiccup-b-realized dom-a)
          (let [a-attrs (get-attrs hiccup-a-realized)
                b-attrs (get-attrs hiccup-b-realized)
                b-value (:value b-attrs)]
            (when (and (contains? b-attrs :value) (not= (:value a-attrs) b-value))
              (log "patch: value changed from" (:value a-attrs) "to" b-value "on" dom-a)
              (if (and (= (.-tagName dom-a) "SELECT") (.-multiple dom-a))
                (let [value-set (set b-value)]
                  (doseq [opt (.-options dom-a)]
                    (aset opt "selected" (contains? value-set (.-value opt)))))
                (aset dom-a "value" b-value))))
          dom-a))))

(defn modify-dom [normalized-component]
  (log "modify-dom called for component:" normalized-component)
  (remove-watchers-for-component normalized-component)
  (reset! positional-key-counter 0)
  (let [mounted-info (get @mounted-components normalized-component)
        _ (log "modify-dom: mounted-info from cache:" mounted-info)
        {:keys [hiccup dom container]} mounted-info
        new-hiccup-unrendered (with-watcher-bound
                                normalized-component
                                (fn [] (component->hiccup normalized-component)))
        new-hiccup-rendered (fully-render-hiccup new-hiccup-unrendered)]
    (if (and (vector? hiccup) (= :<> (first hiccup)))
      (do
        (reset! positional-key-counter 0)
        (patch-children hiccup new-hiccup-rendered container)
        (swap! mounted-components assoc normalized-component
               {:hiccup new-hiccup-rendered
                :dom dom
                :container container}))
      (let [_ (reset! positional-key-counter 0)
            new-dom (patch hiccup new-hiccup-rendered dom)]
        (log "modify-dom: new DOM" new-dom)
        (swap! mounted-components assoc normalized-component
               {:hiccup new-hiccup-rendered
                :dom new-dom
                :container container})
        (when (not= dom new-dom)
          (log "modify-dom: DOM changed, replacing in container")
          (aset container "innerHTML" "")
          (.appendChild container new-dom))))))

(defn notify-watchers [watchers]
  (log "notify-watchers called with" (count @watchers) "watchers")
  ;(js/console.log "notify-watchers called with" (count @watchers) "watchers")
  (doseq [watcher @watchers]
    (log "calling watcher")
    ;(js/console.log "calling watcher")
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
  (with-watcher-bound
    normalized-component
    (fn []
      (let [reagent-render (-> normalized-component first :reagent-render)
            params (rest normalized-component)
            hiccup (apply reagent-render params)
            dom (hiccup->dom hiccup)]
        [hiccup dom]))))

(defn ratom [initial-value]
  (let [a (core-atom initial-value)
        orig-deref (aget a "_deref")
        orig-reset_BANG_ (aget a "_reset_BANG_")
        ratom-id (str "ratom-" (random-uuid))]
    (aset a "ratom-id" ratom-id)
    (aset a "watchers" (core-atom #{}))
    (aset a "cursors" (core-atom #{}))
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
    (swap! all-ratoms assoc ratom-id a)
    a))

(defn cursor [the-ratom path]
  (let [cursors (aget the-ratom "cursors")
        found-cursor (some (fn [c] (when (= path (aget c "path")) c)) @cursors)]
    (if (nil? found-cursor)
      (let [watchers (core-atom #{})
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

(defn unmount-components [container]
  ;(js/console.log "unmount-components called for container:" container)
  (when-let [mounted-component (get @container->mounted-component container)]
    (remove-watchers-for-component mounted-component)
    (let [[{:keys [component-will-unmount]} & _params] mounted-component]
      (component-will-unmount mounted-component)
      (swap! container->mounted-component dissoc container)))
  ;; Always clear the component cache when unmounting
  ;; This matches Reagent's behavior where component instances
  ;; are destroyed when unmounted
  (reset! component-instances {})
  (doseq [child (vec (aget container "childNodes"))]
    (remove-node-and-unmount! child)))

(defn do-render [normalized-component container]
  (unmount-components container)
  (reset! positional-key-counter 0)

  (let [[{:keys [_reagent-render]}
         & _params] normalized-component
        [hiccup dom] (add-modify-dom-watcher-on-ratom-deref normalized-component)
        _ (reset! positional-key-counter 0)
        hiccup-rendered (fully-render-hiccup hiccup)]
    (.appendChild container dom)
    (swap! mounted-components assoc normalized-component
           {:hiccup hiccup-rendered
            :dom dom
            :container container})
    (swap! container->mounted-component assoc container normalized-component)))

(defn render [component container]
  (log "render called with component:" component "and container:" container)
  (let [normalized-component (normalize-component component)]
    (log "render: normalized-component is" normalized-component)
    (do-render normalized-component container)))

(def render-component render)

(defn clear-component-instances! []
  (reset! component-instances {}))

(defn init []
  (prn "eucalypt init"))

#_:clj-kondo/ignore
(def atom ratom)

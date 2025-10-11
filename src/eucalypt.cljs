(ns eucalypt
  ;(:refer-clojure :exclude [atom])
  (:require
    ["squint-cljs/core.js" :as squint]))

(def ^:private core-atom squint/atom)

(def default-namespace :html)

(def namespaces
  {:html {:uri "http://www.w3.org/1999/xhtml"}
   :svg {:uri "http://www.w3.org/2000/svg"
         :entry-tags #{"svg"}
         :boundary-tags #{"foreignObject"}}
   :math {:uri "http://www.w3.org/1998/Math/MathML"
          :entry-tags #{"math"}
          :boundary-tags #{"annotation-xml"}}})

(def entry-tag->namespace
  (reduce-kv
    (fn [acc ns {:keys [entry-tags]}]
      (reduce (fn [m tag] (assoc m tag ns))
              acc
              (or entry-tags #{})))
    {}
    namespaces))

(def uri->namespace
  (reduce-kv
    (fn [acc ns {:keys [uri]}]
      (if uri
        (assoc acc uri ns)
        acc))
    {}
    namespaces))

(defn- namespace-uri [ns-key]
  (or (get-in namespaces [ns-key :uri])
      (get-in namespaces [default-namespace :uri])))

(defn- normalize-namespace [uri]
  (let [candidate (or uri (namespace-uri default-namespace))]
    (if (contains? uri->namespace candidate)
      candidate
      (namespace-uri default-namespace))))

(defn- namespace-key [uri]
  (get uri->namespace uri default-namespace))

(defn- next-namespace [current tag-name]
  (let [current-uri (normalize-namespace current)
        current-key (namespace-key current-uri)
        boundary-tags (get-in namespaces [current-key :boundary-tags] #{})
        enter-target (get entry-tag->namespace tag-name)]
    (cond
      enter-target
      (namespace-uri enter-target)

      (contains? boundary-tags tag-name)
      (namespace-uri default-namespace)

      :else
      current-uri)))

(defn- dom->namespace [dom]
  (if (some? dom)
    (normalize-namespace (.-namespaceURI dom))
    (namespace-uri default-namespace)))

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

(declare hiccup->dom)
(declare modify-dom)

(defonce ^:dynamic *watcher* nil)

(defonce mounted-components (core-atom {}))
(defonce container->mounted-component (core-atom {}))
(defonce component-instances (core-atom {}))
(defonce all-ratoms (core-atom {}))

(defonce pending-watcher-queue (core-atom []))
(defonce watcher-flush-scheduled? (core-atom false))

(declare flush-queued-watchers)

(defn- create-render-state [{:keys [normalized-component container base-namespace]}]
  (let [state {:active true
               :positional-key-counter 0
               :base-namespace (normalize-namespace base-namespace)}
        state (cond-> state
                normalized-component (assoc :normalized-component normalized-component)
                container (assoc :container container))]
    (core-atom state)))

(defn- next-positional-key! [render-state]
  (let [next-val (inc (:positional-key-counter @render-state))]
    (swap! render-state assoc :positional-key-counter next-val)
    next-val))

(defn- schedule-watcher-flush! []
  (when-not @watcher-flush-scheduled?
    (reset! watcher-flush-scheduled? true)
    (let [runner (fn []
                   (flush-queued-watchers))]
      (if (some? (.-queueMicrotask js/globalThis))
        (.queueMicrotask js/globalThis runner)
        (js/setTimeout runner 0)))))

(defn- run-watcher-now [watcher]
  (let [old-watcher *watcher*]
    (try
      (set! *watcher* watcher)
      (watcher)
      (finally
        (set! *watcher* old-watcher)))))

(defn- flush-queued-watchers []
  (let [queued @pending-watcher-queue]
    (reset! pending-watcher-queue [])
    (reset! watcher-flush-scheduled? false)
    (doseq [watcher queued]
      (run-watcher-now watcher))))

(defn- queue-watcher!
  [watcher]
  (swap! pending-watcher-queue conj watcher)
  (schedule-watcher-flush!))

(defn- should-defer-watcher? [watcher]
  (let [meta-info (meta* watcher)
        defer-fn (and meta-info (:should-defer? meta-info))]
    (boolean (and (fn? defer-fn)
                  (defer-fn)))))

(defn- with-watcher-bound [normalized-component render-state f]
  (let [old-watcher *watcher*
        watcher-fn (with-meta* #(modify-dom normalized-component)
                     {:normalized-component normalized-component
                      :should-defer? #(boolean (:active @render-state))})]
    (try
      (set! *watcher* watcher-fn)
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

(defn- style-map->css-str [style-map]
  (apply str (map (fn [[k v]] (str k ":" v ";")) style-map)))

(defn- get-event-name [k tag-name]
  (cond
    (and (= k :on-change)
         (#{"INPUT" "TEXTAREA"} tag-name))
    "oninput"
    (= k :on-double-click)
    "ondblclick"
    :else
    (.replaceAll k "-" "")))

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
        (let [css (style-map->css-str v)]
          (if (seq css)
            (.setAttribute element "style" css)
            (.removeAttribute element "style"))))

      (= :class k)
      (let [class-val (if (and (sequential? v) (not (string? v)))
                        (.join (vec (remove nil? v)) " ")
                        v)]
        (if (or (nil? class-val) (= "" class-val))
          (.removeAttribute element "class")
          (.setAttribute element "class" class-val)))

      (or (= :checked k) (= :selected k))
      (when (some? v)
        (aset element k v))

      :else
      (when (some? v)
        (.setAttributeNS element nil k v)))))

(defn- parse-tag [tag]
  (let [tag-str (str tag)
        [before-hash after-hash] (.split tag-str "#" 2)
        [tag-name-str & classes-from-before-hash] (.split before-hash #"\.")
        tag-name (if (empty? tag-name-str) "div" tag-name-str)
        [id & classes-from-after-hash] (if after-hash (.split after-hash #"\.") [])
        all-classes (vec (remove empty? (concat classes-from-before-hash classes-from-after-hash)))]
    {:tag-name tag-name
     :id id
     :classes (when (seq all-classes) all-classes)}))

(defn- parse-hiccup [hiccup]
  (let [[tag-keyword & content] hiccup
        {:keys [tag-name id classes]} (parse-tag tag-keyword)
        attrs-from-hiccup (if (map? (first content)) (first content) {})
        final-id (or (:id attrs-from-hiccup) id)
        class-from-hiccup (:class attrs-from-hiccup)
        all-classes (let [tag-classes (or classes [])
                          attr-classes (cond
                                         (nil? class-from-hiccup) []
                                         (string? class-from-hiccup) [class-from-hiccup]
                                         (and (sequential? class-from-hiccup) (not (string? class-from-hiccup))) (vec class-from-hiccup)
                                         :else [class-from-hiccup])
                          combined (vec (concat tag-classes attr-classes))]
                      (when (seq combined) combined))
        attrs-with-id (if final-id (assoc attrs-from-hiccup :id final-id) attrs-from-hiccup)
        final-attrs (if (some? all-classes) (assoc attrs-with-id :class all-classes) (dissoc attrs-with-id :class))
        final-content (if (map? (first content)) (rest content) content)]
    {:tag-name tag-name
     :attrs final-attrs
     :content final-content}))

(defn- create-element [hiccup current-ns render-state]
  (let [{:keys [tag-name attrs content]} (parse-hiccup hiccup)
        value (:value attrs)
        attrs-without-value (dissoc attrs :value)
        new-ns (next-namespace (normalize-namespace current-ns) tag-name)
        element (.createElementNS js/document new-ns tag-name)]
    (set-attributes! element attrs-without-value)
    (doseq [child content]
      (when-let [child-node (hiccup->dom child new-ns render-state)]
        (.appendChild element child-node)))
    (when (some? value)
      (if (and (= (.-tagName element) "SELECT") (.-multiple element))
        (let [value-set (set value)]
          (doseq [opt (.-options element)]
            (aset opt "selected" (contains? value-set (.-value opt)))))
        (aset element "value" value)))
    element))

(defn- get-or-create-fn-id [f]
  (if-let [id (aget f "_eucalypt_id")]
    id
    (let [new-id (str "fn_" (random-uuid))]
      (aset f "_eucalypt_id" new-id)
      new-id)))

(defn normalize-component [component render-state]
  (when (sequential? component)
    (let [first-element (first component)
          params (rest component)]
      (cond
        (fn? first-element)
        (let [a-fn first-element
              params-vec (vec params)
              component-meta (meta component)
              fn-id (get-or-create-fn-id a-fn)
              instance-key (if (contains? component-meta :key)
                             (str fn-id "_key_" (:key component-meta))
                             (str fn-id "_pos_"
                                  (if render-state
                                    (next-positional-key! render-state)
                                    (random-uuid))))
              shared-key fn-id
              cached-instance (get @component-instances instance-key)
              cached-shared (get @component-instances shared-key)]
          (cond
            cached-instance
            (into [(:instance cached-instance)] params-vec)

            cached-shared
            (into [(:instance cached-shared)] params-vec)

            :else
            (let [func-or-hiccup (apply a-fn params-vec)]
              (if (fn? func-or-hiccup)
                ;; Form-2 component (stateful)
                (let [closure func-or-hiccup
                      instance {:reagent-render closure}
                      result (into [instance] params-vec)]
                  (swap! component-instances assoc instance-key {:type :form-2 :instance instance})
                  result)
                ;; Form-1 component (stateless)
                (let [instance {:reagent-render a-fn}
                      result (into [instance] params-vec)]
                  (swap! component-instances assoc shared-key {:type :form-1 :instance instance})
                  result)))))

        (string? first-element)
        (into [{:reagent-render (fn [] component)}]
              params)

        (map? first-element)
        (let [component-as-map first-element
              render-fn (:reagent-render component-as-map)
              comp-with-lifecycle {:reagent-render render-fn}]
          (into [comp-with-lifecycle] params))))))

(defn- component->hiccup [normalized-component]
  (let [[config & params] normalized-component
        reagent-render (:reagent-render config)]
    (apply reagent-render params)))

(defn hiccup->dom
  ([hiccup render-state]
   (hiccup->dom hiccup (namespace-uri default-namespace) render-state))
  ([hiccup current-ns render-state]
   (let [result
         (cond
           (or (string? hiccup) (number? hiccup))
           (.createTextNode js/document (str hiccup))

           (vector? hiccup)
           (let [[tag & _content] hiccup]
             (cond
               (fn? tag) (hiccup->dom (component->hiccup (normalize-component hiccup render-state)) current-ns render-state)
               (vector? tag) (let [fragment (.createDocumentFragment js/document)]
                               (doseq [item hiccup]
                                 (when-let [child-node (hiccup->dom item current-ns render-state)]
                                   (.appendChild fragment child-node)))
                               fragment)
               (= :<> tag) (let [fragment (.createDocumentFragment js/document)]
                             (doseq [child (rest hiccup)]
                               (when-let [child-node (hiccup->dom child current-ns render-state)]
                                 (.appendChild fragment child-node)))
                             fragment)
               :else (create-element hiccup current-ns render-state)))

           (seq? hiccup)
           (let [fragment (.createDocumentFragment js/document)]
             (doseq [item hiccup]
               ;; Preserve metadata when processing sequences
               (let [item-with-meta (if (and (vector? item) (meta item))
                                      (with-meta item (meta item))
                                      item)]
                 (when-let [child-node (hiccup->dom item-with-meta current-ns render-state)]
                   (.appendChild fragment child-node))))
             fragment)

           (fn? hiccup)
           (hiccup->dom (hiccup) current-ns render-state)

           (map? hiccup) (hiccup->dom ((:reagent-render hiccup)) current-ns render-state)
           (or (nil? hiccup) (boolean? hiccup)) nil

           :else
           (.createTextNode js/document (str hiccup)))]
     result)))

(declare fully-render-hiccup)
(declare patch)

(defn- get-hiccup-children [hiccup]
  (let [content (rest hiccup)]
    (if (map? (first content))
      (rest content)
      content)))

(defn- hiccup-seq? [x]
  (and (seq? x)
       (not (string? x))
       (not (vector? x))))

(defn- fully-render-hiccup [hiccup render-state]
  (let [result
        (cond
          (nil? hiccup) nil
          (hiccup-seq? hiccup)
          (mapv #(fully-render-hiccup % render-state) hiccup)

          (vector? hiccup)
          (let [tag (first hiccup)]
            (if (fn? tag)
              (fully-render-hiccup (component->hiccup (normalize-component hiccup render-state)) render-state)
              (let [attrs (let [?attrs (aget hiccup 1)]
                            (when (map? ?attrs)
                              ?attrs))
                    children (if attrs (subvec hiccup 2) (subvec hiccup 1))
                    head (if attrs [(aget hiccup 0) attrs] [(aget hiccup 0)])]
                (into head
                      (reduce (fn [acc child]
                                (let [processed (fully-render-hiccup child render-state)]
                                  (cond
                                    (nil? processed) acc

                                    ;; Unpack fragments
                                    (and (vector? processed) (= :<> (aget processed 0)))
                                    (into acc (subvec processed 1))

                                    ;; Unnest hiccup children
                                    (hiccup-seq? child)
                                    (into acc processed)

                                    ;; Single child
                                    :else (conj acc processed))))
                              [] children)))))

          (map? hiccup)
          ;; This is a map component directly in the hiccup tree
          (fully-render-hiccup ((:reagent-render hiccup)) render-state)
          :else
          hiccup)]
    result))


(defn- unmount-node-and-children [node]
  (when node
    (when-let [ref-fn (aget node "---ref-fn")]
      (ref-fn nil)
      (aset node "---ref-fn" nil))
    (doseq [child (vec (aget node "childNodes"))]
      (unmount-node-and-children child))))

(defn- remove-node-and-unmount! [node]
  (when node
    (unmount-node-and-children node)
    (.remove node)))

(defn- patch-children [hiccup-a-rendered hiccup-b-rendered dom-a render-state]
  (let [children-a (vec (remove nil? (get-hiccup-children hiccup-a-rendered)))
        children-b (vec (remove nil? (get-hiccup-children hiccup-b-rendered)))
        dom-nodes (core-atom (vec (aget dom-a "childNodes")))
        len-a (count children-a)
        len-b (count children-b)
        parent-ns (dom->namespace dom-a)]
    ;; Patch or replace existing nodes
    (loop [i 0]
      (when (< i (min len-a len-b))
        (let [child-a (nth children-a i)
              child-b (nth children-b i)
              dom-node (nth @dom-nodes i)
              new-dom-node (patch child-a child-b dom-node render-state)]
          (when (not= dom-node new-dom-node)
            (swap! dom-nodes assoc i new-dom-node)))
        (recur (inc i))))
    ;; Add new nodes
    (when (> len-b len-a)
      (doseq [i (range len-a len-b)]
        (when-let [new-child (hiccup->dom (nth children-b i) parent-ns render-state)]
          (.appendChild dom-a new-child))))
    ;; Remove surplus nodes
    (when (> len-a len-b)
      (dotimes [_ (- len-a len-b)]
        (remove-node-and-unmount! (.-lastChild dom-a))))))

(defn- get-attrs [hiccup]
  (let [s (second hiccup)]
    (if (map? s) s {})))

(defn- patch-attributes [hiccup-a-rendered hiccup-b-rendered dom-a]
  (let [a-attrs (get-attrs hiccup-a-rendered)
        b-attrs (get-attrs hiccup-b-rendered)
        a-ref (get a-attrs :ref)
        b-ref (get b-attrs :ref)
        tag-name (.-tagName dom-a)]
    ;; Handle :ref lifecycle
    (when (not (= a-ref b-ref))
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
              (cond
                (= :value k) nil ;; handled in patch
                (= :class k)
                (let [class-val (if (and (sequential? v) (not (string? v)))
                                  (.join (vec (remove nil? v)) " ")
                                  v)]
                  (if (or (nil? class-val) (= "" class-val))
                    (.removeAttribute dom-a "class")
                    (.setAttribute dom-a "class" class-val)))
                (= :style k)
                (let [css (style-map->css-str v)]
                  (if (seq css)
                    (.setAttribute dom-a "style" css)
                    (.removeAttribute dom-a "style")))
                (or (= :checked k) (= :selected k))
                (aset dom-a k v)

                :else
                (if (nil? v)
                  (.removeAttribute dom-a k)
                  (let [val-str (if (= :style k)
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
  [hiccup-a-rendered hiccup-b-rendered dom-a render-state]
  (let [hiccup-a-realized (realize-deep hiccup-a-rendered)
        hiccup-b-realized (realize-deep hiccup-b-rendered)]
    (cond
      (= hiccup-a-realized hiccup-b-realized)
      dom-a

      (or (not (vector? hiccup-a-realized))
          (not (vector? hiccup-b-realized))
          (not= (first hiccup-a-realized)
                (first hiccup-b-realized)))
      (let [parent (.-parentNode dom-a)
            parent-ns (dom->namespace parent)
            new-node (hiccup->dom hiccup-b-realized parent-ns render-state)]
        (unmount-node-and-children dom-a)
        (.replaceWith dom-a new-node)
        new-node)

      :else
      (do (patch-attributes hiccup-a-realized hiccup-b-realized dom-a)
          (patch-children hiccup-a-realized hiccup-b-rendered dom-a render-state)
          (let [a-attrs (get-attrs hiccup-a-realized)
                b-attrs (get-attrs hiccup-b-realized)
                b-value (:value b-attrs)]
            (when (and (contains? b-attrs :value) (not= (:value a-attrs) b-value))
              (if (and (= "SELECT" (.-tagName dom-a) ) (.-multiple dom-a))
                (let [value-set (set b-value)]
                  (doseq [opt (.-options dom-a)]
                    (aset opt "selected" (contains? value-set (.-value opt)))))
                (aset dom-a "value" b-value))))
          dom-a))))

(defn modify-dom [normalized-component]
  (remove-watchers-for-component normalized-component)
  (when-let [mounted-info (get @mounted-components normalized-component)]
    (let [{:keys [hiccup dom container base-namespace]} mounted-info
          render-state (create-render-state {:normalized-component normalized-component
                                             :container container
                                             :base-namespace (or base-namespace
                                                                 (dom->namespace container))})]
      (try
        (swap! render-state assoc :positional-key-counter 0)
        (let [new-hiccup-unrendered (with-watcher-bound
                                      normalized-component
                                      render-state
                                      (fn [] (component->hiccup normalized-component)))
              _ (swap! render-state assoc :positional-key-counter 0)
              new-hiccup-rendered (fully-render-hiccup new-hiccup-unrendered render-state)]
          (if (and (vector? hiccup) (= :<> (first hiccup)))
            (do
              (swap! render-state assoc :positional-key-counter 0)
              (patch-children hiccup new-hiccup-rendered container render-state)
              (let [base-ns (:base-namespace @render-state)]
                (swap! mounted-components assoc normalized-component
                       {:hiccup new-hiccup-rendered
                        :dom dom
                        :container container
                        :base-namespace base-ns})))
            (let [_ (swap! render-state assoc :positional-key-counter 0)
                  new-dom (patch hiccup new-hiccup-rendered dom render-state)
                  base-ns (:base-namespace @render-state)]
              (swap! mounted-components assoc normalized-component
                     {:hiccup new-hiccup-rendered
                      :dom new-dom
                      :container container
                      :base-namespace base-ns})
              (when (not= dom new-dom)
                (aset container "innerHTML" "")
                (.appendChild container new-dom)))))
        (finally
          (swap! render-state assoc :active false))))))

(defn notify-watchers [watchers]
  (doseq [watcher @watchers]
    (when watcher
      (if (should-defer-watcher? watcher)
        (queue-watcher! watcher)
        (run-watcher-now watcher)))))

(defn- add-modify-dom-watcher-on-ratom-deref
  "This is where the magic of adding watchers to ratoms happen automatically.
  This is achieved by setting the dnymaic var *watcher* then evaluating reagent-render
  which causes the deref of the ratom to trigger adding the watcher to (.-watchers ratom)"
  [normalized-component render-state]
  (with-watcher-bound
    normalized-component
    render-state
    (fn []
      (let [reagent-render (-> normalized-component first :reagent-render)
            params (rest normalized-component)
            hiccup (apply reagent-render params)
            base-ns (:base-namespace @render-state)
            dom (hiccup->dom hiccup base-ns render-state)]
        [hiccup dom]))))

(defn unmount-components [container]
  (when-let [mounted-component (get @container->mounted-component container)]
    (remove-watchers-for-component mounted-component)
    (rm-watchers mounted-component)
    (swap! container->mounted-component dissoc container))
  ;; Always clear the component cache when unmounting
  ;; This matches Reagent's behavior where component instances
  ;; are destroyed when unmounted
  (reset! component-instances {})
  (doseq [child (vec (aget container "childNodes"))]
    (remove-node-and-unmount! child)))

(defn do-render [normalized-component container render-state]
  (unmount-components container)
  (swap! render-state assoc :positional-key-counter 0)
  (try
    (let [base-ns (:base-namespace @render-state)
          [hiccup dom] (add-modify-dom-watcher-on-ratom-deref normalized-component render-state)
          _ (swap! render-state assoc :positional-key-counter 0)
          hiccup-rendered (fully-render-hiccup hiccup render-state)]
      (.appendChild container dom)
      (swap! mounted-components assoc normalized-component
             {:hiccup hiccup-rendered
              :dom dom
              :container container
              :base-namespace base-ns})
      (swap! container->mounted-component assoc container normalized-component))
    (finally
      (swap! render-state assoc :active false))))

; mirrored as atom below
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
                        (swap! (aget a "watchers") conj *watcher*))
                      (.call orig-deref a)))
    (aset a "_reset_BANG_" (fn [new-val]
                             (let [res (.call orig-reset_BANG_ a new-val)]
                               (notify-watchers (aget a "watchers"))
                               (doseq [c @(aget a "cursors")]
                                 (notify-watchers (aget c "watchers")))
                               res)))
    (swap! all-ratoms assoc ratom-id a)
    a))

; *** Reagent API functions *** ;

;; Reagent API
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

;; Reagent API
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

;; Reagent API
(defn render [component container]
  (let [base-ns (dom->namespace container)
        render-state (create-render-state {:container container
                                           :base-namespace base-ns})
        normalized (normalize-component component render-state)]
    (swap! render-state assoc :normalized-component normalized)
    (do-render normalized container render-state)))

;; Reagent API
(def render-component render)

(defn clear-component-instances! []
  (reset! component-instances {}))

;; Reagent API
#_:clj-kondo/ignore
(def atom ratom)

(ns eucalypt
  ;(:refer-clojure :exclude [atom])
  (:require
    ["squint-cljs/core.js" :as squint]))

(def ^:private core-atom squint/atom)

(defn- empty-js-map []
  (js/Map.))

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
  (->> namespaces
       (mapcat (fn [[ns {:keys [entry-tags]}]]
                 (map (fn [tag] [tag ns]) entry-tags)))
       (into {})))

(def uri->namespace
  (->> namespaces
       (keep (fn [[ns {:keys [uri]}]] (when uri [uri ns])))
       (into {})))

(defonce ^:dynamic *watcher* nil)

(defonce roots (core-atom (empty-js-map))) ; roots created by r/render and mounted in DOM

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

(defn- remove-watcher-from-runtime-queue! [watcher]
  (when-let [runtime (-> watcher meta* :runtime)]
    (swap! runtime update :pending-watchers
           (fn [queue]
             (let [existing (or queue [])]
               (into [] (remove #(= watcher %) existing)))))))

(defn- watcher-entry-key [watcher]
  (or (-> watcher meta* :normalized-component)
      watcher))

(defn- register-watcher-with-host! [host watchers-atom watcher]
  (let [meta-info (meta* watcher)
        runtime (:runtime meta-info)
        component-key (:normalized-component meta-info)]
    (when (and runtime component-key host)
      (swap! runtime
             (fn [state]
               (let [existing (get-in state [:subscriptions component-key host])]
                 (if (= watcher (:watcher existing))
                   state
                   (let [subs (or (:subscriptions state) (empty-js-map))
                         component-map (or (get subs component-key) (empty-js-map))
                         entry {:host host
                                :watchers-atom watchers-atom
                                :watcher watcher}
                         new-component-map (assoc component-map host entry)
                         new-subs (assoc subs component-key new-component-map)]
                     (assoc state :subscriptions new-subs)))))))))

(defn- render-state-runtime [render-state]
  (when render-state
    (:runtime @render-state)))

(defn- runtime-component-cache [runtime]
  (when runtime
    (:component-instances @runtime)))

(defn- update-component-cache! [runtime update-fn]
  (when runtime
    (swap! runtime update :component-instances
           (fn [instances]
             (update-fn (or instances (empty-js-map)))))))

(defn- runtime-mounted-info [runtime normalized-component]
  (when runtime
    (get (:mounted-components @runtime) normalized-component)))

(defn- assoc-runtime-mounted-info! [runtime normalized-component info]
  (when runtime
    (swap! runtime update :mounted-components
           (fn [components]
             (assoc (or components (empty-js-map)) normalized-component info)))))

(defn- create-render-state [{:keys [normalized-component container base-namespace runtime]}]
  (let [state {:active true
               :positional-key-counter 0
               :base-namespace (normalize-namespace base-namespace)}
        state (cond-> state
                normalized-component (assoc :normalized-component normalized-component)
                container (assoc :container container)
                runtime (assoc :runtime runtime))]
    (core-atom state)))

(defn- next-positional-key! [render-state]
  (let [next-val (inc (:positional-key-counter @render-state))]
    (swap! render-state assoc :positional-key-counter next-val)
    next-val))

(defn- run-watcher-now [watcher]
  (let [old-watcher *watcher*]
    (try
      (set! *watcher* watcher)
      (watcher)
      (finally
        (set! *watcher* old-watcher)))))

(defn- flush-queued-watchers [runtime]
  (let [queued (:pending-watchers @runtime)]
    (swap! runtime
           (fn [state]
             (-> state
                 (assoc :pending-watchers [])
                 (assoc :watcher-flush-scheduled? false))))
    (doseq [watcher queued]
      (run-watcher-now watcher))))

(defn- schedule-watcher-flush! [runtime]
  (when (and runtime (not (:watcher-flush-scheduled? @runtime)))
    (swap! runtime assoc :watcher-flush-scheduled? true)
    (let [flush-fn #(flush-queued-watchers runtime)]
      (if (some? (.-queueMicrotask js/globalThis))
        (.queueMicrotask js/globalThis flush-fn)
        (js/setTimeout flush-fn 0)))))

(defn- queue-watcher!
  [watcher]
  (if-let [runtime (-> watcher meta* :runtime)]
    (do
      (swap! runtime update :pending-watchers (fnil conj []) watcher)
      (schedule-watcher-flush! runtime))
    (run-watcher-now watcher)))

(defn- should-defer-watcher? [watcher]
  (let [meta-info (meta* watcher)
        defer-fn (and meta-info (:should-defer? meta-info))]
    (boolean (and (fn? defer-fn)
                  (defer-fn)))))

(declare hiccup->dom)
(declare modify-dom)

(defn- with-watcher-bound [normalized-component render-state f]
  (let [old-watcher *watcher*
        runtime (render-state-runtime render-state)
        watcher-fn (with-meta* #(modify-dom runtime normalized-component)
                     {:normalized-component normalized-component
                      :should-defer? #(boolean (:active @render-state))
                      :runtime runtime})]
    (try
      (set! *watcher* watcher-fn)
      (f)
      (finally
        (set! *watcher* old-watcher)))))

(defn- remove-watchers-for-component [runtime normalized-component]
  (let [runtime-state (when runtime @runtime)
        subscriptions (get-in runtime-state [:subscriptions normalized-component])]
    (when (and runtime normalized-component)
      (when (seq subscriptions)
        (doseq [{:keys [watchers-atom watcher]} (vals subscriptions)]
          (remove-watcher-from-runtime-queue! watcher)
          (when (and watchers-atom watcher)
            (let [key (watcher-entry-key watcher)]
              (swap! watchers-atom
                     (fn [state]
                       (dissoc (or state (empty-js-map)) key))))))))
      (swap! runtime
             (fn [state]
               (let [subs (or (:subscriptions state) (empty-js-map))
                     new-subs (dissoc subs normalized-component)]
                 (assoc state :subscriptions new-subs)))))
    nil)

(defn- remove-all-runtime-watchers! [runtime]
  (when runtime
    (let [components (keys (or (:subscriptions @runtime) (empty-js-map)))]
      (doseq [component components]
        (remove-watchers-for-component runtime component)))))

;; *** hiccup-to-dom implementation ***

(defn- style-map->css-str [style-map]
  (apply str (map (fn [[k v]] (str k ":" v ";")) style-map)))

(defn- get-event-name [k tag-name]
  (cond
    (and (= :on-change k)
         (#{"INPUT" "TEXTAREA"} tag-name))
    "oninput"
    (= :on-double-click k)
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
      (if (and (= "SELECT" (.-tagName element)) (.-multiple element))
        (let [value-set (set value)]
          (doseq [opt (.-options element)]
            (aset opt "selected" (contains? value-set (.-value opt)))))
        (aset element "value" value)))
    element))


(defn- normalize-component [component render-state]
  (when (vector? component)
    (let [first-element (aget component 0)
          params (subvec component 1)]
      (cond
        (fn? first-element)
        (let [a-fn first-element
              params-vec (vec params)
              component-meta (meta component)
              runtime (render-state-runtime render-state)
              component-cache (runtime-component-cache runtime)
              fn-cache (when component-cache (get component-cache a-fn))
              cached-form1-instance (when fn-cache (get fn-cache :form-1-instance))
              instance-key (if (contains? component-meta :key)
                             (:key component-meta)
                             (if render-state
                               (next-positional-key! render-state)
                               (random-uuid)))
              cached-form2-instance (when fn-cache (get fn-cache instance-key))]
          (cond
            cached-form2-instance
            (into [(:instance cached-form2-instance)] params-vec)

            cached-form1-instance
            (into [(:instance cached-form1-instance)] params-vec)

            :else
            (let [func-or-hiccup (apply a-fn params-vec)]
              (if (fn? func-or-hiccup)
                ;; Form-2 component (stateful)
                (let [closure func-or-hiccup
                      instance {:reagent-render closure}
                      result (into [instance] params-vec)]
                  (update-component-cache! runtime
                                           (fn [cache]
                                             (let [fn-cache (or (get cache a-fn) (empty-js-map))
                                                   new-fn-cache (assoc fn-cache instance-key {:type :form-2
                                                                                             :instance instance})]
                                               (assoc cache a-fn new-fn-cache))))
                  result)
                ;; Form-1 component (stateless)
                (let [instance {:reagent-render a-fn}
                      result (into [instance] params-vec)]
                  (update-component-cache! runtime
                                           (fn [cache]
                                             (let [fn-cache (or (get cache a-fn) (empty-js-map))
                                                   new-fn-cache (assoc fn-cache :form-1-instance {:type :form-1
                                                                                                  :instance instance})]
                                               (assoc cache a-fn new-fn-cache))))
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

(defn- hiccup->dom
  ([hiccup render-state]
   (hiccup->dom hiccup (namespace-uri default-namespace) render-state))
  ([hiccup current-ns render-state]
   (let [result
         (cond
           (or (string? hiccup) (number? hiccup))
           (.createTextNode js/document (str hiccup))

           (vector? hiccup)
           (let [tag (aget hiccup 0)]
             (cond
               (fn? tag) (hiccup->dom (component->hiccup (normalize-component hiccup render-state)) current-ns render-state)
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

           (or (nil? hiccup) (boolean? hiccup)) nil

           :else
           (.createTextNode js/document (str hiccup)))]
     result)))

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

(declare patch)

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

(defn- patch
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
        (when-not (instance? js/DocumentFragment dom-a)
          (.replaceWith dom-a new-node))
        new-node)

      :else
      (do (patch-attributes hiccup-a-realized hiccup-b-rendered dom-a)
          (patch-children hiccup-a-realized hiccup-b-rendered dom-a render-state)
          (let [a-attrs (get-attrs hiccup-a-realized)
                b-attrs (get-attrs hiccup-b-rendered)
                b-value (:value b-attrs)]
            (when (and (contains? b-attrs :value) (not= (:value a-attrs) b-value))
              (if (and (= "SELECT" (.-tagName dom-a) ) (.-multiple dom-a))
                (let [value-set (set b-value)]
                  (doseq [opt (.-options dom-a)]
                    (aset opt "selected" (contains? value-set (.-value opt)))))
                (aset dom-a "value" b-value))))
          dom-a))))

(defn- modify-dom [runtime normalized-component]
  (remove-watchers-for-component runtime normalized-component)
  (when-let [mounted-info (and runtime
                               (runtime-mounted-info runtime normalized-component))]
    (let [{:keys [hiccup dom container base-namespace]} mounted-info
          render-state (create-render-state {:normalized-component normalized-component
                                             :container container
                                             :base-namespace (or base-namespace
                                                                 (dom->namespace container))
                                             :runtime runtime})]
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
                (assoc-runtime-mounted-info! runtime normalized-component
                  {:hiccup new-hiccup-rendered
                   :dom dom
                   :container container
                   :base-namespace base-ns
                   :runtime runtime})))
            (let [_ (swap! render-state assoc :positional-key-counter 0)
                  new-dom (patch hiccup new-hiccup-rendered dom render-state)
                  base-ns (:base-namespace @render-state)]
              (assoc-runtime-mounted-info! runtime normalized-component
                {:hiccup new-hiccup-rendered
                 :dom new-dom
                 :container container
                 :base-namespace base-ns
                 :runtime runtime})
              (when (not= dom new-dom)
                (aset container "innerHTML" "")
                (.appendChild container new-dom)))))
        (finally
          (swap! render-state assoc :active false))))))

(defn- notify-watchers [watchers]
  (doseq [watcher (vals (or @watchers (empty-js-map)))]
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

(defn- unmount-components [container]
  (when-let [{:keys [runtime]} (get @roots container)]
    (when runtime
      (remove-all-runtime-watchers! runtime))
    (when runtime
      (swap! runtime
             (fn [state]
               (-> state
                   (assoc :mounted-components (empty-js-map))
                   (assoc :component-instances (empty-js-map))
                   (assoc :pending-watchers [])
                   (assoc :watcher-flush-scheduled? false)
                   (assoc :subscriptions (empty-js-map))))))
    (swap! roots dissoc container))
  (doseq [child (vec (aget container "childNodes"))]
    (remove-node-and-unmount! child)))

(defn- do-render [normalized-component container render-state]
  (unmount-components container)
  (swap! render-state assoc :positional-key-counter 0)
  (try
    (let [runtime (render-state-runtime render-state)
          base-ns (:base-namespace @render-state)
          [hiccup dom]
          (add-modify-dom-watcher-on-ratom-deref
            normalized-component
            render-state)
          _ (swap! render-state assoc :positional-key-counter 0)
          hiccup-rendered (fully-render-hiccup hiccup render-state)]
      (.appendChild container dom)
      (assoc-runtime-mounted-info! runtime normalized-component
                                   {:hiccup hiccup-rendered
                                    :dom dom
                                    :container container
                                    :base-namespace base-ns
                                    :runtime runtime})
      (when container
        (swap! roots assoc container
               {:container container
                :component normalized-component
                :runtime runtime})))
    (finally
      (swap! render-state assoc :active false))))

; mirrored as atom below
(defn- ratom [initial-value]
  (let [a (core-atom initial-value)
        orig-deref (aget a "_deref")
        orig-reset_BANG_ (aget a "_reset_BANG_")]
    (aset a "watchers" (core-atom (empty-js-map)))
    (aset a "cursors" (core-atom #{}))
    (aset a "_deref" (fn []
                       (when *watcher*
                         (let [watchers (aget a "watchers")
                               current @watchers
                               watcher-key (watcher-entry-key *watcher*)]
                           (when-not (contains? current watcher-key)
                             (swap! watchers
                                    (fn [state]
                                      (assoc (or state (empty-js-map)) watcher-key *watcher*))))
                           (register-watcher-with-host! a watchers *watcher*)))
                      (.call orig-deref a)))
    (aset a "_reset_BANG_" (fn [new-val]
                             (let [res (.call orig-reset_BANG_ a new-val)]
                               (notify-watchers (aget a "watchers"))
                               (doseq [c @(aget a "cursors")]
                                 (notify-watchers (aget c "watchers")))
                               res)))
    a))

; *** Reagent API functions *** ;

;; Reagent API
(defn cursor [the-ratom path]
  (let [cursors (aget the-ratom "cursors")
        found-cursor (some (fn [c] (when (= path (aget c "path")) c)) @cursors)]
    (if (nil? found-cursor)
      (let [watchers (core-atom (empty-js-map))
            this-cursor (js-obj)]
        (aset this-cursor "_deref"
              (fn []
                (when *watcher*
                  (let [current @watchers
                        watcher-key (watcher-entry-key *watcher*)]
                    (when-not (contains? current watcher-key)
                      (swap! watchers
                             (fn [state]
                               (assoc (or state (empty-js-map)) watcher-key *watcher*))))
                    (register-watcher-with-host! this-cursor watchers *watcher*)))
                (let [old-watcher *watcher*]
                  (try
                    (set! *watcher* nil)
                    (get-in @the-ratom path)
                    (finally
                      (set! *watcher* old-watcher))))))
        (aset this-cursor "_swap"
              (fn [f & args]
                (swap! the-ratom
                  (fn [current-state]
                    (let [current-cursor-value (get-in current-state path)
                          new-cursor-value (apply f current-cursor-value args)]
                      (assoc-in current-state path new-cursor-value))))))
        (aset this-cursor "watchers" watchers)
        (aset this-cursor "path" path)
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
  (let [runtime (core-atom {:runtime-id (str "runtime-" (random-uuid))
                            :component-instances (empty-js-map)
                            :pending-watchers []
                            :watcher-flush-scheduled? false
                            :mounted-components (empty-js-map)
                            :subscriptions (empty-js-map)})
        base-ns (dom->namespace container)
        render-state (create-render-state {:container container
                                           :base-namespace base-ns
                                           :runtime runtime})
        normalized (normalize-component component render-state)]
    (swap! render-state assoc :normalized-component normalized)
    (do-render normalized container render-state)))

;; Reagent API
#_:clj-kondo/ignore
(def atom ratom)

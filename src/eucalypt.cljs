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
  (if dom
    (normalize-namespace (.-namespaceURI dom))
    (namespace-uri default-namespace)))

(defn- with-meta* [obj m]
  (doto obj (aset "---meta" m)))

(defn- meta* [obj]
  (aget obj "---meta"))

(defn- remove-watcher-from-runtime-queue! [watcher]
  (when-let [runtime (-> watcher meta* :runtime)]
    (swap! runtime update :pending-watchers
           #(vec (remove (partial = watcher) (or % []))))))

(defn- watcher-entry-key [watcher]
  (or (-> watcher meta* :normalized-component)
      watcher))

(defn- register-watcher-with-host! [host watchers-atom watcher]
  (let [{:keys [runtime normalized-component]} (meta* watcher)]
    (when (and runtime normalized-component host)
      (swap! runtime
             (fn [state]
               (if (= watcher (get-in state [:subscriptions normalized-component host :watcher]))
                 state
                 (let [entry {:host host :watchers-atom watchers-atom :watcher watcher}]
                   (assoc-in state [:subscriptions normalized-component host] entry))))))))

(defn- ensure-watcher-registered! [host watchers-atom]
  (when *watcher*
    (let [watcher-key (watcher-entry-key *watcher*)]
      (when-not (contains? @watchers-atom watcher-key)
        (swap! watchers-atom assoc watcher-key *watcher*)))
    (register-watcher-with-host! host watchers-atom *watcher*)))

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
  (let [next-val (swap! render-state update :positional-key-counter inc)]
    (:positional-key-counter next-val)))

(defn- reset-positional-counter! [render-state]
  (swap! render-state assoc :positional-key-counter 0))

(defn- run-watcher-now [watcher]
  (let [old-watcher *watcher*]
    (try
      (set! *watcher* watcher)
      (watcher)
      (finally
        (set! *watcher* old-watcher)))))

(defn- flush-queued-watchers [runtime]
  (let [queued (:pending-watchers @runtime)]
    (swap! runtime assoc :pending-watchers [] :watcher-flush-scheduled? false)
    (run! run-watcher-now queued)))

(defn- schedule-watcher-flush! [runtime]
  (when (and runtime (not (:watcher-flush-scheduled? @runtime)))
    (swap! runtime assoc :watcher-flush-scheduled? true)
    (let [flush-fn #(flush-queued-watchers runtime)]
      (if (.-queueMicrotask js/globalThis)
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
  (when-let [{:keys [should-defer?]} (meta* watcher)]
    (and (fn? should-defer?) (should-defer?))))

(declare hiccup->dom)
(declare modify-dom)

(defn- with-watcher-bound [normalized-component render-state f]
  (let [old-watcher *watcher*
        {:keys [runtime]} @render-state
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

(defn- text-like? [x]
  (or (string? x) (number? x)))

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
    (-> k
        .toLowerCase
        (.replaceAll "-" ""))))

(defn- assign-event! [element event-key handler]
  (let [event-name (get-event-name event-key (.-tagName element))]
    (aset element event-name (if (fn? handler) handler nil))))

(defn- apply-style! [element style-map]
  (if (not-empty style-map)
    (.setAttribute element "style" (style-map->css-str style-map))
    (.removeAttribute element "style")))

(defn- apply-class! [element class-val]
  (let [normalized (if (and (sequential? class-val) (not (string? class-val)))
                     (.join (vec (remove nil? class-val)) " ")
                     class-val)]
    (if (or (nil? normalized) (= "" normalized))
      (.removeAttribute element "class")
      (.setAttribute element "class" normalized))))

(defn- queue-ref-mount! [render-state new-ref element]
  (when new-ref
    (when-let [runtime-atom (:runtime @render-state)]
      (when-let [ref-queue-atom (:ref-queue @runtime-atom)]
        (swap! ref-queue-atom conj [new-ref element])))))

(defn- flush-ref-queue! [runtime]
  (when-let [ref-queue-atom (:ref-queue @runtime)]
    (let [refs-to-process @ref-queue-atom]
      (when (seq refs-to-process)
        (reset! ref-queue-atom [])
        (doseq [[ref-fn value] refs-to-process]
          (ref-fn value))))))

(defn- set-or-remove-attribute! [element k v]
  (cond
    (let [s (str k)]
      (and (> (count s) 2)
           (.startsWith s "on")
           (let [c3 (.charCodeAt s 2)]
             (or (= c3 45) ; "-"
                 (and (>= c3 65) (<= c3 90)))))) ; "A" to "Z"
    (assign-event! element k v)
    (= :style k) (apply-style! element v)
    (= :class k) (apply-class! element v)
    (or (= :checked k) (= :selected k)) (aset element k v)
    :else (if (nil? v)
            (.removeAttribute element k)
            (.setAttributeNS element nil k v))))

(defn- set-attributes! [element attrs]
  (doseq [[k v] attrs]
    (when (not= k :ref)
      (cond
        (= :xmlns k) nil
        :else (set-or-remove-attribute! element k v)))))

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
        danger-html (get-in attrs [:dangerouslySetInnerHTML :__html])
        attrs-without-value (dissoc attrs :value :dangerouslySetInnerHTML)
        current-ns-normalized (normalize-namespace current-ns)
        element-ns (if (get entry-tag->namespace tag-name)
                     (next-namespace current-ns-normalized tag-name)
                     current-ns-normalized)
        children-ns (next-namespace element-ns tag-name)
        element (.createElementNS js/document element-ns tag-name)]
    (set-attributes! element attrs-without-value)
    (if (some? danger-html)
      (set! (.-innerHTML element) danger-html)
      (doseq [child content]
        (when-let [child-node (hiccup->dom child children-ns render-state)]
          (.appendChild element child-node))))
    (let [new-ref (:ref attrs)]
      (when new-ref
        (aset element "---ref-fn" new-ref)
        (queue-ref-mount! render-state new-ref element)))
    (when (some? value)
      (if (and (= "SELECT" (.-tagName element)) (.-multiple element))
        (let [value-set (set value)]
          (doseq [opt (.-options element)]
            (aset opt "selected" (contains? value-set (.-value opt)))))
        (aset element "value" value)))
    element))

(defn- component->hiccup [normalized-component]
  (let [[config & params] normalized-component
        reagent-render (:reagent-render config)]
    (apply reagent-render params)))

(defn- fetch-or-create-component-instance [a-fn params-vec component-meta render-state]
  (let [{:keys [runtime]} (when render-state @render-state)
        component-cache (runtime-component-cache runtime)
        fn-cache (when component-cache (get component-cache a-fn))
        instance-key (if (contains? component-meta :key)
                       (:key component-meta)
                       (if render-state
                         (next-positional-key! render-state)
                         (random-uuid)))
        cached-instance (or (get-in fn-cache [instance-key :instance])
                            (get-in fn-cache [:form-1-instance :instance]))]
    (or cached-instance
        (let [func-or-hiccup (apply a-fn params-vec)
              [instance cache-key type] (if (fn? func-or-hiccup)
                                          [{:reagent-render func-or-hiccup} instance-key :form-2]
                                          [{:reagent-render a-fn} :form-1-instance :form-1])]
          (update-component-cache! runtime
                                   (fn [cache]
                                     (let [fn-cache (or (get cache a-fn) (empty-js-map))
                                           new-fn-cache (assoc fn-cache cache-key {:type type :instance instance})]
                                       (assoc cache a-fn new-fn-cache))))
          instance))))

(defn- normalize-component [component render-state]
  (when (vector? component)
    (let [first-element (aget component 0)
          params (subvec component 1)]
      (cond
        (fn? first-element)
        (let [instance (fetch-or-create-component-instance first-element (vec params) (meta component) render-state)]
          (into [instance] params))

        (string? first-element)
        (into [{:reagent-render (fn [] component)}]
              params)

        (map? first-element)
        (let [component-as-map first-element
              render-fn (:reagent-render component-as-map)
              comp-with-lifecycle {:reagent-render render-fn}]
          (into [comp-with-lifecycle] params))))))

(defn- expand-hiccup [hiccup render-state]
  (loop [hiccup' hiccup]
    (cond
      (and (vector? hiccup') (fn? (first hiccup')))
      (recur (component->hiccup (normalize-component hiccup' render-state)))

      (and (map? hiccup') (:reagent-render hiccup'))
      (recur ((:reagent-render hiccup')))

      :else
      hiccup')))

(defn- hiccup->dom
  ([hiccup render-state]
   (hiccup->dom hiccup (namespace-uri default-namespace) render-state))
  ([hiccup current-ns render-state]
   (let [hiccup (expand-hiccup hiccup render-state)
         result
         (cond
           (text-like? hiccup)
           (.createTextNode js/document (str hiccup))

           (vector? hiccup)
           (let [tag (aget hiccup 0)]
             (if (= :<> tag)
               (let [fragment (.createDocumentFragment js/document)]
                 (doseq [child (rest hiccup)]
                   (when-let [child-node (hiccup->dom child current-ns render-state)]
                     (.appendChild fragment child-node)))
                 fragment)
               (create-element hiccup current-ns render-state)))

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

(defn- normalized-hiccup-children [hiccup]
  (vec (remove nil? (get-hiccup-children hiccup))))

(defn- hiccup-seq? [x]
  (and (seq? x)
       (not (string? x))
       (not (vector? x))))

(defn- get-key [hiccup]
  (when (vector? hiccup)
    (-> hiccup meta :key)))

(defn- get-type [hiccup]
  (cond
    (vector? hiccup) (:tag-name (parse-tag (first hiccup)))
    (text-like? hiccup) :<text>
    :else nil))

(defn- fully-render-hiccup [hiccup render-state]
  (let [hiccup (expand-hiccup hiccup render-state)
        result
        (cond
          (nil? hiccup) nil
          (hiccup-seq? hiccup)
          (mapv #(fully-render-hiccup % render-state) hiccup)

          (vector? hiccup)
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
                          [] children)))
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
  (let [old-hiccup-children (normalized-hiccup-children hiccup-a-rendered)
        new-hiccup-children (normalized-hiccup-children hiccup-b-rendered)
        old-dom-nodes (vec (.-childNodes dom-a))
        parent-ns (dom->namespace dom-a)

        old-keyed-map (into {}
                            (keep-indexed (fn [idx child]
                                            (when-let [key (get-key child)]
                                              [key {:hiccup child
                                                    :dom (get old-dom-nodes idx)}])))
                            old-hiccup-children)

        old-unkeyed-pool (vec (for [i (range (count old-hiccup-children))
                                    :let [child (nth old-hiccup-children i)]
                                    :when (not (get-key child))]
                                {:hiccup (nth old-hiccup-children i)
                                 :dom (nth old-dom-nodes i)
                                 :used? false}))

        new-dom-nodes
        (mapv (fn [new-child]
                (let [key (get-key new-child)
                      old-match (if key
                                  (let [match (get old-keyed-map key)]
                                    (when match (js-delete old-keyed-map key))
                                    match)
                                  (let [match-idx (first (keep-indexed (fn [idx old-info]
                                                                         (when (and (not (:used? old-info))
                                                                                    (= (get-type (:hiccup old-info)) (get-type new-child)))
                                                                           idx))
                                                                       old-unkeyed-pool))]
                                    (when (some? match-idx)
                                      (let [match (get old-unkeyed-pool match-idx)]
                                        (aset match "used?" true)
                                        match))))]
                  (if old-match
                    (patch (:hiccup old-match) new-child (:dom old-match) render-state)
                    (hiccup->dom new-child parent-ns render-state))))
              new-hiccup-children)]

    ;; Remove unused old nodes
    (doseq [old-info (vals old-keyed-map)]
      (remove-node-and-unmount! (:dom old-info)))
    (doseq [old-info (filter #(not (:used? %)) old-unkeyed-pool)]
      (remove-node-and-unmount! (:dom old-info)))

    ;; Re-order/add nodes in the DOM
    (let [num-new (count new-dom-nodes)]
      (dotimes [i num-new]
        (let [desired-node (nth new-dom-nodes i)
              current-node (get (.-childNodes dom-a) i)]
          (when-not (identical? desired-node current-node)
            (.insertBefore dom-a desired-node (or current-node nil)))))
      ;; Remove any extra nodes from the end
      (while (> (.-length (.-childNodes dom-a)) num-new)
        (let [last-child (.-lastChild dom-a)]
          (remove-node-and-unmount! last-child))))))

(defn- get-attrs [hiccup]
  (let [s (second hiccup)]
    (if (map? s) s {})))

(defn- patch-attributes [hiccup-a-rendered hiccup-b-rendered dom-a]
  (let [a-attrs (get-attrs hiccup-a-rendered)
        b-attrs (get-attrs hiccup-b-rendered)
        all-keys (set (concat (keys a-attrs) (keys b-attrs)))]
    (doseq [k all-keys]
      (when (and (not= k :ref) (not= k :xmlns) (not= k :value) (not= k :dangerouslySetInnerHTML))
        (let [old-v (get a-attrs k)
              new-v (get b-attrs k)]
          (when (not (= old-v new-v))
            (set-or-remove-attribute! dom-a k new-v)))))))

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
  (if (identical? hiccup-a-rendered hiccup-b-rendered)
    dom-a
    (let [hiccup-a-realized (realize-deep hiccup-a-rendered)
          hiccup-b-realized (realize-deep hiccup-b-rendered)]
      (cond
        (= hiccup-a-realized hiccup-b-realized)
        dom-a

        (and (text-like? hiccup-a-realized)
             (text-like? hiccup-b-realized)
             (= (.-nodeType dom-a) 3))
        (do
          (when (not (= (str hiccup-a-realized) (str hiccup-b-realized)))
            (set! (.-data dom-a) (str hiccup-b-realized)))
          dom-a)

        (or (not (vector? hiccup-a-realized))
            (not (vector? hiccup-b-realized))
            (not= (get-type hiccup-a-realized)
                  (get-type hiccup-b-realized)))
        (let [parent (.-parentNode dom-a)
              parent-ns (dom->namespace parent)]
          (unmount-node-and-children dom-a)
          (hiccup->dom hiccup-b-realized parent-ns render-state))

        :else
        (do (patch-attributes hiccup-a-realized hiccup-b-realized dom-a)
            (let [a-attrs (get-attrs hiccup-a-realized)
                  b-attrs (get-attrs hiccup-b-realized)
                  a-html (get-in a-attrs [:dangerouslySetInnerHTML :__html])
                  b-html (get-in b-attrs [:dangerouslySetInnerHTML :__html])]
              (cond
                (some? b-html)
                (when (not (= a-html b-html))
                  (set! (.-innerHTML dom-a) b-html))

                (some? a-html) ; b-html is nil
                (do
                  (set! (.-innerHTML dom-a) "")
                  (patch-children hiccup-a-realized hiccup-b-realized dom-a render-state))

                :else
                (patch-children hiccup-a-realized hiccup-b-realized dom-a render-state)))
            (let [old-ref (:ref (get-attrs hiccup-a-realized))
                  new-ref (:ref (get-attrs hiccup-b-realized))]
              (when (not (= old-ref new-ref))
                (when old-ref
                  (old-ref nil))
                (queue-ref-mount! render-state new-ref dom-a)
                (aset dom-a "---ref-fn" new-ref)))
            (let [a-attrs (get-attrs hiccup-a-realized)
                  b-attrs (get-attrs hiccup-b-realized)
                  b-value (:value b-attrs)]
              (when (and (contains? b-attrs :value) (not (= (:value a-attrs) b-value)))
                (if (and (= "SELECT" (.-tagName dom-a) ) (.-multiple dom-a))
                  (let [value-set (set b-value)]
                    (doseq [opt (.-options dom-a)]
                      (aset opt "selected" (contains? value-set (.-value opt)))))
                  (let [tag-name (.-tagName dom-a)
                        is-input? (or (= "INPUT" tag-name) (= "TEXTAREA" tag-name))
                        is-active? (identical? dom-a (.-activeElement js/document))]
                    (if (and is-input? is-active?)
                      (let [start (.-selectionStart dom-a)
                            end (.-selectionEnd dom-a)]
                        (aset dom-a "value" b-value)
                        (set! (.-selectionStart dom-a) start)
                        (set! (.-selectionEnd dom-a) end))
                      (aset dom-a "value" b-value))))))
            dom-a)))))

(defn- modify-dom [runtime normalized-component]
  (if (contains? (:rendering-components @runtime) normalized-component)
    (when *watcher*
      (queue-watcher! *watcher*))
    (try
      (swap! runtime update :rendering-components (fnil conj #{}) normalized-component)
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
            (reset-positional-counter! render-state)
            (let [new-hiccup-unrendered (with-watcher-bound
                                          normalized-component
                                          render-state
                                          (fn [] (component->hiccup normalized-component)))
                  _ (reset-positional-counter! render-state)
                  new-hiccup-rendered (fully-render-hiccup new-hiccup-unrendered render-state)
                  base-ns (:base-namespace @render-state)]
              (assoc-runtime-mounted-info!
                runtime normalized-component
                {:hiccup new-hiccup-rendered
                 :container container
                 :base-namespace base-ns
                 :runtime runtime
                 :dom
                 (if (and (vector? hiccup) (= :<> (first hiccup)))
                   (do
                     (reset-positional-counter! render-state)
                     (patch-children hiccup new-hiccup-rendered container render-state)
                     dom)
                   (do
                     (reset-positional-counter! render-state)
                     (let [new-dom (patch hiccup new-hiccup-rendered dom render-state)]
                       (when (not (identical? dom new-dom))
                         (aset container "innerHTML" "")
                         (.appendChild container new-dom))
                       new-dom)))}))
            (finally
              (swap! render-state assoc :active false)))))
      (finally
        (flush-ref-queue! runtime)
        (swap! runtime update :rendering-components disj normalized-component)))))

(defn- notify-watchers [watchers]
  (doseq [watcher (vals @watchers)]
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
      (remove-all-runtime-watchers! runtime)))
  (swap! roots dissoc container)
  (doseq [child (vec (aget container "childNodes"))]
    (remove-node-and-unmount! child)))

(defn- update-mounted-info! [runtime normalized-component hiccup dom container render-state]
  (assoc-runtime-mounted-info! runtime normalized-component
                               {:hiccup hiccup
                                :dom dom
                                :container container
                                :base-namespace (:base-namespace @render-state)
                                :runtime runtime}))

(defn- do-render [normalized-component container render-state]
  (unmount-components container)
  (reset-positional-counter! render-state)
  (try
    (let [{:keys [runtime]} @render-state
          [hiccup dom]
          (add-modify-dom-watcher-on-ratom-deref
            normalized-component
            render-state)
          _ (reset-positional-counter! render-state)
          hiccup-rendered (fully-render-hiccup hiccup render-state)]
      (.appendChild container dom)
      (update-mounted-info! runtime normalized-component hiccup-rendered dom container render-state)
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
                       (ensure-watcher-registered! a (aget a "watchers"))
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
                (ensure-watcher-registered! this-cursor watchers)
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

(defn- render-into-container [component container runtime old-root-info]
  (let [render-state (create-render-state {:container container
                                           :base-namespace (dom->namespace container)
                                           :runtime runtime})
        new-normalized (normalize-component component render-state)]
    (if old-root-info
      ;; Update logic
      (let [old-normalized (:component old-root-info)]
        (remove-watchers-for-component runtime old-normalized)
        (when-let [mounted-info (runtime-mounted-info runtime old-normalized)]
          (let [{:keys [hiccup dom]} mounted-info]
            (reset-positional-counter! render-state)
            (let [new-hiccup-unrendered (with-watcher-bound
                                          new-normalized
                                          render-state
                                          (fn [] (component->hiccup new-normalized)))
                  _ (reset-positional-counter! render-state)
                  new-hiccup-rendered (fully-render-hiccup new-hiccup-unrendered render-state)]
              (if (and (vector? hiccup) (= :<> (first hiccup)))
                (do
                  (patch-children hiccup new-hiccup-rendered container render-state)
                  (update-mounted-info! runtime new-normalized new-hiccup-rendered dom container render-state))
                (let [new-dom (patch hiccup new-hiccup-rendered dom render-state)]
                  (update-mounted-info! runtime new-normalized new-hiccup-rendered new-dom container render-state)
                  (when (not= dom new-dom)
                    (.replaceWith dom new-dom)
                    (swap! runtime assoc :component-instances (empty-js-map)))))
              (swap! roots assoc container (assoc old-root-info :component new-normalized))
              (when (not (identical? old-normalized new-normalized))
                (swap! runtime update :mounted-components dissoc old-normalized))
              (flush-ref-queue! runtime)))))
      ;; New render logic
      (do
        (swap! render-state assoc :normalized-component new-normalized)
        (do-render new-normalized container render-state)
        (flush-ref-queue! runtime)))))

;; Reagent API
(defn render [component container]
  (if-let [root-info (get @roots container)]
    (render-into-container component container (:runtime root-info) root-info)
    (let [runtime (core-atom {:runtime-id (str "runtime-" (random-uuid))
                              :ref-queue (core-atom [])
                              :component-instances (empty-js-map)
                              :pending-watchers []
                              :watcher-flush-scheduled? false
                              :mounted-components (empty-js-map)
                              :subscriptions (empty-js-map)
                              :rendering-components #{}})]
      (render-into-container component container runtime nil))))

;; Reagent API
#_:clj-kondo/ignore
(def atom ratom)

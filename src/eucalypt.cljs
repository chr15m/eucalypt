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
                       (dissoc (or state (empty-js-map)) key)))))))
      (swap! runtime
             (fn [state]
               (let [subs (or (:subscriptions state) (empty-js-map))
                     new-subs (dissoc subs normalized-component)]
                 (assoc state :subscriptions new-subs)))))
    nil))

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

(defn- apply-style! [element style-val]
  (let [css-text (if (string? style-val)
                   style-val
                   (when (map? style-val)
                     (style-map->css-str style-val)))]
    (if (not-empty css-text)
      (.setAttribute element "style" css-text)
      (.removeAttribute element "style"))))

(defn- apply-class! [element class-val]
  (let [normalized (if (and (sequential? class-val) (not (string? class-val)))
                     (.join (vec (remove nil? class-val)) " ")
                     class-val)]
    (if (or (nil? normalized) (= "" normalized))
      (.removeAttribute element "class")
      (.setAttribute element "class" normalized))))

(defn- queue-ref-mount! [render-state new-ref element]
  (when new-ref
    (when element
      (aset element "---ref-cleanup" nil))
    (when-let [runtime-atom (:runtime @render-state)]
      (when-let [ref-queue-atom (:ref-queue @runtime-atom)]
        (swap! ref-queue-atom conj [new-ref element])))))

(defn- store-ref-cleanup! [element cleanup]
  (when element
    (if (fn? cleanup)
      (aset element "---ref-cleanup" cleanup)
      (aset element "---ref-cleanup" nil))))

(defn- call-ref-cleanup! [element]
  (when element
    (let [cleanup (aget element "---ref-cleanup")
          ref-fn (aget element "---ref-fn")]
      (cond
        (fn? cleanup)
        (cleanup)

        (fn? ref-fn)
        (ref-fn nil)))
    (aset element "---ref-cleanup" nil)
    (aset element "---ref-fn" nil)))

(defn- flush-ref-queue! [runtime]
  (when-let [ref-queue-atom (:ref-queue @runtime)]
    (let [refs-to-process @ref-queue-atom]
      (when (seq refs-to-process)
        (reset! ref-queue-atom [])
        (doseq [[ref-fn value] refs-to-process]
          (let [cleanup (ref-fn value)]
            (store-ref-cleanup! value cleanup)))))))

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
        (aset element "---ref-cleanup" nil)
        (queue-ref-mount! render-state new-ref element)))
    (when (some? value)
      (if (and (= "SELECT" (.-tagName element)) (.-multiple element))
        (let [value-set (set value)]
          (doseq [opt (.-options element)]
            (aset opt "selected" (contains? value-set (.-value opt)))))
        (aset element "value" value)))
    element))

(defn- component->hiccup [normalized-component render-state]
  (let [instance (first normalized-component)
        params (rest normalized-component)]
    (if (:needs-initialization instance)
      (let [{:keys [runtime]} @render-state
            outer-render-fn (:reagent-render instance)
            result (apply outer-render-fn params)
            is-form-2? (fn? result)]
        ;; MUTATE THE INSTANCE
        (js-delete instance "needs-initialization")
        (when is-form-2?
          (aset instance "reagent-render" result))
        ;; UPDATE CACHE FOR FORM-1
        (when-not is-form-2?
          (update-component-cache! runtime
                                   (fn [cache]
                                     (let [a-fn (:component-fn instance)
                                           instance-key (:instance-key instance)
                                           fn-cache (get cache a-fn)]
                                       ;; Move from instance-key to :form-1-instance
                                       (if (and fn-cache (get fn-cache instance-key))
                                         (-> cache
                                             (assoc-in [a-fn :form-1-instance] {:instance instance})
                                             (update-in [a-fn] dissoc instance-key))
                                         cache)))))
        (if is-form-2? (apply result params) result))
      (let [reagent-render (:reagent-render instance)]
        (apply reagent-render params)))))

(defn- fetch-or-create-component-instance [a-fn _params-vec component-meta render-state]
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
        (let [instance {:reagent-render a-fn
                        :component-fn a-fn
                        :instance-key instance-key
                        :needs-initialization true}]
          (update-component-cache! runtime
                                   (fn [cache]
                                     (let [fn-cache (or (get cache a-fn) (empty-js-map))
                                           new-fn-cache (assoc fn-cache instance-key {:instance instance})]
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
  (let [original-meta (meta hiccup)]
    (loop [hiccup' hiccup
           input-meta original-meta]
      (cond
        (and (vector? hiccup') (fn? (first hiccup')))
        (let [expanded (component->hiccup (normalize-component hiccup' render-state) render-state)]
          (recur expanded input-meta))

        (and (map? hiccup') (:reagent-render hiccup'))
        (recur ((:reagent-render hiccup')) input-meta)

        :else
        ;; Preserve the original metadata on the final expanded result
        (if (and input-meta (vector? hiccup') (not (meta hiccup')))
          (with-meta hiccup' input-meta)
          hiccup')))))

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
  (reduce (fn [acc child]
            (if (or (nil? child) (boolean? child))
              acc
              (conj acc child)))
          []
          (get-hiccup-children hiccup)))

(defn- hiccup-seq? [x]
  (and (seq? x)
       (not (string? x))
       (not (vector? x))))

(defn- get-key [hiccup]
  (when (vector? hiccup)
    (let [m (meta hiccup)]
      (when m
        (:key m)))))

(defn- get-type [hiccup]
  (cond
    (vector? hiccup) (:tag-name (parse-tag (first hiccup)))
    (text-like? hiccup) :<text>
    :else nil))

(defn- fully-render-hiccup [hiccup render-state]
  (let [input-meta (meta hiccup)
        ;; Check if this is a component (fn as first element) before expansion
        is-component-root (and (vector? hiccup) (fn? (first hiccup)))
        hiccup (expand-hiccup hiccup render-state)
        original-meta (or (meta hiccup) input-meta)
        ;; Add :component-root marker if this was a component
        final-meta (if is-component-root
                     (assoc (or original-meta {}) :component-root true)
                     original-meta)
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
                head (if attrs [(aget hiccup 0) attrs] [(aget hiccup 0)])
                processed-children
                (reduce (fn [acc child]
                          (let [child-meta (meta child)
                                ;; Check if child is a component before processing
                                child-is-component (and (vector? child) (fn? (first child)))
                                processed (fully-render-hiccup child render-state)
                                ;; Preserve child metadata and add component marker if needed
                                merged-meta (cond
                                              (and child-is-component child-meta)
                                              (assoc child-meta :component-root true)
                                              child-is-component
                                              {:component-root true}
                                              :else
                                              child-meta)
                                has-key? (contains? merged-meta :key)]
                            (cond
                              (or (nil? processed) (boolean? processed)) acc

                              (and (vector? processed) (= :<> (aget processed 0)) (not has-key?))
                              (into acc (subvec processed 1))

                              (hiccup-seq? child)
                              (into acc processed)

                              :else
                              (let [final-child (if (and merged-meta (vector? processed))
                                                  (with-meta processed merged-meta)
                                                  processed)]
                                (conj acc final-child)))))
                        [] children)
                result-vec (into head processed-children)]
            (if final-meta
              (with-meta result-vec final-meta)
              result-vec))
          :else
          hiccup)]
    result))

(defn- unmount-node-and-children [node]
  (when node
    (call-ref-cleanup! node)
    (doseq [child (vec (aget node "childNodes"))]
      (unmount-node-and-children child))))

(defn- remove-node-and-unmount! [node]
  (when node
    (unmount-node-and-children node)
    (.remove node)))

(declare patch)

(defn- count-dom-nodes [hiccup]
  (if (and (vector? hiccup) (= :<> (aget hiccup 0)))
    (reduce + 0 (map count-dom-nodes (subvec hiccup 1)))
    1))

(defn- patch-children [hiccup-a-rendered hiccup-b-rendered dom-a render-state]
  (let [old-hiccup-children (normalized-hiccup-children hiccup-a-rendered)
        new-hiccup-children (normalized-hiccup-children hiccup-b-rendered)
        old-dom-nodes (vec (.-childNodes dom-a))
        parent-ns (dom->namespace dom-a)

        ;; Map old children to DOM nodes (handling fragments)
        old-children-with-dom
        (let [dom-idx (atom 0)]
          (mapv (fn [child]
                  (let [node-count (count-dom-nodes child)
                        current-dom-idx @dom-idx
                        dom-nodes (subvec old-dom-nodes current-dom-idx (+ current-dom-idx node-count))]
                    (swap! dom-idx + node-count)
                    {:hiccup child
                     :dom (if (= 1 node-count) (first dom-nodes) dom-nodes)}))
                old-hiccup-children))

        old-keyed-map (into {}
                            (keep (fn [info]
                                    (when-let [key (get-key (:hiccup info))]
                                      [key info]))
                                  old-children-with-dom))

        old-unkeyed-pool (vec (for [info old-children-with-dom
                                    :when (not (get-key (:hiccup info)))]
                                (assoc info :used? false)))

        new-dom-nodes-nested
        (mapv (fn [new-child]
                (let [key (get-key new-child)
                      is-keyed-match (boolean key)
                      is-component-root (-> new-child meta :component-root)
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
                                        match))))
                      should-preserve-ref (or is-keyed-match is-component-root)]
                  (if old-match
                    (patch (:hiccup old-match) new-child (:dom old-match) render-state
                           {:preserve-ref should-preserve-ref})
                    (hiccup->dom new-child parent-ns render-state))))
              new-hiccup-children)

        ;; Flatten new DOM nodes (handle fragments and DocumentFragments)
        new-dom-nodes
        (reduce (fn [acc node]
                  (cond
                    (vector? node) (into acc node)
                    (and node (= 11 (.-nodeType node))) ;; DocumentFragment
                    (into acc (vec (.-childNodes node)))
                    node (conj acc node)
                    :else acc))
                [] new-dom-nodes-nested)]

    ;; Remove unused old nodes
    (doseq [old-info (vals old-keyed-map)]
      (let [dom (:dom old-info)]
        (if (vector? dom)
          (doseq [n dom] (remove-node-and-unmount! n))
          (remove-node-and-unmount! dom))))
    (doseq [old-info (filter #(not (:used? %)) old-unkeyed-pool)]
      (let [dom (:dom old-info)]
        (if (vector? dom)
          (doseq [n dom] (remove-node-and-unmount! n))
          (remove-node-and-unmount! dom))))

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
          (remove-node-and-unmount! last-child))))

    new-dom-nodes))

(defn- get-attrs [hiccup]
  (let [s (second hiccup)]
    (if (map? s) s {})))

(defn- fragment? [x] (and (vector? x) (= :<> (aget x 0))))

(defn- patch-attributes [h-a h-b dom]
  (let [a (get-attrs h-a) b (get-attrs h-b)]
    (doseq [k (set (concat (keys a) (keys b)))]
      (when-not (#{:ref :xmlns :value :dangerouslySetInnerHTML} k)
        (let [ov (get a k) nv (get b k)]
          (when-not (= ov nv) (set-or-remove-attribute! dom k nv)))))))

(defn- patch
  ([h-a h-b dom rs] (patch h-a h-b dom rs {}))
  ([h-a h-b dom rs opts]
   (let [pref (:preserve-ref opts)]
     (if-not dom (hiccup->dom h-b (namespace-uri default-namespace) rs)
       (if (or (identical? h-a h-b) (= h-a h-b)) dom
         (cond
           (and (fragment? h-a) (fragment? h-b))
           (patch-children h-a h-b (if (vector? dom) (.-parentNode (first dom)) (.-parentNode dom)) rs)
           (and (text-like? h-a) (text-like? h-b) (= (.-nodeType dom) 3))
           (do (when-not (= (str h-a) (str h-b)) (set! (.-data dom) (str h-b))) dom)
           (or (not (vector? h-a)) (not (vector? h-b)) (not= (get-type h-a) (get-type h-b)))
           (let [p (if (vector? dom) (.-parentNode (first dom)) (.-parentNode dom))]
             (run! unmount-node-and-children (if (vector? dom) dom [dom]))
             (hiccup->dom h-b (dom->namespace p) rs))
           :else
           (do (patch-attributes h-a h-b dom)
               (let [aa (get-attrs h-a) ab (get-attrs h-b)
                     ah (get-in aa [:dangerouslySetInnerHTML :__html])
                     bh (get-in ab [:dangerouslySetInnerHTML :__html])]
                 (cond (some? bh) (when-not (= ah bh) (set! (.-innerHTML dom) bh))
                       (some? ah) (do (set! (.-innerHTML dom) "") (patch-children h-a h-b dom rs))
                       :else (patch-children h-a h-b dom rs)))
               (let [or (:ref (get-attrs h-a)) nr (:ref (get-attrs h-b)) sr (aget dom "---ref-fn")]
                 (when (if pref (and (not (identical? sr nr)) (identical? sr or)) (not (= or nr)))
                   (call-ref-cleanup! dom) (when nr (queue-ref-mount! rs nr dom)) (aset dom "---ref-fn" nr)))
               (let [aa (get-attrs h-a) ab (get-attrs h-b) bv (:value ab)]
                 (when (and (contains? ab :value) (not (= (:value aa) bv)))
                   (if (and (= "SELECT" (.-tagName dom)) (.-multiple dom))
                     (let [vs (set bv)] (doseq [o (.-options dom)] (aset o "selected" (contains? vs (.-value o)))))
                     (let [tn (.-tagName dom) is-i (or (= "INPUT" tn) (= "TEXTAREA" tn))
                           is-a (identical? dom (.-activeElement js/document))]
                       (if (and is-i is-a)
                         (let [s (.-selectionStart dom) e (.-selectionEnd dom)]
                           (aset dom "value" bv) (set! (.-selectionStart dom) s) (set! (.-selectionEnd dom) e))
                         (aset dom "value" bv))))))
               dom)))))))

(defn- modify-dom [runtime normalized-component]
  (if (contains? (:rendering-components @runtime) normalized-component)
    (when *watcher* (queue-watcher! *watcher*))
    (try
      (swap! runtime update :rendering-components (fnil conj #{}) normalized-component)
      (when-let [{:keys [hiccup dom container base-namespace]} (runtime-mounted-info runtime normalized-component)]
        (if-not (and container (or (identical? container js/document.body) (.-isConnected container)))
          (do (remove-watchers-for-component runtime normalized-component)
              (swap! runtime update :mounted-components dissoc normalized-component))
          (let [rs (create-render-state {:normalized-component normalized-component :container container
                                         :base-namespace (or base-namespace (dom->namespace container))
                                         :runtime runtime})]
            (remove-watchers-for-component runtime normalized-component)
            (try
              (reset-positional-counter! rs)
              (let [new-h (fully-render-hiccup (with-watcher-bound normalized-component rs #(component->hiccup normalized-component rs)) rs)]
                (reset-positional-counter! rs)
                (assoc-runtime-mounted-info!
                  runtime normalized-component
                  {:hiccup new-h :container container :runtime runtime :base-namespace (:base-namespace @rs)
                   :dom (if (fragment? hiccup) (do (patch-children hiccup new-h container rs) nil)
                          (let [new-dom (patch hiccup new-h dom rs)]
                            (when-not (identical? dom new-dom) (set! (.-innerHTML container) "") (.appendChild container new-dom))
                            new-dom))}))
              (finally (swap! rs assoc :active false))))))
      (finally (flush-ref-queue! runtime) (swap! runtime update :rendering-components disj normalized-component)))))

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
      (let [hiccup (component->hiccup normalized-component render-state)
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
          [hiccup dom] (add-modify-dom-watcher-on-ratom-deref normalized-component render-state)
          _ (reset-positional-counter! render-state)
          h-rendered (fully-render-hiccup hiccup render-state)]
      (.appendChild container dom)
      (update-mounted-info! runtime normalized-component h-rendered (when-not (fragment? h-rendered) dom) container render-state)
      (when container (swap! roots assoc container {:container container :component normalized-component :runtime runtime})))
    (finally (swap! render-state assoc :active false))))

; mirrored as atom below. A deftype implementing the squint ref protocols:
; deref registers the active watcher, reset!/swap! notify watchers and
; cursors. add-watch etc delegate to the backing atom.
(deftype RAtom [base watchers cursors]
  IDeref
  (-deref [this]
    (ensure-watcher-registered! this watchers)
    @base)
  IReset
  (-reset! [_this new-val]
    (let [res (reset! base new-val)]
      (notify-watchers watchers)
      (doseq [c @cursors]
        (notify-watchers (aget c "watchers")))
      res))
  ISwap
  (-swap! [this f] (-reset! this (f @base)))
  (-swap! [this f a] (-reset! this (f @base a)))
  (-swap! [this f a b] (-reset! this (f @base a b)))
  (-swap! [this f a b xs] (-reset! this (apply f @base a b xs)))
  IWatchable
  (-add-watch [this k f]
    (add-watch base k (fn [k _ o n] (f k this o n)))
    this)
  (-remove-watch [_this k]
    (remove-watch base k))
  (-notify-watches [this _oldv _newv]
    (notify-watchers watchers)
    this))

(defn- ratom [initial-value]
  (->RAtom (core-atom initial-value) (core-atom (empty-js-map)) (core-atom #{})))

; *** Reagent API functions *** ;

(deftype Cursor [the-ratom path watchers]
  IDeref
  (-deref [this]
    (ensure-watcher-registered! this watchers)
    (let [old-watcher *watcher*]
      (try
        (set! *watcher* nil)
        (get-in @the-ratom path)
        (finally
          (set! *watcher* old-watcher)))))
  ISwap
  (-swap! [_this f]
    (swap! the-ratom
           (fn [state] (assoc-in state path (f (get-in state path))))))
  (-swap! [_this f a]
    (swap! the-ratom
           (fn [state] (assoc-in state path (f (get-in state path) a)))))
  (-swap! [_this f a b]
    (swap! the-ratom
           (fn [state] (assoc-in state path (f (get-in state path) a b)))))
  (-swap! [_this f a b xs]
    (swap! the-ratom
           (fn [state] (assoc-in state path (apply f (get-in state path) a b xs)))))
  IReset
  (-reset! [this new-val] (-swap! this (constantly new-val))))

;; Reagent API
(defn cursor [the-ratom path]
  (let [cursors (.-cursors the-ratom)
        found-cursor (some (fn [c] (when (= path (aget c "path")) c)) @cursors)]
    (if (nil? found-cursor)
      (let [this-cursor (->Cursor the-ratom path (core-atom (empty-js-map)))]
        (swap! cursors conj this-cursor)
        this-cursor)
      found-cursor)))

(deftype Reaction [ra]
  IDeref
  (-deref [_this] @ra)
  ISwap
  (-swap! [_this _f] (throw (js/Error. "Reactions are readonly")))
  IReset
  (-reset! [_this _v] (throw (js/Error. "Reactions are readonly"))))

;; Reagent API
(defn reaction [f & params]
  (let [ra (ratom nil)
        watcher #(reset! ra (apply f params))
        old-watcher *watcher*]
    (try
      (set! *watcher* watcher)
      (watcher)
      (->Reaction ra)
      (finally
        (set! *watcher* old-watcher)))))

(defn- render-into-container [component container runtime old-root-info]
  (let [rs (create-render-state {:container container :runtime runtime :base-namespace (dom->namespace container)})
        new-comp (normalize-component component rs)]
    (if-let [mounted (and old-root-info (runtime-mounted-info runtime (:component old-root-info)))]
      (let [{:keys [hiccup dom]} mounted old-comp (:component old-root-info)]
        (remove-watchers-for-component runtime old-comp)
        (if (and (not (fragment? hiccup)) dom (not (.-parentNode dom)))
          (do-render new-comp container rs)
          (do (reset-positional-counter! rs)
              (let [new-h (fully-render-hiccup (with-watcher-bound new-comp rs #(component->hiccup new-comp rs)) rs)]
                (reset-positional-counter! rs)
                (if (fragment? hiccup)
                  (do (patch-children hiccup new-h container rs) (update-mounted-info! runtime new-comp new-h nil container rs))
                  (let [new-dom (patch hiccup new-h dom rs)]
                    (update-mounted-info! runtime new-comp new-h new-dom container rs)
                    (when-not (identical? dom new-dom) (.replaceWith dom new-dom) (swap! runtime assoc :component-instances (empty-js-map)))))
                (swap! roots assoc container (assoc old-root-info :component new-comp))
                (when-not (identical? old-comp new-comp) (swap! runtime update :mounted-components dissoc old-comp))
                (flush-ref-queue! runtime)))))
      (do (swap! rs assoc :normalized-component new-comp) (do-render new-comp container rs) (flush-ref-queue! runtime)))))

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

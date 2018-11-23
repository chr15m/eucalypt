(ns stigmergy.mr-clean
  (:refer-clojure :exclude [atom])
  (:require [stigmergy.ikota :as ik]
            [clojure.core.async :as async]
            [goog.dom :as gdom]
            [clojure.walk :as w])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defonce ^:dynamic *watcher* nil)
(defonce render-queue (async/chan 10))  

(defonce create-class identity)
(defonce dom-node identity)

(defonce mounted-components (clojure.core/atom {}))
(defonce container->mounted-component (clojure.core/atom {}))

(extend-type js/NodeList
  ISeqable
  (-seq [nodeList] (vec (array-seq nodeList))))

(extend-type js/NamedNodeMap
  ISeqable
  (-seq [named-node-map] (vec (array-seq named-node-map))))

(defn- rm-fn [hiccup]
  (w/postwalk #(if (fn? %)
                 nil
                 %)
              hiccup))

(defn hiccup-eq? [hiccup-a hiccup-b]
  (or (= hiccup-a hiccup-b)
      (let [hiccup-a (rm-fn hiccup-a)
            hiccup-b (rm-fn hiccup-b)]
        (= hiccup-a hiccup-b))))

(defn component? [c]
  (and (sequential? c)
       (let [first-element (first c)]
         (or (fn? first-element)
             (map? first-element)))))

(defn- component-node->hiccup [node]
  (if (component? node)
    (let [render-fn (let [first-element (first node)]
                      (cond
                        (fn? first-element) first-element
                        (map? first-element) (:reagent-render first-element)))
          params (rest node)
          hiccup (apply render-fn params) ;;TODO: should check mounted-components first
          ]
      hiccup)
    node))

(defn component->hiccup [[{:keys [reagent-render]} & params :as normalized-component]]
  (let [hiccup (apply reagent-render params)
        hiccup (w/prewalk component-node->hiccup
                          hiccup)
        hiccup (ik/normalize-hiccup hiccup)]
    hiccup))

(defn- patch-children [hiccup-a hiccup-b dom-a]
  (let [index-hiccup-a (->> hiccup-a
                            (map-indexed (fn [index item] [index item]))
                            rest rest)
        index-hiccup-b (->> hiccup-b
                            (map-indexed (fn [index item] [index item]))
                            rest rest)
        dom-a-child-nodes (seq (.. dom-a -childNodes))]
    (doseq [[index b] index-hiccup-b]
      (if (< index (count hiccup-a))
        (let [a (hiccup-a index)]
          (when (not= a b)
            (when-not (and (sequential? a)
                           (sequential? b)
                           (= :input (first a))
                           (= :input (first b)))
              (let [child-index (- index 2)
                    a-dom-node (dom-a-child-nodes child-index)
                    b-dom-node (ik/hiccup->dom b)]
                (gdom/replaceNode b-dom-node a-dom-node)))))
        (let [b-dom-node (ik/hiccup->dom b)]
          (gdom/appendChild dom-a b-dom-node))))
    (doseq [[index a] index-hiccup-a
            :let [child-index (- index 2)
                  a-dom-node (dom-a-child-nodes child-index)]]
      (if (< index (count hiccup-b))
        (let [b (hiccup-b index)]
          (when (not= a b)
            (when-not (and (sequential? a)
                           (sequential? b)
                           (= :input (first a))
                           (= :input (first b)))
              (gdom/removeNode a-dom-node))))
        (gdom/removeNode a-dom-node)))))

(defn- patch-attributes [hiccup-a hiccup-b dom-a]
  (let [b-attr-map (second hiccup-b)
        a-attr-map (second hiccup-a)]
    (doseq [[a-attr-kw a-value] a-attr-map
            :let [b-value (b-attr-map a-attr-kw)]
            :when (not= a-value b-value)]
      (.. dom-a (removeAttribute (name a-attr-kw))))

    (doseq [[b-attr-kw b-value] b-attr-map
            :let [a-value (a-attr-map b-attr-kw)
                  b-attr-value (if (= b-attr-kw :style)
                                 (ik/style-map->css-str b-value)
                                 b-value)]
            :when (not= a-value b-value)]
      (.. dom-a (setAttribute (name b-attr-kw) b-attr-value)))))

(defn patch
  "patch dom-a with the diff of hiccup-a and hiccup-b transforming dom-a the dom representation of hiccup-b.
  if hiccup-a and hiccup-b are not the same element type, then a new dom element is created from hiccup-b."
  [hiccup-a hiccup-b dom-a]
  (let [tag-a (first hiccup-a)
        tag-b (first hiccup-b)
        different-tags? (not= tag-a tag-b)]
    (cond
      (hiccup-eq? hiccup-a hiccup-b) dom-a
      different-tags? (ik/hiccup->dom hiccup-b)
      :else (do
              (patch-children hiccup-a hiccup-b dom-a)
              (patch-attributes hiccup-a hiccup-b dom-a)
              dom-a))))

(defn modify-dom [normalized-component]
  (let [[{:keys [reagent-render]} & params] normalized-component
        {:keys [hiccup dom container]} (@mounted-components normalized-component)
        new-hiccup (component->hiccup normalized-component)
        new-dom (patch hiccup new-hiccup dom)] 
    (swap! mounted-components assoc normalized-component {:hiccup new-hiccup
                                                          :dom new-dom
                                                          :container container})
    (when (not= dom new-dom)
      (gdom/removeChildren container)
      (.. container (appendChild new-dom)))))

(defn notify-watchers [watchers]
  (doseq [watcher @watchers]
    (watcher)))

(deftype RAtom [^:mutable value watchers cursors]
  IAtom

  IDeref
  (-deref [this]
    (when *watcher*
      (let [cursor-watcher (some->> @cursors
                                    (filter (fn [c]
                                              (let [watchers (.-watchers c)]
                                                (contains? @watchers *watcher*))))
                                    first)]

        (when (nil? cursor-watcher)
          (swap! watchers conj *watcher*))))
    value)

  IReset
  (-reset! [this new-value]
    (prn "ratom reset " (not= value new-value))
    (when (not= value new-value)
      (set! value new-value)
      (notify-watchers watchers))
    this)

  ISwap
  (-swap! [this f] (let [new-value (f value)]
                     (-reset! this new-value)))
  (-swap! [this f x] (let [new-value (f value x)]
                       (-reset! this new-value)))
  (-swap! [this f x y] (let [new-value (f value x y)]
                         (-reset! this new-value)))
  (-swap! [this f x y more] (let [new-value (apply f value x y more)]
                              (-reset! this new-value))))

(deftype Cursor [the-atom path watchers]
  IAtom

  IDeref
  (-deref [this]
    (when *watcher*
      (let [ratom-watcher (some->> the-atom
                                   .-watchers
                                   deref
                                   (filter (fn [watcher]
                                             (= watcher *watcher*)))
                                   first)]
        (when (nil? ratom-watcher)
          (swap! watchers conj *watcher*))))

    (let [state @the-atom
          value (get-in  state path)]
      value))

  IReset
  (-reset! [this new-value]
    (let [value (get-in @the-atom path)]
      (when (not= value new-value)
        (swap! the-atom assoc-in path new-value)
        (notify-watchers watchers))))

  ISwap
  (-swap! [a f] (-reset! a (let [current-value (get-in @the-atom path)]
                             (f current-value))))
  (-swap! [a f x] (-reset! a (let [current-value (get-in @the-atom path)]
                               (f current-value x) )))
  (-swap! [a f x y] (-reset! a (let [current-value (get-in @the-atom path)]
                                 (f current-value x y))))
  (-swap! [a f x y more] (-reset! a (let [current-value (get-in @the-atom path)]
                                      (apply f current-value x y more)))))

(defn throw-read-only []
  (throw (js/Error. "Reactions are readonly")))

(deftype Reaction [ratom]
  IAtom

  IDeref
  (-deref [this]
    @ratom)

  IReset
  (-reset! [this new-value]
    (throw-read-only))

  ISwap
  (-swap! [a f] (throw-read-only)))

(defn- add-modify-dom-watcher-on-ratom-deref
  "This is where the magic of adding watchers to ratoms happen automatically.
  This is achieved by setting the dnymaic var *watcher* then evaluating reagent-render
  which causes the deref of the ratom to trigger adding the watcher to (.-watchers ratom)"
  [normalized-component]
  (binding [*watcher* (with-meta #(modify-dom normalized-component)
                        {:normalized-component normalized-component})]
    (let [reagent-render (-> normalized-component first :reagent-render)
          params (rest normalized-component)
          hiccup (apply reagent-render params)
          hiccup (w/prewalk component-node->hiccup
                            hiccup)
          hiccup (ik/normalize-hiccup hiccup)]
      hiccup)))

(defn atom [state]
  (let [watchers (clojure.core/atom #{})
        cursors (clojure.core/atom #{})]
    (RAtom. state watchers cursors)))

(defn cursor [the-atom path]
  (let [cursors (.-cursors the-atom)
        found-cursor (some->> @cursors
                              (filter (fn [c]
                                        (= path (.-path c))))
                              first)]
    (if (nil? found-cursor)
      (let [watchers (clojure.core/atom #{})
            this-cursor (->Cursor the-atom path watchers)]
        (swap! cursors conj this-cursor)
        this-cursor)
      found-cursor)))

(defn reaction [f & params]
  (let [ra (atom nil)
        watcher #(reset! ra (apply f params))]
    (binding [*watcher* watcher]
      (watcher)
      (Reaction. ra))))

(defn- rm-watchers [normalized-component]
  (let [params (rest normalized-component)]
    (doseq [p params
            :when (satisfies? IAtom p)
            :let [watchers (.-watchers p)]]
      (doseq [w @watchers
              :when (= (-> w meta :normalized-component)
                       normalized-component)]
        (swap! watchers (fn [watchers]
                          (set (remove #(= w %) watchers))))))))


(defonce life-cycle-methods {:get-initial-state (fn [this])
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
                                      comp-with-lifecycle (assoc life-cycle-methods
                                                                 :reagent-render render-fn)]
                                  (into [comp-with-lifecycle] params))
            (keyword? first-element) (into [(assoc life-cycle-methods :reagent-render (fn [] component))]
                                           params)
            (map? first-element) (let [component-as-map first-element
                                       render-fn (:reagent-render component-as-map)
                                       comp-with-lifecycle (into {:reagent-render render-fn}
                                                                 (for [[k func] life-cycle-methods
                                                                       :let [func2 (k component-as-map)
                                                                             func-func2 (comp func2 func)]]
                                                                   [k func-func2]))]
                                   (into [comp-with-lifecycle] params))))))

(defn unmount-components [container]
  (when-let [mounted-component (@container->mounted-component container)]
    (let [[{:keys [component-will-unmount]} & params] mounted-component]
      (component-will-unmount mounted-component)
      (swap! container->mounted-component dissoc container)))
  (gdom/removeChildren container))

(defn do-render [normalized-component container]
  (unmount-components container)

  (let [[{:keys [reagent-render component-will-mount component-did-mount]}
         & params] normalized-component
        hiccup (add-modify-dom-watcher-on-ratom-deref normalized-component)
        dom (ik/hiccup->dom hiccup)]
    (component-will-mount normalized-component)
    (.. container (appendChild dom))
    (component-did-mount normalized-component)
    (swap! mounted-components assoc normalized-component {:hiccup hiccup
                                                          :dom dom
                                                          :container container})
    (swap! container->mounted-component assoc container normalized-component)))

(go-loop []
  (let [ [normalized-component container] (<! render-queue)]
    (do-render normalized-component container))
  (recur))

(defn render [component container]
  (let [normalized-component (normalize-component component)]
    (go (>! render-queue [normalized-component container]))))

(defn init []
  (prn "mr-clean init"))

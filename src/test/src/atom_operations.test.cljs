(ns atom-operations.test
  (:require ["vitest" :refer [describe it afterEach]]
            [eucalypt :as r]
            [helpers :as th]))

(afterEach
  (fn []
    (set! (.-innerHTML js/document.body) "")))

(describe "Atom operations"
  (fn []
    (it "reset! works"
      (fn []
        (let [a (r/atom {})]
          (reset! a {:a 1})
          (th/assert-equal @a {:a 1})
          (reset! a {:b 2})
          (th/assert-equal @a {:b 2})
          (reset! a nil)
          (th/assert-equal @a nil))))

    (it "swap! with assoc works"
      (fn []
        (let [a (r/atom nil)]
          (swap! a assoc :a 1)
          (th/assert-equal @a {:a 1}))
        (let [a (r/atom {:a 1})]
          (swap! a assoc :b 2)
          (th/assert-equal @a {:a 1 :b 2})
          (swap! a assoc :a 2)
          (th/assert-equal @a {:a 2 :b 2}))))

    (it "swap! with assoc-in works"
      (fn []
        #_(let [a (r/atom nil)]
            (swap! a assoc-in [:a :b] 1)
            (th/assert-equal @a {:a {:b 1}}))
        (let [a (r/atom {:a {:b 1}})]
          (swap! a assoc-in [:c :d] 1)
          (th/assert-equal @a {:a {:b 1} :c {:d 1}})
          (swap! a assoc-in [:a :b] 2)
          (th/assert-equal @a {:a {:b 2} :c {:d 1}}))))

    (it "swap! with update works"
      (fn []
        (let [a (r/atom nil)]
          (swap! a update :counter (fnil inc 0))
          (th/assert-equal @a {:counter 1}))
        (let [a (r/atom {:other-key "value"})]
          (swap! a update :counter (fnil inc 0))
          (th/assert-equal @a {:other-key "value" :counter 1}))
        (let [a (r/atom {:counter 5})]
          (swap! a update :counter inc)
          (th/assert-equal @a {:counter 6}))))

    (it "swap! with update-in works"
      (fn []
        #_(let [a (r/atom nil)]
            (swap! a update-in [:a :b] (fnil inc 0))
            (th/assert-equal @a {:a {:b 1}}))
        (let [a (r/atom {:a {:c 2}})]
          (swap! a update-in [:a :b] (fnil inc 0))
          (th/assert-equal @a {:a {:c 2 :b 1}}))
        (let [a (r/atom {:a {:b 5}})]
          (swap! a update-in [:a :b] inc)
          (th/assert-equal @a {:a {:b 6}}))))

    (it "swap! with conj works"
      (fn []
        (let [a (r/atom nil)]
          (swap! a conj {:a 1})
          (th/assert-equal (vec @a) [{:a 1}]))
        (let [a (r/atom [])]
          (swap! a conj 1)
          (th/assert-equal @a [1]))
        (let [a (r/atom [1 2])]
          (swap! a conj 3)
          (th/assert-equal @a [1 2 3]))))

    (it "swap! with dissoc works"
      (fn []
        #_(let [a (r/atom nil)]
            (swap! a dissoc :a)
            (th/assert-equal @a nil))
        (let [a (r/atom {:a 1})]
          (swap! a dissoc :b)
          (th/assert-equal @a {:a 1}))
        (let [a (r/atom {:a 1 :b 2})]
          (swap! a dissoc :a)
          (th/assert-equal @a {:b 2}))))

    (it "swap! with update-in and conj on nil"
      (fn []
        (let [a (r/atom {:a {}})]
          (swap! a update-in [:a :items] (fnil conj []) "new-item")
          (th/assert-equal (-> @a :a :items) ["new-item"]))
        (let [a (r/atom {})]
          (swap! a update-in [:a :items] (fnil conj []) "new-item")
          (th/assert-equal (-> @a :a :items) ["new-item"]))))

    (it "swap! with update-in and assoc on nil"
      (fn []
        (let [a (r/atom {})]
          (swap! a update-in [:a] (fnil assoc {}) :b 1)
          (th/assert-equal @a {:a {:b 1}}))))))

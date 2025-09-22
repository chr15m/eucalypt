(ns index
  (:require
    [eucalypt :as r]
    ;["../README.md?raw" :as readme]
    #_ [clojure.string :as string]))

(defonce state (r/atom {}))

(defn get-page-size []
  (-> (js/fetch (.-href js/location))
      (.then (fn [response]
               (let [headers (.-headers response)
                     is-gzipped (= (.get headers "content-encoding") "gzip")
                     content-length (.get headers "content-length")]
                 (js/console.log is-gzipped content-length)
                 (if (and is-gzipped content-length)
                   (let [size-kb (/ (js/parseInt content-length) 1024)]
                     (swap! state assoc :page-size (str (.toFixed size-kb 1) "kb gzipped")))
                   (-> (.text response)
                       (.then (fn [text]
                                (let [size-kb (/ (.-length text) 1024)]
                                  (swap! state assoc :page-size (str (.toFixed size-kb 1) "kb"))))))))))
      (.catch (fn [error]
                (js/console.error "Error fetching page size:" error)
                (swap! state assoc :page-size "Error")))))

#_ (defn md [content]
  (when content
    (->> (.split content "\n")
         (remove #(or
                    ; h1 headers
                    (re-find #"^# " %)
                    ; images
                    (re-find #"!\[.*\]\(.*\)" %)))
         (map (fn [line]
                (let [line-with-links (string/replace line #"\[(.*?)\]\((.*?)\)" "<a href=\"$2\">$1</a>")]
                  (cond
                    ; Convert h2 headers to hiccup
                    (re-find #"^## " line)
                    (str "<h2>" (string/replace line #"^## " "") "</h2>")

                    ; Convert list items (lines starting with dash)
                    (re-find #"^- " line)
                    (str line-with-links "</br>\n")

                    ; Lines with links but not starting with dash
                    (re-find #"\[.*?\]\(.*?\)" line)
                    line-with-links

                    :else
                    (str "<p>" line "</p>")))))
         (string/join "\n"))))

(defn component:main [state]
  [:main
   [:p "The size of this index.html file is: "
    [:strong (or (:page-size @state)
                 "loading...")]]
   #_ [:div
    {:ref #(when %
             (->> readme .-default md
                  (aset % "innerHTML")))}]])

(r/render
  [component:main state]
  (js/document.getElementById "app"))

(get-page-size)

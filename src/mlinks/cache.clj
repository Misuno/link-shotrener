(ns mlinks.cache
  (:require [mlinks.config :as c]))

(def ^:private cache (atom (array-map)))

(defn clear-links!
  "Clears links map"
  []
  (reset! cache (array-map)))

(defn add-to-cache!
  "Takes a context and a link, adds link to cache, returns link"
  [ctx link]
  (swap! cache #(conj % [(:short link) link]))
  (when (> (count @cache) (c/buffer-size ctx))
    (swap! cache next))
  link)

(defn get-from-cache!
  [ctx short]
  (let [link (get @cache short)]
    (swap! cache dissoc short)
    (add-to-cache! ctx link)))

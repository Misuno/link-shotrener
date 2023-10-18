(ns test-bot.cache
  (:require [test-bot.config :as c]))

(def ^:private cache (atom (array-map)))

(defn clear-links!
  "Clears links map"
  []
  (reset! cache {}))

(defn add-to-cache!
  [ctx short long]
  (swap! cache #(conj % [short long]))
  (when (> (count @cache) (c/buffer-size ctx))
    (swap! cache next)))

(defn get-from-cache!
  [ctx short]
  (let [ll (get @cache short)]
    (swap! cache dissoc short)
    (add-to-cache! ctx short ll)
    ll))


(ns test-bot.cache
  (:require [test-bot.config :as c]))

(def ^:private cache (atom {}))
(def ^:private cache-keys (atom []))

(defn clear-links!
  "Clears links map"
  []
  (reset! cache {}))

(defn add-to-cache!
  [long short]
  (swap! cache #(assoc % short long))
  (swap! cache-keys #(conj % short))
  (when (> (count @cache-keys) (c/buffer-size))
    (swap! cache #(dissoc % (first @cache-keys)))
    (swap! cache-keys #(vec (rest %)))))

(defn get-from-cache
  ([short] (get-from-cache short #()))
  ([short default] (get @cache short default)))
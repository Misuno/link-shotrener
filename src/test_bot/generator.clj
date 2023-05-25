(ns test-bot.generator
  (:require [nano-id.core :refer [nano-id]]
            [test-bot.filedbcontroller :as db]
            [test-bot.config :as c]))

(def cache (atom {}))
(def cache-keys (atom []))

(defn clear-links!
  "Clears links map"
  []
  (reset! cache {}))

(defn add-to-cache!
  [long short]
  (swap! cache #(assoc % short long))
  (swap! cache-keys #(conj % short))
  (if (> (count @cache-keys) (c/buffer-size))
    (do (swap! cache #(dissoc % (first @cache-keys)))
        (swap! cache-keys #(vec (rest %))))))

(defn save-link
  [id long short]
  (db/save-to-db! id long short)
  (add-to-cache! long short)
  short)

(defn get-long-link!
  [short]
  (let [l (get @cache short)]
    (if l
      l
      (let [ll (db/get-from-db! short)]
              (if ll
                (do (add-to-cache! ll short)
                    ll)
                "No link")))))

(defn make-link []
  (nano-id 7))

(defn link-generator!
  [id link]
  (->> (make-link)
       (save-link id link)
       (str (:base-uri c/config) "/")))

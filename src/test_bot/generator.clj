(ns test-bot.generator
  (:require [nano-id.core :refer [nano-id]]
            [test-bot.filedbcontroller :as db]))

(def domain-name
  (or (System/getenv "BASE_IRI") "http://localhost"))

(def cache-size 500)
(def cache (atom {}))

(defn clear-links!
  "Clears links map"
  []
  (reset! cache {}))

(defn add-to-cache!
  [long short]
  (swap! cache #(assoc % short long))
  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (> (count @cache) cache-size)
    (swap! cache #(dissoc % (first (keys %))))))

(defn save-link
  [id long short]
  (db/save-to-db! id long short)
  (add-to-cache! long short)
  short)

(defn get-long-link!
  [short]
  (get @cache (keyword short)
       (let [ll (db/get-from-db! short)]
         (if ll
           (do (add-to-cache! ll short)
               ll)
           false))))

(defn make-link []
  (nano-id 7))

(defn link-generator!
  [id link]
  (->> (make-link)
       (save-link id link)
       (str domain-name "/")))



(ns test-bot.generator
  (:require [nano-id.core :refer [nano-id]]
            [test-bot.dbcontroller :refer [get-from-db! save-to-db!]]))

(def links (atom {}))

(defn clear-links!
  "Clears links map"
  []
  (reset! links {}))


(def domain-name
  (or (System/getenv "BASE_IRI") "http://localhost"))

(defn save-link-locally!
  [long short]
  (swap! links #(assoc % short long)))

(defn save-link
  [id long short]
  (save-to-db! id long short)
  (save-link-locally! long short)
  short)

(defn get-long-link!
  [short]
  (get @links (keyword short)
       (let [ll (get-from-db! short)]
         (if ll
           (do (save-link-locally! ll short)
               ll)
           false))))

(defn make-link []
  (nano-id 7))

(defn link-generator!
  [id link]
  (->> (make-link)
       (save-link id link)
       (str domain-name)))



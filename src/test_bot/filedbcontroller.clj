(ns test-bot.filedbcontroller
  (:require [clojure.string :as s]))

(def filename "resources/urls.csv")

(defn save-click! [sl data])

(defn save-to-db!
  [id ll sl]
  (spit filename (str (s/join "," [sl ll id]) "\n") :append true))

(defn- split
  [str]
  (map #(s/split % #",") (s/split-lines str)))

(defn- read-split! [f]
  (split (slurp f)))

(defn- is-needed-link?
  [sl list]
  (= sl (first list)))

(defn get-from-db!
  [sl]
  (let [[_ ll]
        (->> (split (slurp filename))
             (filter #(is-needed-link? sl %))
             (first))]
    ll))

(defn get-all-links!
  [id]
  (->> (read-split! filename)
       (filter (fn [[_ _ chatid]]
                 (= (str id) chatid)))))

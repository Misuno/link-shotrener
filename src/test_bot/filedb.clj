(ns test-bot.filedb
  (:require [clojure.string :as s]
            [test-bot.config :as c]))

(defn save-click! [& args])

(defn save-to-db!
  [ctx id ll sl]
  (spit (c/db-url ctx) (str (s/join "," [sl ll id]) "\n") :append true))

(defn- split
  [str]
  (map #(s/split % #",") (s/split-lines str)))

(defn- read-split! [f]
  (split (slurp f)))

(defn- is-needed-link?
  [sl list]
  (= sl (first list)))

(defn get-from-db!
  [ctx sl]
  (let [[_ ll]
        (->> (split (slurp (c/db-url ctx)))
             (filter #(is-needed-link? sl %))
             (first))]
    ll))

(defn get-all-links!
  [ctx id]
  (->> (read-split! (c/db-url ctx))
       (filter (fn [[_ _ chatid]]
                 (= (str id) chatid)))))

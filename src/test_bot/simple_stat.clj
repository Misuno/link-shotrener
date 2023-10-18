(ns test-bot.simple-stat
  (:require [test-bot.simple-stat :as sut]
            [clojure.test :as t]))

(def stats (atom {}))

(defn save-click! [sl]
  (let [kw (keyword sl)
        old (kw @stats 0)]
    (swap! stats assoc kw (inc old))))

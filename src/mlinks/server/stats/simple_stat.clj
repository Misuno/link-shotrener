(ns mlinks.server.stats.simple-stat)

(def stats (atom {}))

(defn save-click! [sl]
  (let [kw (keyword sl)
        old (get @stats kw 0)]
    (swap! stats assoc kw (inc old))))

(defn stat-for-link [link]
  -1)

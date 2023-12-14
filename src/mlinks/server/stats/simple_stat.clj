(ns mlinks.server.stats.simple-stat
  (:require
   [clojure.core.async :refer [go-loop timeout chan alt! close!]]
   [mlinks.server.database.dbcontroller :as dbc]
   [mlinks.utils :refer [log]]))

(def stats (atom {}))

(def stop-saving-ch (atom (chan)))

(defn save-click! [link]
  (let [id (:id link)]
    (->> (get @stats id 0)
         inc
         (swap! stats assoc id))))

(defn stat-for-link [link]
  (if-let [res (->> link
                    :id
                    (get @stats))]
    res
    0))

(defn- write-stats! [ctx stats]
  (when (> (count (keys @stats)) 0)
    (dosync (try
              (dbc/write-simple-stat! ctx @stats)
              (reset! stats {})
              (catch Exception e (throw e))))))

(defn- save-stat!
  "Saves stat array once a period time. Returns close channel."
  [ctx period stats]
  (let [stop-ch (chan)]
    (go-loop [t (timeout period)]
      (alt!
        t (do 
            (write-stats! ctx stats)
            (recur (timeout period)))
        stop-ch nil))
    stop-ch))

(defn start-saving!
  [ctx time]
  ;; (when-not (nil? stop-saving-ch) (close! @stop-saving-ch))
  (->> (save-stat! ctx time stats)
       (reset! stop-saving-ch)))

(comment
  (do
    (def context (atom {}))
    (require '[mlinks.config :as c])
    (swap! context conj {:config (c/read-config!)})

    (dbc/init-db! context))

  (dbc/write-simple-stat! context @stats)

  (start-saving! context 3000)

  (close! @stop-saving-ch)
  ;
  )

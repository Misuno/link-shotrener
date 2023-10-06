(ns test-bot.stats
  (:require [test-bot.dbcontroller :as db]
            [jsonista.core :as j]
            [test-bot.config :as c]
            ;; [clojure.core.async :refer [go]]
            [test-bot.utils :refer [log]]))

(def buf-size 300)
(def buffer (atom []))

(defn write-buffer! []
  (log "Clearing buffer!")
  (->> (reset-vals! buffer [])
       (first)
       (db/save-click!))
  (log "buffer is " @buffer))


(defn save-click
  [sl data]
  (when (c/stat-enabled?)
    (swap! buffer
           conj
           {:short_link sl
            :data (j/write-value-as-string data)})
    (when (<= buf-size (count @buffer))
      (write-buffer!))))

(comment
  (c/read-config!)
  (save-click "lala" ";a;a;")
  (c/stat-enabled?))

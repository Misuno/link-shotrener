(ns mlinks.server.stats.rich-stat
  (:require [mlinks.server.database.dbcontroller :as db]
            [jsonista.core :as j]
            [mlinks.utils :refer [log]]))

(def buf-size 300)
(def buffer (atom []))
(def clean-time 5000)
(def f (atom nil))

(defn write-buffer! [ctx]
  (log ctx "Clearing buffer!")
  (->> (reset-vals! buffer [])
       (first)
       (db/save-click! ctx))
  (log ctx "buffer is " @buffer))


(defn save-click!
  [ctx sl data]
  (future-cancel f)
  (swap! buffer
         conj
         {:short_link sl
          :data (j/write-value-as-string data)})
  (if (<= buf-size (count @buffer))
    (write-buffer! ctx)
    (reset! f (future (Thread/sleep clean-time)
                      (write-buffer! ctx)))))



(ns test-bot.stats
  (:require [test-bot.dbcontroller :as db]
            [jsonista.core :as j]
            [test-bot.config :as c]
            ;; [clojure.core.async :refer [go]]
            [test-bot.utils :refer [log]]))

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
  (when (c/stat-enabled? ctx)
    (future-cancel f)
    (swap! buffer
           conj
           {:short_link sl
            :data (j/write-value-as-string data)})
    (if (<= buf-size (count @buffer))
      (write-buffer! ctx)
      (reset! f (future (Thread/sleep clean-time)
                     (write-buffer! ctx))))))

(comment
  (require '[test-bot.config :as c])
  (c/read-config!)
  (save-click! "lala" ";a;a;")
  @buffer
  (c/stat-enabled?)

  (swap! buffer into (range 10))

  (defn atom-test [atm]
    (reset! atm []))

  (atom-test buffer)
;;
  )

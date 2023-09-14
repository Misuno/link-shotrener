(ns test-bot.stats
  (:require [test-bot.dbcontroller :as db]
            [jsonista.core :as j]
            [test-bot.config :as c]))

(defn save-click
  [sl data]
  (when (c/stat-enabled?)
    (db/save-click! sl (j/write-value-as-string data))))

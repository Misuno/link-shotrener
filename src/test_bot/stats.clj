(ns test-bot.stats
  (:require [test-bot.filedbcontroller :as db]
            [jsonista.core :as j]))

(defn save-click
  [sl data]
  (db/save-click! sl (j/write-value-as-string data)))
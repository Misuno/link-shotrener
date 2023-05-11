(ns test-bot.stats
  (:require [test-bot.dbcontroller :refer [save-click!]]
            [jsonista.core :as j]))

(defn save-click
  [sl data]
  (save-click! sl (j/write-value-as-string data)))

(comment
  (save-click "blavlavla" {:test "data"})
  :rcf)
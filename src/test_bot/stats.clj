(ns test-bot.stats
  (:require [test-bot.config :as c]
            [test-bot.simple-stat :as ss]
            [test-bot.rich-stat :as rs]
            [test-bot.utils :refer [log]]))

(defn save-click!
  [ctx sl data]
  (log ctx "save click" sl)
  (when (c/simple-stat-enabled? ctx)
    (ss/save-click! sl))
  (when (c/rich-stat-enabled? ctx)
    (rs/save-click! ctx sl data)))

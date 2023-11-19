(ns mlinks.server.stats.stats
  (:require [mlinks.config :as c]
            [mlinks.server.stats.simple-stat :as ss]
            [mlinks.server.stats.rich-stat :as rs]
            [mlinks.utils :refer [log]]))

(defn save-click!
  [ctx link data]
  (log ctx "save click" link)
  (when (c/simple-stat-enabled? ctx)
    (ss/save-click! link))
  (when (c/rich-stat-enabled? ctx)
    (rs/save-click! ctx link data)))

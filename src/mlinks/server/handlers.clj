(ns mlinks.server.handlers
  (:require [mlinks.utils :refer [log]]
            [mlinks.link :refer [get-long!]]
            [mlinks.server.stats.stats :refer [save-click!]]
            [ring.util.response :as r]))


(defn srv-handler [ctx {uri :uri :as request}]
  (log ctx "working on a uri" uri)
  (try
    (->> uri
         (get-long! ctx)
         (save-click! ctx request)
         :ll
         r/redirect)
    (catch Exception e (r/response (str e)))))

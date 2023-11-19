(ns mlinks.server.handlers
  (:require [mlinks.utils :refer [log]]
            [mlinks.link :refer [get-long!]]
            [mlinks.server.stats.stats :refer [save-click!]]
            [ring.util.response :as r]))

(defn link-found
  [ctx request link]
  (log ctx "link found" (:short link) "->" (:long link))
  (save-click! ctx link request)
  link)

(defn srv-handler [ctx {uri :uri :as request}]
  (log ctx "working on a uri" uri)
  (try
    (->> uri
         (get-long! ctx)
         (link-found ctx request)
         :long
         r/redirect)
    (catch Exception e (r/response (str e)))))

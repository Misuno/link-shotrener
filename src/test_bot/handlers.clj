(ns test-bot.handlers
  (:require [test-bot.utils :refer [log]]
            [test-bot.generator :refer [get-long-link! link-generator!]]
            [test-bot.stats :refer [save-click!]]
            [ring.util.response :as r]))

(defn link-found
  [ctx request sl ll]
  (log ctx "link found" sl "->" ll)
  (save-click! ctx sl request)
  ll)

(defn srv-handler [ctx {uri :uri :as request}]
  (log ctx "working on a uri" uri)
  (try
    (->> uri
         (get-long-link! ctx)
         (link-found ctx request uri)
         r/redirect)
    (catch Exception _ (r/response "No link!!! Fuck you!"))))

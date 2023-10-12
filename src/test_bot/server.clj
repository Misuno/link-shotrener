(ns test-bot.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as r]
            [test-bot.generator :refer [get-long-link!]]
            [test-bot.stats :refer [save-click]]
            [test-bot.config :as c]
            [test-bot.utils :refer [log]]))

(defn link-found!
  [sl ll request]
  (save-click sl request)
  (r/redirect ll))

(defn srv-handler [{uri :uri :as request}]
  (log "working on a uri" uri)
  (try
    (link-found! uri (get-long-link! uri) request)
    (catch Error e (r/response "No link!!!"))))

(defn run-server []
  (log "Starting server on port" (c/server-port!))
  (run-jetty srv-handler {:port (c/server-port!)
                          :join? false}))

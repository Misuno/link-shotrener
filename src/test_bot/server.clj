(ns test-bot.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as r]
            [test-bot.generator :refer [get-long-link!]]
            [test-bot.stats :refer [save-click!]]
            [test-bot.config :as c]
            [test-bot.utils :refer [log]]))

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

(defn run-server [ctx]
  (let [port (c/server-port ctx)]
    (log ctx "Starting server on port" port)
    (run-jetty (partial srv-handler ctx)
               {:port port
                :join? false})))

(comment

  
;;
  )

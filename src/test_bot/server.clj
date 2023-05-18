(ns test-bot.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as r]
            [test-bot.generator :refer [get-long-link!]]
            [test-bot.stats :refer [save-click]]
            [test-bot.config :as c]))

(defn link-found!
  [sl ll request]
  (save-click sl request)
  (r/redirect ll))

(defn srv-handler [{uri :uri :as request}]
  (let [l (subs uri 1)
        ll (get-long-link! l)]
    (prn "ll = " ll)
    (if ll
      (link-found! l ll request)
      (r/response "No link!!!"))))

(def server
  (run-jetty srv-handler {:port (c/server-port);; (:server-port @c/config)
                          :join? false}))

(comment
  (.stop server)
  :rcf)

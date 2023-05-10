(ns test-bot.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [redirect response]]
            [test-bot.generator :refer [get-long-link!]]))

(defn srv-handler [{uri :uri}]
  (let [l (subs uri 1)
        ll (get-long-link! l)]
    (if ll
      (redirect ll)
      (response "No link"))))

(defonce server
  (run-jetty srv-handler {:port 3000
                          :join? false}))

(comment 
   (.start server)
  (.stop server)
  :rcf)





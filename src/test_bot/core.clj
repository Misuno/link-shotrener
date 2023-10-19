(ns test-bot.core
  (:require [test-bot.server :refer [run-server]]
            [test-bot.telegram :refer [start-telegram!]]
            [test-bot.config :as c]
            [test-bot.utils :refer [log]]
            [test-bot.dbcontroller :as dbc])
  (:gen-class))

(def context (atom {}))

(defn -main
  [& _args]
  (try (swap! context conj {:config (c/read-config!)})
       (dbc/init-db! context)
       (run-server context)
       (catch Exception e
         (log context (str "Exiting because of "
                           (.getMessage e)))
         (System/exit 1)))
  ;; (start-telegram! context)
  )

(comment
  (def context (atom {}))

  (swap! context conj {:config (c/read-config!)})

  (dbc/init-db! context)

  (def srv (run-server context))

  (.stop srv)
  
 ;;
  )


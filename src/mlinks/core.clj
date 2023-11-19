(ns mlinks.core
  (:require [mlinks.server.server :refer [run-server]]
            [mlinks.telegram.telegram :refer [start-telegram!]]
            [mlinks.config :as c]
            [mlinks.utils :refer [log]]
            [mlinks.server.database.dbcontroller :as dbc]
            [morse.polling :as p])
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
  (start-telegram! context)
  )

(comment
  (do
    (def context (atom {}))

    (swap! context conj {:config (c/read-config!)})

    (dbc/init-db! context)

    (require '[mlinks.telegram.telegram :as tg])

    (require '[morse.polling :as p])

    (tg/create-handler context))

  (def running (p/start (c/token context) tg/handler))

  (p/stop running)

  (def srv (run-server context))

  (.stop srv)

 ;;
  )


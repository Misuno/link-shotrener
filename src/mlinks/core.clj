(ns mlinks.core
  (:require [mlinks.server.server :refer [run-server]]
            [mlinks.telegram.telegram :refer [start-telegram!]]
            [mlinks.config :as c]
            [mlinks.utils :refer [log]]
            [mlinks.server.database.dbcontroller :as dbc]
            [morse.polling :as p]
            [clojure.core.async :as ca])
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
  (do
    (def context (atom {}))

    (swap! context conj {:config (c/read-config!)})

    (dbc/init-db! context)

    (require '[mlinks.telegram.telegram :as tg])

    (require '[morse.polling :as p])

    (require '[mlinks.server.stats.simple-stat :as ss])

    (require '[clojure.core.async :as ca]))

  (tg/create-handler context)

  (def tg (p/start (c/token context) tg/handler))

  (p/stop tg)

  (def stop (ss/start-saving! context 3000))

  (ca/close! stop)

  (def srv (run-server context))

  (.stop srv)

  (-main [])
;
  
  )
;;
  


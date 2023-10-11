(ns test-bot.core
  (:require [test-bot.server :refer [run-server]]
            [test-bot.telegram :refer [start-telegram!]]
            [test-bot.config :as c]
            [test-bot.utils :refer [log]])
  (:gen-class))

(defn -main
  [& _args]
  (try (c/read-config!)
       (run-server)
       (catch Exception e
         (log (str "Exiting because of "
                   (.getMessage e)))
         (System/exit 1)))
  (start-telegram!))

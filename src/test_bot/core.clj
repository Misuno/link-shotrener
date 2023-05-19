(ns test-bot.core
  (:require [test-bot.server :refer [run-server]]
            [test-bot.telegram :refer [start-telegram!]]
            [test-bot.config :as c])
  (:gen-class))

(defn -main
  [& args]
  (c/read-config!)
  (start-telegram!)
  (run-server))

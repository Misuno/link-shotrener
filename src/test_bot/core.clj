(ns test-bot.core
  (:require [test-bot.server :refer [server]]
            [test-bot.telegram :refer [start-telegram!]])
  (:gen-class))



(defn -main
  [& args]
  (start-telegram!)
  (.start server))

(ns test-bot.utils
  (:require [test-bot.config :as c]))

(defn log
  [& text]
  (when (c/log-enabled?)
    (println text)))

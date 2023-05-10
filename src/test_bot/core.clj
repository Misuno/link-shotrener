(ns test-bot.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [morse.polling :as p]
            [test-bot.generator :refer :all]
            [test-bot.telegram :refer :all])
  (:gen-class))



(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the test-bot")
  (<!! (p/start token handler)))

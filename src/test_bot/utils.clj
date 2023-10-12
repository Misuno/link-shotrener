(ns test-bot.utils
  (:require [test-bot.config :as c]
            [clojure.string :as str]))

(defn log
  [& text]
  (when (c/log-enabled!?)
    (println text)))

(defn get-shortlink-tail
  "Cuts all excessive parts from shortened link and getting it to format /foobar"
  [dirty]
  (let [almost-clean (-> dirty
                         (str/split #"/")
                         last)]
       (if (not= (first almost-clean) "/")
         (str "/" almost-clean)
         almost-clean
         )))
        

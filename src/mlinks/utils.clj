(ns mlinks.utils
  (:require [mlinks.config :as c]
            [clojure.string :as str]))

(defn log
  [ctx & text]
  (when (c/log-enabled? ctx)
    (println text)))

(defn get-shortlink-tail
  "Cuts all excessive parts from shortened link and getting it to format /foobar"
  [dirty]
  (-> dirty
      (str/split #"/")
      last))

(defn complete-short-link [ctx link]
  (assoc link
         :sl
         (str (get-in @ctx [:config :server :url])
              (:sl link))))
        
(comment
  (get-shortlink-tail "http://localhost:3000/l/googa")
  ;;
  )

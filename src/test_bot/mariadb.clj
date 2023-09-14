(ns test-bot.mariadb
  (:require [clojure.java.jdbc :as j]
            [java-time.api :as jt]
            [test-bot.utils :refer [log]]))

(def database {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/link_shortener"
               :user "root"
               :password "fafner"})

(defn get-current-timestamp
  []
  (jt/instant->sql-timestamp (jt/instant)))

(defn save-click!
  [sl data]
  (log "saving " sl " and " data)
  (j/insert! database :clicks {:short_link sl
                               :click_data data
                               :click_ts (get-current-timestamp)}))

(defn save-to-db!
  [id ll sl]
  (j/insert! database :links {:short_link sl
                              :long_link ll
                              :chat id}))

(defn get-from-db!
  [sl]
  (:long_link (first (j/query
                      database [(str " select long_link from links"
                                     " where short_link = '" sl "'")]))))

(defn get-all-links!
  [id]
  (j/query database (str " select long_link, short_link from links"
                         " where chat = " id ";")))

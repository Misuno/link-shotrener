(ns test-bot.mariadb
  (:require [clojure.java.jdbc :as j]
            [java-time.api :as jt]
            [test-bot.config :as c]
            [test-bot.utils :refer [log]]))


(defn setup-database! [ctx]
  (swap! ctx assoc :database {:subprotocol (c/db-type ctx)
                              :subname     (str (c/db-url ctx)
                                                (c/db-name ctx))
                              :user        (c/db-user ctx)
                              :password    (c/db-password ctx)}))


(comment

  (def config (atom {:config (test-bot.config/read-config!)}))
  @config
  (setup-database! config)
  (save-to-db! config 12345 "https://google.com" "/googa")
;;
  )

(defn current-timestamp
  []
  (str (jt/instant->sql-timestamp (jt/instant))))

(defn- prepare-rows [clicks]
  (reduce (fn [acc {sl :short_link d :data}]
            (conj acc [sl d (current-timestamp)]))
          []
          clicks))

(defn save-click!
  ([ctx sl data]
   (log ctx "saving " sl " and " data)
   (j/insert! (:database (c/get-config ctx))
              :clicks {:short_link sl
                       :click_data data
                       :click_ts (current-timestamp)}))
  ([ctx clicks]
   ;; [{:short_link sl :data d}]
   (when (> (count clicks) 1)
     (log ctx "Sending " (count clicks) " to DB")
     (j/insert-multi! (:database (c/get-config ctx))
                      :clicks
                      [:short_link :click_data :click_ts]
                      (prepare-rows clicks)))))

(defn save-to-db!
  [ctx id ll sl]
  (j/insert! (:database @ctx)
             :links {:short_link sl
                     :long_link ll
                     :chat id}))

(defn get-from-db!
  [ctx sl]
  (:long_link (first (j/query (:database @ctx)
                              [(str " select long_link from links"
                                    " where short_link = '" sl "'")]))))

(defn get-all-links!
  [ctx id]
  (vec (j/query (:database @ctx)
                (str " select long_link, short_link from links"
                     " where chat = " id ";"))))

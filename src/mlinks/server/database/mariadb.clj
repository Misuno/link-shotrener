(ns mlinks.server.database.mariadb
  (:require [clojure.java.jdbc :as j]
            [java-time.api :as jt]
            [mlinks.config :as c]
            [mlinks.utils :refer [log]]
            [mlinks.dsl :refer [make-link]]
            [honey.sql :as sql]))


(defn setup-database! [ctx]
  (swap! ctx assoc :database {:subprotocol (c/db-type ctx)
                              :subname     (str (c/db-url ctx)
                                                (c/db-name ctx))
                              :user        (c/db-user ctx)
                              :password    (c/db-password ctx)}))

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
  [ctx {:keys [author short long] :as link}]
  (j/insert! (:database @ctx)
             :links {:short_link short
                     :long_link long
                     :chat author})
  link)

(defn get-from-db!
  [ctx sl]
  (let [{:keys [id short_link long_link chat]}
        (try (j/query (:database @ctx)
                      (sql/format {:select [:long_link]
                                     :from [:links]
                                     :where [:= :short_link sl]}))
             (catch Exception e (throw e)))]
    (make-link id chat long_link short_link)))

(defn get-all-links!
  [ctx id]
  (->> (try (j/query (:database @ctx)
                     (sql/format {:select [:long_link :short_link]
                                  :from :links
                                  :where [:= :chat id]}))
            (catch Exception e (throw e)))
       (mapv (fn [{:keys [id long_link short_link chat]}]
               (make-link id chat long_link short_link)))))
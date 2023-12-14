(ns mlinks.server.database.mariadb
  (:require [clojure.java.jdbc :as j]
            [java-time.api :as jt]
            [mlinks.config :as c]
            [mlinks.utils :refer [log]]
            [mlinks.dsl :refer [make-link]]
            [honey.sql :as sql]))

(def links  :links)

(def simple-stats :simple-stats)


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

(defn save-link!
  [ctx link]
  (let [res (try (j/insert! (:database @ctx)
                             links link)
                 (catch Exception e (throw e)))]
    (->> res
        first
        :generated_key
        (assoc link :id))))

(defn get-long!
  [ctx sl]
  (let [req (sql/format {:select [:id :ll :sl]
                         :from [links]
                         :where [:= :sl (str sl)]}
                        {:inline true})]
    (-> (try
          (j/query (:database @ctx) req)
          (catch Exception e (throw e)))
        first)))

(defn get-all-links!
  [ctx id]
  (let [req (sql/format {:select [:id :ll :sl]
                         :from links
                         :where [:= :author id]}
                        {:inline true})]
    (try (j/query (:database @ctx) req)
         (catch Exception e (throw e)))))

(defn get-links-with-info! [ctx author]
  (let [req (-> {:select [:links.id :sl :ll :author :clicks]
                 :from links
                 :left-join [simple-stats [:= :links.id :simple-stats.id]]
                 :where [:= :author author]}
                (sql/format {:inline true}))
        res (try (j/query (:database @ctx) req)
               (catch Exception e (throw e)))]
    (mapv (fn [{:keys [id author sl ll clicks]}]
            {:link (make-link id author ll sl)
             :info {:clicks clicks}})
          res)))

;; TODO debug this func
(defn write-simple-stat! [ctx stats]
  (let [req (-> {:insert-into [simple-stats]
                 :columns [:id :clicks]
                 :values (vec stats)
                 :on-duplicate-key-update {:fields {:clicks [:+ :clicks [:values 'clicks]]}}}
                (sql/format {:inline true}))]
    (try (j/db-do-commands (:database @ctx) req)
         (catch Exception e (throw e)))))


(comment

  (do
    (def context (atom {}))

    (swap! context conj {:config (c/read-config!)})

    (setup-database! context))

  (get-links-with-info! context "sad")

  (save-link! context {:author "sad" :sl "googa" :ll "https://google.com"})

  (j/insert! (:database @context) links {:author "sad" :sl "googa" :ll "https://google.com"})

  (get-all-links! context "sad")

  (get-long! context "V2HKU")

  (j/query (:database @context)
           (sql/format {:select [:*]
                        :from [links]
                        :where [:= :short_link "lalal"]}
                       {:inline true}))

  ;;
  )

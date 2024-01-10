(ns  mlinks.server.api.v1.dbcontroller
  (:require [clojure.java.jdbc :as j]
            [mlinks.config :as c]
            [mlinks.utils :refer [log]]
            [honey.sql :as sql]))

(def users :users)

(defn setup-database! [ctx]
  (swap! ctx assoc :database {:subprotocol (c/db-type ctx)
                              :subname     (str (c/db-url ctx)
                                                (c/db-name ctx))
                              :user        (c/db-user ctx)
                              :password    (c/db-password ctx)}))

(defn user-data! [ctx username]
  (let [req (-> {:select :*
                 :from users
                 :where [:= :username username]}
                (sql/format {:inline true}))
        resp (try (j/query (:database @ctx) req)
                  (catch Exception e (throw e)))]
    (if (>= (count resp) 1)
      (first resp)
      {})))

(defn new-user [ctx username pwd-hash]
  (log ctx "Username: " username "\n" "Pwd-hash: " pwd-hash "\n")
  (let [req (-> {:insert-into [users]
                 :columns [:username :pwd_hash]
                 :values [[username pwd-hash]]}
                (sql/format {:inline true}))]
    (try (j/db-do-commands (:database @ctx) req)
         (catch Exception e (throw e)))))

(defn user-pwd-hash [ctx username]
  (let [req (-> {:select :pwd_hash
                 :from users
                 :where [:= :username username]}
                (sql/format {:inline true}))
        resp (try (j/query (:database @ctx) req)
                  (catch Exception e (throw e)))]
    (if (>= (count resp) 1)
      (first resp)
      (throw (Exception. "No user")))))


(comment

  (do
    (def context (atom {}))

    (swap! context conj {:config (c/read-config!)})

    (setup-database! context))

  (user-pwd-hash context "alex@misuno.org")
  ;
  )

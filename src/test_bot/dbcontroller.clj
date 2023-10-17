(ns test-bot.dbcontroller
  (:require
   ;;[test-bot.filedb :as dbc]
   [test-bot.mariadb :as dbc]))

(defn init-db! [ctx]
  (dbc/setup-database! ctx))

(defn save-click!
  ([ctx sl data]
   (dbc/save-click! ctx sl data))
  ([ctx clicks]
   (dbc/save-click! ctx clicks)))


(defn get-all-links! [ctx id]
  (dbc/get-all-links! ctx id))

(defn save-to-db! [ctx id long short]
  (dbc/save-to-db! ctx id long short))

(defn get-from-db! [ctx short]
  (dbc/get-from-db! ctx short))

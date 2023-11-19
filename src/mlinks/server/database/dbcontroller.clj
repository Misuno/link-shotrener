(ns mlinks.server.database.dbcontroller
  (:require
   ;;[mlinks.filedb :as dbc]
   [mlinks.server.database.mariadb :as db]))

(defn init-db! [ctx]
  (db/setup-database! ctx))

(defn save-click!
  ([ctx sl data]
   (db/save-click! ctx sl data))
  ([ctx clicks]
   (db/save-click! ctx clicks)))


(defn get-all-links! [ctx id]
  (db/get-all-links! ctx id))

(defn save-link! [ctx link]
  (db/save-link!
 ctx link))

(defn get-long! [ctx short]
  (db/get-long! ctx short))

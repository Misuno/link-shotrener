(ns test-bot.dbcontroller
  (:require
   ;;[test-bot.filedb :as dbc]
   [test-bot.mariadb :as dbc]))

(defn save-click!
  ([sl data]
   (dbc/save-click! sl data))
  ([clicks]
   (dbc/save-click! clicks)))


(defn get-all-links! [id]
  (dbc/get-all-links! id))

(defn save-to-db! [id long short]
  (dbc/save-to-db! id long short))

(defn get-from-db! [short]
  (dbc/get-from-db! short))

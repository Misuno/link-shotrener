(ns mlinks.link
  (:require [mlinks.cache :refer [add-to-cache! get-from-cache!]]
            [mlinks.utils :refer [log]]
            [mlinks.server.database.dbcontroller :as dbc]))

(defn save-link
  [ctx link]
  (log ctx "save link " link)
  (->> link
       (dbc/save-link! ctx)
       (add-to-cache! ctx)))

(defn get-long!
  [ctx short]
  (log ctx "get long link" short )
  (or (get-from-cache! ctx short)
      (try (->> (dbc/get-long! ctx short)
                (add-to-cache! ctx))
           (catch Exception e (throw e)))))

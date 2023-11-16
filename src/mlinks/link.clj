(ns mlinks.link
  (:require [mlinks.cache :refer [add-to-cache! get-from-cache!]]
            [mlinks.utils :refer [log]]
            [mlinks.server.database.dbcontroller :as db]))

(defn save-link
  [ctx link]
  (->> link
       (db/save-to-db! ctx)
       (add-to-cache! ctx)))

(defn- get-long-from-db!
  [ctx short]
  (let [link (try (db/get-from-db! ctx short)
                  (catch Exception e (throw e)))]
    (add-to-cache! ctx link)))

(defn get-long-link!
  [ctx short]
  (log ctx "get long link" short )
  (or (get-from-cache! ctx short)
      (try (get-long-from-db! ctx short)
           (catch Exception e (throw e)))))

(ns mlinks.server.api.v1.links
  (:require [jsonista.core :as json]
            [mlinks.server.database.dbcontroller :as dbc]
            [mlinks.utils :as utils]))

(defn all-links [ctx author]
  (->> (dbc/get-all-links! ctx author)
       (map (partial utils/complete-short-link ctx))
       (assoc {} :links)
       json/write-value-as-string))


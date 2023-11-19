(ns mlinks.telegram.callbacks
  (:require [mlinks.telegram.handlers :as h]
            [clojure.edn :refer [read-string]]))

(defn callback
  [ctx msg]
  (let [{{message_id :message_id} :message
         {id :id} :from
         data :data} msg
        d (read-string data)
        sl (:l d)]
    (when (and d sl (= :stats (:a d)))
      (h/link-info ctx id message_id sl))))

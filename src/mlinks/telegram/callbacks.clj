(ns mlinks.telegram.callbacks
  (:require [mlinks.telegram.handlers :as h]
            [mlinks.utils :refer [log]]
            [clojure.edn :refer [read-string]]))

(defn callback
  [ctx msg]
  (log ctx "Callback. Data = " (:data msg))
  (let [{{message_id :message_id} :message
         {id :id} :from
         data :data} msg
        d (read-string data)
        sl (str (:l d))]
    (when (and d sl (= :stats (:a d)))
      (try (h/link-info ctx id message_id sl)
           (catch Exception e (log ctx "can't form link info: " msg ". \n" (.message e)))))))

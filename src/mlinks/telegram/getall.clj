(ns mlinks.telegram.getall
  (:require [morse.api :as t]
            [mlinks.config :as c]
            [mlinks.utils :refer [log]]
            [mlinks.server.database.dbcontroller :as db]))

(defmacro authorized-callback [& body]
  `(try ~@body
        (catch Exception ~'_e
          (not-authorized ~'ctx ~'id
           ))))

(defn link-keyboard [link cb-data]
  {:reply_markup {:inline_keyboard [[{:text link
                                      :callback_data cb-data}]]}})

(defn callback [ctx
                {{message_id :message_id} :message {id :id} :from}]
  (t/edit-text (c/token ctx) id message_id "Clicked!!!"))

(defn not-authorized [ctx id]
  (t/send-text (c/token ctx) id "You are not authorized"))

(defn all-links
  [ctx id]
  (println (c/log-enabled? ctx))
  (log ctx "all links: " id)
  (authorized-callback (->> (db/get-all-links! ctx id)
                            (mapv (fn [{:keys [:short_link :long_link]}]
                                    (t/send-text (c/token ctx)
                                                 id
                                                 (link-keyboard long_link "test")
                                                 (str long_link " -> "
                                                      (c/base-url ctx) short_link)))))))

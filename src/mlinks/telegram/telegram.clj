(ns mlinks.telegram.telegram
  (:require [morse.api :as t]
            [morse.handlers :as mh]
            [morse.polling :as p]
            [mlinks.config :as c]
            [mlinks.server.database.dbcontroller :as db]
            [mlinks.utils :refer [log]]
            [mlinks.cache :refer [clear-links!]]
            [mlinks.telegram.handlers :as h]
            [mlinks.telegram.callbacks :refer [callback]]
            [clojure.core.async :refer [<!!]]))


#_{:clj-kondo/ignore [:unresolved-symbol]}
(defn create-handler [ctx]
  (let [token (c/token ctx)]
    (mh/defhandler handler

      (mh/callback-fn (fn [data] (callback ctx data)))
      
      (mh/command-fn "start"
                    (fn [{{id :id username :username :as _} :chat}]
                      (t/send-text token
                                   id
                                   "Welcome to woo link shortener")
                      (if (c/bot-admin? ctx username)
                        (t/send-text token
                                     id
                                     "✅ You have admin rights to here!")
                        (t/send-text token
                                     id
                                     "✅ You have rights to work here!"))))

      (mh/command-fn "help"
                    (fn [{{id :id :as chat} :chat}]
                      (log ctx "Help was requested in " chat)
                      (t/send-text token
                                   id
                                   "Help is on the way")))

      (mh/command-fn "reset"
                    (fn [_] (clear-links!)))

      (mh/command-fn "new"
                    (fn [{{:keys [:id :username]} :chat}]
                      (log ctx "Chat [" username "] newlink command")
                      (h/newlink ctx id)))

      (mh/command-fn "all"
                    (fn [{{:keys [username] :as msg} :chat}]
                      (log ctx "Chat [" username "] getall command")
                      (h/all-links ctx msg)))

      (mh/message-fn
       (fn [{{id :id, username :username} :chat
             text :text :as message}]
         (log ctx "Chat:[" username "]" text)
         ((h/get-text-handler id) ctx id message))))))

(defn start-telegram!
  [ctx]
  (println "Starting the bot")

  (let [handler (create-handler ctx)
        running (try (p/start (c/token ctx) handler)
                     (catch Exception e
                       (println "Exception in start bot: " e)))]
    (<!! running)
    (log ctx "Restarting bot timer starts")
    (Thread/sleep 2000)
    (log ctx "Restarting the bot")
    (recur ctx))
  )


(comment

  (do
    (def context (atom {}))

    (swap! context conj {:config (c/read-config!)})

    (db/init-db! context)

    (create-handler context))

  (def running (p/start (c/token context) handler))

  @context

  (p/stop running)
  ;;
  )



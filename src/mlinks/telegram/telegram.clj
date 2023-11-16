(ns mlinks.telegram.telegram
  (:require [morse.api :as t]
            [morse.handlers :as h]
            [morse.polling :as p]
            [mlinks.generator :refer [link-generator!]]
            [mlinks.config :as c]
            [mlinks.server.database.dbcontroller :as db]
            [mlinks.utils :refer [log get-shortlink-tail]]
            [mlinks.cache :refer [clear-links!]]
            [mlinks.telegram.getall :as handlers]
            [mlinks.link :refer [get-long-link!]]
            [clojure.core.async :refer [<!!]]))

(def text-handlers (atom {}))

(defn set-handler
  [id func]
  (swap! text-handlers assoc id func))

(defn clear-handler
  [id]
  (swap! text-handlers #(dissoc % id)))

(defn default-text-handler []
  (fn [ctx id _message]
    (t/send-text (c/token ctx)
                 id
                 {:reply_markup {:keyboard [[{:text "Create link"}]
                                            [{:text "Get link"}]
                                            [{:text "Show all my links"}]]}
                  :one_time_keyboard false
                  :is_persistent true}
                 "That's your options:")))

(defn get-text-handler [chatid]
  (get @text-handlers chatid (default-text-handler)))

(defn newlink
  [ctx id]
  (let [token (c/token ctx)]
    (t/send-text token id "Enter link")
    (set-handler id (fn [ctx id {link :text}]
                      (t/send-text token
                                   id
                                   (str (c/base-url ctx)
                                        (link-generator! ctx id link)))
                      (clear-handler id)))))

(defn getlink
  [ctx id]
  (let [token (c/token ctx)]
    (t/send-text token id "Enter link")
    (set-handler id
                 (fn [ctx id {link :text}]
                   (try (t/send-text token
                                     id
                                     (->> link
                                         get-shortlink-tail
                                         (get-long-link! ctx)))
                        (catch Exception e (log ctx "Error: " e)))
                   (clear-handler id)))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defn create-handler [ctx]
  (let [token (c/token ctx)]
    (h/defhandler handler

      (h/callback-fn (fn [data] (handlers/callback ctx data)))
      
      (h/command-fn "start"
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

      (h/command-fn "help"
                    (fn [{{id :id :as chat} :chat}]
                      (log ctx "Help was requested in " chat)
                      (t/send-text token
                                   id
                                   "Help is on the way")))

      (h/command-fn "reset"
                    (fn [_] (clear-links!)))

      (h/command-fn "newlink"
                    (fn [{{:keys [:id :username]} :chat}]
                      (log ctx "Chat [" username "] newlink command")
                      (newlink ctx id)))

      (h/command-fn "getall"
                    (fn [{{:keys [id username]} :chat}]
                      (log ctx "Chat [" username "] getall command")
                      (handlers/all-links ctx id)))

      (h/message-fn
       (fn [{{id :id, username :username} :chat
             text :text
             :as message}]
         (log ctx "Chat:[" username "]" text)
         (when (c/bot-admin? ctx username)
           (case text
             "Create link" (newlink ctx id)
             "Get link" (getlink ctx id)
             "Show all my links" (handlers/all-links ctx id)
             ((get-text-handler id) ctx id message))))))))


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



(ns test-bot.telegram
  (:require [clojure.core.async :refer [<!!]]
            [morse.api :as t]
            [morse.handlers :as h]
            [morse.polling :as p]
            [test-bot.generator :refer [link-generator! get-long-from-db!]]
            [test-bot.config :as c]
            [test-bot.dbcontroller :as db]
            [test-bot.utils :refer [log get-shortlink-tail]]
            [test-bot.cache :refer [clear-links!]]))

(def text-handlers (atom {}))

(defn default-text-handler []
  (fn [t id _]
    (t/send-text t
                 id
                 {:reply_markup
                  {:keyboard
                   [[{:text "Create link"}]
                    [{:text "Get link"}]
                    [{:text "Show all my links"}]]}
                  :one_time_keyboard false
                  :is_persistent true}
                 "That's your options:")))

(defn get-text-handler [chatid]
  (get @text-handlers chatid (default-text-handler)))

(defn set-handler
  [id func]
  (swap! text-handlers assoc id func))

(defn clear-handler
  [id]
  (swap! text-handlers #(dissoc % id)))

(defn newlink
  [ctx id]
  (let [token (c/token ctx)]
    (t/send-text token id "Enter link")
    (set-handler id (fn [id {link :text}]
                      (t/send-text token
                                   id
                                   (str (c/base-url ctx)
                                        (link-generator! id link)))
                      (clear-handler id)))))

(defn getlink
  [ctx id]
  (let [token (c/token ctx)]
   (t/send-text token id "Enter link")
    (set-handler id
                 (fn [id {link :text}]
                   (try (t/send-text token
                                     id
                                     (-> link
                                         get-shortlink-tail
                                         get-long-from-db!))
                        (catch Exception e (log ctx "Error: " e)))
                   (clear-handler id)))))

(defn all-links
  [ctx id]
  (log ctx "all links: " id)
   (let [links-list (db/get-all-links! ctx id)]
    (doall (map (fn [{short :short_link long :long_link}]
                  (t/send-text (c/token ctx)
                               id
                               (str long " -> " (c/base-url ctx) short)))
                links-list))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defn create-handler [ctx]
  (h/defhandler handler
    (let [token (c/token ctx)]
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

      (h/message-fn
       (fn [{{id :id, username :username} :chat
             text :text
             :as message}]
         (log ctx "Chat:[" username "]" text)
         (when (c/bot-admin? ctx username)
           (case text
             "Create link" (newlink ctx id)
             "Get link" (getlink ctx id)
             "Show all my links" (all-links ctx id)
             ((get-text-handler id) token id message))))))))


(defn start-telegram!
  [ctx]
  (println "Starting the bot")
  (let [handler (create-handler ctx)]
    (<!!
     (try (p/start (c/token ctx) handler)
          (catch Exception e
            (println "Exception in start bot: " e)))))
  (recur ctx))


(comment

  (start-telegram! (atom {:config {:telegram {:token "lalal"}
                                   :base-url "base"
                                   }}))
  ;;
  )

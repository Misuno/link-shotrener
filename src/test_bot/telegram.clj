(ns test-bot.telegram
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
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
  (fn [id _]
    (t/send-text
     (c/token)
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
  (swap! text-handlers #(assoc % id func)))

(defn clear-handler
  [id]
  (swap! text-handlers #(dissoc % id)))

(defn newlink
  [id]
  (t/send-text (c/token) id "Enter link")
  (set-handler id (fn [id {link :text}]
                    (t/send-text (c/token)
                                 id
                                 (str (c/base-uri)
                                      (link-generator! id link)))
                    (clear-handler id))))

(defn getlink
  [id]
  (t/send-text (c/token) id "Enter link")
  (set-handler id (fn [id {link :text}] (t/send-text (c/token)
                                                     id
                                                     (try (-> link
                                                              get-shortlink-tail
                                                              get-long-from-db!)
                                                          (catch Exception e (str e))))
                    (clear-handler id))))

(defn get-all-links!
  [id]
  (let [links-list (db/get-all-links! id)]
    (reduce (fn [acc {short :short_link long :long_link}]
              (conj acc
                    (str long " -> " (c/base-uri) short)))
            []
            links-list)))

(comment 
  (get-all-links! 539153875)
  
  ;;
  )


(defn all-links
  [id]
  (log "all links: " id)
   (let [links-list (db/get-all-links! id)]
    (doall (map (fn [{short :short_link long :long_link}]
                  (t/send-text (c/token)
                               id
                               (str long " -> " (c/base-uri) short)))
                links-list))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(h/defhandler handler

  (h/command-fn "start"
                (fn [{{id :id username :username :as _} :chat}]
                  (t/send-text (c/token) id "Welcome to woo link shortener")
                  (if (c/bot-admin? username)
                    (t/send-text (c/token) id "✅ You have admin rights to here!")
                    (t/send-text (c/token) id "✅ You have rights to work here!"))))

  (h/command-fn "help"
                (fn [{{id :id :as chat} :chat}]
                  (log "Help was requested in " chat)
                  (t/send-text ((c/token)) id "Help is on the way")))

  (h/command-fn "reset"
                (fn [_] (clear-links!)))


  (h/message-fn
   (fn [{{id :id username :username} :chat text :text :as message}]
     (log "Chat:[" username "]" text)
     (when (c/bot-admin? username)
       (case text
         "Create link" (newlink id)
         "Get link" (getlink id)
         "Show all my links" (all-links id)
         ((get-text-handler id) id message))))))


(defn start-telegram!
  []
  (when (str/blank? (c/token))
    (println "Please provde token in TELEGRAM_TOKEN environment variable!"))

  (println "Starting the bot")
  (<!! #_{:clj-kondo/ignore [:unresolved-symbol]}
   (try (p/start (c/token) handler)
        (catch Exception e
          (println "Exception in start bot: " e))))
  (recur))

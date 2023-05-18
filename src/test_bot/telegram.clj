(ns test-bot.telegram
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [morse.api :as t]
            [morse.handlers :as h]
            [morse.polling :as p]
            [test-bot.generator :refer [clear-links! get-long-link! link-generator!]]
            [test-bot.filedbcontroller :as db]
            [test-bot.config :as c]))

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
                    (t/send-text (c/token) id (link-generator! id link))
                    (clear-handler id))))

(defn getlink
  [id]
  (t/send-text (c/token) id "Enter link")
  (set-handler id (fn [id {link :text}]
                    (t/send-text (c/token) id (get-long-link! link))
                    (clear-handler id))))

(defn all-links
  [id]
  (println "all links: " id)
  (let [links-list (db/get-all-links! id)]
    (println links-list)
    (doall (map #(do
                   (println "combined: " %)
                   (t/send-text (c/token) id %))
                links-list))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(h/defhandler handler

  (h/command-fn "start"
                (fn [{{id :id username :username :as chat} :chat}]
                  (t/send-text (c/token) id "Welcome to woo link shortener")
                  (if (c/bot-admin? username)
                    (t/send-text (c/token) id "✅ You have rights to work here!")
                    (t/send-text (c/token) id "✅ You have rights to work here!"))))

  (h/command-fn "help"
                (fn [{{id :id :as chat} :chat}]
                  (println "Help was requested in " chat)
                  (t/send-text ((c/token)) id "Help is on the way")))

  (h/command-fn "reset"
                (fn [_] (clear-links!)))


  (h/message-fn
   (fn [{{id :id username :username} :chat text :text :as message}]
     (when (c/bot-admin? username)
       (println text)
       (case text
         "Create link" (newlink id)
         "Get link" (getlink id)
         "Show all my links" (all-links id)
         ((get-text-handler id) id message))))))


(defn start-telegram!
  []
  (when (str/blank? (c/token))
    (println "Please provde token in TELEGRAM_TOKEN environment variable!"))

  (println "Starting the test-bot")
  (<!! #_{:clj-kondo/ignore [:unresolved-symbol]}
       (p/start (c/token) handler)))
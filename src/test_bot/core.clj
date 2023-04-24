(ns test-bot.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t]
            [test-bot.generator :refer :all])
  (:gen-class))

; TODO: fill correct token
(def token "1324057622:AAHw82jikU8YK6_5jXbQP44i0oQNQNU03EY")

(def text-handlers (atom {}))

(defn default-text-handler []
  (fn [id msg]
    (t/send-text
     token
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
  (t/send-text token id "Enter link")
  (set-handler id (fn [id {link :text}]
                    (t/send-text token id (link-generator! id link))
                    (clear-handler id))))

(defn getlink
  [id]
  (t/send-text token id "Enter link")
  (set-handler id (fn [id {link :text}]
                    (do (t/send-text token id (get-long-link! link))
                        (clear-handler id)))))

(defn all-links
  [id]
  (println "all links: " id)
  (let [links-list (get-all-links! id)]
    (println links-list) 
    (doall (map #(do
                  (println "combined: " %)
                  (t/send-text token id %))
                links-list))))

(h/defhandler handler

  (h/command-fn "start"
                (fn [{{id :id :as chat} :chat}]
                  (println "Bot joined new chat: " chat)
                  (t/send-text token id "Welcome to test-bot!")))

  (h/command-fn "help"
                (fn [{{id :id :as chat} :chat}]
                  (println "Help was requested in " chat)
                  (t/send-text token id "Help is on the way")))

  (h/message-fn
   (fn [{{id :id} :chat text :text :as message}]
     (println text)
     (case text
       "Create link" (newlink id)
       "Get link" (getlink id)
       "Show all my links" (all-links id)
       ((get-text-handler id) id message)))))

(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the test-bot")
  (<!! (p/start token handler)))

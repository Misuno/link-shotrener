(ns test-bot.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t])
  (:gen-class))

; TODO: fill correct token
(def token "1324057622:AAHw82jikU8YK6_5jXbQP44i0oQNQNU03EY")

(def chats (atom #{}))

(defn write-state
  "Function for operating on state. Accepts function to operate on set"
  [func id]
  (swap! chats #(func % id)))

(defn add-chat
  "Adds chat to subscribed list"
  [id]
  (write-state conj id))

(defn remove-chat
  "Removes chat from  subscribed list"
  [id]
  (write-state disj id))

(h/defhandler handler

  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (t/send-text token id "Welcome to test-bot!")))

  (h/command-fn "help"
                (fn [{{id :id :as chat} :chat}]
                  (println "Help was requested in " chat)
                  (t/send-text token id "Help is on the way")))

  (h/command-fn "addme" 
                (fn [{{id :id :as chat} :chat}]
                  (println "Add user comand for chat " id)
                  (add-chat id)))

   (h/command-fn "removeme" 
                (fn [{{id :id :as chat} :chat}]
                  (print "Add user comand for chat " id)
                  (remove-chat id)))

  (h/message-fn
    (fn [{{id :id} :chat :as message}]
      (println "Intercepted message: " message)
      (t/send-text token id "I don't do a whole lot ... yet."))))

(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the test-bot")
  (<!! (p/start token handler)))


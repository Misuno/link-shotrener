(ns mlinks.telegram.handlers
  (:require [morse.api :as t]
            [mlinks.config :as c]
            [mlinks.utils :refer [log get-shortlink-tail]]
            [mlinks.server.stats.simple-stat :as ss]
            [mlinks.link :as l]
            [mlinks.generator :as g]
            [mlinks.server.database.dbcontroller :as dbc]))

(def text-handlers (atom {}))

(defn set-handler
  [id func]
  (swap! text-handlers assoc id func))

(defn clear-handler
  [id]
  (swap! text-handlers #(dissoc % id)))

(defn default-text-handler []
  (fn [_ctx _id _message]))

(defn get-text-handler [chatid]
  (get @text-handlers chatid (default-text-handler)))


(defn liks-header [ctx link]
  (str "Short Link: " (str (c/base-url ctx) (:short link))
       "\nLong Link: " (:long link)
       "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"))

(defn check-authorization
  "Checks if user is in authorized users list. If no, throws Exception"
  [ctx {:keys [username] :as msg}]
  (if (c/bot-admin? ctx username)
    msg
    (throw (Exception. "Not Authorized Telegram User")))
  )

(defn not-authorized [ctx id]
  (t/send-text (c/token ctx) id "You are not authorized"))



(defn disable-preview [params]
  (conj params {:disable_web_page_preview true}))

(defn link-keyboard
  ([link]
   (link-keyboard link {}))
  ([link  params]
   (let [{:keys [long short]} link]
     (conj params
           {:reply_markup
            {:inline_keyboard [[{:text long :url long}
                                {:text "📈 Stats" :callback_data (str "{:a :stats :l " short "}")}]]}}))))

(defn newlink
  [ctx id]
  (log ctx "newlink function")
  (let [token (c/token ctx)]
    (t/send-text token id "Enter link")
    (set-handler id (fn [ctx id {link :text}]
                      (log ctx "Getlink callback")
                      (t/send-text token
                                   id
                                   (->> (g/link-generator! ctx id link)
                                        (l/save-link ctx)
                                        :short
                                        (str (c/base-url ctx))))
                      (clear-handler id)))))

(defn getlink
  [ctx id]
  (let [token (c/token ctx)]
    (log "Getlink function")
    (t/send-text token id "Enter link")
    (set-handler id
                 (fn [ctx id {link :text}]
                   (try (t/send-text token
                                     id
                                     (->> link
                                          get-shortlink-tail
                                          (dbc/get-long! ctx)))
                        (catch Exception e (log ctx "Error: " e)))
                   (clear-handler id)))))

(defn all-links
  [ctx {:keys [id] :as msg}]
  (log ctx "all links: " id)
  (->> (check-authorization ctx msg)
       (:id)
       (dbc/get-all-links! ctx)
       (mapv (fn [link]
               (t/send-text (c/token ctx)
                            id
                            (->> {}
                                 (link-keyboard link)
                                 disable-preview)
                            (liks-header ctx link))))))

(defn link-info [ctx chatid message-id short-link]
  (try (let [link (l/get-long! ctx short-link)]
         (t/edit-text (c/token ctx) chatid message-id
                      (->> {}
                           disable-preview
                           (link-keyboard link))
                      (str (liks-header ctx link)
                           "\n"
                           "Click count: " (ss/stat-for-link short-link))) )
       (catch Exception e ((t/edit-text (c/token ctx) chatid message-id (str "Error: " e))))))

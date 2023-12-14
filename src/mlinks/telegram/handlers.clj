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


(defn liks-header [ctx {:keys [link info]}]
  (str "Short Link: " (str (c/base-url ctx) (:sl link)) "\n"
       "Long Link: " (:ll link) "\n"
       "Click count: " (:clicks info 0) "\n"
       "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"))

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
   (let [{:keys [sl ll]} link]
     (println "making keyboard: long " ll ", short: " sl)
     (conj params
           {:reply_markup
            {:inline_keyboard [[{:text ll :url ll}
                                {:text "📈 Stats"
                                 :callback_data (str {:a :stats :l sl})}]]}}))))

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
                                        :sl
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
       (dbc/get-links-with-info! ctx)
       (mapv (fn [link-info]
               (t/send-text (c/token ctx)
                            id
                            (->> {}
                                 (link-keyboard (:link link-info))
                                 disable-preview)
                            (liks-header ctx link-info))))))

(defn link-info [ctx chatid message-id short-link]
  (let [link (l/get-long! ctx short-link)]
    (try
      (t/edit-text (c/token ctx) chatid message-id
                   (->> {}
                        disable-preview
                        (link-keyboard link))
                   (str (liks-header ctx link)
                        "\n"
                        "Click count: " (ss/stat-for-link short-link)))
      (catch Exception e (t/edit-text (c/token ctx)
                                      chatid
                                      message-id
                                      (str "Error: " e))))))

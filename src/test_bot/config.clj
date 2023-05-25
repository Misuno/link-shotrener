(ns test-bot.config
  (:require [jsonista.core :as j]))

(def config (atom {}))

(def ^:private config-file
  "resources/config.json")

(defn read-config! []
  (reset! config (j/read-value (slurp config-file) j/keyword-keys-object-mapper)))


(defn token
  "Shorthand for bot-token parameter"
  []
  (:bot-token @config))

(defn db-file
  "Shorthand for db-file parameter"
  []
  (:urls-file @config))

(defn server-port
  "Shorthand for server-port parameter"
  []
  (:server-port @config))

(defn buffer-size []
  (:buffer-size @config))

(defn base-uri []
  (let [u (:base-uri @config)]
    (if u
      u
      "http://localhost")))

(defn tail-length []
  (:tail-length @config 7))

(defn bot-admin?
  [chatid]
  (some #(= chatid %) (:bot-admins @config)))


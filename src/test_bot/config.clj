(ns test-bot.config
  (:require [jsonista.core :as j]))

(def config (atom {}))

(def ^:private config-file
  "resources/config.json")

(defn read-config! []
  (reset! config
          (j/read-value (slurp config-file)
                        j/keyword-keys-object-mapper)))

(defn token
  "Shorthand for bot-token parameter"
  []
  (:bot-token @config))

(defn server-port
  "Shorthand for server-port parameter"
  []
  (:server-port @config))

(defn buffer-size []
  (:buffer-size @config))

(defn base-uri []
  (:base-uri @config "http://localhost:3000"))


(defn tail-length []
  (:tail-length @config 7))

(defn log-enabled? []
  (:log @config false))

(defn stat-enabled? []
  (:stat_enabled @config false))

(defn ^:private db
  "Private shortcut for db object in config file"
  []
  (:db @config))

(defn db-type []
  (:type (db)
    (throw (Exception. "Db type not specified"))))

(defn db-url
  "Shorthand for database location parameter"
  []
  (:url (db)
    (throw (Exception. "DB URL not specified"))))

(defn bot-admin?
  [chat-id]
  (some #(= chat-id %) (:bot-admins @config)))


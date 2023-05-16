(ns test-bot.config
  (:require [jsonista.core :as j]))

(def config (atom {}))

(def ^:private config-file
  "resources/config.json")

(defn read-config! []
  (reset! config (j/read-value (slurp config-file) j/keyword-keys-object-mapper)))


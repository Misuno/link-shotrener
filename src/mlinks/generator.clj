(ns mlinks.generator
  (:require [nano-id.core :refer [nano-id]]
            [mlinks.config :as c]
            [mlinks.link :refer [save-link]]
            [mlinks.utils :refer [log]]
            [mlinks.dsl :refer [make-link]]))


(defn create-short-link [ctx]
  (nano-id (c/tail-length ctx)))

(defn link-generator!
  [ctx id link]
  (->> (create-short-link ctx)
       (make-link nil id link)))

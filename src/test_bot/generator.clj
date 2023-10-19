(ns test-bot.generator
  (:require [nano-id.core :refer [nano-id]]
            [test-bot.dbcontroller :as db]
            [test-bot.config :as c]
            [test-bot.cache :refer [add-to-cache! get-from-cache!]]
            [test-bot.utils :refer [log]]))

(defn save-link
  [ctx id long short]
  (db/save-to-db! ctx id long short)
  (add-to-cache! ctx short long)
  short)

(defn get-long-from-db!
  [ctx short]
  (let [ll (db/get-from-db! ctx short)]
    (if ll
      (do (add-to-cache! ctx short ll)
          ll)
      (throw (Exception. "No link in db")))))

(defn get-long-link!
  [ctx short]
  (log ctx "get long link" short )
  (or (get-from-cache! ctx short)
      (try (get-long-from-db! ctx short)
           (catch Exception e (throw e)))))

(defn make-link [ctx]
  (nano-id (c/tail-length ctx)))

(defn link-generator!
  [ctx id link]
  (->> (make-link ctx)
       (save-link ctx id link)))


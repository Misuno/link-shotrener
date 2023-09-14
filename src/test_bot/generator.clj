(ns test-bot.generator
  (:require [nano-id.core :refer [nano-id]]
            [test-bot.dbcontroller :as db]
            [test-bot.config :as c]
            [test-bot.cache :refer [add-to-cache! get-from-cache]]))

(defn save-link
  [id long short]
  (db/save-to-db! id long short)
  (add-to-cache! long short)
  short)

(defn get-long-from-db!
  [short]
  (let [ll (db/get-from-db! short)]
    (if ll
      (do (add-to-cache! ll short)
          ll)
      (throw "No link in db"))))

(defn get-long-link!
  [short]
  (get-from-cache short
                  (try (get-long-from-db! short)
                       (catch Exception e (throw e)))))

(defn make-link []
  (str (nano-id (c/tail-length))))

(defn link-generator!
  [id link]
  (->> (make-link)
       (str "/")
       (save-link id link)))

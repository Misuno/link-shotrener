(ns test-bot.generator
  (:require
   [nano-id.core :refer [nano-id]]
   [clojure.java.jdbc :as j]))

(def mysql-db {:subprotocol "mysql"
               :subname "//127.0.0.1:3306/link_shortener"
               :user "root"
               :password "fafner"})

(def links (atom {}))

(defn clear-links!
  "Clears links map"
  []
  (reset! links {}))


(def domain-name
  (or (System/getenv "BASE_IRI") "http://localhost"))

(defn get-from-db!
  [db sl]
  (j/query db [(str
                " select long_link from links"
                " where short_link = '" sl "'")]))

(defn get-long-from-db!
  [sl]
  (:long_link (first (get-from-db! mysql-db sl))))

(defn save-to-db!
  [db id ll sl]
  (j/insert! db :links {:short_link sl :long_link ll :chat id}))

(defn save-link-locally!
  [long short]
  (swap! links #(assoc % short long)))

(defn save-link
  [id long short]
  (save-to-db! mysql-db id long short)
  (save-link-locally! long short)
  short)

(defn get-long-link!
  [short]
  (get @links (keyword short)
       (let [ll (get-long-from-db! short)]
         (if ll
           (do (save-link-locally! ll short)
               ll)
           false))))

(defn make-link []
  (nano-id 7))

(defn link-generator!
  [id link]
  (->> (make-link)
       (save-link id link)
       (str domain-name)))

(defn get-all-links!
  [id]
  (j/query mysql-db (str
                     " select long_link, short_link from links"
                     " where chat = " id ";")))

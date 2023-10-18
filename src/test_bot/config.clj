(ns test-bot.config
  (:require [clojure.edn :as e]))

(def ^:private config-file
  "resources/config.edn")

(defn get-config [ctx]
  (or (:config @ctx)
      (throw (Exception. "Can't read config from context"))))

(defn read-config! []
  (try (e/read-string (slurp config-file))
       (catch Exception e
         (throw e))))

(defn ^:private get-param-inner
  ([ctx kws not-found]
   (or (get-in (get-config ctx) kws)
       (if (fn? not-found)
         (not-found)
         not-found))))

(defn get-param
  "Returns parameter from config based on the keyword"
  [ctx not-found & kws]
  (get-param-inner ctx kws not-found))

(defn get-param-callback
  "Returns parameter from config file or throws Exception"
  [ctx callback & kws]
  (apply get-param ctx callback kws))

(defn get-param-throws
  [ctx errmsg & kws]
  (let [error-msg errmsg]
    (apply get-param-callback
           ctx
           #(throw (Exception. error-msg))
           kws)))

(defn get-param-def-value
  [ctx defv & kws]
  (apply get-param ctx defv kws))

(comment
  (get-param-inner (atom {:config {:stat-enabled true}})
                   [:lowg]
                   "lalla")

  (get-param-inner (atom {:config {:stat-enabled true}})
                   [:ilog]
                   (println "kaka"))

  (get-param-inner (atom {:config {:stat-enabled true
                                   :telegram {:token "token"}}})
                   [:telegram :token]
                   #(println "kaka"))

  (get-param (atom {:config {:stat-enabled true
                              :telegram {:token "token"}}})
              #(throw (Exception. "qqqq"))
              :telegram :token)

  (get-param-callback #(throw (Exception. "qqqq")) :log)
  (get-param-throws "rrrrr" :log)

  (get-param-def-value "tutut" :log)
  (get-param-def-value "tutut" :loga)
  ;;
  )



(defn token
  "Shorthand for bot-token parameter"
  [ctx]
  (get-param-throws ctx "No telegram token in config"
                     :telegram :token))

(defn server-port
  "Shorthand for server-port parameter"
  [ctx]
  (get-param-throws ctx
                     "No server port in config"
                     :server :port))

(defn buffer-size [ctx]
  (get-param-def-value ctx 128 :buffer-size))

(defn base-url [ctx]
  (get-param-throws ctx "No base URL in config file" :server :url))

(defn tail-length [ctx]
  (get-param-def-value ctx 7 :tail-length))

(defn log-enabled? [ctx]
  (get-param-def-value ctx false :log))

(defn rich-stat-enabled? [ctx]
  (get-param-def-value ctx false :rich-stat :enabled))

(defn db-type [ctx]
  (get-param-throws ctx "No db type specified in config" :db :type))

(defn db-url
  "Shorthand for database location parameter"
  [ctx]
  (get-param-throws ctx "No DB URL in config" :db :url))

(defn bot-admin?
  [ctx chat-id]
  (some #(= chat-id %)
        (get-param-def-value ctx
                              []
                              :telegram :admins)))

(defn db-user [ctx]
  (get-param-throws ctx "No db user in config" :db :user))

(defn db-name [ctx]
  (get-param-throws ctx "No db name in config" :db :db-name))

(defn db-password [ctx]
  (get-param-def-value ctx "" :db :password))

(defn simple-stat-enabled? [ctx]
  (get-param-def-value ctx false :simple-stat :enabled))

(ns test-bot.config
  (:require [jsonista.core :as j]))

(def config (atom {}))

(def ^:private config-file
  "resources/config.json")

(defn read-config! []
  (reset! config
          (j/read-value (slurp config-file)
                        j/keyword-keys-object-mapper)))

(defn ^:private get-param-inner
  ([atm kws not-found]
   (or (get-in @atm kws) (if (fn? not-found) (not-found) not-found))))

(defn get-param!
  "Returns parameter from config based on the keyword"
  ([not-found & kws]
   (get-param-inner config kws not-found)))

(defn get-param-callback!
  "Returns parameter from config file or throws Exception"
  [callback & kws]
  (apply get-param! callback kws))

(defn get-param-throws!
  [errmsg & kws]
  (let [error-msg errmsg]
    (apply get-param-callback!
           #(throw (Exception. error-msg))
           kws)))

(defn get-param-def-value!
  [defv & kws]
  (apply get-param! defv kws))

(comment
  (get-param-inner config [:lowg] "lalla")
  (get-param-inner config [:ilog] (println "kaka"))
  (get-param-inner config [:log] #(println "kaka"))
  
  (get-param! #(throw (Exception. "qqqq")) :telegram :token)
  
  (get-param-callback! #(throw (Exception. "qqqq")) :log)
  (get-param-throws! "rrrrr" :log)

  (get-param-def-value! "tutut" :log)
  (get-param-def-value! "tutut" :loga)
  ;;
  )



(defn token!
  "Shorthand for bot-token parameter"
  []
  (get-param-throws! "No telegram token in config"
                     :telegram :token))

(defn server-port!
  "Shorthand for server-port parameter"
  []
  (get-param-throws! "No server port in config"
                     :server :port))

(defn buffer-size! []
  (get-param-def-value! 128 :buffer-size))

(defn base-url! []
  (get-param-throws! "No base URL in config file" :server :url))

(defn tail-length []
  (get-param-def-value! 7 :tail-length))

(defn log-enabled!? []
  (get-param-def-value! false :log))

(defn stat-enabled!? []
  (get-param-def-value! false :stat-enabled))

(defn db-type! []
  (get-param-throws! "No db type specified in config" :db :type))

(defn db-url!
  "Shorthand for database location parameter"
  []
  (get-param-throws! "No DB URL in config" :db :url))

(defn bot-admin?
  [chat-id]
  (some #(= chat-id %) (:bot-admins @config)))


(ns mlinks.server.api.v1.handlers
  (:require [buddy.hashers :as hashers]
            [mlinks.server.api.v1.dbcontroller :as dbc]
            [ring.util.response :as r]))

(def hash-alg {:alg :bcrypt+blake2b-512})

(defn username-free?! [ctx username]
  (try (-> (dbc/user-data! ctx username)
           empty?)
       (catch Exception _ false)))

(defn new-user! [ctx username password]
  (if (username-free?! ctx username)
    (try (dbc/new-user ctx
                       username
                       (hashers/derive password hash-alg))
         (catch Exception e (throw e)))
    (throw (ex-info "Username taken"
                    {:username username}))))

(defn valid-pwd? [ctx username password]
  (try (->> username
            (dbc/user-pwd-hash ctx)
            (hashers/verify password))
       (catch Exception _ false)))

(defn login [ctx username pwd]
  (r/redirect (if (valid-pwd? ctx username pwd)
                "/logged-in"
                "/invalid-login")))


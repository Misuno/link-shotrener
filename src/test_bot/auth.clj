(ns test-bot.auth
  (:require [buddy.auth.backends :as backends]
            [ring.util.response :as r]))

;; Define a in-memory relation between tokens and users:
(def tokens {:2f904e245c1f5 :admin
             :45c1f5e3f05d0 :foouser})

;; Simple self defined handler for unauthorized requests.
(defn my-unauthorized-handler
  [request metadata]
  (-> (r/response "Unauthorized request")
      (assoc :status 403)))

;; Define an authfn, function with the responsibility
;; to authenticate the incoming token and return an
;; identity instance

(defn my-authfn
  [request token]
  (let [token (keyword token)]
    (get tokens token nil))) ;; Find a user who owns token

;; Create an instance
(def backend (backends/token {:authfn my-authfn
                              :unauthorized-handler my-unauthorized-handler}))


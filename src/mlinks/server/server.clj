(ns mlinks.server.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :as rp]
            [ring.middleware.json :refer [wrap-json-body]]

            ;;Compojure
            [compojure.core :refer [defroutes GET POST context]]
            [compojure.route :as route]

            ;; Auth
            [buddy.auth.middleware :refer (wrap-authentication wrap-authorization)]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            
            ;; mlinks
            [mlinks.generator :refer [link-generator!]]
            [mlinks.config :as c]
            [mlinks.server.middleware :as m]
            [mlinks.utils :refer [log]]
            [mlinks.server.auth :as auth]
            [mlinks.server.handlers :as h]))

(def srv-ctx (atom {}))

(defroutes myroutes
  (context "/api/v1" []
    (POST "/addlink" {{uri :uri} :body :as request}
      (when (authenticated? request)
        (throw-unauthorized {:message "Not authorized"}))
      (link-generator! srv-ctx 1 uri)))

  (GET "/l/:short-link" [short-link]
    (h/srv-handler srv-ctx {:uri short-link}))

  (GET "/" request
    (str "<h1>Hello World</h1>" request)))
  
  (route/not-found "<h1>Page not found</h1>")

(def app
  (-> myroutes
      (wrap-json-body {:keywords? true :bigdecimals? true})
      m/extract-body
      rp/wrap-params
      (wrap-authentication auth/backend)
      (wrap-authorization auth/backend)))

(defn run-server [ctx]
  (def srv-ctx ctx)
  (let [p (c/server-port ctx)
        port (if (string? p) (Integer/parseInt p) p)]
    (log ctx "Starting server on port" port)
    (run-jetty app ;;(partial srv-handler ctx)
               {:port port
                :join? false})))

(comment
  
  (def srv-ctx (atom {}))

  (swap! srv-ctx conj {:config (c/read-config!)})

  (require '[mlinks.dbcontroller :as dbc])
  
  (dbc/init-db! srv-ctx)

  (def srv (run-jetty app {:port 3000 :join? false}))

  (.stop srv)
  
;;
  )

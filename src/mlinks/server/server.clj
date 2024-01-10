(ns mlinks.server.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :as rp]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.util.response :refer [response, status]]

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
            [mlinks.server.handlers :as h]
            [mlinks.server.stats.stats :as stats]
            [mlinks.server.api.v1.handlers :as api-h]
            [mlinks.server.api.v1.links :refer [all-links]]))

(def srv-ctx (atom {}))

(defroutes myroutes

  ;; API 
  (context "/api/v1" []

    (POST "/addlink" {{uri :uri} :body :as request}
      (when-not (authenticated? request)
        (throw-unauthorized {:message "Not authorized"}))
      (link-generator! srv-ctx 1 uri))

    (POST "/register" {params :form-params}
      (try (api-h/new-user! srv-ctx (get params "email") (get params "password"))
           (catch clojure.lang.ExceptionInfo _
             (status {:status "error"
                      :code 409
                      :message "Email already registered"}
                     409))
           (catch Exception _ (status 500))))

    (GET "/links/:author" [author] (all-links srv-ctx author)))

  ;; LINK shortener
  (GET "/l/:short-link" [short-link]
    (h/srv-handler srv-ctx {:uri short-link}))

  ;; TODO add login

  ;; FRONT
  (GET "/" request
    (str "<h1>Hello World</h1>" request))

  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> myroutes
      (wrap-json-body {:keywords? true :bigdecimals? true})
      m/extract-body
      rp/wrap-params
      (wrap-authentication auth/backend)
      (wrap-authorization auth/backend)))

(defn run-server [ctx]
  (reset! srv-ctx @ctx)
  (let [p (c/server-port ctx)
        port (if (string? p) (Integer/parseInt p) p)]
    (log ctx "Starting server on port" port)
    (stats/start! ctx)
    (run-jetty app
               {:port port
                :join? false})))

(comment

  (do (def srv-ctx (atom {}))

      (swap! srv-ctx conj {:config (c/read-config!)})

      (require '[mlinks.server.database.dbcontroller :as dbc])
      
      (dbc/init-db! srv-ctx))

  (def srv (run-jetty app {:port 3000 :join? false}))

  (.stop srv)

;;
  )

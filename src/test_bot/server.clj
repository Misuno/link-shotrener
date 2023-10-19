(ns test-bot.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as r]
            [ring.middleware.params :as rp]
            [ring.middleware.json :refer [wrap-json-body]]
            [test-bot.generator :refer [get-long-link! link-generator!]]
            [test-bot.stats :refer [save-click!]]
            [test-bot.config :as c]
            [test-bot.utils :refer [log]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]))

(def srv-ctx (atom {}))

(defn link-found
  [ctx request sl ll]
  (log ctx "link found" sl "->" ll)
  (save-click! ctx sl request)
  ll)

(defn srv-handler [ctx {uri :uri :as request}]
  (log ctx "working on a uri" uri)
  (try
    (->> uri
         (get-long-link! ctx)
         (link-found ctx request uri)
         r/redirect)
    (catch Exception _ (r/response "No link!!! Fuck you!"))))

(defroutes myroutes
  (GET "/l/:short-link" [short-link] (srv-handler srv-ctx {:uri short-link :data "some data"}))
  ;; (GET "/api/v1/addlink/" _request (str "HELLO"))
  (POST "/api/v1/addlink/" {{uri :uri} :body} (link-generator! srv-ctx 1 uri))
  (GET "/" request (str "<h1>Hello World</h1>" request))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> myroutes
      (wrap-json-body {:keywords? true :bigdecimals? true})
      rp/wrap-params))

(defn run-server [ctx]
  (reset! srv-ctx ctx)
  (let [p (or (System/getenv "PORT") (c/server-port ctx))
        port (if (string? p) (Integer/parseInt p) p)]
    (log ctx "Starting server on port" port)
    (run-jetty app ;;(partial srv-handler ctx)
               {:port port
                :join? false})))

(comment
  
  (def srv-ctx (atom {}))

  (swap! srv-ctx conj {:config (c/read-config!)})

  (require '[test-bot.dbcontroller :as dbc])
  
  (dbc/init-db! srv-ctx)

  (def srv (run-jetty app {:port 3000 :join? false}))

  (.stop srv)
  
;;
  )

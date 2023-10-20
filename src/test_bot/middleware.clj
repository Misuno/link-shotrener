(ns test-bot.middleware)

(defn extract-body [handler]
  (fn [request]
    (let [response (handler request)]
      response)))

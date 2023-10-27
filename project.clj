(defproject link-shortener "0.1.3"
  :description "telegram bot-operated link shortener"
  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure        "1.11.1"]
                 [environ                    "1.2.0"]
                 [morse                      "0.4.3"]
                 [nano-id                    "1.0.0"]
                 [org.clojure/java.jdbc      "0.7.12"]
                 [mysql/mysql-connector-java "8.0.33"]
                 [ring/ring-core             "1.10.0"]
                 [ring/ring-jetty-adapter    "1.8.2"]
                 [ring/ring-json             "0.5.1"]
                 [metosin/jsonista           "0.3.7"]
                 [clojure.java-time          "1.2.0"]
                 [org.clojure/core.async     "1.6.681"]
                 [com.taoensso/carmine       "3.3.0"]
                 [compojure                  "1.7.0"]
                 [buddy/buddy-auth           "3.0.323"]
                 [org.clojure/core.async     "1.6.681"]]

  :plugins [[lein-environ "1.2.0"]]

  :main ^:skip-aot test-bot.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}}

  :min-lein-version "2.9.0")

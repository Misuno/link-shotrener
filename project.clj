(defproject test-bot "0.1.1"
  :description "telegram bot-operated link shortener"
  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure        "1.8.0" ]
                 [environ                    "1.1.0" ]
                 [morse                      "0.2.4" ]
                 [nano-id                    "1.0.0" ]
                 [org.clojure/java.jdbc      "0.7.12"]
                 [mysql/mysql-connector-java "8.0.33"]
                 [ring/ring-core             "1.10.0"]
                 [ring/ring-jetty-adapter    "1.8.2" ]
                 [metosin/jsonista           "0.3.7" ]
                 [clojure.java-time          "1.2.0" ]]

  :plugins [[lein-environ "1.1.0"]]

  :main ^:skip-aot test-bot.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})

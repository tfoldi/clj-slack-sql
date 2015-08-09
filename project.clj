(defproject clj-slack-sql "0.1.0-SNAPSHOT"
  :description "Post SQL Statements results to slack channels"
  :url "https://github.com/tfoldi/clj-slack-sql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.julienxx/clj-slack "0.4.3"]
                 [org.clojure/java.jdbc "0.4.1"]
                 [com.h2database/h2 "1.4.187"]]
  :main ^:skip-aot clj-slack-sql.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

(defproject clj-slack-sql "0.1.1-SNAPSHOT"
  :description "Post SQL Statements results to slack channels"
  :url "https://github.com/tfoldi/clj-slack-sql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.julienxx/clj-slack "0.5.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.cli "0.3.2"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
                 [org.clojure/java.jdbc "0.4.1"]
                 [clj-yaml  "0.4.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]]
  :dev-dependencies [
                     ; required for tests
                     [postgresql/postgresql "9.1-901.jdbc4"]
                     [com.h2database/h2 "1.4.187"]]
  :main ^:skip-aot clj-slack-sql.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

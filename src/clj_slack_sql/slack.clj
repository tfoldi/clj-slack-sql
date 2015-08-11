(ns clj-slack-sql.slack
  (:require [clojure.tools.logging :as log])
  (:require [clj-slack.chat :as chat]))


(defn get-connection
  "Creates slack connection handle using config or environment variables"
  [config]
  (log/debug "Building slack connection")
  {:api-url  "https://slack.com/api"
   :token    (or (System/getenv "SLACK_TOKEN") (get-in config [:slack :token]))
   :username (get-in config [:slack :username])})

(defn- format-sql-result
  "```(clj-slack-sql.slack/format-sql-result '({1 1 2 2} {1 1 2 3}) )\n
    => \"1: 1,2: 2\\n1: 1,2: 3\"```
  "
  [result]
  (clojure.string/join \newline
                       (map (fn [x]
                              (str
                                (clojure.string/join "|"
                                                     (map
                                                       #(str (name (first %)) ": " (second %))
                                                       x)))
                              ) result)))

(defn- post-message-for-one-database
  "Post results for one database to its predefined slack channels"
  [slack-connection db-results]
  (log/info "Posting result" db-results)
  (doall
    (map (fn [statement-res]
           (log/info "Posting statement result" statement-res)

           (chat/post-message slack-connection (get statement-res :channel)
                              (apply str
                                     "*" (get statement-res :dbname) "*: "
                                     (get statement-res :name)
                                     \newline ">>>" ; TODO: colorize block quote sign
                                     (if (get statement-res :error)
                                       (str
                                         "ERROR: when executing query:"
                                         (get statement-res :error))
                                       (format-sql-result (get statement-res :results))))
                              {:username (get slack-connection :username)}))
         db-results)))


(defn post-messages
  [slack-connection results]
  (map #(post-message-for-one-database slack-connection %) results))

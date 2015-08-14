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

(defn- format-message
  "Format slack notification message"
  [statement-res]
  (apply str
         "*" (get statement-res :dbname) "*: "
         (get statement-res :name)
         \newline ">>>"                                     ; TODO: colorize blockquote sign
         (if (get statement-res :error)
           (str
             "ERROR: when executing query:"
             (get statement-res :error))
           (format-sql-result (get statement-res :results)))))

(defn- post-message-for-one-database
  "Post results for one database to its predefined slack channels"
  [slack-connection db-results]
  (log/debug "Posting result" db-results)
  (doall
    (map (fn [statement-res]
           (when (or (get statement-res :error) (not-empty (get statement-res :results)))
             (log/info "Posting statement result: " statement-res)

             (try
               (chat/post-message slack-connection
                                  (get statement-res :channel)
                                  (format-message statement-res)
                                  {:username (get slack-connection :username)})
               (catch Exception ex (log/error "post-message-for-one-database" ex)))))
         db-results)))


(defn post-messages
  [slack-connection results]
  (map #(post-message-for-one-database slack-connection %) results))

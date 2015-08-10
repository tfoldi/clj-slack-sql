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

(defn post-message
  [slack-connection results]
  (map (fn [result]
         (log/info "Posting result" result)
         (chat/post-message slack-connection "#test" (apply str result) {:username (get slack-connection :username)})
         )
       results))

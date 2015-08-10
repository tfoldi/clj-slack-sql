; This is where the story begins
(ns clj-slack-sql.core
  (:require [clojure.tools.logging :as log])
  (:require [clj-slack-sql.db :as db])
  (:require [clojure.java.jdbc :as jdbc]) ; TODO: remove me
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clj-slack-sql.config :as config])
  (:gen-class))


; default config file
(def config-file "config/statements.yml")

; command line options as clojure.tools.cli required
(def cli-options
  ; you can specify alternate config file with the `--config` or `-c-`
  ; parameter
  [["-c" "--config FILE" "Config file for Slack SQL"
    :default config-file
    :validate [#(.exists (clojure.java.io/as-file %)) "Config file does not exists"]]
   ; Show show to help `-h` or `--help
   ["-h" "--help"]])

(defn validate-opts
  [options]
  (if (get options :errors)
    (throw (ex-info "Error when validating command line options"
                    {:errors (get options :errors)})))
  options)

(defn -main
  "TODO: doc"
  [& args]
  (try
    (let [opts (validate-opts (parse-opts args cli-options))
          config (config/parse-config-file
                   (get-in opts [:options :config]))
          db-map (db/create-connection-pool-map config)
          sleep-time (* 1000
                         (or (get config :poll_interval)
                             10))]

      (log/debug "opts & config: " opts config)

      (log/info "Starting up Application's main loop")
      (loop [prev_cycle_start (System/currentTimeMillis)
             current_cycle_start (System/currentTimeMillis)
             cycle_counter 1]

        (log/info "Starting cycle " cycle_counter)
        (log/info
          (db/execute-queries config db-map))


        (log/info "Cycle finished, sleeping " (/ sleep-time 1000) " seconds... ")
        (Thread/sleep sleep-time)

        ; wake up, neo
        (recur current_cycle_start (System/currentTimeMillis) (inc cycle_counter))))

    (catch Exception ex
      ; TODO: please format meeeee
      (print "Error during execution" (ex-data ex))
      (log/error ex))))


; This is where the story begins
(ns clj-slack-sql.core
  (:require [clojure.tools.logging :as log])
  (:require [clj-slack-sql.db :as db])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clj-slack-sql.config :as config])
  (:require [clj-slack-sql.slack :as slack])
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
   ["-n" "--num-cycles NUMBER" "Number of cycles to perform"
    :default nil
    :parse-fn #(Integer/parseInt %)]
   ; Show show to help `-h` or `--help
   ["-h" "--help"]])

(defn validate-opts
  [options]
  (if (get options :errors)
    (throw (ex-info "Error when validating command line options"
                    {:errors (get options :errors)})))
  options)


(defn- sleep
  "Sleep at least 0 seconds, ideally sleep-time minus current the cycle's duration"
  [cycle-start sleep-time]
  (Thread/sleep (max (- sleep-time (- (System/currentTimeMillis) cycle-start)) 0)))

(defn -main
  "TODO: doc"
  [& args]
  (try
    (let [opts (validate-opts (parse-opts args cli-options))
          config (config/parse-config-file
                   (get-in opts [:options :config]))
          num-cycles (get-in opts [:options :num-cycles])
          db-map (db/create-connection-pool-map config)
          slack-connection (slack/get-connection config)
          sleep-time (* 1000
                        (or (get config :poll_interval)
                            10))]

      (log/debug "opts & config: " opts config)

      (log/info "Starting up Application's main loop")
      ; loop forever. During every cycle we will update the `prev_cycle_start`
      ; and `current_cycle_start` values. In the first cycle these values are
      ; equal.
      (loop [prev-cycle-start (- (System/currentTimeMillis) sleep-time)
             current-cycle-start (System/currentTimeMillis)
             cycle-counter 1]

        (log/info "Starting cycle " cycle-counter)

        (doall
          (slack/post-messages slack-connection
                               (db/execute-queries config db-map [prev-cycle-start current-cycle-start])))

        (log/info "Cycle finished, sleeping")
        (sleep current-cycle-start sleep-time)

        ; After waiting sleep time we can proceed with the next cycle.
        ; if max cycle is defined then stop when necessary
        (when (or (not num-cycles) (> num-cycles cycle-counter))
          (recur current-cycle-start (System/currentTimeMillis) (inc cycle-counter)))))

    (catch Exception ex
      ; TODO: please format meeeee
      (print "Error during execution" (ex-data ex))
      (log/error ex))))


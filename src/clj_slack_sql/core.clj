; This is where the story begins
(ns clj-slack-sql.core
  (:require [clojure.tools.logging :as log])
  (:require [clj-slack-sql.config :as config])
  (:gen-class))


; default config file
(def config-file "config/statements.yml" )

; At the moment
;
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [config-file (config/parse-config-file config-file)]
    (log/info config-file)))

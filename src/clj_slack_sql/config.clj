(ns clj-slack-sql.config
  (:require [clojure.tools.logging :as log])
  (:require [clj-yaml.core :as yaml]))

; throw exception in any case of issues
(defn check-config-sanity
  [config]
  ; TODO: add sanity and error checks
  config)

(defn parse-config-file
  [file]
  (->>
    (slurp file)
    (yaml/parse-string)
    (check-config-sanity)))

(ns clj-slack-sql.db
  (:require [clojure.tools.logging :as log])
  (:require [clojure.java.jdbc :as jdbc])
  (:require [jdbc.pool.c3p0 :as pool])
  (:import (java.util Date)))

(defn create-connection-pool-map
  [config]
  "Get dbinfo object from configuration file
  the return hash-map will contain database name and dbinfo pairs
  containing `:connection-uri`, `:user` and `:password` encapsulated into
  a c3p0 database pool descriptor"
  (log/info "Building database connection pools")
  (into {}
        (map (fn [dbnode]
               ; key is database name
               [(get dbnode :name)
                ; value is a c3p0 datasource-dbspec
                (pool/make-datasource-spec (select-keys dbnode [:connection-uri :user :password]))])
             (get config :databases))))


(defn- map-or-pmap
  "if yes then pmap otherwise map"
  [multithread?]
  (if (= multithread? "yes")
    pmap
    map))

(defn- replace-several [content & replacements]
  (let [replacement-list (partition 2 replacements)]
    (reduce #(apply clojure.string/replace %1 %2) content replacement-list)))

(defn- get-iso-date
  [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (Date. date)))

(defn- get-iso-datetime
  [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZZZZZ") (Date. date)))

(defn- get-iso-timestamp
  [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SZZZZZ") (Date. date)))

(defn- replace-interval-in-query
  [statement [prev_cycle_start current_cycle_start]]
  (replace-several statement
                   "%WINDOW_START_DATE%" (get-iso-date prev_cycle_start)
                   "%WINDOW_END_DATE%" (get-iso-date current_cycle_start)
                   "%WINDOW_START_DATETIME%" (get-iso-datetime prev_cycle_start)
                   "%WINDOW_END_DATETIME%" (get-iso-datetime current_cycle_start)
                   "%WINDOW_START_TIMESTAMP%" (get-iso-timestamp prev_cycle_start)
                   "%WINDOW_END_TIMESTAMP%" (get-iso-timestamp current_cycle_start)))

(defn- prepare-and-execute-statement
  [db-info statement interval]
  (jdbc/query db-info (replace-interval-in-query statement interval)) )

(defn execute-queries-for-database
  [config interval dbnode db-info]
  (let [mymap (map-or-pmap (config get :multithread))]
    ; for each database we will call
    (mymap (fn [statement_node]
             (log/debug " db-info " db-info " statement " statement_node)
             (merge {:dbname (get dbnode :name)}
                    ; location and configuration information
                    (select-keys statement_node [:group :channel :name :type])
                    ; add eighter :results with map of SQL return or :error with error information
                    (try
                      {:results
                       (prepare-and-execute-statement db-info (get statement_node :query) interval)}
                      ; Bad luck, save the exception
                      (catch Exception ex
                        {:error (.getMessage ex)}))))
           (get dbnode :statements))))

(defn execute-queries
  "Execute queries in all databases in-parallel"
  [config db-map interval]
    (pmap
      (fn [dbnode]
        (execute-queries-for-database config interval dbnode (get db-map (get dbnode :name))))
      (get config :databases)))
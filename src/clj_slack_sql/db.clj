(ns clj-slack-sql.db
  (:require [clojure.tools.logging :as log])
  (:require [clojure.java.jdbc :as jdbc])
  (:require [jdbc.pool.c3p0 :as pool]))

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

(defn execute-queries-for-database
  [config dbnode db-info]
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
                       (jdbc/query db-info (get statement_node :query))}
                      ; Bad luck, save the exception
                      (catch Exception ex
                        {:error (.getMessage ex)}))))
           (get dbnode :statements))))

(defn execute-queries
  "Execute queries in all databases in-parallel"
  [config db-map]
    (pmap
      (fn [dbnode]
        (execute-queries-for-database config dbnode (get db-map (get dbnode :name))))
      (get config :databases)))
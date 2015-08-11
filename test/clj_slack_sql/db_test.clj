(ns clj-slack-sql.db-test
  (:require [clojure.test :refer :all]
            [clj-slack-sql.db :refer :all]))

(def start_epoch 1439276286595)
(def end_epoch 1439278741954)                               ; "2015-08-11T07:39:01.954-00:00"
(def start_date (#'clj-slack-sql.db/get-iso-date start_epoch))
(def start_datetime (#'clj-slack-sql.db/get-iso-datetime start_epoch))
(def start_timestampt (#'clj-slack-sql.db/get-iso-timestamp start_epoch))
(def end_date (#'clj-slack-sql.db/get-iso-date end_epoch))
(def end_datetime (#'clj-slack-sql.db/get-iso-datetime end_epoch))
(def end_timestampt (#'clj-slack-sql.db/get-iso-timestamp end_epoch))

(deftest date-and-sql-parsing
  ; testing private functions are ugly
  (testing "replace several key val pairs in a string"
    (is (= (#'clj-slack-sql.db/replace-several "abcdef" "b" "BB" "c" "CC") "aBBCCdef"))
    (is (= (#'clj-slack-sql.db/replace-several "abcdef" "g" "GG" "c" "CC") "abCCdef")))
  (testing "search and replace of statements"
    ; simple replace
    (is (= (#'clj-slack-sql.db/replace-interval-in-query "FOO %WINDOW_START_DATE% BAR" [start_epoch end_epoch])
           (str "FOO " start_date " BAR")))
    ; test all template
    (is (= (#'clj-slack-sql.db/replace-interval-in-query (str "FOO %WINDOW_START_DATE%%WINDOW_START_DATETIME%"
                                                              "%WINDOW_START_TIMESTAMP%%WINDOW_END_DATE%"
                                                              "%WINDOW_END_DATETIME%%WINDOW_END_TIMESTAMP% BAR")
             [start_epoch end_epoch])
           (str "FOO " start_date start_datetime start_timestampt end_date end_datetime end_timestampt " BAR")))
    ))
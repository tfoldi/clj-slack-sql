(ns clj-slack-sql.db-test
  (:require [clojure.test :refer :all]
            [clj-slack-sql.db :refer :all]))

(def start_epoch 1439276286595)
(def end_epoch 1439278741954) ; "2015-08-11T07:39:01.954-00:00"

(deftest date-and-sql-parsing
  ; testing private functions are ugly
  (testing "basic date formats"
    (is (= (#'clj-slack-sql.db/get-iso-date start_epoch )"2015-08-11"))
    (is (= (#'clj-slack-sql.db/get-iso-datetime start_epoch) "2015-08-11T08:58:06"))
    (is (= (#'clj-slack-sql.db/get-iso-timestamp start_epoch) "2015-08-11T08:58:06.595")))
  (testing "replace several key val pairs in a string"
    (is (= (#'clj-slack-sql.db/replace-several "abcdef" "b" "BB" "c" "CC") "aBBCCdef"))
    (is (= (#'clj-slack-sql.db/replace-several "abcdef" "g" "GG" "c" "CC") "abCCdef")))
  (testing "search and replace of statements"

    ))


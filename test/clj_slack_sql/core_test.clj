(ns clj-slack-sql.core-test
  (:require [clojure.test :refer :all]
            [clj-slack-sql.slack]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-slack-sql.core :refer :all]))

(deftest validate-main-function
  (testing "command line parsing"
    (is (= (get-in (validate-opts (parse-opts [] cli-options)) [:options :config]) "config/statements.yml"))
    (is (= (get-in (validate-opts (parse-opts [] cli-options)) [:options :num-cycles]) nil))
    (is (= (get-in (validate-opts (parse-opts ["-c" "config/statements.yml"] cli-options)) [:options :config])
           "config/statements.yml"))
    (is (= (get-in (validate-opts (parse-opts ["--config" "config/statements.yml"] cli-options)) [:options :config])
           "config/statements.yml"))
    (is (= (get-in (validate-opts (parse-opts ["-n" "123"] cli-options)) [:options :num-cycles]) 123))
    (is (= (get-in (validate-opts (parse-opts ["--num-cycles" "123"] cli-options)) [:options :num-cycles]) 123))
    (is (validate-opts (parse-opts ["--config" "config/statements.yml"] cli-options)))
    (is (thrown? Throwable (validate-opts (parse-opts ["--n" "non-integer"] cli-options))))
    (is (thrown? Throwable (validate-opts (parse-opts ["--config" "config/non_existent.yml"] cli-options))))))



;(deftest functional-end-to-end-test
;  (testing "running single SQL with one parameter"
;    (#'clj-slack-sql.core/-main "-c" "test\\clj_slack_sql\\single_sql.yml" "-n" "1")))
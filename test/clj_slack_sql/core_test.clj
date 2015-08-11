(ns clj-slack-sql.core-test
  (:require [clojure.test :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-slack-sql.core :refer :all]))

(deftest validate-main-function
  (testing "command line parsing"
    (is (validate-opts (parse-opts [] cli-options)))
    (is (validate-opts (parse-opts ["-c" "config/statements.yml"] cli-options)))
    (is (validate-opts (parse-opts ["--config" "config/statements.yml"] cli-options)))
    (is (thrown? Throwable (validate-opts (parse-opts ["--config" "config/non_existent.yml"] cli-options))))))

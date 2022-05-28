(ns caiorulli.vega.reaction-test
  (:require [caiorulli.vega.test-helpers :as test]
            [clojure.test :refer [deftest is testing]]))

(deftest reaction-test
  (test/with-context
    (test/execute! "/reaction \"trigger sentence\" \"reaction sentence\""
                   "In the middle of a sentence, trigger sentence..."
                   "/reaction_list")

    (let [[result-msg
           reaction-msg
           list-msg] (test/requests)]

      (testing "adds reaction successfully"
        (is (= "Reaction added successfully." result-msg)))

      (testing "reacts appropriately"
        (is (= "reaction sentence" reaction-msg)))

      (testing "list available reactions"
        (is (= "Registered reactions:
\"lala\" => \"lalala\"
\"this is the way\" => \"This is the way.\"
\"trigger sentence\" => \"reaction sentence\"
"
               list-msg))))))

(ns vega.reaction-test
  (:require [clojure.test :refer [deftest is testing]]
            [vega.test-helpers :refer [vega-process]]))

(deftest reaction-test
  (let [[result-msg
         reaction-msg
         list-msg]
        (vega-process "/reaction \"trigger sentence\" \"reaction sentence\""
                      "In the middle of a sentence, trigger sentence..."
                      "/reaction_list")]

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
             list-msg)))))

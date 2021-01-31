(ns vega.reaction-test
  (:require [clojure.test :refer [deftest is testing]]
            [vega.test-helpers :refer [vega-process]]))

(deftest reaction-test
  (let [[result-msg
         reaction-msg]
        (vega-process "/reaction \"trigger sentence\" \"reaction sentence\""
                      "In the middle of a sentence, trigger sentence...")]

    (testing "adds reaction successfully"
      (is (= "Reaction added successfully." result-msg)))

    (testing "reacts appropriately"
      (is (= "reaction sentence" reaction-msg)))))

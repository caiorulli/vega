(ns caiorulli.vega.reaction-test
  (:require [caiorulli.vega.domain :as domain]
            [caiorulli.vega.test-helpers :as test]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]))

(defn- react-command
  [{::domain/keys [trigger sentence]}]
  (str "/reaction \"" trigger "\" \"" sentence "\""))

(defspec reaction-contains-defined-sentence 10
  (prop/for-all [reaction (s/gen ::domain/reaction)]
    (test/with-context
      (let [addition-res (test/exec! (react-command reaction))]
        ;; (println addition-res)
        (= addition-res "Reaction added successfully.")

        (test/reset-requests!)

        (let [{::domain/keys [trigger sentence]} reaction
              trigger-res (test/exec! trigger)]
          (println (str trigger "/" sentence))
          (println trigger-res)
          (= sentence trigger-res))))))

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

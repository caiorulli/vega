(ns caiorulli.vega.reaction-test
  (:require [caiorulli.vega.domain :as domain]
            [caiorulli.vega.test-helpers :as test]
            [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]))

(defn- react-command
  [{::domain/keys [trigger sentence]}]
  (str "/reaction \"" trigger "\" \"" sentence "\""))

(defspec reaction-contains-defined-sentence 10
  (prop/for-all [reaction (s/gen ::domain/reaction)]
    (test/with-context
      (let [addition-res (test/exec! (react-command reaction))]

        (test/reset-requests!)

        (let [{::domain/keys [trigger sentence]} reaction
              trigger-res (test/exec! trigger)]
          (and
           (= addition-res "Reaction added successfully.")
           (= sentence trigger-res)))))))

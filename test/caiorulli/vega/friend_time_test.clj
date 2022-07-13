(ns caiorulli.vega.friend-time-test
  (:require [caiorulli.vega.domain :as domain]
            [caiorulli.vega.test-helpers :as test]
            [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [datahike.api :as d]
            [medley.core :refer [map-keys]]))

(defn- with-ns
  [ns k]
  (keyword ns (name k)))

(defspec friend-time 100
  (prop/for-all [friend (s/gen ::domain/friend)]
    (test/with-context
      (d/transact (d/connect test/db-config)
                  [(map-keys (partial with-ns "friend") friend)])

      (let [res (test/exec! (str "/time " (::domain/name friend)))]
        (re-matches #"\d{2}:\d{2}" res)))))

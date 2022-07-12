(ns caiorulli.vega.friend-time-test
  (:require [caiorulli.vega.domain.friend :as friend]
            [caiorulli.vega.test-helpers :as test]
            [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [datahike.api :as d]
            [medley.core :refer [map-keys]]))

(defn- friend-ns->friend
  [k]
  (keyword "friend" (name k)))

(defspec friend-time 100
  (prop/for-all [friend (s/gen ::friend/friend)]
    (test/with-context
      (d/transact (d/connect test/db-config)
                  [(map-keys friend-ns->friend friend)])

      (let [res (test/exec! (str "/time " (::friend/name friend)))]
        (re-matches #"\d{2}:\d{2}" res)))))

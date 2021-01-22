(ns vega.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [integrant.core :as ig]
            [java-time :as t]
            [vega.commands :refer [now friend-time default-zone]]
            [vega.core :refer [config]]
            vega.test-mocks))

(def current-time (t/zoned-date-time (t/local-date-time 2021 1 16 19 0)
                                     default-zone))

(def ^:private test-config
  (update-in config
             [:db/setup :store]
             assoc
             :backend :mem
             :id      "test-vegadb"))

(deftest friend-time-test
  (with-redefs [now (constantly current-time)]

    (let [{:db/keys [setup]} (ig/init test-config)]

      (testing "Find friends in Brazil"
        (is (= "19:00" (friend-time setup "caio")))
        (is (= "19:00" (friend-time setup "bruno"))))

      (testing "Find friends in Portugal"
        (is (= "22:00" (friend-time setup "thiago"))))

      (testing "Find friends in Germany"
        (is (= "23:00" (friend-time setup "pedrotti")))))))

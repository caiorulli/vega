(ns vega.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [java-time :as t]
            [vega.core :refer [now friend-time]]))

(def current-time (t/local-date-time 2021 1 14 1 19))

(deftest friend-time-test
  (with-redefs [now (constantly current-time)]

    (testing "Find friends in Brazil"
      (is (= "2021-01-14T01:19" (friend-time "caio")))
      (is (= "2021-01-14T01:19" (friend-time "bruno"))))

    (testing "Find friends in Portugal"
      (is (= "2021-01-14T04:19" (friend-time "thiago"))))

    (testing "Find friends in Germany"
      (is (= "2021-01-14T05:19" (friend-time "pedrotti"))))))

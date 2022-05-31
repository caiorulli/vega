(ns caiorulli.vega.friend-time-test
  (:require [caiorulli.vega.test-helpers :as test]
            [caiorulli.vega.utils :refer [now default-zone]]
            [clojure.test :refer [deftest is testing]]
            [java-time :as t]))

(def current-time (t/zoned-date-time (t/local-date-time 2021 1 16 19 0)
                                     default-zone))

(deftest friend-time-test
  (test/with-context
    (with-redefs [now (constantly current-time)]

      (test/execute! "/time caio"
                     "/time bruno"
                     "/time thiago"
                     "/time pedrotti")

      (let [[caio-msg
             bruno-msg
             thiago-msg
             pedrotti-msg] (test/requests)]

          (testing "Find friends in Brazil"
            (is (= "19:00" caio-msg))
            (is (= "19:00" bruno-msg)))

          (testing "Find friends in Portugal"
            (is (= "22:00" thiago-msg)))

          (testing "Find friends in Germany"
            (is (= "23:00" pedrotti-msg)))))))

(ns vega.friend-time-test
  (:require [clojure.test :refer [deftest is testing]]
            [java-time :as t]
            [vega.commands :refer [now default-zone]]
            [vega.test-helpers :refer [vega-process]]))

(def current-time (t/zoned-date-time (t/local-date-time 2021 1 16 19 0)
                                     default-zone))

(deftest friend-time-test
  (with-redefs [now (constantly current-time)]

    (let [[caio-msg
           bruno-msg
           thiago-msg
           pedrotti-msg]
          (vega-process "/time caio"
                        "/time bruno"
                        "/time thiago"
                        "/time pedrotti")]

      (testing "Find friends in Brazil"
        (is (= "19:00" caio-msg))
        (is (= "19:00" bruno-msg)))

      (testing "Find friends in Portugal"
        (is (= "22:00" thiago-msg)))

      (testing "Find friends in Germany"
        (is (= "23:00" pedrotti-msg))))))

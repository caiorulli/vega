(ns caiorulli.vega.friend-time-test
  (:require [caiorulli.vega.core :refer [now default-zone]]
            [caiorulli.vega.test-helpers :as test]
            [clojure.core.async :refer [chan onto-chan!! <!!]]
            [clojure.test :refer [deftest is testing]]
            [java-time :as t]))

(def current-time (t/zoned-date-time (t/local-date-time 2021 1 16 19 0)
                                     default-zone))

(deftest friend-time-test
  (test/with-context
    (with-redefs [now (constantly current-time)]

      (let [producer (chan)
            consumer (test/consumer producer)]
        (onto-chan!! producer [{:text "/time caio"
                                :chat 1}
                               {:text "/time bruno"
                                :chat 1}
                               {:text "/time thiago"
                                :chat 1}
                               {:text "/time pedrotti"
                                :chat 1}])
        (<!! consumer))

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

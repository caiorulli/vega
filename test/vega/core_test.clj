(ns vega.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [java-time :as t]
            [vega.core :refer [now friend-time default-zone setup-db]]))

(def current-time (t/zoned-date-time (t/local-date-time 2021 1 16 19 0)
                                     default-zone))

(deftest friend-time-test
  (with-redefs [now (constantly current-time)]

    (setup-db)

    (testing "Find friends in Brazil"
      (is (= "19:00" (friend-time "caio")))
      (is (= "19:00" (friend-time "bruno"))))

    (testing "Find friends in Portugal"
      (is (= "22:00" (friend-time "thiago"))))

    (testing "Find friends in Germany"
      (is (= "23:00" (friend-time "pedrotti"))))))

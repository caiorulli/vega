(ns caiorulli.vega.reddit-test
  (:require [caiorulli.vega.test-helpers :as test]
            [clj-http.fake :as fake]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]))

(def fake-requests
  {"https://reddit.com/r/wallpaper.rss"
   (constantly {:status 200
                :body   (slurp (io/resource "test/wallpaper.rss"))})})

(def ^:private expected-urls
  #{"https://i.redd.it/g63xqkjk5ni61.jpg"
    "https://i.redd.it/cdk216i2umi61.gif"
    "https://i.redd.it/ndndhg2fdii61.jpg"
    "https://i.redd.it/skbvzdkr6ni61.jpg"
    "https://i.redd.it/53k1nnc5tmi61.png"
    "https://i.redd.it/pr5jhhmsbpi61.jpg"
    "https://i.redd.it/43xrnakluoi61.jpg"
    "https://i.redd.it/zfihul8yxoi61.jpg"
    "https://i.redd.it/8s9qn1kptmi61.jpg"
    "https://i.redd.it/3l8pahjewki61.jpg"
    "https://i.redd.it/ysyxjx9yzhi61.png"
    "https://i.redd.it/p4fae6jjvki61.jpg"
    "https://i.redd.it/q5kg9567vmi61.jpg"
    "https://i.redd.it/irdnz4eltmi61.jpg"
    "https://i.redd.it/y8ab3yqvtmi61.jpg"
    "https://i.redd.it/q1z4btcirji61.jpg"
    "https://i.redd.it/qfpzxwvildi61.jpg"
    "https://www.reddit.com/gallery/lo3vax"
    "https://i.redd.it/wtoqsk0usli61.png"
    "https://i.redd.it/9ldop1mxnni61.png"
    "https://i.redd.it/ctla5nsz6li61.jpg"
    "https://i.redd.it/4zupwnigyli61.jpg"
    "https://i.redd.it/nzrtao5hxni61.jpg"
    "https://i.redd.it/clr3kjm5qfi61.jpg"})

(deftest reddit-test
  (test/with-context
    (fake/with-fake-routes-in-isolation fake-requests
      (test/execute! "/reddit"
                     "/reddit wallpaper 4"))

    (let [[url & urls] (test/requests)]

      (testing "URL is correctly extracted"
        (is (contains? expected-urls url)))

      (testing "Will fire as many urls as required"
        (is (= (count urls) 4))
        (is (set/subset? (set urls) expected-urls))))))

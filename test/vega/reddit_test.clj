(ns vega.reddit-test
  (:require [clojure.test :refer [deftest is testing]]
            [vega.test-helpers :refer [vega-process]]))

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
  (let [[url]
        (vega-process "/reddit")]

    (testing "URL is correctly extracted"
      (is (contains? expected-urls url)))))

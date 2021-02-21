(ns vega.infrastructure.reddit
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [vega.core :refer [try-get]]
            [vega.protocols.reddit :as reddit]))

(def ^:const initial-backoff 1000)
(def ^:const maximum-backoff 8000)
(def ^:const backoff-growth-rate 2)

(defn- -rss
  [{:keys [rss-cache]} subreddit]
  (if-let [cached-feed (get @rss-cache subreddit)]
    (do
      (timbre/info "Using cached rss feed.")
      cached-feed)

    (let [url (str "https://reddit.com/r/" subreddit ".rss")]
      (loop [result  (try-get url)
             backoff initial-backoff]

        (if (seq result)
          (let [feed [(:body result)]]
            (swap! rss-cache #(assoc % subreddit feed))
            feed)

          (do
            (timbre/warn "Retrying: fetch rss feed")
            (Thread/sleep backoff)
            (recur (try-get url)
                   (max maximum-backoff
                        (* backoff-growth-rate backoff)))))))))

(defrecord RedditHttpApi [rss-cache]
  reddit/RedditApi
  (rss [this subreddit]
    (-rss this subreddit)))

(defmethod ig/init-key :reddit/api [_ _]
  (->RedditHttpApi (atom {})))

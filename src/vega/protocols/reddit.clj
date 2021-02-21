(ns vega.protocols.reddit)

(defprotocol RedditApi
  (rss [api subreddit]))

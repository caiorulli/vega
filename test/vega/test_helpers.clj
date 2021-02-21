(ns vega.test-helpers
  (:require [clojure.core.async :refer [<!! >!! chan close!]]
            [datahike.api :as d]
            [integrant.core :as ig]
            vega.consumer
            [vega.core :refer [config]]
            vega.infrastructure.db
            [vega.protocols.reddit :as reddit]
            [vega.protocols.telegram :as telegram]
            [clojure.java.io :as io]))

(defrecord MorseMockApi [requests]
  telegram/TelegramApi
  (send-text [_this _chat-id text]
    (swap! requests conj text))

  (send-photo [_this _chat-id url _caption]
    (swap! requests conj url)))

(defmethod ig/init-key :telegram/api [_ _]
  (->MorseMockApi (atom [])))

(defrecord RedditMockApi []
  reddit/RedditApi
  (rss [_this _subreddit]
    (slurp (io/resource "test/wallpaper.rss"))))

(defmethod ig/init-key :reddit/api [_ _]
  (->RedditMockApi))

(defmethod ig/init-key :telegram/producer [_ _]
  (chan 4))

(defmethod ig/halt-key! :telegram/producer [_ producer]
  (close! producer))

(defmethod ig/halt-key! :db/setup [_ db-setup]
  (d/delete-database db-setup))

(def ^:private test-config
  (update-in config
             [:db/setup :store]
             assoc
             :backend :mem
             :id      "test-vegadb"))

(defn vega-process
  [& messages]
  (let [{producer :telegram/producer
         api      :telegram/api
         consumer :core/consumer
         :as      system} (ig/init test-config)]

    (doseq [message messages]
      (>!! producer {:message {:text message :chat {:id 1}}})
      (<!! consumer))

    (ig/halt! system)

    @(:requests api)))

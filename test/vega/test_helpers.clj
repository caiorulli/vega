(ns vega.test-helpers
  (:require [clojure.core.async :refer [<!! >!! chan]]
            [clojure.java.io :as io]
            [datahike.api :as d]
            [integrant.core :as ig]
            vega.consumer
            vega.producer
            [vega.core :refer [config]]
            vega.infrastructure.db
            [vega.protocols.error-reporting :as error-reporting]
            [vega.protocols.reddit :as reddit]
            [vega.protocols.telegram :as telegram]))

(defrecord MorseMockApi [requests]
  telegram/TelegramApi
  (send-text [_this _chat-id text]
    (swap! requests conj text))

  (send-photo [_this _chat-id url _caption]
    (swap! requests conj url))

  (get-updates [_this _opts]
    (chan)))

(defmethod ig/init-key :telegram/api [_ _]
  (->MorseMockApi (atom [])))

(defrecord RedditMockApi []
  reddit/RedditApi
  (rss [_this _subreddit]
    (slurp (io/resource "test/wallpaper.rss"))))

(defmethod ig/init-key :reddit/api [_ _]
  (->RedditMockApi))

(defrecord SentryMockReporting []
  error-reporting/ErrorReporting
  (init! [_] nil)
  (send-event [_ _] nil))

(defmethod ig/init-key :etc/error-reporting [_ _]
  (->SentryMockReporting))

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
  (let [{api      :telegram/api
         producer :core/producer
         consumer :core/consumer
         :as      system} (ig/init test-config)]

    (doseq [message messages]
      (>!! (:updates-chan producer) {:message {:text message :chat {:id 1}}})
      (<!! consumer))

    (ig/halt! system)

    @(:requests api)))

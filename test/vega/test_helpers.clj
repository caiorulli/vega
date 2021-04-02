(ns vega.test-helpers
  (:require [clojure.core.async :refer [<!!]]
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

(defrecord MorseMockApi [requests responses]
  telegram/TelegramApi
  (send-text [_this _chat-id text]
    (swap! requests conj text))

  (send-photo [_this _chat-id url _caption]
    (swap! requests conj url))

  (get-updates [_this _opts]
    (let [return @responses]
      (reset! responses [])
      return)))

(defmethod ig/init-key :telegram/api [_ {:keys [responses]}]
  (->MorseMockApi (atom []) (atom responses)))

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

(defn- test-config
  [responses]
  (-> config
      (update-in [:db/setup :store]
                 assoc
                 :backend :mem
                 :id      "test-vegadb")
      (assoc-in [:telegram/api :responses]
                responses)
      (assoc-in [:core/producer :timeout]
                1)))

(defn vega-process
  [& messages]
  (let [updates (map (fn [message]
                       {:message   {:text message
                                    :chat {:id 1}}
                        :update_id 1})
                     messages)

        {api      :telegram/api
         consumer :core/consumer
         :as      system} (ig/init (test-config updates))]

    (doseq [_ (range 0 (count updates))]
      (<!! consumer))

    (ig/halt! system)

    @(:requests api)))

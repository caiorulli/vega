(ns vega.test-helpers
  (:require [clojure.core.async :refer [<!! >!! chan]]
            [integrant.core :as ig]
            [vega.core :refer [config]]
            vega.infrastructure.db
            [vega.protocols.telegram :as telegram]))

(defrecord MorseMockApi [requests]
  telegram/TelegramApi
  (send-text [_this chat-id text]
    (swap! requests conj [chat-id text])))

(defmethod ig/init-key :telegram/api [_ _]
  (->MorseMockApi (atom [])))

(defmethod ig/init-key :telegram/producer [_ _]
  (chan 4))

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
         consumer :core/consumer} (ig/init test-config)]

    (doseq [message messages]
      (>!! producer message)
      (<!! consumer))

    @(:requests api)))

(ns vega.test-mocks
  (:require [clojure.core.async :refer [chan]]
            [integrant.core :as ig]
            [vega.protocols.telegram :as telegram]))

(defrecord MorseMockApi [requests]
  telegram/TelegramApi
  (send-text [_this chat-id text]
    (swap! requests conj [chat-id text])))

(defmethod ig/init-key :telegram/api [_ _]
  (->MorseMockApi (atom [])))

(defmethod ig/init-key :telegram/producer [_ _]
  (chan))

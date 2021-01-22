(ns vega.infrastructure.telegram
  (:require [clojure.string :as s]
            [clojure.core.async :refer [close!]]
            [integrant.core :as ig]
            [morse.api :as api]
            [morse.polling :as polling]
            [taoensso.timbre :as timbre]
            [vega.protocols.telegram :as telegram]))

(defrecord MorseApi [token]

  telegram/TelegramApi
  (send-text [this chat-id text]
    (api/send-text (:token this) chat-id text)))

(defmethod ig/init-key :telegram/api [_ {:keys [token]}]
  (when (s/blank? token)
    (timbre/info "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (->MorseApi token))

(defmethod ig/init-key :telegram/producer [_ {:keys [runtime token opts]}]
  (when (s/blank? token)
    (timbre/info "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (timbre/info "Starting Vega...")
  (polling/create-producer runtime token opts))

(defmethod ig/halt-key! :telegram/producer [_ {:keys [runtime]}]
  (close! runtime))

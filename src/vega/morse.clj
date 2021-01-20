(ns vega.morse
  (:require [clojure.string :as s]
            [integrant.core :as ig]
            [morse.api :as api]
            [morse.polling :as polling]
            [taoensso.timbre :as timbre]
            [vega.protocols.api :as protocols.api]))

(defrecord MorseApi [token]

  protocols.api/TelegramApi
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

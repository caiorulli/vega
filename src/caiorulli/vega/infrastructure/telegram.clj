(ns caiorulli.vega.infrastructure.telegram
  (:require [caiorulli.vega.protocols.telegram :as telegram]
            [caiorulli.vega.utils :refer [blurp!]]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [integrant.core :as ig]
            [morse.api :as api]
            [taoensso.timbre :as log]))

(defrecord MorseApi [token limit timeout]

  telegram/TelegramApi
  (send-text [this chat-id text]
    (api/send-text (:token this) chat-id text))

  (send-photo [this chat-id url caption]
    (let [file (blurp! url)]

      (api/send-photo (:token this) chat-id
                      {:caption caption}
                      file)
      (io/delete-file file))))

(defmethod ig/init-key :telegram/api [_ {:keys [token limit timeout]}]
  (when (s/blank? token)
    (log/info "Please provide token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (->MorseApi token limit timeout))

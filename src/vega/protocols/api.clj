(ns vega.protocols.api)

(defprotocol TelegramApi
  (send-text [api chat-id text]))

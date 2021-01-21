(ns vega.protocols.telegram)

(defprotocol TelegramApi
  (send-text [api chat-id text]))

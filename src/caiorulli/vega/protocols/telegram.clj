(ns caiorulli.vega.protocols.telegram)

(defprotocol TelegramApi
  (send-text [api chat-id text])
  (send-photo [api chat-id url caption]))

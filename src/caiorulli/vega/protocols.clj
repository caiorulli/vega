(ns caiorulli.vega.protocols)

(defprotocol ErrorReporting
  (init! [this])
  (send-event [this event]))

(defprotocol TelegramApi
  (send-text [api chat-id text])
  (send-photo [api chat-id url caption]))

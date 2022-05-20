(ns caiorulli.vega.protocols.error-reporting)

(defprotocol ErrorReporting
  (init! [this])
  (send-event [this event]))

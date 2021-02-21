(ns vega.infrastructure.sentry
  (:require [sentry-clj.core :as sentry]
            [vega.protocols.error-reporting :as error-reporting]
            [integrant.core :as ig]))

(defn- -send-event [event]
  (sentry/send-event event))

(defrecord SentryReporting [dsn]

  error-reporting/ErrorReporting
  (init! [{:keys [dsn]}]
    (sentry/init! dsn))

  (send-event [_this event]
    (-send-event event)))

(defmethod ig/init-key :etc/error-reporting [_ {:keys [dsn]}]
  (let [sentry (->SentryReporting dsn)]
    (error-reporting/init! sentry)
    sentry))

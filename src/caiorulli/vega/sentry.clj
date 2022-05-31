(ns caiorulli.vega.sentry
  (:require [caiorulli.vega.protocols :as protocols]
            [integrant.core :as ig]
            [sentry-clj.core :as sentry]
            [taoensso.timbre :as log]))

(defn- -send-event [event]
  (log/error "Uncaught exception" (:throwable event))
  (sentry/send-event event))

(defn- -init!
  [dsn]
  (sentry/init! dsn)

  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ _thread t]
       (-send-event {:message   "Uncaught exception"
                     :throwable t})))))

(defrecord SentryReporting [dsn]

  protocols/ErrorReporting
  (init! [{:keys [dsn]}]
    (-init! dsn))

  (send-event [_this event]
    (-send-event event)))

(defmethod ig/init-key ::error-reporting [_ {:keys [dsn]}]
  (let [sentry (->SentryReporting dsn)]
    (protocols/init! sentry)
    sentry))

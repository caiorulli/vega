(ns vega.container
  (:require [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            vega.consumer
            [vega.core :refer [config]]
            vega.infrastructure.db
            vega.infrastructure.sentry
            vega.infrastructure.telegram
            vega.producer)
  (:gen-class))

(defn -main
  []
  (ig/init config)
  (while true
    (timbre/info "Vega is running. Don't panic.")
    (Thread/sleep 10000)))

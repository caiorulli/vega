(ns caiorulli.vega.container
  (:require caiorulli.vega.consumer
            [caiorulli.vega.core :refer [config]]
            caiorulli.vega.infrastructure.db
            caiorulli.vega.infrastructure.sentry
            caiorulli.vega.infrastructure.telegram
            caiorulli.vega.producer
            caiorulli.vega.scheduler
            [integrant.core :as ig]
            [taoensso.timbre :as log])
  (:gen-class))

(defn -main
  []
  (ig/init config)
  (while true
    (log/info "Vega is running. Don't panic.")
    (Thread/sleep 10000)))

(ns vega.container
  (:require [integrant.core :as ig]
            vega.consumer
            [vega.core :refer [config]]
            vega.infrastructure.db
            vega.infrastructure.reddit
            vega.infrastructure.sentry
            vega.infrastructure.telegram
            vega.producer)
  (:gen-class))

(defn -main
  []
  (ig/init config))

(ns caiorulli.vega.container
  (:require caiorulli.vega.consumer
            [caiorulli.vega.core :refer [config]]
            caiorulli.vega.infrastructure.db
            caiorulli.vega.infrastructure.sentry
            caiorulli.vega.infrastructure.telegram
            caiorulli.vega.producer
            caiorulli.vega.scheduler
            [clojure.core.async :refer [<!!]]
            [integrant.core :as ig])
  (:gen-class))

(defn -main
  []
  (let [{consumer :core/consumer} (ig/init config)]
    (<!! consumer)))

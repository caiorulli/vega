(ns user
  (:require caiorulli.vega.consumer
            [caiorulli.vega.core :refer [config]]
            caiorulli.vega.infrastructure.db
            caiorulli.vega.infrastructure.sentry
            caiorulli.vega.infrastructure.telegram
            [integrant.repl :refer [clear go halt prep init reset reset-all]]))

(integrant.repl/set-prep! (constantly config))

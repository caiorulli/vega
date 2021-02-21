(ns vega.container
  (:require [clojure.core.async :refer [<!!]]
            [integrant.core :as ig]
            vega.consumer
            [vega.core :refer [config]]
            vega.infrastructure.db
            vega.infrastructure.reddit
            vega.infrastructure.sentry
            vega.infrastructure.telegram)
  (:gen-class))

(defn -main
  []
  (let [{:core/keys [runtime]} (ig/init config)]
    (<!! runtime)))

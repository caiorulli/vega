(ns vega.container
  (:require [clojure.core.async :refer [<!!]]
            [integrant.core :as ig]
            vega.consumer
            [vega.core :refer [config]]
            vega.infrastructure.db
            vega.infrastructure.telegram)
  (:gen-class))

(defn start [_]
  (let [{:core/keys [runtime]} (ig/init config)]
    (<!! runtime)))

(defn -main
  []
  (start {}))

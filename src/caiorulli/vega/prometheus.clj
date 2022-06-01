(ns caiorulli.vega.prometheus
  (:require [iapetos.collector.jvm :as jvm]
            [iapetos.core :as prometheus]
            [iapetos.standalone :as standalone]
            [integrant.core :as ig]))

(defn create-registry
  []
  (-> (prometheus/collector-registry)
      (jvm/initialize)))

(defmethod ig/init-key ::registry [_ _]
  (create-registry))

(defmethod ig/init-key ::webserver [_ {:keys [registry]}]
  (standalone/metrics-server registry {:port 8080}))

(defmethod ig/halt-key! ::webserver [_ handle]
  (.close handle))

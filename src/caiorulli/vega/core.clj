(ns caiorulli.vega.core
  (:require [caiorulli.vega.consumer :as consumer]
            [caiorulli.vega.datahike :as db]
            [caiorulli.vega.producer :as producer]
            [caiorulli.vega.scheduler :as scheduler]
            [caiorulli.vega.sentry :as sentry]
            [caiorulli.vega.telegram :as telegram]
            [clojure.core.async :refer [<!!]]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [taoensso.timbre :as log])
  (:gen-class))

(def ^:const datahike-path
  "/var/lib/datahike")

(def config
  {::consumer/worker {:api      (ig/ref ::telegram/api)
                      :db-setup (ig/ref ::db/setup)
                      :producer (ig/ref ::producer/worker)}

   ::producer/worker {:token           (env :telegram-token)
                      :error-reporting (ig/ref ::sentry/error-reporting)
                      :scheduler       (ig/ref ::scheduler/worker)}

   ::scheduler/worker {:recurrence 1}

   ::telegram/api {:token   (env :telegram-token)
                   :limit   100
                   :timeout 1}

   ::db/setup {:store      {:backend :file
                            :path    datahike-path}
               :initial-tx (edn/read-string (slurp (io/resource "schema.edn")))
               :name       "vegadb"}

   ::sentry/error-reporting {:dsn (env :sentry-dsn)}

   ::logging {:level (keyword (env :log-level))}})

(defmethod ig/init-key ::logging [_ {:keys [level]
                                        :or   {level :info}}]
  (log/set-level! level))

(defn -main
  []
  (let [{consumer ::consumer/worker} (ig/init config)]
    (<!! consumer)))

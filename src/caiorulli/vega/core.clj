(ns caiorulli.vega.core
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(def ^:const datahike-path
  "/var/lib/datahike")

(def config
  {:core/consumer {:api             (ig/ref :telegram/api)
                   :db-setup        (ig/ref :db/setup)
                   :producer        (ig/ref :core/producer)}

   :core/producer {:token           (env :telegram-token)
                   :error-reporting (ig/ref :etc/error-reporting)
                   :scheduler       (ig/ref :core/scheduler)}

   :core/scheduler {:recurrence 1}

   :telegram/api {:token   (env :telegram-token)
                  :limit   100
                  :timeout 1}

   :db/setup {:store      {:backend :file
                           :path    datahike-path}
              :initial-tx (edn/read-string (slurp (io/resource "schema.edn")))
              :name       "vegadb"}

   :etc/logging         {:level (keyword (env :log-level))}
   :etc/error-reporting {:dsn (env :sentry-dsn)}})

(defmethod ig/init-key :etc/logging [_ {:keys [level]
                                        :or   {level :info}}]
  (log/set-level! level))


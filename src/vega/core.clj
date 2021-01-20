(ns vega.core
  (:require [clojure.core.async :refer [<!! chan]]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            vega.consumer
            vega.db
            vega.morse)
  (:gen-class))

(def config
  {:core/runtime  {}
   :core/consumer {:api      (ig/ref :telegram/api)
                   :db-setup (ig/ref :db/setup)
                   :producer (ig/ref :telegram/producer)}

   :telegram/producer {:token   (env :telegram-token)
                       :runtime (ig/ref :core/runtime)
                       :opts    {:timeout 4}}

   :telegram/api {:token (env :telegram-token)}

   :db/setup {:store      {:backend :file
                           :path    "/tmp/vegadb"}
              :initial-tx [{:db/ident       :friend/name
                            :db/valueType   :db.type/string
                            :db/cardinality :db.cardinality/one}
                           {:db/ident       :friend/zone-id
                            :db/valueType   :db.type/string
                            :db/cardinality :db.cardinality/one}]
              :name       "vegadb"}

   :etc/logging {:level (keyword (env :log-level))}})

(defmethod ig/init-key :core/runtime [_ _]
  (chan))

(defmethod ig/init-key :etc/logging [_ {:keys [level]
                                        :or   {level :info}}]
  (timbre/set-level! level))

(defn start [_]
  (let [{:core/keys [runtime]} (ig/init config)]
    (<!! runtime)))

(defn -main
  []
  (start {}))

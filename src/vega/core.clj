(ns vega.core
  (:require [clojure.core.async :refer [<!! chan]]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [morse.handlers :refer [handlers command-fn message-fn]]
            [morse.polling :as polling]
            [taoensso.timbre :as timbre]
            [vega.commands :as commands]
            vega.infrastructure.db
            vega.infrastructure.telegram
            [vega.interceptors :as interceptors])
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

(defn start-consumer [api db-setup producer-chan]
  (polling/create-consumer
   producer-chan
   (handlers
    (command-fn "start" (partial commands/start api db-setup))
    (command-fn "help" (partial commands/help api db-setup))
    (command-fn "time" (partial commands/time-command api db-setup))
    (message-fn (partial interceptors/reaction-interceptor api db-setup))
    (message-fn (partial interceptors/default-interceptor api db-setup)))))

(defmethod ig/init-key :core/consumer [_ {:keys [api db-setup producer]}]
  (start-consumer api db-setup producer))

(defn start [_]
  (let [{:core/keys [runtime]} (ig/init config)]
    (<!! runtime)))

(defn -main
  []
  (start {}))

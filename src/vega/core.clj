(ns vega.core
  (:require [clojure.core.async :refer [<! >! chan go-loop sliding-buffer close!]]
            [clojure.tools.reader.edn :as edn]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [morse.handlers :refer [handlers command-fn message-fn]]
            [taoensso.timbre :as timbre]
            [vega.commands :as commands]
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
              :initial-tx (edn/read-string (slurp "resources/schema.edn"))
              :name       "vegadb"}

   :etc/logging {:level (keyword (env :log-level))}})

(defmethod ig/init-key :core/runtime [_ _]
  (chan))

(defmethod ig/halt-key! :core/runtime [_ runtime]
  (close! runtime))

(defmethod ig/init-key :etc/logging [_ {:keys [level]
                                        :or   {level :info}}]
  (timbre/set-level! level))

(defn start-consumer
  "Modified morse consumer to support completion report through
  next-chan, which is useful for testing."

  [api db-setup producer-chan]
  (let [handler
        (handlers
         (command-fn "start" (partial commands/start api db-setup))
         (command-fn "help" (partial commands/help api db-setup))
         (command-fn "time" (partial commands/time-command api db-setup))
         (command-fn "reaction" (partial commands/reaction api db-setup))
         (message-fn (partial interceptors/reaction api db-setup))
         (message-fn (partial interceptors/default api db-setup)))

        next-chan (chan (sliding-buffer 1))]

    (go-loop []

      (when-let [message (<! producer-chan)]

        (try
          (handler message)
          (>! next-chan message)
          (catch Throwable t
            (timbre/error "Error processing message" message t)))

        (recur)))

    next-chan))

(defmethod ig/init-key :core/consumer [_ {:keys [api db-setup producer]}]
  (start-consumer api db-setup producer))

(defmethod ig/halt-key! :core/consumer [_ consumer]
  (close! consumer))

(ns caiorulli.vega.consumer
  (:require [caiorulli.vega.commands :as commands]
            [caiorulli.vega.interceptors :as interceptors]
            [caiorulli.vega.protocols.error-reporting :as error-reporting]
            [clojure.core.async :refer [<! go-loop]]
            [integrant.core :as ig]
            [morse.handlers :refer [handlers command-fn message-fn]]
            [taoensso.timbre :as log]))

(defn create
  [api db-setup error-reporting producer]
  (let [handle!
        (handlers
         (command-fn "start" (partial commands/start api db-setup))
         (command-fn "help" (partial commands/help api db-setup))
         (command-fn "time" (partial commands/time-command api db-setup))
         (command-fn "reaction" (partial commands/reaction api db-setup))
         (command-fn "reaction_list" (partial commands/reaction-list api db-setup))
         (command-fn "reddit" (partial commands/reddit api db-setup))
         (message-fn (partial interceptors/reaction api db-setup))
         (message-fn (partial interceptors/default api db-setup)))]

    (go-loop []
      (when-let [message (<! producer)]
        (try
          (handler message)
          (catch Throwable t
            (error-reporting/send-event error-reporting
                                        {:message   "Error processing message"
                                         :extra     message
                                         :throwable t})
            (log/error "Error processing message" message t)))

        (recur)))))

(defmethod ig/init-key :core/consumer [_ {:keys [api
                                                 db-setup
                                                 producer
                                                 error-reporting]}]
  (create api db-setup error-reporting producer))

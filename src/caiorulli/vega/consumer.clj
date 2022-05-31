(ns caiorulli.vega.consumer
  (:require [caiorulli.vega.commands :as commands]
            [caiorulli.vega.interceptors :as interceptors]
            [clojure.core.async :refer [<! go-loop]]
            [integrant.core :as ig]
            [morse.handlers :refer [handlers command-fn message-fn]]
            [taoensso.timbre :as log]))

(defn create
  [api db-setup producer]
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
      (if-let [message (<! producer)]
        (do
          (log/debug (str "Handling message: " message))
          (handle! message)
          (recur))

        (log/warn "Consumer shutting down.")))))

(defmethod ig/init-key :core/consumer [_ {:keys [api
                                                 db-setup
                                                 producer]}]
  (create api db-setup producer))

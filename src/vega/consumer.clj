(ns vega.consumer
  (:require [clojure.core.async :refer [<! >! chan go-loop sliding-buffer close!]]
            [integrant.core :as ig]
            [morse.handlers :refer [handlers command-fn message-fn]]
            [taoensso.timbre :as timbre]
            [vega.commands :as commands]
            [vega.interceptors :as interceptors]))

(defn start-consumer
  "Modified morse consumer to support completion report through
  next-chan, which is useful for testing."

  [api db-setup reddit-api producer-chan]
  (let [handler
        (handlers
         (command-fn "start" (partial commands/start api db-setup))
         (command-fn "help" (partial commands/help api db-setup))
         (command-fn "time" (partial commands/time-command api db-setup))
         (command-fn "reaction" (partial commands/reaction api db-setup))
         (command-fn "reaction_list" (partial commands/reaction-list api db-setup))
         (command-fn "reddit" (partial commands/reddit api db-setup reddit-api))
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

(defmethod ig/init-key :core/consumer [_ {:keys [api
                                                 db-setup
                                                 reddit-api
                                                 producer]}]
  (start-consumer api db-setup reddit-api producer))

(defmethod ig/halt-key! :core/consumer [_ consumer]
  (close! consumer))
